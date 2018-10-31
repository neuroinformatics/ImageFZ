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
 *  �����̃s�N�Z����F�������Ƃ��́AisExist��false��Ԃ�
 *  NewResultsCalculator���ŗ��p(packeage������̂݃A�N�Z�X�\�j
 */
/*
 * 2012.11���ݎg�p���Ă���TM�̂��߂̂��́BOnline��Offline�ł̈Ⴂ�����Ȃ����邽�߂�Online�Ŏg�p���Ă�����̂��ڐA�����B
 */
class TMOnuParticleAnalyzer implements Measurements{
    
    private ParticleAnalyzer analyzer;
    private ParticleAnalyzer areaAnalyzer;
    private ResultsTable resultsTable;
    private ResultsTable areaResultsTable;
    
    /**
     * 
     * @param minSize  particle�Ƃ��ĔF������ŏ��T�C�Y
     * @param maxSize  particle�Ƃ��ĔF������ő�T�C�Y
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

        float[] dx = resultsTable.getColumn(ResultsTable.X_CENTROID);//�S�p�[�e�B�N���̐^�񒆂�X���W
		float[] dy = resultsTable.getColumn(ResultsTable.Y_CENTROID);//�S�p�[�e�B�N���̐^�񒆂�Y���W

		areaAnalyzer.analyze(imp,processor.duplicate());
		float[] dArea = areaResultsTable.getColumn(ResultsTable.AREA);//�S�p�[�e�B�N���e�ʐ�
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
			int equal = Float.compare(maxValue,dArea[i]);//���float�l���r�B�������Ƃ���0�B
			if(equal==0){
				index = i;
				break;
			}
		}
		return index;
	}
}
