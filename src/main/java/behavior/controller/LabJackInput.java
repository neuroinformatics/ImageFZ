package behavior.controller;

import ij.IJ;

import com.teravation.labjack.LabJack;
import com.teravation.labjack.LabJackException;
import com.teravation.labjack.LabJackFactory;

class LabJackInput extends AbstractInput{
	private LabJack lj;
	private int inputType;

	LabJackInput(int type) throws Exception{
		inputType = type;

		LabJack[] labjacks;
		try{
			labjacks = new LabJackFactory().getLabJacks();
			if(labjacks == null || labjacks.length == 0){
				IJ.showMessage("Error: could not detect labjack");
				throw new Exception(); //InputController で処理してもらうため
			}
			else{
				lj = labjacks[0];
			}
		}catch(Throwable e){
			IJ.showMessage("Error: set up labjack");
			throw new Exception(); //InputController で処理してもらうため
		}
	}

	@Override
	boolean getInput(int channel){
		switch(inputType){
		    case InputController.PORT_AI: return getAIinput(channel);
		    case InputController.PORT_IO: return getIOinput(channel);
		    case InputController.PORT_D:  return getDinput(channel);
		    default: return false;
		}
	}

	private boolean getAIinput(int channel){
		int value = 0;
		try{
			lj.updateAI(0);
			value = (int)lj.getAI(0);
			if(value > 3){
				value = 0;
				return channel==0;
			}
			lj.updateAI(1);
			value = (int)lj.getAI(1);
			if(value > 3){
				value = 0;
				return channel==1;
			}
			lj.updateAI(2);
			value = (int)lj.getAI(2);
			if(value > 3){
				value = 0;
				return channel==2;
			}
			lj.updateAI(3);
			value = (int)lj.getAI(3);
			if(value > 3){
				value = 0;
				return channel==3;
			}
		}catch(LabJackException e){
			IJ.showMessage("Error: get input(LabJack.AI)");
		}
		return false;
	}

	private boolean getIOinput(int channel){
		if(!(channel<0) && !(3<channel)){
		    try{
			    lj.setIOForInput(channel);
			    lj.updateIO(channel);
			    return lj.getIO(channel);
		    }catch(LabJackException e){
			    IJ.showMessage("Error: get input(LabJack.IO)");
		    }
		}

		return false;		
	}

	private boolean getDinput(int channel){
		if(!(channel<0) && !(15<channel)){
		    try{
			    lj.setDForInput(channel);
			    lj.updateD(channel);
			    return lj.getD(channel);
		    }catch(LabJackException e){
			    IJ.showMessage("Error: get input(LabJack.IO)");
		    }
		}

		return false;		
	}

	@Override
	void clear(int bits){
		try{
			if(bitsCheck(bits, 3)){
				lj.setIO(3, false);
			}
			if(bitsCheck(bits, 2)){
				lj.setIO(2, false);
			}
			if(bitsCheck(bits, 1)){
				lj.setIO(1, false);
			}
			if(bitsCheck(bits, 0)){
				lj.setIO(0, false);
			}
		}catch(LabJackException e){
			IJ.showMessage("Error: clear LabJack");
		}
	}

	private boolean bitsCheck(int bits, int portNo){	//bitをport number　になおす。もっと簡単にならないか？
		if(bits >= 8){
			if(portNo == 3){
				return true;
			}
			bits -= 8;
		}
		if(bits >= 4){
			if(portNo == 2){
				return true;
			}
			bits -= 4;
		}
		if(bits >= 2){
			if(portNo == 1){
				return true;
			}
			bits -= 2;
		}
		if(bits >= 1){
			if(portNo == 0){
				return true;
			}
			bits -= 1;
		}
		return false;
	}

	@Override
	void reset(){}
	void resetAll(){}
	void close(){}
}