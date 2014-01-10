// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DesignMatrixExponential.java

package bijfit;


// Referenced classes of package bijfit:
//            DesignMatrix

public class DesignMatrixExponential extends DesignMatrix
{

    public DesignMatrixExponential(float xs[])
    {
        super.a = new double[xs.length][];
        for(int j = 0; j < xs.length; j++)
            super.a[j] = fexp(xs[j]);

    }

    protected double[] fexp(double x)
    {
        double p[] = new double[2];
        p[0] = 1.0D;
        p[1] = 1.0D + x;
        return p;
    }
}
