package behavior.setup;

import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

import behavior.io.FileManager;

import ij.IJ;

/**
 * Morris　Water　Maze　のセットアップクラス。
 * この実験では、鼠のスタート位置、区画の分け方などを入力する必要がある。
 * 鼠が、以前の実験でどのような動きをしたかや、実験の種類　Hidden　Visible　Probe の設定も行う。
 */
public class Water_Maze_Setup{
	protected String projectName;
	protected String sessionName;
	protected String prefsPath;
	/*
	 * StartPosition
	 */
	private String Mouse_Start_Position = "";
	private int Mouse_Start_Position_X = 0;
	private int Mouse_Start_Position_Y = 0;
	private int Mouse_Start_Position_ANGLE = 0;
	final int ANGLE_TO_PLATFORM = 500;
	final int ANGLE_TO_IVERSE = 600;
	private int PlatForm_Position_X = 0;
	private int PlatForm_Position_Y = 0;

	private File[] positionFile;


	public static final int VISIBLE = 0;
	public static final int HIDDEN = 1;
	public static final int PROVE = 2;

	public static int[] SequenceElements;
	/**
	 * 利用せず
	 * 
	 * コンストラクター、Pluginから、選択した時に使用。
	 */
	public Water_Maze_Setup(String path){
		prefsPath = path;
	}

	public Water_Maze_Setup(String project,String session){
		projectName = project;
		sessionName = session;
		loadMousePositionFiles();
	}
	/**
	 * 利用せず
	 * 
	 * マウスの開始位置が記述されたファイルを読み込む
	 */	
	private void loadMousePositionFiles(){
		File prefsFile = new File(projectName+ File.separator + "Prefs" + File.separator);
		if(prefsFile.exists()){
			positionFile = prefsFile.listFiles(new FilenameFilter(){
				public boolean accept(File dir,String name){
					return name.endsWith("Position.txt");
				}
			});

		}
	}
	/**
	 * 指定された開始位置が書き込まれたファイルを読み込む。
	 */
	public File getMousePositionFiles(int index){
		if(index > positionFile.length){
			IJ.showMessage(String.valueOf(index) + "Position.txt isn't found." + "\n" + "Please Enter another number");
		}
		return positionFile[index];
	}

	public boolean loadMousePosition(int num){
		String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir);
		File prefsFile = new File(path);
		final int filenum = num;
		if(prefsFile.exists()){
			positionFile = prefsFile.listFiles(new FilenameFilter(){
				public boolean accept(File dir,String name){
					return name.endsWith("Sequence" + String.valueOf(filenum) + ".txt");
				}
			});

		}
		if(positionFile.length == 0){
			IJ.showMessage("Cannot Open SequenceFile. Creating or Copiing file is needed.");
			return false;
		}
		loadMousePosition(positionFile);
		if(SequenceElements == null)
			return false;
		return true;
	}

	public void loadMousePosition(File[] pFile){
		FileReader fi = null;
		try{
			int c;
			fi = new FileReader(pFile[0].getAbsolutePath());
			while((c = fi.read()) != -1){
				Mouse_Start_Position += (char)c;
			}
		}catch(IOException e){
			IJ.showMessage(e.toString());
		}


		if(Mouse_Start_Position != null){
			StringTokenizer st = new StringTokenizer(Mouse_Start_Position," ");
			try{
				SequenceElements = new int[st.countTokens()];
				for(int j=0;j<SequenceElements.length;j++){
					//				if(!st.hasMoreTokens())
					//					break;
					String R = st.nextToken(" ");
					SequenceElements[j] = Integer.parseInt(R);
				}
			}catch(NoSuchElementException e){
				IJ.showMessage("Selected PositionFile is corrupted." + "\n" + "Please correct file or copy file from another folder");
				SequenceElements = null;
			}

		}

	}

	/**
	 * 利用せず
	 * @param pFile
	 */
	public void loadMousePosition(File pFile){
		FileReader fi = null;
		try{
			int c;
			fi = new FileReader(pFile.getAbsolutePath());
			while((c = fi.read()) != -1){
				Mouse_Start_Position += (char)c;
			}
		}catch(IOException e){
			IJ.showMessage(e.toString());
		}
		finally{
			try{
				fi.close();
			}catch(IOException e){
				IJ.showMessage(e.toString());
			}
		}
		/* 
		 * 指定された開始位置を読み込む
		 */
		if(Mouse_Start_Position != null){
			StringTokenizer st = new StringTokenizer(Mouse_Start_Position);
			try{
				Mouse_Start_Position_X = Integer.parseInt((st.nextToken("\t")));
				Mouse_Start_Position_Y = Integer.parseInt((st.nextToken("\t")));
				Mouse_Start_Position_ANGLE = Integer.parseInt((st.nextToken("\t")));
				PlatForm_Position_X = Integer.parseInt((st.nextToken("\t")));
				PlatForm_Position_Y = Integer.parseInt((st.nextToken("\t")));
			}catch(NoSuchElementException e){
				IJ.showMessage("Selected PositionFile is corrupted." + "\n" + "Please correct file or copy file from another folder");
			}

		}
	}

	/**
	 * 利用せず
	 * 
	 * 開始位置を指定のファイルに保存する
	 * @param path Prefsフォルダの絶対パス
	 * @param index ファイルのＩＤ、このパラメータで、区別する。
	 */
	public void saveMousePosition(String path,int index){
		File newPFile = new File(path+"Start"+index+"Position.txt");
		try {
			if(!newPFile.createNewFile()){
				try{
					newPFile.delete();
					newPFile.createNewFile();
				}catch(SecurityException e){
					IJ.showMessage(e.toString());
				}

			}
		} catch (IOException e1) {
			IJ.showMessage(e1.toString());
		}
		try{
			FileWriter fw = new FileWriter(path+"Start"+index+"Position.txt");
			PrintWriter out = new PrintWriter(fw);
			out.println(Mouse_Start_Position_X+"\t"+Mouse_Start_Position_Y+"\t"+Mouse_Start_Position_ANGLE+"\t"+PlatForm_Position_X+"\t"+PlatForm_Position_Y+"\t");
			out.close();
			fw.close();
		}catch(IOException e){
			IJ.showMessage(e.toString());
		}
	}

}
