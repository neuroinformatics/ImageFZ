package behavior.image;

import ij.*;
import ij.process.*;

import scion.fg.*;
import scion.fg.capture.*;
import scion.util.*;
import scion.ij.*;

/**
 * Scion Frame Grabber を用いてカメラの画像の取得を行う．
 * 今ではほとんどが InterView & LabJack になっているようですが，今でもこれを用いているものが実験室にあるので注意．
 */
public class ScionCapture extends AbstractCapture{

	private fg_manager fgm;
	private fg_config config;
	private fg_signal signal;
	private jsfg_fg fg;
	private jsfg_lut lut;

	ScionCapture() {
		setFG();
	}

	ImagePlus capture(){
		setFG();
		jsfg_dimensions image_size;
		int width,height;
		ImageProcessor ip;
		ImagePlus imp;
		simage image;
		try{
			image_size = config.get_image_dimensions();
			width = image_size.get_width();
			height = image_size.get_height();
			ip = new ByteProcessor(width,height);
			imp = new ImagePlus("Image"+ScionIJTools.getImageNo(),ip);
			image = ScionIJTools.createSimage(imp);
		}catch(OutOfMemoryError e){
			mem_alert();
			fgm.closeFG();
			return null;
		}
		fg_capture co = new fg_capture(fg,config,signal);
		co.setup_normal(fg_config.gray_image,false,1);
		co.disable_ext_trigger();
		co.capture(image,1);
		// 反転するようなので修正
		imp.getProcessor().invert();
		return imp;
	}

	/**
	 Frame Grabberをセットする。
	 */
	private void setFG(){
		fgm = new fg_manager(IJ.getInstance());
		fgm.openFG();
		if(fgm.openFG(false) != fg_manager.success){
			return;
		}
		fg = fgm.getFG();
		config = new fg_config(fg,true);
		signal = new fg_signal(fg);
		lut = new jsfg_lut(true);
		lut.invert();
		signal.load();
		config.set_video_format(fg_config.ntsc);
	}

	/**
	 メモリーアウトした場合にエラーメッセージを表示する。
	 */
	private void mem_alert(){
		new InfoDialog(IJ.getInstance(), "Scion Frame Grabber Alert","Insufficient memory to perform image capture.\n \n"+"The image capture will not proceed.\n\n"+"Check your parameter configuration and reset the duration.",true);
	}
	
	void close(){
	}

}
