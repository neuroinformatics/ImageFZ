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

import behavior.controller.OutputController;
import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

public class FZSessionDialogPanel extends AbstractDialogPanel implements ActionListener{
	public String id = "Input_Session"; //デフォルト表示
	private JComboBox sessionList;
	private JComboBox referenceList;

	@Override
	public String getDialogName(){
		return "Session Name";
	}

	public FZSessionDialogPanel(DialogManager manager){
		super(manager);
	}

	@Override
	public void preprocess(){
		removeAll();
		createDialog();
	}

	@Override
	public void postprocess(){
		sessionList.requestFocus();
	}

	private void createDialog(){
		if(manager.getSessionID() != null)
			id = manager.getSessionID();

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Session: ");
		label.setHorizontalAlignment(JLabel.RIGHT);
		JLabel label2 = new JLabel("Reference ");
		label2.setHorizontalAlignment(JLabel.RIGHT);

		sessionList = new ExtendedJComboBox();
		sessionList.setPreferredSize(new Dimension(280,25));
		sessionList.getEditor().addActionListener(this);

        referenceList= new JComboBox();
        referenceList.setPreferredSize(new Dimension(280,25));
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
		referenceList.addItem("#no use");

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

	    gbc.gridy++;
	    add(label2, gbc);
	    gbc.gridy++;
	    add(referenceList, gbc);
	}

	private String getSessionID(){
		return ((ExtendedJComboBox)sessionList).getText();
	}

	@Override
	public void load(Properties properties){}

	@Override
	public boolean canGoNext(){
	    String input = getSessionID();
	    FileManager fm = FileManager.getInstance();

	    if(!FilenameValidator.validate(input)){
		    BehaviorDialog.showErrorDialog(this, "Invalid session name.\nSession name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
		    return false;
	    }

		fm.setSessionID(input);
		manager.setSessionID(input);

		if(new File(fm.getPath(FileManager.sessionPath)).exists()){
			if(!BehaviorDialog.showWarningDialog(this, "The Session name is already exist."+"\n"+"Delete old file?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}

		if(referenceList.getSelectedItem().toString().equals("#no use")){
			FileManager.getInstance().setReferenceFile("#NoUse");
			return true;
		}

		if(!FilenameValidator.validate(referenceList.getSelectedItem().toString())){
			BehaviorDialog.showErrorDialog(this, "Invalid reference file name.\nReference file name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
			return false;
		}

	    FileManager.getInstance().setReferenceFile(referenceList.getSelectedItem().toString());

		try{
		    String line;
		    boolean flag = false;
		    BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.referencePath)));
		    while((line = reader.readLine()) != null){
			    if(line.startsWith("#")) continue;
			    flag =true;
			    break;
		    }
		    if(!flag){
		    	FileManager.getInstance().setReferenceFile("#NoUse");
		    	reader.close();
		    	return true;
		    }
		    reader.close();
		}catch(Exception e){
			e.printStackTrace();
			BehaviorDialog.showErrorDialog(this, "ERROR:Can not load the reference file.");
		}

		if(!referenceList.getSelectedItem().toString().equals("(no use)") && OutputController.getInstance().setup()) return false;

		return true;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
}