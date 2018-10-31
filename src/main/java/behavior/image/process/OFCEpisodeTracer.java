package behavior.image.process;

import ij.process.ImageProcessor;

public class OFCEpisodeTracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;
	private int roiRadius;

	public OFCEpisodeTracer(ImageProcessor backIp,int roiRadius){
		traceIp = backIp.duplicate();
		clearTrace();
		this.roiRadius = roiRadius;
	}

	public void clearTrace(){
		byte[] pixel = (byte[])traceIp.getPixels();
		for(int num = 0; num < pixel.length; num++)
			pixel[num] = 0;
	}

	@Override
	public ImageProcessor getTrace(){
		return traceIp;
	}

	public ImageProcessor getRoiTrace(int[] startPlot){
		if(startPlot==null) return traceIp;

		ImageProcessor bufIp = traceIp.duplicate();
		bufIp.setValue(255);
		for(int w = startPlot[0]-roiRadius; w<startPlot[0]+roiRadius; w++){
			if(w < 0 || w >= bufIp.getWidth()){ continue;}
			int h1 = (int)Math.sqrt(roiRadius * roiRadius - (w - startPlot[0]) * (w - startPlot[0]));
			int h2 = -h1;
			if(h1 + startPlot[1] < bufIp.getHeight()){
				bufIp.drawPixel(w, h1 + startPlot[1]);
			}
			if(h2 + startPlot[1] >= 0){
				bufIp.drawPixel(w, h2 + startPlot[1]);
			}
		}

		return bufIp;
	}

	@Override
	public void setPrevXY(int x, int y) {
		prevX = x;
		prevY = y;
	}
	
	@Override
	public void writeTrace(int x,int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevX != 0 && prevY != 0){
			traceIp.drawLine(prevX, prevY, x, y);
		}
	}

	public void writeState(char state){
		traceIp.drawString(""+state, prevX+3, prevY+3);		
	}
}