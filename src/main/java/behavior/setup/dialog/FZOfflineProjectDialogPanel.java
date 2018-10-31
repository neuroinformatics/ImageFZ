package behavior.setup.dialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.setup.Program;
import behavior.util.FilenameValidator;

public class FZOfflineProjectDialogPanel extends ProjectDialogPanel{
	private Program program;

	public FZOfflineProjectDialogPanel(DialogManager manager, int type,Program program) {
		super(manager, type);
		this.program = program;
	}

	@Override
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
				if(!subID.startsWith("#"))		//#で始まっている場合はコメントとして扱う。
					subIDNum++;
			reader.close();
			subjectID = new String[subIDNum];

			reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
				
			int num = 0;
				
			while((subID = reader.readLine()) != null){
				if(subID.startsWith("#")) continue;		//#で始まっている場合はコメントとして扱う。
	
                String[] buf = subID.split("\t");
				subjectID[num++] = buf[0];
			}
			reader.close();
		}catch(Exception e){
			BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.sessionPath));
			return false;
		}

		String path2 = FileManager.getInstance().getPath(FileManager.ImagesDir) + "/";
		if(program == Program.FZ){
		    for(int i = 0; i < subjectID.length; i++){
			    if(!new File(path2 + subjectID[i] + ".tif").exists() && !new File(path2 + subjectID[i] + ".tiff").exists()){
				    BehaviorDialog.showErrorDialog(this, "Can't find image file : " + path2 + subjectID[i] + ".tif");
				    return false;
			    }
		    }
		}else if(program == Program.FZS){
			for(int i = 0; i < subjectID.length; i++){
			    if(!new File(path2 + subjectID[i] + "_shock.tif").exists() && !new File(path2 + subjectID[i] + "_shock.tiff").exists()){
				    BehaviorDialog.showErrorDialog(this, "Can't find image file : " + path2 + subjectID[i] + "_shock.tif");
				    return false;
			    }
		    }
		}

		manager.setSubjectID(subjectID);

		if(check.isSelected()){
			if(!FilenameValidator.validate(text.getText())){
			    BehaviorDialog.showErrorDialog(this, "Invalid projecy name.\nproject name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			    return false;
		    }
			FileManager.getInstance().setSaveProjectID(text.getText());
		}else{
			FileManager.getInstance().setSaveProjectID(input);
		}
		return true;
	}
}