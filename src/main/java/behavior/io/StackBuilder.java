package behavior.io;

import ij.*;
import ij.gui.*;
import ij.process.*;

/**stack �̎g�p��⏕*/
public class StackBuilder{

	private int nIp;
	String[] name;
	ImagePlus[] imp;

	/**�R���X�g���N�^�̈������Ȃ��Ƃ��A1��ʁB����Ƃ��́A�w�肳�ꂽ�����������X�^�b�N���쐬����B*/
	public StackBuilder(){
		nIp = 1;
	}

	public StackBuilder(int nIp){
		this.nIp = nIp;
	}

	/**�����̂���R���X�g���N�^���g���āA������ʂ��쐬����Ƃ��͂�����B*/
	public ImagePlus[] buildStack(ImagePlus[] imp){
		ImagePlus[] simp = new ImagePlus[nIp];
		for(int i=0;i<nIp;i++){
			simp[i] = createStack(imp[i]);
		}
		return simp;
	}

	/**1�����쐬����Ƃ��B*/
	public ImagePlus createStack(ImagePlus imp){
		ImageStack stack = imp.getImageStack();//�����Ɏw�肳�ꂽimage�Ɠ����T�C�Y�ł����̃X�^�b�N���쐬�B
		ImagePlus stackImp = new ImagePlus(imp.getTitle(), stack);//�X�^�b�N����ImagePlus���쐬�B
		return stackImp;
	}

	/**�����̃X�^�b�N���擾����B*/
	public ImageStack[] getStack(ImagePlus[] imp){
		ImageStack[] stack = new ImageStack[nIp];
		for(int i=0;i<nIp;i++){
			stack[i] = imp[i].getStack();//1�̃X�^�b�N���擾
		}
		return stack;
	}

	/**�����̃X�^�b�N���ꂼ��ɃX���C�X��ǉ��B*/
	public void addImage(ImageStack[] stack,ImagePlus imp,ImageProcessor[]ip,int n){
		for(int i=0;i<nIp;i++){
			stack[i].addSlice(imp.getTitle(),ip[i],n);
		}
	}

	/**�R���X�g���N�^�̈����ɐݒ肳�ꂽ��ʐ��ɉ����āA�摜����ׂ�i�P���ɉ��ɕ��ׂ邾���j�Bx,y�Ŏn�_��ݒ肷��B*/
	public void setWindow(ImagePlus[] imp,int x,int y){
		ImageWindow[] win = new ImageWindow[nIp];
		for(int i=0;i<nIp;i++){
			imp[i].show();
			win[i] = imp[i].getWindow();
			win[i].setLocation(x+i*imp[i].getWidth(),y);
		}

	}

	/**�����̃X�^�b�N�ŁAn�Ɏw�肵���X���C�X�ԍ��̉摜��\������B*/
	public void setSlice(ImagePlus[] imp,int n){
		for(int i=0;i<nIp;i++){
			imp[i].setSlice(n);
		}
	}

	/**�����̉摜���A�b�v�f�[�g�B*/
	public void updateAndDraw(ImagePlus[] imp){
		for(int i=0;i<nIp;i++){
			imp[i].updateAndDraw();
		}
	}

	/**�����̃X�^�b�N���ꂼ��ɂ��A�Ō�̃X���C�X����������B*/
	public void deleteLastSlice(ImageStack[] stack){
		for(int i=0;i<nIp;i++){
			stack[i].deleteLastSlice();
		}
	}

	/**�����̃X�^�b�N���ꂼ��ɂ��An�Ŏw�肵���X���C�X����������B*/
	public void deleteSlice(ImageStack[] stack,int n){
		for(int i=0;i<nIp;i++){
			stack[i].deleteSlice(n);
		}
	}
}
