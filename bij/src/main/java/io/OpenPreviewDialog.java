// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   OpenPreviewDialog.java

package io;

import ij.IJ;
import ij.Prefs;
import ij.io.OpenDialog;
import java.awt.Button;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Label;
import java.awt.List;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextComponent;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

// Referenced classes of package io:
//            FileAndString

public class OpenPreviewDialog
    implements ActionListener, ItemListener, WindowListener
{

    public OpenPreviewDialog(String title, Object filter, String directory)
    {
        canceling = true;
        canListRoots = true;
        this.filter = filter;
        try
        {
            File.listRoots();
        }
        catch(Exception ignored)
        {
            canListRoots = false;
        }
        catch(Error ignored)
        {
            canListRoots = false;
        }
        if(!canListRoots)
        {
            OpenDialog od = new OpenDialog("Select any OID file to select directory...", null);
            OpenPreviewDialog _tmp = this;
            directory = od.getDirectory();
        }
        OpenPreviewDialog _tmp1 = this;
        if(directory == null && directory == null)
        {
            OpenPreviewDialog _tmp2 = this;
            directory = Prefs.getString("dir.image");
        } else
        {
            OpenPreviewDialog _tmp3 = this;
            if(directory == null)
            {
                OpenPreviewDialog _tmp4 = this;
                directory = directory;
            }
        }
        ij.ImageJ ij = IJ.getInstance();
        Frame parent = ((Frame) (ij == null ? new Frame() : ((Frame) (ij))));
        dialog = new Dialog(parent, title);
        dialog.addWindowListener(this);
        dialog.setModal(false);
        if(canListRoots)
        {
            Panel paneldir = new Panel();
            paneldir.setLayout(new FlowLayout());
            dialog.add(paneldir, "North");
            Label ldir = new Label("Look in:");
            paneldir.add(ldir);
            choiceDir = new Choice();
            choiceDir.addItemListener(this);
            choiceDir.setSize(150, 20);
            paneldir.add(choiceDir);
            showDirs(choiceDir, directory);
            choiceDir.add(" _____________________________");
        }
        list = new List(30, false);
        list.setMultipleMode(true);
        list.addItemListener(this);
        list.addActionListener(this);
        list.setSize(200, 20);
        showFiles(list, directory, filter);
        list.add("________________________________");
        dialog.add(list, "West");
        textarea = new TextArea(30, 40);
        dialog.add(textarea, "East");
        panel = new Panel();
        panel.setLayout(new FlowLayout());
        dialog.add(panel, "South");
        open = new Button("Start");
        panel.add(open);
        open.addActionListener(this);
        cancel = new Button("Close");
        panel.add(cancel);
        cancel.addActionListener(this);
        dialog.pack();
        dialog.show();
    }

    public void itemStateChanged(ItemEvent ie)
    {
        if(ie.getSource() == list)
        {
            File file = validFiles[list.getSelectedIndex()].file;
            if(!file.isDirectory())
            {
                showFileHeader(file);
                return;
            }
            directory = file.getAbsolutePath();
            showDirs(choiceDir, directory);
            showFiles(list, directory, filter);
        } else
        if(ie.getSource() == choiceDir)
        {
            directory = dirs[choiceDir.getSelectedIndex()].file.getAbsolutePath();
            showDirs(choiceDir, directory);
            showFiles(list, directory, filter);
        }
    }

    public void actionPerformed(ActionEvent ae)
    {
        if(ae.getSource() == cancel)
        {
            names = null;
            directory = null;
            canceling = true;
            dialog.dispose();
        } else
        if(ae.getSource() == open)
            selectedFileNames();
    }

    protected void selectedFileNames()
    {
        int indices[] = list.getSelectedIndexes();
        if(indices == null)
        {
            names[0] = validFiles[list.getSelectedIndex()].getFile().getName();
        } else
        {
            names = new String[indices.length];
            for(int i = 0; i < indices.length; i++)
                names[i] = validFiles[indices[i]].getFile().getName();

        }
    }

    public void windowActivated(WindowEvent windowevent)
    {
    }

    public void windowClosed(WindowEvent windowevent)
    {
    }

    public void windowClosing(WindowEvent e)
    {
        if(canceling)
        {
            names = null;
            directory = null;
        }
    }

    public void windowDeactivated(WindowEvent windowevent)
    {
    }

    public void windowDeiconified(WindowEvent windowevent)
    {
    }

    public void windowIconified(WindowEvent windowevent)
    {
    }

    public void windowOpened(WindowEvent windowevent)
    {
    }

    public String[] getFileNames()
    {
        return names;
    }

    public String getDirectory()
    {
        return directory;
    }

    protected void showFileHeader(File file)
    {
        textarea.setText("showFileHeader called");
    }

    protected void showFiles(List list, String directory, Object filter)
    {
        File f = new File(directory);
        File files[] = null;
        if(canListRoots)
        {
            files = f.listFiles((FileFilter)filter);
        } else
        {
            String fileNames[] = f.list();
            int n = 0;
            for(int i = 0; i < fileNames.length; i++)
                if(((FilenameFilter)filter).accept(new File(directory), fileNames[i]))
                    n++;

            files = new File[n];
            int j = 0;
            for(int i = 0; i < fileNames.length; i++)
                if(((FilenameFilter)filter).accept(new File(directory), fileNames[i]))
                    files[j++] = new File(directory, fileNames[i]);

        }
        validFiles = new FileAndString[files.length];
        for(int i = 0; i < files.length; i++)
            validFiles[i] = new FileAndString(files[i], validFileDescription(files[i]));

        try
        {
            java.util.Arrays.sort(validFiles);
        }
        catch(Exception e) { }
        list.removeAll();
        for(int i = 0; i < validFiles.length; i++)
        {
            String name = null;
            if(validFiles[i].file.isDirectory())
            {
                StringBuffer sb = new StringBuffer();
                sb.append("<DIR> ");
                sb.append(validFiles[i].string);
                name = sb.toString();
            } else
            {
                name = validFiles[i].string;
            }
            list.add(name);
        }

    }

    protected String validFileDescription(File file)
    {
        return file.getName();
    }

    protected void showDirs(Choice choiceDir, String directory)
    {
        dirs = getTree(directory);
        choiceDir.removeAll();
        for(int i = 0; i < dirs.length; i++)
            choiceDir.add(dirs[i].string);

        choiceDir.select(indexOfCurrentDir);
    }

    protected FileAndString[] getTree(String path)
    {
        java.util.Vector v = new java.util.Vector();
        File roots[] = File.listRoots();
        File current = new File(path);
        for(int i = 0; i < roots.length; i++)
        {
            FileAndString fas = new FileAndString(roots[i], roots[i].getAbsolutePath());
            v.addElement(fas);
            int j = v.size();
            String rootName = roots[i].getAbsolutePath();
            if(rootName != null && current.getAbsolutePath().startsWith(rootName))
            {
                addPathTree(v, current);
                indexOfCurrentDir = v.size() - 1;
            }
        }

        v.addElement(new FileAndString(null, "                          "));
        FileAndString tree[] = new FileAndString[v.size()];
        for(int i = 0; i < tree.length; i++)
            tree[i] = (FileAndString)v.elementAt(i);

        return tree;
    }

    protected void addPathTree(java.util.Vector v, File current)
    {
        java.util.Vector tv = new java.util.Vector();
        for(; current != null; current = current.getParentFile())
            tv.insertElementAt(current, 0);

        StringBuffer spaces = new StringBuffer(" ");
        for(int i = 0; i < tv.size(); i++)
        {
            File file = (File)tv.elementAt(i);
            FileAndString fas = new FileAndString(file, spaces.toString() + file.getName());
            if(!file.getName().equals(""))
                v.addElement(fas);
            spaces.append("  ");
        }

    }

    protected Dialog dialog;
    protected static String directory;
    protected String names[];
    protected TextArea textarea;
    protected FileAndString validFiles[];
    protected FileAndString dirs[];
    protected List list;
    protected Button open;
    protected Button cancel;
    protected Choice choiceDir;
    protected Object filter;
    protected boolean canListRoots;
    protected boolean canceling;
    protected Panel panel;
    protected int indexOfCurrentDir;
}
