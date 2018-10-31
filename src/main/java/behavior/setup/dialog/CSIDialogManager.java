package behavior.setup.dialog;

import behavior.setup.Program;
import behavior.setup.Setup;

public class CSIDialogManager extends DialogManager {
	private String[] offlineRightCageID;
    private String[] offlineLeftCageID;
    private String onlineLeftCageID;
    private String onlineRightCageID;

	public CSIDialogManager(Program program, int type) {
		super(program, type, 1);
		setProjectDialog(new RMReferenceProjectDialogPanel(this, type));
        setSessionDialog(new CSISessionDialogPanel(this, type));
		setSubjectDialog(new CSISubjectDialogPanel(this));
		if(type == Setup.ONLINE)
		setSetCageDialog(new CSISetCageDialogPanel(this));
	}

	public void setOnlineSubCageIDs(final String leftCageID,final String rightCageID){
	   onlineLeftCageID = leftCageID;
	   onlineRightCageID = rightCageID;
	}

	public String getOnlineRightCageID(){
		if(onlineRightCageID == null)
			throw new NullPointerException();

		return onlineRightCageID;
	}

	public String getOnlineLeftCageID(){
		if(onlineLeftCageID == null)
			throw new NullPointerException();

		return onlineLeftCageID;
	}

	public void setOfflineSubCageIDs(final String[] leftCageID,final String[] rightCageID){
		offlineLeftCageID = leftCageID;
		offlineRightCageID = rightCageID;
	}

	public String[] getOfflineRightCageID(){
		if(offlineRightCageID == null)
			throw new NullPointerException();

		return offlineRightCageID;
	}

	public String[] getOfflineLeftCageID(){
		if(offlineLeftCageID == null)
			throw new NullPointerException();

		return offlineLeftCageID;
	}
}