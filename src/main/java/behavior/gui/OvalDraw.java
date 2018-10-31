package behavior.gui;

import ij.ImagePlus;

import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * Frameのまま利用中、実際にはこいつ自体を表示する必要はないのだが…
 * BufferedImageを用いて、ImagePlusにJava.awt.Imageの受け渡しを行う
 * @author tt
 *
 */
public class OvalDraw extends JFrame {
	private static final long serialVersionUID = 1L;
	public static final int RECTANGLE = 1;
	public static final int OVAL = 2;
	public static final int LINE = 3;
	public static final int LETTER = 4; 
	public static final int FILLOVAL = 5;
	public static final int TOP_MARGE = 35;
	public static final int END_MARGE = 5;
	public boolean located = false;
	protected ImagePlus imp;
	private Image image;
	private BufferedImage bImage = null;

	public OvalDraw() {
		super();
		located = false;
	}

	public static void resetObject(){
		ObjectSaver.resetObjects();
	}

	public void setObject(double x,double y,double w,double h,int shape){
		ObjectSaver.setObject(x,y,w,h,shape,null);
	}

	public void setObject(double x,double y,double w,double h,int shape,Color color){
		ObjectSaver.setObject(x,y,w,h,shape,color);
	}

	public void setLetter(double x,double y,String letter){
		ObjectSaver.setLetter(x,y,letter,null);
	}

	public void setLetter(double x,double y,String letter,Color color){
		ObjectSaver.setLetter(x,y,letter,color);
	}

	public void setWindowSize(int x,int y,int w,int h,OvalDraw d){
		ObjectSaver.setWindowSize(x,y,w,h);
	}

	public void setImage(Image image,ImagePlus imp){
		this.imp = null;//一度初期化
		this.image = null;//一度初期化、これをしないと何故か重くなる
		this.image = image;
		this.imp = imp;
		if(bImage == null)
			bImage = new BufferedImage(imp.getWidth(),imp.getHeight(),BufferedImage.TYPE_INT_RGB);
	}

	public void setLocation(int x,int y){
		ObjectSaver.setLocation(x,y);
	}



	/**
	 * runメソッドを一時的に利用
	 * @param args
	 */


	public void run(String args){
		ObjectSaver.setImage();
		addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){}//今のところ、終了時に付け加えるものはないが…
		});
		setTitle(args);
//		name = args;
		setVisible(true);
		setLocation(700,150);
		setSize(100,100);
	}

	public void paint(Graphics g){

		Graphics2D g2 = (Graphics2D)bImage.getGraphics();

		g2.drawImage(image,0,0,this);

		for(int i=0;i<ObjectSaver.nObject;i++){
			if(ObjectSaver.objectShape[i] == OVAL){
				if(ObjectSaver.objectColor[i] == null){
					g2.setPaint(Color.red);
				}else{
					g2.setPaint(ObjectSaver.objectColor[i]);
				}
				g2.draw(new Ellipse2D.Double(ObjectSaver.objectX[i],ObjectSaver.objectY[i],ObjectSaver.objectWidth[i],ObjectSaver.objectHeight[i]));
			}else if(ObjectSaver.objectShape[i] == LETTER){
				if(ObjectSaver.objectColor[i] == null){
					g2.setPaint(Color.red);
				}else{
					g2.setPaint(ObjectSaver.objectColor[i]);
				}
				g2.drawString(ObjectSaver.objectLetter[i],(int)ObjectSaver.objectX[i],(int)ObjectSaver.objectY[i]);
			}else if(ObjectSaver.objectShape[i] == RECTANGLE){
				if(ObjectSaver.objectColor[i] == null){
					g2.setPaint(Color.red);
				}else{
					g2.setPaint(ObjectSaver.objectColor[i]);
				}
				g2.draw(new Rectangle2D.Double(ObjectSaver.objectX[i],ObjectSaver.objectY[i],ObjectSaver.objectWidth[i],ObjectSaver.objectHeight[i]));
			}else if(ObjectSaver.objectShape[i] == LINE){
				if(ObjectSaver.objectColor[i] == null){
					g2.setPaint(Color.red);
				}else{
					g2.setPaint(ObjectSaver.objectColor[i]);
				}
				g2.drawLine((int)ObjectSaver.objectX[i],(int)ObjectSaver.objectY[i],(int)ObjectSaver.objectWidth[i],(int)ObjectSaver.objectHeight[i]);	
			}else if(ObjectSaver.objectShape[i] == FILLOVAL){
				if(ObjectSaver.objectColor[i] == null){
					g2.setPaint(Color.red);
				}else{
					g2.setPaint(ObjectSaver.objectColor[i]);
				}
				g2.fill(new Ellipse2D.Double(ObjectSaver.objectX[i],ObjectSaver.objectY[i],ObjectSaver.objectWidth[i],ObjectSaver.objectHeight[i]));
			}else{

			}

		}

		if(!located){
			this.setBounds(imp.getWidth()*2+120,150+(imp.getHeight() + 80)*(ObjectSaver.nImage - 1),imp.getWidth()+END_MARGE+10,imp.getHeight()+TOP_MARGE+10);
			located = true;
		}

		//	IJ.showMessage(""+ObjectSaver.nImage);
		g.drawImage(bImage,END_MARGE,TOP_MARGE,this);
		g.dispose();

	}

	public Image getImage(){
		return bImage;
	}

	public void closeWindow(){
		ObjectSaver.resetObjects();
		this.setVisible(false);
		this.dispose();
		this.located = false;
	}
	/**
	 * WMでは、同じものを何度も描くために追加
	 * 書くものをここに保存しておく。
	 * @author tt
	 *
	 */
	static class ObjectSaver{

		static final int MAX_OBJECT = 100;
		static double[] objectX = new double[MAX_OBJECT];
		static double[] objectY = new double[MAX_OBJECT];
		static double[] objectWidth = new double[MAX_OBJECT];
		static double[] objectHeight = new double[MAX_OBJECT];
		static int[] objectShape = new int[MAX_OBJECT];
		static Color[] objectColor = new Color[MAX_OBJECT];
		static String[] objectLetter = new String[MAX_OBJECT];
		static int nObject = 0;
		static int windowX,windowY,windowW,windowH;
		static boolean ajustSize = false,ajustLocation = false;
		static int nImage = 0;

		public static void setObject(double x,double y,double w,double h,int shape,Color color){
			objectX[nObject] = x;
			objectY[nObject] = y;
			objectWidth[nObject] = w;
			objectHeight[nObject] = h;
			objectShape[nObject] = shape;
			objectColor[nObject] = color;
			nObject++;
		}

		public static void setLetter(double x,double y,String letter,Color color){
			nObject++;
			objectX[nObject] = x;
			objectY[nObject] = y;
			objectWidth[nObject] = 0;
			objectHeight[nObject] = 0;
			objectShape[nObject] = LETTER;
			objectLetter[nObject] = letter;
			objectColor[nObject] = color;
			nObject++;
		}

		public static void resetObjects(){
			Arrays.fill(objectX,0);
			Arrays.fill(objectY,0);
			Arrays.fill(objectWidth,0);
			Arrays.fill(objectHeight,0);
			Arrays.fill(objectShape,0);
			Arrays.fill(objectLetter,"");
			nObject = 0;
			nImage = 0;
		}

		public static void setWindowSize(int x,int y,int w, int h){
			windowX = x;
			windowY = y;
			windowW = w;
			windowH = h;
			ajustSize = true;
		}

		public static void setImage(){
			nImage++;
		}

		public static void setLocation(int x,int y){
			ajustLocation = true;
			windowX = x;
			windowY = y;
		}

	}

}