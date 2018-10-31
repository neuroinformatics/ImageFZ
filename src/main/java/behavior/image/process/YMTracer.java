package behavior.image.process;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import java.io.IOException;

import behavior.io.FileManager;

public class YMTracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;
	private String sep = System.getProperty("file.separator");

	public YMTracer(ImageProcessor backIp){
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
		String roiName = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm1.roi";
		Roi roi = null;
		try{
	        roi = new RoiDecoder(roiName).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi.drawPixels(traceIp);
		String roiName2 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm2.roi";
		Roi roi2 = null;
		try{
	        roi2 = new RoiDecoder(roiName2).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi2.drawPixels(traceIp);
		String roiName3 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm3.roi";
		Roi roi3 = null;
		try{
	        roi3 = new RoiDecoder(roiName3).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi3.drawPixels(traceIp);
		String roiName4 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm1Outer.roi";
		Roi roi4 = null;
		try{
	        roi4 = new RoiDecoder(roiName4).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi4.drawPixels(traceIp);
		String roiName5 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm2Outer.roi";
		Roi roi5 = null;
		try{
	        roi5 = new RoiDecoder(roiName5).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi5.drawPixels(traceIp);
		String roiName6 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Arm3Outer.roi";
		Roi roi6 = null;
		try{
	        roi6 = new RoiDecoder(roiName6).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi6.drawPixels(traceIp);
		String roiName7 = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "Center.roi";
		Roi roi7 = null;
		try{
	        roi7 = new RoiDecoder(roiName7).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi7.drawPixels(traceIp);
	}

	public ImageProcessor getTrace() {
		return traceIp;
	}
}