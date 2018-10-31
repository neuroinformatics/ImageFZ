package behavior.gui;

import ij.process.ImageProcessor;
import ij.*;
import ij.gui.ImageWindow;

/**
 * 
 * @author Modifier:Butoh
 */
public class Window3x2Operator extends WindowOperator{
	private int[][] imageAlloc;
	protected Window3x2Operator(int allCage, ImageProcessor[] backIp){
		super(allCage, backIp);
	}

//	protected void setAllocation(){
	/*
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = Math.max(formIp[0].getWidth(), formIp[3].getWidth());
		eachLineMaxSize[WIDTH][1] = Math.max(formIp[1].getWidth(), formIp[4].getWidth());
		eachLineMaxSize[WIDTH][2] = Math.max(formIp[2].getWidth(), (allCage == 6? formIp[5].getWidth() : 0));
		eachLineMaxSize[HEIGHT][0] = max(formIp[0].getHeight(), formIp[1].getHeight(), formIp[2].getHeight()) + WINDOW_FRAME_HEIGHT;
		eachLineMaxSize[HEIGHT][1] = max(formIp[3].getHeight(), formIp[4].getHeight(),
								(allCage == 6? formIp[5].getHeight() : 0)) + WINDOW_FRAME_HEIGHT;
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
								eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] + BLANK;

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
	 */
//	}

	public synchronized void setImageWindow(ImagePlus[] imp, int allocation){
		//画像の表示
		int[] windowWidth = new int[6];
		int[] windowHeight = new int[6];
		for(int y = 0; y < 2; y++){
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

		//表示位置の計算
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = Math.max(windowWidth[0], windowWidth[3]);
		eachLineMaxSize[WIDTH][1] = Math.max(windowWidth[1], windowWidth[4]);
		eachLineMaxSize[WIDTH][2] = Math.max(windowWidth[2], (allCage == 6? windowWidth[5] : 0));
		eachLineMaxSize[HEIGHT][0] = max(windowHeight[0], windowHeight[1], windowHeight[2]);
		eachLineMaxSize[HEIGHT][1] = max(windowHeight[3], windowHeight[4], (allCage == 6? windowHeight[5] : 0));
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
		eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] + BLANK;

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



		//ウィンドウの整列
		int[] init = getImageAllocation(allocation);
		for(int y = 0; y < 2; y++){
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
		int[] windowWidth = new int[6];
		int[] windowHeight = new int[6];
		for(int y = 0; y < 2; y++){
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

		//表示位置の計算
		BLANK = 0;
		int[][] eachLineMaxSize = new int[2][3];
		eachLineMaxSize[WIDTH][0] = Math.max(windowWidth[0], windowWidth[3]);
		eachLineMaxSize[WIDTH][1] = Math.max(windowWidth[1], windowWidth[4]);
		eachLineMaxSize[WIDTH][2] = Math.max(windowWidth[2], (allCage == 6? windowWidth[5] : 0));
		eachLineMaxSize[HEIGHT][0] = max(windowHeight[0], windowHeight[1], windowHeight[2]);
		eachLineMaxSize[HEIGHT][1] = max(windowHeight[3], windowHeight[4], (allCage == 6? windowHeight[5] : 0));
		int blockWidth = eachLineMaxSize[WIDTH][0] + eachLineMaxSize[WIDTH][1] +
		eachLineMaxSize[WIDTH][2] + BLANK * 2;
		int blockHeight = eachLineMaxSize[HEIGHT][0] + eachLineMaxSize[HEIGHT][1] + BLANK;

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



		//ウィンドウの整列
		int[] init = getImageAllocation(allocation);
		for(int y = 0; y < 2; y++){
			for(int x = 0; x < 3; x++){
				if(!activeCage[x+y*3]) continue;
				if(x + y * 3 == allCage)
					break;
				ImageWindow win = imp[x + y * 3].getWindow();
				win.setLocation(init[WIDTH] + imageAlloc[WIDTH][x], init[HEIGHT] + imageAlloc[HEIGHT][y]);
				openWait();
			}
		}
	}
}





