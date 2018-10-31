package behavior.plugin.analyzer;

import java.util.ArrayList;

import ij.*;
import ij.process.ImageProcessor;

import behavior.plugin.analyzer.Episode;
import behavior.plugin.analyzer.EpisodeCalculator;

public class OFCAnalyzer extends OFAnalyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int BIN_DISTANCE = 1;
	public static final int CIRCLE = 4;
	public static final int CENTER_TIME = 2;
	public static final int PARTITION_AREA = 3;
	public static final int BIN_REAR_TIMES = 4;
	public static final int BIN_REAR_SECOND = 5;

	private EpisodeCalculator epCalc;

	//private int[] binRearingTimes;
	//private double[] binRearingSecond;
	//private boolean rearingBefore = false, notRearingBefore = true;

	public OFCAnalyzer(ImageProcessor backIp){
		super(backIp);
		epCalc = new EpisodeCalculator(backIp);
	}

	@Override
	public void initializeArrays(){
		super.initializeArrays();
		/*binRearingTimes = new int[binLength];
		Arrays.fill(binRearingTimes, 0);
		binRearingSecond = new double[binLength];
		Arrays.fill(binRearingSecond, 0);*/
	}

	@Override
	public void calculate(int currentSlice){
		super.calculate(currentSlice);
		if(currentSlice > 0){
			int[] plot = new int[2];
			plot[0] = xyaData[0][X_CENTER];
			plot[1] = xyaData[0][Y_CENTER];
			epCalc.setNextPlot(plot);
		}
		/*if(notRearingBefore == true)
			rearingBefore = false;
		notRearingBefore = true;*/
	}

	/*public void isRearing(){
		binRearingSecond[currentBin] += 1.0 / Parameter.getInt(Parameter.rate);
		if(rearingBefore == false)
			binRearingTimes[currentBin]++;
		rearingBefore = true;
		notRearingBefore = false;
	}*/

	public String getInfo(String subjectID, int sliceNum){
		//int rear = notRearingBefore? 0:1;
		return subjectID +"\t"+ getElapsedTime(sliceNum) +"\t"+ xyaData[0][0] +"\t"+
		           xyaData[0][1] +"\t"+ Math.round(currentDistance*10.0)/10.0;// + "\t" + rear;
	}

	public ImageProcessor getEpisodeTrace(){
		return epCalc.getEpisodeTrace();
	}

	public String[] getCircleResults(){
		int circleNum = 0, leftCircleNum = 0, rightCircleNum = 0;
		for(Episode episode : epCalc.getEpisodeList()){
			if(episode.isCircle()){
				circleNum++;
			}
			if(episode.isLeftCircle()){
				leftCircleNum++;
			}else if(episode.isRightCircle()){
				rightCircleNum++;
			}
		}

		String[] results = new String[4];
		results[0] = ""+ epCalc.getEpisodeList().size();
		results[1] = ""+ circleNum;
		results[2] = ""+ leftCircleNum;
		results[3] = ""+ rightCircleNum;

		return results;
	}

	public ArrayList<Episode> getEpisodeList(){
		return epCalc.getEpisodeList();
	}

	public ImageStack getEpisodeTraceStack(){
		return epCalc.getTraceStack();
	}

	@Override
	public String[] getBinResult(int option){
		//if(option != BIN_REAR_TIMES && option != BIN_REAR_SECOND){
			return super.getBinResult(option);
		/*}

		String[] result = new String[binLength];
		for(int num = 0; num < binLength; num++){
			switch(option){
			    case BIN_REAR_TIMES:  result[num] = ""+ binRearingTimes[num]; break;
			    case BIN_REAR_SECOND: result[num] = ""+ Math.round(binRearingSecond[num]*10.0)/10.0; break;
			}
		}
		if(state == INTERRUPT){
			result[binLength-1] += "(" + currentBinSlice + "frame )";
		}

		return result;*/
	}
}