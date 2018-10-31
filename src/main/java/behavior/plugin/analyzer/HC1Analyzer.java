package behavior.plugin.analyzer;

import java.awt.Color;
import java.util.Arrays;

import ij.process.ImageProcessor;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.HCParameter;
import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;

/**
 * Home Cage 1匹用Analyze
 */
public class HC1Analyzer extends HCAnalyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int XOR_AREA = 0;
	public static final int DISTANCE = 1;

	private double totalDistance;
	private double totalXorArea;
	private double currentDistance;

	public HC1Analyzer(ImageProcessor backIp, String StartTime){
		imgManager = new ImageManager(backIp.duplicate()); //とりあえず複製
		tracer = new Tracer(backIp);
		setImageSize(backIp.getWidth(), backIp.getHeight());
		this.StartTime = StartTime;
		binResult = new double[2];
		canSave = false;
	}

	public void calculate(int currentSlice){
		if(currentSlice > 0){
			currentBinSlice++;

			if(xyaData[0][FLAG] > 0)
				currentDistance = getDistance(xyaData[0], prevXyaData[0]);
			else currentDistance = 0;
			totalDistance += currentDistance;

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
				binResult[DISTANCE] = totalDistance;
				totalDistance = 0;
				canSave = true;
			}

			tracer.writeTrace(xyaData[0][0], xyaData[0][1]);
		}
	    tracer.setPrevXY(xyaData[0][0], xyaData[0][1]);
	}

	public ImageProcessor getTraceImage(int sliceNum){
		ImageProcessor traceIp = (tracer.getTrace()).duplicate();
		if(sliceNum != 0 && sliceNum % (60 * Parameter.getInt(Parameter.rate)) == 0)
			tracer.clearTrace();
		return traceIp;
	}


	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + StartTime + "\t" + getElapsedTime(sliceNum) + "\t" 
		+ xyaData[0][X] + "\t" + xyaData[0][Y] + "\t" + Math.round(currentDistance * 10.0) / 10.0 + "\t" + currentXorArea;

	}

	public String getXY(int sliceNum){
		StringBuilder xyBuffer = new StringBuilder(sliceNum + "\t" + xyaData[0][X] + "\t" + xyaData[0][Y] + "\t"
				+ xyaData[0][AREA] + "\t" + state);
		if(xyaData.length > 1)
			for(int i = 1; i < xyaData.length; i++){
				if(xyaData[i] == null)
					continue; //応急処置
				xyBuffer.append("\n"+ sliceNum +"\t"+ xyaData[i][X] +"\t"+ xyaData[i][Y] +"\t" 
						+ xyaData[i][AREA] +"\t"+ state + "\t" + "f");
			}

		xyData = xyBuffer.toString();
		return xyData;
	}

	public ImageProcessor getSubtractImage(){
		ImageProcessor ip = imgManager.getIp(ImageManager.SUBTRACT_IMG).convertToRGB();
		ip.setColor(Color.red);
		if(xyaData[0] != null){
			ip.drawLine(xyaData[0][X] - 2, xyaData[0][Y], xyaData[0][X] + 2, xyaData[0][Y]);
			ip.drawLine(xyaData[0][X], xyaData[0][Y] - 2, xyaData[0][X], xyaData[0][Y] + 2);
		}

		return ip;
	}

	public double getDistance(int[] plot, int[] plot2){
		if(plot[X] + plot[Y] == 0 || plot2[X] + plot2[Y] == 0)
			return 0.0;
		if(pixelWidth == 0 || pixelHeight == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		float distX = plot[X] - plot2[X];
		float distY = plot[Y] - plot2[Y];
		double lengthPerPixelHor = (double)Parameter.getInt(Parameter.frameWidth) / pixelWidth;
		double lengthPerPixelVer = (double)Parameter.getInt(Parameter.frameHeight) / pixelHeight;
		double distance = Math.sqrt(Math.pow(distX * lengthPerPixelHor, 2) + Math.pow(distY * lengthPerPixelVer, 2));
		return distance;
	}

	public String[] getResults(){
		String[] results = new String[1];
		results[0] = String.valueOf(Math.round(Parameter.getInt(HCParameter.duration) * 10.0)/10.0);   //解析全体のduration
		return results;
	}

}


