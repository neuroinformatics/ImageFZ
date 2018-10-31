package behavior.plugin.analyzer;

import java.io.IOException;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.image.process.TMImageManager;
import behavior.image.process.TMTracer;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.TMParameter;
import behavior.tmaze.StateManager;

public class TMAnalyzer extends Analyzer{
	private final int rate = Parameter.getInt(Parameter.rate);

	protected int[][] xyaData;
	protected int[] prevXyaData;

	private double totalSlice = 0;
	private double totalDistance = 0;

	private Roi[] rois = new Roi[6];
	private int previousPosition;

	private final String tab = "\t";

	private StateManager stateManager;
	private String testType;

	public TMAnalyzer(ImageProcessor backIp){
		imgManager = new TMImageManager(backIp.duplicate(),Parameter.getInt(Parameter.minSize), Parameter.getInt(Parameter.maxSize));
		
		setImageSize(backIp.getWidth(), backIp.getHeight());

		setRois();
        tracer = new TMTracer(backIp,rois);
		previousPosition = 5;

		String type = Parameter.getString(TMParameter.testType);
		if(type.equals("RR")){
			testType = "Right_Discrimination(Offline)";
			stateManager = new StateManager(StateManager.TestType.RIGHT);
		}else if(type.equals("LL")){
			testType = "Left_Discrimination(Offline)";
			stateManager = new StateManager(StateManager.TestType.LEFT);
		}else if(type.equals("FA")){
			testType = "Forced_Alternation(Offline)";
			stateManager = new StateManager(StateManager.TestType.FORCED_ALTERNATION);
		}else if(type.equals("SA")){
			testType = "Spontaneous_Alternation(Offline)";
			stateManager = new StateManager(StateManager.TestType.SPONTANEOUS_ALTERNATION);
		}
	}

	public void setRois(){
        final String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir) + System.getProperty("file.separator");

        for(int i=0; i < 6; i++){
	     	String fileName = "Cage Field"+(i+1)+".roi"; 
	     	RoiDecoder decoder = new RoiDecoder(path+fileName);
	     	try {
	     		Roi roi = decoder.getRoi();
				rois[i] = roi;
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	@Override
	public boolean binUsed(){return false;}

	@Override
	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		if(xyaData != null){
			prevXyaData = xyaData[0];
		}
		xyaData = ((TMImageManager)imgManager).analyzeParticleOnu(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	@Override
	public void calculate(int currentSlice){
		if(currentSlice > 0){
		    totalDistance += getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					                        prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);

		    final int currentPosition = getCurrentRoiNumber(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

		    analyzeState(currentPosition);

		    totalSlice++;

		    tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
	    tracer.setPrevXY(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}

	private int getCurrentRoiNumber(int x,int y){
        for(int i=0;i<6;i++){
            Roi roi = rois[i];
            if(roi.contains(x,y)){
                return i+1;
            }
        }
        
        return -1;
    }

	private void analyzeState(int position){
		if(position == -1 || position == previousPosition) return;
		stateManager.fireTrigger(position);
		previousPosition = position;
	}

	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	@Override
	public void interruptAnalysis(){
		state = NOTHING;
	}

	@Override
	public void nextBin(){}

	@Override
	public String getInfo(String subjectID, int sliceNum){
		String info = subjectID +tab+ getElapsedTime(sliceNum);
        return info;
	}

	@Override
	public String getXY(int sliceNum){
		String xyData = sliceNum +tab+ xyaData[0][X_CENTER] +tab+ xyaData[0][Y_CENTER] +tab+
                (xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA])) ;
        return xyData;
	}

	@Override
	public String[] getResults(){
		double totalChoice = stateManager.getChoiceRightNum() + stateManager.getChoiceLeftNum();
		double correct =  stateManager.getCorrectNum();

		if(testType.equals("LL")){
			
		}
		String[] results = new String[9];
		results[0] =  testType;//TestType
		results[1] =  ""+ (Math.round((totalSlice/rate)*10)/10.0); //Latency(sec)
		results[2] =  ""+ (int)totalChoice; //NumberOfCoices
		results[3] =  ""+ stateManager.getChoiceRightNum(); //NumberOfChoices_Right
		results[4] =  ""+ stateManager.getChoiceLeftNum(); //NumberOfCoices_Left
		results[5] =  ""+ (int)correct; //Correct
		results[6] =  ""+ (int)(totalChoice - correct); //Error
		results[7] =  ""+ (Math.round(((correct/totalChoice)*100)*10)/10.0); //CorrectPercentage
		results[8] =  ""+ Math.round(totalDistance * 10.0) / 10.0;//TotalDistance(cm)
		return results;
	}

	@Override
	public String[] getBinResult(int option){return null;}
}