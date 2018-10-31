package behavior.plugin.executer;

import java.awt.event.*;

import ij.IJ;

import behavior.image.ImageCapture;

public abstract class ProgramExecuter{
	protected ImageCapture imageCapture;
	protected boolean ONLINE;

	public ProgramExecuter(){}

	public void run(){
		initialize();
		
		/*****
		�v���O�����Z�b�g�A�b�v�B�����Ŏw�肳�ꂽ�v���O�����ɉ����āA�v���W�F�N�g��
		�p�����[�^�A�T�u�W�F�N�gID�Ȃǂ̐ݒ��ʂ����[�U�ɒ񋟂���B
		 ******/
		if(setup()){
			if(ONLINE)
				imageCapture.close();
			return;
		}
		int trialNum = 1;
		while(true){
			IJ.setKeyUp(KeyEvent.VK_SHIFT);	//use these keys to interrupt the analysis.
			IJ.setKeyUp(KeyEvent.VK_ALT);
			if(run(trialNum)){
				if(ONLINE)
					imageCapture.close();
				break;
			}
			trialNum++;
		}
	}
	
	protected abstract boolean setup();

	protected boolean run(int trialNum){
		run();
		return false;
	}

	protected void initialize(){}
}