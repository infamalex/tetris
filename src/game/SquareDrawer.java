package game;

import java.awt.*;

/**
 * Created by Alexander on 07/12/2016.
 */
public interface SquareDrawer {
	default void drawBlock(GridView.JBlock jBlock, Graphics g){
		drawSquare(jBlock.getWidth(), jBlock.getHeight(),jBlock.getBlock(),g);
	}

	void drawSquare(int width, int height, BlockGrid.Block block, Graphics g);

	class BeveledDrawer implements SquareDrawer{
		private int width = 0,height =0;
		private Polygon top,bottom;

		private void calculate(int width, int height){
			top = new Polygon(new int[]{0, width,0},new int[]{0,0, height},3);
			bottom = new Polygon(new int[]{0, width, width},new int[]{height, height,0},3);
		}
		@Override
		public void drawSquare(int width, int height, BlockGrid.Block block, Graphics g) {
			if (block.isEmpty())return;
			if(this.width != width || this.height != height){
				calculate(width, height);
				this.width =width;
				this.height = height;
			}
			Color color2 = block.getColor();
				g.setColor(color2);
				g.fillPolygon(top);
				g.setColor(color2.darker().darker());
				g.fillPolygon(bottom);
				g.setColor(color2.darker());
				g.fillRect(width /5, height /5,6* width /10,6* height /10);

		}
	}

	class FillDrawer implements SquareDrawer{


		@Override
		public void drawSquare(int width, int height, BlockGrid.Block block, Graphics g) {
			g.setColor(block.getColor());
			g.fillRect(0,0,width,height);

		}
	}

	class TranlucentDrawer implements SquareDrawer{

		private final SquareDrawer primaryDrawer;
		private final int alpha;

		public TranlucentDrawer(SquareDrawer sd, int alpha){
			this.primaryDrawer = sd;
			this.alpha = alpha;
		}

		@Override
		public void drawSquare(int width, int height, BlockGrid.Block block, Graphics g) {
			if (!block.isEmpty()){
				Color temp = block.getColor();
				block = new BlockGrid.Block(new Color(temp.getRGB()&0xFFFFFF | alpha<<24,true));
			}
			primaryDrawer.drawSquare(width, height, block, g);
		}
	}
}
