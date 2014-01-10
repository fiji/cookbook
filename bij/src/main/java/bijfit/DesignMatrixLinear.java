// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DesignMatrixLinear.java

package bijfit;


// Referenced classes of package bijfit:
//            DesignMatrix

public class DesignMatrixLinear extends DesignMatrix
{

    public DesignMatrixLinear(double xs[])
    {
        super.a = new double[xs.length][2];
        for(int j = 0; j < xs.length; j++)
        {
            super.a[j][0] = 1.0D;
            super.a[j][1] = xs[j];
        }

    }

    public DesignMatrixLinear(float xs[])
    {
        super.a = new double[xs.length][2];
        for(int j = 0; j < xs.length; j++)
        {
            super.a[j][0] = 1.0D;
            super.a[j][1] = xs[j];
        }

    }
}
