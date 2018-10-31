package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import behavior.gui.ExtendedJTextField;

/**
 * HC Ç…Ç®ÇØÇÈ hour Ç∆ minute ÇàµÇ§ÇΩÇﬂÇÃÇ‡ÇÃÅB
 */
public class HCIntVariable1 extends IntVariable{
	private String text1,text2;
	private ExtendedJTextField field1, field2;

	public HCIntVariable1(String text1, String text2, String showName, int defVar){
		super(showName, showName, 0);
		var = this.defVar = defVar;
		this.text1 = text1;
		this.text2 = text2;
	}

	public void setProperties(Properties prop){
		prop.setProperty(name, String.valueOf(var));
	}

	public void loadProperties(Properties prop){
		String value = prop.getProperty(name, String.valueOf(defVar));
		var = Integer.parseInt(value);
	}

	public void initialize(){
		var = (int)defVar;
	}

	public int getVariable(){
		return (int)var;
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(field1 == null)
			field1 = new ExtendedJTextField(Integer.toString(defVar / 3600), 6);
		if(field2 == null)
			field2 = new ExtendedJTextField(Integer.toString(defVar / 60), 6);
		gbc.gridx = 1;
		panel.add(new JLabel(text1), gbc);
		gbc.gridx = 2;
		panel.add(field1, gbc);
		gbc.gridy++;
		gbc.gridx = 1;
		panel.add(new JLabel(text2), gbc);
		gbc.gridx = 2;
		panel.add(field2, gbc);
		gbc.gridy++;
	}

	public void load(Properties properties){
		if(field1 != null && field2 != null && properties != null){
			String value = properties.getProperty(name, String.valueOf(defVar));
			var = Integer.parseInt(value);
			int temp = var / 60;

			field1.setText(Integer.toString(temp / 60));
			field2.setText(Integer.toString(temp % 60));
		}
	}

	public void setVar(){
		this.var = Integer.parseInt(field1.getText()) * 60 * 60 + Integer.parseInt(field2.getText()) * 60;
	}
}