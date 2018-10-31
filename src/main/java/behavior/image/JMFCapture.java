package behavior.image;

import behavior.image.StateHelper;

import java.awt.*;
import java.util.Vector;

import javax.media.*;
import javax.media.util.*;
import javax.media.control.TrackControl;
import javax.media.Format;
import javax.media.format.*;

import ij.*;
import ij.process.*;

/**
 * Java Media Framework を用いてカメラの画像の取得を行う．
 *
 */
public class JMFCapture extends AbstractCapture{

	private Processor p;
	private StateHelper sh = null;
	private TrackControl tc[];
	private ImagePlus imp;
	private ImageProcessor ip = null;
	private boolean prepare = true;
	private int t = 0;
	
	JMFCapture() throws Exception{
		if(setup())
			throw new Exception();
	}

	ImagePlus capture() {
		while(prepare){
			try{
				Thread.sleep(50);
				t++;
				if(t > 20){
					t = 0;
					return null;
				}
			}catch(InterruptedException e){
				IJ.showMessage("interrupt in captureImage");
			}
		}
		ip = imp.getProcessor();
		ip = ip.convertToByte(false);
		Object pixel = ip.getPixels();
		if(pixel instanceof int[]){
			int[] temp = (int[])pixel;
			byte[] correct = new byte[temp.length];
			for(int i = 0; i < temp.length; i++)
				correct[i] = (byte)temp[i];
			ip.setPixels(correct);
		}
		imp.setProcessor(null, ip);
		prepare = true;
		return imp;
	}

	@SuppressWarnings("unchecked")
	private boolean setup(){
		try{
			Vector deviceList = CaptureDeviceManager.getDeviceList(new VideoFormat( VideoFormat.YUV ) );
			Vector<CaptureDeviceInfo> vlist = deviceList;
			MediaLocator ml;
			if(vlist.size() == 0){
//				IJ.showMessage("Error: could not detect Camera by JMF");
				ml = new MediaLocator("vfw://0");
			}
			else{
				CaptureDeviceInfo vinfo = vlist.elementAt(0);
				ml = vinfo.getLocator();
			}
			p = Manager.createProcessor(ml);
			sh = new StateHelper(p);
			sh.configure(10000);
			p.setContentDescriptor(null);
			tc = p.getTrackControls();
			TrackControl videoTrack = null;
			for (int i = 0; i < tc.length; i++) {
				if (tc[i].getFormat() instanceof VideoFormat) {
					videoTrack = tc[i];
					break;
				}
			}
			System.err.println("Video format: " + videoTrack.getFormat());
			Codec codec[] = { new DrawLineCodec()};
			videoTrack.setCodecChain(codec);
			sh.prefetch(10000);    // prefetched状態にする
			p.start();
			return false;
		} catch (Exception e) {
			return true;
		}
	}

	void close(){
		p.stop();
		p.close();
	}

	private class DrawLineCodec implements Codec {
		private Format input = null;
		private Format output = null;

		// 全てのvideo formatsを扱えるよう広く宣言
		private Format supportedIns[] = new Format [] {
				new VideoFormat(null)
		};

		private Format supportedOuts[] = new Format [] {
				new VideoFormat(null)
		};

		public Format [] getSupportedInputFormats() {
			return supportedIns;
		}

		public Format [] getSupportedOutputFormats(Format in) {
			if (in == null){
				return supportedOuts;
			} else {
				// input format が与えられたら output formatもそれに合わせる
				Format outs[] = new Format[1];
				outs[0] = in;
				return outs;
			}
		}

		public Format setInputFormat(Format format) {
			input = format;
			return input;
		}

		public Format setOutputFormat(Format format) {
			output = format;
			return output;
		}

		public void open() {}

		public void close() {}

		public void reset() {}

		public String getName() {
			return "DrawLine Codec";
		}

		public Object[] getControls() {
			return new Object[0];
		}

		public Object getControl(String type) {
			return null;
		}

		public int process(Buffer in, Buffer out) {
			Image image = null;
			BufferToImage bti;
			int i;
			for (i = 0; i < tc.length; i++) {
				if (tc[i].getFormat() instanceof VideoFormat) {
					break;
				}
			}
			bti = new BufferToImage((VideoFormat)tc[i].getFormat());
			image = bti.createImage(in);
			imp = new ImagePlus("START",image);
			prepare = false;
			return BUFFER_PROCESSED_OK;
		}
	}
}


