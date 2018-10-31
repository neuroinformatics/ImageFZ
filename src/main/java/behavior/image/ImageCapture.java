package behavior.image;

import java.util.Vector;

import behavior.gui.BehaviorDialog;
import behavior.gui.SelectCaptureDialog;

import ij.*;
/**
 * 各種キャプチャークラスの管理．
 * カメラの画像はこのクラスを介して取得する．
 */
public class ImageCapture {
	private AbstractCapture cap;
	private boolean setupFailed = false;
	private static ImageCapture imageCapture;
	
	public static final String[] DEVICE = {"QuickTime", "JMF", "Scion FG", "Debug"};

	static{
		System.loadLibrary("ctrdshow");
	}
	
	private ImageCapture(){
		String classPath = System.getProperty("java.class.path");
		try{
			// Macの場合はQuickTimeCaptureを使用．
			if(IJ.isMacOSX())
				cap = new QuickTimeCapture();
			else{
				// classpath から使えるものを判別．複数存在するときは選択ダイアログを表示．
				Vector<Integer> select = new Vector<Integer>(0);
				if(classPath.indexOf("QT") >= 0)
					select.add(0);
				if(classPath.indexOf("jmf") >= 0)
					select.add(1);
				if(classPath.indexOf("scion") >= 0)
					select.add(2);
				if(classPath.indexOf("debug") >= 0)
					select.add(3);
				
				int device = -1;
				if(select.size() == 0)
					throw new Exception();
				else if(select.size() == 1)
					device = select.elementAt(0);
				else{
					SelectCaptureDialog dialog = new SelectCaptureDialog(select);
					try{
						while(dialog.isVisible())
							Thread.sleep(200);
					} catch(Exception e){
						e.printStackTrace();
					}
					device = dialog.getSelected();
				}
					
				switch(device){
				case 0:
					cap = new QuickTimeCapture();
					break;
				case 1:
					cap = new JMFCapture();
					break;
				case 2:
					cap = new ScionCapture();
					break;
				case 3:
					cap = new DebugCapture();
					break;
				default:
					setupFailed = true;
				}
			}
		} catch(Exception e){
			e.printStackTrace();
			BehaviorDialog.showErrorDialog("Error setting capture device.");
			setupFailed = true;
		}
	}

	/**
	 * このメソッドでインスタンスを取得する
	 */
	public static ImageCapture getInstance(){
		if(imageCapture == null)
			imageCapture = new ImageCapture();
		return imageCapture;
	}

	/** 
	 * capture のセットアップが失敗した場合 true. true の時は終了するように
	 */
	public boolean setupFailed(){
		return setupFailed;
	}

	/**
	 * 画像を取得
	 * 返す画像はinvertLut() で0を白、255を黒にした上で invert() によりつじつまを合わせたものである。
	 * これは、behavior では particle を黒で表示するので、こうした方が都合がよいためである。
	 */
	public synchronized ImagePlus capture(){
		ImagePlus imp = cap.capture();
		
		// JMFCapture でたまに null が返ってくることがある
		while(imp == null)
			imp = cap.capture();
		
		imp.getProcessor().invert();
		imp.getProcessor().invertLut();
		return imp;
	}
	
	/*
	 * 以下は native メソッド．ctrdshow.dll を使用して Brightness/Contrast を調節する． 
	 */
	public native int getBrightness();
	public native int getContrast();
	public native void setBrightness(int arg);
	public native void setContrast(int arg);
	public native CaptureProperties getCaptureProperties();

	/**
	 * 終了する。これを使わないと、次回 device を open する際にエラーが出る
	 */
	public void close(){
		if(cap != null)
			cap.close();
		imageCapture = null;
	}
	
}
