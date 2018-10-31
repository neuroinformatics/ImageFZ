package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.setup.BMReferenceSetup;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;

/**
 * Project の名前を入力する。
 * ONLINE の場合は新規に作成するか既存のものを選択。
 * Referenceファイルがなければ作成するDialogを表示。
 * OFFLINE の場合は既存のものを選択、既存の Project がなければエラーを出して終了。
 */
public class BMProjectDialogPanel extends AbstractDialogPanel implements ActionListener {

	private String projectID = "Input Project Name";
	private JComboBox projectList;
	private int type;

	protected JCheckBox check;
	protected JTextField text;

	public String getDialogName(){
		return "Project ID";
	}

	public BMProjectDialogPanel(DialogManager manager, int type){
		super(manager);
		this.type = type;

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Project ID: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		if(type == Setup.OFFLINE)
			projectList = new JComboBox();
		// 既存のものから選択する場合の処理。ここでは、空のJComboBoxを作っているだけ
		else {
			projectList = new ExtendedJComboBox();	// 入力可能な ComboBox
			projectList.getEditor().addActionListener(this);
		}

		File root = new File(FileManager.getInstance().getPath(FileManager.program));
		File[] list = root.listFiles();
		if(list != null)
			for(int i = 0; i < list.length; i++)
				if(list[i].isDirectory())
					projectList.addItem(list[i].getName());
		if(type == Setup.OFFLINE && projectList.getItemCount() == 0){
			// OFFLINE で Project が存在しなければ終了。
			
			BehaviorDialog.showErrorDialog("Can't find any " + manager.getProgram() + " projects.");
			manager.setEndFlag();
		}

		if(type == Setup.ONLINE)
			projectList.setSelectedItem(projectID);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(label, gbc);
		gbc.gridx = 1;
		add(projectList, gbc);
		if(type == Setup.OFFLINE){
		check = new JCheckBox("Save file to another location",false);
	    check.addActionListener(this);
	    JLabel label3 = new JLabel("Enter the parent folder name:");
	    text = new JTextField();
	    text.setEnabled(false);

	    JPanel panel = new JPanel();
	    gbc.gridx = 0;
	    gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		add(panel, gbc);
	    gbc.gridx = 0;
	    gbc.gridy++;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 4;
	    add(check, gbc);
	    gbc.gridy++;
	    add(label3, gbc);
	    gbc.gridy++;
	    add(text,gbc);
		}
	}

	public void postprocess(){
		projectList.requestFocus();
	}

	private String getProjectID() {
		if(type == Setup.ONLINE)
			return ((ExtendedJComboBox)projectList).getText();
		else
			return (String)projectList.getSelectedItem();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext() {    
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
		
		file = new File(FileManager.getInstance().getPath(FileManager.ReferencesDir));
        if(!file.exists())
        	file.mkdirs();
        
        do{ // Reference が存在しないと先に進まない
        	File[] list = file.listFiles(new FileFilter(){
    			public boolean accept(File pathname){
    				String path = pathname.getPath();
    				if(path.substring(path.length() - 4).equals(".txt")/* && BMReferenceValidator.validate(path)*/)
    					return true;
    				else
    					return false;
    			}
    		});
        	
        	if(list.length > 0)
        		break;
        	else{
        		if(BehaviorDialog.showQuestionDialog(this, "Reference files don't exist.\nSetup Reference?"))
        			new BMReferenceSetup(manager, input);
        		else
        			return false;
        	}
        } while(true);
        

		if(type == Setup.OFFLINE){
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

			if(check.isSelected()){
				if(!FilenameValidator.validate(text.getText())){
				    BehaviorDialog.showErrorDialog(this, "Invalid project name.\nproject name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
				    return false;
			    }
				FileManager.getInstance().setSaveProjectID(text.getText());
			}else{
				FileManager.getInstance().setSaveProjectID(input);
			}
		}

		return true;
	}

	public void actionPerformed(ActionEvent e) {
		if(type == Setup.OFFLINE && e.getSource()==check){
			text.setEnabled(check.isSelected());
		}else{
		manager.getNextButton().doClick();
		}
	}
}