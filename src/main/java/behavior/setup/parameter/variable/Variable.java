package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

public abstract class Variable{
	protected String name;	//Properties �t�@�C���ɕۑ�����ۂ̖��O
	protected String showName;	//�p�����[�^�[�_�C�A���[�O�ɕ\�����閼�O�i�\�����Ȃ��ꍇ�� null)

	public Variable(String name, String showName){
		this.name = name;
		this.showName = showName;
	}

	public String getName(){
		return name;
	}

	public String getShowName(){
		return showName;
	}

	/*****
	Properties �ɁA�ϐ�������
	 ******/
	public abstract void setProperties(Properties prop);

	/*****
	Properties ����A�ϐ����o��
	 ******/
	public abstract void loadProperties(Properties prefs);

	/*****
	�ϐ�������������
	 ******/
	public abstract void initialize();

	/**
	 * �VSetup�p�ɒǉ�
	 */
	public abstract void addComponent(JPanel panel, GridBagConstraints gbc);

	public abstract void load(Properties properties);

	public abstract void setVar();

	public void setVar(int var){};
	public void setVar(double var){};
	public void setVar(boolean var){};
}