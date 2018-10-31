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

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
//import javax.swing.JPanel;
//import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileManager;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;

/**
 * Project の名前を入力する。
 * ONLINE の場合は新規に作成するか既存のものを選択。
 * OFFLINE の場合は既存のものを選択、既存の Project がなければエラーを出して終了。
 */
public class ProjectDialogPanel extends AbstractDialogPanel implements ActionListener {

	private String projectID = "Enter the Project ID";
	private JComboBox projectList;
	private int type;
	public String id = "New Session"; //デフォルト表示
	private JComboBox sessionList;
	//private JTextField saveFileName;
	protected JCheckBox check;
	protected JTextField text;

	public String getDialogName(){
		return "Project ID";
	}

	public ProjectDialogPanel(DialogManager manager, int mode){
		super(manager);
		this.type = mode;

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Project ID: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		if(type == Setup.OFFLINE){
			projectList = new JComboBox();
		    projectList.addActionListener(this);
		// 既存のものから選択する場合の処理。ここでは、空のJComboBoxを作っているだけ
		}else{
			projectList = new ExtendedJComboBox();	// 入力可能な ComboBox
			projectList.getEditor().addActionListener(this);
		}

		projectList.setPreferredSize(new Dimension(200,25));

		File root = new File(FileManager.getInstance().getPath(FileManager.program));
		File[] list = root.listFiles();
		if(list != null)
			for(int i = 0; i < list.length; i++)
				if(list[i].isDirectory())
					projectList.addItem(list[i].getName());
		if(type == Setup.OFFLINE && projectList.getItemCount() == 0){
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
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(label, gbc);
		gbc.gridx = GridBagConstraints.RELATIVE;
		gbc.gridwidth = 3;
		add(projectList, gbc);

		if(type == Setup.OFFLINE){
			if(manager.getSessionID() != null)
				id = manager.getSessionID();

		    JLabel label2 = new JLabel("Session: ");
		    label2.setHorizontalAlignment(JLabel.RIGHT);

		    sessionList = new JComboBox();
		    sessionList.setPreferredSize(new Dimension(200,25));

		    File path = new File(FileManager.getInstance().getPath(FileManager.program) +File.separator+ projectList.getItemAt(0).toString() +File.separator+ "Sessions");
		    
		    File[] sList = path.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				String name = pathname.getName();
				if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
					return true;
				else
					return false;
			    }
		    });

		    if(sList != null)
			    for(int i = 0; i < sList.length; i++){
			    	sessionList.addItem(sList[i].getName().substring(0, sList[i].getName().length() - 4));
			    	if(id.equals(sessionList.getItemAt(i)))
					    sessionList.setSelectedIndex(i);
			}

		    gbc.gridx = 0;
		    gbc.gridy++;
			gbc.gridwidth = 1;
			gbc.anchor = GridBagConstraints.WEST;
		    add(label2, gbc);
		    gbc.gridx = GridBagConstraints.RELATIVE;
			gbc.gridwidth = 3;
		    add(sessionList, gbc);

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

	protected String getProjectID() {
		if(type == Setup.ONLINE)
			return ((ExtendedJComboBox)projectList).getText();
		else
			return (String)projectList.getSelectedItem();
	}

	protected String getSessionID(){
		if(type == Setup.OFFLINE)
			//return sessionList.getSelectedValue().toString();
			return sessionList.getSelectedItem().toString();
		else
			return "";
	}

	public void load(Properties properties) {
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
		}

		if(type == Setup.OFFLINE){
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

			String path = FileManager.getInstance().getPath(FileManager.ImagesDir) + File.separator;
			for(int i = 0; i < subjectID.length; i++){
				if(!new File(path + subjectID[i] + ".tif").exists() && !new File(path + subjectID[i] + ".tiff").exists()){
					BehaviorDialog.showErrorDialog(this, "Can't find image file : " + path + subjectID[i] + ".tif/.tiff");
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
		}
		return true;
	}

	public void actionPerformed(ActionEvent e){
		if(type == Setup.OFFLINE && e.getSource()==check){
			text.setEnabled(check.isSelected());
		}else{
			if(type == Setup.ONLINE){
		        manager.getNextButton().doClick();
		    }else if(sessionList!= null){
	    	    File path = new File(FileManager.getInstance().getPath(FileManager.program) +File.separator+ projectList.getSelectedItem().toString() +File.separator+ "Sessions");

		        File[] sList = path.listFiles(new FileFilter(){
			    public boolean accept(File pathname){
				    String name = pathname.getName();
				    if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
					    return true;
				    else
					    return false;
			        }
		        });

		        sessionList.setEnabled(false);
		        sessionList.removeAllItems();
		        // model.removeAllElements();
		        if(sList != null){
			        for(int i = 0; i < sList.length; i++){
				        //model.addElement(sList[i].getName().substring(0, sList[i].getName().length() - 4));
			    	    sessionList.addItem(sList[i].getName().substring(0, sList[i].getName().length() - 4));
				        //if(id.equals(model.getElementAt(i)))
			    	    if(id.equals(sessionList.getItemAt(i))){
					        sessionList.setSelectedIndex(i);
			    	    }
			        }
			    }
		        sessionList.setEnabled(true);
		        //System.out.println("reach");
		    }
	    }
	}
}