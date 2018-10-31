package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Properties;

import javax.swing.AbstractButton;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.setup.parameter.*;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;
import behavior.setup.parameter.variable.ThresholdIntVariable;
import behavior.setup.parameter.variable.Variable;

/**
 * Threshold à»äOÇÃ Parameter Çê›íËÅB
 */
public class ParameterDialogPanel extends AbstractDialogPanel implements ActionListener,KeyListener,ItemListener{
	protected Parameter parameter;

	public String getDialogName(){
		return "Parameter Settings";
	}

	public ParameterDialogPanel(DialogManager manager, int program) {
		super(manager);
		parameter = Parameter.getInstance();
		createDialog();
	}

	public void postprocess(){
		this.getComponent(1).requestFocus();
	}

	private void createDialog(){
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.weighty = 1.0;
		gbc.anchor = GridBagConstraints.LINE_END;

		Variable[] var = parameter.getVar();
		for(int i = 1; var[i] != null; i++)
			var[i].addComponent(this, gbc);

		for(int i = 0; i < getComponentCount(); i++){
			if(getComponent(i) instanceof ExtendedJTextField){
				((ExtendedJTextField)getComponent(i)).addActionListener(this);
				((ExtendedJTextField)getComponent(i)).addKeyListener(this);
			}else if(getComponent(i) instanceof AbstractButton){
				((AbstractButton)getComponent(i)).addItemListener(this);
			}
		}
	}

	public void load(Properties properties) {
		for(int i = 1; parameter.getVar(i) != null; i++)
			parameter.getVar(i).load(properties);
	}

	public boolean canGoNext(){
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

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}

	@Override
	public void itemStateChanged(ItemEvent arg0) {
		manager.setModifiedParameter(true);
	}

	@Override
	public void keyPressed(KeyEvent arg0){
		manager.setModifiedParameter(true);
	}
	@Override
	public void keyReleased(KeyEvent e){}

	@Override
	public void keyTyped(KeyEvent e) {
		manager.setModifiedParameter(true);
	}
}