package behavior.setup.dialog;

import behavior.setup.Program;

public class CSIOldDialogManager extends DialogManager{
	private String[] _RightCageID;
    private String[] _LeftCageID;

	public CSIOldDialogManager(final Program program,final int type) {
		super(program, type, 1);
		setSessionDialog(new CSIOldSessionDialogPanel(this));
	}

	public void setSubCageIDs(final String[] rightCageID,final String[] leftCageID){
		_RightCageID = rightCageID;
	    _LeftCageID = leftCageID;
	}

	public String[] getRightCageID(){
		if(_RightCageID == null)
			throw new NullPointerException();

		return _RightCageID;
	}

	public String[] getLeftCageID(){
		if(_LeftCageID == null)
			throw new NullPointerException();

		return _LeftCageID;
	}
}