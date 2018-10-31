package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;
import behavior.setup.Program;
import behavior.setup.parameter.BTParameter;
import behavior.setup.parameter.Parameter;
import behavior.controller.InputController;

/**
 * BeamTest用の解析を行う。
 * 
 * 出力するデータは以下の13種類。
 * TotalDistance         -マウスが全体で移動した距離
 * AverageSpeed          -全体でのマウスの移動速度
 * MovingSpeed           -マウスが移動した時間中でのマウスの移動速度
 * MoveEpisodeNumber     -マウスが移動して、立ち止まるまでの回数
 * TotalMovementDuration -全体でマウスが移動していた時間
 * Slip(*)     　　　　　-マウスが滑った回数
 * Latency 　　　　-終了条件に関わらず、実験全体の時間
 * Call(*)     -ボタンを押して終了したか、解析で終了したか
 * Duration 　　　-制限時間
 * 
 * （以下のデータは出力数が一定ではない）
 * MovingSpeed          -マウスが移動して、立ち止まるまで(Phase)ごとの速度
 * DistancePerMovement  -Phaseごとの移動距離
 * DurationPerMoveament -Phaseごとの時間
 * SlipedTime(*)           -マウスが滑ったときの時間
 * 
 * Latency(マウスが落ちた場合),SlipedTimeの取得にはLabjackを使用。
 * (*)が付いたデータは、Offlineでは出力しない。
 * @author Butoh
 */
//終了条件と、出力数が一定ではない要素、さらにLabjackが絡み合って複雑になっている。
//うまくいかなかったら条件を整理する
public class BTAnalyzer extends Analyzer {
	//個別に結果を出力するためのフィールド
	public static final int DISTANCE_PER_MOVEMENT = 1;
	public static final int DURATION_PER_MOVEMENT = 2;
	public static final int SPEED_PER_MOVEMENT = 3;
	public static final int SLIPED_TIME = 4;

	//使用する座標
	//private final double LEFTMOST_X = 0.0,TOP_Y  = 0.0; 
	private double rightmostX = 10.0;//lowerY= 10.0;
	protected int[][] xyaData; //マウスの座標
	protected int[] prevXyaData; //1コマ前のマウスの座標

	//使用するパラメータ、一々呼び出さないでここでセットする
	private final int rate = Parameter.getInt(Parameter.rate);
	private final int duration = Parameter.getInt(Parameter.duration);
	private final double movementCriterion = Parameter.getDouble(BTParameter.movementCriterion);
	private final int goalArea = Parameter.getInt(BTParameter.goalArea);

	//使用する数値
	private int currentSlice; //interrupt()用
	private double currentDistance; //Slice間の移動距離
	private double distanceP; //移動してから立ち止まるまでの距離を一時的に保存(Pがついた変数の役割はこれ)
	private int slicePerMovement; //移動してから立ち止まるまでのSlice数
	private boolean setFreeze = true; //一度立ち止まったら動き出すまでtrue
	//private double conseqFreeze;
	//private double distanceF;
	private int allSlice;

	protected boolean[] endAnalyze; //これについては後で修正する(予定)
	private Slip spc = null;

	//結果データ
	private List<Double> speedPerMovement = new ArrayList<Double>();
	private List<Double> distancePerMovement = new ArrayList<Double>();
	private List<Double> durationPerMovement = new ArrayList<Double>();
 	//private double goalTime = 0.0,goalTimeB = 0.0, fellTime = 0.0;
	private double time;
	private List<Double> slipedTime = new ArrayList<Double>();
	private boolean button = false;

	//フラグ
    private boolean isGoal = false;
    private boolean offline = false;

	protected BTAnalyzer() {
	}

	public BTAnalyzer(ImageProcessor backIp) {
		imgManager = new ImageManager(backIp.duplicate());
		tracer = new Tracer(backIp);
		setImageSize(backIp.getWidth(), backIp.getHeight());

		//this.endAnalyze[0] = endAnalyze;
	}

	//Offline用に
	public void setFlag(final Program program, final int allSlice){
		if(program == Program.BTO){
			offline = true;
			this.allSlice = allSlice;
		}
	}

	//binは使用しない
	public boolean binUsed(){
		return false;
	}

	//OnlineExecuterのポインタを渡す
	public void setEndAnalyze(boolean[] endAnalyze){
		this.endAnalyze = endAnalyze;
	}

	//画像を加工
	public void analyzeImage(ImageProcessor currentIp) {
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		if (xyaData != null)
			prevXyaData = xyaData[0];
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG,	prevXyaData);
	}

	//MainArea.roiから右端のX座標を取得する
	public void createField(Rectangle rec) {
		rightmostX = rec.getWidth(); // 右側面
		//lowerY = rec.getHeight(); // 下側面
	}

	//ここでスレッドを開始させる
	public void setStart(boolean set){
		isGoal = false;
		spc = new Slip();
		spc.start();
	}

	//解析
	public void calculate(final int currentSlice) {
		this.currentSlice = currentSlice;	//interruptAnalysis()用
		//slicePerMovement++;
		if (currentSlice > 0) {
			currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
			//マウスが動かない場合
			/*if(setFreeze && (currentDistance <= movementCriterion/rate)){
				//distanceF += currentDistance;
				//conseqFreeze++;
			}*/
			//マウスが移動した場合距離を加えていく
			//currentDistance > movementCriterion/rate
			if((currentDistance*rate) > movementCriterion){
		        distanceP += currentDistance;
		        slicePerMovement++;

		        /*//動かなかったとみなした間の移動距離がmovementCriterionを超えた場合
				if(distanceF > movementCriterion){
				  distanceP += distanceF; 
				  slicePerMovement += conseqFreeze;
				  //初期化
				  distanceF = 0.0; conseqFreeze = 0.0;
				}*/
				setFreeze = false;
		    }
			//マウスが移動した後、動かなくなったら要素を追加
			//currentDistance > movementCriterion/rate
		    if(!setFreeze && ((currentDistance*rate) <= movementCriterion)){
		    	double durationP = (double)slicePerMovement/rate;
		    	double speedP = distanceP/durationP;
		    	setPhase(speedP,distanceP, durationP);
		    	//初期化
		    	distanceP = 0.0; slicePerMovement = 0;
		    	//次に動くまでこれは行わない
		    	setFreeze = true;
		    }


		    //GoalLine(GoalArea.roi)の左端のX座標を超えたらGoalTimeをセット
			if (!offline && xyaData[0][X_CENTER] >= (rightmostX-goalArea)) {
				time = ((double)(currentSlice + 1) / rate);
				//セットするのは一度のみ
				setGoal();
			}
			//GoalTimeをセットした場合解析を終了

			//traceを描く
			((Tracer)tracer).writeTrace(prevXyaData[X_CENTER], prevXyaData[Y_CENTER],	xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

			if(offline && currentSlice == allSlice)
				spc.end();
			//制限時間が来たらスレッドを終了させる
			if(!offline && currentSlice == duration*rate)
				spc.end();
		}
	}

	//結果をセット
	private void setPhase(final double speedP,final double distanceP, final double durationP){
    	speedPerMovement.add(speedP);
    	distancePerMovement.add(distanceP);
    	durationPerMovement.add(durationP);
	}

	//この辺は正直よく分かっていない
	public void interruptAnalysis(){
		if(currentSlice == 0)
			state = NOTHING;
		else if(currentSlice == (rate*duration)+1)
			state = COMPLETE;
		else{
			state = INTERRUPT;
		}
	}

	private void setGoal(){
		isGoal = true;
	}

	//解析を終了させる
	public boolean isGoal(){
		return isGoal;
	}

	public ImageProcessor getSubtractImage() {
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	//この辺はどうせ実験中に画面を見れないので適当で
	public String getInfo(final String subjectID, final int sliceNum) {
		return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + slipedTime.size();
	}

	public String getXY(final int sliceNum) {
		String xyData = (sliceNum + 1) + "\t" + xyaData[0][X_CENTER] + "\t"	+ xyaData[0][Y_CENTER] + "\t"
				          + (xyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(xyaData[0][AREA]));
		return xyData;
	}

	public String[] getResults() {
		double durationOfAnalysis;
		double totalDistance = 0.0;
		double averageSpeed = 0.0;
		double movingSpeed = 0.0;
		double totalMovementDuration = 0.0;
		int movingEpisodeNumber = speedPerMovement.size();

		//終了条件ごとに分ける必要がある
		if(time != 0.0)        durationOfAnalysis=time;
		else if(state == INTERRUPT)durationOfAnalysis=(double)currentSlice/rate;
		else                       durationOfAnalysis=duration;

		if(offline)durationOfAnalysis = (double)(allSlice-2)/rate;

		//最後に立ち止まってからそれまでの分を加える必要がある
		if(slicePerMovement != 0 && distanceP != 0.0){
			double durationP = (double)slicePerMovement/rate;
	    	double speedP = distanceP/durationP;
	    	setPhase(speedP,distanceP, durationP);
	    	movingEpisodeNumber = speedPerMovement.size();
		}

		//制限時間で終了して、1回も立ち止まらなかった場合
		if(movingEpisodeNumber == 0 && distanceP != 0.0){
			double speedP = distanceP/durationOfAnalysis;
			setPhase(speedP,distanceP,durationOfAnalysis);
	    	movingEpisodeNumber = speedPerMovement.size();
		}

		//TotalDistance(cm)
		for(Iterator<Double> i=distancePerMovement.iterator(); i.hasNext();){
	       totalDistance += i.next();
		}

		//AverageSpeed(cm/s)
		averageSpeed = totalDistance/durationOfAnalysis;

		//TotalMovementDuration(sec)
		for(Iterator<Double> i=durationPerMovement.iterator(); i.hasNext();){
			totalMovementDuration += i.next();
		}

		//MovingSpeed(cm/s)
		if(totalMovementDuration == 0){
			movingSpeed = 0.0;
		}else{
		    movingSpeed = totalDistance/totalMovementDuration;
		}

		//小数点第2位以下は四捨五入される
		//""+　はString.ValueOf()と同じ役割
		int i = offline ?7:9;
		String[] results = new String[i];
		results[0] = "" + Math.round(totalDistance*10.0)/10.0;
		results[1] = "" + Math.round(averageSpeed*10.0)/10.0;
		results[2] = "" + Math.round(movingSpeed*10.0)/10.0;
		results[3] = "" + movingEpisodeNumber;
		results[4] = "" + Math.round(totalMovementDuration*10.0)/10.0;
		//OnlineとOfflineで出力結果が異なる
		if(!offline){
		  results[5] = "" + slipedTime.size();  //SlipCount
		  results[6] = "" + Math.round(durationOfAnalysis*10.0)/10.0;
		  results[7] = button ?"Button":"Analysis";
		  results[8] = "" + duration;
		}else{
		  results[5] = "" + Math.round(durationOfAnalysis*10.0)/10.0;
		  results[6] = "" + duration;
	    }

		return results;
	}

	//要素が一定でない物で個別に出力するもののデータを渡す
    public List<String> getRespectiveResults(final int option) {
		int length = 4;
		List<String> result = new ArrayList<String>();
		for(int num = 0; num < length; num++){
			switch(option){
			case DISTANCE_PER_MOVEMENT:
				for(Iterator<Double> i=distancePerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			case DURATION_PER_MOVEMENT:
				for(Iterator<Double> i=durationPerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			case SLIPED_TIME: 
				for(Iterator<Double> i=slipedTime.iterator();i.hasNext();){
				    result.add("" + Math.round(i.next()*10.0)/10.0);
			        i.remove();
		        }
				break;
			case SPEED_PER_MOVEMENT:
				for(Iterator<Double> i=speedPerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}

		if(state == INTERRUPT)
			result.add("(" + currentSlice + "frame)");
		return result;
	}

	@Override
	public String[] getBinResult(int option){return null;}
	@Override
	public void nextBin(){}
	@Override
	public void resetDuration(int duration){}

	//fellTimeにはボタン1、slipedTimeにはボタン2を使用
	//終了させる場合はsetEndAnalyze()へ
	class Slip extends Thread{
		private boolean isRunnable;
		private long startTime;
		private InputController input;
		private boolean twoPushed;
		private boolean setOver;

		protected Slip() {
			isRunnable = true;
			if (input != null)
				input.close();
			input = InputController.getInstance(InputController.PORT_IO);
		}

		public void run(){
			startTime = System.currentTimeMillis();
			while (isRunnable) {
				try {
					//実験開始にもボタン1を使用するため
					if(!setOver && input.getInput(0))
						setOver = true;

					//if(!onePushed &&setOver && value == 1){
					//ボタン1を押した場合解析を終了
					if(setOver && !offline && input.getInput(0)){
						isRunnable = false;
						setGoal();
						time = (double)(System.currentTimeMillis()-startTime)/1000;
						button = true;
					}

                    //slipedTimeをセット
                    if(!twoPushed && input.getInput(1)){
                    	double x = (double)(System.currentTimeMillis()-startTime)/1000;
                    	slipedTime.add(Math.round(x*10.0)/10.0);
                    	twoPushed = true;
                    }
                    //長押し防止
                    if(twoPushed && !input.getInput(1))
                    	twoPushed = false;

                    Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		protected void end() {
			isRunnable = false;
		}
	}
}