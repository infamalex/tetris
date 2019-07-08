package game;

import javax.swing.*;
import java.awt.*;

/**
 * Created by Alexander on 05/12/2016.
 */
public class GridView extends JPanel{

	private final BlockGrid.Edit buffer; //Stores the permanent Blocks in Grid
	private final JBlock[][] displayGrid; //A grid of blocks that rpresent what is currently being displayed
	private final SquareDrawer defaultDrawer; //The method used for drawing blocks in the grid unless specified otherwise


	public GridView(int rows, int cols, SquareDrawer drawer){
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new GridLayout(rows, cols));
		setBackground(Color.black);
		this.defaultDrawer = drawer;
		buffer = new BlockGrid.Edit(new BlockGrid(rows, cols));

		displayGrid = new JBlock[rows][cols];
		Dimension size = new Dimension(30,30);
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				JBlock jblock = new JBlock(drawer);
				jblock.setPreferredSize(size);
				displayGrid[i][j] = jblock;
				add(jblock);
			}
		}
	}

	//Setters-----------------------------------------------------------------------------------------------------------
	public void setBlocks(int row, int col, BlockGrid piece){
		buffer.drawPiece(row,col,piece, true);
	}

	public void setBlocks(int row, int col, BlockGrid piece, boolean ignorBlanks){
		buffer.drawPiece(row,col,piece, ignorBlanks);
	}

	public void setBlockSize(int px){
		Dimension d = new Dimension(px,px);
		for (int i = 0; i < buffer.getHeight(); i++) {
			for (int j = 0; j < buffer.getWidth(); j++) {
				displayGrid[i][j].setPreferredSize(d);
			}
		}
	}

	//Setters-----------------------------------------------------------------------------------------------------------

	public BlockGrid.Edit getCanvas(){
		return buffer;
	}

	public boolean validPosition(int row, int col, BlockGrid piece){
		return buffer.pieceFits(row, col, piece);
	}

	//Control Methods---------------------------------------------------------------------------------------------------
	public void refresh(){
		for (int i = 0; i < buffer.getHeight(); i++) {
			for (int j = 0; j < buffer.getWidth(); j++) {
					displayGrid[i][j].block =buffer.getBlock(i,j);
				displayGrid[i][j].drawer = defaultDrawer;
			}
		}
	}

	public void drawPiece(int row, int col, BlockGrid piece){
		drawPiece(row, col, piece, defaultDrawer);
	}
	public void drawPiece(int row, int col, BlockGrid piece, SquareDrawer drawer){
		for (int i = 0; i < piece.getHeight(); i++) {
			for (int j = 0; j < piece.getWidth(); j++) {
				if (buffer.contains(row+ i, col +j) && !piece.getBlock(i,j).isEmpty()) {
					displayGrid[row + i][col + j].block = piece.getBlock(i, j);
					displayGrid[row + i][col + j].drawer = drawer;
				}
			}
		}
		repaint();
	}

	//Inner Classes-----------------------------------------------------------------------------------------------------

	public static class JBlock extends JComponent{
		private SquareDrawer drawer;
		private BlockGrid.Block block;

		public JBlock(SquareDrawer drawer){

			this.block = BlockGrid.Block.EMPTY;
			this.drawer = drawer;
		}

		public void setDrawer(SquareDrawer drawer) {
			this.drawer = drawer;
		}

		public void setBlock(BlockGrid.Block block) {
			this.block = block;
		}

		public BlockGrid.Block getBlock() {
			return block;
		}


		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON
			);
			drawer.drawBlock(this, g);
		}
	}
}
