package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JLabel;
import javax.swing.JPanel;

import behavior.gui.ExtendedJTextField;

/**
 * HC �� binDuration �p�B
 * min �œ��͂����āA�����ł� sec �ɒ����B
 */
public class HCIntVariable2 extends IntVariable {

	public HCIntVariable2(String name, String showName, int defVar){
		super(name, showName, defVar);
	}

	public void load(Properties properties){
		if(field != null && properties != null){
			int tmp = Integer.parseInt(properties.getProperty(name, Integer.toString(defVar)));
			field.setText(Integer.toString(tmp / 60));
		}
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(field == null)
			field = new ExtendedJTextField(Integer.toString(defVar / 60), 6);
		gbc.gridx = 1;
		panel.add(new JLabel(showName), gbc);
		gbc.gridx = 2;
		panel.add(field, gbc);
		gbc.gridy++;
	}

	public void setVar(){
		this.var = Integer.parseInt(field.getText()) * 60;
	}
}
