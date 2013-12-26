import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**
 * This class interfaces the VolumeJ package to a ImageJ plugin.
 *
 * Copyright (c) 1999-2002, Michael Abramoff. All rights reserved.
 * @author: Michael Abramoff
 *
 * Small print:
 * Permission to use, copy, modify and distribute this version of this software or any parts
 * of it and its documentation or any parts of it ("the software"), for any purpose is
 * hereby granted, provided that the above copyright notice and this permission notice
 * appear intact in all copies of the software and that you do not sell the software,
 * or include the software in a commercial package.
 * The release of this software into the public domain does not imply any obligation
 * on the part of the author to release future versions into the public domain.
 * The author is free to make upgraded or improved versions of the software available
 * for a fee or commercially only.
 * Commercial licensing of the software is available by contacting the author.
 * THE SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS, IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY
 * WARRANTY OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 */
public class Depth_Coded_Stack_ implements PlugIn
{
	private int width, height, depth;

	public void run(String arg)
	{
		ImagePlus imp = WindowManager.getCurrentImage();
		if ((imp instanceof ImagePlus) && imp.getStackSize() > 1) {
			width = imp.getWidth();
			height = imp.getHeight();
			depth = imp.getStackSize();
		}

		GenericDialog gd = new GenericDialog("Create Depth-coded stack (slices have increasing pixel value (0->255))", IJ.getInstance());
		gd.addNumericField("Width (pixels):", width, 0);
		gd.addNumericField("Height (pixels):", height, 0);
		gd.addNumericField("Depth (pixels):", depth, 0);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return;
		}

		width = (int)gd.getNextNumber();
		height = (int)gd.getNextNumber();
		depth = (int)gd.getNextNumber();

		IJ.write("Depth-coded stack (" + width + "x" + height + "x" + depth + ")");
		ImageStack s = new ImageStack(width, height);
		for (int i = 0; i < depth; i++) {
			ImageProcessor ip = new ByteProcessor(width, height);
			byte pixels[] = (byte[])(byte[])ip.getPixels();
			for (int j = 0; j < ip.getWidth(); j++) {
				for(int k = 0; k < ip.getHeight(); k++) {
					pixels[k * ip.getWidth() + j] = (byte)((i * 255) / (depth - 1));
				}

			}

			s.addSlice("", ip);
		}

		new ImagePlus("depth_coded", s).show();
	}
}
