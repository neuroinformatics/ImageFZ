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
 * RM(ReferenceMemory)�p�Ƀt�@�C���̓��o�͂𐧌䂷�邽�߂̂��́B
 * Reference�t�@�C������MouseID�Ɖa�̂���A�[���̔ԍ���ǂݍ��ޏꍇ�ƁA
 * Session�t�@�C������SubjectID�Ɖa�̂���A�[���̔ԍ���ǂݍ��ޏꍇ������B
 * ���̂Ƃ���{���ʂł����L2�̗p�r�ŗp���Ă���B
 *
 * �ǂ����ID�ƃA�[���ԍ��͓���̍s��tsv�ŋL�^����Ă���B
 * (ID)	(�A�[���ԍ�)�̏��B
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

	/* Session�t�@�C������SubjectID�Ɖa�̂���A�[���̔ԍ���ǂݍ��ށ@*/

	/**
	 * ReferenceMemory�p��SubjectID�Ɖa�̂���A�[���̔ԍ���ǂݍ���
	 * @return�@ID�ƃA�[���ԍ�
	 * @throws IOException
	 * @throws NullPointerException �t�@�C����ID���Ȃ��ꍇ
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
	 * ReferenceMemory�p��SubjectID�Ɖa�̂���A�[���̔ԍ���ǂݍ��݁AMap�`���ŕԂ��B
	 * ��₱�����̂�ID�̐����s��Ȃ��߁B
	 * @return�@ID�ƃA�[���ԍ�
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
	 * WorkingMemory�p��SubjectID�Ɖa�̂���A�[���̔ԍ���ǂݍ���
	 * @return�@ID
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

	/* Reference�t�@�C������MouseID��ǂݍ��� */

	/**
	 * MouseID��ǂݍ��ށB
	 * @return�@ID
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
	 * �w�肵��ID�̃A�[���ԍ���ǂݍ��ށB
	 * @param ID MouseID
	 * @param def�@ID���Ȃ��ꍇ�ɕԂ��l
	 * @return �A�[���ԍ�
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