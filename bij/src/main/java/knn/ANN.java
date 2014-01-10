// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   ANN.java

package knn;


public class ANN
{

    public ANN()
    {
    }

    public static native int bdtree(float af[][], int i);

    public static native int getvote(float af[], int i);

    public static native int annkPriSearch(float af[], int i, int ai[], float af1[], double d);

    public static native int annkEstimates(float af[], float af1[][], int i, int j, double d, float af2[]);

    public static float[] estimate(float querypts[][], float trueclasses[], int nrclasses, int k, double epsilon)
        throws Exception
    {
        if(querypts == null || trueclasses == null)
            throw new IllegalArgumentException("ANN.estimate(): arguments are null");
        float estimates[] = new float[querypts.length];
        int error = annkEstimates(trueclasses, querypts, k, nrclasses, epsilon, estimates);
        if(error < 0)
            throw new Exception("ANN.estimate(): error in annkPriSearch " + error);
        else
            return estimates;
    }

    static 
    {
        System.loadLibrary("ann_java");
    }
}
