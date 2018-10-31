package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import behavior.gui.ExtendedJTextField;

public class DoubleVariable extends Variable{
	protected double var, defVar;
	private ExtendedJTextField field;

	public DoubleVariable(String name, String showName, double defVar){
		super(name, showName);
		var = this.defVar = defVar;
	}

	public void setProperties(Properties prop){
		prop.setProperty(name, String.valueOf(var));
	}

	public void loadProperties(Properties prop){
		String value = prop.getProperty(name, String.valueOf(defVar));
		var = Double.parseDouble(value);
	}

	public void initialize(){
		var = defVar;
	}

	public double getVariable(){
		return var;
	}

	public void load(Properties properties){
		if(field != null && properties != null)
			field.setText(properties.getProperty(name, Double.toString(defVar)));
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(field == null)
			field = new ExtendedJTextField(Double.toString(defVar), 6);
		gbc.gridx = 1;
		panel.add(new JLabel(showName), gbc);
		gbc.gridx = 2;
		panel.add(field, gbc);
		gbc.gridy++;
	}

	public void setVar(){
		this.var = Double.parseDouble(field.getText());
	}

	public void setVar(double var){
		this.var = var;
	}
}