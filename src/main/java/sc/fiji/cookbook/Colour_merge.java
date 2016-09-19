package sc.fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;

public class Colour_merge implements PlugIn {

	@Override
	public void run(final String arg) {
		final int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.error("No images are open.");
			return;
		}

		final String[] titles = new String[wList.length + 1];
		for (int i = 0; i < wList.length; i++) {
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp != null ? imp.getTitle() : "";
		}
		final String none = "*None*";
		titles[wList.length] = none;
		final String[] pscolours =
			{ "<Current>", "Cyan", "Magenta", "Yellow", "Red", "Green", "Blue",
				"Grays" };

		final GenericDialog gd = new GenericDialog("Colour Merge");
		gd.addChoice("First Stack:", titles, titles[0]);
		gd.addChoice("First colour", pscolours, pscolours[0]);

		gd.addChoice("Second Stack:", titles, titles[1]);

		gd.addChoice("Second colour", pscolours, pscolours[0]);
		gd.addCheckbox("Use 'Difference' operator?", false);
		gd.addCheckbox("Keep source stacks?", true);
		gd.addNumericField("% of 2 pre-subtracted from 1?", 0, 0);
		gd.addMessage("When merging brightfield with  fluorescence,\nensure the brightfield image is the first stack");

		gd.showDialog();

		if (gd.wasCanceled()) return;
		final int[] index = new int[3];
		final int[] colourindex = new int[3];

		index[0] = gd.getNextChoiceIndex();
		colourindex[0] = gd.getNextChoiceIndex();

		index[1] = gd.getNextChoiceIndex();

		colourindex[1] = gd.getNextChoiceIndex();

		final boolean UseDiff = gd.getNextBoolean();
		final boolean keep = gd.getNextBoolean();
		double preSub = gd.getNextNumber();

		final ImagePlus impCh1 = WindowManager.getImage(wList[index[0]]);
		final ImagePlus impCh2 = WindowManager.getImage(wList[index[1]]);

		final String firstcol = pscolours[colourindex[0]];
		final String secondcol = pscolours[colourindex[1]];

		final ImagePlus[] image = new ImagePlus[3];

		int width = 0;
		for (int i = 0; i < 3; i++) {
			if (index[i] < wList.length) {
				image[i] = WindowManager.getImage(wList[index[i]]);
				width = image[i].getWidth();
			}
		}
		if (width == 0) {
			IJ.error("There must be at least one 8-bit or RGB source stack.");
			return;
		}

//get origina magenta image

		final ImageWindow winCh1 = impCh1.getWindow();
		WindowManager.setCurrentWindow(winCh1);

//duplicate and assign vars
		IJ.run("Duplicate...", "title=Ch1 duplicate");
		IJ.selectWindow("Ch1");
		final ImagePlus impCh1B = WindowManager.getCurrentImage();
		final ImageWindow winCh1B = impCh1B.getWindow();

//get orignial cyan image
		final ImageWindow winCh2 = impCh2.getWindow();
		WindowManager.setCurrentWindow(winCh2);
//Duplicate and assign vars

		IJ.run("Duplicate...", "title=Ch2 duplicate");
		final ImagePlus impCh2B = WindowManager.getCurrentImage();
		final ImageWindow winCh2B = impCh2B.getWindow();

		if (preSub != 0) {
			WindowManager.setCurrentWindow(winCh2B);
			IJ.run("Duplicate...", "title=Ch2C duplicate");
			final ImagePlus impCh2C = WindowManager.getCurrentImage();

			final ImageWindow winCh2C = impCh2C.getWindow();
			WindowManager.setCurrentWindow(winCh2C);
			preSub = preSub / 100;
			IJ.run("Multiply...", "value=" + preSub);
			IJ.run("Image Calculator...", "image1=Ch1 operation=Subtract image2=Ch2C");
			impCh2C.changes = false;
			winCh2C.close();
		}

		WindowManager.setCurrentWindow(winCh2B);
		if (secondcol != "<Current>") IJ.run(secondcol);
		IJ.run("RGB Color");

		WindowManager.setCurrentWindow(winCh1B);
		if (firstcol != "<Current>") IJ.run(firstcol);
		IJ.run("RGB Color");

//merge
		if (UseDiff == false) IJ.run("Image Calculator...",
			"image1='Ch1' operation=Add  image2=Ch2 stack");

		if (UseDiff == true) IJ.run("Image Calculator...",
			"image1='Ch1' operation=Difference image2=Ch2 stack");

//rename merge
		IJ.run("Rename...", "title='Colour merge");

		impCh2B.changes = false;

		if (!keep) {
			winCh2.close();
			winCh1.close();
		}
		winCh2B.close();
		IJ.selectWindow("Ch1");
		IJ.run("Rename...", "title='Colour merge'");
	}

}
