package behavior.io;

import java.io.File;

import ij.process.ImageProcessor;
import behavior.setup.Program;

public class SIResultSaver extends ResultSaver{

	public SIResultSaver(Program program, int allCage, ImageProcessor[] backIp) {
		super(program, allCage, backIp);
	}

	@Override
	public synchronized void addTraceImage(int cage, ImageProcessor traceIp){
		if(traceIp == null)
			return;
		traceStack[cage].addSlice("trace", traceIp.convertToRGB());
	}

	@Override
	public synchronized void saveTraceImage(){
		if(traceStack[0].getSize() == 0) return;
		SISafetySaver saver = new SISafetySaver();
		File traceFile = new File(FileManager.getInstance().getPath(FileManager.TracesDir));
		if(!traceFile.exists())
			traceFile.mkdir();
		saver.saveRGBImage(FileManager.getInstance().getPaths(FileManager.tracePath)[0], traceStack[0]);
	}
}