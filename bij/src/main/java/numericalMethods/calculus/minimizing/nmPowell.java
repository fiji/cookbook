package numericalMethods.calculus.minimizing;

import java.io.Serializable;

import numericalMethods.function.nmDoubleArrayParametrized;
import numericalMethods.function.nmDoubleValued;

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
public final class nmPowell
    implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static int ITMAX = 200;
    private static final double TOL = 0.00020000000000000001D;
    private static boolean debug = false;
    private static int iter;

    public static int getITMAX()
    {
        return ITMAX;
    }

    public static void setITMAX(int v)
    {
        ITMAX = v;
    }

    public static boolean getDebug()
    {
        return debug;
    }

    public static void setDebug(boolean v)
    {
        debug = v;
    }

    static final double sqr(double a)
    {
        return a * a;
    }

    public static int getIter()
    {
        return iter;
    }

    public static double[][] getStandardBasis(int dim)
    {
        double basis[][] = new double[dim][dim];
        for(int i = 0; i < dim; i++)
            basis[i][i] = 1.0D;

        return basis;
    }

    public static final double search(double p[], double ftol, nmDoubleValued f)
    {
        return search(p, getStandardBasis(p.length), ftol, f);
    }

    public static final double search(double p[], double xi[][], double ftol, nmDoubleValued f)
    {
        nmDoubleArrayParametrized F = (nmDoubleArrayParametrized)f;
        return search(p, xi, ftol, F, f);
    }

    public static final double search(double p[], double xi[][], double ftol, nmDoubleArrayParametrized F, nmDoubleValued f)
    {
        return search(p, xi, ftol, F, f, ITMAX, debug);
    }

    public static final double search(double p[], double ftol, nmDoubleArrayParametrized F, nmDoubleValued f, int n, boolean debug)
    {
        return search(p, getStandardBasis(p.length), ftol, F, f, n, debug);
    }

    public static double search(double p[], double xi[][], double ftol, nmDoubleArrayParametrized F, nmDoubleValued f, int itMax, boolean debug)
    {
        double aTuple[] = new double[2];
        int n = p.length;
        double pt[] = new double[n];
        double ptt[] = new double[n];
        double xit[] = new double[n];
        nmLineNDBrent lineNDBrent = new nmLineNDBrent(F, f, p, xit);
        F.setDoubleArrayParameter(p, 0);
        double fret = f.getDoubleValue();
        if(debug)
        {
            String s = new String(String.valueOf(String.valueOf((new StringBuffer(" f(p) = ")).append(fret).append(" , p = "))));
            for(int i = 0; i < n; i++)
                s = String.valueOf(s) + String.valueOf(String.valueOf(String.valueOf(p[i])).concat(" "));

            System.out.println(s);
        }
        for(int j = 0; j < n; j++)
            pt[j] = p[j];

        iter = 1;
        do
        {
            double fp = fret;
            int ibig = 0;
            double del = 0.0D;
            double fptt;
            for(int i = 0; i < n; i++)
            {
                for(int j = 0; j < n; j++)
                    xit[j] = xi[j][i];

                fptt = fret;
                fret = lineNDBrent.search();
                if(Math.abs(fptt - fret) > del)
                {
                    del = Math.abs(fptt - fret);
                    ibig = i;
                }
            }

            if(debug)
            {
                String s = new String(String.valueOf(String.valueOf((new StringBuffer("iter = ")).append(iter).append(", fret = ").append(fret).append(", fp = ").append(fp).append(", p = "))));
                for(int i = 0; i < n; i++)
                    s = String.valueOf(s) + String.valueOf(String.valueOf(String.valueOf(p[i])).concat(" "));

                System.out.println(s);
            }
            if(2D * Math.abs(fp - fret) <= ftol * (Math.abs(fp) + Math.abs(fret)))
                return fret;
            if(debug && iter >= itMax)
            {
                System.out.println("Too many iterations in routine POWELL");
                return fret;
            }
            for(int j = 0; j < n; j++)
            {
                ptt[j] = 2D * p[j] - pt[j];
                xit[j] = p[j] - pt[j];
                pt[j] = p[j];
            }

            F.setDoubleArrayParameter(ptt, 0);
            fptt = f.getDoubleValue();
            if(fptt < fp)
            {
                double t = 2D * ((fp - 2D * fret) + fptt) * sqr(fp - fret - del) - del * sqr(fp - fptt);
                if(t < 0.0D)
                {
                    fret = lineNDBrent.search();
                    for(int j = 0; j < n; j++)
                        xi[j][ibig] = xit[j];

                }
            }
            iter++;
        } while(true);
    }
}
