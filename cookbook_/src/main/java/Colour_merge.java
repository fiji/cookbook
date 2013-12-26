import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class Colour_merge implements PlugIn {

	public void run(String arg) {
         int[] wList = WindowManager.getIDList();
        if (wList==null) {
            IJ.error("No images are open.");
            return;
        }

        String[] titles = new String[wList.length+1];
        for (int i=0; i<wList.length; i++) {
            ImagePlus imp = WindowManager.getImage(wList[i]);
            titles[i] = imp!=null?imp.getTitle():"";
        }
        String none = "*None*";
        titles[wList.length] = none;
int pscol1 = 1;
int pscol2 = 2;
String[] pscolours =  { "<Current>","Cyan", "Magenta", "Yellow", "Red", "Green","Blue", "Grays"};


        GenericDialog gd = new GenericDialog("Colour Merge");
        gd.addChoice("First Stack:", titles, titles[0]);
      gd.addChoice("First colour", pscolours, pscolours[0]);

        gd.addChoice("Second Stack:", titles, titles[1]);



      gd.addChoice("Second colour", pscolours, pscolours[0]);
 gd.addCheckbox("Use 'Difference' operator?", false);
 gd.addCheckbox("Keep source stacks?", true);
gd.addNumericField("% of 2 pre-subtracted from 1?",0,0);
gd.addMessage("When merging brightfield with  fluorescence,\nensure the brightfield image is the first stack");


        String title3 = titles.length>2?titles[2]:none;

      
        gd.showDialog();

        if (gd.wasCanceled())
            return;
        int[] index = new int[3];
        int[] colourindex = new int [3];

        index[0] = gd.getNextChoiceIndex();
   colourindex[0] = gd.getNextChoiceIndex();

        index[1] = gd.getNextChoiceIndex();

        colourindex [1] = gd.getNextChoiceIndex();

       boolean UseDiff = gd.getNextBoolean();
  boolean keep = gd.getNextBoolean();
double preSub = gd.getNextNumber();


ImagePlus impCh1 = WindowManager.getImage(wList[index[0]]);
ImagePlus impCh2 = WindowManager.getImage(wList[index[1]]);

String firstcol = pscolours[colourindex[0]];
String secondcol = pscolours[colourindex[1]];

        ImagePlus[] image = new ImagePlus[3];



        int stackSize = 0;
        int width = 0;
        int height = 0;

        for (int i=0; i<3; i++) {
            if (index[i]<wList.length) {
                image[i] = WindowManager.getImage(wList[index[i]]);
                width = image[i].getWidth();
                height = image[i].getHeight();
                stackSize = image[i].getStackSize();
            }
        }
        if (width==0) {
            IJ.error("There must be at least one 8-bit or RGB source stack.");
            return;
        }
        
//get origina magenta image

ImageWindow winCh1 = impCh1.getWindow();
WindowManager.setCurrentWindow(winCh1);

//duplicate and assign vars
IJ.run("Duplicate...", "title=Ch1 duplicate");
IJ.selectWindow("Ch1");
ImagePlus impCh1B = WindowManager.getCurrentImage();
ImageWindow winCh1B= impCh1B.getWindow();

//get orignial cyan image
ImageWindow winCh2 = impCh2.getWindow();
WindowManager.setCurrentWindow(winCh2);
//Duplicate and assign vars

IJ.run("Duplicate...", "title=Ch2 duplicate");
ImagePlus impCh2B = WindowManager.getCurrentImage();
ImageWindow winCh2B = impCh2B.getWindow();

if (preSub!=0){
WindowManager.setCurrentWindow(winCh2B);
IJ.run("Duplicate...", "title=Ch2C duplicate");
ImagePlus impCh2C = WindowManager.getCurrentImage();

ImageWindow winCh2C = impCh2C.getWindow();
WindowManager.setCurrentWindow(winCh2C);
preSub = preSub/100;
IJ.run("Multiply...", "value="+preSub);
IJ.run("Image Calculator...", "image1=Ch1 operation=Subtract image2=Ch2C");
impCh2C.changes = false;
winCh2C.close();
}

WindowManager.setCurrentWindow(winCh2B);
if (secondcol!="<Current>") IJ.run(secondcol);
IJ.run("RGB Color");

WindowManager.setCurrentWindow(winCh1B);
if (firstcol!="<Current>") IJ.run(firstcol);
IJ.run("RGB Color");

//merge
if (UseDiff==false)
IJ.run("Image Calculator...", "image1='Ch1' operation=Add  image2=Ch2 stack");

if (UseDiff==true)
IJ.run("Image Calculator...", "image1='Ch1' operation=Difference image2=Ch2 stack");

//rename merge
IJ.run("Rename...", "title='Colour merge");



impCh2B.changes = false;

if (!keep){
winCh2.close();
winCh1.close();}
winCh2B.close();
IJ.selectWindow("Ch1");
		IJ.run("Rename...", "title='Colour merge'");
	}

}
