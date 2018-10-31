package behavior.setup;

import behavior.setup.dialog.CSIDialogManager;
import behavior.setup.dialog.CSIOldDialogManager;
/**
 * 
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class CSISetup extends Setup{
	private Program program;
	public CSISetup(Program program, int type, int allCage){
		super(program, type, allCage);
		this.program=program;
	}

	public String getOnlineRightCageID(){
		return ((CSIDialogManager)manager).getOnlineRightCageID();
	}

	public String getOnlineLeftCageID(){
		return ((CSIDialogManager)manager).getOnlineLeftCageID();
	}

	public String[] getOfflineRightCageID(){
		if(program == Program.CSI)
			return ((CSIDialogManager)manager).getOfflineRightCageID();
		else
		    return ((CSIOldDialogManager)manager).getRightCageID();
	}

	public String[] getOfflineLeftCageID(){
		if(program == Program.CSI)
			return ((CSIDialogManager)manager).getOfflineLeftCageID();
		else
		    return ((CSIOldDialogManager)manager).getLeftCageID();
	}
}