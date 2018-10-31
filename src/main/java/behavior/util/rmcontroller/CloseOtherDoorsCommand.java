package behavior.util.rmcontroller;

import behavior.util.rmconstants.RMConstants;

public class CloseOtherDoorsCommand extends Command {

	private int num;
	
	public CloseOtherDoorsCommand(int num){
		this.num = num;
	}
	protected void execute() {
		RMController.getInstance().open(num);
		int i = 0;
		for(i=0;i<num;i++){
			RMController.getInstance().close(i);
		}
		i++;
		for(int j=i;j<RMConstants.ARM_NUM;j++){
			RMController.getInstance().close(j);
		}
	}
}