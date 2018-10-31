package behavior.image.process;

import java.util.List;

import ij.gui.Roi;
import ij.process.ImageProcessor;

/**
 * TraceÇêßå‰Ç∑ÇÈÅB
 * RMÇ≈ÇÕTraceâÊñ Ç…RoiÇï\é¶Ç≥ÇπÇÈÅB
 * 
 * @author Butoh
 */
public class RMTracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;
	private List<Roi> rois;

	public RMTracer(ImageProcessor backIp , List<Roi> rois){
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
		for(int i = 0; i < rois.size(); i++){
			Roi roi = (Roi)rois.get(i);
			roi.drawPixels(traceIp);
		}
	}
}