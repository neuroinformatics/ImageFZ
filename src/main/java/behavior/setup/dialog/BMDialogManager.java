package behavior.setup.dialog;

import behavior.setup.Program;

public class BMDialogManager extends DialogManager {

	public BMDialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		setProjectDialog(new BMProjectDialogPanel(this, type));
		setSetCageDialog(new BMSetCageDialogPanel(this, allCage, type));
		setSessionDialog(new BMSessionDialogPanel(this, type));
		setSubjectDialog(new BMSubjectDialogPanel(this, allCage));
	}


}
