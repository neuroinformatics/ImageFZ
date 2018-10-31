package behavior.image;

import java.util.Vector;

import behavior.gui.BehaviorDialog;
import behavior.gui.SelectCaptureDialog;

import ij.*;
/**
 * �e��L���v�`���[�N���X�̊Ǘ��D
 * �J�����̉摜�͂��̃N���X����Ď擾����D
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
			// Mac�̏ꍇ��QuickTimeCapture���g�p�D
			if(IJ.isMacOSX())
				cap = new QuickTimeCapture();
			else{
				// classpath ����g������̂𔻕ʁD�������݂���Ƃ��͑I���_�C�A���O��\���D
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
	 * ���̃��\�b�h�ŃC���X�^���X���擾����
	 */
	public static ImageCapture getInstance(){
		if(imageCapture == null)
			imageCapture = new ImageCapture();
		return imageCapture;
	}

	/** 
	 * capture �̃Z�b�g�A�b�v�����s�����ꍇ true. true �̎��͏I������悤��
	 */
	public boolean setupFailed(){
		return setupFailed;
	}

	/**
	 * �摜���擾
	 * �Ԃ��摜��invertLut() ��0�𔒁A255�����ɂ������ invert() �ɂ����܂����킹�����̂ł���B
	 * ����́Abehavior �ł� particle �����ŕ\������̂ŁA�������������s�����悢���߂ł���B
	 */
	public synchronized ImagePlus capture(){
		ImagePlus imp = cap.capture();
		
		// JMFCapture �ł��܂� null ���Ԃ��Ă��邱�Ƃ�����
		while(imp == null)
			imp = cap.capture();
		
		imp.getProcessor().invert();
		imp.getProcessor().invertLut();
		return imp;
	}
	
	/*
	 * �ȉ��� native ���\�b�h�Dctrdshow.dll ���g�p���� Brightness/Contrast �𒲐߂���D 
	 */
	public native int getBrightness();
	public native int getContrast();
	public native void setBrightness(int arg);
	public native void setContrast(int arg);
	public native CaptureProperties getCaptureProperties();

	/**
	 * �I������B������g��Ȃ��ƁA���� device �� open ����ۂɃG���[���o��
	 */
	public void close(){
		if(cap != null)
			cap.close();
		imageCapture = null;
	}
	
}
