package behavior.setup.dialog;

import behavior.setup.Program;

public class LDDialogManager extends DialogManager {

	public LDDialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		//TSëºÇ∆ìùçá
		//setSubjectDialog(new LDSubjectDialogPanel(this, allCage));
		setSetCageDialog(new LDSetCageDialogPanel(this, allCage));
		setSubjectDialog(new LDSubjectDialogPanel(this, allCage));
	}

}