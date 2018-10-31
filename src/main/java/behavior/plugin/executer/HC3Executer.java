package behavior.plugin.executer;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.io.FileInputStream;
import java.util.Properties;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.HC3Analyzer;
import behavior.setup.Program;

/**
 * Home Cage 3匹以上用Executer
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public class HC3Executer extends HCExecuter {
	public HC3Executer(){
		this(4);
	}

	public HC3Executer(int allCage){
		program = Program.HC3;
		this.allCage = allCage;
		binFileName = new String[2];
		binFileName[HC3Analyzer.XOR_AREA] = "xor_area";
		binFileName[HC3Analyzer.PARTICLE] = "particle";

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
		// MouseNumber を読み込む
		String path = fm.getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "mouseNumber.properties";
		Properties prefs = new Properties();
		int[] mouseNumber = new int[allCage];
		try{
			FileInputStream fis = new FileInputStream(path);
			prefs.load(fis);
			fis.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		String defaultNumber = "4";
		for(int cage = 0; cage < allCage; cage++)
			mouseNumber[cage] = Integer.parseInt(prefs.getProperty(subjectID[cage], defaultNumber));

		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new HC3Analyzer(backIp[cage], StartTime, mouseNumber[cage]);
	}
}
