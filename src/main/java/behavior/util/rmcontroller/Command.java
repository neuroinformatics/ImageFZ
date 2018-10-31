package behavior.util.rmcontroller;

public abstract class Command extends Thread{

	public void run(){
		execute();
	}
	protected abstract void execute();
}