package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;

public class CSISessionDialogPanel extends AbstractDialogPanel implements ActionListener,PopupMenuListener{
	public String id = "Input_Session"; //デフォルト表示
	private JComboBox sessionList;
	private JComboBox referenceList;
	private int type;

	private static final int POPUP_MIN_WIDTH = 320;
	private boolean adjusting = false;


	public String getDialogName(){
		return "Session Name";
	}

	public CSISessionDialogPanel(DialogManager manager, int type){
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
		JLabel label2 = new JLabel("Reference: ");
		label2.setHorizontalAlignment(JLabel.RIGHT);

		if(type == Setup.ONLINE){
			sessionList = new ExtendedJComboBox();
		    sessionList.getEditor().addActionListener(this);

            referenceList= new JComboBox();
	        referenceList.addPopupMenuListener(this);
	        File path2 = new File(FileManager.getInstance().getPath(FileManager.ReferencesDir));
			File[] list2 = path2.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					String name = pathname.getName();
					if(name.length() >= 4 && name.substring(name.length()-4).equals(".txt"))	// .txt なファイルのみ受け付ける
						return true;
					else
						return false;
				}
			});

			for(int i = 0; i < list2.length; i++){
				referenceList.addItem(list2[i].getName().substring(0, list2[i].getName().length()-4));
			}
			referenceList.setPreferredSize(new Dimension(280,25));
		}else{
			sessionList = new JComboBox();
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
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(10, 0, 10, 0);
		add(label, gbc);
		gbc.gridy++;
		add(sessionList, gbc);
		if(type == Setup.ONLINE){
		    gbc.gridy++;
		    add(label2, gbc);
		    gbc.gridy++;
		    add(referenceList, gbc);
		}
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

		if(type == Setup.ONLINE && (new File(fm.getPath(FileManager.sessionPath)).exists())){
			if(!BehaviorDialog.showWarningDialog(this, "The Session name is already exist."+"\n"+"Delete old file?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}

		if(type == Setup.ONLINE)
		    FileManager.getInstance().setReferenceFile(referenceList.getSelectedItem().toString());

		if(type == Setup.OFFLINE){
			String[] subjectID = null;
			String[] leftCageID = null;
			String[] rightCageID = null;
			try{
				BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
				int subIDNum = 0;
				while(reader.readLine() != null)
					subIDNum++;
				reader.close();
				subjectID = new String[subIDNum];
				leftCageID = new String[subIDNum];
				rightCageID = new String[subIDNum];

				reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
				String bufID;
				int num = 0;
				while((bufID = reader.readLine()) != null){
					String[] subID = bufID.split("\t");
					if(subID.length!=3){
						BehaviorDialog.showErrorDialog(this, "Selected session file is incorrect.");
						reader.close();
						return false;
					}
					subjectID[num] = subID[0];
					leftCageID[num] = subID[1];
					rightCageID[num] = subID[2];
					num++;
				}
				reader.close();
			}catch(Exception e){
				BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.referencePath));
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
			((CSIDialogManager)manager).setOfflineSubCageIDs(leftCageID,rightCageID);
		}

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}

	public void popupMenuWillBecomeVisible(PopupMenuEvent e){
		if(adjusting) return;
	    JComboBox combo = (JComboBox)e.getSource();
	    Dimension size  = combo.getSize();
	    if(size.width>=POPUP_MIN_WIDTH) return;
	    adjusting = true;

	    combo.setSize(POPUP_MIN_WIDTH, size.height);
	    combo.showPopup();
	    combo.setSize(size);

	    adjusting = false;
	}

	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
	public void popupMenuCanceled(PopupMenuEvent e) {}
}