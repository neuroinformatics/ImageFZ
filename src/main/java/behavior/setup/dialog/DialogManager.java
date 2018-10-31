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
 * �N�����ɕ\�������ݒ�_�C�A���O�̊�{�N���X�B
 * �_�C�A���O�� Center �� JPanel ���ANext ���������тɎ��X�ɓ���ւ���B
 */
public class DialogManager extends JDialog{
	protected Vector<JPanel> dialogs;                    
	protected Vector<JPanel> captureDialogs;               //��荞�݉摜�Ɋւ���ݒ������Dialog
	private int counter;
	private AbstractDialogPanel currentDialog;         //���ݕ\������Dialog��\��
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
	
	// �_�C�A���O�̃T�C�Y�w��
	private int dialogX = 800;
	private int dialogY = 150;
	private int dialogWidth = 350;
	private int dialogHeight = 350;
	private boolean isModifiedParameter=false;

	public DialogManager(Program program, int type, int allCage){
		super(IJ.getInstance(),program + " Settings (" + Version.getVersion() + ")" ,false);
		//super()�̒��g��JDialog(Frame owner, String title, boolean modal);
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

		// Back, Next, Cancel ��z�u
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
		
		// ��ʉ𑜓x�ɍ��킹�ă_�C�A���O�̈ʒu��ύX
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

		// �_�C�A���O�����ɃZ�b�g����B
		addDialog(projectDialog);
		if(type == Setup.ONLINE || program == Program.BM || program == Program.CSI
				|| program == Program.OLDCSI || program == Program.RM)
		    addDialog(sessionDialog);
		addDialog(parameterDialog);
		if(type == Setup.ONLINE){
			addDialog(subjectDialog);
			addCaptureDialog(cameraDialog);
		}
		addCaptureDialog(thresholdDialog); // ThresholdDialog �� OFFLINE �ł��K�v�B
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

		// �T�C�Y����
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
	 *  complete�{�^���i�Ō��Dialog��Next�{�^���j�������ꂽ��Ăяo�����
	 *  �e�p�����[�^��ۑ����āADialog���I������
	 */
	private void complete(){
		isComplete = true;
		savePrefs();
		Parameter.endSetting();
		dispose();
	}

	/**
	 * �e�_�C�A���O�p�l��������������B
	 *�@preference.txt ����O��̐ݒ��ǂݍ��݁A�e�_�C�A���O��load()���\�b�h�ɓn���B
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
	 �ݒ肵���p�����[�^�l���A�w�肵���t�H���_���Ɏw�肵�����O�ŕۑ�����B
	 */
	public void savePrefs(){
		try{
			// Preference �t�H���_�����݂��Ȃ���΍쐬�D
			File dir = new File(FileManager.getInstance().getPath(FileManager.PreferenceDir));
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			String project = program.toString();
			String path = FileManager.getInstance().getPath(FileManager.parameterPath);
			Parameter parameter = Parameter.getInstance();
			//�v���p�e�B��ۑ�������A�ǂݍ��񂾂�ł���N���X���C���X�^���X���B��̃v���p�e�B���X�g���쐬�B
			Properties prefs = new Properties();

			//�w�肵���t�H���_�Ɏw�肵�����O�ŋ�̃t�@�C�����쐬�B�����̓t�@�C�����̃t���p�X�B
			File file = new File(path);
			file.createNewFile();
			FileOutputStream fos = new FileOutputStream(path);
			PrintWriter pw = new PrintWriter(fos);

			//�t�@�C���Ƀw�b�_���������݁B
			String header = "# "+ project + " Preference";
			pw.println(header);

			//�t�@�C���ɍX�V�����������������݁B
			pw.println("#"+ new Date());
			pw.println("");
			pw.close();
			fos.close();

			//���̓X�g���[������L�[�Ɨv�f���΂ɂȂ����v���p�e�B���X�g��ǂݍ��݁B
			prefs.load(new FileInputStream(path));

			Variable[] var = parameter.getVar();
			for(int i = 1; var[i] != null; i++)
				var[i].setProperties(prefs);

			if (cameraDialog != null)
				cameraDialog.setProperties(prefs);

			//�������݁Bheader ������ null �łȂ��ꍇ�́AASCII ������ #�Aheader �̕�����A����эs��؂蕶�����ŏ��ɏo�̓X�g���[���ɏ������܂��B���̂��߁Aheader �͎��ʃR�����g�Ƃ��Ďg����B 
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

	// �ǉ��̃_�C�A���O������ꍇ�́A�ȉ����I�[�o�[���C�h����B
	protected void addSubDialogs(){
	}

	// �����ւ��p�B
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

	// Next, Back, Cancel �������ꂽ�Ƃ��̓���
	public class NextActionListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			if(!currentDialog.canGoNext()){
				return ;
			}
	        getApplication().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			counter++;
			setButtonLabel();
			if(currentDialog == cameraDialog)
				backIp = cameraDialog.getImage();	// �o�b�N�O���E���h���擾
				currentDialog.endprocess();
				back.setEnabled(true);
				if(counter == 1){
					load();	// ProjectID �����͂��ꂽ���Ƃɓǂ݂���
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
	
	// �~�{�^���ŕ���ꂽ�Ƃ��������ƏI������D
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
	 * �G���[�Ȃǂœr���I������ꍇ�ɌĂяo���B
	 */
	 public void setEndFlag(){
		endFlag = true;
	 }

	 /**
	  * CameraSettingDialogPanel �Ŏ擾���� background �͂�������擾�ł���B
	  */
	 public ImageProcessor getBackIp(){
		 return backIp;
	 }
}
