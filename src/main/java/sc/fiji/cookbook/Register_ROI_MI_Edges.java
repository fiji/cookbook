package sc.fiji.cookbook;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import ij.IJ;
import ij.process.ImageProcessor;
import registration.RegisterMI;
import volume.Hessian;

/**
 * This class interfaces the VolumeJ package to a ImageJ plugin. Copyright (c)
 * 1999-2002, Michael Abramoff. All rights reserved.
 * 
 * @author: Michael Abramoff Small print: Permission to use, copy, modify and
 *          distribute this version of this software or any parts of it and its
 *          documentation or any parts of it ("the software"), for any purpose
 *          is hereby granted, provided that the above copyright notice and this
 *          permission notice appear intact in all copies of the software and
 *          that you do not sell the software, or include the software in a
 *          commercial package. The release of this software into the public
 *          domain does not imply any obligation on the part of the author to
 *          release future versions into the public domain. The author is free
 *          to make upgraded or improved versions of the software available for
 *          a fee or commercially only. Commercial licensing of the software is
 *          available by contacting the author. THE SOFTWARE IS PROVIDED "AS IS"
 *          AND WITHOUT WARRANTY OF ANY KIND, EXPRESS, IMPLIED OR OTHERWISE,
 *          INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY OR
 *          FITNESS FOR A PARTICULAR PURPOSE.
 */
public class Register_ROI_MI_Edges extends Register_ROI
{

	@Override
	public void run(final ImageProcessor ip) {
		if (super.imp.getStackSize() < 2) {
			IJ.showMessage("Registration", "This command requires a stack.");
			return;
		}
		try {
			extractRegImages(super.imp);
			final float edges[][] = new float[super.a.length][];
			IJ.showStatus("Extracting edges... (" + super.a.length + ")");
			for (int j = 0; j < super.a.length; j++)
				edges[j] = Hessian.largest(super.a[j], super.width, 2D);

			IJ.showStatus("Starting MI reg.. (" + super.a.length + ")");
			final registration.Register reg = new RegisterMI(edges[0], super.width);
			IJ.showStatus("Registering...");
			super.estimates = registerRegImages(reg, edges);
			plotEstimates(super.estimates);
		}
		catch (final Exception ex) {
			final CharArrayWriter c = new CharArrayWriter();
			ex.printStackTrace(new PrintWriter(c));
			IJ.error(c.toString());
		}
		return;
	}

	@Override
	void showAbout() {
		IJ.showMessage("About Register ROI MI Edges...",
			"Does edge based image registration using mutual information.\n.");
	}
}
