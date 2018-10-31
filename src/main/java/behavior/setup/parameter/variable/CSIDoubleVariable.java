package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

public class CSIDoubleVariable extends DoubleVariable{
		public CSIDoubleVariable(String name, String showName, double defVar){
			super(name, showName, defVar);
		}

		public void load(Properties prop){
			if(prop != null)
				this.var = Double.parseDouble(prop.getProperty(name, Double.toString(defVar)));
		}

		public void addComponent(JPanel panel, GridBagConstraints gbc){}
		public void setVar(){}

		public void setVar(double var){
			this.var = var;
		}
}