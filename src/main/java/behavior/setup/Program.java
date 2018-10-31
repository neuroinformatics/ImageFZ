package behavior.setup;

/**
 * 実行しているプラグインを識別するためのID。
 * 列挙型に変更した。
 * @author anonymous
 * @author Modifier:Butoh
 */
public enum Program{
	/*どのプログラムにも該当しないことが保障されているフィールド*/
	DEFAULT(""),
	/*各実験のフィールド*/
	LD("LD"),    //Light-Dark
	OF("OF"),    //Open-Field
	FZ("FZ"),    //Fear Conditioning
	CSI("CSI"),  //Social Interaction (Crawley's version)
	OLDCSI("CSI"),
	RM("RM"),    //Radial Maze
	PS("PS"),
	EPC("EP"),
	EP("EP"),    //Elevated Plus
	BT("BT"),    //Beam Test Online
	BTO("BT"),   //Beam Test Offline
	TS("TS"),    //Tail-Suspention
	OFC("OFC"),  //Open Field Circle
	HC1("HC1"),  //HomeCage 1匹
	HC2("HC2"),  //HomeCage 2匹
	HC3("HC3"),  //HomeCage 3匹以上
	FZS("FZ"),   //FearConditioning shock offline
	//WM("WM"),    //Water Maze
	//WMP("WM"),   //Water Maze Probe
	YM("YM"),    //Y-Maze
	BM("BM"),    //Barnes Maze
	SI("SI"),  //Old Social Interaction
	TM("TM"); //T-Maze

	private final String id;
	Program(String id){ this.id = id;}

	/**各実験の名前を返却。
	 *@param id プログラム番号フィールド
	 */
	@Override
	public String toString(){ return id; }

	/**
	 * HCかどうかを判定。
	 */
	public boolean isHC(){
		return this == HC1 || this == HC2 || this == HC3;
	}
}