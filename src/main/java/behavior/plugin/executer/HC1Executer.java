package behavior.plugin.executer;

import ij.process.ImageProcessor;

import behavior.io.FileManager;
import behavior.plugin.analyzer.HC1Analyzer;
import behavior.setup.Program;

/**
 * Home Cage 1•C—pExecuter
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class HC1Executer extends HCExecuter{
	public HC1Executer(){
		this(4);
	}

	public HC1Executer(int allCage){
		program = Program.HC1;
		this.allCage = allCage;
		binFileName = new String[2];
		binFileName[HC1Analyzer.XOR_AREA] = "xor_area";
		binFileName[HC1Analyzer.DISTANCE] = "dist";

		fm = FileManager.getInstance();
	}

	protected void setAnalyze(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new HC1Analyzer(backIp[cage], StartTime);
	}

}