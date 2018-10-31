package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import java.awt.Color;
import java.util.Arrays;

import behavior.image.process.ImageManager;
import behavior.setup.parameter.HCParameter;
import behavior.setup.parameter.Parameter;

/**
 * Home Cage 2匹用Analyze
 */
public class HC2Analyzer extends HCAnalyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int XOR_AREA = 0;
	public static final int PARTICLE = 1;

	private int[][] mouseplot;
	private double nowParticleNumber, tempParticleNumber;
	private HC2Calculator calc;

	private double totalXorArea;

	public HC2Analyzer(ImageProcessor backIp, String StartTime){
		imgManager = new ImageManager(backIp.duplicate()); //とりあえず複製
		// tracer = new Tracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());
		calc = new HC2Calculator(backIp.getWidth(),backIp.getHeight());
		this.StartTime = StartTime;
		binResult = new double[2];
		canSave = false;

	}

	protected void initializeArrays(){
		mouseplot = new int[2][4];
		for(int i = 0; i < 2; i++)
			Arrays.fill(mouseplot[i], 0);
	}


	public void calculate(int currentSlice){
		calc.setPlot(xyaData);
		if(currentSlice > 0){
			currentBinSlice++;

			mouseplot = calc.returnplot();
			nowParticleNumber = calc.getParticleNumber();
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


	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + StartTime + "\t" + getElapsedTime(sliceNum) + "\t"
		+ mouseplot[0][X] + "\t" + mouseplot[0][Y] + "\t" + mouseplot[1][X] + "\t" + mouseplot[1][Y] + "\t" + (int) nowParticleNumber + " / 2" + "\t" + currentXorArea;
	}

	/*by okazawa*/
	public String getXY(int sliceNum){
		int[][] plot = calc.returnplotForXYData();
		StringBuilder xyBuffer = new StringBuilder(sliceNum + "\t" + plot[0][X] + "\t" + plot[0][Y] + "\t"
				+ plot[0][AREA] + "\t" + state);
		if(plot.length > 1)
			xyBuffer.append("\n" + sliceNum + "\t" + plot[1][X] + "\t" + plot[1][Y] + "\t"
					+ plot[1][AREA] + "\t" + state);
		for(int i = 2; i < plot.length; i++){
			if(plot[i] == null)
				continue; //応急処置
			xyBuffer.append("\n"+ sliceNum +"\t"+ plot[i][X] +"\t"+ plot[i][Y] +"\t" 
					+ plot[i][AREA] +"\t"+ state + "\t" + "f");
		}

		xyData = xyBuffer.toString();

		return xyData;
	}

	public ImageProcessor getSubtractImage(){
		ImageProcessor ip = imgManager.getIp(ImageManager.SUBTRACT_IMG).convertToRGB();
		ip.setColor(Color.red);

		int[][] plot = calc.returnplotForXYData();
		if(plot[0] != null){
			ip.drawLine(plot[0][X] - 2, plot[0][Y], plot[0][X] + 2, plot[0][Y]);
			ip.drawLine(plot[0][X], plot[0][Y] - 2, plot[0][X], plot[0][Y] + 2);
		}

		if(plot.length > 1){
			ip.drawLine(plot[1][X] - 2, plot[1][Y], plot[1][X] + 2, plot[1][Y]);
			ip.drawLine(plot[1][X], plot[1][Y] - 2, plot[1][X], plot[1][Y] + 2);
		}

		return ip;
	}

	public String[] getResults(){
		String[] results = new String[1];
		results[0] = String.valueOf(Math.round(Parameter.getInt(HCParameter.duration) * 10.0)/10.0);   //解析全体のduration
		return results;
	}

}






