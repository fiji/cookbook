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
public final class nmLineNDBrent extends nmLineNDEvaluator implements Serializable
{
	private static final long serialVersionUID = 1l;
    private static final double TOL = 0.002D;
    private static final double abc[] = new double[3];
    private static final double f[] = new double[3];
    private static final double aTuple[] = new double[2];

    public nmLineNDBrent(nmDoubleArrayParametrized functionNDpar, nmDoubleValued functionNDval)
    {
        super(functionNDpar, functionNDval);
    }

    public nmLineNDBrent(nmDoubleArrayParametrized functionNDpar, nmDoubleValued functionNDval, double point[], double direction[])
    {
        super(functionNDpar, functionNDval, point, direction);
    }

    public final double search()
    {
        abc[0] = -1D;
        abc[1] = 0.0D;
        abc[2] = 1.0D;
        nmBracket.search(abc, f, this);
        nmBrent.search(abc, aTuple, this, 0.002D);
        double xmin = aTuple[0];
        for(int j = 0; j < super.n; j++)
            super.point[j] += super.direction[j] *= xmin;

        return aTuple[1];
    }
}
