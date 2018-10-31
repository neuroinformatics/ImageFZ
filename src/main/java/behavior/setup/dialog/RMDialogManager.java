package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.util.rmconstants.RMConstants;

public class RMDialogManager extends DialogManager{

	public RMDialogManager(Program program, int type) {
		super(program, type, 1);

		if(RMConstants.isReferenceMemoryMode())
		    setProjectDialog(new RMReferenceProjectDialogPanel(this, type));

		if(RMConstants.isReferenceMemoryMode() || type == Setup.OFFLINE)
	        setSessionDialog(new RMSessionDialogPanel(this, type));

		setSubjectDialog(new RMSubjectDialogPanel(this));
		setSetCageDialog(new RMSetCageDialogPanel(this));
	}
}