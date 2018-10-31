package behavior.util.rmstate;

public class EnterWorkingMemoryErrorState implements RMState {
	private static RMState singleton = new EnterWorkingMemoryErrorState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){}

	public void exit(RMROI roi){
		roi.changeState(OutAfterIntakeState.getInstance());
	}
}