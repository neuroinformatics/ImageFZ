package behavior.setup.dialog;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import behavior.gui.ExtendedJButton;
import behavior.gui.ExtendedJTextField;
import behavior.gui.MovieManager;
import behavior.gui.OfflineMovieManager;
import behavior.gui.OnlineMovieManager;
import behavior.setup.Setup;
import behavior.setup.parameter.*;
import behavior.setup.parameter.variable.IntSliderVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;
import behavior.setup.parameter.variable.ThresholdIntVariable;
import behavior.setup.parameter.variable.Variable;

/**
 * Threshold の設定。
 *　ONLINE,OFFLINE ともに画面を見ながら調節できる。
 */
public class ThresholdDialogPanel extends AbstractDialogPanel {

	private final int FIELD_WIDTH = 3;

	private MovieManager movie;
	private JSlider[] sliders;
	private ExtendedJTextField[] fields;
	private JCheckBox[] checks;
	private JComboBox combo;
	private ExtendedJButton preview;
	private int type;

	private Parameter parameter;
	private boolean isChangeParameter = false;

	public String getDialogName(){
		return "Threshold Settings";
	}

	public ThresholdDialogPanel(DialogManager manager, int type){
		super(manager);
		this.type = type;
		parameter = Parameter.getInstance();
		isChangeParameter = false;

		setDialog();
	}

	public void preprocess(){
		isChangeParameter = true;
		if(type == Setup.ONLINE)
			movie = new OnlineMovieManager(sliders, fields, checks, manager.getProgram(), manager.getBackIp());
		else
			movie = new OfflineMovieManager(sliders, fields, checks, manager.getProgram(), manager.getSubjectID()[0]);
		movie.start();

		if(type == Setup.OFFLINE){
			combo.removeAllItems();
			for(int i = 0; i < manager.getSubjectID().length; i++)
				combo.addItem(manager.getSubjectID()[i]);
		}
	}

	public void postprocess(){
		this.getComponent(2).requestFocus();
	}

	public void endprocess(){
		isChangeParameter = false;
		movie.end();
	}

	private void setDialog(){
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		Variable[] vars = parameter.getVar();
		sliders = new JSlider[vars.length];
		fields = new ExtendedJTextField[vars.length];
		checks = new JCheckBox[vars.length];
		for(int i = 1; vars[i] != null; i++){
			if(vars[i] instanceof IntSliderVariable){
				add(new JLabel(vars[i].getShowName()), gbc);
				gbc.gridx = 1;
				add(sliders[i] = new JSlider(((IntSliderVariable)vars[i]).getMin(),
												((IntSliderVariable)vars[i]).getMax(), 
												((IntSliderVariable)vars[i]).getDefVar()), gbc);
				gbc.gridx = 2;
				add(fields[i] = new ExtendedJTextField(Integer.toString(sliders[i].getValue()), FIELD_WIDTH), gbc);
				fields[i].addCaretListener(new ThresholdValueChangeListener(sliders[i]));
				sliders[i].addChangeListener(new SliderChangeListener(fields[i]));
				gbc.gridy++;
				gbc.gridx = 0;
			} else if(vars[i] instanceof ThresholdIntVariable){
				gbc.gridx = 1;
				gbc.anchor = GridBagConstraints.LINE_START;
				add(new JLabel(vars[i].getShowName()), gbc);
				gbc.anchor = GridBagConstraints.LINE_END;
				gbc.gridx = 2;
				add(fields[i] = new ExtendedJTextField("0", FIELD_WIDTH), gbc);
				gbc.gridy++;
				gbc.gridx = 0;
			} else if(vars[i] instanceof ThresholdBooleanVariable){
				add(checks[i] = new JCheckBox(), gbc);
				gbc.gridx = 1;
				gbc.anchor = GridBagConstraints.LINE_START;
				add(new JLabel(vars[i].getShowName()), gbc);
				gbc.gridy++;
				gbc.gridx = 0;
				gbc.anchor = GridBagConstraints.LINE_END;
			}
		}

		// OFFLINE のプレビュー選択用
		if(type == Setup.OFFLINE){
			combo = new JComboBox();
			combo.setPreferredSize(new Dimension(160,18));
			preview = new ExtendedJButton("Preview");
			preview.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					movie.end();
					movie = new OfflineMovieManager(sliders, fields, checks, manager.getProgram(), 
							(String)combo.getSelectedItem());
					movie.start();
				}
			});

			add(new JLabel("Subject ID: "), gbc);
			gbc.gridx = 1;
			gbc.anchor = GridBagConstraints.LINE_START;
			add(combo, gbc);
			gbc.gridx = 2;
			add(preview, gbc);
		}
	}

	public void load(Properties properties) {
		if(properties != null){
			Variable[] var = Parameter.getInstance().getVar();
			for(int i = 1; var[i] != null; i++){
				if(var[i] instanceof IntSliderVariable){
					fields[i].setText(properties.getProperty(parameter.getVar(i).getName(), Integer.toString(((IntSliderVariable)var[i]).getDefVar())));
					sliders[i].setValue(Integer.parseInt(fields[i].getText()));
				} else if(var[i] instanceof ThresholdIntVariable)
					fields[i].setText(properties.getProperty(parameter.getVar(i).getName(), Integer.toString(((ThresholdIntVariable)var[i]).getDefVar())));
				else if(var[i] instanceof ThresholdBooleanVariable)
					checks[i].setSelected(Boolean.valueOf(properties.getProperty(parameter.getVar(i).getName(), Boolean.toString(((ThresholdBooleanVariable)var[i]).getDefVar()))));
			}

		}
	}

	public boolean canGoNext(){
		Variable[] vars = parameter.getVar();
		for(int i = 1; vars[i] != null; i++)
			if(vars[i] instanceof IntSliderVariable)
				((IntSliderVariable)vars[i]).setVar(sliders[i].getValue());
			else if(vars[i] instanceof ThresholdIntVariable)
				((ThresholdIntVariable)vars[i]).setVar(Integer.parseInt(fields[i].getText()));
			else if(vars[i] instanceof ThresholdBooleanVariable)
				((ThresholdBooleanVariable)vars[i]).setVar(checks[i].getSelectedObjects() != null);

		return true;
	}

	public class SliderChangeListener implements ChangeListener{
		private JTextField field;
		public SliderChangeListener(JTextField field){
			this.field = field;
		}
		public void stateChanged(ChangeEvent e) {
			if(isChangeParameter)manager.setModifiedParameter(true);
			int value = ((JSlider)e.getSource()).getValue();
			this.field.setText(value+"");
		}
	}

	private class ThresholdValueChangeListener implements CaretListener{
		private JSlider slider;
		public ThresholdValueChangeListener(JSlider slider){
			this.slider = slider;
		}

		public void caretUpdate(CaretEvent e) {
			String value = ((JTextField)e.getSource()).getText();

			try{
				int v = Integer.parseInt(value);
				if(v > 255){
					v = 255;  
				}
				this.slider.setValue(v);
			}
			catch (Exception ex) {
			}
		}   
	}


}
