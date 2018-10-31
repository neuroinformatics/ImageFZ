package behavior.setup.dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JPanel;

import behavior.io.FileManager;
import behavior.setup.Program;
import behavior.setup.Setup;

public class TMDialogManager extends DialogManager{

	protected void load(){
		FileManager.getInstance().setPropertyFileName("preference.txt");
		String path = FileManager.getInstance().getPath(FileManager.parameterPath);
		Properties properties = new Properties();
		try {
			File file = new File(path);
			if(file.exists())
				properties.load(new FileInputStream(new File(path)));
			else
				properties = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<JPanel> it = dialogs.iterator();
		while(it.hasNext()){
			AbstractDialogPanel dialog = (AbstractDialogPanel) it.next();
			dialog.load(properties);
		}
		it = captureDialogs.iterator();
		while(it.hasNext()){
			AbstractDialogPanel dialog = (AbstractDialogPanel) it.next();
			dialog.load(properties);
		}
	}

	public TMDialogManager(Program program, int type) {
		super(program, type, 1);

		if(type == Setup.OFFLINE){
			setProjectDialog(new TMProjectDialogPanel(this, Setup.OFFLINE));
		    setOfflineSetCageDialog(new TMOfflineSetCageDialogPanel(this));
		}
	}
}