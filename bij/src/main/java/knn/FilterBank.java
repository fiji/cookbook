// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FilterBank.java

package knn;

import volume.Convolver;
import volume.GaussianDerivative;
import bijnum.BIJmatrix;

// Referenced classes of package knn:
//            Feature

public class FilterBank
{

    public FilterBank(float scales[], int nrOrders)
    {
        n = -1;
        this.scales = scales;
        this.nrOrders = nrOrders;
        thetas = new float[nrOrders][];
        for(int scale = 0; scale < scales.length; scale++)
        {
            for(int order = 0; order < nrOrders; order++)
                thetas[order] = thetaSet(order);

        }

    }

    public int getNumber()
    {
        if(n < 0)
        {
            for(int scale = 0; scale < scales.length; scale++)
            {
                for(int order = 0; order < nrOrders; order++)
                    if(thetas[order] != null)
                    {
                        for(int theta = 0; theta < thetas[order].length; theta++)
                            n++;

                    } else
                    {
                        n++;
                    }

            }

        }
        return n + 1;
    }

    public Feature filter(int filternumber, float image[], int width, String extraText)
    {
        Feature f = null;
        double params[] = params(filternumber);
        f = L(image, width, (int)params[0], params[1], params[2], extraText);
        return f;
    }

    public String toString(int filternumber, String preString)
    {
        String name = "<unknown " + filternumber + ">";
        double params[] = params(filternumber);
        name = name((int)params[0], params[1], params[2], preString);
        return name;
    }

    protected static String name(int order, double scale, double theta, String extraText)
    {
        if(order == 0)
            return extraText + "L" + order + " scale=" + scale + "p";
        else
            return extraText + "L" + order + "(" + theta + " dgrs) scale=" + scale + "p";
    }

    protected double[] params(int filternumber)
    {
        double r[] = new double[3];
        for(int scale = 0; scale < scales.length; scale++)
        {
            for(int order = 0; order < nrOrders; order++)
            {
                for(int theta = 0; theta < thetas[order].length; theta++)
                    if(filternumber-- <= 0)
                    {
                        r[0] = order;
                        r[1] = scales[scale];
                        r[2] = thetas[order][theta];
                        return r;
                    }

            }

        }

        return null;
    }

    public static Feature L(float image[], int width, int n, double scale, double theta, String extraText)
    {
        Feature f = null;
        if(n == 0)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            float L[] = Convolver.convolvex(image, width, image.length / width, k0);
            L = Convolver.convolvey(L, width, image.length / width, k0);
            f = new Feature(name(n, scale, theta, extraText), L);
        } else
        if(n == 1)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            volume.Kernel1D k1 = new GaussianDerivative(scale, 1);
            float Lx[] = Convolver.convolvex(image, width, image.length / width, k1);
            Lx = Convolver.convolvey(Lx, width, image.length / width, k0);
            float Ly[] = Convolver.convolvex(image, width, image.length / width, k0);
            Ly = Convolver.convolvey(Ly, width, image.length / width, k1);
            double cth = Math.cos((theta / 180D) * 3.1415926535897931D);
            double sth = Math.sin((theta / 180D) * 3.1415926535897931D);
            float px[] = new float[Lx.length];
            BIJmatrix.mulElements(px, Lx, cth);
            float py[] = new float[Lx.length];
            BIJmatrix.mulElements(py, Ly, sth);
            float L[] = BIJmatrix.addElements(px, py);
            f = new Feature(name(n, scale, theta, extraText), L);
        } else
        if(n == 2)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            volume.Kernel1D k1 = new GaussianDerivative(scale, 1);
            volume.Kernel1D k2 = new GaussianDerivative(scale, 2);
            float Lxx[] = Convolver.convolvex(image, width, image.length / width, k2);
            Lxx = Convolver.convolvey(Lxx, width, image.length / width, k0);
            float Lxy[] = Convolver.convolvex(image, width, image.length / width, k1);
            Lxy = Convolver.convolvey(Lxy, width, image.length / width, k1);
            float Lyy[] = Convolver.convolvex(image, width, image.length / width, k0);
            Lyy = Convolver.convolvey(Lyy, width, image.length / width, k2);
            double cth = Math.cos((theta / 180D) * 3.1415926535897931D);
            double sth = Math.sin((theta / 180D) * 3.1415926535897931D);
            double c2th = cth * cth;
            double csth = cth * sth;
            double s2th = sth * sth;
            float pxx2[] = new float[Lxx.length];
            BIJmatrix.mulElements(pxx2, Lxx, c2th);
            float pxy2[] = new float[Lxy.length];
            BIJmatrix.mulElements(pxy2, Lxy, 2D * csth);
            float pyy2[] = new float[Lyy.length];
            BIJmatrix.mulElements(pyy2, Lyy, s2th);
            float L[] = BIJmatrix.addElements(pxx2, pxy2);
            BIJmatrix.addElements(L, L, pyy2);
            f = new Feature(name(n, scale, theta, extraText), L);
        }
        return f;
    }

    public static Feature[] filter(float image[], int width, float scales[])
    {
        int nrOrders = 3;
        Feature ls[][][] = new Feature[scales.length][nrOrders][];
        float thetas[][] = new float[nrOrders][];
        int length = 0;
        for(int j = 0; j < scales.length; j++)
        {
            for(int order = 0; order < nrOrders; order++)
            {
                thetas[order] = thetaSet(order);
                ls[j][order] = L(image, width, order, scales[j], thetas[order]);
                length += ls[j][order].length;
            }

        }

        Feature Ln[] = new Feature[length];
        int index = 0;
        for(int j = 0; j < scales.length; j++)
        {
            for(int order = 0; order < nrOrders; order++)
            {
                for(int i = 0; i < ls[j][order].length; i++)
                    Ln[index++] = ls[j][order][i];

            }

        }

        ls = (Feature[][][])null;
        return Ln;
    }

    public static float[] thetaSet(int order)
    {
        float theta[] = new float[order + 1];
        theta[0] = 0.0F;
        if(order == 1)
            theta[1] = 90F;
        else
        if(order == 2)
        {
            theta[1] = 60F;
            theta[2] = 120F;
        } else
        if(order != 0)
            throw new IllegalArgumentException("order > 2");
        return theta;
    }

    public static float[] scaleSet(int n)
    {
        float scales[] = new float[n];
        for(int i = 0; i < n; i++)
            scales[i] = (float)Math.pow(2D, i);

        return scales;
    }

    public static Feature[] L(float image[], int width, int n, double scale, float theta[])
    {
        Feature f[] = new Feature[theta.length];
        float L[][] = new float[theta.length][];
        if(n == 0)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            L[0] = Convolver.convolvex(image, width, image.length / width, k0);
            L[0] = Convolver.convolvey(L[0], width, image.length / width, k0);
            f[0] = new Feature(name(n, scale, 0.0D, ""), L[0]);
        } else
        if(n == 1)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            volume.Kernel1D k1 = new GaussianDerivative(scale, 1);
            float Lx[] = Convolver.convolvex(image, width, image.length / width, k1);
            Lx = Convolver.convolvey(Lx, width, image.length / width, k0);
            float Ly[] = Convolver.convolvex(image, width, image.length / width, k0);
            Ly = Convolver.convolvey(Ly, width, image.length / width, k1);
            for(int i = 0; i < theta.length; i++)
            {
                double cth = Math.cos((double)(theta[i] / 180F) * 3.1415926535897931D);
                double sth = Math.sin((double)(theta[i] / 180F) * 3.1415926535897931D);
                float px[] = new float[Lx.length];
                BIJmatrix.mulElements(px, Lx, cth);
                float py[] = new float[Lx.length];
                BIJmatrix.mulElements(py, Ly, sth);
                L[i] = BIJmatrix.addElements(px, py);
                f[i] = new Feature(name(n, scale, theta[i], ""), L[i]);
            }

        } else
        if(n == 2)
        {
            volume.Kernel1D k0 = new GaussianDerivative(scale, 0);
            volume.Kernel1D k1 = new GaussianDerivative(scale, 1);
            volume.Kernel1D k2 = new GaussianDerivative(scale, 2);
            float Lxx[] = Convolver.convolvex(image, width, image.length / width, k2);
            Lxx = Convolver.convolvey(Lxx, width, image.length / width, k0);
            float Lxy[] = Convolver.convolvex(image, width, image.length / width, k1);
            Lxy = Convolver.convolvey(Lxy, width, image.length / width, k1);
            float Lyy[] = Convolver.convolvex(image, width, image.length / width, k0);
            Lyy = Convolver.convolvey(Lyy, width, image.length / width, k2);
            for(int i = 0; i < theta.length; i++)
            {
                double cth = Math.cos((double)(theta[i] / 180F) * 3.1415926535897931D);
                double sth = Math.sin((double)(theta[i] / 180F) * 3.1415926535897931D);
                double c2th = cth * cth;
                double csth = cth * sth;
                double s2th = sth * sth;
                float pxx2[] = new float[Lxx.length];
                BIJmatrix.mulElements(pxx2, Lxx, c2th);
                float pxy2[] = new float[Lxy.length];
                BIJmatrix.mulElements(pxy2, Lxy, 2D * csth);
                float pyy2[] = new float[Lyy.length];
                BIJmatrix.mulElements(pyy2, Lyy, s2th);
                L[i] = BIJmatrix.addElements(pxx2, pxy2);
                BIJmatrix.addElements(L[i], L[i], pyy2);
                f[i] = new Feature(name(n, scale, theta[i], ""), L[i]);
            }

        }
        return f;
    }

    protected float scales[];
    protected float thetas[][];
    protected int nrOrders;
    protected int n;
}
