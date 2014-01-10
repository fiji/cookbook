// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   HoughTransform.java

package registration;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.Vector;

// Referenced classes of package registration:
//            LocatedCircle

public class HoughTransform
{

    public HoughTransform(int width, int height, double threshold)
    {
        xoffset = 0;
        yoffset = 0;
        this.width = width;
        this.height = height;
        this.threshold = threshold;
        maxvalue = -1.7976931348623157E+308D;
    }

    public HoughTransform(Rectangle roi, double threshold)
    {
        xoffset = roi.x;
        yoffset = roi.y;
        width = roi.width;
        height = roi.height;
        this.threshold = threshold;
        maxvalue = -1.7976931348623157E+308D;
    }

    public void circleTransform(float image[], int imagewidth, double radius)
    {
        this.radius = radius;
        int amax = (int)Math.round(8D * radius);
        int a[][] = new int[amax][2];
        int i = 0;
        for(int j = 0; j < a.length; j++)
        {
            double theta = (6.2831853071795862D * (double)j) / (double)a.length;
            int rhoj = (int)Math.round(radius * Math.cos(theta));
            int thetaj = (int)Math.round(radius * Math.sin(theta));
            if(i == 0 || rhoj != a[i][0] && thetaj != a[i][1])
            {
                a[i][0] = rhoj;
                a[i][1] = thetaj;
                i++;
            }
        }

        amax = i;
        compute(a, image, imagewidth);
    }

    public static float[] line(float image[], int width, int distanceBins, int thetaBins)
    {
        int height = image.length / width;
        float a[] = new float[thetaBins * distanceBins];
        double thetaStep = 3.1415926535897931D / (double)thetaBins;
        double rStep = Math.max(width, height) / distanceBins;
        for(int y = 0; y < height; y++)
        {
            IJ.showStatus("Hough: " + y);
label0:
            for(int x = 0; x < width; x++)
            {
                double d = image[y * width + x];
                if(d <= 0.0D)
                    continue;
                double dx = (double)x - (double)width / 2D;
                double dy = (double)y - (double)height / 2D;
                int tix = 0;
                do
                {
                    if(tix >= thetaBins)
                        continue label0;
                    double theta = (double)tix * thetaStep;
                    double r = dx * Math.cos(theta) + dy * Math.sin(theta);
                    if(r < 0.0D)
                    {
                        r = -r;
                        theta += 3.1415926535897931D;
                    }
                    int rix = (int)Math.round(r / rStep);
                    if(rix >= distanceBins)
                        IJ.log("Error in line Hough rix " + rix + " theta" + theta + " ix" + tix + " x=" + x + "y=" + y);
                    a[tix * distanceBins + rix]++;
                    tix++;
                } while(true);
            }

        }

        return a;
    }

    protected void compute(int a[][], float pixels[], int pixelswidth)
    {
        values = new float[height * width];
        double maxd = -1.7976931348623157E+308D;
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                double d = pixels[(y + yoffset) * pixelswidth + (x + xoffset)];
                if(d <= threshold)
                    continue;
                for(int i = 0; i < a.length; i++)
                {
                    int centerj = y + a[i][0];
                    int centeri = x + a[i][1];
                    if(centerj < 0 || centerj >= height || centeri < 0 || centeri >= width)
                        continue;
                    values[centerj * width + centeri]++;
                    if((double)values[centerj * width + centeri] > maxvalue)
                        maxvalue = values[centerj * width + centeri];
                }

            }

        }

        maxvalue = maxd * (double)a.length;
    }

    public float[] getValues()
    {
        return values;
    }

    public float[] getCenter()
    {
        int bestx = 0;
        int besty = 0;
        double max = -1.7976931348623157E+308D;
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
                if((double)values[y * width + x] / maxvalue > max)
                {
                    max = (double)values[y * width + x] / maxvalue;
                    bestx = x;
                    besty = y;
                }

        }

        float result[] = new float[2];
        result[0] = bestx;
        result[1] = besty;
        return result;
    }

    public Vector getCircles(int maxc, double scale)
    {
        float copy[] = (float[])(float[])values.clone();
        Vector centers = new Vector();
        double maxd = -1.7976931348623157E+308D;
        double lastmaxd = 0.0D;
        int c = 0;
        do
        {
            if(c >= maxc)
                break;
            lastmaxd = maxd;
            maxd = -1.7976931348623157E+308D;
            int maxdx = 0;
            int maxdy = 0;
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                    if((double)copy[y * width + x] > maxd)
                    {
                        maxd = copy[y * width + x];
                        maxdx = x;
                        maxdy = y;
                    }

            }

            if(maxd < lastmaxd / 2D)
                break;
            double confidence = maxd / maxvalue;
            LocatedCircle hc = new LocatedCircle(maxdx, maxdy, radius, confidence);
            centers.addElement(hc);
            int annihRadius = (int)Math.max(Math.round(scale), 2L);
            for(int y = maxdy - annihRadius; y < maxdy + annihRadius; y++)
            {
                for(int x = maxdx - annihRadius; x < maxdx + annihRadius; x++)
                    if(x >= 0 && x < width && y >= 0 && y < height)
                        copy[y * width + x] = 0.0F;

            }

            c++;
        } while(true);
        return centers;
    }

    public LocatedCircle getLargestCircle(float image[], int imagewidth, int imageheight)
    {
        double minRadius = Math.min(width, height) / 2 - 30;
        double maxRadius = Math.min(width, height) / 2 + 30;
        double radius = minRadius;
        int bestCenterx = 0;
        int bestCentery = 0;
        double max = -1.7976931348623157E+308D;
        for(; radius < maxRadius; radius += 5D)
        {
            IJ.showStatus("Fitting circle " + (100D * (radius - minRadius)) / (maxRadius - minRadius) + "%");
            circleTransform(image, imagewidth, radius);
            for(int y = 0; y < height; y++)
            {
                for(int x = 0; x < width; x++)
                    if((double)values[y * width + x] > max)
                    {
                        max = values[y * width + x];
                        bestCenterx = x;
                        bestCentery = y;
                    }

            }

        }

        LocatedCircle hc = new LocatedCircle(bestCenterx, bestCentery, radius, max);
        return hc;
    }

    public LocatedCircle getCircle(float image[], int imagewidth, int imageheight, float radius)
    {
        int bestCenterx = 0;
        int bestCentery = 0;
        double max = -1.7976931348623157E+308D;
        IJ.showStatus("Fitting circle...");
        circleTransform(image, imagewidth, radius);
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
                if((double)values[y * width + x] > max)
                {
                    max = values[y * width + x];
                    bestCenterx = x;
                    bestCentery = y;
                }

        }

        LocatedCircle hc = new LocatedCircle(bestCenterx, bestCentery, radius, max);
        return hc;
    }

    public ImageProcessor getImageProcessor()
    {
        if(values == null)
            return null;
        else
            return new FloatProcessor(width, height, values, null);
    }

    protected int width;
    protected int height;
    protected float values[];
    protected int xoffset;
    protected int yoffset;
    protected double radius;
    protected double maxvalue;
    public double threshold;
}
