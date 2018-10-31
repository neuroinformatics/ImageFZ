package behavior.setup.parameter.variable;

import java.awt.GridBagConstraints;
import java.util.Properties;

import javax.swing.JPanel;

public abstract class Variable{
	protected String name;	//Properties ファイルに保存する際の名前
	protected String showName;	//パラメーターダイアローグに表示する名前（表示しない場合は null)

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
	Properties に、変数を入れる
	 ******/
	public abstract void setProperties(Properties prop);

	/*****
	Properties から、変数を出す
	 ******/
	public abstract void loadProperties(Properties prefs);

	/*****
	変数を初期化する
	 ******/
	public abstract void initialize();

	/**
	 * 新Setup用に追加
	 */
	public abstract void addComponent(JPanel panel, GridBagConstraints gbc);

	public abstract void load(Properties properties);

	public abstract void setVar();

	public void setVar(int var){};
	public void setVar(double var){};
	public void setVar(boolean var){};
}