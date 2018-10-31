package behavior.plugin.analyzer;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.image.process.RMTracer;
import behavior.io.FileManager;
import behavior.io.RMReferenceManager;
import behavior.util.rmstate.RMROI;
import behavior.util.rmstate.SensorMonitor;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.RMParameter;
import behavior.util.rmcontroller.*;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmconstants.StateConstants;

public class RMAnalyzer extends Analyzer {
	//�g�p����p�����[�^�[
	private final int rate = Parameter.getInt(Parameter.rate);
	private final int duration = Parameter.getInt(Parameter.duration);
	private final int waitTime = Parameter.getInt(RMParameter.delay)*1000; //Center�Ƀ}�E�X������߂鎞��
	private final int foodArmNum = RMConstants.getFoodArmNum(); //�a�̂���A�[���̐�
	private boolean NSense; //�a�̗L������ʂ��Ȃ����[�h

	//�g�p������W
	protected int[][] xyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData;

	//��͌���
	private double totalDistance = 0.0; //����

	private static int intake = 0; //������H�ׂ��Ƃ�
	private static int intakeAfterOmission = 0; //������H�ׂȂ������㓯���A�[���ŐH�ׂ��Ƃ�
	private static int omission = 0; //������H�ׂȂ������Ƃ�
	private static int omissionAfterOmission = 0; //2�񓯂��A�[���ɓ����Ăǂ����������H�ׂȂ������Ƃ�
	private static int workingMemoryError = 0; //�H�ׂ��㓯���A�[���ɓ������Ƃ�
	private static int referenceMemoryError = 0; //���Ƃ��Ɖa�̂Ȃ��A�[���ɓ������Ƃ��B
	//2��ڂɂ��Ƃ��Ɖa�̂Ȃ��A�[���ɓ������Ƃ��B(WorkingMemoryError+ReferenceMemoryError�Ƃ���
	private static int doubleError = 0;

	private static List<Integer> armHistory = new ArrayList<Integer>(); //�������A�[��
	private static List<StateConstants> episode = new ArrayList<StateConstants>(); //�G�s�\�[�h

	private List<Roi> rois = new ArrayList<Roi>();
	private List<RMROI> RMROIstates = new ArrayList<RMROI>();
	private static int previousPosition; //�ړ��O�̃|�W�V����
	private RMROI currentRMROI;
	private int currentSlice;
	private static double distance; //Arm1.roi��Center.roi�̒��S�̊Ԃ̋���
	protected boolean endAnalyze; //�������I�������邽�߂̃t���O

	private int totalSlice;
	protected boolean start = false;

    private final String tab = "\t";

	//�R���X�g���N�^�B���������s���B
	public RMAnalyzer(ImageProcessor backIp){
		//���l�̏�����
		initialize();

		imgManager = new ImageManager(backIp.duplicate()); //�Ƃ肠��������
		setImageSize(backIp.getWidth(), backIp.getHeight());

		//Arm��Center��Roi��ǂݍ���rois�ɃZ�b�g����
		setRois();
		//Arm1.roi��Center.roi�̒��S�̊Ԃ̋���
		distance = getRoiDistance(rois.get(0),rois.get(rois.size()-1));

		//RMROI��ʂ��ď��(intake�Ȃǁj�𐧌䂷��
		for(int i=0;i<RMConstants.ARM_NUM;i++){
			RMROIstates.add(new RMROI(i+1));
		}

		//ReferenceMemory��Offline�̏ꍇ�AMouse�����Ȃ��A�[���̃G�s�\�[�h��ω�������
		//(Online�ł�SensorMonitor�ōs���Ă��邪�AOffline�ł͎g�p���Ȃ�����B)
		if((RMConstants.isOffline() || Parameter.getBoolean(RMParameter.NSense)) && RMConstants.isReferenceMemoryMode()){
			String[] id = FileManager.getInstance().getPaths(FileManager.SubjectID);
			RMReferenceManager ref = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.sessionPath));
			try{
			    String alignment = ref.getFoodArmAlignment(id[0], "def");
			    for(int i=1;i<RMConstants.ARM_NUM+1;i++){
			        if(alignment.indexOf(""+i)==-1){
			        	RMROIstates.get(i-1).notifyMissing();
			        }
			    }
			}catch(IOException e){
				e.printStackTrace();
			}
		}

		/*Online��SensorMonitor���g�p����ꍇ�A�G�s�\�[�h�𐧌䂷��RMROI��n��*/
		if(!RMConstants.isOffline()){
			NSense = Parameter.getBoolean(RMParameter.NSense);
		    if(!(RMConstants.DEBUG || NSense))
		        SensorMonitor.getInstance().setRoiList(RMROIstates);
		}else{
			NSense = true;
		}

		tracer = new RMTracer(backIp,rois);
	}

	/*static�Ȑ��l�̏�����*/
	private void initialize(){
		intake = 0;
		intakeAfterOmission = 0;
		omission = 0; 
		omissionAfterOmission = 0;
		workingMemoryError = 0;
		referenceMemoryError = 0;
		doubleError = 0;

		previousPosition = -1;
		currentRMROI = null;

		rois.clear();
		RMROIstates.clear();

		armHistory.clear();
		episode.clear();

		endAnalyze= false;
	}

	/*Arm��Center��Roi��ǂݍ���rois�ɃZ�b�g����*/
	public void setRois(){
        final String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir) + System.getProperty("file.separator");

        for(int i = 0; i < RMConstants.ARM_NUM; i++){
	     	String fileName = "Arm"+(i+1)+".roi"; 
	     	RoiDecoder decoder = new RoiDecoder(path+fileName);
	     	try {
	     		Roi roi = decoder.getRoi();
				rois.add(roi);
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        String fileName2 = "Center.roi";
        RoiDecoder decoder = new RoiDecoder(path+fileName2);
     	try {
     		Roi roi = decoder.getRoi();
			rois.add(roi);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

	public boolean isEndAnalyze(){
		return endAnalyze;
	}

	//bin�͎g�p���Ȃ�
	@Override
	public boolean binUsed(){
		return false;
	}

	/*�摜�����H����B*/
	@Override
	public void analyzeImage(ImageProcessor currentIp) {
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//�O��̐��l���c���Ă���
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	@Override
	public void calculate(final int currentSlice){
	   this.currentSlice = currentSlice;
	   if(currentSlice > 0){
		   //����
		   final double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
                                                        prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
		   totalDistance += currentDistance;

		   final int currentPosition = getCurrentRoiNumber(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],RMConstants.ROI_NUM);

		   /*�}�E�X��state�֌W*/
		   analyzeState(currentPosition, xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

		   //Offline�͑S��NSense
		   if(NSense){
			   if(intake == foodArmNum)
			       endAnalyze = true;
		   }else{
			   if(intake+intakeAfterOmission == foodArmNum)
				   endAnalyze = true;
		   }

		   //trace��`��
		   tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

		   totalSlice++;
	   }
	   tracer.setPrevXY(xyaData[0][0], xyaData[0][1]);
	}

	//���݂̃}�E�X�̃|�W�V������Roi�ƍ��W�Ŕ��肷��(1-8�F�A�[���̔ԍ��A9�FCenter)
	private int getCurrentRoiNumber(final int x,final int y,final int size) {
        for(int i=0;i<size;i++){
            Roi roi = rois.get(i);
            if(roi.contains(x,y)){
                return i+1;
            }
        }
        return -1;
    }

	//�|�W�V�������ړ������Ƃ��̏���
	private void analyzeState(final int currentPosition,final int x, final int y){
		final int CENTER = RMConstants.ROI_NUM;
		//�ړ����Ă��Ȃ��Ƃ��A�܂��͈ړ��O�̃|�W�V�������ʏ�z�肵�Ă��Ȃ����̂ł���ꍇ
        if(currentPosition < 1 || previousPosition == currentPosition){
            return;
        }else if(currentPosition < CENTER && !(previousPosition==-1 || previousPosition == CENTER)){
        	return;
        }

		if(currentPosition < CENTER){
			currentRMROI = RMROIstates.get(currentPosition-1);
			if(!(RMConstants.isOffline() || RMConstants.DEBUG))
		        new CloseOtherDoorsCommand(currentPosition-1).start();
		    currentRMROI.notifyEnter();
		    previousPosition = currentPosition;
		}else if(currentPosition == CENTER && currentRMROI != null){//���S�ɗ����Ƃ�
             double distanceToCenter
                 = Point2D.distance(x,y,rois.get(currentRMROI.getNumber()-1).getBounds().getCenterX(),
            		                         rois.get(currentRMROI.getNumber()-1).getBounds().getCenterY());

             if(distanceToCenter < distance){
                 return;
             }

		     currentRMROI.notifyExit();

		     //�I�������𖞂������ꍇ�A�ȍ~�̃h�A�̑���͂��Ȃ�
		     if(NSense){
			     if(intake+referenceMemoryError == 8)
				     return;
			 }else{
				 if(intake+intakeAfterOmission == foodArmNum)
				     return;
			 }


		     if(!(RMConstants.isOffline() || RMConstants.DEBUG)){
			     new Thread(){    //�摜�擾���~�܂�Ȃ��悤�ɕʃX���b�h���쐬
			         public void run(){
					     Thread th = new CloseAllDoorsCommand();
					     th.start();

					     //�X���b�h�̑҂�����
					     int threadWaitTime = 5000;
					     if(!RMConstants.isReferenceMemoryMode() && ((intake+intakeAfterOmission) == 4) 
					    		 && (episode.get(episode.size()-1)==StateConstants.INTAKE || episode.get(episode.size()-1)==StateConstants.INTAKE_AFTER_OMISSION))
					    	 threadWaitTime = Parameter.getInt(RMParameter.delayAfter4)*1000;
					     else
					    	 threadWaitTime = waitTime;
					     try{
					         Thread.sleep(threadWaitTime);    //�����߂�b�� 
					     }catch (InterruptedException e){
						     e.printStackTrace();
					     }
					     new OpenAllDoorsCommand().start();
				     }
			     }.start();
		     }

		     previousPosition = currentPosition;
		 }
    }

	private double getRoiDistance(final Roi source,final Roi target){
	    return Point2D.distance(source.getBounds().getCenterX(), source.getBounds().getCenterY(),
	    		                   target.getBounds().getCenterX(), target.getBounds().getCenterY());
	}

	public static void addVisitedArm(final int number){
		armHistory.add(number);
	}

	public static void addEpisode(StateConstants his){
		episode.add(his);
		switch(his){
		   case INTAKE: 
		   	 intake++;
		     break;
		   case INTAKE_AFTER_OMISSION:
		   	 intakeAfterOmission++;
		     break;
		   case OMISSION:
		   	 omission++;
		     break;
		   case OMISSION_AFTER_OMISSION:
		   	 omissionAfterOmission++;
		     break;
		   case WORKING_MEMORY_ERROR:
		   	 workingMemoryError++;
		     break;
		   case REFERENCE_MEMORY_ERROR:
			 referenceMemoryError++;
			 break;
		   case DOUBLE_ERROR:
			 doubleError++;
			 break;
		}
	} 

	public void interruptAnalysis(){
		if(currentSlice == 0)
			state = NOTHING;
		else if(currentSlice == (rate*duration)+1)
			state = COMPLETE;
		else{
			state = INTERRUPT;
		}
	}

	//SubtractImage��Ԃ�
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

    //Information�E�B���h�E�̏��
	@Override
	public String getInfo(final String subjectID,final int sliceNum){
		String info = subjectID +tab+ getElapsedTime(sliceNum);
		return info;
	}

	//XY�E�B���h�E�̏��
	@Override
	public String getXY(final int sliceNum){
		String xyData = sliceNum +tab+ xyaData[0][X_CENTER] +tab+ xyaData[0][Y_CENTER] +tab+
		                   (xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA]));

		return xyData;
	}

	public int getArmCounter(){
		return armHistory.size();
	}

	public int getLatestVisitedArm(){
		if(armHistory.size()==0){
			return 0;
		}

		return armHistory.get(armHistory.size()-1);
	}

	public String getLatestEpisode(){
		if(episode.size()!=0&&episode.size() == armHistory.size())
		    return episode.get(episode.size()-1).getString();
		else
			return "";
	}

	public void endProcess(){
		final int CENTER = RMConstants.ROI_NUM;
		final int currentPosition = previousPosition;
		if(currentPosition>0 && currentPosition!= CENTER){
			currentRMROI = RMROIstates.get(currentPosition-1);
			currentRMROI.notifyExit();
		}
	}

	@Override
	public String[] getResults(){
		String[] results = new String[13];

		StringBuilder selectedMode = new StringBuilder();
		if(RMConstants.isReferenceMemoryMode()){
			selectedMode.append("Reference");
		}else{
			selectedMode.append("Working");
		}
		if(NSense){
			selectedMode.append("-Nsense");
		}else{
			selectedMode.append("-Normal");
		}

		final int totalRevisiting = doubleError+workingMemoryError+intakeAfterOmission+omissionAfterOmission;
		final int totalOmissionError = omission+omissionAfterOmission;
		final int totalArmChoiseNum = armHistory.size();
		final int totalFoodIntake = intake+intakeAfterOmission;

		results[0]  = selectedMode.toString();
		results[1]  = ""+ (Math.round((totalSlice/rate)*10)/10.0);
		results[2]  = ""+ (Math.round(totalDistance*10.0)/10.0);
		results[3]  = ""+ (workingMemoryError+doubleError);
		results[4]  = ""+ intakeAfterOmission;
		results[5]  = ""+ omissionAfterOmission;
		results[6]  = ""+ totalRevisiting;
		results[7]  = ""+ totalOmissionError;
		results[8]  = ""+ totalArmChoiseNum;
		results[9]  = ""+ totalFoodIntake;
		results[10] = ""+ getDifferentArmNoInFirst8();
		results[11] = ""+ (referenceMemoryError+doubleError);
		results[12] = ""+ doubleError;

		return results;
	}

	private int getDifferentArmNoInFirst8(){
		int result = 0;
		String temp = "";

		for(int i=0;i<8;i++){
			if(i>armHistory.size()-1)
				break;
			String str = armHistory.get(i).toString();
			if(temp.indexOf(str)== -1){
				result++;
			}
			temp += str;
		}
		return result;
	}

	public List<Integer> getArmHistory(){
		return armHistory;
	}

	public List<StateConstants> getEpisode(){
		return episode;
	}

	@Override
	public String[] getBinResult(int option){return null;}
	@Override
	public void nextBin(){}
}