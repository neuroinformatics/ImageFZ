package behavior.image.process;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**トレースを記述*/
public class Tracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;

	/**
	 *@param backIp バックグラウンド画像
	 */
	public Tracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		clearTrace();
	}

	/**
	 *@param width 扱う画像の大きさ
	 */
	public Tracer(int width, int height){
		traceIp = new ByteProcessor(width, height);
		traceIp.invertLut();
	}

	/**現在のトレース画像を真っ白に初期化
	 */
	public void clearTrace(){
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
	}

	/**トレースの直線を描く
	 *@param px 始点（前回の座標）
	 *@param x 終点（今回の画像）
	 */
	public void writeTrace(int px, int py, int x, int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && px != 0 && py != 0)
			traceIp.drawLine(px, py, x, y);
	}

	public ImageProcessor getTrace(){
		return traceIp;
	}
}