package behavior.gui;

import ij.process.ImageProcessor;
import ij.*;
import ij.gui.ImageWindow;

/**
 * 
 * @author Modifier:Butoh
 */
public class Window3x3Operator extends WindowOperator{
	private int[][] imageAlloc;
	protected Window3x3Operator(int allCage, ImageProcessor[] backIp){
		super(allCage, backIp);
	}

//	protected void setAllocation(){
	/*
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = max(formIp[0].getWidth(), formIp[3].getWidth(), formIp[6].getWidth()) + WINDOW_FRAME_WIDTH;
		eachLineMaxSize[WIDTH][1] = max(formIp[1].getWidth(), formIp[4].getWidth(),
							(allCage >= 8? formIp[7].getWidth() : 0)) + WINDOW_FRAME_WIDTH;
		eachLineMaxSize[WIDTH][2] = max(formIp[2].getWidth(), formIp[5].getWidth(),
							(allCage == 9? formIp[8].getWidth() : 0)) + WINDOW_FRAME_WIDTH;
		eachLineMaxSize[HEIGHT][0] = max(formIp[0].getWidth(), formIp[1].getWidth(), formIp[2].getWidth()) + WINDOW_FRAME_HEIGHT;
		eachLineMaxSize[HEIGHT][1] = max(formIp[3].getWidth(), formIp[4].getWidth(), formIp[5].getWidth()) + WINDOW_FRAME_HEIGHT;
		eachLineMaxSize[HEIGHT][2] = max(formIp[6].getWidth(), (allCage >= 8? formIp[7].getWidth() : 0),
							(allCage == 9? formIp[8].getWidth() : 0)) + WINDOW_FRAME_HEIGHT;
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
								eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] +
								eachLineMaxSize[HEIGHT][2] + BLANK * 2;

		initXYWin[WIDTH] = Math.max((blockWidth + BLANK * 2) * 2, XY_WIN_X);
		initXYWin[HEIGHT] = 0;
		infoWinSize[HEIGHT] = INFO_LINE_HEIGHT * allCage + INFO_MIN_HEIGHT;

		initImageWin[WIDTH][0] = 0;
		initImageWin[WIDTH][1] = initImageWin[WIDTH][0] + blockWidth + BLANK * 2;
		initImageWin[HEIGHT][0] = infoWinSize[HEIGHT] + BLANK * 2;
		initImageWin[HEIGHT][1] = initImageWin[HEIGHT][0] + blockHeight + BLANK * 2;

		imageAlloc = new int[2][3];
		imageAlloc[WIDTH][0] = 0;
		imageAlloc[WIDTH][1] = eachLineMaxSize[WIDTH][0] + BLANK;
		imageAlloc[WIDTH][2] = imageAlloc[WIDTH][1] + eachLineMaxSize[WIDTH][1] + BLANK;
		imageAlloc[HEIGHT][0] = 0;
		imageAlloc[HEIGHT][1] = eachLineMaxSize[HEIGHT][0] + BLANK;
		imageAlloc[HEIGHT][2] = imageAlloc[HEIGHT][1] + eachLineMaxSize[HEIGHT][1] + BLANK;
	 */
//	}

	public synchronized void setImageWindow(ImagePlus[] imp, int allocation){

		//画像の表示
		int[] windowWidth = new int[9];
		int[] windowHeight = new int[9];
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 3; x++){
				if(x + y * 3 == allCage)
					break;
				int i = x + y * 3;
				imp[i].show();
				ImageWindow win = imp[i].getWindow();
				openWait();
				windowWidth[i] = win.getWidth();
				windowHeight[i] = win.getHeight();
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


		//画像の高さ・幅の取得及び表示位置の決定
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = max(windowWidth[0], windowWidth[3], windowWidth[6]);
		eachLineMaxSize[WIDTH][1] = max(windowWidth[1], windowWidth[4],	(allCage >= 8? windowWidth[7] : 0));
		eachLineMaxSize[WIDTH][2] = max(windowWidth[2], windowWidth[5],	(allCage == 9? windowWidth[8] : 0));
		eachLineMaxSize[HEIGHT][0] = max(windowHeight[0], windowHeight[1], windowHeight[2]);
		eachLineMaxSize[HEIGHT][1] = max(windowHeight[3], windowHeight[4], windowHeight[5]);
		eachLineMaxSize[HEIGHT][2] = max(windowHeight[6], (allCage >= 8? windowHeight[7] : 0),
				(allCage == 9? windowHeight[8] : 0));
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
		eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] +
		eachLineMaxSize[HEIGHT][2] + BLANK * 2;

		initXYWin[WIDTH] = Math.max((blockWidth + BLANK * 2) * 2, XY_WIN_X);
		initXYWin[HEIGHT] = 0;
		infoWinSize[HEIGHT] = INFO_LINE_HEIGHT * allCage + INFO_MIN_HEIGHT;

		initImageWin[WIDTH][0] = 0;
		initImageWin[WIDTH][1] = initImageWin[WIDTH][0] + blockWidth + BLANK * 2;
		initImageWin[HEIGHT][0] = infoWinSize[HEIGHT] + BLANK * 2;
		initImageWin[HEIGHT][1] = initImageWin[HEIGHT][0] + blockHeight + BLANK * 2;

		imageAlloc = new int[2][3];
		imageAlloc[WIDTH][0] = 0;
		imageAlloc[WIDTH][1] = eachLineMaxSize[WIDTH][0] + BLANK;
		imageAlloc[WIDTH][2] = imageAlloc[WIDTH][1] + eachLineMaxSize[WIDTH][1] + BLANK;
		imageAlloc[HEIGHT][0] = 0;
		imageAlloc[HEIGHT][1] = eachLineMaxSize[HEIGHT][0] + BLANK;
		imageAlloc[HEIGHT][2] = imageAlloc[HEIGHT][1] + eachLineMaxSize[HEIGHT][1] + BLANK;

		//ウィンドウの整列
		int[] init = getImageAllocation(allocation);
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 3; x++){
				if(x + y * 3 == allCage)
					break;
				ImageWindow win = imp[x + y * 3].getWindow();
				win.setLocation(init[WIDTH] + imageAlloc[WIDTH][x], init[HEIGHT] + imageAlloc[HEIGHT][y]);
				openWait();
			}
		}
	}

	@Override
	public synchronized void setImageWindow(ImagePlus[] imp, int allocation,
			boolean[] activeCage) {
		//画像の表示
		int[] windowWidth = new int[9];
		int[] windowHeight = new int[9];
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 3; x++){
				if(x + y * 3 == allCage)
					break;
				int i = x + y * 3;
				if(activeCage[i]){
					imp[i].show();
					ImageWindow win = imp[i].getWindow();
					openWait();
					windowWidth[i] = win.getWidth();
					windowHeight[i] = win.getHeight();
				}else{
					windowWidth[i] = imp[i].getWidth();
					windowHeight[i] = imp[i].getHeight();
				}
			}
		}


		//画像の高さ・幅の取得及び表示位置の決定
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = max(windowWidth[0], windowWidth[3], windowWidth[6]);
		eachLineMaxSize[WIDTH][1] = max(windowWidth[1], windowWidth[4],	(allCage >= 8? windowWidth[7] : 0));
		eachLineMaxSize[WIDTH][2] = max(windowWidth[2], windowWidth[5],	(allCage == 9? windowWidth[8] : 0));
		eachLineMaxSize[HEIGHT][0] = max(windowHeight[0], windowHeight[1], windowHeight[2]);
		eachLineMaxSize[HEIGHT][1] = max(windowHeight[3], windowHeight[4], windowHeight[5]);
		eachLineMaxSize[HEIGHT][2] = max(windowHeight[6], (allCage >= 8? windowHeight[7] : 0),
				(allCage == 9? windowHeight[8] : 0));
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
		eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] +
		eachLineMaxSize[HEIGHT][2] + BLANK * 2;

		initXYWin[WIDTH] = Math.max((blockWidth + BLANK * 2) * 2, XY_WIN_X);
		initXYWin[HEIGHT] = 0;
		infoWinSize[HEIGHT] = INFO_LINE_HEIGHT * allCage + INFO_MIN_HEIGHT;

		initImageWin[WIDTH][0] = 0;
		initImageWin[WIDTH][1] = initImageWin[WIDTH][0] + blockWidth + BLANK * 2;
		initImageWin[HEIGHT][0] = infoWinSize[HEIGHT] + BLANK * 2;
		initImageWin[HEIGHT][1] = initImageWin[HEIGHT][0] + blockHeight + BLANK * 2;

		imageAlloc = new int[2][3];
		imageAlloc[WIDTH][0] = 0;
		imageAlloc[WIDTH][1] = eachLineMaxSize[WIDTH][0] + BLANK;
		imageAlloc[WIDTH][2] = imageAlloc[WIDTH][1] + eachLineMaxSize[WIDTH][1] + BLANK;
		imageAlloc[HEIGHT][0] = 0;
		imageAlloc[HEIGHT][1] = eachLineMaxSize[HEIGHT][0] + BLANK;
		imageAlloc[HEIGHT][2] = imageAlloc[HEIGHT][1] + eachLineMaxSize[HEIGHT][1] + BLANK;

		//ウィンドウの整列
		int[] init = getImageAllocation(allocation);
		for(int y = 0; y < 3; y++){
			for(int x = 0; x < 3; x++){
				if(x + y * 3 == allCage)
					break;
				if(!activeCage[x+y*3]) continue;
				ImageWindow win = imp[x + y * 3].getWindow();
				win.setLocation(init[WIDTH] + imageAlloc[WIDTH][x], init[HEIGHT] + imageAlloc[HEIGHT][y]);
				openWait();
			}
		}
		
	}
}





