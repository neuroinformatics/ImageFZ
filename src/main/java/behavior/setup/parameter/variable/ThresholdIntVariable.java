package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

public class ThresholdIntVariable extends IntVariable {

	public ThresholdIntVariable(String name, String showName, int defVar) {
		super(name, showName, defVar);
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
	}

	public void setVar(int var){
		this.var = var;
	}

	public int getDefVar(){
		return defVar;
	}
}
