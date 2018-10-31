package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;


public class ThresholdBooleanVariable extends BooleanVariable {

	public ThresholdBooleanVariable(String name, String showName, boolean defVar) {
		super(name, showName, defVar);
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
	}

	public void setVar(boolean var){
		this.var = var;
	}

	public boolean getDefVar(){
		return defVar;
	}
}
