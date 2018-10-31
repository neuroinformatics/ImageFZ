package behavior.controller;

import com.teravation.labjack.LabJack;
import com.teravation.labjack.LabJackFactory;

import ij.*;

/**�d�C�I�A�E�g�v�b�g���Ǘ�����N���X�BLabjack, Scion �ǂ���ɂ��Ή�
 */
public class OutputController {
	/**�S�`���l���ł��邱�Ƃ��w�肷��t�B�[���h*/
	public static final int ALL_CHANNEL = 15;
	/**�e�`���l�����w�肷��t�B�[���h*/
	public static final int[] CHANNEL = {1, 2, 4, 8};

	/**�A�E�g�v�b�g�^�C�v���w�肷��t�B�[���h(LD)*/
	public static final int LD_TYPE = 1;
	/**�A�E�g�v�b�g�^�C�v���w�肷��t�B�[���h*/
	public static final int NORMAL_TYPE = 2;

	private AbstractOutput device = null;

	private static OutputController output;

	private OutputController(){}

	/**�C���X�^���X���擾*/
	public static OutputController getInstance(){
		if(output == null)
			output = new OutputController();
		return output;
	}

	/**�Z�b�g�A�b�v������i�ʏ�̃A�E�g�v�b�g�^�C�v�j
	 *@return ���s������ true
	 */
	public boolean setup(){
		return setup(NORMAL_TYPE);
	}

	/**�Z�b�g�A�b�v������
	 *@param outputType �A�E�g�v�b�g�^�C�v���w�肷��t�B�[���h�l
	 *@return ���s������ true
	 */
	public boolean setup(int outputType){
		if(device != null) return false;

		String classPath = System.getProperty("java.class.path");
		try{
			if(classPath.indexOf("labjack") >= 0){
				LabJack[] labjacks = new LabJackFactory().getLabJacks();
				if(labjacks == null || labjacks.length == 0){
					IJ.showMessage("Error: could not detect labjack");
					return debug();
				}
				device = new LabJackOutput(outputType);
				return false;
			}else if(classPath.indexOf("scion") >= 0){
				device = new ScionOutput();
				return open();
			}else{
				IJ.showMessage("Not found any classPath for output device.");
				return debug();
			}
		}catch(Throwable e){
			IJ.showMessage("Not found any output device.");
			return debug();
		}
	}

	private boolean debug(){
		String classPath = System.getProperty("java.class.path");
		if(classPath.indexOf("debug") != 0){
			if(IJ.showMessageWithCancel("debug mode", "Do you use debug mode(output in console)?") == true){
				device = new DebugOutput();
				return false;
			}else{
				return true;
			}
		}else{
			return true;
		}
	}

	/**�A�E�g�v�b�g�f�o�C�X�̍쓮���J�n
	 */
	private boolean open(){
		return device.open();
	}

	/**�`���l��������������B
	 *@param bits ����������`���l��
	 */
	public void clear(int bits){
		device.clear(bits);
	}

	/**�`���l������A�E�g�v�b�g���o���B
	 *@param bits �A�E�g�v�b�g����`���l��
	 */
	public void controlOutput(int bits){
		device.controlOutput(bits);
	}

	/**�A�E�g�v�b�g�f�o�C�X���I������B
	 */
	public void close(){
		device.close();
	}
}