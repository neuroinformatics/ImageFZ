package behavior.setup.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

public class TMProjectDialogPanel extends ProjectDialogPanel{
	public TMProjectDialogPanel(DialogManager manager, int mode) {
		super(manager, mode);
	}

	public boolean canGoNext(){
		String input = getProjectID();

		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager.getInstance().setProjectID(input);
		File file = new File(FileManager.getInstance().getPath(FileManager.project));
		if(!file.exists()){
			if(!BehaviorDialog.showQuestionDialog(this, "make new project?"))
				return false;
			else
				FileManager.getInstance().makeDirectory(manager.getProgram());
		}

		File path = new File(FileManager.getInstance().getPath(FileManager.SessionsDir));
		File[] list = path.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				String name = pathname.getName();
				if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
					return true;
				else
					return false;
			}
		});
		if(list.length == 0){
			BehaviorDialog.showErrorDialog("Can't find any sessions.");
			return false;
		}

	    String sInput = getSessionID();
	    FileManager fm = FileManager.getInstance();
	    if(!FilenameValidator.validate(sInput)){
		    BehaviorDialog.showErrorDialog(this, "Invalid session name.\nSession name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
		    return false;
	    }

	    fm.setSessionID(sInput);
		manager.setSessionID(sInput);

		String[] subjectID = null;

		try{
			BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
			int subIDNum = 0;
			String subID;
			while((subID = reader.readLine()) != null)
				if (!subID.startsWith("#"))		//#で始まっている場合はコメントとして扱う。
					subIDNum++;
			reader.close();
			subjectID = new String[subIDNum];
			reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
			
			int num = 0;
			
			while((subID = reader.readLine()) != null)
				if (!subID.startsWith("#"))		//#で始まっている場合はコメントとして扱う。
					subjectID[num++] = subID;
			reader.close();
		}catch(Exception e){
			BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.sessionPath));
			return false;
		}

		String[] imageID = new String[subjectID.length];
		for(int i=0;i<imageID.length; i++){
			 StringBuilder str = new StringBuilder(subjectID[i]);
			 for(int m=0;m<2;m++){
				 str.setCharAt(str.lastIndexOf("-"),'_');
			 }
			imageID[i] = str.toString();
		}
		String imagePath = FileManager.getInstance().getPath(FileManager.ImagesDir) + File.separator+ sInput + File.separator;
		for(int i = 0; i < subjectID.length; i++){
			if(!new File(imagePath +imageID[i] + ".tif").exists() && !new File(imagePath + imageID[i] + ".tiff").exists()){
				BehaviorDialog.showErrorDialog(this, "Can't find image file : " + imagePath + imageID[i] + ".tif/.tiff");
				return false;
			}
		}

		manager.setSubjectID(subjectID);

		if(check.isSelected()){
			if(!FilenameValidator.validate(text.getText())){
			    BehaviorDialog.showErrorDialog(this, "Invalid project name.\nproject name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			    return false;
		    }
			FileManager.getInstance().setSaveProjectID(text.getText());
		}else{
			FileManager.getInstance().setSaveProjectID(input);
		}

		return true;
	}
}
