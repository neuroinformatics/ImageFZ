package behavior.plugin;

import java.io.*;
import java.util.Properties;

import quicktime.QTSession;
import quicktime.std.sg.SGDeviceList;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

import ij.IJ;
import ij.gui.GenericDialog;
import ij.text.TextWindow;
import ij.util.Tools;

/**
 * QuickTimeCapture ���g�p����f�o�C�X�̑I��������B
 * �����f�o�C�X�����݂���Ƃ��ɕK�v�ŁA1�����Ȃ��ꍇ�͍��̂Ƃ�����ɕK�v�Ȃ��B
 * �ݒ�̕ۑ���� ImageJ �����݂���f�B���N�g����"QT_Prefs.properties"
 */
public class QuickTimeSelectDevice extends BehaviorEntryPoint {
	public static final String path = "QT_Prefs.properties";

	public static final String deviceKey = "device";

	public void run(String arg0) {
		try {
			QTSession.open();
			SequenceGrabber grabber = new SequenceGrabber();
			SGVideoChannel channel = new SGVideoChannel(grabber);

			SGDeviceList list = channel.getDeviceList(0);
			String[] nameList = new String[list.getCount()];
			for(int i = 0; i < list.getCount(); i++)
				nameList[i] = list.getDeviceName(i).getName(); // �f�o�C�X�̖��O���擾

			Properties prefs = new Properties();
			try{
				FileInputStream fis = new FileInputStream(path);
				prefs.load(fis);
				fis.close();
			} catch(Exception e) {
			}

			String def = prefs.getProperty(deviceKey, nameList[0]);

			// �ݒ肳��Ă���f�o�C�X�����݂��Ȃ��ꍇ�ɑΉ��B
			boolean flag = false;
			for(int i = 0; i < list.getCount(); i++)
				flag |= nameList[i].equals(def);
			if(!flag)
				def = nameList[0];

			GenericDialog gd = new GenericDialog("QT Settings");
			gd.addChoice("Using Device: ", nameList, def);
			gd.showDialog();

			if(!gd.wasCanceled()){
				String device = gd.getNextChoice();
				prefs.setProperty(deviceKey, device);
				FileOutputStream fos = new FileOutputStream(path);
				prefs.store(fos, null); // �ݒ��ۑ��B
				fos.close();
			}
		} catch (Exception e) {
			printStackTrace(e);
		} finally {
			QTSession.close();
		}

	}

	private void printStackTrace(Exception e) {
		String msg = e.getMessage();
		if (msg!=null && msg.indexOf("-9405")>=0)
			IJ.error("QT Settings", "QuickTime compatible camera not found");
		else {
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			String s = caw.toString();
			if (IJ.isMacintosh())
				s = Tools.fixNewLines(s);
			new TextWindow("Exception", s, 500, 300);
		}

	}


}
