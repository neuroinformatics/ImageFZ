package behavior.util.rmcontroller;

import behavior.util.rmconstants.RMConstants;

public class OpenAllDoorsCommand extends Command {

	public void execute() {
		for(int i=0;i<RMConstants.ARM_NUM;i++){
			RMController.getInstance().openAllDoors();
		}
	}
}