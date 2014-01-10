// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OpenPreviewDialog.java

package io;

import java.io.File;
import java.io.FileFilter;

class DirFilter
    implements FileFilter
{

    DirFilter()
    {
    }

    public boolean accept(File dir)
    {
        return dir.isDirectory();
    }
}
