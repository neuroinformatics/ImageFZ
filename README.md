# ImageFZ - Fear conditioning test plugin for ImageJ

ImageFZ is an ImageJ plugin for the fear conditioning test.
This program can work with Image J 1.46 or above on Windows 7 or 10 (32-bit).


## How to build

### prepare
Download java inteface for LabJack U12

```shellscript
 $ curl -LO https://labjack.com/sites/default/files/2012/08/LabJackJavaV40.zip
 $ unzip -p LabJackJavaV40.zip labjack.jar > lib/labjack.jar
```

Download java package for Scion Frame Grabber FG-7

```shellscript
 $ curl -LO https://imagej.nih.gov/ij/download/tools/ScionDrivers.zip
 $ unzip -p ScionDrivers.zip ScionImageJDrivers/FG-7/ImageJ/SFG_ImageJ_Update64.exe > SFG_ImageJ_Update64.exe
 $ cabextract -p -F scion.jar SFG_ImageJ_Update64.exe > lib/scion.jar 
```

Download java interface for Apple QuickTime 7

http://www.apple.com/quicktime/download/

```shellscript
 $ curl -OL https://secure-appldnld.apple.com/QuickTime/031-43075-20160107-C0844134-B3CD-11E5-B1C0-43CA8D551951/QuickTimeInstaller.exe
 $ cabextract -F QuickTime.msi ./QuickTimeInstaller.exe
 $ msiinfo extract ./QuickTime.msi QuickTime.cab > QuickTime.cab
 $ cabextract -p -F QTJava.zip ./QuickTime.cab > lib/QTJava.zip


### compile
Build jar package using Maven

```shcellscript
 $ mvn package
 $ file target/behavior_FZ130121.jar
```

## Binary distribution

http://www.mouse-phenotype.org/software.html

Distributed by Keizo Takao and Tsuyoshi Miyakawa.

In publication of data that is analyzed with ImageFZ, cite the following article that describes the method of the fear conditioning test using this software.

Shoji H, Takao K, Hattori S, Miyakawa T. Contextual and cued fear conditioning test using a video analyzing system in Mice. J. Vis. Exp. (85), doi:10.3791/50871 (2014).

