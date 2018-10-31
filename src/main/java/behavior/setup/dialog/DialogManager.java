package behavior.setup.dialog;

import ij.IJ;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.util.*;

import javax.swing.*;

import behavior.Version;
import behavior.controller.OutputController;
import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.io.FileManager;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.Variable;

/**
 * 起動時に表示される設定ダイアログの基本クラス。
 * ダイアログの Center の JPanel を、Next を押すたびに次々に入れ替える。
 */
public class DialogManager extends JDialog{
	protected Vector<JPanel> dialogs;                    
	protected Vector<JPanel> captureDialogs;               //取り込み画像に関する設定をするDialog
	private int counter;
	private AbstractDialogPanel currentDialog;         //現在表示中のDialogを表す
	private JLabel step;
	private JButton next,back,cancel;
	private boolean isComplete;
	private AbstractDialogPanel projectDialog;
	private AbstractDialogPanel sessionDialog;
	private AbstractDialogPanel parameterDialog;
	private AbstractDialogPanel subjectDialog;
	private CameraSettingsDialogPanel cameraDialog;
	private AbstractDialogPanel thresholdDialog;
	private AbstractDialogPanel setCageDialog;
	private AbstractDialogPanel offlineSetCageDialog = null;
	private ImageProcessor backIp;
	private FileManager fm;
	private Program program;
	private String session;
	private String[] subjects;
	private int startCount;
	private int type;
	private boolean firstTrial;
	private boolean[] isExistMice;

	private boolean endFlag;
	
	// ダイアログのサイズ指定
	private int dialogX = 800;
	private int dialogY = 150;
	private int dialogWidth = 350;
	private int dialogHeight = 350;
	private boolean isModifiedParameter=false;

	public DialogManager(Program program, int type, int allCage){
		super(IJ.getInstance(),program + " Settings (" + Version.getVersion() + ")" ,false);
		//super()の中身はJDialog(Frame owner, String title, boolean modal);
		this.program = program;
		this.type = type;
		endFlag = false;
		setResizable(false);
		addWindowListener(new CloseListener());

		fm = FileManager.getInstance();
		fm.setProgram(program);
		dialogs = new Vector<JPanel>();
		captureDialogs = new Vector<JPanel>();
		counter = 0;
		startCount = 0;

		step = new JLabel("Step " + (counter + 1) + ": ");
		step.setHorizontalAlignment(JLabel.LEFT);
		step.setFont(new Font("Dialog", Font.BOLD, 12));

		setProjectDialog(new ProjectDialogPanel(this, type));
		setSessionDialog(new SessionDialogPanel(this, type));
		setParameterDialog(new ParameterDialogPanel(this, type));
		setSubjectDialog(new SubjectDialogPanel(this, allCage));
		if(type == Setup.ONLINE)
			setCameraSettingDialog(new CameraSettingsDialogPanel(this));
		setThresholdDialog(new ThresholdDialogPanel(this, type));
		setSetCageDialog(new SetCageDialogPanel(this, allCage));

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		next = new ExtendedJButton("Next >");
		back = new ExtendedJButton("< Back");
		cancel = new JButton("Cancel");
		next.addActionListener(new NextActionListener());
		back.addActionListener(new BackActionListener());
		cancel.addActionListener(new CancelActionListener());

		// Back, Next, Cancel を配置
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
		add(step, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);
		
		// 画面解像度に合わせてダイアログの位置を変更
		Dimension dm = getToolkit().getScreenSize();
		if(dm.width < dialogX + dialogWidth)
			dialogX = dm.width - dialogWidth;
		if(dm.height < dialogY + dialogHeight)
			dialogY = dm.height - dialogHeight;
		setBounds(dialogX, dialogY, dialogWidth, dialogHeight);
	}

	public int getTypes(){
		return type;
	}

	public DialogManager getApplication(){
		return this;
	}

	public void showSubDialog(){
		back.setEnabled(false);
		load();
		if(type == Setup.ONLINE){
			counter = 3;
			startCount = 3;
		} else{
			counter = 1;
			startCount = 1;
		}
		firstTrial = false;
		showDialog();
	}
	public void showAllDialog(){
		back.setEnabled(false);
		firstTrial = true;

		// ダイアログを順にセットする。
		addDialog(projectDialog);
		if(type == Setup.ONLINE || program == Program.BM || program == Program.CSI
				|| program == Program.OLDCSI || program == Program.RM)
		    addDialog(sessionDialog);
		addDialog(parameterDialog);
		if(type == Setup.ONLINE){
			addDialog(subjectDialog);
			addCaptureDialog(cameraDialog);
		}
		addCaptureDialog(thresholdDialog); // ThresholdDialog は OFFLINE でも必要。
		if(type == Setup.ONLINE || program == Program.BM){
			addCaptureDialog(setCageDialog);
	    }else{
		    addCaptureDialog(offlineSetCageDialog);
	    }
		if(program == Program.EP && type == Setup.OFFLINE){
			addCaptureDialog(new EPOfflineSetCageDialogPanel(this));
		}else if(program == Program.OLDCSI){
			addCaptureDialog(new CSIOldOfflineSetCageDialogPanel(this));
		}else if(program == Program.CSI && type == Setup.OFFLINE){
			addCaptureDialog(new CSIOfflineSetCageDialogPanel(this));
		}

		addSubDialogs();

		showDialog();
	}

	private void showDialog(){
		isComplete = false;
		if(counter < startCount){
			counter = startCount;
		}
		else if(counter >= dialogs.size()+captureDialogs.size()){
			complete();
			return;
		}
		if(currentDialog != null){
			remove(currentDialog);
		}
		if(counter < dialogs.size()){
			currentDialog = (AbstractDialogPanel)dialogs.get(counter);
		} else {
			currentDialog = (AbstractDialogPanel)captureDialogs.get(counter-dialogs.size());
		}
		step.setText("Step " + (counter + 1) + ": " + currentDialog.getDialogName());
		currentDialog.preprocess();
		add(currentDialog,BorderLayout.CENTER);

		// サイズ調整
		if(getSize().width < getPreferredSize().width)
			setSize(getPreferredSize().width, getSize().height);
		if(getSize().height < getPreferredSize().height)
			setSize(getSize().width, getPreferredSize().height);

        getApplication().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		setVisible(true);
		currentDialog.postprocess();
		repaint();

		if(endFlag)
			setVisible(false);
	}

	/**
	 *  completeボタン（最後のDialogのNextボタン）が押されたら呼び出される
	 *  各パラメータを保存して、Dialogを終了する
	 */
	private void complete(){
		isComplete = true;
		savePrefs();
		Parameter.endSetting();
		dispose();
	}

	/**
	 * 各ダイアログパネルを初期化する。
	 *　preference.txt から前回の設定を読み込み、各ダイアログのload()メソッドに渡す。
	 */
	protected void load(){
		String path = fm.getPath(FileManager.parameterPath);
		Properties properties = new Properties();
		try {
			File file = new File(path);
			if(file.exists())
				properties.load(new FileInputStream(new File(path)));
			else
				properties = null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Iterator<JPanel> it = dialogs.iterator();
		while(it.hasNext()){
			AbstractDialogPanel dialog = (AbstractDialogPanel) it.next();
			dialog.load(properties);
		}
		it = captureDialogs.iterator();
		while(it.hasNext()){
			AbstractDialogPanel dialog = (AbstractDialogPanel) it.next();
			dialog.load(properties);
		}
	}

	/**
	 設定したパラメータ値を、指定したフォルダ下に指定した名前で保存する。
	 */
	public void savePrefs(){
		try{
			// Preference フォルダが存在しなければ作成．
			File dir = new File(FileManager.getInstance().getPath(FileManager.PreferenceDir));
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			String project = program.toString();
			String path = FileManager.getInstance().getPath(FileManager.parameterPath);
			Parameter parameter = Parameter.getInstance();
			//プロパティを保存したり、読み込んだりできるクラスをインスタンス化。空のプロパティリストを作成。
			Properties prefs = new Properties();

			//指定したフォルダに指定した名前で空のファイルを作成。引数はファイル名のフルパス。
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(path);
			PrintWriter pw = new PrintWriter(fos);

			//ファイルにヘッダを書き込み。
			String header = "# "+ project + " Preference";
			pw.println(header);

			//ファイルに更新した日時を書き込み。
			pw.println("#"+ new Date());
			pw.println("");
			pw.close();
			fos.close();

			//入力ストリームからキーと要素が対になったプロパティリストを読み込み。
			prefs.load(new FileInputStream(path));

			Variable[] var = parameter.getVar();
			for(int i = 1; var[i] != null; i++)
				var[i].setProperties(prefs);

			if (cameraDialog != null)
				cameraDialog.setProperties(prefs);

			//書き込み。header 引数が null でない場合は、ASCII 文字の #、header の文字列、および行区切り文字が最初に出力ストリームに書き込まれる。このため、header は識別コメントとして使える。 
			prefs.store(new FileOutputStream(path) ,header);
		}catch(Exception e){
			BehaviorDialog.showErrorDialog(String.valueOf(e));
		}
	}


	protected void addDialog(AbstractDialogPanel dialog){
		dialogs.add(dialog);
	}

	protected void addCaptureDialog(AbstractDialogPanel dialog){
		if(dialog != null)
		    captureDialogs.add(dialog);
	}

	// 追加のダイアログがある場合は、以下をオーバーライドする。
	protected void addSubDialogs(){
	}

	// 差し替え用。
	protected void setProjectDialog(AbstractDialogPanel panel){
		projectDialog = panel;
	}

	protected void setSessionDialog(AbstractDialogPanel panel){
		sessionDialog = panel;
	}

	protected void setParameterDialog(AbstractDialogPanel panel){
		parameterDialog = panel;
	}

	protected void setSubjectDialog(AbstractDialogPanel panel){
		subjectDialog = panel;
	}

	protected void setCameraSettingDialog(CameraSettingsDialogPanel panel){
		cameraDialog = panel;
	}

	protected void setThresholdDialog(AbstractDialogPanel panel){
		thresholdDialog = panel;
	}

	protected void setSetCageDialog(AbstractDialogPanel panel){
		setCageDialog = panel;
	}

	protected void setOfflineSetCageDialog(AbstractDialogPanel panel){
		offlineSetCageDialog = panel;
	}

	// Next, Back, Cancel が押されたときの動作
	public class NextActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(!currentDialog.canGoNext()){
				return ;
			}
	        getApplication().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			counter++;
			setButtonLabel();
			if(currentDialog == cameraDialog)
				backIp = cameraDialog.getImage();	// バックグラウンドを取得
				currentDialog.endprocess();
				back.setEnabled(true);
				if(counter == 1){
					load();	// ProjectID が入力されたあとに読みこむ
				}
				showDialog();
		}
	}
	public class BackActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			getApplication().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			counter--;
			setButtonLabel();
			currentDialog.backprocess();
			currentDialog.endprocess();
			if(counter == startCount)
				back.setEnabled(false);
			showDialog();
		}
	}
	
	public class CancelActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e){
			if (program == Program.LD && (currentDialog == subjectDialog || currentDialog == cameraDialog || currentDialog == thresholdDialog || currentDialog == setCageDialog) ) {
				BehaviorDialog.showErrorDialog("Please click OK to close the doors.");
				OutputController output = OutputController.getInstance();
				output.setup(OutputController.LD_TYPE);
				output.clear(OutputController.ALL_CHANNEL);
			}
			currentDialog.endprocess();
			dispose();
		}
	}
	
	// ×ボタンで閉じられたときもちゃんと終了する．
	public class CloseListener implements WindowListener{
		public void windowClosing(WindowEvent arg0) {	
			if (program == Program.LD && (currentDialog == subjectDialog || currentDialog == cameraDialog || currentDialog == thresholdDialog || currentDialog == setCageDialog) ) {
				BehaviorDialog.showErrorDialog("Please click OK to close the doors.");
				OutputController output = OutputController.getInstance();
				output.setup(OutputController.LD_TYPE);
				output.clear(OutputController.ALL_CHANNEL);
			}
			currentDialog.endprocess();
		}
		
		public void windowActivated(WindowEvent arg0) {	
		}
		public void windowClosed(WindowEvent arg0) {
		}
		public void windowDeactivated(WindowEvent arg0){
		}
		public void windowDeiconified(WindowEvent arg0) {
		}
		public void windowIconified(WindowEvent arg0) {
		}
		public void windowOpened(WindowEvent arg0) {
		}
	}

	public JButton getNextButton(){
		return next;
	}

	private void setButtonLabel(){
		if(dialogs.size()+captureDialogs.size() == counter+1){
			next.setText("Complete");
		}
		else{
			next.setText("Next >");
		}
	}


	public boolean isComplete(){
		return isComplete;
	}

	public Program getProgram(){
		return program;
	}

	public void setSessionID(String session){
		this.session = session;
	}

	public void setSubjectID(String[] subjects){
		this.subjects = subjects;
	}

	public void setExistMice(boolean[] cage){
		this.isExistMice = cage;
	}

	public boolean[] getExistMice(){
		return isExistMice;
	}

	public String getSessionID(){
		return session;
	}

	public String[] getSubjectID(){
		return subjects;
	}

	public boolean isFirstTrial(){
		return firstTrial;
	}

	public boolean isModifiedParameter(){
		return isModifiedParameter;
	}

	public void setModifiedParameter(boolean b){
		isModifiedParameter = b;
	}

	/**
	 * エラーなどで途中終了する場合に呼び出す。
	 */
	 public void setEndFlag(){
		endFlag = true;
	 }

	 /**
	  * CameraSettingDialogPanel で取得した background はここから取得できる。
	  */
	 public ImageProcessor getBackIp(){
		 return backIp;
	 }
}
