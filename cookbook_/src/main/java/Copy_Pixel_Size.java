import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;

/**
 * This plugin copies the calibration from one stack to a second stack.
 *
 * It can be used after windows to stack and scale to regain pixel size
 *   information
 *
 * @author J. Anthony Parker, MD PhD <J.A.Parker@IEEE.org>
 * @version 22November2001
 */

public class Copy_Pixel_Size implements PlugIn {
	ImagePlus imp1, imp2;

	public void run(String arg) {
		double scaleW = 1.0, scaleH = 1.0, scaleD = 1.0;
		if (!showDialog())
			return;
		ij.measure.Calibration cal1 = imp1.getCalibration();
		ij.measure.Calibration cal2 = imp2.getCalibration();

		GenericDialog gd = new GenericDialog("Copy Pixel Size");
		gd.addMessage("Pixel sizes");
		gd.addNumericField("pixel width:", cal1.pixelWidth, 3);
		gd.addNumericField("pixel height:", cal1.pixelHeight, 3);
		gd.addNumericField("pixel depth:", cal1.pixelDepth, 3);
		gd.addStringField("unit:",cal1.getUnit());
		gd.addMessage("Factors by which image was scaled");
		gd.addNumericField("width:", scaleW, 3);
		gd.addNumericField("height:", scaleH, 3);
		gd.addNumericField("depth:", scaleD, 3);
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		cal2.pixelWidth = gd.getNextNumber();
		cal2.pixelHeight = gd.getNextNumber();
		cal2.pixelDepth = gd.getNextNumber();
		cal2.setUnit(gd.getNextString());
		cal2.pixelWidth /= gd.getNextNumber();
		cal2.pixelHeight /= gd.getNextNumber();
		cal2.pixelDepth /= gd.getNextNumber();
		if(cal2.pixelDepth==1.0) {	// check for dicom depth
			String infoProperty = (String) imp1.getProperty("Info");
			if(infoProperty != null) {
				String thick = "0018,0050 \u0000Slice Thickness:";
				int start = infoProperty.indexOf(thick);
				if(start>0) {
					start += thick.length();
					while(infoProperty.charAt(start)==' ')
						start++;
					int end;
					for(end = start; end<start+50; end++) {
						char c = infoProperty.charAt(end);
						if(c>='0'&&c<='9') continue;
						if(c=='.') continue;
						if(c=='+'||c=='-') continue;
						if(c=='e'||c=='E'||c=='d'||c=='D') continue;
						break;
					}
					String numb = infoProperty.substring(start, end).trim();
					cal2.pixelDepth = Double.valueOf(numb).doubleValue();
				}
			}
		}
		imp2.updateAndRepaintWindow();
		//IJ.write("Calibration1:\n"+cal1.toString());
		//IJ.write("Calibration2:\n"+cal2.toString());
		return;
	}
	public boolean showDialog() {
		int[] wList = WindowManager.getIDList();
		if (wList==null || wList.length<2) {
			error();
			return false;
		}
		 String[] titles = new String[wList.length];
		 for (int i=0; i<wList.length; i++) {
			ImagePlus imp = WindowManager.getImage(wList[i]);
			titles[i] = imp!=null?imp.getTitle():"";
		}

		GenericDialog gd = new GenericDialog("Copy Pixel Size");
		gd.addMessage("Copy the the pixel size");
		gd.addChoice("from:", titles, titles[0]);
		gd.addChoice("to:", titles, titles[1]);
		gd.showDialog();
		if (gd.wasCanceled())
			return false;
		int index1 = gd.getNextChoiceIndex();
		int index2 = gd.getNextChoiceIndex();
		imp1 = WindowManager.getImage(wList[index1]);
		imp2 = WindowManager.getImage(wList[index2]);
		return true;
	}
	void error() {
		IJ.showMessage("Copy Pixel Size", "This plugin requires two "+
					"open images.");
	}

}
