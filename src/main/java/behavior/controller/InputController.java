package behavior.controller;

import ij.*;

/**�d�C�I Input �̃R���g���[�����s���N���X�B
 *Labjack, Scion �ǂ���̃f�o�C�X�ł��A������g�p����Ζ��Ȃ��B
 */
public class InputController{
	/**Labjack�Ń|�[�gAI���g�p����*/
	public static final int PORT_AI = 0;
	/**Labjack�Ń|�[�gIO���g�p����*/
	public static final int PORT_IO = 1;
	/**Labjack�Ń|�[�gD���g�p����*/
	public static final int PORT_D = 2;

	private AbstractInput device;

	private boolean setupFailed = false;

	private static InputController ic;

	/**
	 *@param type �t�B�[���h�ɂ���C���v�b�g�^�C�v
	 */
	private InputController(int type){
		String classPath = System.getProperty("java.class.path");
		try{
			if(classPath.indexOf("labjack") >= 0){
				device = new LabJackInput(type);
			}else if(classPath.indexOf("scion") >= 0){
				device = new ScionInput();
			}else{
				throw new Exception();
			}
		}catch(Throwable e){
			if(classPath.indexOf("debug") >= 0){
				if(IJ.showMessageWithCancel("debug mode", "start debug mode(use button dialog input)?") == true)
					device = new DebugInput();
				else
					setupFailed = true;
			}else{
				setupFailed = true;
			}
		}
	}

	public static InputController getInstance(int type){
		if(ic == null)
			ic = new InputController(type);
		return ic;

	}

	/**input device �̐ݒ�Ɏ��s�������B���s������ true
	 */
	public boolean setupFailed(){
		return setupFailed;
	}

	//�w�肳�ꂽ�`���l���ɃC���v�b�g�����邩�ǂ�����ԋp�B
	public boolean getInput(int channel){
		return device.getInput(channel);
	}

	/**�C���v�b�g�`���l����������
	 *@param value ����������`���l���ԍ�(�P�`�S)
	 */
	public void clrPortBits(int value){
		device.clear(value);
	}

	public void reset(){
		device.reset();
	}

	public void resetAll(){
		device.resetAll();
	}


	/**�C���v�b�g�f�o�C�X���I��
	 */
	public void close(){
		device.close();
	}

	public AbstractInput getDevice(){
		return device;
	}

//	public void initialize(){
//	refvalue.initialize();
//	}

}
