package behavior.setup.dialog;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.setup.Setup;

public class CopyRoiDialog extends JDialog implements ActionListener {
	protected JButton ok;
	protected JButton cancel;
	protected JComboBox list;
	protected int allCage;
	protected boolean isOnline;

	public CopyRoiDialog(JDialog manager, boolean isModal, int allCage){
		super(manager, isModal);
		this.allCage = allCage;
		isOnline = ((DialogManager)manager).getTypes()==Setup.ONLINE;

		setModal(true);
		setTitle("Copy ROI");
		setLocation(manager.getLocation().x, manager.getLocation().y);

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.anchor = GridBagConstraints.EAST;
		JLabel label = new JLabel("Copy from: ");
		add(label, gbc);

		gbc.gridx = 1;
		gbc.ipadx = 5;
		gbc.anchor = GridBagConstraints.WEST;
		list = createList();
		add(list, gbc);

		ok = new JButton("OK");
		ok.addActionListener(this);
		cancel = new JButton("Cancel");
		cancel.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 2;
		gbc.ipadx = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		JPanel panel = new JPanel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, gbc);

		setSize(350, 150);
		setResizable(false);

		if(list.getItemCount() == 0)
			BehaviorDialog.showErrorDialog(this, "You don't have other projects!!");
		else
			setVisible(true);
	}

	private JComboBox createList(){
		JComboBox box = new JComboBox();

		File root = new File(FileManager.getInstance().getPath(FileManager.program));
		File[] list = root.listFiles();
		if(list != null)
			for(int i = 0; i < list.length; i++)
				if(list[i].isDirectory() && !list[i].getName().equals(new File(FileManager.getInstance().getPath(FileManager.project)).getName()))
					box.addItem(list[i].getName());

		return box;
	}

	protected void copyRoi(){
		String sep = System.getProperty("file.separator");

		String toPath = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String[] tmp = toPath.split(sep.equals("\\") ? "\\\\" : sep);
		tmp[tmp.length - 2] = (String)list.getSelectedItem();
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < tmp.length; i++)
			if(!tmp[i].equals(""))
				buf.append(sep).append(tmp[i]);
		String fromPath = buf.toString();

		if(!(new File(fromPath).exists()))
			BehaviorDialog.showErrorDialog(this, "Not Found: " + fromPath);
		else
			for(int i = 0; i < allCage; i++){
				File target = new File(fromPath + sep + "Cage Field" + (i + 1) + ".roi");
				if(!target.exists())
					BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
				else{
					try{
						Roi roi = new RoiDecoder(target.getAbsolutePath()).getRoi();
						String toRoi = toPath + sep + "Cage Field" + (i + 1) + ".roi";
						OutputStream output_stream = new FileOutputStream(toRoi);
						RoiEncoder encoder = new RoiEncoder(output_stream);
						encoder.write(roi);
						output_stream.close();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == ok){
			copyRoi();
			setVisible(false);
		} else if(e.getSource() == cancel)
			setVisible(false);
	}
}