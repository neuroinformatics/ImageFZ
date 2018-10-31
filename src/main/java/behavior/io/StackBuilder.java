package behavior.io;

import ij.*;
import ij.gui.*;
import ij.process.*;

/**stack の使用を補助*/
public class StackBuilder{

	private int nIp;
	String[] name;
	ImagePlus[] imp;

	/**コンストラクタの引数がないとき、1画面。あるときは、指定された引数分だけスタックを作成する。*/
	public StackBuilder(){
		nIp = 1;
	}

	public StackBuilder(int nIp){
		this.nIp = nIp;
	}

	/**引数のあるコンストラクタを使って、複数画面を作成するときはこちら。*/
	public ImagePlus[] buildStack(ImagePlus[] imp){
		ImagePlus[] simp = new ImagePlus[nIp];
		for(int i=0;i<nIp;i++){
			simp[i] = createStack(imp[i]);
		}
		return simp;
	}

	/**1つだけ作成するとき。*/
	public ImagePlus createStack(ImagePlus imp){
		ImageStack stack = imp.getImageStack();//引数に指定されたimageと同じサイズである空のスタックを作成。
		ImagePlus stackImp = new ImagePlus(imp.getTitle(), stack);//スタックからImagePlusを作成。
		return stackImp;
	}

	/**複数のスタックを取得する。*/
	public ImageStack[] getStack(ImagePlus[] imp){
		ImageStack[] stack = new ImageStack[nIp];
		for(int i=0;i<nIp;i++){
			stack[i] = imp[i].getStack();//1つのスタックを取得
		}
		return stack;
	}

	/**複数のスタックそれぞれにスライスを追加。*/
	public void addImage(ImageStack[] stack,ImagePlus imp,ImageProcessor[]ip,int n){
		for(int i=0;i<nIp;i++){
			stack[i].addSlice(imp.getTitle(),ip[i],n);
		}
	}

	/**コンストラクタの引数に設定された画面数に応じて、画像を並べる（単純に横に並べるだけ）。x,yで始点を設定する。*/
	public void setWindow(ImagePlus[] imp,int x,int y){
		ImageWindow[] win = new ImageWindow[nIp];
		for(int i=0;i<nIp;i++){
			imp[i].show();
			win[i] = imp[i].getWindow();
			win[i].setLocation(x+i*imp[i].getWidth(),y);
		}

	}

	/**複数のスタックで、nに指定したスライス番号の画像を表示する。*/
	public void setSlice(ImagePlus[] imp,int n){
		for(int i=0;i<nIp;i++){
			imp[i].setSlice(n);
		}
	}

	/**複数の画像をアップデート。*/
	public void updateAndDraw(ImagePlus[] imp){
		for(int i=0;i<nIp;i++){
			imp[i].updateAndDraw();
		}
	}

	/**複数のスタックそれぞれにつき、最後のスライスを消去する。*/
	public void deleteLastSlice(ImageStack[] stack){
		for(int i=0;i<nIp;i++){
			stack[i].deleteLastSlice();
		}
	}

	/**複数のスタックそれぞれにつき、nで指定したスライスを消去する。*/
	public void deleteSlice(ImageStack[] stack,int n){
		for(int i=0;i<nIp;i++){
			stack[i].deleteSlice(n);
		}
	}
}
