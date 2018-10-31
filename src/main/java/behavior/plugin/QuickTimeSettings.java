package behavior.plugin;

import quicktime.QTSession;
import quicktime.std.sg.SGVideoChannel;
import quicktime.std.sg.SequenceGrabber;

import ij.IJ;

/**
 * QuickTimeCapture ‚Ì Settings Dialog ‚ðŒÄ‚Ño‚·B
 */
public class QuickTimeSettings extends BehaviorEntryPoint {
	public void run(String arg0) {
		try {
			QTSession.open();
			SequenceGrabber grabber = new SequenceGrabber();
			SGVideoChannel channel = new SGVideoChannel(grabber);
			channel.settingsDialog();
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
		/*else {
			CharArrayWriter caw = new CharArrayWriter();
			PrintWriter pw = new PrintWriter(caw);
			e.printStackTrace(pw);
			String s = caw.toString();
			if (IJ.isMacintosh())
				s = Tools.fixNewLines(s);
			new TextWindow("Exception", s, 500, 300);
		}*/
	}
}
