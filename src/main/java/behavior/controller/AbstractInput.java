package behavior.controller;

public abstract class AbstractInput{
	abstract boolean getInput(int channel);	//input の取得
	abstract void clear(int value);	//初期化
	abstract void reset();	//初期化?
	abstract void resetAll();	//初期化?
	abstract void close();	//デバイスを閉じる
}