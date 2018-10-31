package behavior.image.process;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import java.io.IOException;

import behavior.io.FileManager;
import behavior.setup.parameter.CSIParameter;
import behavior.setup.parameter.Parameter;

public class CSITracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;

	public CSITracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		clearTrace();
	}

	public void clearTrace() {
		byte[] pixel = (byte[])traceIp.getPixels();
		for(int num = 0; num < pixel.length; num++)
			pixel[num] = 0;
	}

	public void setPrevXY(int x,int y){
		this.prevX=x;
		this.prevY=y;
	}

	public void writeTrace(int x,int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevX != 0 && prevY != 0)
			traceIp.drawLine(prevX, prevY, x, y);

		drawRoi(traceIp);
	}

	private void drawRoi(ImageProcessor traceIp){
		traceIp.drawLine(Parameter.getInt(CSIParameter.leftSeparator),0, Parameter.getInt(CSIParameter.leftSeparator), traceIp.getHeight());
		traceIp.drawLine(Parameter.getInt(CSIParameter.rightSeparator),0, Parameter.getInt(CSIParameter.rightSeparator), traceIp.getHeight());
		String sep = System.getProperty("file.separator");
		String roiName = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "LInner.roi";
		Roi roi = null;
		try{
	        roi = new RoiDecoder(roiName).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi.drawPixels(traceIp);
		String roiName2 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "LOuter.roi";
		Roi roi2 = null;
		try{
	        roi2 = new RoiDecoder(roiName2).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi2.drawPixels(traceIp);
		String roiName3 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "RInner.roi";
		Roi roi3 = null;
		try{
	        roi3 = new RoiDecoder(roiName3).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi3.drawPixels(traceIp);
		String roiName4 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "ROuter.roi";
		Roi roi4 = null;
		try{
	        roi4 = new RoiDecoder(roiName4).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi4.drawPixels(traceIp);
	}

	public ImageProcessor getTrace() {
		return traceIp;
	}
}