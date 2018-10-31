package behavior.setup.dialog;

import java.util.Properties;

import javax.swing.JPanel;



public abstract class AbstractDialogPanel extends JPanel {

	protected DialogManager manager;

	public AbstractDialogPanel(DialogManager manager){
		this.manager = manager;
	}

	/**
	 * Project ID ����͂������_�ł��̃��\�b�h���Ă΂��B
	 * ��ʂɁA���� Project �́@preference ���ǂݍ��܂�A�e�_�C�A���O�����������邽�߂ɗp����B
	 */
	public abstract void load(Properties properties);

	/**
	 * �e�_�C�A���O�̖��O�i�����j���Ȍ��ɗ^����B
	 */
	public abstract String getDialogName();

	/**
	 *  Next�������ꂽ���ɁAvalid�ȓ��͂�����Ă��邩��
	 *  check�������ꍇ�̓I�[�o���C�h����
	 *  ���͂��ꂽ�f�[�^�̔��f�������ɏ����B	
	 */
	public boolean canGoNext(){
		return true;
	}

	/**
	 * �\������钼�O�ɏ������s�������ꍇ�Ɏg��
	 */
	public void preprocess(){
	}

	/**
	 * �\�����ꂽ����ɏ������s�������ꍇ
	 */
	public void postprocess(){
	}

	/**
     * back���������ꍇ�ɏ������s�������ꍇ
     */
	public void backprocess(){
	}

	/**
	 * Next,Back,Cancel�Ɋւ�炸�s����������
	 */
	public void endprocess(){
	}
}
