package behavior.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * JOptionPane���g���₷������B
 * �ȒP�ȃ_�C�A���O�̕\���@�\��񋟂���B
 */
public class BehaviorDialog {
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;

	/**
	 * ���̃N���X�̋@�\�͂��ׂ� static method �Œ񋟂����B
	 */
	private BehaviorDialog(){
	}

	/**
	 * OK�{�^�������̒P���ȃ_�C�A���O��\��
	 */
	public static void showMessageDialog(Component parentComponent, String message, String title){
		Object[] options = {"OK"};
		JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.OK_OPTION,JOptionPane.INFORMATION_MESSAGE,null,options,options[0]);
	}
	public static void showMessageDialog(Component parentComponent, String message){
		showMessageDialog(parentComponent,message,"Information");
	}
	public static void showMessageDialog(String message){
		showMessageDialog(null,message);
	}

	/**
	 * showMessageDialog()�Ɠ��������A�A�C�R����Error�ɂȂ�
	 */
	public static void showErrorDialog(Component parentComponent, String message, String title){
		Object[] options = {"OK"};
		JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.OK_OPTION,JOptionPane.ERROR_MESSAGE,null,options,options[0]);
	}
	public static void showErrorDialog(Component parentComponent, String message){
		showErrorDialog(parentComponent, message, "Error");
	}
	public static void showErrorDialog(String message){
		showErrorDialog(null,message);
	}

	/**
	 * Yes��No����₤�_�C�A���O
	 * @return Yes�Ȃ�true,No�Ȃ�False
	 */
	public static boolean showQuestionDialog(Component parentComponent, String message, String title, int defaultSelect){
		Object[] options = {"Yes","No"};
		return JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[defaultSelect]) == YES_OPTION;
	}
	public static boolean showQuestionDialog(Component parentComponent, String message){
		return showQuestionDialog(parentComponent, message, "Confirm", YES_OPTION);
	}
	public static boolean showQuestionDialog(String message, int defaultSelect){
		return showQuestionDialog(null,message,"Confirm",defaultSelect);
	}

	/**
	 * showQuestionDialog()�Ɠ��������A�A�C�R����Warning�ɂȂ�
	 */
	public static boolean showWarningDialog(Component parentComponent, String message, String title, int defaultSelect){
		Object[] options = {"Yes","No"};
		return JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[defaultSelect]) == YES_OPTION;
	}
}
