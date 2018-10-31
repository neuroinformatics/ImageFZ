package behavior.image;

import ij.ImagePlus;

public abstract class AbstractCapture {
	abstract ImagePlus capture();
	abstract void close();
}
