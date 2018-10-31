package behavior.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import ij.ImagePlus;
import ij.gui.StackWindow;


/**
 * Roiの設定画像を、他のウインドウに直接書き込めるようにするもの
 * @author behaviorcore
 *
 */
public class WMNavigator extends StackWindow implements FocusListener, WindowListener{

	/**
	 * 構想
	 * 目的：CurrentImage、Trace、Movie　に　直接Navigator　を　表示させるためのもの。リアルタイム画像に次々と表示させる
	 * 必要となるもの：書き込む対象、書き込れるもの 今のところ書き込むものは、OvalDrawぐらいのものか、
	 * ImagePlus　から、このクラスを呼び出し、このクラスが必要なものを書き込む。
	 * 書き込むものは、RoiOperatorなどから、Roiとして呼び出す。
	 * String等を書き込みたい場合には、それも呼び出す。
	 * このあたりは、OvalDrawを参考にして作ればよいか…。
	 */
	public static final int NO_OBJECT = 0;
	public static final int LINE = 1;
	public static final int RECTANGEL = 2;
	public static final int OVAL = 3;
	public static final int LETTER = 4;

	static int objectX,objectY,objectW,objectH,objectShape;
	static  Color objectColor;
	static Image image;
	static int nObject;

	private static final long serialVersionUID = 1L;

	public WMNavigator(ImagePlus arg0) {
		super(arg0);
	}

	public void draw(int x,int y,int w,int h,int shape,Color color){
		objectX = x;
		objectY = y;
		objectW = w;
		objectH = h;
		objectColor = color;
		objectShape = shape;
	}

	public void paint(Graphics g){
		super.paint(g);

		Graphics2D g2 = (Graphics2D) g;

		if(objectShape != NO_OBJECT){
			if(objectShape == OVAL){
				g2.setColor(objectColor);
				g2.drawOval(objectX,objectY,objectW,objectH);
			}
		}

		image = this.createImage((this.imp).getWidth(),(this.imp).getHeight());

	}

	public Image getImage(){
		return image;
	}

	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

}
