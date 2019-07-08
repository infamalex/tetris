package game;

import java.awt.*;
import java.util.*;
import java.util.function.Predicate;

/**
 * Created by Alexander on 06/12/2016.
 */
public enum TetrisPiece{
	L_BLOCK(2,Color.BLUE,
			1,0,
			1,0,
			1,1),
	FLIPPED_L_BLOCK(2,Color.GREEN,
			0,1,
			0,1,
			1,1),
	S_BLOCK(3,Color.RED,
			0,1,1,
			1,1,0),
	FLIPPED_S_BLOCK(3, new Color(255,170,255),
			1,1,0,
			0,1,1),
	T_BLOCK(3,Color.MAGENTA,
			1,1,1,
			0,1,0),
	SQUARE_BLOCK(2,Color.YELLOW,
			1,1,
			1,1),
	I_BLOCK(1,Color.CYAN,
			1,1,1,1);

	private final BlockGrid piece; //Blockgrid for Tetris piece

	private TetrisPiece(int cols, Color color, int...values){
		this.piece = new BlockGrid(cols,color,values);
	}


	public BlockGrid getPiece() {
		return piece;
	}

	/**
	 * Gets an iterator that produces an infinite sequence of random tetris pieces in a random orientation.
	 * Then same block will not appear again for at least 5 iterations.
	 * @param seed Seed used to generate the sequence
	 * @param pieces Array of blocks to select from
	 * @return An iterator over the sequence
	 */
	private static Iterator<BlockGrid> semiRandomPieces(long seed, TetrisPiece...pieces){
		Random generator = new Random(seed);
		Deque<TetrisPiece> deque = new LinkedList<>();
		Predicate<TetrisPiece> notRecentlyUsed = piece->{//filters blocks if they appeared less than 5 iterations ago
			if (deque.contains(piece))
				return false;
			else{
				if (deque.size() >= 5)
					deque.removeFirst();
				deque.add(piece);
				return true;
			}
		};
		return generator.ints(0,pieces.length).
				mapToObj(i->pieces[i]).filter(notRecentlyUsed).
				map(TetrisPiece::getPiece).
				map(b->b.rotate(generator.nextInt(4))).iterator();

	}

	/**
	 * Gets an iterator that produces an infinite sequence of random tetris pieces in a random orientation.
	 * @param pieces Array of blocks to select from
	 * @return An iterator over the sequence
	 */
	public static Iterator<BlockGrid> randomPieces(TetrisPiece...pieces){
		long seed = (long) (Long.MAX_VALUE * Math.random());
		Random generator = new Random(seed);
		return generator.ints(0,pieces.length).
				mapToObj(i->pieces[i]).
				map(TetrisPiece::getPiece).
				map(b->b.rotate(generator.nextInt(4))).iterator();
	}

	/**
	 * Gets an iterator that produces an infinite sequence of random tetris pieces in a random orientation.
	 * Then same block will not appear again for at least 5 iterations.
	 * @param seed Seed used to generate the sequence
	 * @return An iterator over the sequence
	 */
	public static Iterator<BlockGrid> semiRandomPieces(long seed){
		return semiRandomPieces(seed,TetrisPiece.values());
	}

	/**
	 * Gets an iterator that produces an infinite sequence of random tetris pieces in a random orientation based on a
	 * random seed.
	 * Then same block will not appear again for at least 5 iterations.
	 * @return An iterator over the sequence
	 */
	public static Iterator<BlockGrid> semiRandomPieces(){
		long seed = (long) (Long.MAX_VALUE * Math.random());
		return semiRandomPieces(seed,TetrisPiece.values());
	}
}
