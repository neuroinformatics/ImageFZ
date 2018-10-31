package behavior.plugin.analyzer;

import java.awt.Rectangle;
import java.util.*;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import behavior.image.process.EPTracer;
import behavior.image.process.ImageManager;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.EPParameter;

/**
 * Elevated Plus Maze用の解析を行う。
 * 
 * 出力するデータは以下の9種類。
 * TotalDistance         -マウスが全体で移動した距離
 * TotalCenterTime       -全体でのマウスが中央にいた時間
 * StayTime(North,South,West,East,NoWhere) -それぞれの場所にマウスがいた時間
 * Entries(North,South,West,East)  -それぞれの場所への移動回数
 * OpenArmLocation 　-壁のないアームの位置
 * 
 * （以下のデータはbinごとに出力)
 * binDistance  -マウスの移動距離
 * CenterTime  -中心にいた時間
 * binTime(Open,Close) -壁のない場所またはある場所にいた時間
 * binNE(Open,Close) -回数
 * 
 * @author Butoh
 */
public final class EPAnalyzer extends Analyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int BIN_DISTANCE = 0;
	public static final int CENTER_TIME = 1;
	public static final int BIN_TIME_OPEN = 2;
	public static final int BIN_TIME_CLOSE = 3;
	public static final int BIN_NE_OPEN = 4;
	public static final int BIN_NE_CLOSE = 5;

	private static final int NORTH = 0;
	private static final int SOUTH = 1;
	private static final int WEST = 2;
	private static final int EAST = 3;
	private static final int NOWHERE = 4;
	private static final int CENTER = 5;

	private static final int OPEN = 0;
	private static final int CLOSE = 1;

	protected double startDelay = 0;

	//使用するパラメーター
	private final int rate = Parameter.getInt(EPParameter.rate);
	private final int duration = Parameter.getInt(EPParameter.duration);
	private final boolean openArmLocation = Parameter.getBoolean(EPParameter.openArmLocation);
	//CenterのMainAreaに対する相対座標
	private final int topCenter;
	private final int lowCenter;
	private final int rightmostCenter;
	private final int leftmostCenter;
	//一コマごとの時間
	private final double timePerSlice = 1.0/rate;

	//解析に使用する数値
	protected int[][] xyaData;	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[？個目のかたまり][データの種類]
	protected int[] prevXyaData;
	protected int moveEpisodeNum = 0, moveFlag = 0;
	protected double[] _BinDistance, binCenterTime, areaResult;

	private double[] stayTime; //場所ごとの滞在時間
	protected int[] entries;   //場所ごとの滞在回数
	protected int[][] binEntries; //binごとの壁のある場所とない場所にいた回数
	private double[][] binTime; //bin ごとの特定の位置にいるパーセンテージ。位置は OPEN と CLOSE
	private int prePosition = CENTER;//前の画像でのマウスの位置、(変更)

	protected boolean start = false;
	
	private final String tab = "\t";

	public EPAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate()); //とりあえず複製
		tracer = new EPTracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());

		//Centerの座標を取得
		String roiName2 = FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "center1.roi";
		Roi centerRoi = null;
		try{
		    centerRoi = new RoiDecoder(roiName2).getRoi();
		}catch(Exception e){
			e.printStackTrace();
		}
		Rectangle centerRec = centerRoi.getBounds();
		topCenter = centerRec.y;
		lowCenter = centerRec.y+centerRec.height;
		rightmostCenter = centerRec.x+centerRec.width;
		leftmostCenter = centerRec.x;
		
	}

	protected void initializeArrays(){
		_BinDistance = new double[binLength];
		Arrays.fill(_BinDistance, 0.0);
		binCenterTime  = new double[binLength];
		Arrays.fill(binCenterTime, 0.0);
		stayTime = new double[5];
		Arrays.fill(stayTime, 0);
		entries = new int[4];
		Arrays.fill(entries, 0);
		binTime = new double[2][binLength];
		for(int num = 0; num < binTime.length; num++)
			Arrays.fill(binTime[num], 0);
		binEntries = new int[2][binLength];
		for(int num = 0; num < binEntries.length; num++)
			Arrays.fill(binEntries[num], 0);
	}

	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//前回の数値を残しておく
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	public void calculate(final int currentSlice){
		if(currentSlice > 0){

		    currentBinSlice++;
		    /*binごとの距離を加えていく*/
		    double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					                        prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
		    _BinDistance[currentBin] += currentDistance;

		    /*マウスがCenterにいるかどうかを解析し、Centerにいれば時間を加える。*/
		    if(CheckArm(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]) == CENTER)
		        binCenterTime[currentBin] += timePerSlice;

		    /*マウスの位置情報を解析する*/
		    final int position = CheckArm(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

		    //マウスの場所ごとの滞在時間
		    if(position!=CENTER)
			    stayTime[position] += timePerSlice;

		    //マウスの場所が前回と違う場合、マウスの滞在回数をカウントしていく
            if(position!=prePosition){
          	    //マウスの場所ごとの滞在回数
                if(position!=CENTER && position!=NOWHERE){
           	        entries[position]++;
                    if(openArmLocation){
                        if(position==EAST || position==WEST){
           	                binEntries[OPEN][currentBin]++;
                        }else{
           	                binEntries[CLOSE][currentBin]++;
                        }
                    }else{
                        if(position==NORTH || position==SOUTH){
                            binEntries[OPEN][currentBin]++;
                        }else{
                            binEntries[CLOSE][currentBin]++;
                        }
                    }
                }
            }

            //マウスが壁のない場所にいた回数と壁のある場所にいた回数
            //壁のあるアームの位置によって異なる
            //もう少し整理できないか。
            if(position!=CENTER && position!=NOWHERE){
                if(openArmLocation){
                    if(position==EAST || position==WEST){
           	            binTime[OPEN][currentBin] += timePerSlice;
                    }else{
            	        binTime[CLOSE][currentBin] += timePerSlice;
                    }
                }else{
                    if(position==NORTH || position==SOUTH){
                        binTime[OPEN][currentBin] += timePerSlice;
                    }else{
            	        binTime[CLOSE][currentBin] += timePerSlice;
                    }
                }
            }

            prePosition = position;

		    //traceを描く
		    tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
	    tracer.setPrevXY(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}

	//マウスの位置を解析し、位置情報を返す
	private int CheckArm(final int x,final int y){
		int EPposition = NOWHERE;

		if (y < topCenter && (x< rightmostCenter && x > leftmostCenter)){	//中心の上で、width は中心範囲
			EPposition = NORTH;
		}else if (y>lowCenter && (x<rightmostCenter && x>leftmostCenter)){	//中心の下で、width は中心範囲
			EPposition = SOUTH;
		}else if (x<leftmostCenter && (y > topCenter && y<lowCenter)) {	//中心の左で、height は中心範囲
			EPposition = WEST;
		}else if (x>rightmostCenter && (y > topCenter && y < lowCenter) ) {	//中心の右で、height は中心範囲
			EPposition = EAST;
		}else if ((x <= rightmostCenter && x >= leftmostCenter) && (y >= topCenter && y <= lowCenter) ) {	//中心範囲
			EPposition = CENTER;
		}

		return EPposition;
	}

	//次のbinに移動
	public void nextBin(){
		if(currentBin<binLength-1){
		    currentBin++;
		    currentBinSlice = 0;
		}
	}

	//SubtractImageを返す
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

    //Informationウィンドウの情報
	public String getInfo(final String subjectID,final int sliceNum){
		String info = subjectID +tab+ getElapsedTime(sliceNum) +tab+ stayTime[NORTH] +tab+ stayTime[SOUTH]+
		                   tab+ stayTime[WEST] +tab+ stayTime[EAST] +tab+ stayTime[NOWHERE];
		return info;
	}

	//XYウィンドウの情報
	public String getXY(final int sliceNum){
		String xyData = sliceNum +tab+ xyaData[0][X_CENTER] +tab+ xyaData[0][Y_CENTER] +tab+
		                   (xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA])) 
		                        +tab+ getPosition(prePosition);

		return xyData;
	}

	private char getPosition(int position){
		switch(position){
		case CENTER: return 'C';
		case NORTH: return 'N';
		case SOUTH: return 'S';
		case WEST: return 'W';
		case EAST: return 'E';
		case NOWHERE: return ' ';
		}
		return 'X';
	}
	//ここは正しく動作するかまだ確認していない。
	public void interruptAnalysis(){
		if(currentBin == 0 && currentBinSlice == 0)
			state = NOTHING;
		else if(currentBin == binLength)
			state = COMPLETE;
		else if(currentBinSlice == 0){
			state = ONE_BIN_END;
			binLength = currentBin;
		}
		else{
			state = INTERRUPT;
			binLength = currentBin + 1;
		}
	}

	//Totalの結果を出す
	public String[] getResults(){
		//TotalDistance(cm)
		double totalDistance = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalDistance += _BinDistance[bin];
		//totalCenterTime(sec)
		double totalCenterTime = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalCenterTime += binCenterTime[bin];
		//PercentageOpenArmStayTime(%)
		double percentageOpenArmStayTime = 0;
		if(openArmLocation){
			percentageOpenArmStayTime = ((stayTime[WEST]+stayTime[EAST])/(duration-stayTime[NOWHERE]))
			                          *100;
		}else{
			percentageOpenArmStayTime = ((stayTime[NORTH]+stayTime[SOUTH])/(duration-stayTime[NOWHERE]))
                                      *100;
		}
		//TotalEntries
		int totalEntries = 0;
		for(int i=0;i<4;i++)
			totalEntries += entries[i];
        //PercentageOpenArmEntries(%)
		double percentageOpenArmEntries;
		if(openArmLocation){
			percentageOpenArmEntries = ((entries[WEST]+ entries[EAST])/(double)totalEntries)*100;
		}else{
			percentageOpenArmEntries = ((entries[NORTH]+ entries[SOUTH])/(double)totalEntries)*100;
		}

		String[] results = new String[15];
		//セット
		results[0] =  ""+ Math.round(totalDistance * 10.0) / 10.0;	//解析全体で動いた距離(cm)
		results[1] =  ""+ Math.round(totalCenterTime * 10.0) / 10.0; //解析全体で中心にいた時間(sec)
		results[2] =  ""+ Math.round(stayTime[NORTH] * 10.0) / 10.0;	//*10/10 の操作は、小数点第一まで結果として出すため
		results[3] =  ""+ Math.round(stayTime[SOUTH] * 10.0) / 10.0;
		results[4] =  ""+ Math.round(stayTime[WEST] * 10.0) / 10.0;
		results[5] =  ""+ Math.round(stayTime[EAST] * 10.0) / 10.0;
		results[6] =  ""+ Math.round(stayTime[NOWHERE] * 10.0) / 10.0;
		results[7] =  ""+ Math.round(percentageOpenArmStayTime * 10.0) / 10.0;
		results[8] =  ""+ entries[NORTH];
		results[9] =  ""+ entries[SOUTH];
		results[10] = ""+ entries[WEST];
		results[11] = ""+ entries[EAST];
		results[12] = ""+ totalEntries;
		results[13] = ""+ Math.round(percentageOpenArmEntries * 10.0) / 10.0;
		results[14] = Parameter.getBoolean(EPParameter.openArmLocation) ?"East/West":"North/South";
		return results;
	}

	//binごとの結果を出す
	public String[] getBinResult(final int option){
		int length = binLength;
		String[] result = new String[length];
		for(int num = 0; num < length; num++){
			switch(option){
			  case BIN_DISTANCE: result[num]   = ""+ Math.round(_BinDistance[num]*10.0)/10.0; break;
			  case CENTER_TIME: result[num]    = ""+ Math.round(binCenterTime[num]*10.0)/10.0; break;
			  case BIN_TIME_OPEN: result[num]  = ""+ Math.round(binTime[OPEN][num]*10.0)/10.0; break;
			  case BIN_TIME_CLOSE: result[num] = ""+ Math.round(binTime[CLOSE][num]*10.0)/10.0; break;
			  case BIN_NE_OPEN: result[num]    = ""+ binEntries[OPEN][num]; break;
			  case BIN_NE_CLOSE: result[num]   = ""+ binEntries[CLOSE][num]; break;
			  default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}
		if(state == INTERRUPT)
			result[length - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}
}