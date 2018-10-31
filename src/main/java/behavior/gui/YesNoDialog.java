package behavior.gui;

import java.awt.*;
import java.awt.event.*;

import ij.*;
import ij.gui.*;

public class YesNoDialog extends Dialog implements ActionListener{

	private Button yesB,noB;
	private boolean yesPressed,noPressed;
	String title,message;
	Frame parent;
	boolean modal;

	/**
	 引数に親フレーム、ダイアログタイトル、ダイアログ内に表示するメッセージを指定。
	 */
	public YesNoDialog(Frame parent,String title,String message,boolean modal){
		super(parent,title,modal);//引数は親フレーム、タイトル、modalかどうかを示す。modalがtrueの時、他のウィンドウへの入力が不可となる。
		this.parent = parent;
		this.title = title;
		this.message = message;
		this.modal = modal;
		setLayout(new BorderLayout());
		Panel panel1 = new Panel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT,10,10));
		MultiLineLabel dialogMsg = new MultiLineLabel(message);
		dialogMsg.setFont(new Font("Dialog",Font.BOLD,12));
		panel1.add(dialogMsg);
		add("North",panel1);

		Panel panel2 = new Panel();
		panel2.setLayout(new FlowLayout(FlowLayout.RIGHT,15,8));
		yesB = new Button(" Yes ");
		yesB.addActionListener(this);
		panel2.add(yesB);
		noB = new Button(" No ");
		noB.addActionListener(this);
		panel2.add(noB);
		add("South",panel2);
		pack();
		GUI.center(this);
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == yesB){
			yesPressed = true;
		}else if(e.getSource() == noB){
			noPressed = true;
		}
		setVisible(false);
		dispose();
	}

	public void openDialog(){
		new YesNoDialog(parent,title,message,modal);
	}

	public boolean yesPressed(){
		return yesPressed;
	}

	public boolean noPressed(){
		return noPressed;
	}

	public void closeWindows(){
		WindowManager.closeAllWindows();
	}

}