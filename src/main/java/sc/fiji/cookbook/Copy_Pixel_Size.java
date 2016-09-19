package sc.fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/**
 * This plugin copies the calibration from one stack to a second stack. It can
 * be used after windows to stack and scale to regain pixel size information
 *
 * @author J. Anthony Parker, MD PhD (J.A.Parker@IEEE.org)
 * @version 22November2001
 */

public class Copy_Pixel_Size implements PlugIn {

	ImagePlus imp1, imp2;

	@Override
	public void run(final String arg) {
		final double scaleW = 1.0, scaleH = 1.0, scaleD = 1.0;
		if (!showDialog()) return;
		final ij.measure.Calibration cal1 = imp1.getCalibration();
		final ij.measure.Calibration cal2 = imp2.getCalibration();

		final GenericDialog gd = new GenericDialog("Copy Pixel Size");
		gd.addMessage("Pixel sizes");
		gd.addNumericField("pixel width:", cal1.pixelWidth, 3);
		gd.addNumericField("pixel height:", cal1.pixelHeight, 3);
		gd.addNumericField("pixel depth:", cal1.pixelDepth, 3);
		gd.addStringField("unit:", cal1.getUnit());
		gd.addMessage("Factors by which image was scaled");
		gd.addNumericField("width:", scaleW, 3);
		gd.addNumericField("height:", scaleH, 3);
		gd.addNumericField("depth:", scaleD, 3);
		gd.showDialog();
		if (gd.wasCanceled()) return;
		cal2.pixelWidth = gd.getNextNumber();
		cal2.pixelHeight = gd.getNextNumber();
		cal2.pixelDepth = gd.getNextNumber();
		cal2.setUnit(gd.getNextString());
		cal2.pixelWidth /= gd.getNextNumber();
		cal2.pixelHeight /= gd.getNextNumber();
		cal2.pixelDepth /= gd.getNextNumber();
		if (cal2.pixelDepth == 1.0) { // check for dicom depth
			final String infoProperty = (String) imp1.getProperty("Info");
			if (infoProperty != null) {
				final String thick = "0018,0050 \u0000Slice Thickness:";
				int start = infoProperty.indexOf(thick);
				if (start > 0) {
					start += thick.length();
					while (infoProperty.charAt(start) == ' ')
						start++;
					int end;
					for (end = start; end < start + 50; end++) {
						final char c = infoProperty.charAt(end);
						if (c >= '0' && c <= '9') continue;
						if (c == '.') continue;
						if (c == '+' || c == '-') continue;
						if (c == 'e' || c == 'E' || c == 'd' || c == 'D') continue;
						break;
					}
					final String numb = infoProperty.substring(start, end).trim();
					cal2.pixelDepth = Double.valueOf(numb).doubleValue();
				}
			}
		}
		imp2.updateAndRepaintWindow();
		// IJ.log("Calibration1:\n"+cal1.toString());
		// IJ.log("Calibration2:\n"+cal2.toString());
		return;
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

		final GenericDialog gd = new GenericDialog("Copy Pixel Size");
		gd.addMessage("Copy the the pixel size");
		gd.addChoice("from:", titles, titles[0]);
		gd.addChoice("to:", titles, titles[1]);
		gd.showDialog();
		if (gd.wasCanceled()) return false;
		final int index1 = gd.getNextChoiceIndex();
		final int index2 = gd.getNextChoiceIndex();
		imp1 = WindowManager.getImage(wList[index1]);
		imp2 = WindowManager.getImage(wList[index2]);
		return true;
	}

	void error() {
		IJ.showMessage("Copy Pixel Size", "This plugin requires two "
			+ "open images.");
	}

}
