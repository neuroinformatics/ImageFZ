package behavior.util.rmcontroller;

//import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;
import ij.IJ;

//import behavior.io.FileManager;
import behavior.util.rmconstants.RMConstants;

import com.teravation.labjack.LabJack;
import com.teravation.labjack.LabJackException;
import com.teravation.labjack.LabJackFactory;
import com.teravation.labjack.monitor.LabJackMonitorFrame;

public class RMController{
	//private LabJackMonitorFrame frame;
	private LabJack labjack;
	private static RMController singleton = new RMController();
	private Object lock1 = new Object();
	private Object lock2 = new Object();
	//private Logger log = Logger.getLogger("behavior.util.rmcontroller.RMController");

    private RMController(){
		/*try{
			FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "ControllerLog.txt", 102400,1);
            fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
			e.printStackTrace();
		}*/
        LabJackFactory factory = new LabJackFactory();
		LabJack[] labs = null;
		try{
			labs = factory.getLabJacks();
		}catch(LabJackException e) {
		    IJ.error("Please make sure labjack is set properly");
		   	e.printStackTrace();
		}

	    labjack = labs[0];

		new LabJackMonitorFrame(labjack);
		//log.log(Level.INFO, "Setup complete.");
	}

	public synchronized static RMController getInstance(){
		return singleton;
	}

	public void openAllDoors(){
		for(int i=0;i<RMConstants.ARM_NUM;i++){
		    open(i);
		}
		//log.log(Level.INFO, "All Doors are opened.");
	}

	public void closeAllDoors(){
		for(int i=0;i<RMConstants.ARM_NUM;i++){
			close(i);
		}
		//log.log(Level.INFO, "All Doors are closed.");
	}

	void open(int num){
		if(num >= RMConstants.ARM_NUM) return;

		synchronized(lock1){
		    try{
			    labjack.setDForOutput(num);
			    //log.log(Level.INFO, "D" + num + " is " + (labjack.isDForOutput(num)?"Output.":"Input."));
			    labjack.setD(num,true);
			    //log.log(Level.INFO, "D" + num + " is " + "set true(open)" + ".");
		    }catch(LabJackException e) {
			    e.printStackTrace();
		    }
		}
	}

	void close(int num){
		if(num >= RMConstants.ARM_NUM) return;        //D6‚Ü‚Å‚µ‚©‚¢‚ç‚È‚¢‚Ì‚Å‚»‚êˆÈã‚¾‚Æ‰½‚à‚µ‚È‚¢

		synchronized(lock1){
		    try{
			    labjack.setDForOutput(num);
			    //log.log(Level.INFO, "D" + num + " is " + (labjack.isDForOutput(num)?"Output.":"Input."));
			    labjack.setD(num,false);
			    //log.log(Level.INFO, "D" + num + " is " + "set false(close)" + ".");
		    }catch(LabJackException e) {
			    e.printStackTrace();
		    }
		}
	}

	public boolean isFoodExist(int num) throws Exception{
	    if(num > 7) throw new Exception("invalid number");

		num += RMConstants.ARM_NUM;
		boolean result = false;

		synchronized(lock2){
		    try{
			    labjack.setDForInput(num);
			    //log.log(Level.INFO, "D" + num + " is " + (labjack.isDForOutput(num)?"Output.":"Input."));
			    labjack.updateD(num);
			    //log.log(Level.INFO, "D" + num + " is " + (labjack.getD(num)?"true(exist)":"false(none)") + ".");
			    return labjack.getD(num);
		    }catch(LabJackException e) {
			    e.printStackTrace();
		    }
		}
		return result;
	}
}