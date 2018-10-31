package behavior.gui;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import ij.*;
import ij.process.ImageProcessor;
import ij.text.*;

import behavior.setup.Header;
import behavior.setup.Program;

/**************
 *�摜�̕\���ʒu�̐ݒ�A�摜�̕\���A��\�����������ۃN���X�B
 *���ۂɎg�p����ۂ́A�T�u�N���X�ł͂Ȃ��A���̃N���X��錾���Astatic getInstance(int, int) ���\�b�h
 *��p���邱�ƂŁA�P�[�W���ɂ��킹�ĕK�v�ȃT�u�N���X���擾����B
 ***************/
/**
 * 
 * @author Modifier:Butoh
 */
public abstract class WindowOperator{
	/********************
	��������������			��������
	��  a  ��			��������
	��������������         ��������
	�������������� �������������� ��c����
	��     �� ��     �� ��������
	��  b1 �� ��  b2 �� ��������
	��     �� ��     �� ��������
	��     �� ��     �� ��������
	�������������� ��������������
	���������������@��������������
	��     ���@���@�@�@�@��
	��     ���@��     ��
	��  b3 ���@��  b4 ��
	��     ���@��     ��
	���������������@��������������
	a = info �E�B���h�E�B�S�P�[�W�̏����L�ڂ���B
	b = �e�C���[�W�Q�B��̘g�̒��Ɋe�P�[�W���ꂼ��̉摜���\�������B�Ⴆ�� b1 �͌��݂̉摜�Ab2 �̓T�u�g���N�g
�@�@�@	�@�@�����摜�A�Ƃ����ӂ��ɖ������Ƃɔz�u�����B
	c = XYdata �E�B���h�E�B���ꂼ��̃E�B���h�E���e�P�[�W�̏���\������
	 *********************/

	/**�摜�̔z�u�ꏊ���w�肷�邽�߂̃t�B�[���h�B����*/
	public static final int LEFT_UP = 1;	//b1
	/**�摜�̔z�u�ꏊ���w�肷�邽�߂̃t�B�[���h�B����*/
	public static final int LEFT_DOWN = 2;	//b3
	/**�摜�̔z�u�ꏊ���w�肷�邽�߂̃t�B�[���h�B�E��*/
	public static final int RIGHT_UP = 3;	//b2
	/**�摜�̔z�u�ꏊ���w�肷�邽�߂̃t�B�[���h�B�E��*/
	public static final int RIGHT_DOWN = 4;	//b4

	protected final int WIDTH = 0;
	protected final int HEIGHT = 1;

	protected final int INFO_MIN_HEIGHT = 104;	//info �E�B���h�E�̍ŏ��l(�c�j�B����ɁA���͂̕K�v�s������INFO_LINE_HEIGHT ��������΂悢
	protected final int INFO_LINE_HEIGHT = 34;	//info �E�B���h�E�ŕ\������镶�͂̈�s�̏c�����̃T�C�Y(pixel)
	protected final int XY_WIN_X = 800;  //XY�E�B���h�E�̉������̍��W�̍ŏ��l
	protected int BLANK = 30; //blank �̒l(���������������ƕ����l�߂������̔z�u�ɂȂ�)

	protected int allCage;
	protected ImageProcessor[] formIp;  //�e�摜�̑傫�������擾���邽�� backIp ���Q�Ƃ�����B�ύX���Ă͂Ȃ�Ȃ��B
	protected int[] displaySize = {1500, 1000};
	protected int[] initInfoWin = {0, 0};	//info �E�B���h�E�̈ʒu�i����[)
	protected int[] infoWinSize = {700, 0};
	protected int[] initXYWin = new int[2];
	protected int[] xyWinSize = {270, 130};
	protected int[] armWinsize = {270, 120}; //RM�p�A���݃}�E�X������A�[���̔ԍ��Ə�Ԃ�\��
	protected int[][] initImageWin = new int[2][2];	//�e�C���[�W�Q�̈ʒu(����[�̉摜�icage = 0 �̉摜�j�̍���[�j
	
	private TextWindow infoWin, totalResWin;
	private TextWindow[] xyWin;
	private TextWindow armWin;

	protected String[] infoTexts;

	protected WindowOperator(int allCage, ImageProcessor[] backIp){
		this.allCage = allCage;
		formIp = backIp;
		infoTexts = new String[allCage];
		Arrays.fill(infoTexts, "");
		setDisplaySize();
	}

	/******
	�C���X�^���X�擾�B�����I�ɃT�u�N���X���w�肵�Ă����B
	 *@param allCage �P�[�W��
	 *@param backIp �o�b�N�O���E���h�摜
	 *******/
	public static WindowOperator getInstance(int allCage, ImageProcessor[] backIp){
		if(allCage <= 2)
			return new Window2x1Operator(allCage, backIp);
		else if(allCage <= 4)
			return new Window2x2Operator(allCage, backIp);
		else if(allCage <= 6)
			return new Window3x2Operator(allCage, backIp);
		else if(allCage <= 9)
			return new Window3x3Operator(allCage, backIp);
		else
			throw new IllegalArgumentException("allCage must be 1 ~ 9");
	}

	private void setDisplaySize(){
		File infoFile = new File("display.txt");
		if(!infoFile.exists())
			return;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(infoFile));
			displaySize[0] = Integer.parseInt(reader.readLine());
			displaySize[1] = Integer.parseInt(reader.readLine());
			reader.close();
		}catch(Exception e){
			IJ.error(String.valueOf(e));
		}
	}

	protected int max(int a, int b, int c){
		return Math.max(Math.max(a, b), c);
	}

	protected int min(int a, int b, int c){
		return Math.min(Math.min(a, b), c);
	}

	/******
	�摜�Q�i�S�P�[�W�łЂƂ܂Ƃ܂�j��\�����Đ��񂷂�B
	����ʒu�̌v�Z��
		�e�E�B���h�E�̔z�u�ꏊ���A�P�[�W���ɍ��킹�Đݒ肷��B
		�ݒ肷�ׂ��z�u�́Ainfo �E�B���h�E�̑傫���i�c�����j�AXYdata �E�B���h�E�̏c�����A�e�C���[�W�Q�̏c�����A
		�C���[�W�Q���̉摜�̏c�����B
			�A���S���Y���ɂ��Đ����B�܂��c���ɕ��� allCage ���̉摜�ɂ��āA�����s�A��ōł� height, width ���傫������
			������o���B���̒l�v���X blank ���A���̍s�A��̉摜�̐擪�ʒu�ƂȂ�B�Ⴆ�΁A1�s�ځicageNo = 1, 2, 3)�� heigth ��
			��ԑ傫���̂��A2 �� 100 �s�N�Z���ł���Ƃ��Ablank �� 50 �s�N�Z���ł���Ƃ���ƁA2�s�ځicageNO = 4, 5, 6)��
			��[�̈ʒu�͑S�Ẳ摜�ŁA150 �ƂȂ�B
	�摜�̍X�V�́A�e���A�\������ ImagePlus �ɁA�V���� ImageProcessor �� setProcessor ���邱�Ƃł���Ă��炤�B
	 *@param allocation �\���ʒu�̃t�B�[���h����
	 *******/
	public abstract void setImageWindow(ImagePlus[] imp, int allocation);

	public abstract void setImageWindow(ImagePlus[] imp, int allocation,boolean[] activeCage);

	protected int[] getImageAllocation(int allocation){
		int[] init = new int[2];
		switch(allocation){
		case LEFT_UP: 	 init[WIDTH] = initImageWin[WIDTH][0]; init[HEIGHT] = initImageWin[HEIGHT][0]; break;
		case LEFT_DOWN:  init[WIDTH] = initImageWin[WIDTH][0]; init[HEIGHT] = initImageWin[HEIGHT][1]; break;
		case RIGHT_UP:	 init[WIDTH] = initImageWin[WIDTH][1]; init[HEIGHT] = initImageWin[HEIGHT][0]; break;
		case RIGHT_DOWN: init[WIDTH] = initImageWin[WIDTH][1]; init[HEIGHT] = initImageWin[HEIGHT][1]; break;
		}
		return init;
	}

	/******
	 *info �E�B���h�E��\��
	 *@param program �v���O�����ԍ�
	 *******/
	public void setInfoWindow(Program program){
        infoWin = new TextWindow("information", Header.getInfoHeader(program), "", infoWinSize[WIDTH], infoWinSize[HEIGHT]);
		infoWin.setLocation(initInfoWin[0], initInfoWin[1]);
		openWait();
	}

	/**�e�L�X�g�̍X�V*/
	public synchronized void setInfoText(String text,int cage){
		infoTexts[cage] = text;
		StringBuilder textAll = new StringBuilder();
		for(int i = 0; i < infoTexts.length; i++)
			textAll.append(infoTexts[i] + "\n");

		TextPanel infoTp = infoWin.getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
		infoTp.append(textAll.toString());
	}

	/******
	XY �f�[�^�E�B���h�E��\��
	 *******/
	public void setXYWindow(Program program,boolean[] activeCage){
		xyWin = new TextWindow[allCage];
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage]) continue;
            xyWin[cage] = new TextWindow("XY-Data" + (cage + 1), Header.getXYHeader(program), "" , xyWinSize[WIDTH], xyWinSize[HEIGHT]);
			xyWin[cage].setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT] * cage);
			openWait();
		}
	}

	public void setXYWindow(Program program){
		xyWin = new TextWindow[allCage];
		for(int cage = 0; cage < allCage; cage++){
            xyWin[cage] = new TextWindow("XY-Data" + (cage + 1), Header.getXYHeader(program), "" , xyWinSize[WIDTH], xyWinSize[HEIGHT]);
			xyWin[cage].setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT] * cage);
			openWait();
		}
	}
	/******
	XY �f�[�^�E�B���h�E�̃e�L�X�g���X�V�B
	 *@param cage �e�L�X�g���X�V����P�[�W
	 *******/
	public synchronized void setXYText(int cage, String text){
		xyWin[cage].append(text);
	}

	/**
	 * XY�f�[�^�E�B���h�E�̃f�[�^������������B
	 * @param cage ����������P�[�W
	 */
	public void clearXYText(int cage){
		TextPanel infoTp = xyWin[cage].getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
	}

	/******
	XY �f�[�^�E�B���h�E����e�L�X�g�p�l�����擾����B�i���e��ۑ�����ۂɕK�v�ƂȂ�B�j
	 *@param cage �e�L�X�g�p�l�����擾����P�[�W
	 *******/
	public TextPanel getXYTextPanel(int cage){
		return xyWin[cage].getTextPanel();
	}

	/**
	 * RM�p�A���݃}�E�X������A�[���̔ԍ��Ə�Ԃ�\��������B
	 * @param program�@�v���O�����ԍ�
	 */
	public void setRMArmWindow(Program program){
		if(program != Program.RM)
			return;
		armWin = new TextWindow("VisitedArmNumber", "Counter" + "\t" + "ArmNumber" + "\t" + "Episode","", armWinsize[WIDTH],armWinsize[HEIGHT]);
		armWin.setLocation(initXYWin[WIDTH], initXYWin[HEIGHT] + xyWinSize[HEIGHT]*3);
		openWait();
	}

	public synchronized void setRMArmText(final String text){
		armWin.append(text);
	}

	public synchronized void setAllRMArmText(final String text){
		TextPanel infoTp = armWin.getTextPanel();
		infoTp.selectAll();
		infoTp.clearSelection();
		armWin.append(text);
	}

	/******
	���C���f�[�^��\���B
	 *@param cageNum �P�[�W��
	 *@param subID �T�u�W�F�N�gID
	 *@param totalResult ���ʁBtotalResult[�P�[�W��][���ʓ��e]
	 *******/
	 public void showOnlineTotalResult(Program program, int cageNum, String[] subID, String[][] totalResult){
		 totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		 totalResWin.setLocation(0, 600);
		 StringBuilder line = new StringBuilder();
		 for(int cage = 0; cage < cageNum; cage++){
			 line.append(subID[cage]);
			 for(int num = 0; num < totalResult[cage].length; num++)
				 line.append("\t" + totalResult[cage][num]);
			 line.append("\n");
		 }
		 totalResWin.append(line.toString());
	 }
	 
	//�g�p����Cage��I����������ł͂�������g���B
	 public void showOnlineTotalResult(Program program, int allCage, boolean[] activeCage, String[] subID, String[][] totalResult) {
		 totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		 totalResWin.setLocation(0, 600);
		 for (int cage = 0; cage < allCage; cage++) {
			 if (activeCage[cage]) {
				 StringBuffer line = new StringBuffer(subID[cage]);
				 for(int num = 0; num < totalResult[cage].length; num++)
					 line.append("\t" + totalResult[cage][num]);
				 line.append("\n");
				 totalResWin.append(line.toString());
			 }
		 }
	}

	public void showOfflineTotalResult(Program program,ArrayList<String[]> results){
		totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 200);
		totalResWin.setLocation(0, 600);
		StringBuilder line = new StringBuilder();
		for(String[] resultLine:results){
			line.append(resultLine[0]);
			for(int i=1;i<resultLine.length;i++){
				line.append("\t"+resultLine[i]);
			}
		    line.append("\n");
	    }

		totalResWin.append(line.toString());
	}

	//BM��p
	public void showBMTotalResult(Program program, int cageNum, String[] subID, String[][] totalResult){
		totalResWin = new TextWindow("Results - TOTAL", Header.getTotalResultHeader(program), "", 1000, 150);
		totalResWin.setLocation(0, 700);
		for(int cage = 0; cage < cageNum; cage++){
			StringBuffer line = new StringBuffer(subID[cage]);
			for(int num = 0; num < totalResult[cage].length; num++)
				line.append("\t" + totalResult[cage][num]);
			line.append("\n");
			totalResWin.append(line.toString());
		}
	}
	
	//BM��p
	public void showProbeResult(Program program, String[] subID, String[] probeResult){
		String header = "SubjectID" + "\t" + "Target" + "\t" + "N1" + "\t" + "N2" + "\t" + "N3" + "\t" + "N4" + "\t" + "N5" + "\t" + "N6" + "\t" + "N7" + "\t" + "N8" + "\t" + "N9" + "\t" + "N10" + "\t" + "N11" + "\t" + "N12";
		totalResWin = new TextWindow("Results - PROBE", header, "", 650, 150);
		totalResWin.setLocation(0, 860);
	
		StringBuffer line = new StringBuffer(subID[0]);
		for(int num = 0; num < probeResult.length; num++)
			line.append("\t" + probeResult[num]);
		line.append("\n");
		totalResWin.append(line.toString());
	}

	/*****
	 * �S�Ă̕\������Ă���E�B���h�E���\���ɂ���B
	 ******/
	public void closeWindows(){
		WindowManager.closeAllWindows();
	}

	/**
	 * Mac �ł̓E�C���h�E���J���̂Ɏ��Ԃ�v����B
	 */
	protected void openWait(){
		if(IJ.isMacOSX()){
			try{
				Thread.sleep(300);
			} catch(Exception e){
			}
		}
	}
}