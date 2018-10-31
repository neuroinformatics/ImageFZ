package behavior.image;

/**
 * キャプチャーデバイスの　Brightness/Contrast に関する情報を格納．
 * {@link ImageCapture#getCaptureProperties()}で取得する．
 */
public class CaptureProperties {
	private int minBrightness;
	private int maxBrightness;
	private int steppingDeltaBrightness;
	private int defaultBrightness;
	private int minContrast;
	private int maxContrast;
	private int steppingDeltaContrast;
	private int defaultContrast;
	
	/**
	 * このクラスのインスタンスは{@link ImageCapture#getCaptureProperties()}でのみ生成できる．
	 */
	private CaptureProperties(){
	}
	
	public int getMinBrightness(){
		return minBrightness;
	}
	
	public int getMaxBrightness(){
		return maxBrightness;
	}
	
	public int getSteppingDeltaBrightness(){
		return steppingDeltaBrightness;
	}
	
	public int getDefaultBrightness(){
		return defaultBrightness;
	}
	
	public int getMinContrast(){
		return minContrast;
	}
	
	public int getMaxContrast(){
		return maxContrast;
	}
	
	public int getSteppingDeltaContrast(){
		return steppingDeltaContrast;
	}
	
	public int getDefaultContrast(){
		return defaultContrast;
	}
}
