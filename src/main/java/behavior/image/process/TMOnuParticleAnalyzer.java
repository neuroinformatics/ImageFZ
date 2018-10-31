/*
 * Created on 2006/06/29
 *
 * 
 */
package behavior.image.process;

import java.util.Arrays;

import ij.ImagePlus;
import ij.measure.Measurements;
import ij.measure.ResultsTable;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ImageProcessor;

/**
 * @author kazuaki kobayashi
 *
 *  複数のピクセルを認識したときは、isExistはfalseを返す
 *  NewResultsCalculator内で利用(packeage内からのみアクセス可能）
 */
/*
 * 2012.11現在使用しているTMのためのもの。OnlineとOfflineでの違いを少なくするためにOnlineで使用しているものを移植した。
 */
class TMOnuParticleAnalyzer implements Measurements{
    
    private ParticleAnalyzer analyzer;
    private ParticleAnalyzer areaAnalyzer;
    private ResultsTable resultsTable;
    private ResultsTable areaResultsTable;
    
    /**
     * 
     * @param minSize  particleとして認識する最小サイズ
     * @param maxSize  particleとして認識する最大サイズ
     */
    TMOnuParticleAnalyzer(double minSize,double maxSize){
        //this.minSize = minSize;
        //this.maxSize = maxSize;
        this.resultsTable = new ResultsTable();
        this.areaResultsTable = new ResultsTable();
        int options = 0;  //
        this.analyzer = new ParticleAnalyzer(options,Measurements.CENTROID,resultsTable,minSize,maxSize);
        this.areaAnalyzer = new ParticleAnalyzer(options,Measurements.AREA,areaResultsTable,minSize,maxSize);
    }
    
   int[][] analyzeParticle(ImagePlus imp,ImageProcessor processor){
	    int[][] point = new int[1][4];

	    this.resultsTable.reset();
        this.areaResultsTable.reset();
        this.analyzer.analyze(imp,processor.duplicate());
        
		if(resultsTable.getCounter() == 0){
		    point[0][0] = 0;
		    point[0][1] = 0;
		    point[0][2] = 0;
		    point[0][3] = 0;
		    return point;
		}

        float[] dx = resultsTable.getColumn(ResultsTable.X_CENTROID);//全パーティクルの真ん中のX座標
		float[] dy = resultsTable.getColumn(ResultsTable.Y_CENTROID);//全パーティクルの真ん中のY座標

		areaAnalyzer.analyze(imp,processor.duplicate());
		float[] dArea = areaResultsTable.getColumn(ResultsTable.AREA);//全パーティクル各面積
		int index = this.getMaxAreaIndex(dArea);

		point[0][0] = Math.round(dx[index]);;
	    point[0][1] = Math.round(dy[index]);
	    point[0][2] = Math.round(dArea[index]);
	    point[0][3] = 1;

		return point;
   }
    
	private int getMaxAreaIndex(float[] dArea){
		float[] pArea = dArea;
		Arrays.sort(pArea);
		float maxValue = pArea[pArea.length-1];
		int index = 1;
		for(int i=0;i<dArea.length;i++){
			int equal = Float.compare(maxValue,dArea[i]);//二つのfloat値を比較。等しいときは0。
			if(equal==0){
				index = i;
				break;
			}
		}
		return index;
	}
}
