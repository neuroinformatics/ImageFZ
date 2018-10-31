package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class TMStringVariable extends Variable{
	protected String var, defVar;
	private JComboBox type;

	public TMStringVariable(String name, String showName, String defVar){
		super(name, showName);
		var = this.defVar = defVar;
	}

	public void setProperties(Properties prop){
		prop.setProperty(name, var);
	}

	public void loadProperties(Properties prop){
		var = prop.getProperty(name, defVar);
	}

	public void initialize(){
		var = defVar;
	}

	public String getVariable(){
		return var;
	}

	public void load(Properties properties){
		if(type != null && properties != null)
			type.setSelectedItem(convert(properties.getProperty(name, defVar)));
	}

	public void addComponent(JPanel panel, GridBagConstraints gbc){
		if(type == null)
			type = new JComboBox();
		type.addItem("Left_Discrimination");
		type.addItem("Right_Discrimination");
		type.addItem("Forced_Alternation");
		type.addItem("Spontaneous_Alternation");
		gbc.anchor = GridBagConstraints.LINE_END;
		gbc.gridx = 1;
		panel.add(new JLabel(showName), gbc);
		gbc.gridy++;
		panel.add(new JPanel(), gbc);
		gbc.gridx = 2;
		panel.add(type, gbc);
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.LINE_END;
	}

	public void setVar(){
		this.var = convert(""+type.getSelectedItem());
	}

	private String convert(String item){
		if(item.equals("Left_Discrimination")){
			return "LL";
		}else if(item.equals("Right_Discrimination")){
			return "RR";
		}else if(item.equals("Forced_Alternation")){
			return "FA";
		}else if(item.equals("Spontaneous_Alternation")){
			return "SA";
		}else if(item.equals("LL")){
			return "Left_Discrimination";
		}else if(item.equals("RR")){
			return "Right_Discrimination";
		}else if(item.equals("FA")){
			return "Forced_Alternation";
		}else if(item.equals("SA")){
			return "Spontaneous_Alternation";
		}
		
		return null;
	}
}