import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import volume.Diffusion;
import volume.Diffusion3D;
import volume.VolumeFloat;

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

public class Diffusion_ implements PlugInFilter {

	@Override
	public int setup(final String arg, final ImagePlus dummy) {
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		return NO_UNDO | DOES_ALL;
	}

	@Override
	public void run(final ImageProcessor ip) {
		final ImagePlus imp = WindowManager.getCurrentImage();
		if (imp.getStackSize() == 1) {
			final Diffusion df = new Diffusion(imp);
			if (df.params()) {
				df.compute();
				(new ImagePlus("Diffused ", df.getProcessor())).show();
			}
		}
		else {
			final Diffusion3D df = new Diffusion3D();
			final VolumeFloat v = new VolumeFloat(imp.getStack());
			if (df.params(imp, v)) {
				df.compute3D();
				df.intoStack(imp.getStack());
				imp.show();
			}
		}
	}

	void showAbout() {
		IJ.showMessage(
			"About Diffusion plugin...",
			"Nonlinear diffusion filtering of images, and 3D and 4D volumes.\n(c) 1999-2003, Michael Abramoff\nhttp://bij.isi.uu.nl");
	}
}
