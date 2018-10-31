package behavior.image;

import ij.*;
import ij.process.*;
import java.awt.*;
import java.io.FileInputStream;
import java.util.Properties;

import behavior.plugin.QuickTimeSelectDevice;

import quicktime.*;
import quicktime.std.sg.*;
import quicktime.qd.*;


/**
 * QuickTime を用いてカメラの画像の取得を行う．
 * ImageJのプラグイン QuickTime Capture を元に作成
 */
public class QuickTimeCapture extends AbstractCapture {
	private SequenceGrabber grabber;
	private QDRect cameraSize;
	private QDGraphics gWorld;
	private int[] pixelData;
	private ImagePlus imp;
	private int intsPerRow;
	private int width, height;
	private QTThread QTthread;

	QuickTimeCapture() throws Exception{
		if(setup())
			throw new Exception();
	}

	private boolean setup(){
		try {
			QTSession.open();
			initSequenceGrabber();
			width = cameraSize.getWidth();
			height = cameraSize.getHeight();
			intsPerRow = gWorld.getPixMap().getPixelData().getRowBytes()/4;

			pixelData = new int[intsPerRow*height];
			grabber.setDataOutput( null, quicktime.std.StdQTConstants.seqGrabDontMakeMovie);
			grabber.prepare(true, true);
			grabber.startRecord();

			long i = System.currentTimeMillis();
			// 正常に画像が得られるまで回す。5秒でタイムアウト。
			while(System.currentTimeMillis() - i < 5000 && judgeImage(capture()));
			if(System.currentTimeMillis() - i >= 5000)
				throw new Exception(); 

			QTthread = new QTThread();
			QTthread.start();
		} catch (Exception e) {
			return true;
		}
		return false;
	}

	/**
	 * @return 正常に画像が取得できていればfalseを返す。
	 */
	private boolean judgeImage(ImagePlus image){
		byte[] px = (byte[])image.getProcessor().getPixels();
		byte color = px[0];
		for(int i = 1; i < px.length; i++)
			if(color != px[i]) return false;
		return true;
	}

	ImagePlus capture() {
		try{
			ImageProcessor ip = new ColorProcessor(width, height);
			//long ct = System.currentTimeMillis();
			//while(System.currentTimeMillis() - ct < 1000 / 15){ // 遅れが発生するので調整する。
			grabber.idle();
			grabber.update(null);
			//}
			gWorld.getPixMap().getPixelData().copyToArray(0, pixelData, 0, pixelData.length);

			imp = new ImagePlus("", ip);
			int[] pixels = ip != null ? (int[])ip.getPixels() : null;
			if (intsPerRow!=width) {
				for (int i=0; i<height; i++)
					System.arraycopy(pixelData, i*intsPerRow, pixels, i*width, width);
			} else
				ip.setPixels(pixelData);

			// int[] から byte[] に落とす処理
			ip = ip.convertToByte(false);
			// 以下，最新の ImageJ ではバグがなくなっているが，念のため残す．
			// int 配列のままの場合に byte 配列に直す処理．
			Object pixel = ip.getPixels();
			if(pixel instanceof int[]){
				int[] temp = (int[])pixel;
				byte[] correct = new byte[temp.length];
				for(int i = 0; i < temp.length; i++)
					correct[i] = (byte)temp[i];
				ip.setPixels(correct);
			}
			imp.setProcessor(null, ip);
		} catch (Exception e){
			e.printStackTrace();
		}
		return imp;
	}

	void close(){
		QTthread.end();
		QTSession.close();
	}

	private void initSequenceGrabber() throws Exception{
		grabber = new SequenceGrabber();
		SGVideoChannel channel = new SGVideoChannel(grabber);

		Properties prefs = new Properties();
		try{
			FileInputStream fis = new FileInputStream(QuickTimeSelectDevice.path);
			prefs.load(fis);
			fis.close();
		} catch(Exception e) {
		}

		String device = prefs.getProperty(QuickTimeSelectDevice.deviceKey, channel.getDeviceList(0).getDeviceName(0).getName());
		channel.setDevice(device);

		cameraSize = channel.getSrcVideoBounds();
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		if (cameraSize.getHeight()>screen.height-40) // iSight camera claims to 1600x1200!
			cameraSize.resize(640, 480);
		gWorld = new QDGraphics(cameraSize);
		grabber.setGWorld(gWorld, null);
		channel.setBounds(cameraSize);
		channel.setUsage(quicktime.std.StdQTConstants.seqGrabRecord |
				quicktime.std.StdQTConstants.seqGrabPreview |
				quicktime.std.StdQTConstants.seqGrabPlayDuringRecord);
		channel.setFrameRate(0);
		channel.setCompressorType( quicktime.std.StdQTConstants.kComponentVideoCodecType);		
	}
		

	// アイドル用のクラス。別スレッドで動かす。
	private class QTThread extends Thread{
		private boolean idle;

		public QTThread(){
			idle = true;
		}

		public void run(){
			while(idle){
				try{
					grabber.idle();
					grabber.update(null);
					Thread.sleep(10);
				} catch (Exception e){
				}
			}
		}

		// ループから抜ける。
		// これを呼ばないとImageJ終了まで回り続けるので注意。
		public void end(){
			idle = false;
		}
	}
}
