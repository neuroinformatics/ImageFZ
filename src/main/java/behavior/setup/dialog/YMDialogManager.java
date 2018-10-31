package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.Setup;

public class YMDialogManager extends DialogManager{
	public YMDialogManager(Program program, int type) {
		super(program, type, 1);
		//MainArea,GoalAreaÇê›íË
		setSetCageDialog(new YMSetCageDialogPanel(this));

		if(type == Setup.OFFLINE)
		    setOfflineSetCageDialog(new YMOfflineSetCageDialogPanel(this));
	}
}