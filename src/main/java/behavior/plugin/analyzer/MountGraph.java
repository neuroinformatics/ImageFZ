package behavior.plugin.analyzer;

import java.util.Arrays;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.CSIParameter;

import ij.process.ImageProcessor;

/**
 * MountGraphの改訂版
 * 配列を用いて、画素を移す
 * グラフの解析にも利用
 * @author tt
 *
 */
public class MountGraph {
	private int[][] graph;
	private ImageProcessor graphIp;

	/**
	 * 利用する、Byte配列を初期化
	 */
	public MountGraph(ImageProcessor backIp){
		graph = new int[backIp.getWidth()][backIp.getHeight()];
		for(int i=0;i<backIp.getWidth();i++){
			Arrays.fill(graph[i],0);
		}

		graphIp = backIp.createProcessor(backIp.getWidth(),backIp.getHeight());
	}

	public synchronized ImageProcessor createGraph(ImageProcessor currentIp){
		try{
		    for(int w=0;w<currentIp.getWidth();w++){
			    for(int h=0;h<currentIp.getHeight();h++){
				    if(currentIp.getPixel(w,h)>0){
					    graph[w][h]+=Parameter.getInt(CSIParameter.mountDensity);
				    }
			    }
		    }

		    for(int i=0;i<currentIp.getWidth();i++){
			    for(int j=0;j<currentIp.getHeight();j++){
					if(graphIp.getPixel(i,j) < 255){
						graphIp.putPixel(i,j,graph[i][j]);
					}
			    }
		    }
		}catch(Exception e){
			e.printStackTrace();
			e.getStackTrace();
		}

		return graphIp;
	}

	public ImageProcessor getMountGraph(){
		/*int[] copy = graph.clone();
		Arrays.sort(copy);
		int[] graphBits = new int[copy.length];
		for(int i=0;i<graph.length;i++){
			graphBits[i] = (int)(((double)graph[i]*(255.0/copy[copy.length-1]) > 255)? 255:(double)graph[i]*(255.0/copy[copy.length-1]));
		}
		for(int w=0;w<graphIp.getWidth();w++){
			for(int h=0;h<graphIp.getHeight();h++){
				if(graphIp.getPixel(w,h)>0)
					graphIp.putPixel(w,h,graphBits[w+h*graphIp.getHeight()]);
			}
		}*/
		return graphIp;
	}
}