// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   DOGFilterBank.java

package knn;

import volume.Convolver;
import volume.DoG2D;
import volume.Transformer;
import bijnum.BIJmatrix;

// Referenced classes of package knn:
//            StereoFilterBank, Feature

public class DOGFilterBank extends StereoFilterBank
{

    public DOGFilterBank(float scales[], float xshifts[])
    {
        super.scales = scales;
        super.xshifts = xshifts;
    }

    public String toString(int filternumber, String preString)
    {
        double params[] = params(filternumber);
        String name = "<unknown " + filternumber + ">";
        if(params != null)
            name = name((int)params[0], params[1], 0.0D, preString);
        return name;
    }

    protected static String name(double scale, double xshift, double yshift, String extraText)
    {
        if(yshift == 0.0D)
            return extraText + "DOG delta " + "(" + xshift + " pixels) scale=" + scale;
        else
            return extraText + "DOG delta " + "(" + xshift + ", " + yshift + " pixels) scale=" + scale;
    }

    public Feature filter(int filternumber, float left_image[], float right_image[], int width, String extraText)
    {
        Feature f = null;
        double params[] = params(filternumber);
        f = DOG(left_image, right_image, width, params[0], params[1], extraText);
        return f;
    }

    public static Feature DOG(float image_left[], float image_right[], int width, double scale, double xshift, String extraText)
    {
        Feature f = null;
        float dogl[] = BIJmatrix.copy(image_left);
        int ishift = (int)Math.round(xshift);
        float dogr[] = Transformer.quick(image_right, width, ishift, 0);
        if(scale > 0.0D)
        {
            volume.Kernel2D dog = new DoG2D(scale);
            Convolver.convolvexy(dogl, width, image_left.length / width, dog);
            Convolver.convolvexy(dogr, width, image_left.length / width, dog);
        }
        float delta[] = BIJmatrix.sub(dogl, dogr);
        f = new Feature(name(scale, ishift, 0.0D, extraText), delta);
        return f;
    }
}
