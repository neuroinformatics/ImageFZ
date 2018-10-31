package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
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
import behavior.plugin.analyzer.BMAnalyzer;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;

/**
 * BM�ł�Session�t�@�C���̒��ɁASubjectID�ɉ����ăS�[���ƂȂ錊�̔ԍ����L������K�v������B
 * ���̃N���X�͂��̏����̂���
 */
public class BMSessionDialogPanel extends SessionDialogPanel {
	public String id = "Input_Session"; //�f�t�H���g�\��
	private JComboBox sessionList;
	private int type;
	public static int[] targetHole = new int[200];
	//200�͓K�� ���Session�t�@�C�����ɂ���SubjectID�̐����w�肷��̂ŁA72�ȏ゠��Ηǂ��H
	//Session�t�@�C������A���̐����擾�ł���Ɨǂ��̂�������H

	public String getDialogName(){
		return "Session Name";
	}

	public BMSessionDialogPanel(DialogManager manager, int type){
		super(manager, type);
		this.type = type;
		BMAnalyzer.type = type;
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
		File path = new File(FileManager.getInstance().getPath(FileManager.SessionsDir));
		File[] list = path.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				String name = pathname.getName();
				if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt �ȃt�@�C���̂ݎ󂯕t����
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
		gbc.anchor = GridBagConstraints.LINE_END;
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

	//SessionDialogPanel�ƈႤ�̂͂��������B���̃��\�b�h�̓G���[������邽�߂̂��̂ŁA���g�͓���
	public boolean canGoNext(){
		String input = getSessionID();
		FileManager fm = FileManager.getInstance();
		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid session name.\nSession name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
			return false;
		}

		fm.setSessionID(input);
		manager.setSessionID(input);

		// OFFLINE �̏ꍇ�́A��͑Ώۂ̉摜�����݂��邩�ǂ��������Œ��ׂĂ����B
		// ���Ȃ���� Subject ID ���Z�b�g���Ă��܂��B
		String[] subjectID = null;
		if(type == Setup.OFFLINE){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
				int subIDNum = 0;
				while(reader.readLine() != null)
					subIDNum++;
				reader.close();
				subjectID = new String[subIDNum];

				reader = new BufferedReader(new FileReader(fm.getPath(FileManager.sessionPath)));
				String subID;
				int num = 0;
				while((subID = reader.readLine()) != null){
					targetHole[num] = Integer.valueOf(subID.split(" ")[1]);
					subjectID[num++] = subID.split(" ")[0];
					if (targetHole[num-1] == 0) {
						BehaviorDialog.showErrorDialog(" line:" + num + "\n readLine():" + subID + "\n target:" + targetHole[num-1] + "\n ID:" + subjectID[num-1]);
					}
				}
				reader.close();
			}catch(Exception e){
				BehaviorDialog.showErrorDialog(this, "Can't load file: " + fm.getPath(FileManager.sessionPath));
				return false;
			}

			String path = FileManager.getInstance().getPath(FileManager.ImagesDir) + "/";
			for(int i = 0; i < subjectID.length; i++){
				if(!new File(path + subjectID[i] + ".tif").exists()){
					BehaviorDialog.showErrorDialog(this, "Can't find image file: " + path + subjectID[i] + ".tif");
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
