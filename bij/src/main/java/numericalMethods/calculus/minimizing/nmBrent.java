package numericalMethods.calculus.minimizing;

import java.io.Serializable;

import numericalMethods.function.nmDoubleParametrized;
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
public final class nmBrent implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double CGOLD = 0.38196600000000003D;
    private static final double ZEPS = 1E-10D;
    private static double ITMAX = 100D;

    public static double getITMAX()
    {
        return ITMAX;
    }

    public static void setITMAX(double v)
    {
        ITMAX = v;
    }

    private static final double copySign(double value, double signDonor)
    {
        return signDonor <= 0 ? -Math.abs(value) : Math.abs(value);
    }

    public static final void search(double t[], double X[], nmDoubleValued f, double tol)
    {
        search(t[0], t[1], t[2], X, f, tol);
    }

    public static final void search(double ax, double bx, double cx, double X[], nmDoubleValued f, 
            double tol)
    {
        nmDoubleParametrized F = (nmDoubleParametrized)f;
        double d = 0.0D;
        double e = 0.0D;
        double a = ax >= cx ? cx : ax;
        double b = ax <= cx ? cx : ax;
        double v;
        double w;
        double x = w = v = bx;
        F.setDoubleParameter(x);
        double fv;
        double fx;
        double fw = fv = fx = f.getDoubleValue();
        double inValue = fw;
        for(int iter = 1; iter <= ITMAX; iter++)
        {
            double xm = 0.5D * (a + b);
            double tol1;
            double tol2 = 2D * (tol1 = tol * Math.abs(x) + 1E-10D);
            if (Math.abs(x - xm) <= tol2 - 0.5D * (b - a))
            {
                X[0] = x;
                X[1] = fx;
                return;
            }
            double u;
            if (Math.abs(e) > tol1)
            {
                double r = (x - w) * (fx - fv);
                double q = (x - v) * (fx - fw);
                double p = (x - v) * q - (x - w) * r;
                q = 2D * (q - r);
                if (q > 0.0D)
                    p = -p;
                q = Math.abs(q);
                double etmp = e;
                e = d;
                if (Math.abs(p) >= Math.abs(0.5D * q * etmp) || p < q * (a - x) || p >= q * (b - x))
                {
                    d = 0.38196600000000003D * (e = x < xm ? b - x : a - x);
                } else
                {
                    d = p / q;
                    u = x + d;
                    if (u - a < tol2 || b - u < tol2)
                        d = copySign(tol1, xm - x);
                }
            } else
            {
                d = 0.38196600000000003D * (e = x < xm ? b - x : a - x);
            }
            F.setDoubleParameter(u = Math.abs(d) < tol1 ? x + copySign(tol1, d) : x + d);
            double fu = f.getDoubleValue();
            if (fu <= fx)
            {
                if (u >= x)
                    a = x;
                else
                    b = x;
                v = w;
                w = x;
                x = u;
                fv = fw;
                fw = fx;
                fx = fu;
                continue;
            }
            if (u < x)
                a = u;
            else
                b = u;
            if (fu <= fw || w == x)
            {
                v = w;
                w = u;
                fv = fw;
                fw = fu;
                continue;
            }
            if (fu <= fv || v == x || v == w)
            {
                v = u;
                fv = fu;
            }
        }

        System.out.println("Too many iteration in BRENT\n");
        X[0] = x;
        X[1] = fx;
        if (fx > inValue)
            System.out.println(String.valueOf(String.valueOf((new StringBuffer(" proc Brent failed to decrease center value! ")).append(ax).append(" ").append(bx).append(" ").append(cx))));
    }
}
