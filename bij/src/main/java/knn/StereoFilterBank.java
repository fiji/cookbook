// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   StereoFilterBank.java

package knn;

import bijfit.DesignMatrixLinear;
import bijfit.GeneralLinearLeastSquares;
import bijnum.BIJfit;
import bijnum.BIJmatrix;
import bijnum.BIJutil;
import ij.ImagePlus;
import ij.io.FileSaver;
import volume.Convolver;
import volume.GaussianDerivative;
import volume.Transformer;

// Referenced classes of package knn:
//            Feature

public class StereoFilterBank
{

    public StereoFilterBank()
    {
        n = -1;
    }

    public StereoFilterBank(float scales[], float xshifts[])
    {
        n = -1;
        this.scales = scales;
        nrOrders = 1;
        this.xshifts = xshifts;
    }

    public int getNumber()
    {
        n = xshifts.length * scales.length;
        return n;
    }

    public String toString(int filternumber, String preString)
    {
        double params[] = params(filternumber);
        String name = "<unknown " + filternumber + ">";
        if(params != null)
            name = name((int)params[0], params[1], 0.0D, preString);
        return name;
    }

    public static String toString(String preString)
    {
        return preString + "D scale=" + 0 + "p";
    }

    protected static String name(double scale, double xshift, double yshift, String extraText)
    {
        if(yshift == 0.0D)
            return extraText + "S" + "(" + xshift + " pixels) scale=" + scale + "p";
        else
            return extraText + "S" + "(" + xshift + ", " + yshift + " pixels) scale=" + scale + "p";
    }

    protected double[] params(int filternumber)
    {
        double r[] = new double[2];
        for(int scale = 0; scale < scales.length; scale++)
        {
            for(int xshift = 0; xshift < xshifts.length; xshift++)
                if(filternumber-- <= 0)
                {
                    r[1] = xshifts[xshift];
                    r[0] = scales[scale];
                    return r;
                }

        }

        System.err.println("Incorrect featurenumber");
        return null;
    }

    public Feature filter(int filternumber, float left_image[], float right_image[], int width, String extraText)
    {
        Feature f = null;
        double params[] = params(filternumber);
        f = S(left_image, right_image, width, (int)params[0], params[1], 0.0D, extraText);
        return f;
    }

    public Feature depth(float left_image[], float right_image[], int width, String extraText)
        throws Exception
    {
        Feature f = null;
        f = D(left_image, right_image, width, scales[0], xshifts, extraText);
        return f;
    }

    public static Feature S(float image_left[], float image_right[], int width, double scale, double xshift, double yshift, String extraText)
    {
        Feature f = null;
        float shifted[] = Transformer.quick(image_right, width, (int)Math.round(xshift), 0);
        float delta[] = BIJmatrix.sub(image_left, shifted);
        if(scale > 0.0D)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            delta = Convolver.convolvex(delta, width, delta.length / width, k0);
            delta = Convolver.convolvey(delta, width, delta.length / width, k0);
        }
        f = new Feature(name(scale, Math.round(xshift), 0.0D, extraText), delta);
        return f;
    }

    public static Feature D(float image_left[], float image_right[], int width, double scale, float xshifts[], String extraText)
        throws Exception
    {
        Feature f = null;
        float deltas[][] = new float[xshifts.length][];
        if(scale > 0.0D)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            image_left = Convolver.convolvex(image_left, width, image_left.length / width, k0);
            image_left = Convolver.convolvey(image_left, width, image_left.length / width, k0);
            image_right = Convolver.convolvex(image_right, width, image_right.length / width, k0);
            image_right = Convolver.convolvey(image_right, width, image_right.length / width, k0);
        }
        for(int s = 0; s < xshifts.length; s++)
        {
            float params[] = new float[2];
            params[0] = xshifts[s];
            params[1] = 0.0F;
            float shifted_right[] = Transformer.transform(image_right, width, params);
            deltas[s] = BIJmatrix.sub(image_left, shifted_right);
            BIJmatrix.pow(deltas[s], deltas[s], 2D);
        }

        ij.ImageStack is = BIJutil.imageStackFromMatrix(deltas, width);
        ImagePlus newimp = new ImagePlus("deltas", is);
        FileSaver fs = new FileSaver(newimp);
        fs.saveAsTiffStack("G:\\stereo disk data" + System.getProperty("file.separator") + newimp.getTitle() + ".tiff");
        float transpose_deltas[][] = BIJmatrix.transpose(deltas);
        int binning = 1;
        float depthmap[] = linzeros(transpose_deltas, xshifts, width, binning);
        f = new Feature(toString(extraText), depthmap);
        return f;
    }

    public static float[] linzeros(float mys[][], float yxs[], int width, int binning)
    {
        float map[] = new float[mys.length];
        for(int y = 0; y < mys.length / width; y += binning)
        {
            for(int x = 0; x < width; x += binning)
            {
                float xs[] = null;
                float ys[] = null;
                for(int j = 0; j < binning; j++)
                {
                    for(int i = 0; i < binning; i++)
                    {
                        xs = BIJmatrix.concat(xs, yxs);
                        ys = BIJmatrix.concat(ys, mys[(y + j) * width + x + i]);
                    }

                }

                DesignMatrixLinear dm = new DesignMatrixLinear(xs);
                float params[] = GeneralLinearLeastSquares.fit(ys, dm);
                double a = params[0];
                double b = params[1];
                float zerocrossing = 0.0F;
                if(b != 0.0D)
                    zerocrossing = (float)(-a / b);
                for(int j = 0; j < binning; j++)
                {
                    for(int i = 0; i < binning; i++)
                        map[(y + j) * width + x + i] = zerocrossing;

                }

            }

        }

        return map;
    }

    public static float[] linzerosOld(float mys[][], float yxs[], int width, int binning)
    {
        float map[] = new float[mys.length];
        for(int y = 0; y < mys.length / width; y += binning)
        {
            for(int x = 0; x < width; x += binning)
            {
                float xs[] = null;
                float ys[] = null;
                for(int j = 0; j < binning; j++)
                {
                    for(int i = 0; i < binning; i++)
                    {
                        xs = BIJmatrix.concat(xs, yxs);
                        ys = BIJmatrix.concat(ys, mys[(y + j) * width + x + i]);
                    }

                }

                double params[] = BIJfit.linear(xs, ys);
                double a = params[0];
                double b = params[1];
                float zerocrossing = 0.0F;
                if(b != 0.0D)
                    zerocrossing = (float)(-a / b);
                for(int j = 0; j < binning; j++)
                {
                    for(int i = 0; i < binning; i++)
                        map[(y + j) * width + x + i] = zerocrossing;

                }

            }

        }

        return map;
    }

    protected float scales[];
    protected float xshifts[];
    protected int nrOrders;
    protected int n;
}
