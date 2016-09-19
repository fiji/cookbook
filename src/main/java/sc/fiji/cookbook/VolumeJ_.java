package sc.fiji.cookbook;

import VolumeJ.VJClassifier;
import VolumeJ.VJClassifierIsosurface;
import VolumeJ.VJClassifierLevoy;
import VolumeJ.VJClassifiers;
import VolumeJ.VJInterpolator;
import VolumeJ.VJIsosurfaceRender;
import VolumeJ.VJLight;
import VolumeJ.VJNearestNeighbor;
import VolumeJ.VJPhongShader;
import VolumeJ.VJRender;
import VolumeJ.VJRenderView;
import VolumeJ.VJRenderViewCine;
import VolumeJ.VJRenderViewSingle;
import VolumeJ.VJRenderViewStereo;
import VolumeJ.VJRenderer;
import VolumeJ.VJShader;
import VolumeJ.VJTrilinear;
import VolumeJ.VJUserInterface;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;
import volume.Volume;

/**
 * This class interfaces the VolumeJ package to a ImageJ plugin. Copyright (c)
 * 1999-2002, Michael Abramoff. All rights reserved.
 * <p>
 * Small print: Permission to use, copy, modify and distribute this version of
 * this software or any parts of it and its documentation or any parts of it
 * ("the software"), for any purpose is hereby granted, provided that the above
 * copyright notice and this permission notice appear intact in all copies of
 * the software and that you do not sell the software, or include the software
 * in a commercial package. The release of this software into the public domain
 * does not imply any obligation on the part of the author to release future
 * versions into the public domain. The author is free to make upgraded or
 * improved versions of the software available for a fee or commercially only.
 * Commercial licensing of the software is available by contacting the author.
 * THE SOFTWARE IS PROVIDED "AS IS" AND WITHOUT WARRANTY OF ANY KIND, EXPRESS,
 * IMPLIED OR OTHERWISE, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.
 * </p>
 * 
 * @author: Michael Abramoff
 */
public class VolumeJ_ implements PlugIn {

	@Override
	public void run(final String arg) {
		final String macroOptions = Macro.getOptions();
		if (macroOptions != null) {
			final int algorithm =
				Integer.parseInt(Macro.getValue(macroOptions, "algorithm", "0"));
			final int mode =
				Integer.parseInt(Macro.getValue(macroOptions, "mode", "0"));
			final int interpolation =
				Integer.parseInt(Macro.getValue(macroOptions, "interpolation", "1"));
			final int lightx =
				Integer.parseInt(Macro.getValue(macroOptions, "lightx", "0"));
			final int lighty =
				Integer.parseInt(Macro.getValue(macroOptions, "lighty", "0"));
			final int lightz =
				Integer.parseInt(Macro.getValue(macroOptions, "lightz", "0"));
			final double aspectx =
				Double.parseDouble(Macro.getValue(macroOptions, "aspectx", "1"));
			final double aspecty =
				Double.parseDouble(Macro.getValue(macroOptions, "aspecty", "1"));
			final double aspectz =
				Double.parseDouble(Macro.getValue(macroOptions, "aspectz", "1"));
			final double scale =
				Double.parseDouble(Macro.getValue(macroOptions, "scale", "0"));
			final double xrot =
				Double.parseDouble(Macro.getValue(macroOptions, "xrot", "0"));
			final double yrot =
				Double.parseDouble(Macro.getValue(macroOptions, "yrot", "0"));
			final double zrot =
				Double.parseDouble(Macro.getValue(macroOptions, "zrot", "0"));
			final int cine =
				Integer.parseInt(Macro.getValue(macroOptions, "cine", "20"));
			final int cineN =
				Integer.parseInt(Macro.getValue(macroOptions, "cineN", "1"));
			final int classification =
				Integer.parseInt(Macro.getValue(macroOptions, "classification", "0"));
			final double threshold =
				Double.parseDouble(Macro.getValue(macroOptions, "threshold", "100"));
			final double width =
				Double.parseDouble(Macro.getValue(macroOptions, "width", "5"));
			final String text = Macro.getValue(macroOptions, "text", "0");
			final int cineToDisk =
				Integer.parseInt(Macro.getValue(macroOptions, "cineToDisk", "0"));
			final int cineAxis =
				Integer.parseInt(Macro.getValue(macroOptions, "cineAxis", "0"));
			run(algorithm, mode, interpolation, lightx, lighty, lightz, aspectx,
				aspecty, aspectz, scale, xrot, yrot, zrot, cine, cineN, classification,
				threshold, width, text, cineToDisk, cineAxis);

		}
		else new VJUserInterface();
	}

	/**
	 * Interface for macro control of volume rendering. Does the same thing as
	 * VJUserInterface, but without a user interface. Straightly sets up a
	 * rendering.
	 * 
	 * @param algorithm int 0 = RAYTRACE, 1 = ISOSURFACE
	 * @param mode int 0 = mono, 1 = stereo, 2 = cine
	 * @param interpolation int 0 = NN, 1 = trilinear
	 * @param lightx int
	 * @param lighty int
	 * @param lightz int
	 * @param aspectx double
	 * @param aspecty double
	 * @param aspectz double
	 * @param scale double
	 * @param xrot double
	 * @param yrot double
	 * @param zrot double
	 * @param cine int = degrees between renderings
	 * @param cineN int = number of cine renderings
	 * @param classification int see VJClassifiers for index. Default = 0.
	 * @param threshold for Levoy surfaces
	 * @param width for Levoy surfaces.
	 * @param text a text to show as title of the rendering window.
	 * @param cineToDisk whether or not to save the rendering to disk
	 * @param cineAxis which axis to rotate for cine mode 0 = x, 1=y, 2=z
	 */
	public void run(final int algorithm, final int mode, final int interpolation,
		final int lightx, final int lighty, final int lightz, final double aspectx,
		final double aspecty, final double aspectz, final double scale,
		final double xrot, final double yrot, final double zrot, final int cine,
		final int cineN, final int classification, final double threshold,
		final double width, final String text, final int cineToDisk,
		final int cineAxis)
	{
		try {
			VJInterpolator interpolator = null;
			if (interpolation == 1) interpolator = new VJTrilinear();
			else interpolator = new VJNearestNeighbor();
			// Check the shader.
			// Create a white light with 0.9 diffuse light and no specular light.
			final VJLight light = new VJLight(lightx, lighty, lightz, (float) 0.9, 0);
			// Create a shader, with 0.1 background light.
			final VJShader shader = new VJPhongShader((float) 0.1, light, false);
			// Check the classifier (and initialize the indexes if needed).
			final VJClassifier classifier =
				VJClassifiers.getClassifier(classification);
			if (classifier instanceof VJClassifierIsosurface) ((VJClassifierIsosurface) classifier)
				.setThreshold(threshold);
			if (classifier instanceof VJClassifierLevoy) {
				((VJClassifierLevoy) classifier).setThreshold(threshold);
				((VJClassifierLevoy) classifier).setWidth(width);
			}
			VJRenderer renderer = null;
			if (algorithm == 0) renderer =
				new VJRender(interpolator, shader, classifier);
			else if (algorithm == 1) renderer =
				new VJIsosurfaceRender(interpolator, shader, classifier);
			if (renderer == null) return;
			final ImagePlus imp = WindowManager.getImage(0);
			final ImagePlus impindex = null;
			final Volume v =
				VJUserInterface.resetVolume(renderer, imp, impindex, aspectx, aspecty,
					aspectz);
			renderer.setVolume(v);
			VJRenderView rs = null;
			if (mode == 0) rs =
				new VJRenderViewSingle(renderer, scale, xrot, yrot, zrot, text);
			else if (mode == 1) rs =
				new VJRenderViewStereo(renderer, scale, xrot, yrot, zrot, text);
			else if (mode == 2) {
				rs =
					new VJRenderViewCine(renderer, scale, xrot, yrot, zrot, text, cineN /
						cine, cineToDisk == 1);
				switch (cineAxis) {
					case 0: // x axis.
						((VJRenderViewCine) rs).setRotationSteps(cine, 0, 0);
						break;
					case 1: // y axis.
						((VJRenderViewCine) rs).setRotationSteps(0, cine, 0);
						break;
					case 2: // z axis.
						((VJRenderViewCine) rs).setRotationSteps(0, 0, cine);
						break;
				}
			}
			// Start rendering.
			rs.start();
		}
		catch (final Exception e) {
			IJ.log(e.getMessage());
		}
	}
}
