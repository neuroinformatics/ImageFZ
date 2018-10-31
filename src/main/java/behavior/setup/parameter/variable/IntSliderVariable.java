package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;

import javax.swing.JPanel;

public class IntSliderVariable extends IntVariable{
	protected int min, max;

	public IntSliderVariable(String name, String showName, int defVar, int min, int max){
		super(name, showName, defVar);
		this.min = min;
		this.max = max;
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
	}

	public void setVar(int var){
		this.var = var;
	}
	
	
	public int getDefVar(){
		return defVar;
	}
	
	public int getMin(){
		return min;
	}
	
	public int getMax(){
		return max;
	}
}





