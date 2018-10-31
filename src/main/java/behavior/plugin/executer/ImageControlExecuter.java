package behavior.plugin.executer;

import java.awt.event.KeyEvent;

import ij.IJ;

import behavior.setup.Program;
import behavior.plugin.analyzer.OFAnalyzer;

public class ImageControlExecuter extends OFExecuter{

	public ImageControlExecuter(){
		program = Program.OF;
	}

	public ImageControlExecuter(int allCage){
		program = Program.OF;
		this.allCage = allCage;
	}

	protected boolean startAtTheSameTime(){
		return true;
	}

	protected boolean interrupt(){
		if(IJ.shiftKeyDown()){
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
			while(true){
				if(IJ.shiftKeyDown())
					break;
				if(IJ.escapePressed()){
					endAnalysis();
					return true;
				}
			}
			IJ.setKeyUp(KeyEvent.VK_SHIFT);
		}
		if(IJ.escapePressed()){
			endAnalysis();
			return true;
		}
		return false;
	}

	private void endAnalysis(){
		for(int cage = 0; cage < allCage; cage++)
			((OFAnalyzer)analyze[cage]).interruptAnalysis();
	}

}
