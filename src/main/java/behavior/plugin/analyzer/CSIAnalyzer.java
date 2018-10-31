package behavior.plugin.analyzer;

import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.io.IOException;
import java.io.File;
import java.util.Arrays;

import behavior.image.process.CSITracer;
import behavior.image.process.ImageManager;
import behavior.io.FileManager;
import behavior.setup.parameter.CSIParameter;
import behavior.setup.parameter.Parameter;

/**
 * 
 * @author Modifier:Butoh
 */
public class CSIAnalyzer extends Analyzer{
	public static final int BIN_DISTANCE = 101;
	public static final int STLCAGE = 102;
	public static final int STRCAGE = 103;
	public static final int NELCAGE = 104;
	public static final int NERCAGE = 105;
	public static final int DISLCAGE = 106;
	public static final int DISRCAGE = 107;
	public static final int STLA = 108;
	public static final int STRA = 109;
	public static final int STCE = 110;
	public static final int NELA = 111;
	public static final int NERA = 112;
	public static final int NECE = 113;
	public static final int DISLA = 114;
	public static final int DISRA = 115;
	public static final int DISCE = 116;

	public static final int RIGHTCAGE = 6,LEFTCAGE = 7,CHAMBER = 8;
		
	private String _RightCageID,_LeftCageID;

	private final int LEFT_SEPARATOR = Parameter.getInt(CSIParameter.leftSeparator);
	private final int RIGHT_SEPARATOR = Parameter.getInt(CSIParameter.rightSeparator);
	private final int RATE = Parameter.getInt(Parameter.rate);
	private final double MOVEMENT_CRITERION = Parameter.getDouble(CSIParameter.movementCriterion);
	private final boolean useMask = Parameter.getBoolean(CSIParameter.useMask);

	private Roi[] _Rois = new Roi[4];
	private final int NO_CONTACT = -1;

	private final int LEFT_INNER = 0;
	private final int RIGHT_INNER = 1;
	private final int LEFT_OUTER = 2;
	private final int RIGHT_OUTER = 3;

	private final int LEFT_CAGE = 0;
	private final int RIGHT_CAGE = 1;
	private final int LEFT_CHAMBER = 2;
	private final int CENTER_CHAMBER = 3;
	private final int RIGHT_CHAMBER = 4;
	private final int TOTAL = 5;

	private int _SlicePerMovement;
	protected double[][] _BinStay;
	protected int[][] _BinEntry;
	protected double[][] _BinDistance;

	//private int currentArea = 0;
	//private boolean _isLeftExist;
	//private boolean _isRightExist;

	protected int[][] xyaData;	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[？個目のかたまり][データの種類]
	protected int[] prevXyaData;

	private boolean _isLeftContact = false,_isRightContact=false;
	protected boolean start = false;

	private OvalRoi[] rois = {null,null};

	public CSIAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate());
		tracer = new CSITracer(backIp);

		initializeArrays();

		_SlicePerMovement = 0;

		setImageSize(backIp.getWidth(), backIp.getHeight());


		final String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir) + File.separator;

        try{
	        _Rois[LEFT_INNER] = new RoiDecoder(path+"LInner.roi").getRoi();
	        _Rois[RIGHT_INNER] = new RoiDecoder(path+"RInner.roi").getRoi();
	        _Rois[LEFT_OUTER] = new RoiDecoder(path+"LOuter.roi").getRoi();
	        _Rois[RIGHT_OUTER] = new RoiDecoder(path+"ROuter.roi").getRoi();
        }catch(IOException e){
        	e.printStackTrace();
        }

     	String filePath = path+"LCageMask.roi";
     	if(new File(filePath).exists()){
     	    try{
     	    	Rectangle rec = new RoiDecoder(filePath).getRoi().getBounds();
     		    rois[0]= new OvalRoi(rec.x,rec.y,rec.width,rec.height);
		    }catch(IOException e) {
			    e.printStackTrace();
		    }
     	}

     	filePath = path+"RCageMask.roi";
     	if(new File(filePath).exists()){
     	    try{
     	    	Rectangle rec = new RoiDecoder(filePath).getRoi().getBounds();
     		    rois[1]= new OvalRoi(rec.x,rec.y,rec.width,rec.height);
		    }catch(IOException e){
			    e.printStackTrace();
		    }
     	}
	}

	@Override
	protected void initializeArrays(){
		_BinDistance = new double[6][binLength];
		for(int i=0;i<6;i++)
		    Arrays.fill(_BinDistance[i], 0.0);
		_BinStay = new double[5][binLength];
		for(int l=0;l<5;l++)
		    Arrays.fill(_BinStay[l],0.0);
		_BinEntry = new int[5][binLength];
		for(int l=0;l<5;l++)
		    Arrays.fill(_BinEntry[l],0);
	}

	/**
	 * 左右のケージにいるマウスのIDを設定する。マウスが存在する・しないの区別も行う。
	 * @param leftCageID
	 * @param rightCageID
	 */
	public void setSubCageID(String leftCageID,String rightCageID){
		_LeftCageID = leftCageID;
		_RightCageID = rightCageID;
	}

	public String getRightCageID(){
		return _RightCageID;
	}

	public String getLeftCageID(){
		return _LeftCageID;
	}

	public void setStart(final boolean set){
		if (start == true)
			return;
		start = set;
	}

	@Override
	public void analyzeImage(ImageProcessor currentIp){
		ImageProcessor ip = currentIp.duplicate();
		if(useMask){
		    ip.setColor(0);
		    for(Roi roi : rois){
			    if(roi!=null){
				    ImageProcessor mask = ip.getMask();
		            Rectangle rec = ip.getRoi();
		            ip.setRoi(roi);
		            ip.fill(ip.getMask());
		            ip.setMask(mask);
		            ip.setRoi(rec);
			    }
		    }
		}

	    imgManager.setCurrentIp(ip.duplicate());
	    imgManager.subtract();
	    imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
	    imgManager.dilate(ImageManager.SUBTRACT_IMG);

		if(xyaData != null)
			prevXyaData = xyaData[0];	//前回の数値を残しておく
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	@Override
	public void nextBin(){
		if(currentBin<binLength-1){
		    currentBin++;
		    currentBinSlice = 0;
		}
	}

	@Override
	public void calculate(final int currentSlice){
		if(currentSlice > 0){	//この計算だと、最初の画像は解析の対象に入れないことになるが、behavior3 を引き継いで‥
		    currentBinSlice++;
		    //距離
		    final double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
                                                prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);

		    final int currentPosition = getCurrentPosition(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		    //currentDistance > MOVEMENT_CRITERION/RATE
		    if(currentDistance*RATE > MOVEMENT_CRITERION){
			    _SlicePerMovement++;
                _BinDistance[TOTAL][currentBin] += currentDistance;
		        /*マウスのstate関係*/
		        analyzeContact(currentPosition, currentDistance,true);
			    analyzePosition(xyaData[0][X_CENTER],prevXyaData[X_CENTER],currentDistance,true);
		    }else{
			    analyzeContact(currentPosition, currentDistance,false);
		        analyzePosition(xyaData[0][X_CENTER],prevXyaData[X_CENTER],currentDistance,false);
		    }

		    tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
        tracer.setPrevXY(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}

	/**
	 * Roiで指定した場所にマウスがいるかどうか・
	 * @param x　現在のX座標
	 * @param y　現在のY座標
	 * @return　位置関係
	 */
	private int getCurrentPosition(final int x,final int y){
		if(_Rois[LEFT_INNER].contains(x, y)){
			return LEFT_INNER;
		}else if(_Rois[LEFT_OUTER].contains(x, y)){
			return LEFT_OUTER;
		}

		if(_Rois[RIGHT_INNER].contains(x, y)){
			return RIGHT_INNER;
	    }else if(_Rois[RIGHT_OUTER].contains(x, y)){
			return RIGHT_OUTER;
	    }

		return NO_CONTACT;
	}

	private void analyzeContact(final int currentPosition, final double dist,final boolean isMove){
		if(isMove){
		    switch(currentPosition){
		        case NO_CONTACT:
		    	    _isLeftContact = false;
		    	    _isRightContact = false;
		    	    return;
		        case LEFT_INNER:
		    	    if(!_isLeftContact){
	        		    _BinEntry[LEFT_CAGE][currentBin]++;
		    	    }
	        	    _BinStay[LEFT_CAGE][currentBin]++;
	        	    _BinDistance[LEFT_CAGE][currentBin]+= dist;

	        	    _isLeftContact = true;
		    	    return;
		        case RIGHT_INNER:
		    	    if(!_isRightContact){
	        	        _BinEntry[RIGHT_CAGE][currentBin]++;
	    		    }
        		    _BinStay[RIGHT_CAGE][currentBin]++;
        		    _BinDistance[RIGHT_CAGE][currentBin]+= dist;

        		    _isRightContact = true;
		    	    return;
		        case LEFT_OUTER:
		    	    if(_isLeftContact){
	        		    _BinStay[LEFT_CAGE][currentBin]++;
	            	    _BinDistance[LEFT_CAGE][currentBin]+= dist;
	                }
		    	    return;
		        case RIGHT_OUTER:
		    	    if(_isRightContact){
		    		    _BinStay[RIGHT_CAGE][currentBin]++;
	            	    _BinDistance[RIGHT_CAGE][currentBin]+= dist;
		    	    }
		    	    return;

		        default:throw new IllegalStateException();
		    }
		}else{
			switch(currentPosition){
	        case NO_CONTACT:
	    	    return;
	        case LEFT_INNER:
        	    _BinStay[LEFT_CAGE][currentBin]++;
	    	    return;
	        case RIGHT_INNER:
    		    _BinStay[RIGHT_CAGE][currentBin]++;
	    	    return;
	        case LEFT_OUTER:
	    	    if(_isLeftContact){
        		    _BinStay[LEFT_CAGE][currentBin]++;
                }
	    	    return;
	        case RIGHT_OUTER:
	    	    if(_isRightContact){
	    		    _BinStay[RIGHT_CAGE][currentBin]++;
	    	    }
	    	    return;

	        default:throw new IllegalStateException();
	        }
		}
	}

	private void analyzePosition(final int x, final int prevX,final double dist,final boolean isMove){
		if(isMove){
		    if(x <= LEFT_SEPARATOR){
			    if(prevX > LEFT_SEPARATOR)
				    _BinEntry[LEFT_CHAMBER][currentBin]++;
    		    _BinStay[LEFT_CHAMBER][currentBin]++;
    		    _BinDistance[LEFT_CHAMBER][currentBin]+= dist;
	         }else if(x >= RIGHT_SEPARATOR){
			     if(prevX < RIGHT_SEPARATOR)
				     _BinEntry[RIGHT_CHAMBER][currentBin]++;
	    	     _BinStay[RIGHT_CHAMBER][currentBin]++;
	    	     _BinDistance[RIGHT_CHAMBER][currentBin]+= dist;
		     }else{
			     if(prevX <= LEFT_SEPARATOR || prevX >= RIGHT_SEPARATOR)
			         _BinEntry[CENTER_CHAMBER][currentBin]++;
	    	     _BinStay[CENTER_CHAMBER][currentBin]++;
	    	     _BinDistance[CENTER_CHAMBER][currentBin]+= dist;
		     }
		}else{
			if(x <= LEFT_SEPARATOR){
    		    _BinStay[LEFT_CHAMBER][currentBin]++;
	         }else if(x >= RIGHT_SEPARATOR){
	    	    _BinStay[RIGHT_CHAMBER][currentBin]++;
		     }else{
	    	    _BinStay[CENTER_CHAMBER][currentBin]++;
		     }
		}
	}
	
	/*public void checkCage(ImageProcessor cageIp,int cageNum,int currentSlice){
		if(currentSlice == 0)
			return;
		int[] histogram = cageIp.getHistogram();
		Arrays.sort(histogram);
		
		if(histogram[histogram.length-2] > 0){
			binStay[cageNum][currentBin]++;
			if(!ent[cageNum])
				binEntry[cageNum][currentBin]++;
			ent[cageNum] = true;
		}else{
			ent[cageNum] = false;
		}
		
		currentArea = cageNum;
	}*/

	//SubtractImageを返す
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}
	
	public String getInfo(String subjectID, int sliceNum){
		double totalDistance=0,totalStayTimeInLeftCage=0;
		int totalEntryInLeftCage=0;
		for(int i=0;i<currentBin+1;i++){
			totalDistance += _BinDistance[TOTAL][i];
			totalStayTimeInLeftCage += _BinStay[LEFT_CAGE][i];
			totalEntryInLeftCage += _BinEntry[LEFT_CAGE][i];
		}
		
		return subjectID+"\t"+getElapsedTime(sliceNum)+"\t"+_SlicePerMovement+"\t"+Math.round(totalDistance*10.0)/10.0+"\t"+(totalStayTimeInLeftCage/RATE)+"\t"+totalEntryInLeftCage;
	}

	@Override
	public String getXY(int sliceNum){
		String xyData = sliceNum +"\t"+ xyaData[0][X_CENTER] +"\t"+ xyaData[0][Y_CENTER] +"\t"+
							(xyaData[0][EXIST_FLAG] == 0?"NP" : String.valueOf(xyaData[0][AREA]));
		return xyData;
	}

	public String[] getResults(){
		double totalDistance = 0;
		double totalDistanceInLeftCage=0;
		double totalDistanceInRightCage=0;
		double totalDistanceInLeft=0;
		double totalDistanceInCenter=0;
		double totalDistanceInRight=0;

		double totalStayTimeInLeftCage=0;
		double totalStayTimeInRightCage=0;
		double totalStayTimeInLeft=0;
		double totalStayTimeInCenter=0;
		double totalStayTimeInRight=0;

		int totalEntryInLeftCage=0;
		int totalEntryInRightCage=0;
		int totalEntryInLeft=0;
		int totalEntryInCenter=0;
		int totalEntryInRight=0;

		for(int bin=0;bin<binLength;bin++){
			totalDistance += _BinDistance[TOTAL][bin];
			totalDistanceInLeftCage += _BinDistance[LEFT_CAGE][bin];
			totalDistanceInRightCage += _BinDistance[RIGHT_CAGE][bin];
			totalDistanceInLeft += _BinDistance[LEFT_CHAMBER][bin];
			totalDistanceInCenter += _BinDistance[CENTER_CHAMBER][bin];
			totalDistanceInRight += _BinDistance[RIGHT_CHAMBER][bin];

			totalStayTimeInLeftCage += _BinStay[LEFT_CAGE][bin];
			totalStayTimeInRightCage += _BinStay[RIGHT_CAGE][bin];
			totalStayTimeInLeft += _BinStay[LEFT_CHAMBER][bin];
			totalStayTimeInCenter += _BinStay[CENTER_CHAMBER][bin];
			totalStayTimeInRight += _BinStay[RIGHT_CHAMBER][bin];

			totalEntryInLeftCage += _BinEntry[LEFT_CAGE][bin];
			totalEntryInRightCage += _BinEntry[RIGHT_CAGE][bin];
			totalEntryInLeft += _BinEntry[LEFT_CHAMBER][bin];
			totalEntryInCenter += _BinEntry[CENTER_CHAMBER][bin];
			totalEntryInRight += _BinEntry[RIGHT_CHAMBER][bin];
	    }
		double movementTime = _SlicePerMovement/RATE;

		String[] results = new String[19];

		//LeftCageID
		results[0]  = _LeftCageID;
		//RightCageID
		results[1]  = _RightCageID;
		//TotalDistance
		results[2]  = ""+ Math.round(totalDistance*10.0)/10.0;
		//AverageSpeed
		if(movementTime==0){
			results[3]  = "0.0";
		}else{
		    results[3]  = ""+ Math.round((totalDistance/movementTime)*10.0)/10.0;
		}
		//STinLeftCage
		results[4]  = ""+ Math.round((totalStayTimeInLeftCage/RATE)*10.0)/10.0;
		//SRinRightCage
		results[5]  = ""+ Math.round((totalStayTimeInRightCage/RATE)*10.0)/10.0;
		//NEofLeftCage
		results[6]  = ""+ totalEntryInLeftCage;
		//NEinRightCage
		results[7]  = ""+ totalEntryInRightCage;
		//DistInLeftCage
		results[8]  = ""+ Math.round(totalDistanceInLeftCage*10.0)/10.0;
		//DistInRightCage
		results[9]  = ""+ Math.round(totalDistanceInRightCage*10.0)/10.0;
		//STinLeftArea
		results[10] = ""+ Math.round((totalStayTimeInLeft/RATE)*10.0)/10.0;
		//STinCenterArea
		results[11] = ""+ Math.round((totalStayTimeInCenter/RATE)*10.0)/10.0;
		//STinRightCage
		results[12] = ""+ Math.round((totalStayTimeInRight/RATE)*10.0)/10.0;
		//NEofLeftArea
		results[13] = ""+ totalEntryInLeft;
		//NEofCenterArea
		results[14] = ""+ totalEntryInCenter;
		//NEofRightArea
		results[15] = ""+ totalEntryInRight;
		//DistInLeftArea
		results[16] = ""+ Math.round(totalDistanceInLeft*10.0)/10.0;
		//DistInCenterArea
		results[17] = ""+ Math.round(totalDistanceInCenter*10.0)/10.0;
		//DistInRightArea
		results[18] = ""+ Math.round(totalDistanceInRight*10.0)/10.0;

		return results;
	}

	public String[] getBinResult(int option){
		String[] result = new String[binLength];
		for(int num = 0; num < binLength; num++){
			switch(option){
			case BIN_DISTANCE: result[num] = ""+(Math.round(_BinDistance[TOTAL][num]*10.0)/10.0); break;
			case STLCAGE: result[num] = ""+_BinStay[LEFT_CAGE][num]; break;
			case STRCAGE: result[num] = ""+_BinStay[RIGHT_CAGE][num]; break;
			case NELCAGE: result[num] = ""+_BinEntry[LEFT_CAGE][num]; break;
			case NERCAGE: result[num] = ""+_BinEntry[RIGHT_CAGE][num]; break;
			case DISLCAGE: result[num] = ""+Math.round(_BinDistance[LEFT_CAGE][num]*10.0)/10.0; break;
			case DISRCAGE: result[num] = ""+Math.round(_BinDistance[RIGHT_CAGE][num]*10.0)/10.0; break;
			case STLA: result[num] = ""+_BinStay[LEFT_CHAMBER][num]; break;
			case STRA: result[num] = ""+_BinStay[RIGHT_CHAMBER][num]; break;
			case STCE: result[num] = ""+_BinStay[CENTER_CHAMBER][num]; break;
			case NELA: result[num] = ""+_BinEntry[LEFT_CHAMBER][num]; break;
			case NERA: result[num] = ""+_BinEntry[RIGHT_CHAMBER][num]; break;
			case NECE: result[num] = ""+_BinEntry[CENTER_CHAMBER][num]; break;
			case DISLA: result[num] = ""+Math.round(_BinDistance[LEFT_CHAMBER][num]*10.0)/10.0; break;
			case DISRA: result[num] = ""+Math.round(_BinDistance[RIGHT_CHAMBER][num]*10.0)/10.0; break;
			case DISCE: result[num] = ""+Math.round(_BinDistance[CENTER_CHAMBER][num]*10.0)/10.0; break;
			default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}
		if(state == INTERRUPT)
			result[binLength - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}
}