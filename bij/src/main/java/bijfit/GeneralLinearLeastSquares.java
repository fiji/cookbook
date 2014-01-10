// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   GeneralLinearLeastSquares.java

package bijfit;

import Jama.Matrix;
import bijnum.BIJutil;
import java.io.PrintStream;

// Referenced classes of package bijfit:
//            DesignMatrixLinear, DesignMatrix

public class GeneralLinearLeastSquares
{

    public GeneralLinearLeastSquares()
    {
    }

    public static float[] fit(float b[], DesignMatrix dm)
    {
        double db[] = new double[b.length];
        for(int k = 0; k < b.length; k++)
            db[k] = b[k];

        double Aa[][] = dm.getMatrix();
        Matrix a = new Matrix(Aa);
        Matrix x = a.solve((new Matrix(db, 1)).transpose());
        double xs[][] = x.transpose().getArray();
        float coords[] = new float[xs[0].length];
        for(int l = 0; l < coords.length; l++)
            coords[l] = (float)xs[0][l];

        return coords;
    }

    public static double[] fit(double db[], DesignMatrix dm)
    {
        double Aa[][] = dm.getMatrix();
        Matrix a = new Matrix(Aa);
        Matrix x = a.solve((new Matrix(db, 1)).transpose());
        double xs[][] = x.transpose().getArray();
        double coords[] = new double[xs[0].length];
        for(int l = 0; l < coords.length; l++)
            coords[l] = xs[0][l];

        return coords;
    }

    public static float[][] fit(float b[][], DesignMatrix dm)
    {
        float coordinates[][] = new float[b.length][];
        for(int j = 0; j < b.length; j++)
            coordinates[j] = fit(b[j], dm);

        return coordinates;
    }

    public static void test()
    {
        float x[] = {
            -2F, -1F, 0.0F, 1.0F, 2.0F
        };
        float y[] = {
            -2F, 0.0F, 0.0F, 1.0F, 2.0F
        };
        DesignMatrix dm = new DesignMatrixLinear(x);
        float params[] = fit(y, dm);
        System.out.println(" x: " + BIJutil.toString(x) + " y: " + BIJutil.toString(y) + "a's: " + BIJutil.toString(params));
    }
}
