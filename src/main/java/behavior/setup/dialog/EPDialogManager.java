package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.Setup;

public class EPDialogManager extends DialogManager{

	public EPDialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		if(type == Setup.ONLINE)
		setSetCageDialog(new EPSetCageDialogPanel(this, allCage));
	}
}
