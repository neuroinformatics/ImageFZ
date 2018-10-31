package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class EPBooleanVariable extends BooleanVariable {
	protected ButtonGroup select;
	private JRadioButton east;
	private JRadioButton north;
	private static boolean openArm = true;

	public EPBooleanVariable(String name, String showName, boolean defVar){
		super(name, showName, defVar);
	}

	public void load(Properties properties){
		if(east != null && properties != null)
			openArm = Boolean.valueOf(properties.getProperty(name, String.valueOf(defVar)));
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(select == null)
			select = new ButtonGroup();
		east = new JRadioButton("East/West", openArm);
	    north = new JRadioButton("North/South", !openArm);
		select.add(east);
		select.add(north);

		gbc.anchor = GridBagConstraints.LINE_START;
		gbc.gridx = 1;
		panel.add(new JLabel(showName), gbc);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridy++;
		panel.add(east, gbc);
		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 2;
		panel.add(north, gbc);
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.LINE_END;
	}

	public void setVar(){
		this.var = east.isSelected();
	}
}