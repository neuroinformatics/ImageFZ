package behavior.image.process;

import ij.ImagePlus;
import ij.process.ImageProcessor;

public class TMImageManager extends ImageManager{
	private final TMOnuParticleAnalyzer tmonu;

	public TMImageManager(ImageProcessor backIp,int minSize,int maxSize){
		super(backIp);
		tmonu = new TMOnuParticleAnalyzer((double)minSize,(double)maxSize);
	}

	public synchronized int[][] analyzeParticleOnu(int type, int[] plot){
		ImageProcessor analyzeIp = getIp(type);

		return tmonu.analyzeParticle(new ImagePlus("",analyzeIp),analyzeIp);
	}
}
