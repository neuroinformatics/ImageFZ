package behavior.image.process;

import ij.process.ImageProcessor;

public class LDTracer  implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevXMouseA=0;
	private int prevYMouseA=0;
	private int prevXMouseB=0;
	private int prevYMouseB=0;

	/**
	 *@param backIp バックグラウンド画像
	 */
	public LDTracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		clearTrace();
	}

	/**
	 * 現在のトレース画像を真っ白に初期化
	 */
	public void clearTrace(){
		byte[] pixel = (byte[])traceIp.getPixels();
		for(int num = 0; num < pixel.length; num++)
			pixel[num] = 0;
	}

	public void setPrevXY(int Ax,int Ay,int Bx,int By){
		prevXMouseA=Ax;
		prevYMouseA=Ay;
		prevXMouseB=Bx;
		prevYMouseB=By;
	}

	public void writeTraceMouseA(int x, int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevXMouseA != 0 && prevYMouseA != 0)
			traceIp.drawLine(prevXMouseA, prevYMouseA, x, y);
	}

	public void writeTraceMouseB(int x, int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevXMouseB != 0 && prevYMouseB != 0)
			traceIp.drawLine(prevXMouseB, prevYMouseB, x, y);
	}

	public ImageProcessor getTrace(){
		return traceIp;
	}

	@Override
	public void setPrevXY(int x, int y){}

	@Override
	public void writeTrace(int x, int y){}
}