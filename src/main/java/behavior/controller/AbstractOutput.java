package behavior.controller;

public abstract class AbstractOutput{
	abstract boolean open(); //device を開く
	abstract void clear(int bits);	//初期化(アウトプットを閉じる）
	abstract void controlOutput(int bits);	//アウトプットを出す
	abstract void close();	//device を閉じる

}


