package behavior.setup.dialog;

import java.util.Properties;

import behavior.setup.parameter.BTParameter;
import behavior.setup.parameter.Parameter;

public class BTParameterDialog extends ParameterDialogPanel{
	private Parameter param;

    public BTParameterDialog(DialogManager manager, int program) {
		super(manager, program);
	}

	public void load(Properties properties){
		param = Parameter.getInstance();
		for(int i = 1; param.getVar(i) != null; i++)
			param.getVar(i).load(properties);

		try{
		    String key = properties.getProperty("moving.criterion");
		    if(key != null && properties.getProperty("movement.criterion") == null ){
			    param.getVar(BTParameter.movementCriterion).setVar(Double.parseDouble(key)*Parameter.getInt(BTParameter.rate));
		    }
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}