/* 
 * $Id: Adapative3DThreshold_.java,v 1.19 2005/06/15 09:27:36 perchrh Exp $
 * 
 * Copyright (C) 2005 Per Christian Henden 
 * Copyright (C) 2005 Jens Bache-Wiig
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * The authors can be contacted by email to 
 * perchrh [at] pvv.org (Per Christian Henden) or 
 * bachewii [at] stud.ntnu.no (Jens Bache-Wiig) or 
 * by mail to 
 * Per Christian Henden, Rogalandsgata 167, 5522 Haugesund, Norway.
 */

package sc.fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.StackWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Adapative3DThreshold_ implements PlugInFilter {
	
	ImagePlus imRef;
	private boolean noGo;
	private int baseThreshold;
	private int radius;
	private double localWeight;
	
	private int width;
	private int height;
	private int depth;
	
	@Override
	public int setup(String arg, ImagePlus imp) {
		imRef = imp;
		
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		
		getParams();
		
		return DOES_8G;
	}
	
	private void getParams() {
		
		//set defaults
		baseThreshold = 127;
		localWeight = 5;
		int diameter = 3;
		
		GenericDialog gd = new GenericDialog("3D adaptive threshold configuration");
		
		gd.addNumericField("Base threshold", baseThreshold, 0);
		gd.addNumericField("Mask diameter (pixels)", diameter, 0);
		gd.addNumericField("Local weight (percent)", localWeight, 1);
		
		gd.showDialog();
		
		if (gd.wasCanceled()) {
			if (imRef != null) imRef.unlock();
			noGo = true;
		}
		
		baseThreshold = (int)gd.getNextNumber();
		radius = ((int)gd.getNextNumber() )/2;
		localWeight = gd.getNextNumber()/100;
	}
	
	
	@Override
	public void run(ImageProcessor ip) {
		
		if(noGo) return;
		
		width = ip.getWidth();
		height = ip.getHeight();
		depth = imRef.getStackSize();
		
		//create variable to store modificated image
		byte[][] imageCopy = new byte[depth][width*height];		
		
		//do the thresholding
		int value;
		int localValue;
		long sum;
		long count;
		int localThreshold;
		double localAverage;
		for (int z = 0; z < depth; z++){
			IJ.showProgress(z+1,depth-1);
			for (int y = 0; y < height; y++){
				for(int x = 0; x < width; x++){
					
					value = 0xff & ((byte[]) imRef.getStack().getPixels(z +1))[x + y * width];
					
					sum = 0;
					count = 0;
					localAverage = 0;
					for(int i=-radius; i<radius; i++){
						for(int j=-radius; j<radius; j++){
							for(int k=-radius; k<radius; k++){
								localValue = safeGet(z+k+1,x+i,y+j);
								
								if(localValue >= 0 ){ //if not outside mask
									sum+=(localValue-baseThreshold);
									count++;
								}
							}
						}
					}
					
					if(count > 0) localAverage = sum/count;
					
					localThreshold = (int) ( (1-localWeight)*baseThreshold - localWeight*localAverage +0.5 );
					
					if (value >= localThreshold){
						imageCopy[z][x+y*width] = (byte)255;
					}else{
						imageCopy[z][x+y*width] = (byte)0;	
					}
				}
			}
			
		}
		
		ImageStack newStack = new ImageStack(width, height);
		
		for (int i = 0; i < depth; i++) {
			byte[] newPixels = imageCopy[i];
			newStack.addSlice("Slice " + i, newPixels);
		}
		
		IJ.showProgress(1,1); //set to finished
		
		ImagePlus newImage = new ImagePlus("Adaptive3DThreshold - ", newStack);
		new StackWindow(newImage);
		//IJ.run("Invert");	
		IJ.setThreshold(1,255);
	}
	
	
	private int safeGet(int z, int x, int y){
		
		//Gets the value from the image, or if outside image return -1 
		
		int retval;
		
		try{
			retval = 0xff & ((byte[]) imRef.getStack().getPixels(z +1))[x + y * width];			
		}
		catch (Exception e){
			retval = -1;
			
		}
		
		return retval;
		
	}
	
	void showAbout() {
		IJ.showMessage( "About Adaptive 3D Threshold..",
				"This plugin thresholds a stack according to the threshold "
				+ "T = (1-w)*base - w*avg(radius^3 neighbour-base)");
	}
	
}	
