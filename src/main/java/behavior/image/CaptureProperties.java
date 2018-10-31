package behavior.image;

/**
 * �L���v�`���[�f�o�C�X�́@Brightness/Contrast �Ɋւ�������i�[�D
 * {@link ImageCapture#getCaptureProperties()}�Ŏ擾����D
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
	 * ���̃N���X�̃C���X�^���X��{@link ImageCapture#getCaptureProperties()}�ł̂ݐ����ł���D
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
