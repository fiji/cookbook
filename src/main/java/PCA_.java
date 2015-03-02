import bijnum.BIJpca;
import bijnum.BIJstats;
import bijnum.BIJutil;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.io.CharArrayWriter;
import java.io.PrintWriter;

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
public class PCA_ implements PlugInFilter {

	private BIJpca pca;
	private float psi[][];
	private ImagePlus imp = null;

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		else {
			this.imp = imp;
			return STACK_REQUIRED | NO_UNDO | DOES_ALL;
		}
	}

	@Override
	public void run(final ImageProcessor ip) {
		try {
			final float a[][] = BIJutil.matrixFromImageStack(imp);
			pca = new BIJpca();
			psi = pca.compute(a);
			IJ.showStatus("Putting eigenimages into stack");
			final ImagePlus newimp = mapEigenimages();
			newimp.show();
			mapSpectrum();
		}
		catch (final Exception e) {
			final CharArrayWriter c = new CharArrayWriter();
			e.printStackTrace(new PrintWriter(c));
			IJ.log(c.toString());
		}
	}

	void showAbout() {
		IJ.showMessage(
			"About PCA plugin...",
			"Principal component analysis of series of images.\nThe resulting images are normalized and sorted in decreasing eigenvalue order.\n(c) 1999-2003, Michael Abramoff, U of Iowa.\nhttp://bij.isi.uu.nl");
	}

	protected ImagePlus mapEigenimages() {
		final ImageStack is = new ImageStack(imp.getWidth(), imp.getHeight());
		for (int j = 0; j < psi.length; j++) {
			final ImageProcessor sp =
				new FloatProcessor(imp.getWidth(), imp.getHeight(), pca
					.getEigenImage(j), null);
			is.addSlice("variance=" + pca.eigenvalues[j], sp);
		}

		final ImagePlus newimp = new ImagePlus("PCA of " + imp.getTitle(), is);
		return newimp;
	}

	protected void mapSpectrum() {
		IJ.showStatus("Eigenvalue spectrum mapping");
		final float spectrum[] = BIJstats.spectrum(pca.eigenvalues);
		final float xValues[] = new float[spectrum.length];
		for (int i = 0; i < spectrum.length; i++) {
			xValues[i] = i + 1.0f;
		}

		final PlotWindow pw =
			new PlotWindow("Eigenvalue spectrum of " + imp.getShortTitle(),
				"component index", "fractional variance", xValues, spectrum);
		pw.draw();
	}

	protected void mapCoordinates() {
		ImageStack tis = null;
		final float an[][] = pca.getCoordinates();
		final float xValues[] = new float[an[0].length];
		for (int j = 0; j < xValues.length; j++) {
			xValues[j] = j + 1.0f;
		}

		for (int j = 0; j < an.length; j++) {
			final Plot plot = new Plot("", "", "", xValues, an[j]);
			final ImageProcessor tip = plot.getProcessor();
			if (j == 0) {
				tis = new ImageStack(tip.getWidth(), tip.getHeight());
			}
			tis.addSlice("j=" + (j + 1), tip);
		}

		final ImagePlus timp =
			new ImagePlus("Time coordinates of " + imp.getTitle(), tis);
		timp.show();
	}
}
