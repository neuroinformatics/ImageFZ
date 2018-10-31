package behavior.util.rmconstants;
//�ǉ����Ă����Ƃ��Ȃ葝�����B
public class RMConstants{
    public static final int ARM_NUM = 8;
    public static final int ROI_NUM = 9;

    //labjack���g�������Ȃ��Ƃ���true�ɏ���������
    public static final boolean DEBUG = false;
    private static boolean isOffline = false;
    private static boolean isReferenceMemory = false;

    private static int foodArmNum = 8;
    private static String foodArmAlignment = ""+1;
    private static String mouseID = "";

    //�C���X�^���X����邱�Ƃ��֎~����
    private RMConstants(){}

    public static void setOffline(final boolean b){
    	isOffline = b;
    }

    public static void setReferenceMemoryMode(final boolean b){
    	isReferenceMemory = b;
    }

    public static void setFoodArmNum(final int num){
    	foodArmNum = num;
    }

    public static void setFoodArmAlignment(final String str){
    	foodArmAlignment = str;
    }

    public static void setMouseID(final String b){
    	mouseID = b;
    }

    public static String getMouseID(){return mouseID;}
    public static boolean isOffline(){return isOffline;}
    public static boolean isReferenceMemoryMode(){return isReferenceMemory;}
    public static int getFoodArmNum(){return foodArmNum;}
    public static String getFoodArmAlignment(){return foodArmAlignment;}
}