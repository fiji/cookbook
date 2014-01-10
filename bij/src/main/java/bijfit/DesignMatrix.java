// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DesignMatrix.java

package bijfit;

import bijnum.BIJutil;

public class DesignMatrix
{

    public DesignMatrix()
    {
    }

    public double[][] getMatrix()
    {
        return a;
    }

    public String toString()
    {
        return BIJutil.toString(a);
    }

    double a[][];
}
