package behavior.gui;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.process.ImageProcessor;

import behavior.controller.OutputController;
import behavior.image.ImageCapture;
import behavior.setup.Program;

/*******************
映像を表示するクラス。
実験についてのセットアップが終了した時点でこの映像は表示され、ウィンドウが閉じられると
実験へと移る
 ********************/
public class Movie{
	private Program program = Program.DEFAULT;	//どのプログラムにも対応しないデフォルトの値

	public Movie(){
	}

	/************
	Light-Dark 実験などでは、映像の表示とともに、アウトプットデバイスの管理などの特定の作業が必要となる。
	これに対応させるためには、program を指定すればよい。
	 *************/
	public Movie(Program program){
		this.program = program;
	}

	/**@param movieImp 映像に使用する ImagePlus*/
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







