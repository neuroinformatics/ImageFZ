package behavior.plugin.analyzer;

import java.util.ArrayList;

import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.image.process.OFCEpisodeTracer;
import behavior.setup.parameter.OFCParameter;
import behavior.setup.parameter.OFParameter;
import behavior.setup.parameter.Parameter;

public class EpisodeCalculator{
	private ArrayList<Episode> episodeList = new ArrayList<Episode>(); //エピソード配列
	private int id = 1; //現在のエピソードID
	private int[] prevPlot, prev2Plot; //一フレーム前の座標
	private boolean isRecoadingEpisode = false; //現在どのエピソードも始まっていない状態のとき true

	/*パラメータ*/
	private final int ANGLE_MARGIN = Parameter.getInt(OFCParameter.angleMargin);
	private final int RESET_TIME = Parameter.getInt(OFCParameter.resetImmobilityTime)*Parameter.getInt(Parameter.rate);
	private final int PIXEL_WIDTH, PIXEL_HEIGHT;

	/*現エピソードの状況値*/
	private int[] startPlot; //開始地点のプロット
	private double dist = 0;
	private int freezTime = 0; 
	private boolean outOfRoi = false; //現在(前回)Roi の外に出ているか

	private OFCEpisodeTracer tracer;

	private ImageStack traceStack;

	public EpisodeCalculator(ImageProcessor backIp){
		PIXEL_WIDTH = backIp.getWidth();
		PIXEL_HEIGHT = backIp.getHeight();

		double pixelOfCm = (((double)PIXEL_WIDTH/Parameter.getInt(Parameter.frameWidth) +
				                 (double)PIXEL_HEIGHT/Parameter.getInt(Parameter.frameHeight)))/2;
		int roiRadius = (int)(pixelOfCm * Parameter.getDouble(OFCParameter.roiRadius));
		tracer = new OFCEpisodeTracer(backIp,roiRadius);

		traceStack = new ImageStack(PIXEL_WIDTH, PIXEL_HEIGHT);
	}

	/**次のプロットを受け取り計算*/
	public void setNextPlot(int[] xyData){
		if(!isRecoadingEpisode){
			if(prevPlot!=null && Parameter.getDouble(OFParameter.movementCriterion)<=getDistance(xyData[0], xyData[1], prevPlot[0], prevPlot[1])){
				startPlot = prevPlot;

				if(id!=1){
					if(Parameter.getBoolean(OFCParameter.checkModeSave)){			
				        traceStack.addSlice("", tracer.getRoiTrace(startPlot).convertToByte(false).duplicate());
					}else{
						traceStack.addSlice("", tracer.getTrace().convertToByte(false).duplicate());
					}
				    tracer.clearTrace();
				}

				Episode ep = new Episode(id++);
				episodeList.add(ep);
				isRecoadingEpisode = true;

				tracer.writeTrace(xyData[0], xyData[1]);
				tracer.writeState(Episode.State.START_POINT.getStateChar());
			}
		}else{
			Episode.State state = calculatePrevState(xyData,prevPlot,prev2Plot); //前回、ターンしたのか、直進したのか
			episodeList.get(episodeList.size()-1).addState(state);
			if(checkEnd(episodeList.get(episodeList.size()-1),xyData,prevPlot)){
				tracer.writeState(episodeList.get(episodeList.size()-1).isCircle()?Episode.State.CIRCLE.getStateChar():Episode.State.NOT_CIRCLE.getStateChar());

				clearVar();
				isRecoadingEpisode = false;
			}
		}
		prev2Plot = prevPlot;
		prevPlot = xyData;

		tracer.setPrevXY(xyData[0], xyData[1]);
	}

	private double getDistance(int sx,int sy,int dx,int dy){
		if(sx + sy == 0 || dx + dy == 0)
			return 0.0;
		if(PIXEL_WIDTH == 0 || PIXEL_HEIGHT == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		float distX = sx - dx;
		float distY = sy - dy;
		double lengthPerPixelHor = (double)Parameter.getInt(Parameter.frameWidth) / PIXEL_WIDTH;
		double lengthPerPixelVer = (double)Parameter.getInt(Parameter.frameHeight) / PIXEL_HEIGHT;
		double distance = Math.sqrt(Math.pow(distX * lengthPerPixelHor, 2) + Math.pow(distY * lengthPerPixelVer, 2));
		return distance;
	}

	private Episode.State calculatePrevState(int[] xyData, int[] prevPlot, int[] prev2Plot){
		if(prevPlot == null){
			throw new IllegalStateException();
		}
		if(prev2Plot == null){
			return Episode.State.START_POINT;
		}

		double angle = getAngleGap(xyData, prevPlot, prev2Plot);
		if(angle< 90-ANGLE_MARGIN){
			return Episode.State.SHARP_LEFT;
		}else if(angle < 180-ANGLE_MARGIN){
			return Episode.State.OBTUSE_LEFT;
		}else if(angle <= 180+ANGLE_MARGIN){
			return Episode.State.STRAIGHT;
		}else if(angle <= 270+ANGLE_MARGIN){
			return Episode.State.OBTUSE_RIGHT;
		}else{
			return Episode.State.SHARP_RIGHT;
		}
	}

	private double getAngleGap(int[] xyData, int[] prevPlot, int[] prev2Plot){
		int[] vectorA = new int[2], vectorB = new int[2];
		vectorA[0] = xyData[0] - prevPlot[0];
		vectorA[1] = xyData[1] - prevPlot[1];
		vectorB[0] = prev2Plot[0] - prevPlot[0];
		vectorB[1] = prev2Plot[1] - prevPlot[1];

		double currentDegree = getAngle(vectorA[0], vectorA[1]);
//		System.out.println("current:" + currentDegree);
		double prevDegree = getAngle(vectorB[0], vectorB[1]);
//		System.out.println("prev:" + prevDegree);

		if(currentDegree-prevDegree >= 0){
			return currentDegree-prevDegree;
		}else{
			return 360.0+currentDegree-prevDegree;
		}
	}

	private double getAngle(int x, int y){
		double hypotenuse = Math.sqrt(x*x+y*y);
		double sin = (double)y/hypotenuse;
		double cos = (double)x/hypotenuse;

		double sinAngle1, sinAngle2;
		double cosAngle1, cosAngle2;

		sinAngle1 = Math.asin(sin);
		if(sinAngle1 >= 0){
			sinAngle2 = sinAngle1 + (Math.PI / 2 - sinAngle1) * 2;
		}else{
			sinAngle1 += Math.PI * 2;
			sinAngle2 = sinAngle1 - (sinAngle1 - Math.PI * 3 / 2) * 2;
		}
//		System.out.println("sin1:" + Math.toDegrees(sinAngle1) + "  sin2:"+ Math.toDegrees(sinAngle2));

		cosAngle1 = Math.acos(cos);
		cosAngle2 = cosAngle1 + (Math.PI - cosAngle1) * 2;
//		System.out.println("cos1:" + Math.toDegrees(cosAngle1) + "  cos2:"+ Math.toDegrees(cosAngle2));

		if(Math.min(Math.abs(sinAngle1 - cosAngle1), Math.abs(sinAngle1 - cosAngle2))
				<= Math.min(Math.abs(sinAngle2 - cosAngle1), Math.abs(sinAngle2 - cosAngle2))){
			return Math.toDegrees(sinAngle1);
		}
		else{
			return Math.toDegrees(sinAngle2);
		}
	}

	private boolean checkEnd(Episode ep, int[] xyData, int[] prevPlot){
		if(prevPlot == null) throw new IllegalStateException();

		double currentDist = getDistance(xyData[0], xyData[1], prevPlot[0], prevPlot[1]);
		if(currentDist < Parameter.getDouble(OFParameter.movementCriterion)){
			freezTime++;
			if(freezTime/Parameter.getInt(Parameter.rate) >= RESET_TIME){
				ep.endProcess(Episode.State.IMMOBILE);
				return true;
			}
		}else{
			freezTime = 0;
		}
		dist += currentDist;
		if(dist >= Parameter.getDouble(OFCParameter.resetDist)){
			ep.endProcess(Episode.State.REACHED_MAX_DIST);
			return true;
		}

		double distFromStart = getDistance(xyData[0], xyData[1], startPlot[0], startPlot[1]);
		if(distFromStart<Parameter.getDouble(OFCParameter.roiRadius)){
			if(outOfRoi){
				ep.endProcess(Episode.State.REACHED_ROI);
				return true;
			}
		}else{
			outOfRoi = true;
		}

		return false;
	}

	private void clearVar(){
		dist = 0;
		freezTime = 0;
		outOfRoi = false;
		prev2Plot = null;
		startPlot = null;
	}

	public ArrayList<Episode> getEpisodeList(){
		if(episodeList.get(episodeList.size()-1).doesNotEnd()){
			episodeList.get(episodeList.size()-1).endProcess(Episode.State.INTERRUPT);
			tracer.writeState(episodeList.get(episodeList.size()-1).isCircle()?Episode.State.CIRCLE.getStateChar():Episode.State.NOT_CIRCLE.getStateChar());

			if(Parameter.getBoolean(OFCParameter.checkModeSave)){
		        traceStack.addSlice("", tracer.getRoiTrace(startPlot).convertToByte(false).duplicate());
			}else{
				traceStack.addSlice("", tracer.getTrace().convertToByte(false).duplicate());
			}
		}
		return episodeList;
	}

	public ImageProcessor getEpisodeTrace(){
		return Parameter.getBoolean(OFCParameter.checkMode)?tracer.getRoiTrace(startPlot):tracer.getTrace();
	}

	public ImageStack getTraceStack(){
		return traceStack;
	}
}