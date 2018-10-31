package behavior.plugin.executer;

import ij.IJ;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.List;
//import java.util.logging.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileCreate;
import behavior.setup.dialog.ReferenceDialogPanel;
import behavior.util.FilenameValidator;

/**
 * Reference の設定を行う。
 * JTable を用いた使いやすいインターフェースを提供する。
 * 
 * TMの仕組みを流用しています。
 */
public class RMReferenceSettingExecuter extends ReferenceSettingFrame{
	private String projectID;
	private String sep = System.getProperty("file.separator");
	private String programPath = (IJ.isWindows() ? "" : System.getProperty("user.home")) + System.getProperty("file.separator") + "Image_RM";
	private String referencePath;
	private String referenceFileName;
	//private Logger log = Logger.getLogger("behavior.plugin.executer.RMReferenceSettingExecuter");

	public RMReferenceSettingExecuter(){
		super();
		setTitle("RMaze Reference Setup");
		/*try{
			FileHandler fh = new FileHandler(programPath +sep+ "ExecuterLog.txt",102400,1);
			fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
			e.printStackTrace();
		}*/
	}

	@Override
	protected List<ReferenceDialogPanel> addDialogs(List<ReferenceDialogPanel> dialogs){
		dialogs.add(new ProjectDialog());
		dialogs.add(new ReferenceDialog());
		dialogs.add(new SetupDialog());

		return dialogs;
	}

	@Override
	protected void save(List<ReferenceDialogPanel> dialogs){
		dialogs.get(dialogs.size()-1).save();
	}

    /**
     * Step 1 : 対象となるプロジェクトの設定
     */
    private class ProjectDialog extends ReferenceDialogPanel implements ActionListener{
    	private final String defaultProject = "Input Project Name";
        private ExtendedJComboBox projectList;

    	public ProjectDialog(){
    		setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Project ID : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);

    		projectList = new ExtendedJComboBox();
    		File root = new File(programPath);
    		File[] list = root.listFiles();
    		if(list != null)
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
    	
    	public boolean canGoNext(){
    		//log.log(Level.INFO, "1.");
            projectID = projectList.getText();
            
            if(!FilenameValidator.validate(projectID)){
    			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
            
            String path = programPath+sep+projectID+sep+"References";
            File file = new File(path);
            if(file.exists()){
                return true;
            }
            //log.log(Level.INFO, "3.");
            if(BehaviorDialog.showQuestionDialog(this ,"No such Folder : "+projectID+sep+"References"+"\nCreate new file?")
            		== true){
            	//log.log(Level.INFO, "2.");
            	file.mkdirs();
            	new File(path).mkdirs();
            	return true;
            }
            //log.log(Level.INFO, "4.");
            return false;
        }

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }

    /**
     * Step 2 : 対象となるReferenceファイルの設定
     */
    private class ReferenceDialog extends ReferenceDialogPanel implements ActionListener{
    	
    	private ExtendedJComboBox referenceList;
    	
    	public void preprocess(){
        	removeAll();
        	
        	setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Reference File : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);
    		
    		referenceList = new ExtendedJComboBox();
    		File path = new File(programPath +sep+ projectID +sep+ "References");
    		File[] list = path.listFiles(new FileFilter(){
    			public boolean accept(File pathname){
    				String name = pathname.getName();
    				if(name.length()>=4 && name.substring(name.length()-4).equals(".txt"))	// .txt なファイルのみ受け付ける
    					return true;
    				else
    					return false;
    			}
    		});
    		for(int i = 0; i < list.length; i++)
    			referenceList.addItem(list[i].getName());

    	    if(list.length == 0)
    	    	referenceList.setSelectedItem("reference");

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
    		referenceFileName = referenceList.getSelectedItem().toString();
    		//log.log(Level.INFO, referenceFileName);
    		if(!FilenameValidator.validate(referenceFileName)){
    			BehaviorDialog.showErrorDialog(this, "Invalid reference file name.\nReference file name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
    		
    		if((referenceFileName.length()>4) && !referenceFileName.substring(referenceFileName.length()-4).equals(".txt")){
    			referenceFileName += ".txt";
    		}else if(referenceFileName.length()<=4){
    			referenceFileName += ".txt";
    		}

    		referencePath = programPath+sep+projectID+sep+"References"+sep+referenceFileName;
    		//log.log(Level.INFO, referencePath);
    		//log.log(Level.INFO, path);
            File file = new File(referencePath);
            if(file.exists()){
                return true;
            }
            if(BehaviorDialog.showQuestionDialog(this ,"Create new file?")
            		== true){
            	new FileCreate(referencePath).createNewFile();
            	return true;
            }
    		return false;
    	}

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }

    /**
     * Step 3 : 実際のSetup
     */
    private class SetupDialog extends ReferenceDialogPanel{
    	private JTable table;
    	private DefaultTableModel tableModel;
    	private JButton up,down,add,delete;
    	//private Pattern pattern = Pattern.compile("(?:1?2?3?4?5?6?7?8?)");
    	
    	public void preprocess(){
    		removeAll();

    		//log.log(Level.INFO, "5.");
    		new FileCreate().createFile(referencePath);

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
    		JLabel referenceName = new JLabel(referenceFileName);
    		// ボタン類
    		JPanel buttonPanel2 = new JPanel(new GridLayout(4, 1, 5, 20));
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
    		table.putClientProperty("terminateEditOnFocusLost", true);
    		table.getColumn("Mouse Name").setCellEditor(new DefaultCellEditor(new ExtendedJTextField("", 10)));
    		table.getColumn("ArmNumbers(exist foods)").setCellEditor(new DefaultCellEditor(new ExtendedJTextField("", 10)));
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
    	    
    	    buttonPanel2.add(up);
    	    buttonPanel2.add(down);
    	    buttonPanel2.add(add);
    	    buttonPanel2.add(delete);
    	    
    	    add(filePanel, BorderLayout.NORTH);
    	    add(buttonPanel2, BorderLayout.EAST);
    	    add(sp, BorderLayout.CENTER);
    	}
    	
    	public void postprocess(){
    		next.requestFocus();
    	}
    	
    	/**
    	 * reference を読み込んで TableModel を作成
    	 */
    	private DefaultTableModel createTableModel(){
    		String[] column = {"Mouse Name", "ArmNumbers(exist foods)"};
    		DefaultTableModel tableModel = new DefaultTableModel(column, 0);

    		try{
    			BufferedReader br = new BufferedReader(new FileReader(referencePath));
    			String line = "";
    			while((line = br.readLine()) != null){
    				if(line.startsWith("#"))
    				     continue;

    				line = line.trim();
    				String[] row = new String[2];
    				String[] str = line.split("\t");
    				if(str.length == 2){
    				    row[0] = str[0];
    				    row[1] = str[1];
    				    tableModel.addRow(row);
    				}
    			}
    			br.close();
			}catch(Exception e) {
    			e.printStackTrace();
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
				row[1] = "";
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
			FileCreate writer = new FileCreate(referencePath);
			StringBuilder buf = new StringBuilder();
			for(int i = 0; i < tableModel.getRowCount(); i++){
				String[] row = new String[2];
				row[0] = tableModel.getValueAt(i,0).toString().trim();
				row[1] = tableModel.getValueAt(i,1).toString();
				//log.log(Level.INFO, row[0]+" "+row[1]);
				StringBuilder minBuf = new StringBuilder();
				for(int j=0;j<8;j++){
					if(row[1].indexOf(""+(j+1)) != -1)
						minBuf.append((j+1)+" ");
				}
				buf.append(row[0]).append("\t").append(minBuf.toString());
				if(!(i==tableModel.getRowCount()-1))
					buf.append(System.getProperty("line.separator"));
			}
			writer.write(buf.toString(), false);
    		try{
    			BehaviorDialog.showMessageDialog(this, "Created the reference file: " + referencePath);
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}
    	
    	public boolean canGoNext(){
    		if(tableModel.getRowCount() == 0){
    			BehaviorDialog.showErrorDialog(this, "Mouse data table is empty.");
    			return false;
    		}

    		// 同じ名前のマウスがいないかどうかチェック
    		// ついでにマウスの名前もチェック
    		for(int i = 0; i < tableModel.getRowCount(); i++){
    			if(!FilenameValidator.validate(tableModel.getValueAt(i,0).toString())){
    				BehaviorDialog.showErrorDialog(this, "Some mouses have a invalid name.\nMouse name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    				return false;
    			}

    			if(!FilenameValidator.validate(tableModel.getValueAt(i,1).toString())){
    				BehaviorDialog.showErrorDialog(this, "Setting Arm of "+ tableModel.getValueAt(i,0).toString()+" is invalid.");
				    return false;
    		    }

    			for(int j = i + 1; j < tableModel.getRowCount(); j++){
    				if(tableModel.getValueAt(j,0).toString().equals(tableModel.getValueAt(i,0).toString())){
    					BehaviorDialog.showErrorDialog(this, "Some mouses have the same name : " + tableModel.getValueAt(i,0).toString());
    					return false;
    				}
    			}
    		}

    		return true;
    	}
    }
}