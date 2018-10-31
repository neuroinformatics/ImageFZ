package behavior.gui.roi;

import java.awt.*;
import java.io.*;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;

import behavior.setup.Program;
import behavior.io.FileManager;

/**Roi �̎擾�A�Ǘ����s���N���X*/
/**
 * 
 * @author Modifier:Butoh
 */
public class RoiOperator{
	protected Program program;
	protected int allRoi;
	protected Roi[] roi;

	//LDRoiOperator�̂��߂ɕK�v
	public RoiOperator(){}

	/**@param program �v���O�����ԍ�(behavior.setup.Program����j
	 *@param allCage �P�[�W��*/
	public RoiOperator(Program program, int allCage){
		this.program = program;
		this.allRoi = allCage;
		roi = new Roi[allRoi];
	}

	/***********
	�e�P�[�W�� Roi �� load ���āA���g�̃����o�ɑ������
	@return ���s������ true
	 ************/
	public boolean loadRoi(){
		String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir);
		for(int num = 0; num < allRoi; num++){
			String roiFile = null;
            if(program == Program.BT || program == Program.RM || program == Program.CSI
					    || program == Program.YM){
				roiFile = path + File.separator + "MainArea.roi";
			}else if(program == Program.BM) {
				roiFile = path + File.separator + "Field.roi";
			}else{
				roiFile = path + File.separator + "Cage Field" + (num + 1) + ".roi";
			}

			try{
				roi[num] = readRoi(roiFile);
			}catch(IOException e){
				IJ.error("ROI(" + roiFile + ") cannot be found. Try to set ROIs or Copy ROIs from other folder.");
				return true;
			}
		}
		return false;
	}

	/***********
	 *�@�S�P�[�W�� ROI ���擾�����摜�ɕ`�悵�āA���� ROI �ł悢���ǂ������[�U�ɐq�˂�B
	 *�@�킴�킴 roiImp �������Ƃ��Ď擾���Ă���̂́A��ɕ\�����铮��Ƃ̘A������ۂi�`�F�b�N�摜�����̂܂ܓ���Ƃ��Ďg���j���߁B
	 *@param roiImp Roi �̃`�F�b�N�Ɏg�p���� ImagePlus
	 *@return �L�����Z���������ꂽ�� true
	 ************/
	public boolean checkRoi(ImagePlus roiImp){
		ImageProcessor roiIp = roiImp.getProcessor();//�摜�̃v���Z�b�T�擾
		roiIp = roiIp.convertToRGB();//RGB���[�h�ɕϊ�
		roiImp.setProcessor("Check ROI", roiIp);	//���̂� ip ���Z�b�g�������Ȃ��ƁAROI���\������Ȃ�
		roiIp.setColor(Color.red);//��ʂɏ������ޕ����̐F�w��
		roiImp.show();//�摜�\��
		for(int num = 0; num < allRoi; num++){
			String name = null;
			name = "Cage Field" + (num + 1);
			Polygon polygonRoi = roi[num].getPolygon();
			Rectangle outline = polygonRoi.getBounds();
			roiIp.drawString(name, outline.x, outline.y); //ROI �̖��O�̏�������
			roiIp.drawPolygon(polygonRoi); //ROI �{�̂̏�������
		}
		roiImp.updateAndDraw();//�摜�X�V

		if(!IJ.showMessageWithCancel("Field Check", "use these ROIs?\nPress Yes if ROIs are set properly.\nPress No to quit this plugin and reset ROIs.")){
			roiImp.hide();//�摜��\��
			return true;
		}
		return false;
	}

	/************
	���݂� Roi �� ImageProcessor �� crop(�J�b�g�j���A����ɂ��V���ɐ������ꂽ ImageProcessor ��ԋp����
	 *@param all �J�b�g�������摜
	 *@param cage CageNO
	 *@return �e Roi �ŃJ�b�g���ꂽ�摜
	 *************/
	public synchronized ImageProcessor split(ImageProcessor all,int cage){
		all.setRoi(roi[cage]);
		ImageProcessor splitIp = all.crop();

		return splitIp;
	}

	/************
	���݂� Roi �� ImageProcessor �� crop(�J�b�g�j���A����ɂ��V���ɐ������ꂽ ImageProcessor ��ԋp����
	 *@param all �J�b�g�������摜
	 *@return �e Roi �ŃJ�b�g���ꂽ�摜
	 *************/
	public synchronized ImageProcessor[] split(ImageProcessor all){
		ImageProcessor[] splitIp = new ImageProcessor[allRoi];
		for(int num = 0; num < allRoi; num++){
			all.setRoi(roi[num]);
			splitIp[num] = all.crop();
		}
		return splitIp;
	}

	/************
	�����o��Roi ��Ԃ�
	 *************/
	public Roi[] getRoi(){
		for(int num = 0; num < allRoi; num++)
			if(roi[num] == null)
				throw new IllegalStateException("roi is null");
		return roi;
	}

	/**
	 *�w�肵���t�H���_�Ɋ܂܂��ROI�t�@�C�����擾����B
	 */
	protected File[] getList(String path){
		File preference = new File(path);
		File[] roiFiles = preference.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith(".roi");//�t�@�C������".roi"�ŏI��邩�ǂ����Ńt�B���^�����O���Č��ʂ�Ԃ��B
			}
		});
		return roiFiles;
	}

	/**
	 �w�肵���t�@�C������Roi���擾����B
	 */
	protected Roi readRoi(String path) throws IOException{
		RoiDecoder decoder = new RoiDecoder(path);
		Roi roi = null;
		try{
			roi = decoder.getRoi();//path����ROI���擾
		}catch(IOException e){
			throw new IOException();
		}
		return roi;
	}
}