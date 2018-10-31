package behavior.gui;

import ij.gui.*;

import behavior.setup.Program;

/**�v���W�F�N�g���A�Z�b�V�������A�T�u�W�F�N�gID ��q�˂�_�C�A���[�O�̕\��*/
public class AskingDialog{
	private Program program;
	private int charSize = 13;
	private String projectID, sessionID;
	private String[] subjectID;

	/**@param program �v���O�����ԍ�(behavior.setup.Program ����)*/
	public AskingDialog(Program program){
		this.program = program;
	}

	/**
	 *@return �L�����Z���������ꂽ�� true
	 */
	public boolean showProjectDialog(){
		projectID = "Input Project Name";	//�f�t�H���g�̃v���W�F�N�g��
		GenericDialog gd = new GenericDialog("Project ID");
		gd.addStringField("Project ID:", projectID, charSize);
		gd.showDialog();
		if(gd.wasCanceled()){
			return true;
		}
		projectID = gd.getNextString();
		return false;
	}

	/**
	 *@return �L�����Z���������ꂽ�� true
	 */
	public boolean showSessionDialog(){
		sessionID = "Input_Session";	//�f�t�H���g�̃Z�b�V������
		GenericDialog gd = new GenericDialog("Session");//�uSession�v�Ƃ����^�C�g���̃_�C�A���O�쐬
		gd.addStringField("Session:", sessionID, charSize);//�������̓{�b�N�X�ǉ�
		gd.showDialog();//�_�C�A���O�\��
		if(gd.wasCanceled()){
			return true;
		}
		sessionID = gd.getNextString();//���͕����擾
		return false;
	}

	/**
	 *@param allCage �P�[�W��
	 *@return �L�����Z���������ꂽ�� true
	 */
	public boolean showSubjectDialog(int allCage){
		String programID = program.toString();
		subjectID = new String[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subjectID[cage] = "Subject_ID" + (cage + 1);	//�f�t�H���g�̃T�u�W�F�N�g��
		GenericDialog gd = new GenericDialog("Subject ID");
		for(int cage = 0; cage < allCage; cage++)
			gd.addStringField(programID + (cage + 1) + ":", subjectID[cage], charSize);
		gd.showDialog();
		if(gd.wasCanceled()){
			return true;
		}
		for(int cage = 0; cage < allCage; cage++)
			subjectID[cage] = gd.getNextString();
		return false;
	}

	public String getProjectID(){
		return projectID;
	}

	public String getSessionID(){
		return sessionID;
	}

	public String[] getSubjectID(){
		return subjectID;
	}
}

