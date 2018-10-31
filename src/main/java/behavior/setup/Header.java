package behavior.setup;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.HCParameter;
/*******
ヘッダの総合管理
 *******/
public class Header{
	private static final String tab = "\t";

	/******
	メインデータ表示と、保存の際のヘッダ
	Resultsの一行目に出力される。
	 *******/
	public static String getTotalResultHeader(Program program){
		switch(program){
		case LD:  return "ID" +tab+ "Dark-Distance(cm)" +tab+ "Light-Distance(cm)" +tab+ "Dark-Time(sec)" +tab+ "Light-Time(sec)"+tab+ "Number_of_Transitions" +tab+ "Total_Duration_of_Trial(sec)" +tab+ "Latency_to_Enter_Light(sec)";
		case FZS:
		case OFC:
		case OF:  return "ID" +tab+ "TotalDistance(cm)" +tab+ "TotalCenterTime(sec)" +tab+ "AverageSpeed(cm/s)" +tab+ "MovingSpeed(cm/s)" +tab+ "MoveEpisodeN"  +tab+ "TotalMovementDuration(sec)" +tab+ "DistancePerMovement(cm)" +tab+ "DurationPerMovement(sec)" +tab+ "Duration(sec)";
		case FZ:  return "ID" +tab+ "TotalDistance(cm)" +tab+ "TotalFreezPercent(%)";
		case OLDCSI:
		case CSI: return "ID" +tab+ "LeftCageID" +tab+ "RightCageID" +tab+ "TotalDistance(cm)" +tab+ "AverageSpeed(cm/s)" +tab+ "STonLeftCage(sec)" +tab+ "STonRightCage(sec)" +tab+ "NEintoLeftCage" +tab+ "NEintoRightCage" +tab+ "DistinLeftCage(cm)" +tab+ "DistinRightCage(cm)" +tab+ "STonLeftArea(sec)" +tab+ "STonCenterArea(sec)" +tab+ "STonRightArea(sec)" +tab+ "NEintoLeftArea" +tab+ "NEintoCenterArea" +tab+ "NEintoRightArea" +tab+ "DistinLeftArea(cm)" +tab+ "DistinCenterArea(cm)" +tab+ "DistinRightArea(cm)";
		case SI: return "ID" +tab+ "TotalDurContact(sec)" +tab+ "TotalDurActiveContact(sec)" +tab+ "MeanDurPerContact" +tab+ "NumTotalContacts" +tab+ "NumContactFromDark"  +tab+ "NumContactFromLight" +tab+ "TotalDistance(Dark;cm)" +tab+ "TotalDistance(Light;cm)" +tab+ "TotalDistanceAverage";
		case RM:  return "TrialName" +tab+ "SelectedMode" +tab+ "Latency(sec)" +tab+ "TotalDistance(cm)" +tab+ "WorkingMemoryError" +tab+ "IntakeAfterOmission" +tab+ "OmissionAfterOmission" +tab+ "TotalRevisiting" +tab+ "TotalOmissionError" +tab+ "TotalArmChoiceNum" +tab+ "TotalFoodIntake" +tab+ "DifferentArmNOInFirst8" +tab+ "ReferenceMemoryError" +tab+ "R_WMemoryError";
		case YM:  return "ID" +tab+ "TotalDistance(cm)" +tab+ "Latency(sec)" +tab+ "TotalEntries" +tab+ "TotalChoices" +tab+ "TotalAlternations" +tab+ "Alternation(%)" +tab+ "STonCenter(sec)" +tab+ "STonArm1(sec)" +tab+ "STonArm2(sec)" +tab+ "STonArm3(sec)" +tab+ "DistinArm1(cm)" +tab+ "DistinArm2(cm)" +tab+ "DistinArm3(cm)" +tab+ "NEintoArm1" +tab+ "NEintoArm2" +tab+ "NEintoArm3";
		case EPC: return null;
		case EP:  return "ID" +tab+ "TotalDistance(cm)" +tab+ "STonCenter(sec)" +tab+ "STonNorth(sec)" +tab+ "STonSouth(sec)" +tab+ "STonWest(sec)" +tab+ "STonEast(sec)" +tab+ "ST_Nowhere(sec)" +tab+ "PercentageOpenArmStayTime(%)" +tab+ "NEintoNorth" +tab+ "NEintoSouth" +tab+ "NEintoWest" +tab+ "NEintoEast" +tab+ "TotalEntries" +tab+ "PercentageOpenArmEntries(%)" +tab+ "OpenArmLocations";
		case BT : return "TrialName" +tab+ "TotalDiatance(cm)" +tab+ "AverageSpeed(cm/s)" +tab+ "MovingSpeed(cm/s)" +tab+ "MoveEpisodeN" +tab+ "TotalMovementDuration(sec)" +tab+ "Slip" +tab+ "Latency(sec)" + tab + "Call" + tab + "Duration(sec)";
		case BTO: return "TrialName" +tab+ "TotalDistance(cm)" +tab+ "AverageSpeed(cm/s)" +tab+ "MovingSpeed(cm/s)" +tab+ "MoveEpisodeN" +tab+ "TotalMovementDuration(sec)" +tab+ "Latency(sec)" + tab + "Duration(sec)";
		case PS:
		case TS:  return "ID";
		case HC1: return "ID" +tab+ "Duration" +tab+ "rate = " + Parameter.getInt(Parameter.rate) + "fps" + tab + "Light ON = " + Parameter.getInt(HCParameter.LightON) +"h"+tab+ "Light OFF = "+ Parameter.getInt(HCParameter.LightOFF) + "h";
		case HC2: return "ID" +tab+ "Duration" +tab+ "rate = " + Parameter.getInt(Parameter.rate) + "fps" + tab +"Light ON = " + Parameter.getInt(HCParameter.LightON) +"h"+tab+ "Light OFF = "+ Parameter.getInt(HCParameter.LightOFF) + "h";
		case HC3: return "ID" +tab+ "Duration" +tab+ "Mouse Number" +tab+ "rate = " + Parameter.getInt(Parameter.rate) + "fps" + tab + "Light ON = " + Parameter.getInt(HCParameter.LightON) +"h"+tab+ "Light OFF = "+ Parameter.getInt(HCParameter.LightOFF) + "h";
		//case WM:  return "_ID"+tab+"Trial"+tab+"Start"+tab+"totalDistance(cm)"+tab+"meanSpeed(cm/sec)"+tab + "movingSpeed(cm/sec)" + tab+"nomovementTime(sec)" + tab + "Peri(%)"+tab+"Latency(s)"+tab+"Ave Latency(s)"+tab+"Last Time";
		//case WMP: return "_ID"+tab+"Trial"+tab+"Start"+tab+"totalDistance(cm)"+tab+"meanSpeed(cm/sec)"+tab + "movingSpeed(cm/sec)" + tab+"nomovementTime(sec)" +tab+"Peri(sec)"+tab+"Stay0" + tab + "Cross0" +tab+"Stay1" + tab + "Cross1" +tab+"Stay2" + tab + "Cross2" +tab+"Stay3" + tab + "Cross3"+tab + "NP"+tab+"Last Time";
		case BM : return "SubjectID" + tab + "TotalErr" + tab + "LatToEnter" + tab + "TotalDist(cm)"+tab + "ErrTo1st" + tab + "LatTo1st" + tab + "DistTo1st" + tab + "OmissionErr" + tab + "MovingTime" + tab + "TimeAroundTarget" + tab + "TotalSpeed" + tab + "MovingSpeed" + tab +  "MultipleParticles" + tab + "NoParticles";
		case TM : return "MouseID" +tab+ "TestType" +tab+ "Latency(sec)" +tab+ "NumberOfChoices" +tab+ "NumberOfChoices_Right" +tab+ "NumberOfChoices_Left" +tab+ "Correct" +tab+ "Error" +tab+ "CorrectPercentage" +tab+ "TotalDistance(cm)";
		default: throw new IllegalArgumentException("the program does not have header");
		}
	}

	/*******
	XY データウィンドウと保存用のヘッダ。
	 *******/
	public static String getXYHeader(Program program){
		switch(program){
		case LD:  return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "LD";
		case FZ:  return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "AREA(xor)" +tab+ "FZ" +tab+ "conseq FZ";
		case SI:  return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "Interaction";
		case EP:  return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "Position";
		case TS:  return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "AREA(xor)" +tab+ "FZ" +tab+ "conseq FZ";
		//case WM:  return "Slice"+tab+"X"+tab+"Y"+tab+"AREA";
		//case WMP: return "Slice"+tab+"X"+tab+"Y"+tab+"AREA"+tab+"nowQuad";
		case HC1:
		case HC2:
		case HC3:
			return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA" +tab+ "day-night";
		case OF:
		case OLDCSI:
		case CSI:
		case RM:
		case YM:
		case BT :
		case BTO:
		case FZS:
		case OFC: 
		case BM :
		case TM:
			return "Slice" +tab+ "X" +tab+ "Y" +tab+ "AREA";
		case PS:
		case EPC:
			return null;

		default: throw new IllegalArgumentException("the program does not have header");
		}
	}

	/******
	info ウィンドウ用のヘッダ。
	 *******/
	public static String getInfoHeader(Program program){
		switch(program){
		case LD : return "SubjectID" +tab+ "Elapsed Time" +tab+ "Dark-Distance" +tab+ "Light-Distance" +tab +"Dark-Time" +tab+ "Light-Time" +tab+ "transitions";
		case FZS:
		case OF : return "SubjectID" +tab+ "Elapsed Time" +tab+ "x" +tab+ "y" +tab+ "Center Time" +tab+ "Current Dist";
		case FZ : return "SubjectID" +tab+ "Elapsed Time" +tab+ "Freeze" +tab+ "Changed Area";
		case CSI:
		case OLDCSI: return "SubjectID"+tab+"Elapsed Time" +tab+ "Move" +tab+ "Distance" +tab+ "STonLCage" +tab+ "NEintoLCage";
		case SI : return "SubjectID"+tab+"Elapsed Time"+tab+"Interaction Time";
		case RM : return "SubjectID" +tab+ "Latency";
		case YM: return "SubjectID" +tab+ "Elapsed Time" +tab+ "Distance" +tab+ "NEintoArm1"+tab+"STonArm1" +tab+ "Choice" +tab+ "Alternation";
		case PS : return null;
		case EPC: return null;
		case EP : return "SubjectID" +tab+ "Elapsed Time" +tab+ "North" +tab+ "South" +tab+ "West" +tab+ "East" +tab+ "Nowhere";
		case BT : return "SubjectID" + tab +"Latency" + tab +  "SlipCount";
		case BTO: return "SubjectID" + tab +"Latency";
		case TS : return "SubjectID" +tab+ "Elapsed Time" +tab+ "x" +tab+ "y" +tab+ "Immobility" +tab+ "Changed Area";
		case HC1: return "SubjectID" +tab+ "Start Time" +tab+ "Elapsed Time" +tab+ "x" +tab+ "y" +tab+ "Current Dist" +tab+ "Changed Area";
		case HC2: return "SubjectID" +tab+ "Start Time" +tab+ "Elapsed Time" +tab+ "X1" +tab+ "Y1" +tab+"X2"+tab+"Y2"+tab+ "Particle"+tab+"Changed Area";
		case HC3: return "SubjectID" +tab+ "Start Time" +tab+ "Elapsed Time" +tab+ "Particle"+tab+"Changed Area";
		//case WM : return "Chamber"+tab+"Elapsed Time"+ tab + "Start"+tab+"Latency"+tab+"Peri"+tab+"aveLate";
		//case WMP: return "Chamber"+tab+"Elapsed Time"+ tab + "Start"+tab+"Peri"+tab+"now";
		case OFC: return "SubjectID" +tab+ "Elapsed Time" +tab+ "x" +tab+ "y" +tab+"Current Dist" +tab+ "Rearing";
		case BM : return "SubjectID"+tab+"Elapsed Time"+ tab + "X"+tab+"Y"+tab+"Current Dist" + tab + "Target";
		case TM: return "SubjectID" +tab+ "Latency";
		default: throw new IllegalArgumentException("the program does not have header");
		}
	}
}