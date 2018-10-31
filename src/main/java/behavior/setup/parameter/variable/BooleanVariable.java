package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class BooleanVariable extends Variable{
	protected boolean var, defVar;
	private JCheckBox check;

	public BooleanVariable(String name, String showName, boolean defVar){
		super(name, showName);
		var = this.defVar = defVar;
	}

	public void setProperties(Properties prop){
		prop.setProperty(name, String.valueOf(var));
	}

	public void loadProperties(Properties prop){
		String value = prop.getProperty(name, String.valueOf(defVar));
		Boolean bool = Boolean.valueOf(value);
		var = bool.booleanValue();
	}

	public void initialize(){
		var = defVar;
	}

	public boolean getVariable(){
		return var;
	}

	public void load(Properties properties){
		if(check != null && properties != null)
			check.setSelected(Boolean.valueOf(properties.getProperty(name, String.valueOf(defVar))));
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(check == null)
			check = new JCheckBox("", defVar);
		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 0;
		panel.add(check, gbc);
		gbc.gridx = 1;
		panel.add(new JLabel(showName), gbc);
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.LINE_END;
	}

	public void setVar(){
		this.var = (check.getSelectedObjects() != null);
	}
}