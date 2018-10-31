package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.dialog.DialogManager;

public class BTDialogManager extends DialogManager{

	public BTDialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		//this.program = program;
		//TrialNumber‚Ìİ’è‚ğ’Ç‰Á
		setSubjectDialog(new BTSubjectDialogPanel(this, allCage));
		setParameterDialog(new BTParameterDialog(this, allCage));
		//MainArea,GoalArea‚ğİ’è
		setSetCageDialog(new BTSetCageDialogPanel(this, allCage));
	}
}
