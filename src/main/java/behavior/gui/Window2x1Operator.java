package behavior.gui;

import ij.process.ImageProcessor;
import ij.*;
import ij.gui.ImageWindow;

/**
 * 
 * @author Modifier:Butoh
 */
public class Window2x1Operator extends WindowOperator{
	private int[][] imageAlloc;
	protected Window2x1Operator(int allCage, ImageProcessor[] backIp){
		super(allCage, backIp);
	}

	public void setImageWindow(ImagePlus[] imp, int allocation){

		//画像の表示
		int[] windowWidth = new int[2];
		int[] windowHeight = new int[2];
		for(int x = 0; x < allCage; x++){
			imp[x].show();
			ImageWindow win = imp[x].getWindow();
			openWait();
			windowWidth[x] = win.getWidth();
			windowHeight[x] = win.getHeight();
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		//表示位置の計算
		int blockWidth = windowWidth[0] + (allCage == 2? windowWidth[1] : 0) + BLANK;
		int blockHeight = Math.max(windowHeight[0], (allCage == 2? windowHeight[1] : 0));

		initXYWin[WIDTH] = Math.max((blockWidth + BLANK * 2) * 2, XY_WIN_X);
		initXYWin[HEIGHT] = 0;
		infoWinSize[HEIGHT] = INFO_LINE_HEIGHT * allCage + INFO_MIN_HEIGHT;

		initImageWin[WIDTH][0] = 0;
		initImageWin[WIDTH][1] = initImageWin[WIDTH][0] + blockWidth + BLANK * 2;
		initImageWin[HEIGHT][0] = infoWinSize[HEIGHT] + BLANK * 2;
		initImageWin[HEIGHT][1] = initImageWin[HEIGHT][0] + blockHeight + BLANK * 2;

		imageAlloc = new int[2][2];
		imageAlloc[WIDTH][0] = 0;
		imageAlloc[WIDTH][1] = formIp[0].getWidth() + BLANK;
		imageAlloc[HEIGHT][0] = 0;
		imageAlloc[HEIGHT][1] = 0;

		//ウィンドウの整列
		int[] init = getImageAllocation(allocation);
		for(int x = 0; x < allCage; x++){
			ImageWindow win = imp[x].getWindow();
			win.setLocation(init[WIDTH] + imageAlloc[WIDTH][x], init[HEIGHT] + imageAlloc[HEIGHT][0]);
			openWait();
		}
	}

	@Override
	public synchronized void setImageWindow(ImagePlus[] imp, int allocation,	boolean[] activeCage) {
		this.setImageWindow(imp, allocation);
		
	}
}