/*
 * Created on 2005/12/16
 *
 */
package behavior.util.rmstate;

import java.util.List;
//import java.util.logging.FileHandler;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//import java.util.logging.SimpleFormatter;

//import behavior.io.FileManager;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmcontroller.RMController;

/**
 * @author kazuaki kobayashi
 *
 * 餌の状態を監視し、餌を食べた場合にROIに通知する
 * 
 */
public class SensorMonitor extends Thread{
	private static SensorMonitor singleton = new SensorMonitor();
	private List<RMROI> rois = null;
	private boolean[] foodState;
	private boolean isStopped = false;
	//private Logger log = Logger.getLogger("behavior.util.rmstate.SensorMonitor");
	
	private SensorMonitor(){}

	public void initialize(){
		singleton = new SensorMonitor();
	}

	public static SensorMonitor getInstance(){
		return singleton;
	}
	
	/*
	 * これは必ずstartを行う前にセットが必要
	 */
	public void setRoiList(List<RMROI> rois){
		this.rois = rois;
	}
	
	public void run(){
		foodState = new boolean[]{true,true,true,true,true,true,true,true};

		/*try{
			FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "SensorLog.txt", 2048000,2);
			fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		//log.log(Level.INFO, "Start.");
		    for(int i=0;i<RMConstants.ARM_NUM;i++){
			//log.log(Level.INFO, "ini"+i);
		    boolean initialRes;
		        try{
			        initialRes = RMController.getInstance().isFoodExist(i);
			        if(initialRes == false){
			    	    //log.log(Level.INFO, (i+1) + " false.");
			    	    //log.log(Level.INFO, (i+1) + " Missing.");
				        rois.get(i).notifyMissing();
			            foodState[i] = false;
			        }
			        Thread.sleep(100);
		        }catch(Exception e){
			        e.printStackTrace();
		        }
		    }

		//log.log(Level.INFO, "loop");
		//log.log(Level.INFO, "food 1 "+foodState[0]);
		while(!isStopped){
			for(int i=0;i<RMConstants.ARM_NUM;i++){
				boolean res;
                try {
                    res = RMController.getInstance().isFoodExist(i);
                    //log.log(Level.INFO, "res "+res);
                    //log.log(Level.INFO, "food "+foodState[i]);
                    //if(!res && foodState[i])
                    //    log.log(Level.INFO, (i+1) + " false.");
                    if((foodState[i] != res) && (foodState[i] == true)){ //foodState[i] == trueは
                        foodState[i] = false;                        //糞が置かれても反応しないようにするため
                        //log.log(Level.INFO, (i+1) + " change Intake.");
                        rois.get(i).notifyIntake();
                    }
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                	e.printStackTrace();
                }
                catch (Exception e) {
                	e.printStackTrace();
                }
            }
		}
	}

    public void end(){
		isStopped = true;
	}
}