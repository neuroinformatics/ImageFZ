package behavior.plugin.executer;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.HC2Analyzer;
import behavior.setup.Program;

/**
 * Home Cage 2匹用Executer

 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class HC2Executer extends HCExecuter{

	public HC2Executer(){
		this(4);
	}

	public HC2Executer(int allCage){
		program = Program.HC2;
		this.allCage = allCage;
		binFileName = new String[2];
		binFileName[HC2Analyzer.XOR_AREA] = "xor_area";
		binFileName[HC2Analyzer.PARTICLE] = "particle";

		fm = FileManager.getInstance();
	}

	/** Traceを使用しない*/
	@Override
	protected void setWindow(ImageProcessor[] backIp){
		winOperator = WindowOperator.getInstance(allCage, backIp);
		for(int cage = 0; cage < allCage; cage++){
		    currentImp[cage] = new ImagePlus(subjectID[cage], backIp[cage]);
		}
		winOperator.setImageWindow(currentImp, WindowOperator.LEFT_UP,existMice);
		setEachWindow(backIp);	//abstract: 各実験固有のウィンドウを表示する

		winOperator.setInfoWindow(program);
		for(int i=0;i<existMice.length;i++)
			if(!existMice[i]) winOperator.setInfoText("***Empty***", i);
		winOperator.setXYWindow(program,existMice);
	}

	protected void setAnalyze(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new HC2Analyzer(backIp[cage], StartTime);
	}

}
