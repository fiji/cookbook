import bijnum.BIJShortMatrix;
import bijnum.BIJmi;
import bijnum.BIJstats;
import bijnum.BIJutil;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
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
public class MI_ implements PlugInFilter {

	private ImagePlus imp;
	private final boolean displayJH = false;

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
			IJ.showMessage("MI_",
				"This command requires a stack with at least two images.");
			return;
		}
		try {
			final short a[][] = BIJutil.shortMatrixFromImageStack(imp);
			final float minmax[] = BIJShortMatrix.minmax(a);
			final int min = (int) minmax[0];
			final int max = (int) minmax[1];
			final BIJmi mi = new BIJmi(min, max, BIJmi.getNiceScale(min, max));
			final float Cmi[] = new float[a.length];
			ImageStack is = null;
			ImagePlus newimp = null;
			if (displayJH) {
				is = new ImageStack(mi.getBins(), mi.getBins());
				newimp = null;
			}
			for (int j = 0; j < a.length; j++) {
				Cmi[j] = mi.mi(a[0], a[j]);
				IJ.showStatus("MI: " + j + "=" + Cmi[j]);
				if (!displayJH) {
					continue;
				}
				is.addSlice("" + Cmi[j], new FloatProcessor(mi.getBins(), mi.getBins(),
					mi.getJointHistogram()));
				if (j == 0) {
					newimp = new ImagePlus("Joint histograms " + imp.getTitle(), is);
					newimp.show();
				}
				else {
					newimp.setStack(null, is);
					newimp.setSlice(j + 1);
				}
			}

			final double avg = BIJstats.avg(Cmi);
			final float stdev = BIJstats.stdev(Cmi);
			final float xValues[] = new float[Cmi.length];
			for (int i = 0; i < xValues.length; i++) {
				xValues[i] = i + 1.0f;
			}

			final PlotWindow pw =
				new PlotWindow("MI " + imp.getTitle(), "slice",
					"-MI (slice 1 to slice)", xValues, Cmi);
			pw.draw();
			IJ.log("Mutual Information (first slice to slice):\naverage=" + avg +
				"stdev: " + stdev);
		}
		catch (final Exception ex) {
			final CharArrayWriter c = new CharArrayWriter();
			ex.printStackTrace(new PrintWriter(c));
			IJ.error(c.toString());
		}
		return;
	}

	void showAbout() {
		IJ.showMessage("About this plugin...", "\n.");
	}
}
