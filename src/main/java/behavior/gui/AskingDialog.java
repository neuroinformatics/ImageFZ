package behavior.gui;

import ij.gui.*;

import behavior.setup.Program;

/**プロジェクト名、セッション名、サブジェクトID を尋ねるダイアローグの表示*/
public class AskingDialog{
	private Program program;
	private int charSize = 13;
	private String projectID, sessionID;
	private String[] subjectID;

	/**@param program プログラム番号(behavior.setup.Program から)*/
	public AskingDialog(Program program){
		this.program = program;
	}

	/**
	 *@return キャンセルが押されたら true
	 */
	public boolean showProjectDialog(){
		projectID = "Input Project Name";	//デフォルトのプロジェクト名
		GenericDialog gd = new GenericDialog("Project ID");
		gd.addStringField("Project ID:", projectID, charSize);
		gd.showDialog();
		if(gd.wasCanceled()){
			return true;
		}
		projectID = gd.getNextString();
		return false;
	}

	/**
	 *@return キャンセルが押されたら true
	 */
	public boolean showSessionDialog(){
		sessionID = "Input_Session";	//デフォルトのセッション名
		GenericDialog gd = new GenericDialog("Session");//「Session」というタイトルのダイアログ作成
		gd.addStringField("Session:", sessionID, charSize);//文字入力ボックス追加
		gd.showDialog();//ダイアログ表示
		if(gd.wasCanceled()){
			return true;
		}
		sessionID = gd.getNextString();//入力文字取得
		return false;
	}

	/**
	 *@param allCage ケージ数
	 *@return キャンセルが押されたら true
	 */
	public boolean showSubjectDialog(int allCage){
		String programID = program.toString();
		subjectID = new String[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subjectID[cage] = "Subject_ID" + (cage + 1);	//デフォルトのサブジェクト名
		GenericDialog gd = new GenericDialog("Subject ID");
		for(int cage = 0; cage < allCage; cage++)
			gd.addStringField(programID + (cage + 1) + ":", subjectID[cage], charSize);
		gd.showDialog();
		if(gd.wasCanceled()){
			return true;
		}
		for(int cage = 0; cage < allCage; cage++)
			subjectID[cage] = gd.getNextString();
		return false;
	}

	public String getProjectID(){
		return projectID;
	}

	public String getSessionID(){
		return sessionID;
	}

	public String[] getSubjectID(){
		return subjectID;
	}
}

