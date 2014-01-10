// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   FileAndString.java

package io;

import java.io.File;

public class FileAndString
    implements Comparable
{

    public FileAndString(File file, String string)
    {
        this.file = file;
        this.string = string;
    }

    public int compareTo(Object o)
    {
        return string.compareTo(((FileAndString)o).string);
    }

    public File getFile()
    {
        return file;
    }

    public String getString()
    {
        return string;
    }

    File file;
    String string;
}
