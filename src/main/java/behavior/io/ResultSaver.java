package behavior.io;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.process.ImageProcessor;
import ij.text.TextPanel;

import behavior.Version;
import behavior.io.FileManager;
import behavior.io.FileCreate;
import behavior.io.SafetySaver;
import behavior.setup.Header;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.util.GetDateTime;

/***********************************
 *�@�����̌��ʂł��鐔�l��摜�𓝍��I�ɕۑ�����N���X�B
 *�@�ۑ��́A�摜��XY�f�[�^�ȂǊe�P�[�W����̂��̂́A����w�肵�ĕۑ��A
 *�@��̃t�@�C���ɑS�P�[�W�f�[�^���������̂́A�ꊇ�ۑ��ɂ��Ă���B
 ************************************/
/**
 * @author anonymous
 * @author Modifier Butoh
 */
public class ResultSaver{
	protected int allCage;
	protected Program program;
	private ImagePlus[] imageImp;
	protected ImageStack[] traceStack;
	private ImageStack[] imageStack;
	protected String[] subjectID;
	protected boolean[] activeCage;
	protected FileManager fileManager = FileManager.getInstance();

	/******
	�R���X�g���N�^�B
	 *@param program �v���O�����ԍ��B
	 *@param allCage �S�P�[�W���B
	 *@param backIp �o�b�N�O���E���h�摜
	 *******/
	public ResultSaver(Program program, int allCage, ImageProcessor[] backIp){
		this.program = program;
		this.allCage = allCage;
		activeCage = new boolean[allCage];
		Arrays.fill(activeCage, true);
		imageImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			imageImp[cage] = new ImagePlus("", backIp[cage]);
		StackBuilder sb = new StackBuilder(allCage);
		imageImp = sb.buildStack(imageImp);
		imageStack = sb.getStack(imageImp);
		sb.deleteSlice(imageStack, 1);
	}

	public ImageStack[] getImageStack(){
		return imageStack;
	}

	/******
	�g���[�X�摜�̃Z�b�g�A�b�v�B�g���[�X����ۂ́A�Ăяo���K�v������B
	 *@param backIp �o�b�N�O���E���h�摜
	 *******/
	public void setTraceImage(ImageProcessor[] backIp){
		if(backIp.length != allCage)
			throw new IllegalArgumentException("");
		traceStack = new ImageStack[allCage];
		for(int cage = 0; cage < allCage; cage++){
			traceStack[cage] = (new ImagePlus("trace", backIp[cage])).createEmptyStack();
		}
	}

	/******
	�g���[�X�摜���X�^�b�N�ɂ�ł����B
	 *@param cage �X�^�b�N�ɐς݂����P�[�W�̔ԍ��B
	 *******/
	public synchronized void addTraceImage(int cage, ImageProcessor traceIp){
		if(traceIp == null)
			return;
		traceStack[cage].addSlice("trace", traceIp.convertToByte(false)); //�O�̂��߁A������x ByteProcessor �ɂȂ�������������B
	}

	/******
	�T�u�W�F�N�gID���Z�b�g����B��������Ȃ��ƕۑ��̍ۂɍ����x����B
	 *******/
	public void setSubjectID(String[] subjectID){
		if(subjectID.length != allCage)
			throw new IllegalArgumentException("cage num doesn't match:" + allCage + ":" + subjectID.length);
		this.subjectID = subjectID;
	}

	/******
	�ۑ����ׂ��P�[�W�� boolean[] �̌`�ŃZ�b�g����Bfalse �ɂȂ��Ă��鏇�Ԃ̃P�[�W�ɂ��Ă͕ۑ����s���Ȃ��B
	 *******/
	public void setActiveCage(boolean[] activeCage){
		this.activeCage = activeCage;
	}

	/******
	���݂̉摜���X�^�b�N�ɐςށB
	 *@param cage �P�[�W�ԍ��B
	 *******/
	public synchronized void setCurrentImage(int cage, ImageProcessor currentIp){
		imageStack[cage].addSlice("image", currentIp.convertToByte(false)); //�O�̂��߁A������x ByteProcessor �ɂȂ�������������B
	}

	/******
	�摜��ۑ�����B
	 *@param backIp �o�b�N�O���E���h�摜�B
	 *******/
	public void saveImage(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			imageStack[cage].addSlice("background", backIp[cage].convertToByte(false));
			SafetySaver saver = new SafetySaver();
			saver.saveImage(fileManager.getPaths(FileManager.imagePath)[cage], imageStack[cage]);
		}
	}

	public void saveImage(ImageProcessor[] backIp,String session){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			imageStack[cage].addSlice("background", backIp[cage].convertToByte(false));
			SafetySaver saver = new SafetySaver();
			File imageFile = new File(fileManager.getPath(FileManager.ImagesDir)+File.separator+session);
			if(!imageFile.exists())
				imageFile.mkdir();
			saver.saveImage(fileManager.getPath(FileManager.ImagesDir)+File.separator+session+File.separator+subjectID[cage]+".tif", imageStack[cage]);
		}
	}

	/******
	�g���[�X�摜�̕ۑ��B
	 *******/
	public synchronized void saveTraceImage(){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			if(traceStack[cage].getSize() == 0) continue;
			SafetySaver saver = new SafetySaver();
			File traceFile = new File(fileManager.getPath(FileManager.TracesDir));
			if (!traceFile.exists())
				traceFile.mkdir();
			saver.saveImage(fileManager.getPaths(FileManager.tracePath)[cage], traceStack[cage]);
		}
	}

	public void saveOfflineTraceImage(){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			if(traceStack[cage].getSize() == 0) continue;
			SafetySaver saver = new SafetySaver();
			File traceFile = new File(fileManager.getSavePath(FileManager.TracesDir));
			if (!traceFile.exists())
				traceFile.mkdir();
			saver.saveImage(fileManager.getSavePaths(FileManager.tracePath)[cage], traceStack[cage]);
		}
	}

	public synchronized void saveTraceImage(String session){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			if(traceStack[cage].getSize() == 0) continue;
			SafetySaver saver = new SafetySaver();
			File traceFile = new File(fileManager.getPath(FileManager.TracesDir)+File.separator+session);
			if(!traceFile.exists())
				traceFile.mkdir();
			saver.saveImage(fileManager.getPath(FileManager.TracesDir) +File.separator+session+File.separator + subjectID[cage]+".tif", traceStack[cage]);
		}
	}

	/******
	XY �f�[�^�̕ۑ��B
	 *@param cage �P�[�W�ԍ��B
	 *@param xyTp �ۑ�����e�L�X�g�p�l���B
	 *******/
	public void saveXYResult(int cage, TextPanel xyTp){
		File xyFile = new File(fileManager.getPath(FileManager.XY_DataDir));
		if (!xyFile.exists())
			xyFile.mkdir();
		xyTp.saveAs(fileManager.getPaths(FileManager.xyPath)[cage]);
	}

	public void saveXYResult(int cage, TextPanel xyTp,String session){
		File xyFile = new File(fileManager.getPath(FileManager.XY_DataDir)+File.separator+session);
		if(!xyFile.exists())
			xyFile.mkdir();
		xyTp.saveAs(fileManager.getPath(FileManager.XY_DataDir) + File.separator + session + File.separator+subjectID[cage]+".txt");
	}

	public void saveXYResult(int cage,List<String> text){
		File xyFile = new File(fileManager.getPath(FileManager.XY_DataDir));
		if (!xyFile.exists())
			xyFile.mkdir();

		FileCreate creater = new FileCreate(fileManager.getPaths(FileManager.xyPath)[cage]);
		creater.write("", false);
		creater.write(Header.getXYHeader(program), true);
		for(String s:text){
		    creater.write(s, true);
		}
	}

	public void saveOfflineXYResult(int cage, TextPanel xyTp){
		File xyFile = new File(fileManager.getSavePath(FileManager.XY_DataDir));
		if (!xyFile.exists())
			xyFile.mkdir();
		xyTp.saveAs(fileManager.getSavePaths(FileManager.xyPath)[cage]);
	}

	public void saveOfflineXYResult(int cage, TextPanel xyTp,String session){
		File xyFile = new File(fileManager.getSavePath(FileManager.XY_DataDir)+File.separator+session);
		if(!xyFile.exists())
			xyFile.mkdir();
		xyTp.saveAs(fileManager.getSavePath(FileManager.XY_DataDir) + File.separator + session + File.separator+subjectID[cage]+".txt");
	}

	public void saveOfflineXYResult(int cage,List<String> text){
		File xyFile = new File(fileManager.getSavePath(FileManager.XY_DataDir));
		if (!xyFile.exists())
			xyFile.mkdir();

		FileCreate creater = new FileCreate(fileManager.getSavePaths(FileManager.xyPath)[cage]);
		creater.write("", false);
		creater.write(Header.getXYHeader(program), true);
		for(String s:text){
		    creater.write(s, true);
		}
	}

	//���}���u�H
	//saveOfflineTotalResult�ֈڍs���Ă悢����
	public void saveBMOfflineTotalResult(String[][] result, boolean writeHeader){
		FileCreate creater = new FileCreate(fileManager.getPath(FileManager.totalResPath));
		if(writeHeader){
			creater.write("#Offline, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
			String header = Header.getTotalResultHeader(program); 
			creater.write(header, true);
		}
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			StringBuilder line = new StringBuilder(subjectID[cage]);
			for(int bin = 0; bin < result[cage].length; bin++)
				line.append("\t" + result[cage][bin]);
			creater.write(line.toString(), true);
		}
	}

	/******
	�g�[�^���f�[�^�̕ۑ��B
	 *@param result result[�P�[�W�ԍ�][�f�[�^�̎��]�B
	 *@param writeHeader �w�b�_���������ǂ����B
	 *******/
	public void saveOnlineTotalResult(String[][] result,Calendar[] calendar, boolean writeHeader, boolean writeVersion, boolean writeParameter){
		FileCreate creater = new FileCreate(fileManager.getPath(FileManager.totalResPath));
		if(writeHeader){
			String header = Header.getTotalResultHeader(program); 
			creater.write(header+"\tExperimentDate(MMDDYY)\tExperimentTime(HH:MM:SS)", true);
		}
		if(writeVersion){
			creater.write("#Online, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		if(writeParameter){
			saveParameter(creater);
		}
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			StringBuilder line = new StringBuilder(250);
			line.append(subjectID[cage]);
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
	�g�[�^���f�[�^�̕ۑ��B
	 *@param result result[�P�[�W�ԍ�][�f�[�^�̎��]�B
	 *@param writeHeader �w�b�_���������ǂ����B
	 *******/
	public void saveOfflineTotalResult(String[] result, boolean writeHeader, boolean writeVersion, boolean writeParameter){
		File file = new File(fileManager.getSavePath(FileManager.ResultsDir));
		if (!file.exists()){
			file.mkdir();
	    }

		FileCreate creater = new FileCreate(fileManager.getSavePath(FileManager.totalResPath));
		if(writeHeader){
			String header = Header.getTotalResultHeader(program); 
			creater.write(header, true);
		}
		if(writeVersion){
			creater.write("#Offline, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		if(writeParameter){
			saveParameter(creater);
		}
		StringBuffer line = new StringBuffer(subjectID[0]);
		for(int bin = 0; bin < result.length; bin++){
			line.append("\t" + result[bin]);
		}
		creater.write(line.toString(), true);
	}

	//result�t�@�C���Ƀp�����[�^���L�^
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
				if(name.length() >= 4 && name.substring(name.length()-4).equals(".roi")){	// .roi �ȃt�@�C���̂ݎ󂯕t����
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

		for(int i=0;i<list.length;i++){
			Roi bufRoi = null;
			try{
		        bufRoi = new RoiDecoder(list[i].getPath()).getRoi();
			}catch(IOException e){
				continue;
			}
		    final Rectangle bufrec = bufRoi.getBounds();
	        creater.write("##"+list[i].getName()+"="+bufrec.x+"\t"+bufrec.y+"\t"+bufrec.width+"\t"+bufrec.height, true);
		}
	}

	/******
	bin�iduration���������ɕ��������P�ʁj�f�[�^�̕ۑ��B
	 *@param fileName �ۑ�����t�@�C�����B
	 *@param result result[�P�[�W�ԍ�][bin]�B
	 *@param binHeader �w�b�_�ɁA"bin" �Ƃ��Ă������B
	 *******/
	public void saveOnlineBinResult(String fileName, String[][] result, boolean writeHeader, boolean binHeader, boolean writeVersion){
		String binResultPath = fileManager.getBinResultPath(fileName);
		FileCreate creater = new FileCreate(binResultPath);
		if(writeHeader){
			StringBuilder header = new StringBuilder("ID");
			int binLength = Parameter.getInt(Parameter.duration) / Parameter.getInt(Parameter.binDuration);
			/* bin �ɕ�����ƒ[�����ł�悤�Ȃ�Abin �̒��������������K�v������*/
			if(Parameter.getInt(Parameter.duration) % Parameter.getInt(Parameter.binDuration) != 0)
				binLength++;
			for(int bin = 0; bin < binLength; bin++)
				header.append("\t" + (binHeader? "bin" : "") + (bin + 1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("#Online, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage]){
				//creater.write("# null", true);
				continue;
			}
			StringBuilder binResult = new StringBuilder(subjectID[cage]);
			for(int bin = 0; bin < result[cage].length; bin++)
				binResult.append("\t" + result[cage][bin]);
			creater.write(binResult.toString(), true);
		}
	}

	/******
	bin�iduration���������ɕ��������P�ʁj�f�[�^�̕ۑ��B
	saveBinResult��2���邪�Abin�p�̃w�b�_�[�����Ȃ��ꍇ�͂�����B
	 *@param fileName �ۑ�����t�@�C�����B
	 *@param result result[�P�[�W�ԍ�][bin]�B
	 *******/
	public void saveOnlineBinResult(String fileName, String[][] result, boolean writeHeader, boolean writeVersion){
		saveOnlineBinResult(fileName, result, writeHeader, true,writeVersion);
	}

	/**
	 * bin���g�p���Ȃ�BT,RM�p
	 * ����Ă邱�Ƃ�saveBinResult()�Ƃ����ς��Ȃ�
	 * @param FileName ���ʃt�@�C����
	 * @param headerName�@�w�b�_��
	 * @param headerNum�@�w�b�_�̌�
	 * @param result�@����
	 * @param writeHeader�@�w�b�_���L�����邩
	 */
	public void saveOnlineRepectiveResults(String FileName, String headerName, int headerNum, List<String> result, boolean writeHeader, boolean writeVersion){
		String respectiveResultsPath = fileManager.getBinResultPath(FileName);
		FileCreate creater = new FileCreate(respectiveResultsPath);
		//�w�b�_�̋L��
		if(writeHeader){
			StringBuilder header = new StringBuilder("TrialName");
			for(int i= 0; i < headerNum; i++)
				header.append("\t" + headerName + (i+1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("#Online, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		//���ʂ̋L��
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			//���̂������Cage���ւ̑Ή��͂܂��s���S
			//(BT,RM�ł�Cage����1�ɌŒ肵�Ă���j
			StringBuffer respectiveResults = new StringBuffer(subjectID[cage]);
			for(Iterator<String> iter= result.iterator();iter.hasNext();){
				respectiveResults.append("\t" + iter.next());
			    iter.remove();
	 	    }
			creater.write(respectiveResults.toString(), true);
		}
	}

	/******
	���������ʃt�@�C���̖����ɋL������
	 *@param writeTotal total���ʃt�@�C���ɋL�����邩
	 *@param writeBin bin���ʃt�@�C���ɋL�����邩
	 *@param binFileName bin���ʃt�@�C���ɋL������ہA���̖��O
	 */
	public void writeDate(boolean writeTotal, boolean writeBin, String[] binFileName){
		FileCreate create = new FileCreate();
		if(writeTotal)
			create.writeDate(fileManager.getPath(FileManager.totalResPath));
		if(writeBin)
			for(int i = 0; i < binFileName.length; i++)
				create.writeDate(fileManager.getBinResultPath(binFileName[i]));
	}

	/**
	 */
	public void writeDate(String[] binFileName){
		writeDate(true, true, binFileName);
	}

	/**
	 * HC�p
	 * �J�n�E�I�����������ʃt�@�C���̖����ɋL������B
	 * @param startTime �J�n����
	 */
	public void writeDateWithStartTime(String startTime){
		FileCreate create = new FileCreate();
		String date = GetDateTime.getInstance().getDateTimeString();
		String writeString = startTime + " - " + date;
		create.writeChar(fileManager.getPath(FileManager.totalResPath), writeString, true);
	}

	public void writeDateOffline(boolean writeTotal, boolean writeBin, String[] binFileName){
		FileCreate create = new FileCreate();
		if(writeTotal)
			create.writeDate(fileManager.getSavePath(FileManager.totalResPath));
		if(writeBin)
			for(int i = 0; i < binFileName.length; i++)
				create.writeDate(fileManager.getSaveBinResultPath(binFileName[i]));
	}

	public void saveOfflineBinResult(String fileName, String[][] result, boolean writeHeader, boolean binHeader, boolean writeVersion){
		String binResultPath = fileManager.getSaveBinResultPath(fileName);
		FileCreate creater = new FileCreate(binResultPath);
		if(writeHeader){
			StringBuilder header = new StringBuilder("ID");
			int binLength = Parameter.getInt(Parameter.duration) / Parameter.getInt(Parameter.binDuration);
			/* bin �ɕ�����ƒ[�����ł�悤�Ȃ�Abin �̒��������������K�v������*/
			if(Parameter.getInt(Parameter.duration) % Parameter.getInt(Parameter.binDuration) != 0)
				binLength++;
			for(int bin = 0; bin < binLength; bin++)
				header.append("\t" + (binHeader? "bin" : "") + (bin + 1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("#Offline, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage]){
				//creater.write("# null", true);
				continue;
			}
			StringBuilder binResult = new StringBuilder(subjectID[cage]);
			for(int bin = 0; bin < result[cage].length; bin++)
				binResult.append("\t" + result[cage][bin]);
			creater.write(binResult.toString(), true);
		}
	}

	/******
	bin�iduration���������ɕ��������P�ʁj�f�[�^�̕ۑ��B
	saveBinResult��2���邪�Abin�p�̃w�b�_�[�����Ȃ��ꍇ�͂�����B
	 *@param fileName �ۑ�����t�@�C�����B
	 *@param result result[�P�[�W�ԍ�][bin]�B
	 *******/
	public void saveOfflineBinResult(String fileName, String[][] result, boolean writeHeader, boolean writeVersion){
		saveOfflineBinResult(fileName, result, writeHeader, true,writeVersion);
	}

	/**
	 * bin���g�p���Ȃ�BT,RM�p
	 * ����Ă邱�Ƃ�saveBinResult()�Ƃ����ς��Ȃ�
	 * @param FileName ���ʃt�@�C����
	 * @param headerName�@�w�b�_��
	 * @param headerNum�@�w�b�_�̌�
	 * @param result�@����
	 * @param writeHeader�@�w�b�_���L�����邩
	 */
	public void saveOfflineRepectiveResults(String FileName, String headerName, int headerNum, List<String> result, boolean writeHeader, boolean writeVersion){
		String respectiveResultsPath = fileManager.getSaveBinResultPath(FileName);
		FileCreate creater = new FileCreate(respectiveResultsPath);
		//�w�b�_�̋L��
		if(writeHeader){
			StringBuilder header = new StringBuilder("TrialName");
			for(int i= 0; i < headerNum; i++)
				header.append("\t" + headerName + (i+1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("#Offline, " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		//���ʂ̋L��
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;
			//���̂������Cage���ւ̑Ή��͂܂��s���S
			//(BT,RM�ł�Cage����1�ɌŒ肵�Ă���j
			StringBuffer respectiveResults = new StringBuffer(subjectID[cage]);
			for(Iterator<String> iter= result.iterator();iter.hasNext();){
				respectiveResults.append("\t" + iter.next());
			    iter.remove();
	 	    }
			creater.write(respectiveResults.toString(), true);
		}
	}
}