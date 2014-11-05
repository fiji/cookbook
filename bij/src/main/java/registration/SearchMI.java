package registration;

import bijnum.BIJmatrix;
import bijnum.BIJmi;
import ij.IJ;
import numericalMethods.function.nmDoubleArrayParametrized;
import numericalMethods.function.nmDoubleValued;
import volume.Transformer;

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
public class SearchMI extends BIJmi implements nmDoubleArrayParametrized, nmDoubleValued
{

    public SearchMI(float min, float max, float scale, float a[], float b[], int width)
    {
        super(min, max, scale);
        this.a = a;
        this.b = b;
        this.width = width;
    }

    @Override
	public int getDoubleArrayParameterLength()
    {
        return 3;
    }

    @Override
	public void setDoubleArrayParameter(double p[], int offset)
    {
        this.p = p;
        if(offset != 0)
            IJ.log("HELP offset not 0".concat(String.valueOf(String.valueOf(offset))));
    }

    @Override
	public double getDoubleValue()
    {
        return mi(p);
    }

    public double mi(double p[])
    {
        double n[] = new double[p.length];
        for(int i = 0; i < p.length; i++)
            n[i] = p[i];

        return mi(n);
    }

    public float mi(float p[])
    {
        float ret = 3.402823E+38F;
        if(p.length > 0 && Math.abs(p[0]) > width / 2)
            ret = Math.abs(10000 * p[0]);
        else
        if(p.length > 1 && Math.abs(p[1]) > width / 2)
            ret = Math.abs(10000 * p[1]);
        else
        if(p.length > 2 && Math.abs(p[2]) > 180)
            ret = Math.abs(10000 * p[2]);
        else
        if(p.length > 3 && (p[3] >= 100 || p[3] < 0.01D))
            ret = Math.abs(10000 * p[3]);
        else
        if(p.length > 4 && (p[4] >= 100 || p[4] < 0.01D))
        {
            ret = Math.abs(10000 * p[4]);
        } else
        {
            float m[][] = Transformer.convertParametersIntoTransformationMatrix(p);
            ret = mi(a, b, width, m);
        }
        return ret;
    }

    protected float mi(float a[], float b[], int width, float m[][])
    {
        zeroHistograms();
        super.n = 0;
        float mi[][] = null;
        try
        {
            mi = BIJmatrix.inverse(m);
        }
        catch(Exception e)
        {
            IJ.error("mi error ".concat(String.valueOf(String.valueOf(e))));
            float f = 0.0F;
            return f;
        }
        int height = a.length / width;
        float coordinates[] = new float[3];
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                coordinates[0] = x;
                coordinates[1] = y;
                coordinates[2] = 1.0F;
                float tv[] = null;
                try
                {
                    tv = BIJmatrix.mul(mi, coordinates);
                }
                catch(Exception e)
                {
                    IJ.error("mi error ".concat(String.valueOf(String.valueOf(e))));
                    float f1 = 0.0F;
                    return f1;
                }
                int i = y * width + x;
                if(tv[0] < 0 || tv[0] >= width - 1 || tv[1] < 0 || tv[1] >= height - 1)
                    continue;
                float ai = a[i];
                float bi = Transformer.bilinear(b, width, tv[0], tv[1]);
                if(ai == (0.0F / 0.0F) || bi == (0.0F / 0.0F))
                    continue;
                int ix0 = Math.round((ai - super.min) * super.scale);
                int ix1 = Math.round((bi - super.min) * super.scale);
                if(ix0 >= super.Pu.length || ix1 >= super.Pv.length)
                    IJ.error(String.valueOf(String.valueOf((new StringBuffer("mi: array index out of bounds ix0=")).append(ix0).append(" ix1= ").append(ix1).append(" ai=").append(ai).append(" bi=").append(bi))));
                if(ix0 < 0 || ix1 < 0)
                    IJ.error(String.valueOf(String.valueOf((new StringBuffer("mi: array index underflow ix0=")).append(ix0).append(" ix1= ").append(ix1).append(" ai=").append(ai).append(" bi=").append(bi))));
                if(ix0 >= 0 && ix1 >= 0)
                {
                    super.Pu[ix0]++;
                    super.Pv[ix1]++;
                    super.Puv[ix0 * super.bins + ix1]++;
                    super.n++;
                }
            }

        }

        return computeProbs(super.n);
    }

    public float a[];
    public float b[];
    public int width;
    public double p[];
}
