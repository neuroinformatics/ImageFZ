package behavior.setup.dialog;

import behavior.setup.Program;

public class HCDialogManager extends DialogManager {

	public HCDialogManager(Program program, int type, int allCage) {
		super(program, type, allCage);
		setCameraSettingDialog(new HCCameraSettingsDialogPanel(this));
	}
}
