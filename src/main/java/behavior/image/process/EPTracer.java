package behavior.image.process;

import java.io.IOException;

import behavior.io.FileManager;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * Traceを制御する。
 * EPではTrace画面にRoiを表示させる。
 * @author Butoh
 */
public class EPTracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;

	/**
	 *@param backIp バックグラウンド画像
	 */
	public EPTracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		clearTrace();
	}

	/**
	 *@param width 扱う画像の大きさ
	 */
	public EPTracer(int width, int height){
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

		drawRoi(traceIp);
	}

	private void drawRoi(ImageProcessor traceIp){
		String sep = System.getProperty("file.separator");
		String roiName = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep+ "center1.roi";
		Roi roi = null;
		try{
	        roi = new RoiDecoder(roiName).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}
		roi.drawPixels(traceIp);
	}

	public ImageProcessor getTrace(){
		return traceIp;
	}
}