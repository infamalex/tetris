package game;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public class GameApp extends JFrame{

	static {

		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels())
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
		} catch (Exception e) {}
	}

	private JLayeredPane gameView; //Tbe Tetris window
	private TetrisView.Controls controls; // controls for the tetris game
	private final GridView nextTile; // Panel to display the next block
	private ScheduledExecutorService executor; // Executor to update the window
	private final JButton pause; // button for pausing the current game

	public static void main(String[] args) {
		EventQueue.invokeLater(() -> {
			try {
				GameApp window = new GameApp();
				window.setVisible(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
	private JTextField score;

	public GameApp() {
		//Frame initialisation------------------------------------------------------------------------------------------
		super("My Tetris App");
		setResizable(false);
		setBounds(100, 100, 316, 460);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setUndecorated(true);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.rowHeights = new int[] {0};
		gridBagLayout.columnWidths = new int[] {100, 100, 100};
		gridBagLayout.columnWeights = new double[]{2.0/3, 1.0/3, 0.0};
		gridBagLayout.rowWeights = new double[]{1.0};
		getContentPane().setLayout(gridBagLayout);




		//initialise side bar-------------------------------------------------------------------------------------------
		JPanel sideBar = new JPanel();
		sideBar.setBackground(new Color(0, 191, 255));
		GridBagConstraints gbc_sideBar = new GridBagConstraints();
		gbc_sideBar.insets = new Insets(0, 0, 0, 0);
		gbc_sideBar.gridheight = 1;
		gbc_sideBar.fill = GridBagConstraints.BOTH;
		gbc_sideBar.gridx = 2;
		gbc_sideBar.gridy = 0;

		GridBagLayout gbl_sideBar = new GridBagLayout();
		gbl_sideBar.rowHeights = new int[] {100, 150, 30, 20};
		gbl_sideBar.columnWidths = new int[] {30};
		gbl_sideBar.columnWeights = new double[]{1.0};
		gbl_sideBar.rowWeights = new double[]{1.0, 0.0, 1.0, 1.0};
		sideBar.setLayout(gbl_sideBar);

		getContentPane().add(sideBar, gbc_sideBar);

		//window displaying next piece----------------------------------------------------------------------------------
		{
			JPanel nextBlockPanel = new JPanel();
			nextBlockPanel.setLayout(new BoxLayout(nextBlockPanel, BoxLayout.Y_AXIS));

			JLabel lblNewLabel = new JLabel("Next Block");
			lblNewLabel.setFont(new Font("Trajan Pro", Font.PLAIN, 13));
			lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
			nextBlockPanel.add(lblNewLabel);

			nextTile = new GridView(6, 6, new SquareDrawer.BeveledDrawer());
			nextTile.setBlockSize(5);
			GridBagConstraints gbc_nextTile = new GridBagConstraints();
			gbc_nextTile.fill = GridBagConstraints.BOTH;
			gbc_nextTile.insets = new Insets(0, 0, 5, 0);
			gbc_nextTile.gridx = 0;
			gbc_nextTile.gridy = 0;
			nextBlockPanel.add(nextTile);
			sideBar.add(nextBlockPanel, gbc_nextTile);
		}

		//Apps's Main Controls------------------------------------------------------------------------------------------
		JPanel options = new JPanel();
		GridBagConstraints gbc_options = new GridBagConstraints();
		gbc_options.insets = new Insets(0, 0, 5, 0);
		gbc_options.fill = GridBagConstraints.BOTH;
		gbc_options.gridx = 0;
		gbc_options.gridy = 1;
		sideBar.add(options, gbc_options);
		options.setLayout(new GridLayout(3, 0, 0, 0));

		//pause start button
		pause = new JButton("Pause/Start");
		pause.setFocusable(false);
		pause.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
		options.add(pause);


		JButton newGame = new JButton("New Game");
		setFocusable(false);
		newGame.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
		newGame.addActionListener(e->initialiseTetrisGame());
		options.add(newGame);

		JButton exitApp = new JButton("Quit");
		setFocusable(false);
		exitApp.addActionListener(e->System.exit(0));
		exitApp.setFont(new Font("Trebuchet MS", Font.PLAIN, 12));
		options.add(exitApp);

		JPanel infoPanel = new JPanel();
		GridBagConstraints gbc_infoPanel = new GridBagConstraints();
		gbc_infoPanel.insets = new Insets(0, 0, 5, 0);
		gbc_infoPanel.fill = GridBagConstraints.BOTH;
		gbc_infoPanel.gridx = 0;
		gbc_infoPanel.gridy = 2;
		sideBar.add(infoPanel, gbc_infoPanel);
		infoPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JLabel scoreLabel = new JLabel("SCORE");
		scoreLabel.setBackground(Color.BLUE);
		scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
		scoreLabel.setFont(new Font("Trajan Pro", Font.PLAIN, 13));
		infoPanel.add(scoreLabel);

		score = new JTextField();
		score.setFocusable(false);
		score.setHorizontalAlignment(SwingConstants.CENTER);
		score.setText("000000");
		score.setFont(new Font("Miriam Fixed", Font.PLAIN, 13));
		score.setBackground(Color.LIGHT_GRAY);
		score.setEditable(false);
		infoPanel.add(score);
		score.setColumns(10);

		JPanel namePanel = new JPanel();
		GridBagConstraints gbc_namePanel = new GridBagConstraints();
		gbc_namePanel.fill = GridBagConstraints.BOTH;
		gbc_namePanel.gridx = 0;
		gbc_namePanel.gridy = 3;
		sideBar.add(namePanel, gbc_namePanel);
		namePanel.setLayout(new GridLayout(0, 1, 0, 0));

		JTextArea nameLabel = new JTextArea("Alexander Sequeira");
		nameLabel.setFocusable(false);
		nameLabel.setBackground(Color.LIGHT_GRAY);
		nameLabel.setEditable(false);
		nameLabel.setBorder(null);
		nameLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
		nameLabel.setLineWrap(true);
		nameLabel.setWrapStyleWord(true);
		namePanel.add(nameLabel);

		//Menu Bar------------------------------------------------------------------------------------------------------
		JMenuBar menuBar = new JMenuBar();

		JMenu mnGame = new JMenu("Game");
		menuBar.add(mnGame);

		JMenuItem newGameFromSeed = new JMenuItem("From Seed    ");
		newGameFromSeed.addActionListener(e ->initialiseTetrisGameFromSeed());

		mnGame.add(newGameFromSeed);

		JSeparator separator_1 = new JSeparator();
		mnGame.add(separator_1);
		setJMenuBar(menuBar);

		initialiseTetrisGame();
		validate();
		pack();
	}

	/**
	 * Uses an option pane to get a long value from the user and uses it as a seed for a new Tetris game
	 */
	private void initialiseTetrisGameFromSeed(){
		String input = JOptionPane.showInputDialog(this,"New Game","Enter Seed for new game",JOptionPane.QUESTION_MESSAGE);
		try{
			long seed = Long.parseLong(input);
			EventQueue.invokeLater(()->initialiseTetrisGame(seed));
		}
		catch (Exception e){
			JOptionPane.showMessageDialog(this,"The input was not valid");
		}
	}

	/**
	 * Initialises a new tetris game
	 */
	private void initialiseTetrisGame(){
		initialiseTetrisGame((long)(Math.random()*Long.MAX_VALUE));
	}
	/**
	 * Initialises a new tetris game with a given seed
	 */
	private void initialiseTetrisGame(long seed){
		//create new game
		TetrisView tetrisView = new TetrisView(seed);
		this.controls = tetrisView.getControls();
		tetrisView.addInput(new Input.KeyboardInput(controls));
		tetrisView.addInput(new Input.MouseInput(controls));
		if (gameView != null) {
			remove(gameView);

		}
		if(executor != null )
			executor.shutdownNow();
		executor = Executors.newSingleThreadScheduledExecutor();

		gameView = tetrisView;
		GridBagConstraints gbc_gameView = new GridBagConstraints();
		gbc_gameView.gridwidth = 2;
		gbc_gameView.insets = new Insets(0, 0, 0, 0);
		gbc_gameView.gridheight = 1;
		gbc_gameView.fill = GridBagConstraints.NONE;
		gbc_gameView.gridx = 0;
		gbc_gameView.gridy = 0;
		getContentPane().add(gameView, gbc_gameView);
		revalidate();
		gameView.requestFocus();

		Stream.of(pause.getActionListeners()).forEach(pause::removeActionListener);
		pause.addActionListener(e->{controls.start();controls.pause();tetrisView.requestFocus();});
		gameView.addFocusListener(new FocusListener() {//Pauses game if focus is lost
			@Override
			public void focusGained(FocusEvent fe) {

			}

			@Override
			public void focusLost(FocusEvent fe) {
				try {
					if(!tetrisView.isPaused())
					controls.pause();
				}
				catch (Exception e){}
			}
		});
		controls.pause();

		//adds update methods to the executor---------------------------------------------------------------------------
		executor.scheduleAtFixedRate(tetrisView::reDraw,0,1000/60,TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(()->updateScore(tetrisView.getScore()),0,1000/60,TimeUnit.MILLISECONDS);
		executor.scheduleAtFixedRate(()->updateNextBlock(tetrisView.getNextPiece()),0,1000/60,TimeUnit.MILLISECONDS);
	}

	//display the score from the current game
	public void updateScore(int newScore) {
		String text = String.format("%06d", newScore);
		score.setText(text);
		score.repaint();
	}

	//get the block that is coming up and displays it
	private void updateNextBlock(BlockGrid b){
		int row = (6-b.getHeight())/2;
		int col = (6-b.getWidth())/2;
		nextTile.refresh();
		nextTile.drawPiece(row,col,b);
		nextTile.repaint();
	}
}
