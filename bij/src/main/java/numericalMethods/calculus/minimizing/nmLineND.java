package numericalMethods.calculus.minimizing;

import java.io.Serializable;

import numericalMethods.function.nmDoubleArrayValued;
import numericalMethods.function.nmDoubleParametrized;

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
public class nmLineND implements nmDoubleArrayValued, nmDoubleParametrized, Serializable
{
    private static final long serialVersionUID = 1L;
    protected double point[];
    protected double direction[];
    protected int n;
    protected double t;

    public nmLineND(double aPoint[], double aDirection[])
    {
        n = aPoint.length;
        if(n != aDirection.length)
        {
            throw new IllegalArgumentException(" dimension of direction and point do not coincide ");
        } else
        {
            point = aPoint;
            direction = aDirection;
            return;
        }
    }

    public double getT()
    {
        return t;
    }

    public void setT(double v)
    {
        t = v;
    }

    public int getN()
    {
        return n;
    }

    public void setN(int v)
    {
        n = v;
    }

    public double[] getDirection()
    {
        return direction;
    }

    public void setDirection(double v[])
    {
        if(v.length != n)
        {
            throw new IllegalArgumentException(" dimension of direction is not ".concat(String.valueOf(String.valueOf(n))));
        } else
        {
            direction = v;
            return;
        }
    }

    public double[] getPoint()
    {
        return point;
    }

    public void setPoint(double v[])
    {
        if(v.length != n)
        {
            throw new IllegalArgumentException(" dimension of point is not ".concat(String.valueOf(String.valueOf(n))));
        } else
        {
            direction = v;
            return;
        }
    }

    @Override
	public void setDoubleParameter(double p)
    {
        t = p;
    }

    public double[] getDoubleArrayValue()
    {
        double doubleArrayValue[] = new double[n];
        getDoubleArrayValue(doubleArrayValue, 0);
        return doubleArrayValue;
    }

    @Override
	public final int getDoubleArrayValueLength()
    {
        return n;
    }

    public final void getValue(double value[])
    {
        getDoubleArrayValue(value, 0);
    }

    @Override
	public final void getDoubleArrayValue(double value[], int offset)
    {
        int i = 0;
        for(int j = offset; i < n; j++)
        {
            value[j] = point[i] + t * direction[i];
            i++;
        }

    }
}
