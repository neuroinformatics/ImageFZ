package behavior.setup;

import ij.IJ;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.ExtendedJComboBox;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;



/**
 * Reference の設定を行う。
 * 
 */
public class BMReferenceSetup extends JDialog {

	private static final String title = "BM Reference Setup";
	private final String sep = System.getProperty("file.separator");
	private final String LINE_SEPARATOR = System.getProperty("line.separator");
	private final String programPath = (IJ.isWindows() ? "" : System.getProperty("user.home")) + sep + "Image_BM";
	
	private Vector dialogs;
	private DialogPanel currentDialog;
	private JPanel buttonPanel;
	private JButton next,back,cancel;
	private int counter;
	private JComboBox targetList;
	
	private String projectID;
	private String referenceFile;
	private SetupDialog setup;
	
	public BMReferenceSetup(Dialog parent, String projectID){
		super(parent, title, true);
		this.projectID = projectID;
		setup();
	}
	public BMReferenceSetup(Frame parent){
		super(parent, title, true);
		setup();
	}
	public BMReferenceSetup(){
		super();
		setTitle(title);
		setup();
	}
	
	private void setup(){
		counter = 0;
		targetList = new JComboBox();
		for (int i = 1; i <= 12; i++)
			targetList.addItem(i + "");
		
		dialogs = new Vector();
		
		if(projectID == null)
			dialogs.add(new ProjectDialog());
		dialogs.add(new ReferenceDialog());
		dialogs.add(setup = new SetupDialog());
		
		buttonPanel = new JPanel(new GridBagLayout());
        next = new ExtendedJButton("Next >");
        back = new ExtendedJButton("< Back");
        back.setEnabled(false);
        cancel = new ExtendedJButton("Cancel");
        next.addActionListener(new NextActionListener());
        back.addActionListener(new BackActionListener());
        cancel.addActionListener(new CancelActionListener());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.LINE_END;
        buttonPanel.add(back, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        buttonPanel.add(next, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.6;
        buttonPanel.add(cancel, gbc);
        
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(buttonPanel, BorderLayout.SOUTH);
        setBounds(350, 150, 350, 280);
        setResizable(false);
        
        showDialog();
	}
	
	private void showDialog(){
        if(counter < 0){
            counter = 0;
        }
        else if(counter >= dialogs.size()){
            setup.save();
            dispose();
            return;
        }
        
        if(currentDialog != null)
            remove(currentDialog);
        
        if(counter < dialogs.size())
            currentDialog = (DialogPanel)dialogs.get(counter);

        currentDialog.preprocess();
        add(currentDialog,BorderLayout.CENTER);
        
        setVisible(true);
        
        currentDialog.postprocess();
        repaint();
	}
	
	
	public class NextActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	if(!currentDialog.canGoNext()){
                return ;
            }
            counter++;
            if(counter == dialogs.size() - 1)
            	next.setText("Save");
            if(counter == 1)
            	back.setEnabled(true);
            showDialog();
        }
    }
    public class BackActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	counter--;
            next.setText("Next >");
            if(counter == 0)
            	back.setEnabled(false);
            showDialog();
        }
    }
    public class CancelActionListener implements ActionListener{
    	public void actionPerformed(ActionEvent e){
    		dispose();
    	}
    }
    
    private abstract class DialogPanel extends JPanel{
    	public boolean canGoNext(){
    		return true;
    	}
    	public void preprocess(){
    	}
    	public void postprocess(){
    	}
    }
    
    /**
     * Step 1 : 対象となるプロジェクトの設定
     */
    private class ProjectDialog extends DialogPanel implements ActionListener{
    	private String defaultProject = "Input Project Name";
        private ExtendedJComboBox projectList;
    	
    	public ProjectDialog(){
    		setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Project ID : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);
    		
    		projectList = new ExtendedJComboBox();
    		File root = new File(programPath + sep);
    		File[] list = root.listFiles();
    		for(int i = 0; i < list.length; i++)
    			if(list[i].isDirectory())
    				projectList.addItem(list[i].getName());
    		
    	    projectList.setSelectedItem(defaultProject);
    	    projectList.getEditor().addActionListener(this);
    	    
    	    GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridx = 0;
    	    gbc.gridy = 0;
    	    gbc.gridwidth = 1;
    	    gbc.gridheight = 1;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    add(label, gbc);
    	    gbc.gridx = 1;
    		add(projectList, gbc);
    	}
    	
    	public void postprocess(){
    		projectList.requestFocus();
    	}
    	
    	public boolean canGoNext() {    
            projectID = projectList.getText();
            
            if(!FilenameValidator.validate(projectID)){
    			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
            
            FileManager.getInstance().setProjectID(projectID);
            String path = programPath + sep + projectID;
            File file = new File(path);
            if(file.exists()){
                return true;
            }
            if(BehaviorDialog.showQuestionDialog(this ,"No such Folder : "+projectID+"\nCreate new project?")){
            	file.mkdirs();
            	new File(path + "/Delays").mkdirs();
            	new File(path + "/Images").mkdirs();
            	new File(path + "/Preference").mkdirs();
            	new File(path + "/References").mkdirs();
            	new File(path + "/Results").mkdirs();
            	new File(path + "/Sessions").mkdirs();
            	new File(path + "/Traces").mkdirs();
            	return true;
            }
            return false;
        }

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }
    
    /**
     * Step 2 : 対象となるReferenceファイルの設定
     */
    private class ReferenceDialog extends DialogPanel implements ActionListener{
    	
    	private ExtendedJComboBox referenceList;
    	
    	public void preprocess(){
        	removeAll();
        	
        	setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Reference File : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);
    		
    		String ref = programPath + sep + projectID + sep + "References";
    		referenceList = new ExtendedJComboBox();
    		File path = new File(ref + sep);
    		//Referencesフォルダが存在しない場合はここで作成
    		if (!path.exists())
    			new File(ref + sep).mkdirs();
    		File[] list = path.listFiles(new FileFilter(){
    			public boolean accept(File pathname){
    				String name = pathname.getName();
    				if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
    					return true;
    				else
    					return false;
    			}
    		});
    		//Referencesフォルダが存在しない場合はここで止まってしまう
    		for(int i = 0; i < list.length; i++)
    			referenceList.addItem(list[i].getName());
    		
    	    if(list.length == 0)
    	    	referenceList.setSelectedItem("reference.txt");
    	    
    	    referenceList.getEditor().addActionListener(this);
    	    
    	    GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridx = 0;
    	    gbc.gridy = 0;
    	    gbc.gridwidth = 1;
    	    gbc.gridheight = 1;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    add(label, gbc);
    	    gbc.gridx = 1;
    		add(referenceList, gbc);
        }
    	
    	public void postprocess(){
    		referenceList.requestFocus();
    	}
    	
    	public boolean canGoNext(){
    		referenceFile = referenceList.getText();
    		if(!FilenameValidator.validate(referenceFile)){
    			BehaviorDialog.showErrorDialog(this, "Invalid reference file name.\nReference file name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
    		
    		if(!referenceFile.substring(referenceFile.length() - 4).equals(".txt"))
    			referenceFile += ".txt";
    		
    		return true;
    	}

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }
    
    /**
     * Step 3 : 実際のSetup
     */
    private class SetupDialog extends DialogPanel{
    	private JTable table;
    	private DefaultTableModel tableModel;
    	private JButton up,down,add,delete;
    	
    	public void preprocess(){
    		removeAll();
    		
    		setLayout(new BorderLayout());
    
    		// 編集するファイル名の表示
    		JPanel filePanel = new JPanel(new GridBagLayout());
    		GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridx = 0;
    	    gbc.gridy = 0;
    	    gbc.gridwidth = 1;
    	    gbc.gridheight = 1;
    	    gbc.weighty = 1.0;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    
    	    JLabel projectLabel = new JLabel("Project : ");
    		JLabel projectName = new JLabel(projectID);
    		JLabel referenceLabel = new JLabel("Reference : ");
    		JLabel referenceName = new JLabel(referenceFile);
    		
    		// ボタン類
    		JPanel buttonPanel = new JPanel(new GridLayout(4, 1, 5, 20));
    		up = new JButton("Up");
    		down = new JButton("Down");
    		add = new JButton("Add");
    		delete = new JButton("Delete");
    		up.addActionListener(new UpActionListener());
    		down.addActionListener(new DownActionListener());
    		add.addActionListener(new AddActionListener());
    		delete.addActionListener(new DeleteActionListener());
    		
    		// 編集用のテーブル
    		tableModel = createTableModel();
    		table = new JTable(tableModel);
    		table.getTableHeader().setReorderingAllowed(false);
    		table.getColumn("Mouse Name").setCellEditor(new DefaultCellEditor(new ExtendedJTextField("", 10)));
    		table.getColumn("Target").setCellEditor(new DefaultCellEditor(targetList));
    		JScrollPane sp = new JScrollPane(table);
    		
    		// 作成
    	    filePanel.add(projectLabel, gbc);
    	    gbc.gridx = 1;
    	    gbc.anchor = GridBagConstraints.LINE_START;
    	    filePanel.add(projectName, gbc);
    	    gbc.gridx = 0;
    	    gbc.gridy = 1;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    filePanel.add(referenceLabel, gbc);
    	    gbc.gridx = 1;
    	    gbc.anchor = GridBagConstraints.LINE_START;
    	    filePanel.add(referenceName, gbc);
    	    
    	    buttonPanel.add(up);
    	    buttonPanel.add(down);
    	    buttonPanel.add(add);
    	    buttonPanel.add(delete);
    	    
    	    add(filePanel, BorderLayout.NORTH);
    	    add(buttonPanel, BorderLayout.EAST);
    	    add(sp, BorderLayout.CENTER);
    	}
    	
    	public void postprocess(){
    		next.requestFocus();
    	}
    	
    	/**
    	 * reference を読み込んで TableModel を作成
    	 */
    	private DefaultTableModel createTableModel(){
    		String[] column = {"Mouse Name", "Target"};
    		DefaultTableModel tableModel = new DefaultTableModel(column, 0);
    		
    		File ref = new File(programPath + sep + projectID + sep + "References" + sep + referenceFile);
    		if(ref.exists()){
    			try{
    				BufferedReader reader = new BufferedReader(new FileReader(ref));
    				String line = "";
    				while((line = reader.readLine()) != null){
    					if(!line.startsWith("#")){
    						line = line.trim();
    						String[] row = new String[2];
    						if (line.contains(" ")) {
	    						row[0] = line.substring(0,line.indexOf(" "));
	    						row[1] = line.substring(line.indexOf(" ") + 1);
    						} else if (line.contains("\t")) {
    							row[0] = line.substring(0, line.indexOf("\t"));
    							row[1] = line.substring(line.indexOf("\t") + 1);
    						} else {
    							
    						}
    						tableModel.addRow(row);
    					}
    				}
    				reader.close();
    			} catch(Exception e) {
    				e.printStackTrace();
    			}
    		}
    		
    		return tableModel;
    	}
    	
    	
    	
    	/**
    	 * 選択された行を上へ
    	 */
    	private class UpActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() > 0){
					tableModel.moveRow(table.getSelectedRow(), table.getSelectedRow(), table.getSelectedRow() - 1);
					table.setRowSelectionInterval(table.getSelectedRow() - 1, table.getSelectedRow() - 1);
				}
			}
    	}
    	
    	/**
    	 * 選択された行を下へ
    	 */
    	private class DownActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() >= 0 && table.getSelectedRow() < table.getRowCount() - 1){
					tableModel.moveRow(table.getSelectedRow(), table.getSelectedRow(), table.getSelectedRow() + 1);
					table.setRowSelectionInterval(table.getSelectedRow() + 1, table.getSelectedRow() + 1);
				}
			}
    	}
    	
    	/**
    	 * 行を追加
    	 */
    	private class AddActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				String[] row = new String[2];
				row[0] = "MouseName";
				//row[1] = "TargetNumber";
				row[1] = (String)targetList.getItemAt(0);
				tableModel.addRow(row);
				table.setRowSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
				// Mouse Name の入力状態に
				table.editCellAt(table.getRowCount() - 1, 0);
				((JTextField)((DefaultCellEditor)table.getCellEditor(table.getRowCount() - 1, 0)).getComponent()).requestFocus();
			}
    	}

    	/**
    	 * 選択された行を全て削除
    	 */
    	private class DeleteActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				int select = -1;
				while(table.getSelectedRow() >= 0){
					select = table.getSelectedRow();
					tableModel.removeRow(select);
				}
				
				// 削除後の行の選択
				if(select >= 0 && select < table.getRowCount())
					table.setRowSelectionInterval(select, select);
				else if(--select >= 0 && select < table.getRowCount())
					table.setRowSelectionInterval(select, select);
			}
    	}
    	
    	/**
    	 *　入力されたデータを書き出す
    	 */
    	public void save(){
    		Vector data = tableModel.getDataVector();
			StringBuffer buf = new StringBuffer();
			for(int i = 0; i < data.size(); i++){
				String[] row = new String[2];
				row[0] = (String)((Vector)data.elementAt(i)).elementAt(0);
				row[1] = (String)((Vector)data.elementAt(i)).elementAt(1);
				buf.append(row[0]).append(" ").append(row[1]).append(LINE_SEPARATOR);
			}
    		try{
    			BufferedWriter writer = new BufferedWriter(new FileWriter(programPath + sep + projectID + "/References/" + referenceFile));
    			writer.write(buf.toString());
    			writer.close();
    			BehaviorDialog.showMessageDialog(this, "Created the reference file: " + programPath + sep + projectID + "/References/" + referenceFile);
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public boolean canGoNext(){
    		Vector data = tableModel.getDataVector();
    		if(data.size() == 0){
    			BehaviorDialog.showErrorDialog(this, "Mouse data table is empty.");
    			return false;
    		}
    		
    		// 同じ名前のマウスがいないかどうかチェック
    		// ついでにマウスの名前もチェック
    		for(int i = 0; i < data.size(); i++){
    			if(!FilenameValidator.validate(((String)((Vector)data.elementAt(i)).elementAt(0)))){
    				BehaviorDialog.showErrorDialog(this, "Some mouses have a invalid name.\nMouse name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    				return false;
    			}
    			for(int j = i + 1; j < data.size(); j++)
    				if(((String)((Vector)data.elementAt(i)).elementAt(0)).equals(
    					(String)((Vector)data.elementAt(j)).elementAt(0)))
    				{
    					BehaviorDialog.showErrorDialog(this, "Some mouses have the same name : " + (String)((Vector)data.elementAt(i)).elementAt(0));
    					return false;
    				}
    		}
    		
    		//Targetが1~12で指定されているかチェック(comboBoxで１～１２を指定するようにしているので必要ない。TextFieldで入力するように変更したとき用。）
    		for (int i = 0; i < data.size(); i++) {
    			String target = (String)((Vector)data.elementAt(i)).elementAt(1);
    			if (!(target.equals("1") || target.equals("2") || target.equals("3") || target.equals("4") || target.equals("5") || target.equals("6")
    					|| target.equals("7") || target.equals("8") || target.equals("9") || target.equals("10") || target.equals("11") || target.equals("12")) ) {
    				BehaviorDialog.showErrorDialog("TargetNumber must be 1～12");
    				return false;
    			}
    		}
    		return true;
    	}
    }
}
