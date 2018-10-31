package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.Setup;

public class FZDialogManager extends DialogManager{
	public FZDialogManager(Program program, int type, int allCage){
		super(program, type, allCage);

		if(type == Setup.ONLINE){
			setProjectDialog(new FZProjectDialogPanel(this));
	        setSessionDialog(new FZSessionDialogPanel(this));
	        setParameterDialog(new FZOnlineParameterDialogPanel(this, allCage));
		}else{
			setProjectDialog(new FZOfflineProjectDialogPanel(this,type,program));
		}
	}
}