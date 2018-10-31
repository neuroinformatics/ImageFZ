package behavior.image.process;

import java.awt.Color;

import ij.process.ImageProcessor;
import ij.process.ColorProcessor;

public class SITracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevXMouseA=0;
	private int prevYMouseA=0;
	private int prevXMouseB=0;
	private int prevYMouseB=0;

	/**
	 *@param backIp バックグラウンド画像
	 */
	public SITracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		traceIp = traceIp.convertToRGB();
		if(!(traceIp instanceof ColorProcessor)){
		    System.out.println("This image do not use ColorProcessor.");
	    }
		clearTrace();
	}

	/**
	 * 現在のトレース画像を真っ白に初期化
	 */
	public void clearTrace(){
		traceIp.setColor(Color.white);
		for(int y=0;y<traceIp.getHeight();y++){
			for(int x=0;x<traceIp.getWidth();x++){
				traceIp.drawPixel(x,y);
			}
		}
	}

	public void setPrevXY(int Ax,int Ay,int Bx,int By){
		prevXMouseA=Ax;
		prevYMouseA=Ay;
		prevXMouseB=Bx;
		prevYMouseB=By;
	}

	public void writeTraceMouseA(int x, int y){
		traceIp.setColor(Color.red);
		if(x != 0 && y != 0 && prevXMouseA != 0 && prevYMouseA != 0)
			traceIp.drawLine(prevXMouseA, prevYMouseA, x, y);
	}

	public void writeTraceMouseB(int x, int y){
		traceIp.setColor(Color.blue);
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