// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   Open_Different_Sizes_As_Stack.java

package io;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.OpenDialog;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.util.StringSorter;

import java.io.File;

public class Open_Different_Sizes_As_Stack
    implements PlugIn
{

    public Open_Different_Sizes_As_Stack()
    {
    }

    public void run(String arg)
    {
        int height;
        int width = height = 0;
        OpenDialog od = new OpenDialog("Open Different Sizes As Stack", arg);
        String directory = od.getDirectory();
        String name = od.getFileName();
        if(name == null)
            return;
        String list[] = (new File(directory)).list();
        if(list == null)
            return;
        StringSorter.sort(list);
        if(IJ.debugMode)
            IJ.write("FolderOpener: " + directory + " (" + list.length + " files)");
        ImagePlus impl = null;
        for(int i = 0; i < list.length; i++)
        {
            ImagePlus imp = (new Opener()).openImage(directory, list[i]);
            if(imp == null)
            {
                IJ.write(list[i] + ": unable to open");
                continue;
            }
            if(width < imp.getWidth() || height < imp.getHeight())
            {
                width = imp.getWidth();
                height = imp.getHeight();
                impl = imp;
            }
        }

        ImageStack stack = new ImageStack(width, height);
        for(int i = 0; i < list.length; i++)
        {
            ImagePlus imp = (new Opener()).openImage(directory, list[i]);
            String title = imp.getTitle();
            IJ.showStatus((stack.getSize() + 1) + "/" + list.length + ": " + title);
            if(imp == null)
            {
                IJ.write(list[i] + ": unable to open");
                continue;
            }
            FloatProcessor ip = new FloatProcessor(width, height);
            imp.getProcessor().flipHorizontal();
            float f[] = (float[])(float[])ip.getPixels();
            Object p = imp.getProcessor().getPixels();
            if(p instanceof byte[])
            {
                byte b[] = (byte[])(byte[])p;
                for(int y = 0; y < height; y++)
                {
                    int offset = y * imp.getWidth();
                    for(int x = 0; x < width; x++)
                        if(y < imp.getHeight() && x < imp.getWidth())
                            f[y * width + x] = b[offset + x] & 0xff;
                        else
                            f[y * width + x] = 0.0F;

                }

            } else
            if(p instanceof short[])
            {
                short u[] = (short[])(short[])p;
                for(int y = 0; y < height; y++)
                {
                    int offset = y * imp.getWidth();
                    for(int x = 0; x < width; x++)
                        if(y < imp.getHeight() && x < imp.getWidth())
                            f[y * width + x] = u[offset + x];
                        else
                            f[y * width + x] = 0.0F;

                }

            } else
            if(p instanceof float[])
            {
                float ff[] = (float[])(float[])p;
                for(int y = 0; y < height; y++)
                {
                    int offset = y * imp.getWidth();
                    for(int x = 0; x < width; x++)
                        if(y < imp.getHeight() && x < imp.getWidth())
                            f[y * width + x] = ff[offset + x];
                        else
                            f[y * width + x] = 0.0F;

                }

            } else
            if(p instanceof int[])
            {
                int ii[] = (int[])(int[])p;
                for(int y = 0; y < height; y++)
                {
                    int offset = y * imp.getWidth();
                    for(int x = 0; x < width; x++)
                        if(y < imp.getHeight() && x < imp.getWidth())
                        {
                            int c = ii[offset + x];
                            int r = (c & 0xff0000) >> 16;
                            int g = (c & 0xff00) >> 8;
                            int b = c & 0xff;
                            f[y * width + x] = (float)((double)r * 0.29999999999999999D + (double)g * 0.58999999999999997D + (double)b * 0.11D);
                        } else
                        {
                            f[y * width + x] = 0.0F;
                        }

                }

            }
            stack.addSlice(title, ip);
        }

        stack.setColorModel(impl.getProcessor().getColorModel());
        if(stack.getSize() > 1)
        {
            ImagePlus imp = new ImagePlus(name, stack);
            imp.show();
        }
        IJ.register(io.Open_Different_Sizes_As_Stack.class);
    }
}
