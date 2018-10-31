package behavior.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.*;

/**
 * RM(ReferenceMemory)用にファイルの入出力を制御するためのもの。
 * ReferenceファイルからMouseIDと餌のあるアームの番号を読み込む場合と、
 * SessionファイルからSubjectIDと餌のあるアームの番号を読み込む場合がある。
 * 今のところ本来別である上記2つの用途で用いている。
 *
 * どちらもIDとアーム番号は同一の行にtsvで記録されている。
 * (ID)	(アーム番号)の順。
 * 
 * @author Butoh
 */
public class RMReferenceManager{
	private String path;
	public static final int ID = 0;
	public static final int ALIGNMENT = 1;
	//private Logger log = Logger.getLogger("behavior.io.RMReferenceManager");
	//private String sep = System.getProperty("file.separator");

	public RMReferenceManager(final String path){
		this.path = path;
		/*try{
		FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.project) +sep+ "ExecuterLog123.txt",102400,1);
		fh.setFormatter(new SimpleFormatter());
	    log.addHandler(fh);
	    log.log(Level.INFO,"ok");
	    }catch(Exception e){
		e.printStackTrace();
	    }*/
	}

	/* SessionファイルからSubjectIDと餌のあるアームの番号を読み込む　*/

	/**
	 * ReferenceMemory用のSubjectIDと餌のあるアームの番号を読み込む
	 * @return　IDとアーム番号
	 * @throws IOException
	 * @throws NullPointerException ファイルにIDがない場合
	 */
	public String[][] getIDsAndAlignment()throws IOException,NullPointerException{
		Map<String,String> IDsAndAllignment = getReferenceIDsAndAllignment();
		String[][] results = new String[IDsAndAllignment.size()][2];

		Iterator<Map.Entry<String,String>> it = IDsAndAllignment.entrySet().iterator();
		for(int i=0;it.hasNext();i++){
			Map.Entry<String,String> entry = it.next();
			results[i][ID] = entry.getKey();
			results[i][ALIGNMENT] = entry.getValue();
		}

		return results;
	}

	/**
	 * ReferenceMemory用のSubjectIDと餌のあるアームの番号を読み込み、Map形式で返す。
	 * ややこしいのはIDの数が不定なため。
	 * @return　IDとアーム番号
	 * @throws IOException
	 * @throws NullPointerException
	 */
	private Map<String,String> getReferenceIDsAndAllignment()throws IOException,NullPointerException{
		Map<String,String> mouseIDs = new LinkedHashMap<String,String>();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));
		while((line = reader.readLine()) != null){
			line.trim();
			String[] buf = line.split("\t");
			//log.log(Level.INFO,""+buf.length);
			if(buf.length == 2){
				//log.log(Level.INFO,buf[0]+ " " +buf[1]);
			    mouseIDs.put(buf[0],buf[1]);
			}
		}
		reader.close();

		return mouseIDs;
	}

	/**
	 * WorkingMemory用のSubjectIDと餌のあるアームの番号を読み込む
	 * @return　ID
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public List<String> getWorkingIDs()throws IOException,NullPointerException{
		List<String> mouseIDs = new ArrayList<String>();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));
		while((line = reader.readLine()) != null){
			line.trim();
			String[] buf = line.split("\t");
			if(buf.length == 1)
			    mouseIDs.add(buf[0]);
		}
		reader.close();

		return mouseIDs;
	}

	/* ReferenceファイルからMouseIDを読み込む */

	/**
	 * MouseIDを読み込む。
	 * @return　ID
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public List<String> getIDs()throws IOException,NullPointerException{
		List<String> mouseIDs = new ArrayList<String>();
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));
		while((line = reader.readLine()) != null){
			line.trim();
			String[] buf = line.split("\t");
			mouseIDs.add(buf[0]);
		}
        reader.close();
		return mouseIDs;
	}

	/**
	 * 指定したIDのアーム番号を読み込む。
	 * @param ID MouseID
	 * @param def　IDがない場合に返す値
	 * @return アーム番号
	 * @throws IOException
	 * @throws NullPointerException
	 */
	public String getFoodArmAlignment(final String mouseID, final String def)throws IOException,NullPointerException{
		String line;
		BufferedReader reader = new BufferedReader(new FileReader(path));
		while((line = reader.readLine()) != null){
			line.trim();
			String[] buf = line.split("\t");
			if(buf[0].equals(mouseID) && buf.length == 2){
				reader.close();
				return buf[1];
		    }
		}
		reader.close();

		return def;
	}
}