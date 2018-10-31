/*
 * Created on 2005/12/16
 *
 */
package behavior.util.rmstate;

import behavior.plugin.analyzer.RMAnalyzer;


/**
 * @author kazuaki kobayashi
 *
 *  stateパターンを利用する
 *  現在のstateに応じて、intake,enter,exitの際の処理を指定する
 * 
 */
public class RMROI {
	
	private RMState currentState;
	private int number;
	
	public RMROI(int number){
		this.number = number;
		currentState = FirstState.getInstance();
	}

	public synchronized void notifyMissing(){
		currentState.missing(this);
	}

	public synchronized void notifyIntake(){
		currentState.intake(this);
	}
	
	public synchronized void notifyEnter(){
		RMAnalyzer.addVisitedArm(number);
		currentState.enter(this);
	}
	
	public void notifyExit(){
		//RMAnalyzer.addVisitedArm(9);
		this.currentState.exit(this);
	}

	public void changeState(RMState state){
		currentState = state;
	}

	public int getNumber() {
		return number;
	}
}