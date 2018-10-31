package behavior.io;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import behavior.Version;
import behavior.setup.Header;
import behavior.setup.Program;
import behavior.setup.parameter.BMParameter;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;

public class BMResultSaver extends ResultSaver {
	private FileManager fileManager = FileManager.getInstance();
	private Program program;
	private String subjectID;
	private final String sep = System.getProperty("file.separator");
	private final String tab = "\t";

	public BMResultSaver(Program program, int allCage, ImageProcessor[] backIp) {
		super(program, allCage, backIp);
		this.program = program;
	}
	
	public void setSubjectID(String[] subjectID){
		super.setSubjectID(subjectID);
		this.subjectID = subjectID[0];
	}
	
	public void saveBMOfflineTotalResult(String[][] result, boolean writeHeader){
		String path = fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_res.txt";
		FileCreate creater = new FileCreate(path);
		if(writeHeader){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
			String header = Header.getTotalResultHeader(program); 
			creater.write(header, true);
		}
		
		StringBuffer line = new StringBuffer(subjectID);
		for(int bin = 0; bin < result[0].length; bin++)
			line.append("\t" + result[0][bin]);
		creater.write(line.toString(), true);
		
	}
	
	/******
	トータルデータの保存。
	 *@param result result[ケージ番号][データの種類]。
	 *@param writeHeader ヘッダを書くかどうか。
	 *******/
	public void saveOnlineTotalResult(String[][] result,Calendar[] calendar, boolean writeHeader, boolean writeVersion, boolean writeParameter){
		FileCreate creater = new FileCreate(fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_res.txt");
		if(writeHeader){
			String header = Header.getTotalResultHeader(program); 
			creater.write(header+"\tExperimentDate(MMDDYY)\tExperimentTime(HH:MM:SS)", true);
		}
		if(writeVersion){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		if(writeParameter){
			saveParameter(creater);
		}
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			StringBuilder line = new StringBuilder(250);
			line.append(subjectID);
			for(int bin = 0; bin < result[cage].length; bin++){
				line.append("\t" + result[cage][bin]);
			}
			line.append("\t");
			line.append(((calendar[cage].get(Calendar.MONTH)+1)<10?"0":"")+(calendar[cage].get(Calendar.MONTH)+1));
			line.append(((calendar[cage].get(Calendar.DAY_OF_MONTH))<10?"0":"")+calendar[cage].get(Calendar.DAY_OF_MONTH));
			line.append((new String(calendar[cage].get(Calendar.YEAR)+"")).substring(2));
			line.append("\t");
			if(calendar[cage].get(Calendar.AM_PM)==Calendar.AM){
			    line.append((((calendar[cage].get(Calendar.HOUR))<10)?"0":"")+calendar[cage].get(Calendar.HOUR));
			}else{
				line.append((calendar[cage].get(Calendar.HOUR)+12));
			}
			line.append(":");
			line.append(((calendar[cage].get(Calendar.MINUTE))<10?"0":"")+calendar[cage].get(Calendar.MINUTE));
			line.append(":");
			line.append(((calendar[cage].get(Calendar.SECOND))<10?"0":"")+calendar[cage].get(Calendar.SECOND));
			creater.write(line.toString(), true);
		}
	}
	
	/******
	トータルデータの保存。
	 *@param result result[ケージ番号][データの種類]。
	 *@param writeHeader ヘッダを書くかどうか。
	 *******/
	public void saveOfflineTotalResult(String[] result, boolean writeHeader, boolean writeVersion, boolean writeParameter){
		FileCreate creater = new FileCreate(fileManager.getSavePath(FileManager.ResultsDir) + sep + fileManager.getSavePath(FileManager.SessionID) + "_res.txt");
		if(writeHeader){
			String header = Header.getTotalResultHeader(program); 
			creater.write(header, true);
		}
		if(writeParameter){
			saveParameter(creater);
		}
		if(writeVersion){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		StringBuffer line = new StringBuffer(subjectID);
		for(int bin = 0; bin < result.length; bin++){
			line.append("\t" + result[bin]);
		}
		creater.write(line.toString(), true);
	}
	
	/*
	 * 各穴の滞在時間を保存。
	 * probeテストでない場合も保存しておく。
	 */
	public void saveProbeResult(String[] result, boolean writeHeader) {
		String path = fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_probe.txt";
		FileCreate creater = new FileCreate(path);
		if(writeHeader){
			//creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
			String header = "SubjectID" + tab + "Target" + tab + "N1" + tab + "N2" + tab + "N3" + tab + "N4" + tab + "N5" + tab + "N6" + tab + "N7" + tab + "N8" + tab + "N9" + tab + "N10" + tab + "N11" + tab + "N12";
			creater.write(header, true);
		}
		
		StringBuffer line = new StringBuffer(subjectID);
		for(int bin = 0; bin < result.length; bin++)
			line.append("\t" + result[bin]);
		creater.write(line.toString(), true);
	}
	
	/*
	 * マウスがたどった穴の番号を保存。
	 */
	public void saveSelResult(String result, boolean writeHeader) {
		String path = fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_sel.txt";
		FileCreate creater = new FileCreate(path);
		if (writeHeader) {
		}
		
		StringBuffer line = new StringBuffer(subjectID);
		line.append(result);
		creater.write(line.toString(), true);
	}

	public void saveOfflineProbeResult(String[] result, boolean writeHeader) {
		String path = fileManager.getSavePath(FileManager.ResultsDir) + sep + fileManager.getSavePath(FileManager.SessionID) + "_probe.txt";
		FileCreate creater = new FileCreate(path);
		if(writeHeader){
			//creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
			String header = "SubjectID" + tab + "Target" + tab + "N1" + tab + "N2" + tab + "N3" + tab + "N4" + tab + "N5" + tab + "N6" + tab + "N7" + tab + "N8" + tab + "N9" + tab + "N10" + tab + "N11" + tab + "N12";
			creater.write(header, true);
		}
		
		StringBuffer line = new StringBuffer(subjectID);
		for(int bin = 0; bin < result.length; bin++)
			line.append("\t" + result[bin]);
		creater.write(line.toString(), true);
	}
	
	/*
	 * マウスがたどった穴の番号を保存。
	 */
	public void saveOfflineSelResult(String result, boolean writeHeader) {
		String path = fileManager.getSavePath(FileManager.ResultsDir) + sep + fileManager.getSavePath(FileManager.SessionID) + "_sel.txt";
		FileCreate creater = new FileCreate(path);
		if (writeHeader) {
		}
		
		StringBuffer line = new StringBuffer(subjectID);
		line.append(result);
		creater.write(line.toString(), true);
	}

	public void writeDate(boolean writeTotal, boolean writeBin, String[] binFileName){
		FileCreate create = new FileCreate();
		String res_path = fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_res.txt";
		//String probe_path = fileManager.getPath(FileManager.ResultsDir) + sep + fileManager.getPath(FileManager.SessionID) + "_probe.txt";
		if(writeTotal) {
			create.writeDate(res_path);
			//create.writeDate(probe_path);
		}
		if(writeBin)
			for(int i = 0; i < binFileName.length; i++)
				create.writeDate(fileManager.getBinResultPath(binFileName[i]));
	}
	
	public void writeDate(String[] binFileName){
		writeDate(true, false, binFileName);
	}

	protected void saveParameter(FileCreate creater){
		BufferedReader reader = null;
		try{
		    reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.parameterPath)));

		    String line = "";
	        while((line = reader.readLine()) != null){
		        creater.write("##"+line, true);
	        }
		}catch(IOException e){
			e.printStackTrace();
		}

	    File path = new File(FileManager.getInstance().getPath(FileManager.PreferenceDir));
		File[] list = path.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				String name = pathname.getName();
				if(name.length() >= 4 && name.substring(name.length()-4).equals(".roi")){	// .roi なファイルのみ受け付ける
					if(program==Program.CSI || program==Program.OLDCSI || program==Program.YM){
						if(name.length() >= 9 && name.substring(name.length()-9).equals("Outer.roi"))
							return false;
					}
					return true;
				}else{
					return false;
				}
			}
		});

		if(list.length!=0){
			creater.write("####ROI(RoiName=x\ty\twidth\theight)", true);
		}
		
		int diameter = BMParameter.innerR * 2;

		for(int i=0;i<list.length;i++){
			Roi bufRoi = null;
			try{
		        bufRoi = new RoiDecoder(list[i].getPath()).getRoi();
			}catch(IOException e){
				continue;
			}
		    final Rectangle bufrec = bufRoi.getBounds();
		    if ((list[i].getName()).equals("Field.roi"))
		    	creater.write("##"+list[i].getName()+"="+bufrec.x+"\t"+bufrec.y+"\t"+bufrec.width+"\t"+bufrec.height, true);
		    else
		    	creater.write("##"+list[i].getName()+"="+bufrec.x+"\t"+bufrec.y+"\t"+diameter+"\t"+diameter, true);
		}
	}
}
