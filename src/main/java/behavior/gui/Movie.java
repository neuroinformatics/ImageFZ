package behavior.gui;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;

import behavior.controller.OutputController;
import behavior.image.ImageCapture;
import behavior.setup.Program;

/*******************
�f����\������N���X�B
�����ɂ��ẴZ�b�g�A�b�v���I���������_�ł��̉f���͕\������A�E�B���h�E���������
�����ւƈڂ�
 ********************/
public class Movie{
	private Program program = Program.DEFAULT;	//�ǂ̃v���O�����ɂ��Ή����Ȃ��f�t�H���g�̒l

	public Movie(){
	}

	/************
	Light-Dark �����Ȃǂł́A�f���̕\���ƂƂ��ɁA�A�E�g�v�b�g�f�o�C�X�̊Ǘ��Ȃǂ̓���̍�Ƃ��K�v�ƂȂ�B
	����ɑΉ������邽�߂ɂ́Aprogram ���w�肷��΂悢�B
	 *************/
	public Movie(Program program){
		this.program = program;
	}

	/**@param movieImp �f���Ɏg�p���� ImagePlus*/
	public void showMovie(ImagePlus movieImp){
		ImageWindow movieWin = movieImp.getWindow();
		ImageCapture imageCapture = ImageCapture.getInstance();
		if(movieWin == null){
			movieImp.show();
			movieWin = movieImp.getWindow();
		}
		while(true){
			if(movieWin.isClosed()){
				if(program == Program.LD){
					OutputController output = OutputController.getInstance();
					output.clear(OutputController.ALL_CHANNEL);
				}
				break;
			}
			ImageProcessor nextIp = (imageCapture.capture()).getProcessor();
			movieImp.setProcessor("movie", nextIp);
			try{
				Thread.sleep(10);
			}catch(InterruptedException e){
			}
		}
	}
}







