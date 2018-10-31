package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.CSIParameter;

public class AnalyzerForStart{
	protected ImageManager imgManager;
	protected int[][] xyaData;
	private int[] dummy;
	protected final int EXIST_FLAG = 3;
	private Program program;

	public AnalyzerForStart(ImageProcessor backIp, Program program){
		imgManager = new ImageManager(backIp);
		this.program = program;
	}

	public boolean startAnalyze(ImageProcessor currentIp){
		boolean mouseExists = false;

		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.reduceNoise(ImageManager.SUBTRACT_IMG);

		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG,dummy);

		if((program == Program.OF && xyaData[0][EXIST_FLAG] >= 1) || (program == Program.SI && xyaData[0][EXIST_FLAG] > 1)){	
			mouseExists = true;
		}else if(program == Program.CSI){
			if(Parameter.getInt(CSIParameter.leftSeparator)<xyaData[0][0] && xyaData[0][0]<Parameter.getInt(CSIParameter.rightSeparator)){
				mouseExists = true;
			}
		}

		return mouseExists;
	}
}