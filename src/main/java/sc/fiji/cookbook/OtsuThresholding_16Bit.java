/*
 * Otsu Thresholding algorithm
 *
 * Copyright (c) 2003 by Christopher Mei (christopher.mei@sophia.inria.fr)
 *                    and Maxime Dauphin
 *
 * This plugin is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this plugin; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package sc.fiji.cookbook;

import java.text.DecimalFormat;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

/**
 *  This algorithm is an implementation of Otsu thresholding technique 
 *  based on the minimization of inter-class variance [otsu79].
 *
 *  @Article{otsu79,
 *    author =       "N. Otsu",
 *    title =        "A threshold selection method from gray level
 *                    histograms",
 *    journal =      "{IEEE} Trans. Systems, Man and Cybernetics",
 *    year =         "1979",
 *    volume =       "9",
 *    pages =        "62--66",
 *    month =        mar,
 *    keywords =     "threshold selection",
 *    note =         "minimize inter class variance",
 *  }
 *  
 **/

public class OtsuThresholding_16Bit implements PlugInFilter {
    private int threshold;
    final static int HMIN = 0;
    static int HMAX = 65536;
    static boolean is16bit = true;
    DecimalFormat df0 = new DecimalFormat("##0");
	
    public int setup(String arg, ImagePlus imp) 
    	{
	if (arg.equals("about"))
		{showAbout(); return DONE;}
	return DOES_16+DOES_STACKS+SUPPORTS_MASKING+NO_CHANGES;
  	}
    
    public void run(ImageProcessor ip)
	 {
	boolean debug = false;
	int intMax = (int)ip.getMax();
 	double percentageDone = 0;
	int width =  ip.getWidth();
	int height = ip.getHeight();

	GrayLevelClass2.N = width*height;
	GrayLevelClass2.probabilityHistogramDone = false;
	GrayLevelClass2 C1 = new GrayLevelClass2((ShortProcessor) ip, true);
	GrayLevelClass2 C2 = new GrayLevelClass2((ShortProcessor) ip, false);

	float fullMu = C1.getOmega()*C1.getMu()+C2.getOmega()*C2.getMu();
	//IJ.write("Full Omega : "+fullMu);
	double sigmaMax = 0;
	int threshold = 0;

	/** Start  **/
	for(int i=0 ; i<intMax ; i++)
		 {
		if (IJ.escapePressed()) 
			{IJ.beep();  return;}
		percentageDone = 100*((double)i/(double)intMax );
		IJ.showStatus("Percentage Done: "+df0.format(percentageDone )+"%...Esc' to abort");
		IJ.showProgress(i,intMax );
		double sigma = C1.getOmega()*(Math.pow(C1.getMu()-fullMu,2))+C2.getOmega()*(Math.pow(C2.getMu()-fullMu,2));
	   
		if(sigma>sigmaMax) 
			{
			sigmaMax = sigma;
			threshold = C1.getThreshold();
	    		}
		C1.addToEnd();
		C2.removeFromBeginning();
		}
	ImagePlus imp = NewImage.createByteImage ("Threshold", width, height, 1, NewImage.FILL_WHITE);
	ImageProcessor nip = imp.getProcessor();
	
	short[] pixels = (short[]) ((ShortProcessor) ip).getPixels();
	int offset = 0;
	for (int y=0; y<height; y++) {
	    offset = y*width;
	    for (int x=0; x<width; x++) {
		if ((pixels[offset + x] ) <= threshold)
		    nip.putPixel(x, y, 0);
	    }
        }
	//imp.show();
	//IJ.showMessage("Found threshold : "+threshold);
	IJ.setThreshold(threshold,intMax);
    }
    
    void showAbout() {
	IJ.showMessage("About OtsuThresholding_...",
		       "This plug-in filter calculates the OtsuThresholding of a 16-bit image.\n"
		       );
    }
}

