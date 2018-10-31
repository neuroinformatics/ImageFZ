package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

public class CSIBooleanVariable extends BooleanVariable {

	public CSIBooleanVariable(String name,String showName,boolean defVar){
		super(name, showName,defVar);
	}

	public void load(Properties prop){
		if(prop != null)
			this.var = prop.getProperty(name, Boolean.toString(defVar)).equals("true");
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){}
	public void setVar(){}

	public void setVar(boolean var){
		this.var = var;
	}
}