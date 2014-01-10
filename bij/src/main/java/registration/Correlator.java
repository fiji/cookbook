// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Correlator.java

package registration;


public class Correlator
{

    public Correlator()
    {
    }

    public static float[] xcorr(float a[], float b[], int width, int range)
        throws Exception
    {
        int height = a.length / width;
        float xc[] = new float[a.length];
        for(int y = 0; y < height; y++)
        {
            for(int x = 0; x < width; x++)
            {
                float rab = 0.0F;
                float raa = 0.0F;
                float rbb = 0.0F;
                for(int k = -range; k < range; k++)
                {
                    int xi = x + k;
                    if(xi < 0)
                        xi = -xi;
                    else
                    if(xi >= width)
                        xi = 2 * width - xi - 1;
                    float pixela = a[y * width + xi];
                    float pixelb = b[y * width + xi];
                    rab += pixela * pixelb;
                    raa += pixela * pixela;
                    rbb += pixelb * pixelb;
                }

                xc[y * width + x] = rab / ((float)Math.sqrt(raa * rbb) + 1E-13F);
            }

        }

        return xc;
    }
}
