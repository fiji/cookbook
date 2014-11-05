package registration;

import bijfit.Maximum;
import bijnum.BIJfht;
import bijnum.BIJmatrix;
import bijnum.BIJutil;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.FloatProcessor;
import volume.Equalize;

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
public class RegisterFFT extends Register
{

    public RegisterFFT(float reference[], int width)
    {
        debug = 0;
        IJ.showStatus("Registering...equalizing reference image");
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("reference before equal", new FloatProcessor(width, reference.length / width, reference, null));
            mimp.show();
        }
        reference = Equalize.sliding(reference, width, 0.0F, Equalize.getDefaultWindowSize(), true);
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("reference ", new FloatProcessor(width, reference.length / width, reference, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        referencefft = BIJutil.tile(reference, width);
        if(debug == 2)
            (new ImagePlus("reference_tile", new FloatProcessor((int)Math.sqrt(referencefft.length), (int)Math.sqrt(referencefft.length), referencefft, null))).show();
        fht = new BIJfht(referencefft.length);
        IJ.showStatus("Registering...computing FFT of reference image");
        fht.compute(referencefft, false);
        super.width = width;
        super.mask = null;
    }

    public RegisterFFT(float reference[], int width, float mask[])
    {
        debug = 0;
        IJ.showStatus("Registering...equalizing reference image");
        reference = Equalize.sliding(reference, width, 0.0F, Equalize.getDefaultWindowSize(), true);
        if(mask != null)
            reference = BIJmatrix.mulElements(reference, mask);
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("masked reference ", new FloatProcessor(width, reference.length / width, reference, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        referencefft = BIJutil.tile(reference, width);
        if(debug == 2)
            (new ImagePlus("reference_tile", new FloatProcessor((int)Math.sqrt(referencefft.length), (int)Math.sqrt(referencefft.length), referencefft, null))).show();
        fht = new BIJfht(referencefft.length);
        IJ.showStatus("Registering...computing FFT of reference image");
        fht.compute(referencefft, false);
        super.width = width;
        super.mask = mask;
    }

    @Override
	public float[] register(float image[])
    {
        image = Equalize.sliding(image, super.width, 0.0F, Equalize.getDefaultWindowSize(), true);
        if(super.mask != null)
            image = BIJmatrix.mulElements(image, super.mask);
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("(masked) image ", new FloatProcessor(super.width, image.length / super.width, image, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        float imagefft[] = BIJutil.tile(image, super.width);
        if(debug == 2)
            (new ImagePlus("image_tile", new FloatProcessor((int)Math.sqrt(imagefft.length), (int)Math.sqrt(imagefft.length), imagefft, null))).show();
        IJ.showStatus("Registering...computing FFT");
        fht.compute(imagefft, false);
        IJ.showStatus("Registering...computing inverse power spectrum");
        float icps[] = fht.crossPowerSpectrum(referencefft, imagefft);
        fht.compute(icps, true);
        float spectrum[] = fht.flipquad(icps);
        if(debug == 2)
            (new ImagePlus("Inverse cross power spectrum", new FloatProcessor((int)Math.sqrt(spectrum.length), (int)Math.sqrt(spectrum.length), spectrum, null))).show();
        float crosscorrelation[] = BIJutil.fit(spectrum, super.width, image.length / super.width);
        if(super.mask != null)
            BIJmatrix.mulElements(crosscorrelation, super.mask);
        super.estimate = Maximum.findSubpixel(crosscorrelation, super.width);
        if(debug == 2)
            (new ImagePlus("X corr", new FloatProcessor(super.width, image.length / super.width, crosscorrelation, null))).show();
        return super.estimate;
    }

    @Override
	public String toString()
    {
        return "" + super.estimate[0] + "\t" + super.estimate[1] + "\t";
    }

    protected BIJfht fht;
    protected float referencefft[];
    protected int debug;
}
