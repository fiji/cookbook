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
public final class nmBracket
    implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static final double GOLD = 1.618034D;
    private static final double GLIMIT = 100D;
    private static final double TINY = 9.9999999999999995E-21D;

    private static final double copySign(double a, double b)
    {
        return b <= 0 ? -Math.abs(a) : Math.abs(a);
    }

    public static void search(double t[], double fOfT[], nmDoubleValued f)
    {
        nmDoubleParametrized F = (nmDoubleParametrized)f;
        double dum = 0.0D;
        double ax = t[0];
        double bx = t[1];
        F.setDoubleParameter(ax);
        double fa = f.getDoubleValue();
        F.setDoubleParameter(bx);
        double fb = f.getDoubleValue();
        double inValue = fb;
        if (fb > fa)
        {
            dum = ax;
            ax = bx;
            bx = dum;
            dum = fa;
            fa = fb;
            fb = dum;
        }
        double cx = bx + 1.618034D * (bx - ax);
        F.setDoubleParameter(cx);
        double fu;
        double fc;
        for(fc = f.getDoubleValue(); fb > fc; fc = fu)
        {
            double r = (bx - ax) * (fb - fc);
            double q = (bx - cx) * (fb - fa);
            double u = bx - ((bx - cx) * q - (bx - ax) * r) / (2D * copySign(Math.max(Math.abs(q - r), 9.9999999999999995E-21D), q - r));
            double ulim = bx + 100D * (cx - bx);
            if ((bx - u) * (u - cx) > 0.0D)
            {
                F.setDoubleParameter(u);
                fu = f.getDoubleValue();
                if (fu < fc)
                {
                    ax = bx;
                    bx = u;
                    fa = fb;
                    fb = fu;
                    break;
                }
                if (fu > fb)
                {
                    cx = u;
                    fc = fu;
                    break;
                }
                F.setDoubleParameter(u = cx + 1.618034D * (cx - bx));
                fu = f.getDoubleValue();
            } else
            if ((cx - u) * (u - ulim) > 0.0D)
            {
                F.setDoubleParameter(u);
                fu = f.getDoubleValue();
                if (fu < fc)
                {
                    bx = cx;
                    cx = u;
                    u = cx + 1.618034D * (cx - bx);
                    F.setDoubleParameter(u);
                    fb = fc;
                    fc = fu;
                    fu = f.getDoubleValue();
                }
            } else
            if ((u - ulim) * (ulim - cx) >= 0.0D)
            {
                F.setDoubleParameter(u = ulim);
                fu = f.getDoubleValue();
            } else
            {
                F.setDoubleParameter(u = cx + 1.618034D * (cx - bx));
                fu = f.getDoubleValue();
            }
            ax = bx;
            bx = cx;
            cx = u;
            fa = fb;
            fb = fc;
        }

        t[0] = ax;
        fOfT[0] = fa;
        t[1] = bx;
        fOfT[1] = fb;
        t[2] = cx;
        fOfT[2] = fc;
        if (fb > inValue)
            System.out.println(" proc nmBracket failed to decrease center value! ");
    }
}
