package game;

import java.awt.Color;
import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by Alexander on 01/12/2016.
 */
public class BlockGrid {

	private final int height;
	private final int width;

	private final Block[][] layout;

	public BlockGrid(int rows, int cols){
		this.height = rows;
		this.width = cols;
		Block[] row = new Block[cols];
		Arrays.fill(row, Block.EMPTY);

		Block[][] layout = new Block[rows][];
		for (int i = 0; i < rows; i++)
			layout[i] = row.clone();
		this.layout = layout;
	}
	public BlockGrid(int width, Color color, int...values){
		if(values.length%width != 0); //TODO Exception
		Block filled = new Block(color);
		height = values.length/width;
		this.width = width;
		Block[][] layout = new Block[height][];

		for (int i = 0; i < height; i++) {
			layout[i] = IntStream.range(i*width,(i+1)*width).
					map(n->values[n]).
					mapToObj(n->n==0 ? Block.EMPTY : filled).
					toArray(l->new Block[l]);
		}
		this.layout = layout;

	}
public BlockGrid(int width, Block[] blocks){
		if(blocks.length%width != 0); //TODO Exception
		height = blocks.length/width;
		this.width = width;
		Block[][] layout = new Block[height][];

		for (int i = 0; i < height; i++) {
			layout[i] = IntStream.range(i*width,(i+1)*width).
					mapToObj(n->blocks[n]).
					toArray(l->new Block[l]);
		}
		this.layout = layout;

	}

	private  BlockGrid(Block[][] layout){
		height = layout.length;
		width = layout[0].length;
		this.layout = layout;
	}



	public BlockGrid rotateClockwise(){
		Block newBlock[][] = new Block[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				newBlock[i][j] = layout[height -1 - j][i];
			}
		}
		return new BlockGrid( newBlock);
	}

	public BlockGrid rotateAntiClockwise(){
		BlockGrid b = this.rotateClockwise();
		b = b.rotateClockwise();
		return  b.rotateClockwise();
	}

	public BlockGrid rotate(int n){
		BlockGrid b = this;
		for (int i = 0; i < n%4; i++)
			b = b.rotateClockwise();
		return b;
	}

	public boolean contains(int row, int col){
		return row >= 0 && row < height && col >= 0 && col < width;
	}

	private void setBlock(int row, int col, Block block){
		if (!contains(row, col)); //TODO Exception
		layout[row][col] = block;
	}

	public Block getBlock(int row, int col){
		if (!contains(row, col)); //TODO Exception
		return layout[row][col];
	}

	public Block[] getRegion(int row, int col, int heght, int width){
		if((contains(row, col) && contains(row+heght,col+width))); // TODO: exception
		return  IntStream.range(row, heght).boxed().
				flatMap(i->IntStream.range(col, width).mapToObj(j->getBlock(i,j))).toArray(l->new Block[l]);
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public Block[][] getLayout() {
		return Stream.of(layout).map(ba->ba.clone()).toArray(l->new Block[l][]);
	}

	public static class Edit extends BlockGrid{
		public Edit(BlockGrid b){
			super(b.layout);

		}

		public void setBlock(int row, int col, Block block){
			super.setBlock(row, col, block);
		}

		public boolean pieceFits(int row, int col, BlockGrid piece){
			if (contains(row, col) && contains(row + piece.getHeight()-1, col + piece.getWidth()-1)) {
				BlockGrid.Block[] flatPiece = piece.getRegion(0, 0, piece.getHeight(), piece.getWidth());
				BlockGrid.Block[] gridRegion = getRegion(row, col, row + piece.getHeight(), col + piece.getWidth());

				return IntStream.range(0, flatPiece.length).allMatch(i->(flatPiece[i].isEmpty() || gridRegion[i].isEmpty()));
			}
			else return false;
		}

		public void drawPiece(int row, int col, BlockGrid piece, boolean ignoreBlanks){
			BlockGrid.Block[] flatPiece = piece.getRegion(0, 0, piece.getHeight(), piece.getWidth());
			for (int i = 0; i < piece.height; i++) {
				for (int j = 0; j < piece.width; j++) {
					if(contains(row+i,col+j) && !(ignoreBlanks && piece.getBlock(i,j).isEmpty()))
						setBlock(row+i,col+j,piece.getBlock(i,j));
				}
			}
		}

		public BlockGrid getCopy(){
			BlockGrid b = this;
			return new BlockGrid(Stream.of(b.layout).map(ba->ba.clone()).toArray(l->new Block[l][]));
		}
	}

	public static class Block{
		public static final Block EMPTY = new Block();
		private final Color color;
		private final boolean empty;


		public Block(Color color){
			this.color = color;
			empty = false;
		}
		private Block(){
			color = new Color(0,0,0,0);
			empty = true;
		}

		public Color getColor() {
			return color;
		}

		public boolean isEmpty(){
			return empty;
		}
	}
}
