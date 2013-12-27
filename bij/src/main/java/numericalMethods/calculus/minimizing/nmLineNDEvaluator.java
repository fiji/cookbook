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
public class nmLineNDEvaluator extends nmLineND implements nmDoubleValued, Serializable
{
    private nmDoubleArrayParametrized doubleArrayParametrized;
    private nmDoubleValued doubleValued;
    private double pointAtT[];

    public nmLineNDEvaluator(nmDoubleArrayParametrized functionND)
    {
        this(functionND, (nmDoubleValued)functionND);
    }

    public nmLineNDEvaluator(nmDoubleArrayParametrized functionNDPar, nmDoubleValued functionNDVal)
    {
        super(new double[functionNDPar.getDoubleArrayParameterLength()], new double[functionNDPar.getDoubleArrayParameterLength()]);
        doubleArrayParametrized = functionNDPar;
        doubleValued = functionNDVal;
        pointAtT = new double[super.n];
    }

    public nmLineNDEvaluator(nmDoubleArrayParametrized functionNDpar, nmDoubleValued functionNDval, double point[], double direction[])
    {
        super(point, direction);
        doubleArrayParametrized = functionNDpar;
        doubleValued = functionNDval;
        if(functionNDpar.getDoubleArrayParameterLength() != super.n)
        {
            throw new IllegalArgumentException("length of parmeter value does no coincide with the length of the point");
        } else
        {
            pointAtT = new double[super.n];
            return;
        }
    }

    @Override
	public void setDoubleParameter(double p)
    {
        t = p;
        getValue(pointAtT);
        doubleArrayParametrized.setDoubleArrayParameter(pointAtT, 0);
    }

    @Override
	public double getDoubleValue()
    {
        return doubleValued.getDoubleValue();
    }
}
