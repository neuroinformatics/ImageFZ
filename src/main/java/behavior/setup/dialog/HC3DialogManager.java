package behavior.setup.dialog;

import behavior.setup.Program;

public class HC3DialogManager extends HCDialogManager {

	private int allCage;

	public HC3DialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		this.allCage = allCage;
	}

	protected void addSubDialogs(){
		addDialog(new HC3MouseNumberDialog(this, allCage));
	}
}
