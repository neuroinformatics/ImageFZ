package behavior.image.process;

import ij.process.ImageProcessor;

public interface TracerFrame {

	public void clearTrace();
	public void setPrevXY(int x,int y);
	public void writeTrace(int x,int y);
	public ImageProcessor getTrace();
}