package bijfit;

import ij.IJ;
import bijnum.BIJmatrix;

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
 */public class Maximum
{

    public static float[] findSubpixel(float image[], int width)
    {
        int height = image.length / width;
        int intpeak[] = find(image, width);
        float dxpeak = intpeak[0] - width / 2;
        float dypeak = intpeak[1] - height / 2;
        int xpeak = intpeak[0];
        int ypeak = intpeak[1];
        if (xpeak <= 0)
            xpeak = -xpeak + 1;
        if (ypeak <= 0)
            ypeak = -ypeak + 1;
        if (xpeak >= width)
            xpeak = 2 * width - xpeak;
        if (ypeak >= height)
            ypeak = 2 * height - ypeak;
        float fpeak[] = new float[2];
        float z[] = new float[3];
        float w[][] = new float[3][3];
        z[0] = image[(xpeak - 1) + ypeak * width];
        if (Float.isNaN(z[0]))
            z[0] = image[xpeak + ypeak * width];
        z[1] = image[xpeak + ypeak * width];
        z[2] = image[xpeak + 1 + ypeak * width];
        if (Float.isNaN(z[2]))
            z[2] = image[xpeak + ypeak * width];
        w[0][0] = (dxpeak - 1.0F) * (dxpeak - 1.0F);
        w[0][1] = dxpeak - 1.0F;
        w[0][2] = 1.0F;
        w[1][0] = dxpeak * dxpeak;
        w[1][1] = dxpeak;
        w[1][2] = 1.0F;
        w[2][0] = (dxpeak + 1.0F) * (dxpeak + 1.0F);
        w[2][1] = dxpeak + 1.0F;
        w[2][2] = 1.0F;
        float wi[][] = null;
        float x[] = null;
        try
        {
            wi = BIJmatrix.inverse(w);
            x = BIJmatrix.mul(wi, z);
        }
        catch(Exception e)
        {
            IJ.log("Error " + e);
        }
        fpeak[0] = -x[1] / (2.0F * x[0]);
        z[0] = image[xpeak + (ypeak - 1) * width];
        if (Float.isNaN(z[0]))
            z[0] = image[xpeak + ypeak * width];
        z[1] = image[xpeak + ypeak * width];
        z[2] = image[xpeak + (ypeak + 1) * width];
        if (Float.isNaN(z[2]))
            z[2] = image[xpeak + ypeak * width];
        w[0][0] = (dypeak - 1.0F) * (dypeak - 1.0F);
        w[0][1] = dypeak - 1.0F;
        w[0][2] = 1.0F;
        w[1][0] = dypeak * dypeak;
        w[1][1] = dypeak;
        w[1][2] = 1.0F;
        w[2][0] = (dypeak + 1.0F) * (dypeak + 1.0F);
        w[2][1] = dypeak + 1.0F;
        w[2][2] = 1.0F;
        try
        {
            wi = BIJmatrix.inverse(w);
            x = BIJmatrix.mul(wi, z);
        }
        catch(Exception e)
        {
            IJ.log("Error " + e);
        }
        fpeak[1] = -x[1] / (2.0F * x[0]);
        mag = x[0] * (fpeak[1] * fpeak[1]) + x[1] * fpeak[1] + x[2];
        return fpeak;
    }

    public static int[] find(float image[], int width)
    {
        int maxpos[] = new int[2];
        maxpos[0] = 0;
        maxpos[1] = 0;
        float max = -3.402823E+38F;
        for(int j = 0; j < image.length / width; j++)
        {
            for(int i = 0; i < width; i++)
                if (image[j * width + i] > max)
                {
                    max = image[j * width + i];
                    maxpos[0] = i;
                    maxpos[1] = j;
                }

        }

        return maxpos;
    }

    public static int find(float v[])
    {
        int maxpos = 0;
        float max = -3.402823E+38F;
        for(int i = 0; i < v.length; i++)
            if (v[i] > max)
            {
                max = v[i];
                maxpos = i;
            }

        return maxpos;
    }

    /**
     * @deprecated Method peakMag is deprecated
     */

    @Deprecated
	public static float peakMag()
    {
        return mag;
    }

    static float mag;
}
