package behavior.gui;

import ij.gui.GenericDialog;

public class WMDialog{
	private String subjectID;
	private int trial;
	private int charSize = 13;

	public WMDialog(String subjectID,int trial){
		this.subjectID = subjectID;
		this.trial = trial;
	}

	public boolean showSubjectDialog(){
		GenericDialog gd = new GenericDialog("Subject ID");
		gd.addStringField("NextSubject" + ":", subjectID, charSize);
		gd.addNumericField("NextTrial" + ":", trial, 0);
		gd.showDialog();
		if(gd.wasCanceled()){
			return true;
		}
		subjectID = gd.getNextString();
		trial = (int)gd.getNextNumber();
		return false;
	}

	public String getSubjectID(){
		return subjectID;
	}

	public int getTrial(){
		return trial;
	}
}
