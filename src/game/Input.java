package game;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;

import static java.awt.event.KeyEvent.*;
import static java.awt.event.MouseEvent.*;

/**
 * Created by Alexander on 23/11/2016.
 */
public abstract class Input implements KeyListener, MouseListener{

	private final HashSet<Integer> pressedKeyboard;
	private final HashSet<Integer> pressedMouse;

	Input(){
		this.pressedKeyboard = new HashSet<>();
		this.pressedMouse = new HashSet<>();
	}

	abstract void forKey(int keyCode);
	abstract void forMouseKey(int keyCode);
	@Override
	public void keyTyped(KeyEvent e) {}

	/**
	 * receive keyboard events and execute all currently pressed
	 * @param e
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		pressedKeyboard.add(e.getKeyCode());
		pressedKeyboard.forEach(this::forKey);
	}
	@Override
	public void keyReleased(KeyEvent e) {
		pressedKeyboard.remove(e.getKeyCode());
	}

	/**
	 * receive mouse events and execute all currently pressed
	 * @param e
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		pressedMouse.add(e.getButton());
		pressedMouse.forEach(this::forMouseKey);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressedMouse.remove(e.getButton());
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	public static class KeyboardInput extends Input{

		private TetrisView.Controls controls;

		public KeyboardInput(TetrisView.Controls controls) {
			this.controls = controls;
		}

		@Override
		void forKey(int keyCode) {
			try {
				controls.getLock().lock();
				switch (keyCode) {
					case VK_RIGHT:
						if(!super.pressedKeyboard.contains(VK_LEFT))
						controls.moveRight();
						break;
					case VK_LEFT:
						if(!super.pressedKeyboard.contains(VK_RIGHT))
						controls.moveLeft();
						break;
					case VK_DOWN:
						controls.moveDown();
						break;
					case VK_SPACE:
						controls.rotateClockwise();
				}
			}
			finally {
				controls.getLock().unlock();
			}
		}

		@Override
		void forMouseKey(int keyCode) {

		}
	}
	public static class MouseInput extends Input{
		private TetrisView.Controls controls;

		public MouseInput(TetrisView.Controls controls) {
			this.controls = controls;
		}
		@Override
		void forKey(int keyCode) {

		}

		@Override
		void forMouseKey(int keyCode) {
			try {
				controls.getLock().lock();
				switch (keyCode) {
					case BUTTON1:
						if(!super.pressedMouse.contains(BUTTON3))
							controls.moveLeft();
						break;
					case BUTTON3:
						if(!super.pressedMouse.contains(BUTTON1))
							controls.moveRight();
						break;
					case MOUSE_WHEEL:
						controls.moveDown();
						break;
					case BUTTON2:
						controls.rotateClockwise();
				}
			}
			finally {
				controls.getLock().unlock();
			}
		}
	}
}
