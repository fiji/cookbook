package fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Nucleus_Counter implements PlugIn {

	@Override
	public void run(final String arg) {
		final int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.error("No images are open.");
			return;
		}
		final ImagePlus imp1 = WindowManager.getCurrentImage();
		final ImageProcessor ip1 = imp1.getProcessor();

		if (imp1.getType() != ImagePlus.GRAY8 && imp1.getType() != ImagePlus.GRAY16)
		{
			IJ.showMessage("Error", "Plugin requires 8- or 16-bit image");
			return;
		}

		int max = (int) Prefs.get("NC_max.int", 5000);
		int min = (int) Prefs.get("NC_min.int", 50);
		boolean bg = Prefs.get("NC_bg.boolean", true);

		int threshIndex = (int) Prefs.get("NC_threshIndex.int", 0);
		int smoothIndex = (int) Prefs.get("NC_smoothIndex.int", 0);
		boolean watershed = Prefs.get("NC_watershed.boolean", true);
		boolean summarize = Prefs.get("NC_summarize.boolean", true);
		boolean add = Prefs.get("NC_record.boolean", true);

		int minthreshold = (int) ip1.getMinThreshold();
		int maxthreshold = (int) ip1.getMaxThreshold();

		final String[] thresholds =
			{ "Current", "Otsu", "Max. Entropy", "Mixture Modelling",
				"k-means Clustering", "Adaptive" };
		final String[] smooths =
			{ "None", "Mean 3x3", "Mean 5x5", "Median 3x3", "Median 5x5" };

		final GenericDialog gd = new GenericDialog("Nucleus Counter");
		gd.addNumericField("Smallest Particle Size", min, 0);
		gd.addNumericField("Largest Particle Size", max, 0);
		gd.addChoice("Threshold Method", thresholds, thresholds[threshIndex]);
		gd.addChoice("Smooth Method", smooths, smooths[smoothIndex]);
		gd.addCheckbox("Subtract Background", bg);
//	gd.addCheckbox("Smooth prior to segmentation",smooth);
		gd.addCheckbox("Watershed filter", watershed);
		gd.addCheckbox("Add Particles to ROI manager", add);
//	gd.addCheckbox("Show full statistics",fullStats);
		gd.addCheckbox("Show summary", summarize);

		gd.showDialog();
		if (gd.wasCanceled()) return;
		min = (int) gd.getNextNumber();
		max = (int) gd.getNextNumber();
		threshIndex = gd.getNextChoiceIndex();
		smoothIndex = gd.getNextChoiceIndex();
		bg = gd.getNextBoolean();

		watershed = gd.getNextBoolean();
		add = gd.getNextBoolean();
//	fullStats=gd.getNextBoolean();
		summarize = gd.getNextBoolean();

		Prefs.set("NC_max.int", max);
		Prefs.set("NC_min.int", min);
		Prefs.set("NC_bg.boolean", bg);
		Prefs.set("NC_watershed.boolean", watershed);
		Prefs.set("NC_smoothIndex.int", smoothIndex);
		Prefs.set("NC_threshIndex.int", threshIndex);
		Prefs.set("NC_summarize.boolean", summarize);
		Prefs.set("NC_record.boolean", add);
//	Prefs.set("NC_fullStats.boolean",fullStats);

//duplicate and bg subtract subtract BG
		final ImageProcessor ip2 = ip1.duplicate();
		new ImagePlus("Analysis", ip2).show();
		IJ.run("Grays");

		final ImagePlus imp2 = WindowManager.getCurrentImage();
		final ImageWindow winimp2 = imp2.getWindow();
		if (bg && threshIndex != 0) IJ.run("Subtract Background...", "rolling=50");

//duplicate this for image thresholding
		final ImageProcessor ip3 = ip2.duplicate();
		ip3.setThreshold(minthreshold, maxthreshold, 4);
		new ImagePlus("Threshold", ip3).show();
		final ImagePlus imp3 = WindowManager.getCurrentImage();
		final ImageWindow winimp3 = imp3.getWindow();
		ImagePlus imp5 = null;
		ImageWindow winimp5 = null;
		// IJ.showMessage("thres="+threshIndex);

//set threshold

		if (smoothIndex == 1) IJ.run("Mean...", "radius=2 separable");
		else if (smoothIndex == 2) IJ.run("Mean...", "radius=3 separable");
		else if (smoothIndex == 3) IJ.run("Median...", "radius=2");
		else if (smoothIndex == 4) IJ.run("Median...", "radius=3");

		WindowManager.setCurrentWindow(winimp3);

		if (threshIndex == 1 && imp1.getType() == ImagePlus.GRAY8) {
			IJ.run("OtsuThresholding 8Bit");
		}
		if (threshIndex == 1 && imp1.getType() == ImagePlus.GRAY16) {
			IJ.run("OtsuThresholding 16Bit");
		}
		if (threshIndex == 2) {
			IJ.run("8-bit");
			IJ.run("Entropy Threshold");
		}
		if (threshIndex == 3) {
			IJ.run("8-bit");
			IJ.run("Mixture Modeling threshold");
		}

		minthreshold = (int) ip3.getMinThreshold();
		maxthreshold = (int) ip3.getMaxThreshold();
		WindowManager.setCurrentWindow(winimp3);
		IJ.setThreshold(minthreshold, maxthreshold);

		if (threshIndex == 5) {
			WindowManager.setCurrentWindow(winimp3);
			IJ.run("Adapative3DThreshold ");
			imp5 = WindowManager.getCurrentImage();
			winimp5 = imp5.getWindow();
			WindowManager.setCurrentWindow(winimp5);
			IJ.run("Rename...", "title=Threshold");
			IJ.setThreshold(1, 255);
			imp5.changes = false;
		}

		if (threshIndex == 4) {
			WindowManager.setCurrentWindow(winimp3);
			IJ.run("k-means Clustering",
				"number=2 cluster=0.00010000 randomization=48");
			imp5 = WindowManager.getCurrentImage();
			winimp5 = imp5.getWindow();
			WindowManager.setCurrentWindow(winimp5);
			IJ.run("Rename...", "title=Threshold");
			IJ.run("Invert");
			IJ.setThreshold(2, 2);
			imp5.changes = false;
		}

		// IJ.showMessage("Min="+minthreshold+"   Max = "+maxthreshold);

		IJ.run("Convert to Mask");
//watershed
		if (watershed) IJ.run("Watershed");
		IJ.setThreshold(128, 255);

		final ImagePlus mask = WindowManager.getCurrentImage();

//IJ.run("Analyze Particles...", "size=0.65-647.67 circularity=0.00-1.00 show=Outlines display exclude clear summarize");

//analyze particles
		String analyseStr =
			"size=" + min + "-" + max + " circularity=0.00-1.00 show=";
		analyseStr += "Nothing";
		// analyseStr+=" display";
		analyseStr += " exclude clear";

		if (summarize) analyseStr += " summarize";
		if (add) analyseStr += " add";
		// if (fullStats) analyseStr+=" size";

//IJ.showMessage(analyseStr);

		IJ.run("Analyze Particles...", analyseStr + " add");

//get Outline image
		// ImagePlus imp4 = WindowManager.getCurrentImage();
		// ImageProcessor ip4 = imp4.getProcessor();
		// ImageWindow winimp4 = imp4.getWindow();
		// IJ.run("Rename...", "title=boundaries");

		// IJ.run("Invert");
		// WindowManager.setCurrentWindow(winimp2);
		//
		// IJ.run("8-bit");
		// IJ.run("Image Calculator...",
		// "image1='Analysis' operation=Subtract image2='boundaries' ");
		// WindowManager.setCurrentWindow(winimp4);
		// IJ.run("Red");
		// IJ.run("RGB Color");

//get duplicate image		
		// WindowManager.setCurrentWindow(winimp2);
		// IJ.run("RGB Color");
		// IJ.run("Image Calculator...",
		// "image1='Analysis' operation=Add image2='boundaries' create ");

		imp2.changes = false;
		winimp2.close();
//imp3.changes = false;imp4.changes = false;

//	winimp4.close();

		if (!add || threshIndex == 4) {
			winimp3.close();
		}
		if (threshIndex == 4 || threshIndex == 5 && !add) {
			imp5.changes = false;
			winimp5.close();
		}

		if (add) {
			if (threshIndex == 4) {
				WindowManager.setCurrentWindow(winimp5);
				IJ.setThreshold(128, 255);
			}
			else {
				WindowManager.setCurrentWindow(winimp3);
				IJ.setThreshold(minthreshold, maxthreshold);
			}
			// IJ.run("Multi Measure");
			// IJ.showMessage("Click on MultiMeasure 'Add Particles' button\nnext after clearing existing list if required. \nThen close 'Threshold' image.");

		}
		mask.changes = false;
		mask.close();

	}

}
