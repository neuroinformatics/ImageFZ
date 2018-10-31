package behavior.image.process;

import java.util.Arrays;

import ij.process.*;

import behavior.image.process.OnuParticleAnalyze;
import behavior.image.process.OnuThresholder;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

/*********
�摜�����𓝍��I�ɏ�������N���X�B���̃N���X����đS�Ẳ摜�������s�����Ƃ��o����B
�����́A��P�[�W���B�i�ꊇ���ĕ����̃P�[�W���������Ƃ͂ł��Ȃ��j
 **********/
public class ImageManager{
	/**�������ׂ��摜���w�肷��t�B�[���h�A�T�u�g���N�g�i�����Z�����j�摜*/
	public static final int SUBTRACT_IMG = 1;
	/**�������ׂ��摜���w�肷��t�B�[���h�AXOR�摜*/
	public static final int XOR_IMG = 2;
	/**�������ׂ��摜���w�肷��t�B�[���h�A�g���[�X�摜*/
	public static final int TRACE_IMG = 3;

	private ImageProcessor backIp, currentIp;
	private ImageProcessor subtractIp, xorIp, previousIp = null;

	//private Program program;//Water_Maze�p�ɒǉ� Subtract���̋L�q��ύX�����B

	private final OrgParticleAnalyze org = new OrgParticleAnalyze(); // Org ���ɑO��̉摜�̏�񂪊i�[���Ă���̂ŁA���������Ă͂Ȃ�Ȃ��B

	/*******
	�R���X�g���N�^�B
	 *@param backIp �o�b�N�O���E���h�摜�B
	 ********/
	public ImageManager(ImageProcessor backIp){
		this.backIp = backIp;
	}

	/******
	 * �o�b�N�O���E���h�摜��getter
	 */	
	public ImageProcessor getBackIp(){
		return backIp;
	}

	/*******
	�擾���ꂽ���݂̉摜���Z�b�g����B�摜�� Roi �Ő؂������Ƃ̂��́B
	 *@param currentIp roi�ŃJ�b�g������P�[�W���̉摜
	 ********/
	public void setCurrentIp(ImageProcessor currentIp){
		this.currentIp = currentIp;
	}

	public void setProgram(Program program){
		//this.program = program;
	}

	/*******
	���݂̉摜����o�b�N�O���E���h�������Z����BinvertMode �̂Ƃ��͂��̋t�B
	 ********/
	public void subtract(){
		//if(program == Program.WM){
			//subtractIp = currentIp;
		//}else
		if(Parameter.getBoolean(Parameter.invertMode)){
			subtractIp = new ImageConverter().copyBits(backIp,currentIp, 0, 0, ImageConverter.Mode.SUBTRACT);
		}else{
			subtractIp = new ImageConverter().copyBits(currentIp,backIp, 0, 0, ImageConverter.Mode.SUBTRACT);
		}
	}

	/*******
	�摜�� threshold ��������i�����ɂ���j�B
	 *@param type �摜�w��t�B�[���h
	 ********/
	public void applyThreshold(int type){
		applyThreshold(type, Parameter.getInt(Parameter.minThreshold), Parameter.getInt(Parameter.maxThreshold));
	}

	/*******
	�摜�� threshold ��������i�����ɂ���j�B
	 *@param minThres ����臒l
	 *@param maxThres ���臒l�B
	 ********/
	public void applyThreshold(int type, int minThres, int maxThres){
		ImageProcessor thresholdIp = getIp(type);
		OnuThresholder ot = new OnuThresholder(minThres, maxThres);
		ot.applyThreshold(thresholdIp);
	}

	/*******
	�摜�ɓ��t������B
	 *@param type �摜�w��t�B�[���h
	 ********/
	public void dilate(int type){
		ImageProcessor dilateIp = getIp(type);
		dilateIp.dilate();
	}

	/*******
	�p�[�e�B�N���̒[�����
	 *@param type �摜�w��t�B�[���h
	 ********/
	public void erode(int type){
		ImageProcessor erodeIp = getIp(type);
		erodeIp.erode();
	}

	/*******
	�摜�̏d�S�A�ʐςȂǂ���͂���B
	 *@param plot �O��̉�͌��ʁi��ԑ傫���p�[�e�B�N���̂݁j�B�ŏ��� null �ł悢�B
	 *@param type �摜�w��t�B�[���h
	 *@return int[�p�[�e�B�N����][�f�[�^�̎��]�B�f�[�^�� 0�c x���W 1�c y���W 2�c �ʐ� 3�c �p�[�e�B�N����
	 ********/
	public synchronized int[][] analyzeParticle(int type, int[] plot){
		ImageProcessor analyzeIp = getIp(type);
		if(plot == null){
			plot = new int[4];
			Arrays.fill(plot, 0);
		}
		OnuParticleAnalyze opa = new OnuParticleAnalyze(plot);
		
		if(type == SUBTRACT_IMG)
			opa.setParameter(Parameter.getInt(Parameter.minSize), Parameter.getInt(Parameter.maxSize));
		else if(type == XOR_IMG)
			opa.setParameter(0, 9999);
		analyzeIp.autoThreshold();
		
		return opa.analyzeParticle(analyzeIp);
	}
	

	/**analyzeParticle �Ƃ�邱�Ƃ͈ꏏ�B�g�p����A���S���Y�����Ⴄ�BanalyzeParticle �ŃG���[���o��ꍇ�͂�����𐄏�
	 */
	public synchronized int[][] analyzeParticleOrg(int type, int[] plot){
		ImageProcessor analyzeIp = getIp(type);
		if(type == SUBTRACT_IMG)
			org.setParameter(Parameter.getInt(Parameter.minSize), Parameter.getInt(Parameter.maxSize));
		else if(type == XOR_IMG)
			org.setParameter(0, 999999);
		//analyzeIp.autoThreshold();
		return org.analyzeParticle(analyzeIp);
	}


	/*******
	XOR(�O��̉摜�Ƃ̑���n��h��Ԃ��j�摜���쐬����B
	 *@param xorThreshold xor �摜���쐬����ۂ� threshold. subtract �摜���g���ꍇ�͊֌W�Ȃ�
	 *@param subtractBackground XOR�����̑I���B���݂̉摜�����̂܂� XOR ���邩�Asubtract�摜���g�p���邩�B
	 ********/
	public void xorImage(int xorThreshold, boolean subtractBackground){
		if(previousIp == null)
			previousIp = backIp.duplicate();

		if(subtractBackground){
			xorIp = new ImageConverter().copyBits(previousIp,subtractIp, 0, 0, ImageConverter.Mode.XOR);
			previousIp = subtractIp;
		}else{
			ImageProcessor currentTempIp = currentIp.duplicate();
			OnuThresholder ot = new OnuThresholder(xorThreshold, Parameter.getInt(Parameter.maxThreshold));
			ot.applyThreshold(currentTempIp);
			xorIp = new ImageConverter().copyBits(previousIp,currentTempIp, 0, 0, ImageConverter.Mode.XOR);
			previousIp = currentTempIp;
		}
	}


	/*******
	�摜�̃m�C�Y������
	 *@param type �摜�w��t�B�[���h
	 ********/
	public void reduceNoise(int type){
		ImageProcessor ip = getIp(type);
		ip.medianFilter();
	}

	/*******
	�摜���擾����B
	 *@param type �摜�w��t�B�[���h
	 ********/
	public ImageProcessor getIp(int type){
		switch(type){
		case SUBTRACT_IMG:	return subtractIp;
		case XOR_IMG:		return xorIp;
		default:	throw new IllegalArgumentException("no such image(" + type + ")");
		}
	}
}