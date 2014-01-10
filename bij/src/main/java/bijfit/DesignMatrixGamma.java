// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DesignMatrixGamma.java

package bijfit;


// Referenced classes of package bijfit:
//            DesignMatrix

public class DesignMatrixGamma extends DesignMatrix
{

    public DesignMatrixGamma(float t[], float tmax, float t0)
    {
        super.a = new double[t.length][];
        if(tmax == 0.0F)
            tmax = 1.401298E-45F;
        for(int j = 0; j < t.length; j++)
            super.a[j] = fgamma(t[j], tmax, t0);

    }

    protected double[] fgamma(float x, float xmax, float x0)
    {
        double p[] = new double[2];
        p[0] = 1.0D;
        double xacc = (x - x0) / (xmax - x0);
        if(xacc <= 0.0D)
            xacc = 9.999999960041972E-13D;
        p[1] = (1.0D + Math.log(xacc)) - xacc;
        return p;
    }
}
