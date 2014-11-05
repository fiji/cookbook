// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   LocatedCircle.java

package registration;

import bijnum.BIJutil;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.util.Enumeration;
import java.util.Vector;

public class LocatedCircle
{

    public LocatedCircle(double x, double y, double radius, double confidence)
    {
        this.x = (float)x;
        this.y = (float)y;
        this.radius = (float)radius;
        this.confidence = (float)confidence;
    }

    public LocatedCircle(double x, double y, double radius)
    {
        this.x = (float)x;
        this.y = (float)y;
        this.radius = (float)radius;
        confidence = 0.0F;
    }

    public double getX()
    {
        return (double)x;
    }

    public double getY()
    {
        return (double)y;
    }

    public double getConfidence()
    {
        return (double)confidence;
    }

    public double getRadius()
    {
        return (double)radius;
    }

    public void setRadius(double radius)
    {
        this.radius = (float)radius;
    }

    public void drawOutline(ImagePlus imp)
    {
        OvalRoi circleroi = new OvalRoi((int)(x - radius), (int)(y - radius), (int)(2.0F * radius), (int)(2.0F * radius), imp);
        circleroi.drawPixels();
    }

    public void drawCenter(ImageProcessor ip)
    {
        int i = (int)x;
        int j = (int)y;
        for(int k = -10; k <= 10; k++)
        {
            ip.putPixel(i, j + k, 255);
            ip.putPixel(i + k, j, 255);
        }

        for(int k = -2; k <= 2; k++)
        {
            ip.putPixel(i + k, j - 2, 255);
            ip.putPixel(i + k, j + 2, 255);
            ip.putPixel(i + 2, j + k, 255);
            ip.putPixel(i - 2, j + k, 255);
        }

    }

    public OvalRoi getRoi(int width, int height)
    {
        int fitDiameter = (int)Math.min(width, Math.min(height, 2.0F * radius));
        OvalRoi roi = new OvalRoi((int)Math.min(Math.max(0.0F, x - radius), width - fitDiameter), (int)Math.min(Math.max(0.0F, y - radius), height - fitDiameter), fitDiameter, fitDiameter);
        return roi;
    }

    public ImageProcessor getCircleCentersImageProcessor(ImagePlus imp, Vector circles)
    {
        ImageProcessor ip = imp.getProcessor();
        int width = imp.getWidth();
        int height = imp.getHeight();
        byte plane[] = (byte[])(byte[])ip.getPixels();
        for(Enumeration e = circles.elements(); e.hasMoreElements();)
        {
            LocatedCircle hc = (LocatedCircle)e.nextElement();
            OvalRoi circleroi = new OvalRoi((int)(hc.getX() - hc.getRadius()), (int)(hc.getY() - hc.getRadius()), (int)(2D * hc.getRadius()), (int)(2D * hc.getRadius()), imp);
            circleroi.drawPixels();
            int i = (int)hc.getX();
            int j = (int)hc.getY();
            int k;
            for(k = -10; k <= 10; k++)
            {
                if(j + k >= 0 && j + k < height && i >= 0 && i < width)
                    plane[(j + k) * width + i] = -1;
                if(j >= 0 && j < height && i + k >= 0 && i + k < width)
                    plane[j * width + i + k] = -1;
            }

            k = -2;
            while(k <= 2) 
            {
                if(j - 2 >= 0 && j - 2 < height && i + k >= 0 && i + k < width)
                    plane[(j - 2) * width + i + k] = -1;
                if(j + 2 >= 0 && j + 2 < height && i + k >= 0 && i + k < width)
                    plane[(j + 2) * width + i + k] = -1;
                if(j + k >= 0 && j + k < height && i - 2 >= 0 && i - 2 < width)
                    plane[((j + k) * width + i) - 2] = -1;
                if(j + k >= 0 && j + k < height && i + 2 >= 0 && i + 2 < width)
                    plane[(j + k) * width + i + 2] = -1;
                k++;
            }
        }

        return ip;
    }

    public float[] getMask(int width, int height)
    {
        ImageProcessor mp = new ByteProcessor(width, height);
        ImagePlus np = new ImagePlus("mask", mp);
        int fitDiameter = (int)Math.min(width, Math.min(height, 2.0F * radius));
        OvalRoi roi = new OvalRoi((int)Math.min(Math.max(0.0F, x - radius), width - fitDiameter), (int)Math.min(Math.max(0.0F, y - radius), height - fitDiameter), fitDiameter, fitDiameter);
        return BIJutil.getMask(roi, width, height);
    }

    public static float[] getMask(LocatedCircle c, float reference[], int width)
    {
        float mask[] = c.getMask(width, reference.length / width);
        float clone[] = new float[reference.length];
        for(int i = 0; i < clone.length; i++)
            clone[i] = reference[i];

        ImagePlus overlay = new ImagePlus("Can be discarded, confirmation only: reference FOV", new FloatProcessor(width, reference.length / width, clone, null));
        c.drawCenter(overlay.getProcessor());
        c.drawOutline(overlay);
        overlay.show();
        return mask;
    }

    public String toString()
    {
        return "Center at " + x + ", " + y + ", radius " + radius + " at " + confidence;
    }

    protected float x;
    protected float y;
    protected float radius;
    protected float confidence;
}
