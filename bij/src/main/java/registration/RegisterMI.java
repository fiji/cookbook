package registration;

import bijnum.BIJmatrix;
import bijnum.BIJmi;
import numericalMethods.calculus.minimizing.nmPowell;

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
public class RegisterMI extends Register
{

	public RegisterMI()
	{
		this(null, 0, null);
	}

	public RegisterMI(float[] reference, int width)
    {
    	this(reference, width, null);
    }

    public RegisterMI(float[] reference, int width, float[] mask)
    {
        super.reference = reference;
        super.mask = mask;
        minmaxref = BIJmatrix.minmax(reference);
    }

    @Override
	public float[] register(float a[])
    {
        float minmaxim[] = BIJmatrix.minmax(a);
        float min = Math.min(minmaxim[0], minmaxref[0]);
        float max = Math.max(minmaxim[1], minmaxref[1]);
        float scale = BIJmi.getNiceScale(min, max);
        SearchMI function = new SearchMI(min, max, scale, super.reference, a, super.width);
        double p[] = new double[function.getDoubleArrayParameterLength()];
        p[0] = 0.0D;
        p[1] = 0.0D;
        p[2] = 0.0D;
        function.setDoubleArrayParameter(p, 0);
        nmPowell.search(p, 0.01D, function, function, 100, true);
        super.estimate = new float[p.length];
        for(int i = 0; i < p.length; i++)
            super.estimate[i] = (float)p[i];

        return super.estimate;
    }

    @Override
	public String toString()
    {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < super.estimate.length; i++)
        {
            sb.append(super.estimate[i]);
            sb.append("\t");
        }

        return sb.toString();
    }

    protected float minmaxref[];
}
