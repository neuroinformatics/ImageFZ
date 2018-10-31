package behavior.plugin.analyzer;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import behavior.image.process.YMTracer;
import behavior.image.process.ImageManager;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;

/**
 * 
 * @author Butoh
 * @version Last Modified 091214
 */
public class YMAnalyzer extends Analyzer{
	public static final int BIN_DISTANCE = 1;
	public enum FileType{ENTER_TIME,STAY_TIME,EXIT_TIME,ALTERNATION_STATE}

	private Roi[] _Rois = new Roi[7];
	private final int NO_CONTACT = -1;

	private final int ARM1 = 0;
	private final int ARM2 = 1;
	private final int ARM3 = 2;
	private final int CENTER = 3;
	private final int ARM1_OUTER = 4;
	private final int ARM2_OUTER = 5;
	private final int ARM3_OUTER = 6;

	private final int TOTAL = 3;

	protected double[][] _BinStay;
	protected int[][] _BinEntry;
	protected double[][] _BinDistance;

	protected int[][] xyaData;	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[？個目のかたまり][データの種類]
	protected int[] prevXyaData;

	//使用するパラメーター
	private final int RATE = Parameter.getInt(Parameter.rate);

	private int totalSlice = 0;
	private boolean _isContact;
	protected boolean start = false;

	private int _PrevPosition;
	private int _PrevSelection;
	private int _PrepreSelection;
	private int _Alternation;
	private int _TotalChoice;
	private double enterTime;

	private List<Character> _AlternationState = new ArrayList<Character>();
	private List<Double> _EnterTime = new ArrayList<Double>();
	private List<Double> _StayTime = new ArrayList<Double>();
	private List<Double> _ExitTime = new ArrayList<Double>();

	public YMAnalyzer(ImageProcessor backIp) {
		imgManager = new ImageManager(backIp.duplicate()); //とりあえず複製
		tracer = new YMTracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());

		setRois();

		_PrevPosition = NO_CONTACT;
		_PrevSelection = NO_CONTACT;
		_PrepreSelection = NO_CONTACT;

		_Alternation = 0;
		_TotalChoice = 0;

		_AlternationState.clear();
		_EnterTime.clear();
		_StayTime.clear();
		_ExitTime.clear();
	}

	@Override
	protected void initializeArrays(){
		_BinDistance = new double[4][binLength];
		for(int i=0;i<4;i++)
		    Arrays.fill(_BinDistance[i], 0.0);
		_BinStay = new double[4][binLength];
		for(int l=0;l<4;l++)
		    Arrays.fill(_BinStay[l],0.0);
		_BinEntry = new int[3][binLength];
		for(int m=0;m<3;m++)
		    Arrays.fill(_BinEntry[m],0);
	}

	private void setRois(){
        final String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir) + System.getProperty("file.separator");

        try{
	        _Rois[ARM1] = new RoiDecoder(path+"Arm1.roi").getRoi();
	        _Rois[ARM2] = new RoiDecoder(path+"Arm2.roi").getRoi();
	        _Rois[ARM3] = new RoiDecoder(path+"Arm3.roi").getRoi();
	        _Rois[CENTER] = new RoiDecoder(path+"Center.roi").getRoi();
	        _Rois[ARM1_OUTER] = new RoiDecoder(path+"Arm1Outer.roi").getRoi();
	        _Rois[ARM2_OUTER] = new RoiDecoder(path+"Arm2Outer.roi").getRoi();
	        _Rois[ARM3_OUTER] = new RoiDecoder(path+"Arm3Outer.roi").getRoi();
        }catch(IOException e){
        	e.printStackTrace();
        }
	}

	public void setStart(final boolean set){
		if (start == true)
			return;
		start = set;
	}

	/*実験の開始は、外部のボタンによって行う*/
	public boolean startAnalyze(int cage){
		return start;
	}

	@Override
	public void analyzeImage(ImageProcessor currentIp) {
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//前回の数値を残しておく
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	//次のbinに移動
	@Override
	public void nextBin(){
		if(currentBin<binLength-1){
		    currentBin++;
		    currentBinSlice = 0;
		}
	}

	@Override
	public void calculate(int currentSlice){
		if(currentSlice > 0){	//この計算だと、最初の画像は解析の対象に入れないことになるが、behavior3 を引き継いで‥
			currentBinSlice++;
			//距離
			final double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
                                                prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
            _BinDistance[TOTAL][currentBin] += currentDistance;

		    final int currentPosition = getCurrentPosition(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);			    /*マウスのstate関係*/
		    analyzeIndividual(currentPosition, currentDistance);

		    final double currentTime = Math.round((currentSlice/RATE)*10.0)/10.0;;
		    analyzeContact(currentPosition, currentTime);

			tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

			totalSlice++;
		}
	    tracer.setPrevXY(xyaData[0][0], xyaData[0][1]);
	}

	private int getCurrentPosition(final int x,final int y){
		if(_Rois[ARM1].contains(x, y)){
			return ARM1;
		}else if(_Rois[ARM1_OUTER].contains(x, y)){
			return ARM1_OUTER;
		}

		if(_Rois[ARM2].contains(x, y)){
			return ARM2;
		}else if(_Rois[ARM2_OUTER].contains(x, y)){
			return ARM2_OUTER;
		}

		if(_Rois[ARM3].contains(x, y)){
			return ARM3;
		}else if(_Rois[ARM3_OUTER].contains(x, y)){
			return ARM3_OUTER;
		}

		if(_Rois[CENTER].contains(x, y)){
			return CENTER;
		}
		
		return NO_CONTACT;
	}

	private void analyzeIndividual(final int currentPosition, final double dist){
		switch(currentPosition){
		    case NO_CONTACT:
		    	_isContact = false;
		    	return;
		    case CENTER:
		    	_BinStay[CENTER][currentBin]++;
		    	_isContact = false;
		    	return;
		    case ARM1:
		    	if(!_isContact){
	        		_BinEntry[ARM1][currentBin]++;
		    	}else if(_PrevPosition != NO_CONTACT && _PrevPosition != ARM1){
	    			_BinEntry[ARM1][currentBin]++;
	    		}
	        	_BinStay[ARM1][currentBin]++;
	        	_BinDistance[ARM1][currentBin]+= dist;

		    	_isContact = true;
		    	return;
		    case ARM2:
		    	if(!_isContact){
	        	    _BinEntry[ARM2][currentBin]++;
	    		}else if(_PrevPosition != NO_CONTACT && _PrevPosition != ARM2){
	    			_BinEntry[ARM2][currentBin]++;
	    		}
        		_BinStay[ARM2][currentBin]++;
        		_BinDistance[ARM2][currentBin]+= dist;

		    	_isContact = true;
		    	return;
		    case ARM3:
		    	if(!_isContact){
	        	    _BinEntry[ARM3][currentBin]++;
	    		}else if(_PrevPosition != NO_CONTACT && _PrevPosition != ARM3){
	    			_BinEntry[ARM3][currentBin]++;
	    		}
        		_BinStay[ARM3][currentBin]++;
        		_BinDistance[ARM3][currentBin]+= dist;

		    	_isContact = true;
		    	return;
		    case ARM1_OUTER:
		    	if(_isContact){
		    		_BinStay[ARM1][currentBin]++;
	            	_BinDistance[ARM1][currentBin]+= dist;
		    	}
		    	return;
		    case ARM2_OUTER:
		    	if(_isContact){
		    		_BinStay[ARM2][currentBin]++;
	            	_BinDistance[ARM2][currentBin]+= dist;
		    	}
		    	return;
		    case ARM3_OUTER:
		    	if(_isContact){
		    		_BinStay[ARM3][currentBin]++;
	            	_BinDistance[ARM3][currentBin]+= dist;
		    	}
		    	return;

		    default:throw new IllegalStateException();
		}
	}

	private void analyzeContact(int currentPosition,final double currentTime){
		if(currentPosition>2){
		    if(_isContact){
			    currentPosition -= 4;
		    }else{
		        currentPosition = NO_CONTACT;
		    }
		}


		if(_PrevPosition == currentPosition){
			return;
		}

		if(_PrevPosition != NO_CONTACT){
			_ExitTime.add(currentTime);
			_StayTime.add(currentTime-enterTime);
		}

		if(currentPosition != NO_CONTACT){
			char altState;
			altState = '?';
			enterTime = currentTime;
			if(_PrevSelection != currentPosition && _PrepreSelection != NO_CONTACT){
				if(_PrepreSelection != currentPosition){
					altState = '1';
					_Alternation++;
				}else{ //_PrepreSelection == currentPosition
					altState = '0';
				}
				_TotalChoice++;
			}
			if(_PrevSelection == currentPosition){
				altState = 'R';
			}if(_PrepreSelection == NO_CONTACT){
				altState = 'S';
			}

			if(_PrevSelection != currentPosition){
				_PrepreSelection = _PrevSelection;
				_PrevSelection = currentPosition;
			}

			_AlternationState.add(altState);
			_EnterTime.add(enterTime);
		}

		_PrevPosition = currentPosition;
	}

	//SubtractImageを返す
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}
	
	public String getInfo(String subjectID, int sliceNum){
		double distance = 0.0;
		int entryOfArm1 = 0;
		double stayTimeOnArm1 = 0.0; 
		for(int i=0;i<currentBin+1;i++){
			distance += _BinDistance[TOTAL][i];
			entryOfArm1 += _BinEntry[ARM1][i];
			stayTimeOnArm1 += _BinStay[ARM1][i];
		}
		return subjectID +"\t"+ getElapsedTime(sliceNum) +"\t"+ Math.round(distance*10.0)/10.0
		           +"\t"+entryOfArm1+"\t"+stayTimeOnArm1+"\t"+_TotalChoice+"\t"+_Alternation;
	}

	@Override
	public String getXY(int sliceNum){
		String xyData = sliceNum +"\t"+ xyaData[0][X_CENTER] +"\t"+ xyaData[0][Y_CENTER] +"\t"+
							(xyaData[0][EXIST_FLAG] == 0?"NP" : String.valueOf(xyaData[0][AREA]));
		return xyData;
	}

	public String[] getResults(){
		if(_PrevPosition != NO_CONTACT){
			double currentTime = Math.round(((totalSlice)/RATE)*10)/10;
			_ExitTime.add(currentTime);
			_StayTime.add(currentTime-enterTime+Math.round((1/RATE)*10)/10);
		}

		double totalDistance = 0;
		double totalDistanceInArm1 = 0;
		double totalDistanceInArm2 = 0;
		double totalDistanceInArm3 = 0;

		int totalStayTimeInArm1 = 0;
		int totalStayTimeInArm2 = 0;
		int totalStayTimeInArm3 = 0;

		int totalStayTimeInCenter = 0;

		int totalEntryInArm1 = 0;
		int totalEntryInArm2 = 0;
		int totalEntryInArm3 = 0;

		for(int bin=0;bin<binLength;bin++){
			totalDistance += _BinDistance[TOTAL][bin];
			totalDistanceInArm1 += _BinDistance[ARM1][bin];
			totalDistanceInArm2 += _BinDistance[ARM2][bin];
			totalDistanceInArm3 += _BinDistance[ARM3][bin];

			totalStayTimeInArm1 += _BinStay[ARM1][bin];
			totalStayTimeInArm2 += _BinStay[ARM2][bin];
			totalStayTimeInArm3 += _BinStay[ARM3][bin];

			totalStayTimeInCenter += _BinStay[CENTER][bin];

			totalEntryInArm1 += _BinEntry[ARM1][bin];
			totalEntryInArm2 += _BinEntry[ARM2][bin];
			totalEntryInArm3 += _BinEntry[ARM3][bin];
	    }

		int totalEntryInArm = totalEntryInArm1+totalEntryInArm2+totalEntryInArm3; 
		String[] results = new String[16];

		results[0]  = ""+ Math.round(totalDistance*10.0)/10.0;
		results[1]  = ""+ Math.round((totalSlice/RATE)*10)/10;
		results[2]  = ""+ totalEntryInArm;
		results[3]  = ""+ _TotalChoice;
		results[4]  = ""+ _Alternation;
		results[5]  = ""+ (_TotalChoice==0 ? "-" : Math.round(((double)_Alternation*100/_TotalChoice)*10.0)/10.0);
		results[6]  = ""+ Math.round((totalStayTimeInCenter/RATE)*10.0)/10.0;
		results[7]  = ""+ Math.round((totalStayTimeInArm1/RATE)*10.0)/10.0;
		results[8]  = ""+ Math.round((totalStayTimeInArm2/RATE)*10.0)/10.0;
		results[9]  = ""+ Math.round((totalStayTimeInArm3/RATE)*10.0)/10.0;
		results[10] = ""+ Math.round(totalDistanceInArm1*10.0)/10.0;
		results[11] = ""+ Math.round(totalDistanceInArm2*10.0)/10.0;
		results[12] = ""+ Math.round(totalDistanceInArm3*10.0)/10.0;
		results[13] = ""+ totalEntryInArm1;
		results[14] = ""+ totalEntryInArm2;
		results[15] = ""+ totalEntryInArm3;

		return results;
	}
	
	public String[] getBinResult(int option){
		int length = binLength;
		String[] result = new String[binLength];
		for(int num = 0; num < binLength; num++){
			switch(option){
			case BIN_DISTANCE: result[num] = String.valueOf(Math.round(_BinDistance[TOTAL][num]*10.0)/10.0); break;
			default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}
		if(state == INTERRUPT)
			result[length - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}

	public String getOtherResult(FileType option){
		String result = "";

		switch(option){
		    case ALTERNATION_STATE:
		    	Iterator<Character> iter = _AlternationState.iterator();
		    	while(iter.hasNext()){
			        result += "\t"+iter.next();
		    	}
		    	break;
		    case ENTER_TIME:
		    	Iterator<Double> iter2 = _EnterTime.iterator();
		    	while(iter2.hasNext()){
			        result += "\t"+iter2.next();
		    	}
		    	break;
		    case STAY_TIME:
		    	Iterator<Double> iter3 = _StayTime.iterator();
		    	while(iter3.hasNext()){
			        result += "\t"+iter3.next();
		    	}
		    	break;
		    case EXIT_TIME:
		    	Iterator<Double> iter4 = _ExitTime.iterator();
		    	while(iter4.hasNext()){
			        result += "\t"+iter4.next();
		    	}
		    	break;
		}

		return result;
	}
}