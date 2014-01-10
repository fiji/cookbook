// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FeatureComputer.java

package knn;

import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.io.PrintStream;
import volume.Hessian;

// Referenced classes of package knn:
//            Feature, StereoFilterBank, FilterBank, DOGFilterBank

public class FeatureComputer
{

    public FeatureComputer()
    {
    }

    public static Feature compute(int featurenumber, ColorProcessor lp, ColorProcessor rp, float lequalized[])
        throws Exception
    {
        Feature feature = null;
        float pixels[] = null;
        pixels = lequalized;
        switch(featurenumber)
        {
        case 0: // '\0'
        {
            feature = new Feature(name(featurenumber), pixels);
            break;
        }

        case 1: // '\001'
        {
            byte h[] = new byte[lp.getWidth() * lp.getHeight()];
            byte s[] = new byte[lp.getWidth() * lp.getHeight()];
            byte b[] = new byte[lp.getWidth() * lp.getHeight()];
            lp.getHSB(h, s, b);
            feature = new Feature(name(featurenumber), h);
            break;
        }

        case 2: // '\002'
        {
            byte h[] = new byte[lp.getWidth() * lp.getHeight()];
            byte s[] = new byte[lp.getWidth() * lp.getHeight()];
            byte b[] = new byte[lp.getWidth() * lp.getHeight()];
            lp.getHSB(h, s, b);
            feature = new Feature(name(featurenumber), s);
            break;
        }

        case 3: // '\003'
        {
            byte h[] = new byte[lp.getWidth() * lp.getHeight()];
            byte s[] = new byte[lp.getWidth() * lp.getHeight()];
            byte b[] = new byte[lp.getWidth() * lp.getHeight()];
            lp.getHSB(h, s, b);
            feature = new Feature(name(featurenumber), b);
            break;
        }

        case 4: // '\004'
        {
            pixels = distanceToCenterHorizontal(lp.getWidth(), lp.getHeight());
            feature = new Feature(name(featurenumber), pixels);
            break;
        }

        case 5: // '\005'
        {
            pixels = distanceToCenterVertical(lp.getWidth(), lp.getHeight());
            feature = new Feature(name(featurenumber), pixels);
            break;
        }

        case 6: // '\006'
        {
            Hessian.largest(pixels, pixels, lp.getWidth(), hessian_scales[0]);
            feature = new Feature(name(featurenumber), pixels);
            break;
        }

        case 7: // '\007'
        {
            Hessian.largest(pixels, pixels, lp.getWidth(), hessian_scales[1]);
            feature = new Feature(name(featurenumber), pixels);
            break;
        }

        case 8: // '\b'
        {
            if(stereoFilter == null)
                stereoFilter = new StereoFilterBank(shift_scales, xshifts);
            feature = stereoFilter.depth((float[])(float[])lp.convertToFloat().getPixels(), (float[])(float[])rp.convertToFloat().getPixels(), lp.getWidth(), stereoFilter.toString(featureNamePrefix(featurenumber)));
            break;
        }

        case 9: // '\t'
        default:
        {
            if(filterBank == null)
                filterBank = new FilterBank(scales, 3);
            if(stereoFilter == null)
                stereoFilter = new StereoFilterBank(shift_scales, xshifts);
            if(dogFilter == null)
                dogFilter = new DOGFilterBank(dog_scales, dog_xshifts);
            int filternumber = featurenumber - 9;
            int stereofilternumber = filternumber - filterBank.getNumber();
            int dogfilternumber = stereofilternumber - stereoFilter.getNumber();
            if(filternumber >= 0 && filternumber < filterBank.getNumber())
                feature = filterBank.filter(filternumber, pixels, lp.getWidth(), filterBank.toString(filternumber, featureNamePrefix(featurenumber)));
            else
            if(stereofilternumber >= 0 && stereofilternumber < stereoFilter.getNumber())
                feature = stereoFilter.filter(stereofilternumber, (float[])(float[])lp.convertToFloat().getPixels(), (float[])(float[])rp.convertToFloat().getPixels(), lp.getWidth(), stereoFilter.toString(stereofilternumber, featureNamePrefix(featurenumber)));
            else
            if(dogfilternumber >= 0 && dogfilternumber < dogFilter.getNumber())
                feature = dogFilter.filter(dogfilternumber, (float[])(float[])lp.convertToFloat().getPixels(), (float[])(float[])rp.convertToFloat().getPixels(), lp.getWidth(), dogFilter.toString(dogfilternumber, featureNamePrefix(featurenumber)));
            break;
        }
        }
        if(feature != null)
            feature.unitvar();
        return feature;
    }

    public static String featureNamePrefix(int featurenumber)
    {
        return "" + featurenumber + "_";
    }

    public static String name(int featurenumber)
    {
        String name = featureNamePrefix(featurenumber) + "unknown feature";
        switch(featurenumber)
        {
        case 0: // '\0'
            name = featureNamePrefix(featurenumber) + "intensity left";
            break;

        case 1: // '\001'
            name = featureNamePrefix(featurenumber) + "Hue left";
            break;

        case 2: // '\002'
            name = featureNamePrefix(featurenumber) + "Saturation left";
            break;

        case 3: // '\003'
            name = featureNamePrefix(featurenumber) + "Brightness left";
            break;

        case 4: // '\004'
            name = featureNamePrefix(featurenumber) + "Distance center x";
            break;

        case 5: // '\005'
            name = featureNamePrefix(featurenumber) + "Distance center y";
            break;

        case 6: // '\006'
        case 7: // '\007'
            name = featureNamePrefix(featurenumber) + "Hessian eigenvalues scale=" + hessian_scales[featurenumber - 6];
            break;

        case 8: // '\b'
            name = featureNamePrefix(featurenumber) + "Simple depth map";
            break;

        case 9: // '\t'
        default:
            int filternumber = featurenumber - 9;
            int stereofilternumber = filternumber - filterBank.getNumber();
            int dogfilternumber = stereofilternumber - stereoFilter.getNumber();
            if(filternumber >= 0 && filternumber < filterBank.getNumber())
            {
                name = filterBank.toString(filternumber, featureNamePrefix(featurenumber));
                break;
            }
            if(stereofilternumber >= 0 && stereofilternumber < stereoFilter.getNumber())
            {
                name = stereoFilter.toString(stereofilternumber, featureNamePrefix(featurenumber));
                break;
            }
            if(dogfilternumber >= 0 && dogfilternumber < dogFilter.getNumber())
            {
                name = dogFilter.toString(dogfilternumber, featureNamePrefix(featurenumber));
            } else
            {
                name = "unknown feature " + featurenumber;
                System.err.println("Error trying to create incorrect featurenumber " + featurenumber + " " + filternumber + " " + stereofilternumber + " " + dogfilternumber);
            }
            break;
        }
        return name;
    }

    public static float[] distanceToCenterHorizontal(int width, int height)
    {
        float xmax = (float)width / 2.0F;
        float ymax = (float)height / 2.0F;
        float t[] = new float[width * height];
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                float xd = (float)x - (float)width / 2.0F;
                t[y * width + x] = xd / xmax;
            }

        }

        return t;
    }

    public static float[] distanceToCenterVertical(int width, int height)
    {
        float xmax = (float)width / 2.0F;
        float ymax = (float)height / 2.0F;
        float t[] = new float[width * height];
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                float yd = (float)y - (float)height / 2.0F;
                t[y * width + x] = yd / ymax;
            }

        }

        return t;
    }

    protected static final float scales[] = {
        2.0F, 4F, 8F, 16F
    };
    protected static final float hessian_scales[] = {
        8F, 16F, 32F
    };
    protected static final float shift_scales[] = {
        0.0F
    };
    protected static final float xshifts[] = {
        -6F, -5.5F, -5F, -4.5F, -4F, -3.5F, -3F, -2.5F, -2F, -1.5F, 
        -1F, -0.5F, 0.0F, 0.5F, 1.0F, 1.5F, 2.0F, 2.5F, 3F, 3.5F, 
        4F, 4.5F, 5F, 5.5F, 6F, 6.5F, 7F
    };
    protected static final float dog_scales[] = {
        1.0F, 2.0F, 4F
    };
    protected static final float dog_xshifts[] = {
        -8F, -6F, -4F, -2F, 0.0F, 2.0F, 4F, 6F, 8F
    };
    protected static FilterBank filterBank;
    protected static StereoFilterBank stereoFilter;
    protected static DOGFilterBank dogFilter;

}
