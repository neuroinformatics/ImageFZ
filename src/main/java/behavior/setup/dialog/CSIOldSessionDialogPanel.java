package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Properties;
//import java.util.logging.*;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

public class CSIOldSessionDialogPanel  extends AbstractDialogPanel implements ActionListener{
	private JComboBox _sessionList;
	private String _id = "Input_Session"; //デフォルト表示
	//private Logger log = Logger.getLogger("behavior.setup.dialog.CSIOldSessionDialogPanel");

	public String getDialogName(){
		return "Session Name";
	}

	public CSIOldSessionDialogPanel(DialogManager manager){
		super(manager);
	}

	public void preprocess(){
		removeAll();
		createDialog();
		/*try{
		FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.program) + System.getProperty("file.separator") + "ControllerLog.txt", 102400,1);
		fh.setFormatter(new SimpleFormatter());
	    log.addHandler(fh);
	}catch(Exception e){
		e.printStackTrace();
	}*/
	}

	private void createDialog(){
		if(manager.getSessionID() != null)
			_id = manager.getSessionID();

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Session: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		_sessionList = new JComboBox();

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
		if(list != null)
			for(int i = 0; i < list.length; i++){
				_sessionList.addItem(list[i].getName().substring(0, list[i].getName().length() - 4));
				if(_id.equals(_sessionList.getItemAt(i)))
					_sessionList.setSelectedIndex(i);
			}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(label, gbc);
		gbc.gridx = 1;
		add(_sessionList, gbc);
	}

	public void postprocess(){
		_sessionList.requestFocus();
	}

	private String getSessionID(){
		return (String)_sessionList.getSelectedItem();
	}

	public void load(Properties properties){}

	public boolean canGoNext(){
		String input = getSessionID();
		FileManager fm = FileManager.getInstance();
		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid session name.\nSession name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			return false;
		}

		fm.setSessionID(input);
		manager.setSessionID(input);

		String[] subjectID = null;
		String[] rightCageID = null;
		String[] leftCageID = null;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
			int subIDNum = 0;
			while(reader.readLine() != null)
				subIDNum++;
			reader.close();
			subjectID = new String[subIDNum];
			rightCageID = new String[subIDNum];
			leftCageID = new String[subIDNum];

			reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
			String bufID;
			int num = 0;
			while((bufID = reader.readLine()) != null){
				//log.log(Level.INFO, ""+bufID);
				String[] subID = bufID.split(":");
				//log.log(Level.INFO, ""+subID.length);
				subjectID[num] = subID[0];
				rightCageID[num] = subID[1];
				leftCageID[num] = subID[2];
				num++;
			}
			reader.close();
		}catch(Exception e){
			//log.log(Level.INFO, ""+e);
			BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.sessionPath));
			return false;
		}
		String path = FileManager.getInstance().getPath(FileManager.ImagesDir) + "/";
		for(int i = 0; i < subjectID.length; i++){
			if(!new File(path + subjectID[i] + ".tif").exists() && !new File(path + subjectID[i] + ".tiff").exists()){
				BehaviorDialog.showErrorDialog(this, "Can't find image file : " + path + subjectID[i] + ".tif");
				return false;
			}
		}

		manager.setSubjectID(subjectID);
		((CSIOldDialogManager)manager).setSubCageIDs(rightCageID,leftCageID);

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}

}
