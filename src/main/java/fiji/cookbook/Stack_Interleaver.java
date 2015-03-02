package fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.Color;

/**
 * Interleaves two stacks to create a new stack This plugin is modified from
 * Stack_Combiner (see Wayne Rasband)
 * 
 * @author J. Anthony Parker, MD PhD <J.A.Parker@IEEE.org>
 * @version 20September2001
 */

public class Stack_Interleaver implements PlugIn {

	ImagePlus imp1;
	ImagePlus imp2;

	@Override
	public void run(final String arg) {
		if (!showDialog()) return;
		if (imp1.getType() != imp2.getType()) {
			error();
			return;
		}
		final ImageStack stack1 = imp1.getStack();
		final ImageStack stack2 = imp2.getStack();
		final ImageStack stack3 = interleave(stack1, stack2);
		new ImagePlus("Combined Stacks", stack3).show();
		IJ.register(Stack_Interleaver.class);
	}

	public ImageStack
		interleave(final ImageStack stack1, final ImageStack stack2)
	{
		final int d1 = stack1.getSize();
		final int d2 = stack2.getSize();
		final int d3 = d1 + d2;
		final int w1 = stack1.getWidth();
		final int h1 = stack1.getHeight();
		final int w2 = stack2.getWidth();
		final int h2 = stack2.getHeight();
		final int w3 = Math.max(w1, w2);
		final int h3 = Math.max(h1, h2);
		final ImageStack stack3 = new ImageStack(w3, h3, stack1.getColorModel());
		final ImageProcessor ip = stack1.getProcessor(1);
		ImageProcessor ip3;
		final Color background = Toolbar.getBackgroundColor();
		for (int i = 1; i <= d3; i++) {
			IJ.showProgress((double) i / d3);
			ip3 = ip.createProcessor(w3, h3);
			if (i <= d1) {
				if (h1 < h3 || w1 < w3) {
					ip3.setColor(background);
					ip3.fill();
				}
				ip3.insert(stack1.getProcessor(i), 0, 0);
				stack3.addSlice(null, ip3);
			}
			ip3 = ip.createProcessor(w3, h3);
			if (i <= d2) {
				if (h2 < h3 || w2 < w3) {
					ip3.setColor(background);
					ip3.fill();
				}
				ip3.insert(stack2.getProcessor(i), 0, 0);
				stack3.addSlice(null, ip3);
			}
		}
		return stack3;
	}

	public boolean showDialog() {
		final int[] wList = WindowManager.getIDList();
		if (wList == null || wList.length < 2) {
			error();
			return false;
		}
		final String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp != null ? imp.getTitle() : "";
		}

		final GenericDialog gd = new GenericDialog("Interleaver");
		gd.addChoice("Stack 1:", titles, titles[0]);
		gd.addChoice("Stack 2:", titles, titles[1]);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		final int index1 = gd.getNextChoiceIndex();
		final int index2 = gd.getNextChoiceIndex();
		imp1 = WindowManager.getImage(wList[index1]);
		imp2 = WindowManager.getImage(wList[index2]);
		return true;
	}

	void error() {
		IJ.showMessage("Stack_Interleaver", "This plugin requires two stacks\n"
			+ "that are the same data type.");
	}

}
