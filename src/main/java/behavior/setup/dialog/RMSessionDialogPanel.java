package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.io.RMReferenceManager;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;
import behavior.util.rmconstants.RMConstants;

/**
 * Session の名前を入力。
 */
public class RMSessionDialogPanel extends AbstractDialogPanel implements ActionListener{

	public String id = "Input_Session"; //デフォルト表示
	private JComboBox sessionList;
	private JComboBox referenceList;
	private int type;
	private String sep =System.getProperty("file.separator");

	public String getDialogName(){
		return "Session Name";
	}

	public RMSessionDialogPanel(DialogManager manager, int type){
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

		if(type == Setup.ONLINE){
		    try{
		        RMReferenceManager ref = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.project)
		    		                             +sep+"References"+sep+referenceList.getSelectedItem().toString()+".txt");
		        String[][] IDs = ref.getIDsAndAlignment();
		        if(IDs.length == 0){
		            BehaviorDialog.showErrorDialog(this, "Can't find any mouseID.(in selected reference file)");
	                return false;
	            }
		    }catch(IOException e){
			    e.printStackTrace();
		    }
		}

		fm.setSessionID(input);
		manager.setSessionID(input);

		if(type == Setup.ONLINE && (new File(fm.getPath(FileManager.sessionPath)).exists())){
			if(!BehaviorDialog.showWarningDialog(this, "The Session name is already exist."+"\n"+"Delete old file?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}

		if(type == Setup.ONLINE)
			FileManager.getInstance().setReferenceFile(referenceList.getSelectedItem().toString());

		// OFFLINE の場合は、解析対象の画像が存在するかどうかここで調べておく。
		// 問題なければ Subject ID もセットしてしまう。
		String[] subjectID = null;
		if(type == Setup.OFFLINE){
			if(RMConstants.isReferenceMemoryMode()){
			    try{
				    RMReferenceManager refManager = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.sessionPath));
				    String[][] subIDsAndAlignment = refManager.getIDsAndAlignment();

				    if(subIDsAndAlignment.length == 0){
					    BehaviorDialog.showErrorDialog(this, "Can't find any subjectID(for Reference)");
				        return false;
				    }

				    subjectID = new String[subIDsAndAlignment.length];
				    for(int i=0;i<subjectID.length;i++)
					    subjectID[i] = subIDsAndAlignment[i][RMReferenceManager.ID];
			    }catch(Exception e){
				    BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.sessionPath));
				    return false;
			    }
			}else{
				try{
				    RMReferenceManager refManager = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.sessionPath));
				    List<String> subIDs = refManager.getWorkingIDs();

				    if(subIDs.size() == 0){
					    BehaviorDialog.showErrorDialog(this, "Can't find any subjectID(for Working)");
				        return false;
				    }

				    subjectID = new String[subIDs.size()];
				    Iterator<String> it=subIDs.iterator();
				    for(int i=0;it.hasNext();i++)
					    subjectID[i] = it.next();
			    }catch(Exception e){
				    BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.sessionPath));
				    return false;
			    }
			}

			String path = FileManager.getInstance().getPath(FileManager.ImagesDir) + "/";
		 	for(int i = 0; i < subjectID.length;i++){
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