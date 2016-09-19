/*
 * Otsu Thresholding algorithm
 *
 * Copyright (c) 2003 by Christopher Mei (christopher.mei@sophia.inria.fr)
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

import ij.process.ByteProcessor;

/** 
 *  This class implements a class of pixels with basic statistical functions.
 **/

public class GrayLevelClass {
    private static float[] probabilityHistogram;
    public static boolean probabilityHistogramDone;
    public static int N;

    private int index;
    private float omega;
    private float mu;

    public GrayLevelClass(ByteProcessor img, boolean first) {
	if(!probabilityHistogramDone) {
	    int[] histogram = img.getHistogram();
	    probabilityHistogram = new float[256];
	    
	    for(int i=0; i<256 ; i++) {
		probabilityHistogram[i] = ((float) histogram[i])/((float) N);
		//IJ.write(" "+probabilityHistogram[i]);
	    }
	    probabilityHistogramDone = true;
	}

	if(first) {
	    index = 1;
	    omega = probabilityHistogram[index-1];
	    if(omega == 0)
		mu = 0;
	    else
		mu =  1*probabilityHistogram[index-1]/omega;
	}
	else {
	    index = 2;
	    omega = 0;
	    mu = 0;
	    for(int i=index; i<256 ; i++) {
		omega +=  probabilityHistogram[i-1];
		mu +=  probabilityHistogram[i-1]*i;
	    }
	    if(omega == 0)
		mu = 0;
	    else
		mu /= omega;
	}
    }

    public void removeFromBeginning() {
	index++;
	mu = 0;
	omega = 0;

	for(int i=index; i<256 ; i++) {
	    omega +=  probabilityHistogram[i-1];
	    mu +=  i*probabilityHistogram[i-1];//i*
	}
	if(omega == 0)
	    mu = 0;
	else
	    mu /= omega;
	/*mu *= omega;
	  mu -= probabilityHistogram[index-2];//(index-1)*
	  omega -= probabilityHistogram[index-2];
	  if(omega == 0)
	  mu = 0;
	  else
	  mu /= omega;*/
    }

    public void addToEnd() {
	index++;
	mu = 0;
	omega = 0;
	for(int i=1; i<index ; i++) {
	    omega +=  probabilityHistogram[i-1];
	    mu +=  i*probabilityHistogram[i-1];
	}
	if(omega == 0)
	    mu = 0;
	else
	    mu /= omega;
	/*mu *= omega;
	  omega += probabilityHistogram[index-1];
	  mu += probabilityHistogram[index-1];//index*
	  if(omega == 0)
	  mu = 0;
	  else
	  mu /=omega;
	*/
    }

    @Override
    public String toString() {
	StringBuffer ret = new StringBuffer();
	
	ret.append("Index : "+index+"\n");
	ret.append("Mu : "+mu+"\n");
	ret.append("Omega : "+omega+"\n");
	/*for(int i=0; i<10; i++) {
	    ret.append( "\n" );
	    }*/
	
	return ret.toString();
    }

    public float getMu() {
	return mu;
    }

    public float getOmega() {
	return omega;
    }

    public int getThreshold() {
	return index;
    }
}

