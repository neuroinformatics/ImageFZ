package behavior.image.process;

import ij.gui.Roi;
import ij.process.ImageProcessor;

public class TMTracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;
	private Roi[] rois;

	public TMTracer(ImageProcessor backIp , Roi[] rois){
		traceIp = backIp.duplicate();
		clearTrace();
		
		this.rois = rois;
	}

	public void clearTrace() {
		byte[] pixel = (byte[])traceIp.getPixels();
		for(int num = 0; num < pixel.length; num++)
			pixel[num] = 0;
	}

	public ImageProcessor getTrace() {
		return traceIp;
	}

	public void setPrevXY(int x,int y){
		this.prevX=x;
		this.prevY=y;
	}

	public void writeTrace(int x,int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevX != 0 && prevY != 0)
			traceIp.drawLine(prevX, prevY, x, y);
		for(int i = 0; i < rois.length; i++){
			Roi roi = rois[i];
			roi.drawPixels(traceIp);
		}
	}
}