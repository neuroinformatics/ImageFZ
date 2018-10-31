package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;

/**
 * Session の名前を入力。
 */
public class SessionDialogPanel extends AbstractDialogPanel implements ActionListener{

	public String id = "New Session"; //デフォルト表示
	private JComboBox sessionList;
	private int type;

	public String getDialogName(){
		return "Session Name";
	}

	public SessionDialogPanel(DialogManager manager, int type){
		super(manager);
		this.type = type;
	}

	public void preprocess(){
		removeAll();
		createDialog();
	}

	private void createDialog(){
		if(manager.getSessionID() != null)
			id = manager.getSessionID();

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Session: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		if(type == Setup.OFFLINE)
			sessionList = new JComboBox();
		else{
			sessionList = new ExtendedJComboBox();
			sessionList.getEditor().addActionListener(this);
		}
		sessionList.setPreferredSize(new Dimension(280,25));
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
				sessionList.addItem(list[i].getName().substring(0, list[i].getName().length() - 4));
				if(id.equals(sessionList.getItemAt(i)))
					sessionList.setSelectedIndex(i);
			}

		if(type == Setup.ONLINE)
			sessionList.setSelectedItem(id);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_START;
		add(label, gbc);
		gbc.gridy++;
		add(sessionList, gbc);
	}

	public void postprocess(){
		sessionList.requestFocus();
	}

	private String getSessionID(){
		if(type == Setup.ONLINE)
			return ((ExtendedJComboBox)sessionList).getText();
		else
			return (String)sessionList.getSelectedItem();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext(){
		String input = getSessionID();
		FileManager fm = FileManager.getInstance();
		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid session name.\nSession name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			return false;
		}

		fm.setSessionID(input);
		manager.setSessionID(input);

		if(type == Setup.ONLINE && (new File(fm.getPath(FileManager.sessionPath)).exists())){
			if(!BehaviorDialog.showWarningDialog(this, "The Session name is already exist."+"\n"+"Delete old file?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}

		// OFFLINE の場合は、解析対象の画像が存在するかどうかここで調べておく。
		// 問題なければ Subject ID もセットしてしまう。
		String[] subjectID = null;
		if(type == Setup.OFFLINE){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
				int subIDNum = 0;
				String sub;
				while((sub = reader.readLine()) != null){
					if(sub.equals("# null")) continue;
					subIDNum++;
				}
				reader.close();
				subjectID = new String[subIDNum];

				reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
				String subID;
				int num = 0;
				while((subID = reader.readLine()) != null){
					if(subID.equals("# null")) continue;
					subjectID[num++] = subID;
				}
				reader.close();
			}catch(Exception e){
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
		}

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
}