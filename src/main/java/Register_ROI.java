import bijnum.BIJutil;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PlotWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

import registration.Register;
import registration.RegisterFFT;
import volume.Transformer;

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
public class Register_ROI implements PlugInFilter {

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		else {
			return DOES_ALL & ~DOES_8C;
		}
	}

	@Override
	public void run(final ImageProcessor ip) {
		if (imp.getStackSize() < 2) {
			IJ.showMessage("Registration", "This command requires a stack.");
			return;
		}
		try {
			extractRegImages(imp);
			final Register reg = new RegisterFFT(a[0], width);
			estimates = registerRegImages(reg, a);
			plotEstimates(estimates);
		}
		catch (final Exception ex) {
			final CharArrayWriter c = new CharArrayWriter();
			ex.printStackTrace(new PrintWriter(c));
			IJ.error(c.toString());
		}
		return;
	}

	protected void extractRegImages(final ImagePlus imp) {
		a = BIJutil.matrixFromImageStackRoi(imp);
		width = BIJutil.getMatrixWidth(imp);
		height = a[0].length / width;
	}

	protected float[][] registerRegImages(final Register reg, final float a[][]) {
		if (IJ.debugMode) {
			final ImageStack is1 = BIJutil.imageStackFromMatrix(a, width);
			final ImagePlus imp1 = new ImagePlus("regimages", is1);
			imp1.show();
		}
		final ImageStack is = new ImageStack(imp.getWidth(), imp.getHeight());
		is.addSlice("reference slice", imp.getStack().getProcessor(1));
		ImagePlus newimp = null;
		final float estimates[][] = new float[a.length - 1][];
		for (int j = 1; j < a.length; j++) {
			IJ.showProgress(j, a.length);
			IJ.showStatus("Registering " + j + "/" + a.length);
			estimates[j - 1] = reg.register(a[j]);
			final ImageProcessor registered =
				Transformer.transform(imp.getStack().getProcessor(j + 1),
					estimates[j - 1]);
			is.addSlice("", registered);
			if (j == 1) {
				newimp = new ImagePlus("Registered " + imp.getTitle(), is);
				newimp.show();
			}
			else {
				newimp.setStack(null, is);
				newimp.setSlice(j + 1);
			}
		}

		return estimates;
	}

	protected void plotEstimates(final float estimates[][]) {
		final float xValues[] = new float[estimates.length];
		for (int i = 0; i < xValues.length; i++) {
			xValues[i] = i + 1.0f;
		}

		final float yValues[] = new float[estimates.length];
		for (int i = 0; i < yValues.length; i++)
			yValues[i] = estimates[i][0];

		PlotWindow pw =
			new PlotWindow("x-translations " + imp.getTitle(), "slice",
				"x-translation (pixels)", xValues, yValues);
		pw.draw();
		for (int i = 0; i < yValues.length; i++)
			yValues[i] = estimates[i][1];

		pw =
			new PlotWindow("y-translations " + imp.getTitle(), "slice",
				"y-translation (pixels)", xValues, yValues);
		pw.draw();
	}

	void showAbout() {
		IJ.showMessage("About this plugin...",
			"Registers a stack of images to the first slice using FFT cross-correlation");
	}

	protected ImagePlus imp;
	protected int width;
	protected int height;
	protected float a[][];
	protected float estimates[][];
}
