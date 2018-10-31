package behavior.plugin.analyzer;

import java.awt.Rectangle;
import java.util.*;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

import behavior.image.process.EPTracer;
import behavior.image.process.ImageManager;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.EPParameter;

/**
 * Elevated Plus Maze�p�̉�͂��s���B
 * 
 * �o�͂���f�[�^�͈ȉ���9��ށB
 * TotalDistance         -�}�E�X���S�̂ňړ���������
 * TotalCenterTime       -�S�̂ł̃}�E�X�������ɂ�������
 * StayTime(North,South,West,East,NoWhere) -���ꂼ��̏ꏊ�Ƀ}�E�X����������
 * Entries(North,South,West,East)  -���ꂼ��̏ꏊ�ւ̈ړ���
 * OpenArmLocation �@-�ǂ̂Ȃ��A�[���̈ʒu
 * 
 * �i�ȉ��̃f�[�^��bin���Ƃɏo��)
 * binDistance  -�}�E�X�̈ړ�����
 * CenterTime  -���S�ɂ�������
 * binTime(Open,Close) -�ǂ̂Ȃ��ꏊ�܂��͂���ꏊ�ɂ�������
 * binNE(Open,Close) -��
 * 
 * @author Butoh
 */
public final class EPAnalyzer extends Analyzer{
	/*getBinResult(int) ���\�b�h�ŕԋp����f�[�^���w�肷�邽�߂̃t�B�[���h*/
	public static final int BIN_DISTANCE = 0;
	public static final int CENTER_TIME = 1;
	public static final int BIN_TIME_OPEN = 2;
	public static final int BIN_TIME_CLOSE = 3;
	public static final int BIN_NE_OPEN = 4;
	public static final int BIN_NE_CLOSE = 5;

	private static final int NORTH = 0;
	private static final int SOUTH = 1;
	private static final int WEST = 2;
	private static final int EAST = 3;
	private static final int NOWHERE = 4;
	private static final int CENTER = 5;

	private static final int OPEN = 0;
	private static final int CLOSE = 1;

	protected double startDelay = 0;

	//�g�p����p�����[�^�[
	private final int rate = Parameter.getInt(EPParameter.rate);
	private final int duration = Parameter.getInt(EPParameter.duration);
	private final boolean openArmLocation = Parameter.getBoolean(EPParameter.openArmLocation);
	//Center��MainArea�ɑ΂��鑊�΍��W
	private final int topCenter;
	private final int lowCenter;
	private final int rightmostCenter;
	private final int leftmostCenter;
	//��R�}���Ƃ̎���
	private final double timePerSlice = 1.0/rate;

	//��͂Ɏg�p���鐔�l
	protected int[][] xyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData;
	protected int moveEpisodeNum = 0, moveFlag = 0;
	protected double[] _BinDistance, binCenterTime, areaResult;

	private double[] stayTime; //�ꏊ���Ƃ̑؍ݎ���
	protected int[] entries;   //�ꏊ���Ƃ̑؍݉�
	protected int[][] binEntries; //bin���Ƃ̕ǂ̂���ꏊ�ƂȂ��ꏊ�ɂ�����
	private double[][] binTime; //bin ���Ƃ̓���̈ʒu�ɂ���p�[�Z���e�[�W�B�ʒu�� OPEN �� CLOSE
	private int prePosition = CENTER;//�O�̉摜�ł̃}�E�X�̈ʒu�A(�ύX)

	protected boolean start = false;
	
	private final String tab = "\t";

	public EPAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate()); //�Ƃ肠��������
		tracer = new EPTracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());

		//Center�̍��W���擾
		String roiName2 = FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "center1.roi";
		Roi centerRoi = null;
		try{
		    centerRoi = new RoiDecoder(roiName2).getRoi();
		}catch(Exception e){
			e.printStackTrace();
		}
		Rectangle centerRec = centerRoi.getBounds();
		topCenter = centerRec.y;
		lowCenter = centerRec.y+centerRec.height;
		rightmostCenter = centerRec.x+centerRec.width;
		leftmostCenter = centerRec.x;
		
	}

	protected void initializeArrays(){
		_BinDistance = new double[binLength];
		Arrays.fill(_BinDistance, 0.0);
		binCenterTime  = new double[binLength];
		Arrays.fill(binCenterTime, 0.0);
		stayTime = new double[5];
		Arrays.fill(stayTime, 0);
		entries = new int[4];
		Arrays.fill(entries, 0);
		binTime = new double[2][binLength];
		for(int num = 0; num < binTime.length; num++)
			Arrays.fill(binTime[num], 0);
		binEntries = new int[2][binLength];
		for(int num = 0; num < binEntries.length; num++)
			Arrays.fill(binEntries[num], 0);
	}

	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//�O��̐��l���c���Ă���
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	public void calculate(final int currentSlice){
		if(currentSlice > 0){

		    currentBinSlice++;
		    /*bin���Ƃ̋����������Ă���*/
		    double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					                        prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
		    _BinDistance[currentBin] += currentDistance;

		    /*�}�E�X��Center�ɂ��邩�ǂ�������͂��ACenter�ɂ���Ύ��Ԃ�������B*/
		    if(CheckArm(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]) == CENTER)
		        binCenterTime[currentBin] += timePerSlice;

		    /*�}�E�X�̈ʒu������͂���*/
		    final int position = CheckArm(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

		    //�}�E�X�̏ꏊ���Ƃ̑؍ݎ���
		    if(position!=CENTER)
			    stayTime[position] += timePerSlice;

		    //�}�E�X�̏ꏊ���O��ƈႤ�ꍇ�A�}�E�X�̑؍݉񐔂��J�E���g���Ă���
            if(position!=prePosition){
          	    //�}�E�X�̏ꏊ���Ƃ̑؍݉�
                if(position!=CENTER && position!=NOWHERE){
           	        entries[position]++;
                    if(openArmLocation){
                        if(position==EAST || position==WEST){
           	                binEntries[OPEN][currentBin]++;
                        }else{
           	                binEntries[CLOSE][currentBin]++;
                        }
                    }else{
                        if(position==NORTH || position==SOUTH){
                            binEntries[OPEN][currentBin]++;
                        }else{
                            binEntries[CLOSE][currentBin]++;
                        }
                    }
                }
            }

            //�}�E�X���ǂ̂Ȃ��ꏊ�ɂ����񐔂ƕǂ̂���ꏊ�ɂ�����
            //�ǂ̂���A�[���̈ʒu�ɂ���ĈقȂ�
            //�������������ł��Ȃ����B
            if(position!=CENTER && position!=NOWHERE){
                if(openArmLocation){
                    if(position==EAST || position==WEST){
           	            binTime[OPEN][currentBin] += timePerSlice;
                    }else{
            	        binTime[CLOSE][currentBin] += timePerSlice;
                    }
                }else{
                    if(position==NORTH || position==SOUTH){
                        binTime[OPEN][currentBin] += timePerSlice;
                    }else{
            	        binTime[CLOSE][currentBin] += timePerSlice;
                    }
                }
            }

            prePosition = position;

		    //trace��`��
		    tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
	    tracer.setPrevXY(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}

	//�}�E�X�̈ʒu����͂��A�ʒu����Ԃ�
	private int CheckArm(final int x,final int y){
		int EPposition = NOWHERE;

		if (y < topCenter && (x< rightmostCenter && x > leftmostCenter)){	//���S�̏�ŁAwidth �͒��S�͈�
			EPposition = NORTH;
		}else if (y>lowCenter && (x<rightmostCenter && x>leftmostCenter)){	//���S�̉��ŁAwidth �͒��S�͈�
			EPposition = SOUTH;
		}else if (x<leftmostCenter && (y > topCenter && y<lowCenter)) {	//���S�̍��ŁAheight �͒��S�͈�
			EPposition = WEST;
		}else if (x>rightmostCenter && (y > topCenter && y < lowCenter) ) {	//���S�̉E�ŁAheight �͒��S�͈�
			EPposition = EAST;
		}else if ((x <= rightmostCenter && x >= leftmostCenter) && (y >= topCenter && y <= lowCenter) ) {	//���S�͈�
			EPposition = CENTER;
		}

		return EPposition;
	}

	//����bin�Ɉړ�
	public void nextBin(){
		if(currentBin<binLength-1){
		    currentBin++;
		    currentBinSlice = 0;
		}
	}

	//SubtractImage��Ԃ�
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

    //Information�E�B���h�E�̏��
	public String getInfo(final String subjectID,final int sliceNum){
		String info = subjectID +tab+ getElapsedTime(sliceNum) +tab+ stayTime[NORTH] +tab+ stayTime[SOUTH]+
		                   tab+ stayTime[WEST] +tab+ stayTime[EAST] +tab+ stayTime[NOWHERE];
		return info;
	}

	//XY�E�B���h�E�̏��
	public String getXY(final int sliceNum){
		String xyData = sliceNum +tab+ xyaData[0][X_CENTER] +tab+ xyaData[0][Y_CENTER] +tab+
		                   (xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA])) 
		                        +tab+ getPosition(prePosition);

		return xyData;
	}

	private char getPosition(int position){
		switch(position){
		case CENTER: return 'C';
		case NORTH: return 'N';
		case SOUTH: return 'S';
		case WEST: return 'W';
		case EAST: return 'E';
		case NOWHERE: return ' ';
		}
		return 'X';
	}
	//�����͐��������삷�邩�܂��m�F���Ă��Ȃ��B
	public void interruptAnalysis(){
		if(currentBin == 0 && currentBinSlice == 0)
			state = NOTHING;
		else if(currentBin == binLength)
			state = COMPLETE;
		else if(currentBinSlice == 0){
			state = ONE_BIN_END;
			binLength = currentBin;
		}
		else{
			state = INTERRUPT;
			binLength = currentBin + 1;
		}
	}

	//Total�̌��ʂ��o��
	public String[] getResults(){
		//TotalDistance(cm)
		double totalDistance = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalDistance += _BinDistance[bin];
		//totalCenterTime(sec)
		double totalCenterTime = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalCenterTime += binCenterTime[bin];
		//PercentageOpenArmStayTime(%)
		double percentageOpenArmStayTime = 0;
		if(openArmLocation){
			percentageOpenArmStayTime = ((stayTime[WEST]+stayTime[EAST])/(duration-stayTime[NOWHERE]))
			                          *100;
		}else{
			percentageOpenArmStayTime = ((stayTime[NORTH]+stayTime[SOUTH])/(duration-stayTime[NOWHERE]))
                                      *100;
		}
		//TotalEntries
		int totalEntries = 0;
		for(int i=0;i<4;i++)
			totalEntries += entries[i];
        //PercentageOpenArmEntries(%)
		double percentageOpenArmEntries;
		if(openArmLocation){
			percentageOpenArmEntries = ((entries[WEST]+ entries[EAST])/(double)totalEntries)*100;
		}else{
			percentageOpenArmEntries = ((entries[NORTH]+ entries[SOUTH])/(double)totalEntries)*100;
		}

		String[] results = new String[15];
		//�Z�b�g
		results[0] =  ""+ Math.round(totalDistance * 10.0) / 10.0;	//��͑S�̂œ���������(cm)
		results[1] =  ""+ Math.round(totalCenterTime * 10.0) / 10.0; //��͑S�̂Œ��S�ɂ�������(sec)
		results[2] =  ""+ Math.round(stayTime[NORTH] * 10.0) / 10.0;	//*10/10 �̑���́A�����_���܂Ō��ʂƂ��ďo������
		results[3] =  ""+ Math.round(stayTime[SOUTH] * 10.0) / 10.0;
		results[4] =  ""+ Math.round(stayTime[WEST] * 10.0) / 10.0;
		results[5] =  ""+ Math.round(stayTime[EAST] * 10.0) / 10.0;
		results[6] =  ""+ Math.round(stayTime[NOWHERE] * 10.0) / 10.0;
		results[7] =  ""+ Math.round(percentageOpenArmStayTime * 10.0) / 10.0;
		results[8] =  ""+ entries[NORTH];
		results[9] =  ""+ entries[SOUTH];
		results[10] = ""+ entries[WEST];
		results[11] = ""+ entries[EAST];
		results[12] = ""+ totalEntries;
		results[13] = ""+ Math.round(percentageOpenArmEntries * 10.0) / 10.0;
		results[14] = Parameter.getBoolean(EPParameter.openArmLocation) ?"East/West":"North/South";
		return results;
	}

	//bin���Ƃ̌��ʂ��o��
	public String[] getBinResult(final int option){
		int length = binLength;
		String[] result = new String[length];
		for(int num = 0; num < length; num++){
			switch(option){
			  case BIN_DISTANCE: result[num]   = ""+ Math.round(_BinDistance[num]*10.0)/10.0; break;
			  case CENTER_TIME: result[num]    = ""+ Math.round(binCenterTime[num]*10.0)/10.0; break;
			  case BIN_TIME_OPEN: result[num]  = ""+ Math.round(binTime[OPEN][num]*10.0)/10.0; break;
			  case BIN_TIME_CLOSE: result[num] = ""+ Math.round(binTime[CLOSE][num]*10.0)/10.0; break;
			  case BIN_NE_OPEN: result[num]    = ""+ binEntries[OPEN][num]; break;
			  case BIN_NE_CLOSE: result[num]   = ""+ binEntries[CLOSE][num]; break;
			  default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}
		if(state == INTERRUPT)
			result[length - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}
}