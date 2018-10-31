package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractButton;
import javax.swing.JOptionPane;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.setup.parameter.variable.*;

public class FZOnlineParameterDialogPanel extends ParameterDialogPanel{
	private boolean flag = false;

	public FZOnlineParameterDialogPanel(DialogManager manager, int program){
		super(manager, program);
		flag = false;
	}

	protected void createDialog(){
		try{
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.LINE_END;

		Variable[] var = parameter.getVar();
		for(int i = 1; var[i] != null; i++){
			var[i].addComponent(this, gbc);
		}

		int count = 1;
		for(int i=0; i<getComponentCount(); i++){
            if(getComponent(i) instanceof ExtendedJTextField){
		    	if(count == 1){
		    		((ExtendedJTextField)getComponent(i)).addActionListener(this);
		    		((ExtendedJTextField)getComponent(i)).addKeyListener(new TextMessage());
		    	}else{
				    ((ExtendedJTextField)getComponent(i)).addActionListener(this);
				    ((ExtendedJTextField)getComponent(i)).addKeyListener(this);
				    count++;
		    	}
			}else if(getComponent(i) instanceof AbstractButton){
				((AbstractButton)getComponent(i)).addItemListener(this);
			}
		}
		}catch(Throwable e){
			e.printStackTrace();
		}
	}

	public boolean canGoNext(){
		if(flag){
			flag = false;
			Object[] options = {"OK"};
			JOptionPane.showOptionDialog(this,"ImageJ might not do correctly if you raise rate level","",JOptionPane.OK_OPTION,JOptionPane.WARNING_MESSAGE,null,options,options[0]);
		}
		for(int i = 1; parameter.getVar(i) != null; i++)
			try{
				if(!(parameter.getVar(i) instanceof IntSliderVariable) &&
						!(parameter.getVar(i) instanceof ThresholdIntVariable) &&
						!(parameter.getVar(i) instanceof ThresholdBooleanVariable))
					parameter.getVar(i).setVar();
			} catch(Exception e) {
				BehaviorDialog.showErrorDialog(manager, "Invalid input!!");
				return false;
			}

			return true;
	}

	class TextMessage implements KeyListener{
		@Override
		public void keyPressed(KeyEvent arg0){
			flag = true;
			manager.setModifiedParameter(true);
		}
		@Override
		public void keyReleased(KeyEvent e){}

		@Override
		public void keyTyped(KeyEvent e) {
			flag = true;
			manager.setModifiedParameter(true);
		}
	}
}
