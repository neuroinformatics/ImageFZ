package behavior.io;

import java.io.File;

import behavior.setup.Program;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.text.TextPanel;
/**
 * WMResultSaverクラスと名前を変更した方が分かりやすい？
 * Header の利用が異なるため、幾つかのメソッドをオーバーライト
 * Start　Position　の保存を可能にした
 * Trace画像のバックアップを保存。処理の関係なのか、Trace画像が正しく表示されないことがあるので、二度保存することにした。
 * @author Windows
 *
 */

public class WMResultManager extends ResultSaver {
	private int allCage;
	private String[] subjectID;
	private ImageStack traceBackStack;

	private FileManager fileManager = FileManager.getInstance();

	public void setTraceImage(ImageProcessor[] backIp){
		/*
		for(int i=0;i<backIp.length;i++){
			backIp[i].invertLut();
		}
		 */
		super.setTraceImage(backIp);
	}

	public WMResultManager(Program program, int allCage, ImageProcessor[] backIp) {
		super(program, allCage, backIp);
		this.allCage = allCage;
		traceBackStack = (new ImagePlus("trace", backIp[0])).createEmptyStack();

	}

	public void saveImage(ImageProcessor[] backIp,String session){
		fileManager.setSubjectID(subjectID);
		super.saveImage(backIp,session);
	}

	public void saveXYResult(int cage, TextPanel xyTp,String session){
		super.saveXYResult(cage,xyTp,session);
	}

	public void saveTraceImage(String session){
		super.saveTraceImage(session);
	}

	public void addTraceImageBackUp(int cage, ImageProcessor traceIp){
		if(traceIp == null)
			return;
		traceBackStack.addSlice("trace", traceIp.convertToByte(false)); //念のため、もう一度 ByteProcessor になおす処理をする。
	}

	public void saveTraceBackUp(String session){
		for(int cage = 0; cage < allCage; cage++){
			SafetySaver saver = new SafetySaver();
			File traceFile = new File(fileManager.getPath(FileManager.TracesDir)+File.separator+"BackUp");
			if(!traceFile.exists())
				traceFile.mkdir();
			saver.saveImage(fileManager.getPath(FileManager.TracesDir) +File.separator+"BackUp"+File.separator + subjectID[cage]+".tif", traceBackStack);
		}
	}

	public void setSubjectID(String[] subjectID){
		super.setSubjectID(subjectID);
		if(subjectID.length != allCage)
			throw new IllegalArgumentException("cage num doesn't match:" + allCage + ":" + subjectID.length);
		this.subjectID = subjectID;
	}

	public void addTraceImage(int cage, ImageProcessor traceIp){
		super.addTraceImage(cage,traceIp);
	}

	//このメソッドは070906現在使われていない
	/*public void saveStartPosition(String[] position,boolean writeHeader){
		FileCreate creater = new FileCreate(fileManager.getPath(FileManager.totalResPath));
		if(writeHeader){
			String header = "StartPosition X" + "\t" + "Start Position Y";
			creater.write(header, true);
		}
		String startposition = "";;
		for(int i = 0;i<position.length;i++){
			startposition += position[i] + "\t";
		}
	}*/
}
