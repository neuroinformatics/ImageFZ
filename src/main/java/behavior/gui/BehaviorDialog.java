package behavior.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * JOptionPaneを使いやすくする。
 * 簡単なダイアログの表示機能を提供する。
 */
public class BehaviorDialog {
	public static final int YES_OPTION = 0;
	public static final int NO_OPTION = 1;

	/**
	 * このクラスの機能はすべて static method で提供される。
	 */
	private BehaviorDialog(){
	}

	/**
	 * OKボタンだけの単純なダイアログを表示
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
	 * showMessageDialog()と同じだが、アイコンがErrorになる
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
	 * YesかNoかを問うダイアログ
	 * @return Yesならtrue,NoならFalse
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
	 * showQuestionDialog()と同じだが、アイコンがWarningになる
	 */
	public static boolean showWarningDialog(Component parentComponent, String message, String title, int defaultSelect){
		Object[] options = {"Yes","No"};
		return JOptionPane.showOptionDialog(parentComponent,message,title,JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[defaultSelect]) == YES_OPTION;
	}
}
