package behavior;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * behavior.jar �̃o�[�W�������B
 *�@�A�[�J�C�u���� class �t�@�C���ŁA�ŐV�̍X�V�������o�[�W�����Ƃ���B
 *
 * �o�[�W�����͌���A�p�����[�^�ݒ�E�C���h�E�̃^�C�g���Ŋm�F�ł���B
 */
public class Version {

	private static String version;

	/**
	 * @return behavior �̃o�[�W�����B�`���� "behavior-YYYYMMDD"
	 */
	public static String getVersion(){
		if(version != null){
			return version;
		}else{
			try{
			    File[] sList = new File("plugins").listFiles(new FileFilter(){
				public boolean accept(File pathname){
					String name = pathname.getName();
					    if(name.length() >= 4 && name.startsWith("behavior_") && name.substring(name.length() - 4).equals(".jar")){
						    return true;
				        }else{
						    return false;
				        }
				    }
			    });

			    if(sList.length==0){
				    return version = "bahavior-??????";
			    }

			    @SuppressWarnings("resource")
				JarFile behavior = new JarFile("plugins/"+sList[0].getName());
			    long lastModified = 0;
			    for(Enumeration<JarEntry> entries = behavior.entries(); entries.hasMoreElements();){
				    JarEntry entry = entries.nextElement();
				    if(entry.getName().length() >= 6 && 
						            entry.getName().substring(entry.getName().length() - 6, entry.getName().length()).equals(".class")){
					    long temp = entries.nextElement().getTime();
					    lastModified = temp > lastModified ? temp : lastModified;
				    }
			    }
			    Calendar cale = Calendar.getInstance();
			    cale.setTimeInMillis(lastModified);
			    String year = Integer.toString(cale.get(Calendar.YEAR));
			    DecimalFormat format = new DecimalFormat("00");
			    String month = format.format(cale.get(Calendar.MONTH) + 1); 
			    String day = format.format(cale.get(Calendar.DATE));
			    return version = "behavior-" + year + month + day;
		    }catch(Exception e){
			    e.printStackTrace();
			    return version = "bahavior-??????";
		    }
		}
	}
}