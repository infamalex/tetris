package game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.stream.Stream;

/**
 * Created by Alexander on 11/10/2016.
 * This Class is a Panel that contains the logic required to create a Tetris application.
 * The game can be controled using it's instance variable of type TetrisView.Controler.
 *
 */
public class TetrisView extends JLayeredPane{

	static {System.setProperty("sun.java2d.opengl", "true");}
	public static int ROWS = 20, COLS = 10;
	private static int PLACEMENT_DELAY = 10;
	private static final SquareDrawer
			BLOCK_SHADOW_DRAWER =  new SquareDrawer.TranlucentDrawer(new SquareDrawer.FillDrawer(),90),
			BLOCK_PATH_DRAWER = new SquareDrawer.TranlucentDrawer(new SquareDrawer.FillDrawer(),20);

	//Feilds------------------------------------------------------------------------------------------------------------
	private final ScheduledExecutorService executor; //Executor for scheduling task for this game
	private final Iterator<BlockGrid> blockIterator; //An iterator providing the blocks used in the games
	private final GridView gridView; //The grid used to display the game
	private BlockGrid currentPiece; //The current piece in play
	private BlockGrid nextPiece; //The next piece that will put into play after the current one is placed
	private int colpos; //The starting column for the current block
	private int rowpos; //The starting row for the current block
	private int score;	//The current score
	private int noOfDelays; //Stores the number of times the game delayed placing a block
	private boolean delayingPlacement; //stores whether the game should delay making a blocks placement permenant
	private boolean paused; //stores whether the game is paused
	private boolean started;//stores whether the game has started
	private boolean moveMade; //stores whether the player has made a move since the last time the game tried to place a block
	private boolean gameOver; //stores whether the game is over
	private final Controls controls; //The controls for this game

	private ScheduledFuture fixBlockDelay;
	ReentrantLock windowLock = new ReentrantLock();

	public TetrisView(long seed){
		//Set up window-------------------------------------------------------------------------------------------------
		setLayout(new GridBagLayout());
		addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(contains(e.getX(),e.getY()))
					requestFocus();
			}

			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
		});

		//Create grid to display tetris game----------------------------------------------------------------------------
		gridView = new GridView(ROWS,COLS, new SquareDrawer.BeveledDrawer());
		gridView.setFocusable(true);
		gridView.setOpaque(false);
		gridView.setBlockSize(20);
		gridView.setBackground(new Color(0,0,0,0));

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx =0;
		gbc.gridy = 0;
		add(gridView,gbc);
		setLayer(gridView,0);

		//initialise fields---------------------------------------------------------------------------------------------
		this.controls = new Controls(this);
		blockIterator = TetrisPiece.semiRandomPieces(seed);
		nextPiece = blockIterator.next();
		executor = Executors.newSingleThreadScheduledExecutor();
		delayingPlacement = false;
		paused = false;
		moveMade = false;
		gameOver = false;
	}

	//Getters-----------------------------------------------------------------------------------------------------------
	public int getScore() {
		return score;
	}

	public Controls getControls() {
		return controls;
	}

	public boolean isPaused() {
		return paused;
	}

	public BlockGrid getNextPiece() {
		return nextPiece;
	}

	//Setters

	/**
	 * Adds the input object so it can listen for both mouse and keyboard events.
	 * @param input
	 */
	public void addInput(Input input){
		addKeyListener(input);
		addMouseListener(input);
	}

	//Game functions----------------------------------------------------------------------------------------------------
	/**
	 * Removes all the rows that have no empty spaces and increments the score.
	 * The Score is incremented by 10 points for each row multiplied by the number of rows removed at once.
	 * e.g. if 4 rows are removed at once, the score is incremented by 160
	 */
	public void removeRows(){
		BlockGrid.Edit canvas = gridView.getCanvas();
		BlockGrid.Block[][] layout = canvas.getLayout();
		int length = 0;
		for (int i = 0; i < canvas.getHeight(); i++) {
			if(Stream.of(layout[i]).anyMatch(BlockGrid.Block::isEmpty))
				continue;
			length++;
			System.arraycopy(layout,0,layout,1,i);
			Arrays.fill(layout[0], BlockGrid.Block.EMPTY);
		}
		Iterator<BlockGrid.Block> blocks = Stream.of(layout).flatMap(Stream::of).iterator();
		for (int i = 0; i < canvas.getHeight(); i++) {
			for (int j = 0; j < canvas.getWidth(); j++) {
				canvas.setBlock(i,j,blocks.next());
			}
		}
		score+=length*length*10;
	}

	/**
	 * Attempts to advance the game by moving the current block down one row.<br>
	 *     If the block can't be moved lower, the game will allow the player toe move the block up to 10 times
	 *     before fixing it in place.
	 *     Once a block is fixed in place, the current block is substituted for the next block.
	 *     If the next block can't be placed, the game ends.
	 */
	public void nextMove(){
		if (paused)
			return;
		try {
			windowLock.lock();
			if (!gridView.validPosition(rowpos + 1, colpos, currentPiece)) {
				//puts the next block in play
				if(!delayingPlacement || !moveMade || noOfDelays++ >= PLACEMENT_DELAY){
					gridView.setBlocks(rowpos, colpos, currentPiece);
					removeRows();
					colpos = 0;
					rowpos = 0;
					delayingPlacement = false;
					if(gridView.validPosition(rowpos, colpos, nextPiece)) {
						currentPiece = nextPiece;
						nextPiece = blockIterator.next();
					}
					else {
						gameOver = true;
						controls.pause();
					}
				}
				else
					moveMade = false;
			}
			else {
				rowpos++;
				if (!delayingPlacement && !gridView.validPosition(rowpos + 1, colpos, currentPiece)) {
					delayingPlacement = true;
					noOfDelays = 0;
				}
			}
		}
		finally {
			windowLock.unlock();
		}
	}

	/**
	 * Redraws the current window by updating the position of the tetris piece in play
	 * and it's current path were it to fall straight down
	 */
	public void reDraw(){
		try{
			windowLock.lock();
			if (!gameOver && started) {
				gridView.refresh();
				int tempY = rowpos;
				for (; gridView.validPosition(tempY+1,colpos, currentPiece); tempY++)
					gridView.drawPiece(tempY,colpos, currentPiece, BLOCK_PATH_DRAWER);

				gridView.drawPiece(tempY,colpos, currentPiece, BLOCK_SHADOW_DRAWER);
				gridView.drawPiece(rowpos,colpos, currentPiece);
				this.repaint();
			}
		}catch (Exception e){e.printStackTrace();}
		finally {
			windowLock.unlock();
		}
	}

	/**
	 * Override of the paint component method. This is overridden in order to paint the custom background
	 * @param g
	 */
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(0, 111, 145));
		g.fillRect(0,0,getWidth(),getHeight());
		g.setColor(new Color(0, 66, 88));
		for (int i = 1; i <COLS; i++) { //paints the dotted lines along the y axis
			int x = i*getWidth()/COLS;
			for (int j = 0; j < ROWS; j++) {
				int y = j * getHeight() / ROWS;
				int y2 = (j+1) * getHeight() / ROWS;
				g.drawLine(x, y, x, y+(y2-y)/3);
				g.drawLine(x, y2, x, y2-(y2-y)/3);
			}
		}
		for (int i = 1; i <ROWS; i++) { //paints the dotted lines along the x axis
			int y = i*getHeight()/ROWS;
			for (int j = 0; j < COLS; j++) {
				int x = j * getWidth() / COLS;
				int x2 = (j+1) * getWidth() / COLS;
				g.drawLine(x, y, x+(x2-x)/3, y);
				g.drawLine(x2, y, x2 - (x2-x)/3, y);
			}
		}
	}

	//Inner Classes-----------------------------------------------------------------------------------------------------
	public static class Controls{
		private final TetrisView game; //stores instance of the game
		private final JComponent pauseScreen; //stores the pause/game over information panel
		private final JTextField message; //Text field that stores the message text

		public Controls(TetrisView game){
			this.game = game;
			//Creates pause screen
			pauseScreen = new JPanel(new BorderLayout());
			pauseScreen.setBorder(null);
			message = new JTextField("Paused");
			message.setBackground(new Color(0,0,0,90));
			message.setBorder(null);
			message.setForeground(Color.RED);
			message.setFont(new Font("Trebuchet MS", Font.PLAIN, 24));
			message.setHorizontalAlignment(SwingConstants.CENTER);
			message.setEditable(false);
			message.setFocusable(false);
			pauseScreen.add(message);
			pauseScreen.setBorder(null);
			pauseScreen.setOpaque(false);
		}

		public ReentrantLock getLock() {
			return game.windowLock;
		}

		/**
		 * Attempts to move the current block to the right. Will fail if moving it would cause it to
		 * intersect with an existing block
		 * @return true if the block was moved
		 */
		public boolean moveRight(){
			if(game.paused || !game.gridView.validPosition( game.rowpos, game.colpos +1,game.currentPiece)) {
				return false;
			}
			game.colpos++;
			game.moveMade = true;
			return true;
		}
		/**
		 * Attempts to move the current block to the left. Will fail if moving it would cause it to
		 * intersect with an existing block
		 * @return true if the block was moved
		 */
		public boolean moveLeft(){
			if(game.paused || !game.gridView.validPosition( game.rowpos, game.colpos -1,game.currentPiece))
				return false;
			game.colpos--;
			game.moveMade = true;
			return true;
		}
		/**
		 * Attempts to move the current block down. Will fail if moving it would cause it to
		 * intersect with an existing block
		 * @return true if the block was moved
		 */
		public boolean moveDown(){
			if (game.paused || !game.gridView.validPosition(game.rowpos + 1, game.colpos, game.currentPiece))
				return false;
			game.rowpos++;
			game.moveMade = true;
			return true;
		}
		/**
		 * Attempts to rotate the current block by 90 degrees. Will attempt to move the block to the right if it's
		 * rotation would cause it to intersect with an existing block, but it will only attempt to move it
		 * a distance equal to it's width once rotated
		 * @return true if the block was rotated
		 */
		public boolean rotateClockwise(){
			if(game.paused)
				return false;
			int i = 0;
			BlockGrid temp = game.currentPiece.rotateClockwise();
			for (i = 0; i < temp.getWidth(); i++) {
				if (game.gridView.validPosition( game.rowpos, game.colpos -i,temp))
					break;
			}
			if(i<temp.getWidth()){
				game.colpos -= i;
				game.currentPiece = game.currentPiece.rotateClockwise();
				game.moveMade = true;
				return true;
			}
			return false;
		}

		public boolean start(){
			if(game.started)
				return false;
			game.currentPiece = game.nextPiece;
			game.nextPiece = game.blockIterator.next();
			game.executor.scheduleAtFixedRate(game::nextMove,200,200, TimeUnit.MILLISECONDS);
			game.started = true;
			return true;
		}

		/**
		 * Toggles whether the game is paused. If the game is paused, it will
		 * overlays a translucent panel displaying a message saying paused. If the game is over, the message will read
		 * "Game Over".
		 * If the game is unpaused, it will remove the message unless the game is over
		 *
		 * @return
		 */
		public boolean pause(){
			if(game.paused && !game.gameOver)
				game.remove(pauseScreen);
			else {
				if (game.gameOver)
					message.setText("Game Over");
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.gridx =0;
				gbc.gridy = 0;
				gbc.fill = GridBagConstraints.BOTH;
				game.add(pauseScreen,gbc);
				game.setLayer(pauseScreen,1);
			}
			game.revalidate();
			return game.paused = !game.paused;
		}

	}


}
