package behavior.util.rmcontroller;

public class CloseAllDoorsCommand extends Command{

	protected void execute(){
		RMController.getInstance().closeAllDoors();
	}
}