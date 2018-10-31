package behavior.util.rmconstants;

public enum StateConstants{
	INTAKE("T"),INTAKE_AFTER_OMISSION("IO"),OMISSION("O"),WORKING_MEMORY_ERROR("W"),
	OMISSION_AFTER_OMISSION("OO"),REFERENCE_MEMORY_ERROR("R"),DOUBLE_ERROR("RW");

	private final String state;
	private StateConstants(String str){this.state = str;}
	public String getString(){return state;}
}