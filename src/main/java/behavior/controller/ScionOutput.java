package behavior.controller;

import ij.*;

import scion.fg.*;
import scion.fg.capture.*;

class ScionOutput extends AbstractOutput{
	private fg_manager fgm;
	private jsfg_fg fg;

	ScionOutput()throws Exception{
		fgm = new fg_manager(IJ.getInstance());
		if(fgm.openFG(false) != fg_manager.success)
			throw new Exception();
	}

	void controlOutput(int bits){
		fg.set_port_bits(1,bits);
	}

	boolean open(){
		fgm = new fg_manager(IJ.getInstance());
		if(fgm.openFG(false) != fg_manager.success)
			return true;
		fg = fgm.getFG();
		return false;
	}

	void close(){
		fgm.closeFG();
	}

	void clear(int bits){
		fg.clr_port_bits(1,bits);
	}
}