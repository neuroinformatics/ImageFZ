package behavior.setup;

import java.io.File;

import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.setup.dialog.BMSetCageDialogPanel;

public class BMSetup extends Setup {
	public BMSetup(Program program, int type, int allCage) {
		super(program, type, allCage);
	}
	
	/**
	 * session ファイルを書き出す。
	 */
	public void saveSession(){
		String path = FileManager.getInstance().getPath(FileManager.sessionPath);
		if (!new File(path).exists())
			(new FileCreate()).createNewFile(path);
		String[] subjectID = getSubjectID();
		int target = BMSetCageDialogPanel.getTarget();
		(new FileCreate()).write(path, subjectID[0] + " " + Integer.toString(target), true);
		
	}

}
