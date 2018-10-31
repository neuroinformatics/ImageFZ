package behavior.plugin.analyzer;
import java.awt.Color;
import java.util.Arrays;

import ij.process.ImageProcessor;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.HCParameter;
import behavior.image.process.ImageManager;

/**
 * Home Cage 3匹以上用Analyze
 */
public class HC3Analyzer extends HCAnalyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int XOR_AREA = 0;
	public static final int PARTICLE = 1;

	private int[][] mouseplot;
	private double nowParticleNumber, tempParticleNumber;
	private int mouseNumber;

	private double totalXorArea;

	public HC3Analyzer(ImageProcessor backIp, String StartTime, int mouseNumber){
		imgManager = new ImageManager(backIp.duplicate()); //とりあえず複製
		// tracer = new Tracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());
		this.StartTime = StartTime;
		this.mouseNumber = mouseNumber;
		binResult = new double[2];
		canSave = false;
	}

	protected void initializeArrays(){
		mouseplot = new int[2][4];
		for(int i = 0; i < 2; i++)
			Arrays.fill(mouseplot[i], 0);
	}

	public void calculate(int currentSlice){
		if(currentSlice > 0){
			currentBinSlice++;

			nowParticleNumber = 0;
			for(int i = 0; i < xyaData.length; i++)
				nowParticleNumber += xyaData[i][AREA] >= Parameter.getInt(HCParameter.minSize) ? 1 : 0;
				tempParticleNumber += nowParticleNumber;

				currentXorArea = 0;   
				if(xorXyaData == null){
					for(int i = 0; i < xorXyaData.length; i++)
						Arrays.fill(xorXyaData[i], 0);
				}else{ 
					for(int i = 0; i < xorXyaData.length; i++)					
						currentXorArea += xorXyaData[i][AREA];
					totalXorArea += currentXorArea;
				}

				if(currentBinSlice == Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.binDuration)){
					binResult[XOR_AREA] = totalXorArea;
					totalXorArea = 0;
					binResult[PARTICLE] = tempParticleNumber / (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate));
					tempParticleNumber = 0;
					canSave = true;
				}

		}
	}

	private int[][] sortPlot(int[][] plot){
		int[][] pl = new int[plot.length][4];
		boolean[] flag = new boolean[plot.length];

		Arrays.fill(flag, true);

		// 面積の大きい順に並べ替え
		for(int i = 0; i < plot.length; i++){
			int max = 0;
			for(int j = 1; j < plot.length; j++)
				if(plot[j][AREA] > plot[max][AREA] && flag[j]) max = j;
			pl[i][X] = plot[max][X];
			pl[i][Y] = plot[max][Y];
			pl[i][AREA] = plot[max][AREA];
			pl[i][FLAG] = pl[max][FLAG];
			flag[max] = false;
		}

		return pl;

	}

	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + StartTime + "\t" + getElapsedTime(sliceNum) + "\t" 
		+ (int) nowParticleNumber + " / " + mouseNumber + "\t" + currentXorArea;
	}

	/*by okazawa*/
	public String getXY(int sliceNum){
		int[][] plot = sortPlot(xyaData);
		StringBuilder xyBuffer = new StringBuilder(sliceNum + "\t" + plot[0][X] + "\t" + plot[0][Y] + "\t"
				+ plot[0][AREA] + "\t" + state + (plot[0][AREA] < Parameter.getInt(HCParameter.minSize) ? "\tf" : ""));

		for(int i = 1; i < plot.length; i++){
			if(plot[i] == null)
				continue; //応急処置
			xyBuffer.append("\n"+ sliceNum +"\t"+ plot[i][X] +"\t"+ plot[i][Y] +"\t" 
					+ plot[i][AREA] +"\t"+ state + (plot[i][AREA] < Parameter.getInt(HCParameter.minSize) ? "\tf" : ""));
		}

		xyData = xyBuffer.toString();

		return xyData;
	}

	public ImageProcessor getSubtractImage(){
		ImageProcessor ip = imgManager.getIp(ImageManager.SUBTRACT_IMG).convertToRGB();
		ip.setColor(Color.red);

		int[][] plot = sortPlot(xyaData);
		for(int i = 0; i < plot.length; i++)
			if(plot[i] != null && plot[i][AREA] >= Parameter.getInt(HCParameter.minSize)){
				ip.drawLine(plot[i][X] - 2, plot[i][Y], plot[i][X] + 2, plot[i][Y]);
				ip.drawLine(plot[i][X], plot[i][Y] - 2, plot[i][X], plot[i][Y] + 2);
			}

		return ip;
	}

	public String[] getResults(){
		String[] results = new String[2];
		results[0] = String.valueOf(Math.round(Parameter.getInt(HCParameter.duration) * 10.0)/10.0);   //解析全体のduration
		results[1] = String.valueOf(mouseNumber);
		return results;
	}

}






