package behavior.io;

import ij.IJ;

import java.io.*;

import behavior.setup.Program;
import behavior.util.rmconstants.RMConstants;

/*********
�p�X�̈ꊇ�}�l�[�W�����g�N���X
 *********/
public class FileManager{
	/**���m�v���O�����̐�΃p�X*/
	public static final int program = 0;

	/**���Y�v���W�F�N�g�t�@�C���̐�΃p�X*/
	public static final int project = 1;

	/**�擾���ׂ��p�X���w�肷�邽�߂̃t�B�[���h*/
	public static final int PreferenceDir = 2;
	public static final int ImagesDir = 3;
	public static final int TracesDir = 4;
	public static final int XY_DataDir = 5;
	public static final int ResultsDir = 6;
	public static final int SessionsDir = 7;
	public static final int MoveMent_DataDir = 8;
	public static final int ReferencesDir = 9;

	/**total ���ʃt�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int totalResPath = 101;
	/**session �t�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int sessionPath = 103;
	/**parameter �t�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int parameterPath = 10;

	public static final int SessionID = 102;
	public static final int referencePath = 104;

	/**�摜�t�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int imagePath = 201;
	/**�g���[�X�摜�t�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int tracePath = 202;
	/**XY�f�[�^�t�@�C���̃p�X���擾����̂Ɏg�p����t�B�[���h*/
	public static final int xyPath = 203;

	public static final int SubjectID = 204;

	private String programPath;
	private String projectPath;
	private String sessionID;
	private String[] subjectID;
	private String referenceFile;
	private String[] imagePaths;
	private boolean setImagePaths = false;
	private boolean setProgramID = false, setProjectID = false, setSessionID = false, setSubjectID = false;

	//for Offline
	private String saveProjectPath;
	private boolean setSaveProjectID;

	private static FileManager fileManager;

	private static final String sep = System.getProperty("file.separator");

	//for TMOffline
	private String propFileName = "PJ_Prefs.properties";

	private FileManager(){
	}

	/*******
	��������C���X�^���X���擾�B
	 ********/
	public static FileManager getInstance(){
		if(fileManager == null)
			fileManager = new FileManager();
		return fileManager;
	}

	public void setProgram(Program program){
		String programID = program.toString();
		// Windows�Ȃ�ImageJ�̂���h���C�u�̃��[�g�N�_�A����ȊO�Ȃ烆�[�U�̃z�[���N�_
		programPath = (IJ.isWindows() ? "" : System.getProperty("user.home")) + sep + "Image_" + programID;
		setProgramID = true;
	}

	/*
	 *�v���W�F�N�gID���Z�b�g�B���̕ӂ́A���ׂ�behavior.setup.Setup�N���X�����d���B
	 *Setup�ɂ���炵���L�q�������̂����A���s�����͂܂��ʂ̃t�@�C���H
	 */
	public boolean setProjectID(String projectID){
		projectPath = programPath + sep + projectID;
		setProjectID = true;

		File f = new File(projectPath);
		return !f.exists();
	}

	//for Offline
	public void setSaveProjectID(String projectID){
		saveProjectPath = programPath + sep + projectID;
		setSaveProjectID = true;
		File dir = new File(getSavePath(project));
		if(!dir.exists())
			dir.mkdirs();
	}

	public boolean setProjectID(Program program, String projectID){
		String programID = program.toString();
		// Windows�Ȃ�ImageJ�̂���h���C�u�̃��[�g�N�_�A����ȊO�Ȃ烆�[�U�̃z�[���N�_
		programPath = (IJ.isWindows() ? "" : System.getProperty("user.home")) + sep + "Image_" + programID;
		projectPath = programPath + sep + projectID;
		setProgramID = true;
		setProjectID = true;

		File f = new File(projectPath);
		return !f.exists();
	}

	/*******
	�Z�b�V����ID���Z�b�g�Bbehavior.setup.Setup.java �����d���B
	 ********/
	public void setSessionID(String sessionID){
		this.sessionID = sessionID;
		setSessionID = true;
	}

	/*******
	�T�u�W�F�N�gID���Z�b�g�Bbehavior.setup.Setup.java �����d���B
	 ********/
	public void setSubjectID(String[] subjectID){
		this.subjectID = subjectID;
		setSubjectID = true;

		imagePaths = new String[subjectID.length];
	}

	public void setReferenceFile(String referenceFile){
		this.referenceFile = referenceFile;
	}

    public String getReferenceFileName(){
		return referenceFile;
	}

	/*******
	����� behavior.setup.Setup.java ������Ă���BSetup.java ���g���̂Ȃ�C�ɂ��Ȃ��Ă����B
	 ********/
	public void makeDirectory(Program program){
		File dir;
		/*String[] dirPaths = {getPath(PreferenceDir), getPath(ImagesDir), getPath(TracesDir),
					 getPath(ResultsDir), getPath(SessionsDir), getPath(XY_DataDir)};
		for(int num = 0; num < dirPaths.length; num++){
			dir = new File(dirPaths[num]);
			if(!dir.exists())
				dir.mkdirs();
		}*/

		// PreferenceDir
		dir = new File(getPath(PreferenceDir));
		if(!dir.exists())
			dir.mkdirs();

		// ImagesDir
		if(!program.isHC()){
			dir = new File(getPath(ImagesDir));
			if(!dir.exists())
				dir.mkdirs();
		}

		// TracesDir
		if(!program.isHC()){
			dir = new File(getPath(TracesDir));
			if(!dir.exists())
				dir.mkdirs();
		}

		// ResultsDir
		dir = new File(getPath(ResultsDir));
		if(!dir.exists())
			dir.mkdirs();

		// SessionsDir
		dir = new File(getPath(SessionsDir));
		if(!dir.exists())
			dir.mkdirs();

		// XY_DataDir
		dir = new File(getPath(XY_DataDir));
		if(!dir.exists())
			dir.mkdirs();

		// ReferencesDir
		if((program==Program.RM&&RMConstants.isReferenceMemoryMode()) || program == Program.FZ //|| program == Program.WM || program == Program.WMP
				||program == Program.CSI){
			dir = new File(getPath(ReferencesDir));
			if(!dir.exists())
				dir.mkdirs();
		}

		// MoveMent_DataDir
		if(program == Program.PS){
			dir = new File(getPath(MoveMent_DataDir));
			if(!dir.exists())
				dir.mkdirs();
		}
	}

	/*******
	�摜�t�@�C�������łɑ��݂��Ă��邩�ǂ����B�i�㏑���m�F�Ɏg���j
	 ********/
	public boolean subjectImageExist(){
		boolean exist = false;
		int allCage = subjectID.length;
		for(int cage=0; cage<allCage; cage++){
			String fileName = projectPath +sep+ "Images" +sep+ subjectID[cage] + ".tif";
			String fileName2 = projectPath +sep+ "Images" +sep+ subjectID[cage] + ".tiff";
			if(new File(fileName).exists()){
				imagePaths[cage] = fileName;
			    exist = true;
			}else if(new File(fileName2).exists()){
				imagePaths[cage] = fileName2;
			    exist = true;
			}else{
				imagePaths[cage] = fileName;
			}
		}
		setImagePaths = true;

		return exist;
	}

	/*******
	�p�X���擾�B�����Ŏ擾�ł���̂́AString �Ŏ擾�ł�����̂̂݁B�摜�t�@�C���̃p�X�Ƃ��́A
	�P�[�W��������̂ŁAString �łȂ��AString[] �Ŏ擾�����B�����������̂� getPathes(int) �̕��Ŏ擾����B
	 *@param type �t�B�[���h�l
	 ********/
	public String getPath(int type){
		if(!setProgramID)
			throw new IllegalStateException("programID is not set");
		if(type > 0 && !setProjectID)
			throw new IllegalStateException("projectID is not set");
		if(type > 100 && !setSessionID)
			throw new IllegalStateException("sessionID is not set");
		if(type > 200)
			throw new IllegalArgumentException("use FileManager.getPaths(int)");
		switch(type){
		case SessionID:return sessionID;
		case program: return programPath;
		case project: return projectPath;
		case PreferenceDir:	return projectPath + sep + "Preference";
		case ImagesDir:		return projectPath + sep + "Images";
		case TracesDir:		return projectPath + sep + "Traces";
		case XY_DataDir:	return projectPath + sep + "XY_Data";
		case ResultsDir:	return projectPath + sep + "Results";
		case SessionsDir:	return projectPath + sep + "Sessions";
		case MoveMent_DataDir:	return projectPath + sep + "MoveMent_Data";
		case ReferencesDir:	return projectPath + sep + "References";
		case totalResPath:	return projectPath + sep + "Results"+sep+ sessionID + "-RES.txt";
		case sessionPath:	return projectPath + sep + "Sessions"+sep+ sessionID + ".txt";
		case parameterPath:	return projectPath + sep + "Preference"+sep+propFileName;
		case referencePath: return projectPath +sep+ "References" +sep+ referenceFile +".txt";
		default: return null;
		}
	}

	

	//for Offline
	public String getSavePath(int type){
		if(!setProgramID)
			throw new IllegalStateException("programID is not set");
		if(type > 0 && !setSaveProjectID)
			throw new IllegalStateException("projectID is not set");
		if(type > 100 && !setSessionID)
			throw new IllegalStateException("sessionID is not set");
		if(type > 200)
			throw new IllegalArgumentException("use FileManager.getPaths(int)");
		switch(type){
		case SessionID:return sessionID;
		case program: return programPath;
		case project: return saveProjectPath;
		case PreferenceDir:	return saveProjectPath + sep + "Preference";
		case ImagesDir: 	return saveProjectPath + sep + "Images";
		case TracesDir:		return saveProjectPath + sep + "Traces";
		case XY_DataDir:	return saveProjectPath + sep + "XY_Data";
		case ResultsDir:	return saveProjectPath + sep + "Results";
		case SessionsDir:	return saveProjectPath + sep + "Sessions";
		case MoveMent_DataDir:	return saveProjectPath + sep + "MoveMent_Data";
		case ReferencesDir:	return saveProjectPath + sep + "References";
		case totalResPath:	return saveProjectPath + sep + "Results"+sep+ sessionID + "-RES.txt";
		case sessionPath:	return saveProjectPath + sep + "Sessions"+sep+ sessionID + ".txt";
		case parameterPath:	return saveProjectPath + sep + "Preference"+sep+propFileName;
		case referencePath: return saveProjectPath +sep+ "References" +sep+ referenceFile +".txt";
		default: return null;
		}
	}

	public void setPropertyFileName(String fileName){
		propFileName = fileName;
	}

	/*******
	 *@param type �t�B�[���h�l
	 ********/
	public String[] getPaths(int type){
		if(type < 200)
			throw new IllegalArgumentException("use FileManager.getPath(int)");
		if(!setSubjectID)
			throw new IllegalStateException("subjectID is not set");
		if(type == imagePath && !setImagePaths)
			throw new IllegalStateException("ImagePaths is not set");
		int allCage = subjectID.length;
		String[] pathes = new String[allCage];
		for(int cage = 0; cage < allCage; cage++){
			switch(type){
			case SubjectID: pathes[cage] = subjectID[cage]; break;
			case imagePath:	pathes[cage] = imagePaths[cage]; break;
			case tracePath: pathes[cage] = projectPath + sep + "Traces" + sep + subjectID[cage] + ".tif"; break;
			case xyPath: pathes[cage] = projectPath + sep + "XY_Data" + sep + subjectID[cage] + "_XY.txt"; break;
			}
		}
		return pathes;
	}

	public String[] getSavePaths(int type){
		if(type < 200)
			throw new IllegalArgumentException("use FileManager.getPath(int)");
		if(!setSubjectID)
			throw new IllegalStateException("subjectID is not set");
		if(type == imagePath && !setImagePaths)
			throw new IllegalStateException("ImagePaths is not set");
		int allCage = subjectID.length;
		String[] pathes = new String[allCage];
		for(int cage = 0; cage < allCage; cage++){
			switch(type){
			case SubjectID: pathes[cage] = subjectID[cage]; break;
			case imagePath:	pathes[cage] = imagePaths[cage]; break;
			case tracePath: pathes[cage] = saveProjectPath + sep + "Traces" + sep + subjectID[cage] + ".tif"; break;
			case xyPath: pathes[cage] = saveProjectPath + sep + "XY_Data" + sep + subjectID[cage] + "_XY.txt"; break;
			}
		}
		return pathes;
	}

	/*******
	bin ���ʃt�@�C���̃p�X�擾�B����͊�{�I�ɂ� behavior.io.ResultSaver.java �Ŏg����̂݁B
	 *@param resultName ���ʃf�[�^�̖��O�B
	 ********/
	public String getBinResultPath(String resultName){
		if(!setSessionID)
			throw new IllegalStateException("sessionID is not set");
		return projectPath + sep + "Results" + sep + sessionID + "-" + resultName + ".txt";
	}

	//for Offline
	public String getSaveBinResultPath(String resultName){
		if(!setSessionID)
			throw new IllegalStateException("sessionID is not set");
		return saveProjectPath + sep + "Results" + sep + sessionID + "-" + resultName + ".txt";
	}
}