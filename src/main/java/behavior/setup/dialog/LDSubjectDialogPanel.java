package behavior.setup.dialog;

import behavior.controller.OutputController;

public class LDSubjectDialogPanel extends SubjectDialogPanel {
	private int allCage;
	
	public LDSubjectDialogPanel(DialogManager manager, int allCage) {
		super(manager, allCage);
		this.allCage = allCage;
	}
	
	public void preprocess() {
		OutputController output = OutputController.getInstance();
		output.setup(OutputController.LD_TYPE);
		//全てのドアを開く。
		for (int cage = 0; cage < allCage; cage++) {
			output.controlOutput(OutputController.CHANNEL[cage]);
		}
	}
}
