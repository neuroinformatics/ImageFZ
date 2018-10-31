package behavior.setup;

/**
 * ���s���Ă���v���O�C�������ʂ��邽�߂�ID�B
 * �񋓌^�ɕύX�����B
 * @author anonymous
 * @author Modifier:Butoh
 */
public enum Program{
	/*�ǂ̃v���O�����ɂ��Y�����Ȃ����Ƃ��ۏႳ��Ă���t�B�[���h*/
	DEFAULT(""),
	/*�e�����̃t�B�[���h*/
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
	HC1("HC1"),  //HomeCage 1�C
	HC2("HC2"),  //HomeCage 2�C
	HC3("HC3"),  //HomeCage 3�C�ȏ�
	FZS("FZ"),   //FearConditioning shock offline
	//WM("WM"),    //Water Maze
	//WMP("WM"),   //Water Maze Probe
	YM("YM"),    //Y-Maze
	BM("BM"),    //Barnes Maze
	SI("SI"),  //Old Social Interaction
	TM("TM"); //T-Maze

	private final String id;
	Program(String id){ this.id = id;}

	/**�e�����̖��O��ԋp�B
	 *@param id �v���O�����ԍ��t�B�[���h
	 */
	@Override
	public String toString(){ return id; }

	/**
	 * HC���ǂ����𔻒�B
	 */
	public boolean isHC(){
		return this == HC1 || this == HC2 || this == HC3;
	}
}