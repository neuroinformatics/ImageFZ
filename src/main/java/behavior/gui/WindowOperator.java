package behavior.gui;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import ij.*;
import ij.process.ImageProcessor;
import ij.text.*;

import behavior.setup.Header;
import behavior.setup.Program;

/**************
 *画像の表示位置の設定、画像の表示、非表示を扱う抽象クラス。
 *実際に使用する際は、サブクラスではなく、このクラスを宣言し、static getInstance(int, int) メソッド
 *を用いることで、ケージ数にあわせて必要なサブクラスを取得する。
 ***************/
/**
 * 
 * @author Modifier:Butoh
 */
public abstract class WindowOperator{
	/********************
	┌─────┐			┌──┐
	│  a  │			└──┘
	└─────┘         ┌──┐
	┌─────┐ ┌─────┐ └c─┘
	│     │ │     │ ┌──┐
	│  b1 │ │  b2 │ └──┘
	│     │ │     │ ┌──┐
	│     │ │     │ └──┘
	└─────┘ └─────┘
	┌─────┐　┌─────┐
	│     │　│　　　　│
	│     │　│     │
	│  b3 │　│  b4 │
	│     │　│     │
	└─────┘　└─────┘
	a = info ウィンドウ。全ケージの情報を記載する。
	b = 各イメージ群。一つの枠の中に各ケージそれぞれの画像が表示される。例えば b1 は現在の画像、b2 はサブトラクト
　　　	　　した画像、というふうに役割ごとに配置される。
	c = XYdata ウィンドウ。それぞれのウィンドウが各ケージの情報を表示する
	 *********************/

	/**画像の配置場所を指定するためのフィールド。左上*/
	public static final int LEFT_UP = 1;	//b1
	/**画像の配置場所を指定するためのフィールド。左下*/
	public static final int LEFT_DOWN = 2;	//b3
	/**画像の配置場所を指定するためのフィールド。右上*/
	public static final int RIGHT_UP = 3;	//b2
	/**画像の配置場所を指定するためのフィールド。右下*/
	public static final int RIGHT_DOWN = 4;	//b4

	protected final int WIDTH = 0;
	protected final int HEIGHT = 1;

	protected final int INFO_MIN_HEIGHT = 104;	//info ウィンドウの最小値(縦）。これに、文章の必要行数だけINFO_LINE_HEIGHT を加えればよい
	protected final int INFO_LINE_HEIGHT = 34;	//info ウィンドウで表示される文章の一行の縦方向のサイズ(pixel)
	protected final int XY_WIN_X = 800;  //XYウィンドウの横方向の座標の最小値
	protected int BLANK = 30; //blank の値(これを小さくすると幅を詰めた感じの配置になる)

	protected int allCage;
	protected ImageProcessor[] formIp;  //各画像の大きさ情報を取得するため backIp を参照させる。変更してはならない。
	protected int[] displaySize = {1500, 1000};
	protected int[] initInfoWin = {0, 0};	//info ウィンドウの位置（左上端)
	protected int[] infoWinSize = {700, 0};
	protected int[] initXYWin = new int[2];
	protected int[] xyWinSize = {270, 130};
	protected int[] armWinsize = {270, 120}; //RM用、現在マウスがいるアームの番号と状態を表示
	protected int[][] initImageWin = new int[2][2];	//各イメージ群の位置(左上端の画像（cage = 0 の画像）の左上端）
	
	private TextWindow infoWin, totalResWin;
	private TextWindow[] xyWin;
	private TextWindow armWin;

	protected String[] infoTexts;

	protected WindowOperator(int allCage, ImageProcessor[] backIp){
		this.allCage = allCage;
		formIp = backIp;
		infoTexts = new String[allCage];
		Arrays.fill(infoTexts, "");
		setDisplaySize();
	}

	/******
	インスタンス取得。自動的にサブクラスを指定してくれる。
	 *@param allCage ケージ数
	 *@param backIp バックグラウンド画像
	 *******/
	public static WindowOperator getInstance(int allCage, ImageProcessor[] backIp){
		if(allCage <= 2)
			return new Window2x1Operator(allCage, backIp);
		else if(allCage <= 4)
			return new Window2x2Operator(allCage, backIp);
		else if(allCage <= 6)
			return new Window3x2Operator(allCage, backIp);
		else if(allCage <= 9)
			return new Window3x3Operator(allCage, backIp);
		else
			throw new IllegalArgumentException("allCage must be 1 ~ 9");
	}

	private void setDisplaySize(){
		File infoFile = new File("display.txt");
		if(!infoFile.exists())
			return;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			displaySize[0] = Integer.parseInt(reader.readLine());
			displaySize[1] = Integer.parseInt(reader.readLine());
			reader.close();
		}catch(Exception e){
			IJ.error(String.valueOf(e));
		}
	}

	protected int max(int a, int b, int c){
		return Math.max(Math.max(a, b), c);
	}

	protected int min(int a, int b, int c){
		return Math.min(Math.min(a, b), c);
	}

	/******
	画像群（全ケージでひとまとまり）を表示して整列する。
	整列位置の計算↓
		各ウィンドウの配置場所を、ケージ数に合わせて設定する。
		設定すべき配置は、info ウィンドウの大きさ（縦方向）、XYdata ウィンドウの縦横軸、各イメージ群の縦横軸、
		イメージ群内の画像の縦横軸。
			アルゴリズムについて説明。まず縦横に並ぶ allCage 分の画像について、同じ行、列で最も height, width が大きいもの
			を割り出す。この値プラス blank が、次の行、列の画像の先頭位置となる。例えば、1行目（cageNo = 1, 2, 3)の heigth で
			一番大きいのが、2 の 100 ピクセルであるとし、blank は 50 ピクセルであるとすると、2行目（cageNO = 4, 5, 6)の
			上端の位置は全ての画像で、150 となる。
	画像の更新は、各自、表示した ImagePlus に、新たな ImageProcessor を setProcessor することでやってもらう。
	 *@param allocation 表示位置のフィールドを代入
	 *******/
	public abstract void setImageWindow(ImagePlus[] imp, int allocation);

	public abstract void setImageWindow(ImagePlus[] imp, int allocation,boolean[] activeCage);

	protected int[] getImageAllocation(int allocation){
		int[] init = new int[2];
		switch(allocation){
		case LEFT_UP: 	 init[WIDTH] = initImageWin[WIDTH][0]; init[HEIGHT] = initImageWin[HEIGHT][0]; break;
		case LEFT_DOWN:  init[WIDTH] = initImageWin[WIDTH][0]; init[HEIGHT] = initImageWin[HEIGHT][1]; break;
		case RIGHT_UP:	 init[WIDTH] = initImageWin[WIDTH][1]; init[HEIGHT] = initImageWin[HEIGHT][0]; break;
		case RIGHT_DOWN: init[WIDTH] = initImageWin[WIDTH][1]; init[HEIGHT] = initImageWin[HEIGHT][1]; break;
		}
		return init;
	}

	/******
	 *info ウィンドウを表示
	 *@param program プログラム番号
	 *******/
	public void setInfoWindow(Program program){
        infoWin = new TextWindow("information", Header.getInfoHeader(program), "", infoWinSize[WIDTH], infoWinSize[HEIGHT]);
		infoWin.setLocation(initInfoWin[0], initInfoWin[1]);
		openWait();
	}

	/**テキストの更新*/
	public synchronized void setInfoText(String text,int cage){
		infoTexts[cage] = text;
		StringBuilder textAll = new StringBuilder();
		for(int i = 0; i < infoTexts.length; i++)
			textAll.append(infoTexts[i] + "\n");

		TextPanel infoTp = infoWin.getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
		infoTp.append(textAll.toString());
	}

	/******
	XY データウィンドウを表示
	 *******/
	public void setXYWindow(Program program,boolean[] activeCage){
		xyWin = new TextWindow[allCage];
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage]) continue;
            xyWin[cage] = new TextWindow("XY-Data" + (cage + 1), Header.getXYHeader(program), "" , xyWinSize[WIDTH], xyWinSize[HEIGHT]);
			xyWin[cage].setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT] * cage);
			openWait();
		}
	}

	public void setXYWindow(Program program){
		xyWin = new TextWindow[allCage];
		for(int cage = 0; cage < allCage; cage++){
            xyWin[cage] = new TextWindow("XY-Data" + (cage + 1), Header.getXYHeader(program), "" , xyWinSize[WIDTH], xyWinSize[HEIGHT]);
			xyWin[cage].setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT] * cage);
			openWait();
		}
	}
	/******
	XY データウィンドウのテキストを更新。
	 *@param cage テキストを更新するケージ
	 *******/
	public synchronized void setXYText(int cage, String text){
		xyWin[cage].append(text);
	}

	/**
	 * XYデータウィンドウのデータを初期化する。
	 * @param cage 初期化するケージ
	 */
	public void clearXYText(int cage){
		TextPanel infoTp = xyWin[cage].getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
	}

	/******
	XY データウィンドウからテキストパネルを取得する。（内容を保存する際に必要となる。）
	 *@param cage テキストパネルを取得するケージ
	 *******/
	public TextPanel getXYTextPanel(int cage){
		return xyWin[cage].getTextPanel();
	}

	/**
	 * RM用、現在マウスがいるアームの番号と状態を表示させる。
	 * @param program　プログラム番号
	 */
	public void setRMArmWindow(Program program){
		if(program != Program.RM)
			return;
		armWin = new TextWindow("VisitedArmNumber", "Counter" + "\t" + "ArmNumber" + "\t" + "Episode","", armWinsize[WIDTH],armWinsize[HEIGHT]);
		armWin.setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT]*3);
		openWait();
	}

	public synchronized void setRMArmText(final String text){
		armWin.append(text);
	}

	public synchronized void setAllRMArmText(final String text){
		TextPanel infoTp = armWin.getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
		armWin.append(text);
	}

	/******
	メインデータを表示。
	 *@param cageNum ケージ数
	 *@param subID サブジェクトID
	 *@param totalResult 結果。totalResult[ケージ数][結果内容]
	 *******/
	 public void showOnlineTotalResult(Program program, int cageNum, String[] subID, String[][] totalResult){
		 totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		 totalResWin.setLocation(0, 600);
		 StringBuilder line = new StringBuilder();
		 for(int cage = 0; cage < cageNum; cage++){
			 line.append(subID[cage]);
			 for(int num = 0; num < totalResult[cage].length; num++)
				 line.append("\t" + totalResult[cage][num]);
			 line.append("\n");
		 }
		 totalResWin.append(line.toString());
	 }
	 
	//使用するCageを選択する実験ではこちらを使う。
	 public void showOnlineTotalResult(Program program, int allCage, boolean[] activeCage, String[] subID, String[][] totalResult) {
		 totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		 totalResWin.setLocation(0, 600);
		 for (int cage = 0; cage < allCage; cage++) {
			 if (activeCage[cage]) {
				 StringBuffer line = new StringBuffer(subID[cage]);
				 for(int num = 0; num < totalResult[cage].length; num++)
					 line.append("\t" + totalResult[cage][num]);
				 line.append("\n");
				 totalResWin.append(line.toString());
			 }
		 }
	}

	public void showOfflineTotalResult(Program program,ArrayList<String[]> results){
		totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		totalResWin.setLocation(0, 600);
		StringBuilder line = new StringBuilder();
		for(String[] resultLine:results){
			line.append(resultLine[0]);
			for(int i=1;i<resultLine.length;i++){
				line.append("\t"+resultLine[i]);
			}
		    line.append("\n");
	    }

		totalResWin.append(line.toString());
	}

	//BM専用
	public void showBMTotalResult(Program program, int cageNum, String[] subID, String[][] totalResult){
		totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 150);
		totalResWin.setLocation(0, 700);
		for(int cage = 0; cage < cageNum; cage++){
			StringBuffer line = new StringBuffer(subID[cage]);
			for(int num = 0; num < totalResult[cage].length; num++)
				line.append("\t" + totalResult[cage][num]);
			line.append("\n");
			totalResWin.append(line.toString());
		}
	}
	
	//BM専用
	public void showProbeResult(Program program, String[] subID, String[] probeResult){
		String header = "SubjectID" + "\t" + "Target" + "\t" + "N1" + "\t" + "N2" + "\t" + "N3" + "\t" + "N4" + "\t" + "N5" + "\t" + "N6" + "\t" + "N7" + "\t" + "N8" + "\t" + "N9" + "\t" + "N10" + "\t" + "N11" + "\t" + "N12";
		totalResWin = new TextWindow("Results - PROBE", header, "", 650, 150);
		totalResWin.setLocation(0, 860);
	
		StringBuffer line = new StringBuffer(subID[0]);
		for(int num = 0; num < probeResult.length; num++)
			line.append("\t" + probeResult[num]);
		line.append("\n");
		totalResWin.append(line.toString());
	}

	/*****
	 * 全ての表示されているウィンドウを非表示にする。
	 ******/
	public void closeWindows(){
		WindowManager.closeAllWindows();
	}

	/**
	 * Mac ではウインドウを開くのに時間を要する。
	 */
	protected void openWait(){
		if(IJ.isMacOSX()){
			try{
				Thread.sleep(300);
			} catch(Exception e){
			}
		}
	}
}