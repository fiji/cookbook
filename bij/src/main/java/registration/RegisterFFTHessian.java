// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   RegisterFFTHessian.java

package registration;

import bijfit.Maximum;
import bijnum.*;
import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import volume.Hessian;

// Referenced classes of package registration:
//            Register

public class RegisterFFTHessian extends Register
{

    public RegisterFFTHessian(float reference[], int width)
    {
        debug = 3;
        super.reference = reference;
        IJ.showStatus("Registering...computing edges");
        float edges[] = Hessian.largest(reference, width, 2D);
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("reference edges", new FloatProcessor(width, reference.length / width, edges, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        float fit[] = BIJutil.tile(edges, width);
        referencefft = new float[fit.length];
        for(int i = 0; i < fit.length; i++)
            referencefft[i] = fit[i];

        if(debug == 3)
            (new ImagePlus("reference_tile", new FloatProcessor((int)Math.sqrt(fit.length), (int)Math.sqrt(fit.length), fit, null))).show();
        fht = new BIJfht(referencefft.length);
        IJ.showStatus("Registering...computing FFT of reference image");
        fht.compute(referencefft, false);
        if(debug == 3)
            (new ImagePlus("reference fft", new ByteProcessor((int)Math.sqrt(referencefft.length), (int)Math.sqrt(referencefft.length), fht.getPowerSpectrum(referencefft), null))).show();
        super.width = width;
        super.mask = null;
    }

    public RegisterFFTHessian(float reference[], int width, float mask[])
    {
        debug = 3;
        IJ.showStatus("Registering...computing vessel edges");
        float edges[] = Hessian.largest(reference, width, 2D);
        if(mask != null)
            edges = BIJmatrix.mulElements(edges, mask);
        if(debug == 1)
        {
            ImagePlus mimp = new ImagePlus("reference edges", new FloatProcessor(width, reference.length / width, edges, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        float fit[] = BIJutil.tile(edges, width);
        float referencefft[] = new float[fit.length];
        for(int i = 0; i < fit.length; i++)
            referencefft[i] = fit[i];

        if(debug == 3)
            (new ImagePlus("reference_tile", new FloatProcessor((int)Math.sqrt(fit.length), (int)Math.sqrt(fit.length), fit, null))).show();
        fht = new BIJfht(referencefft.length);
        IJ.showStatus("Registering...computing FFT of reference image");
        fht.compute(referencefft, false);
        if(debug == 3)
            (new ImagePlus("reference_fft", new FloatProcessor((int)Math.sqrt(referencefft.length), (int)Math.sqrt(referencefft.length), referencefft, null))).show();
        super.width = width;
        super.mask = mask;
    }

    public float[] register(float image[])
    {
        IJ.showStatus("Registering...computing edges");
        float nmedges[] = Hessian.largest(image, super.width, 2D);
        float edges[] = nmedges;
        if(super.mask != null)
            edges = BIJmatrix.mulElements(nmedges, super.mask);
        float fit[] = BIJutil.tile(edges, super.width);
        if(debug == 3)
        {
            ImagePlus mimp = new ImagePlus("edges_tile", new FloatProcessor((int)Math.sqrt(fit.length), (int)Math.sqrt(fit.length), fit, null));
            mimp.show();
            FileSaver fs = new FileSaver(mimp);
            fs.saveAsTiff("c:/" + mimp.getTitle() + ".tif");
        }
        float fft[] = new float[fit.length];
        for(int i = 0; i < fit.length; i++)
            fft[i] = fit[i];

        IJ.showStatus("Registering...computing FFT");
        fht.compute(fft, false);
        if(debug == 3)
            (new ImagePlus("image fft", new ByteProcessor((int)Math.sqrt(fft.length), (int)Math.sqrt(fft.length), fht.getPowerSpectrum(fft), null))).show();
        IJ.showStatus("Registering...computing inverse power spectrum");
        float icps[] = fht.crossPowerSpectrum(referencefft, fft);
        fht.compute(icps, true);
        float spectrum[] = fht.flipquad(icps);
        if(debug > 2)
            (new ImagePlus("Inverse cross power spectrum", new FloatProcessor((int)Math.sqrt(spectrum.length), (int)Math.sqrt(spectrum.length), spectrum, null))).show();
        float crosscorrelation[] = BIJutil.fit(spectrum, super.width, image.length / super.width);
        if(super.mask != null)
            BIJmatrix.mulElements(crosscorrelation, super.mask);
        super.estimate = Maximum.findSubpixel(crosscorrelation, super.width);
        if(debug > 3)
            (new ImagePlus("X corr", new FloatProcessor(super.width, crosscorrelation.length / super.width, crosscorrelation, null))).show();
        return super.estimate;
    }

    public String toString()
    {
        return "" + super.estimate[0] + "\t" + super.estimate[1] + "\t";
    }

    protected BIJfht fht;
    protected float referencefft[];
    protected int debug;
}
