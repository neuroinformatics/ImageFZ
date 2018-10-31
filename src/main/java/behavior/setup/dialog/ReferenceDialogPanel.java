package behavior.setup.dialog;

import javax.swing.JPanel;

public abstract class ReferenceDialogPanel extends JPanel{
	public boolean canGoNext(){
		return true;
	}
	public void preprocess(){}
	public void postprocess(){}
	public void save(){}
}