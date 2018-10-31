package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

/*SetCageDialogでセットするコンポーネントを必要としない特殊なパラメーター*/
public class BTIntVariable extends IntVariable{
	public BTIntVariable(String name, String showName, int defVar){
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