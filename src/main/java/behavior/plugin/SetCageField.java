package behavior.plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.*;
import ij.plugin.PlugIn;
import ij.plugin.frame.PlugInFrame;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import behavior.gui.AskingDialog;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.setup.Program;

/**
 * ROIを設定するプラグイン。
 */

public abstract class SetCageField extends PlugInFrame implements PlugIn,ActionListener,Runnable{
	protected static final int HEADER_SIZE = 64;
	protected static final int VERSION = 217;
	protected static Frame instance;
	protected final int rect=1, oval=2;
	protected byte[] data;
	protected Panel panel1, panel2;
	protected Button save, exit;
	protected boolean saved, Exited;
	protected boolean done;
	protected int scale = 2;
	protected String sep = File.separator;
	protected Program program;
	protected AskingDialog askDialog;
	protected FileManager fm;
	protected GenericDialog gd;

	protected String[] Chamber;
	protected static final String defChamChoice = "D";
	protected static String ChamChoice = defChamChoice;

	public SetCageField(Program program){
		super("Save ROI");
		this.program = program;

		setup();

		setLayout(new BorderLayout());
		panel1 = new Panel();
		panel1.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		MultiLineLabel dialogMsg = new MultiLineLabel("Set ROI and press save ROI.\nPress Exit, and exit Set Cage Field");
		dialogMsg.setFont(new Font("Dialog", Font.BOLD, 12));
		panel1.add(dialogMsg);
		add("North",panel1);

		panel2 = new Panel();
		panel2.setLayout(new FlowLayout(FlowLayout.RIGHT,15,8));
		save = new Button(" save ROI ");
		save.addActionListener(this);
		panel2.add(save);
		exit = new Button(" Exit ");
		exit.addActionListener(this);
		panel2.add(exit);
		add("South", panel2);
		pack();
		GUI.center(this);
		setVisible(true);

		askDialog = new AskingDialog(program);
		if(askDialog.showProjectDialog()) return;
		fm = FileManager.getInstance();
		fm.setProjectID(program, askDialog.getProjectID());

		Thread thread = new Thread(this,"Roi_Saver");
		thread.start();
		ImageCapture ic = ImageCapture.getInstance();
		ImagePlus roiImp = ic.capture();
		roiImp.show();
		ic.close();
	}

	protected abstract void setup();

	public synchronized void actionPerformed(ActionEvent e){
		Button b = (Button)e.getSource();
		if(b==null){
			return;
		}
		if(b==save){
			saved = true;
		}
		else if(b==exit){
			Exited = true;
		}
		notify();
	}

	public void run(){
		while(!done){
			synchronized(this){
				try{
					wait();
				}catch(InterruptedException e){}
			}
			doSave();
		}
	}

	protected void doSave(){
		ImagePlus imp = WindowManager.getCurrentImage();
		if(!Exited){
			if(saved){
				try{
					saveRoi(imp);
				}catch(Exception e){
					String msg = e.getMessage();
					if(msg==null || msg.equals("")){
						msg = "" + e;
					}
					IJ.showMessage("Set Cage Field", msg);
				}
			}
		}else{
			if(imp!=null){
				imp.hide();
			}
			close();
		}
	}

	//@SuppressWarnings("deprecation")
	protected void saveRoi(ImagePlus imp) throws IOException{
		Roi roi = imp.getRoi();
		if(roi==null){
			throw new IllegalArgumentException("ROI required");
		}
		int roiType = roi.getType();
		int type;
		if(roiType==Roi.RECTANGLE){
			type = rect;
		}else{
			throw new IllegalArgumentException("Rectangle ROI required");
		}

		openDialog();

		if(gd.wasCanceled())
			return;
		String name = getRoiName();
		FileOutputStream f = new FileOutputStream(fm.getPath(FileManager.PreferenceDir) + sep + name);
		int n=0;
		data = new byte[HEADER_SIZE+n*4];
		Rectangle r = roi.getBounds();
		data[0]=73;data[1]=111;data[2]=117;data[3]=116;
		putShort(4,VERSION);
		data[6]=(byte)type;
		putShort(8,r.y);
		putShort(10,r.x);
		putShort(12,r.y+r.height);
		putShort(14,r.x+r.width);
		putShort(16,n);
		f.write(data);
		f.close();
		if(name.endsWith(".roi")){
			name = name.substring(0,name.length()-4);
		}
		roi.setName(name);
	}


	protected void putShort(int base,int v){
		data[base]=(byte)(v>>>8);
		data[base+1]=(byte)v;
	}

	public void windowClosing(WindowEvent e){
		if(IJ.showMessageWithCancel("Set Cage Field","are you sure you want to quit Set Cage Field?")){
			ImagePlus imp = WindowManager.getCurrentImage();
			if(imp!=null){
				imp.hide();
			}
			close();
		}else{
			return;
		}
	}

	public void close(){
		super.close();
		instance = null;
		done = true;
		synchronized(this){
			notify();
		}
	}

	protected void openDialog(){
		gd = new GenericDialog("Cage Field");
		gd.addChoice("Chamber: ", Chamber, defChamChoice);
		gd.showDialog();
	}

	protected String getField(){
		ChamChoice = gd.getNextChoice();
		String Field = ChamChoice;
		return Field;
	}

	protected String getRoiName(){
		return "Cage Field" + getField() + ".roi";
	}

	protected boolean wasCanceled(){
		if(gd.wasCanceled()){
			return true;
		}else{
			return false;
		}
	}
}