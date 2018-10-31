package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

/**
 * setCageField�Őݒ肷��p�����[�^�[�B
 */
public class BMIntVariable extends IntVariable{
	public BMIntVariable(String name, String showName, int defVar){
		super(name, showName, defVar);
	}
	public void load(Properties prop){
		if(prop != null)
			this.var = Integer.parseInt(prop.getProperty(name, Integer.toString(defVar)));
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){}
	public void setVar(){}

	public void setVar(int var){
		this.var = var;
	}
}