package sc.fiji.cookbook;

import java.awt.Font;
import java.awt.Rectangle;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Time_Stamper2 implements PlugInFilter {

	ImagePlus imp;
	double time;
	static int x = 2;
	static int y = 15;
	static int size = 12;
	int maxWidth;
	Font font;
	static double start = 0;
	static double interval = 1;
	static String suffix = "sec";
	static int decimalPlaces = 0;
	boolean firstSlice = true;
	boolean canceled;

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		IJ.register(Time_Stamper2.class);
		return DOES_ALL + DOES_STACKS + STACK_REQUIRED;
	}

	@Override
	public void run(final ImageProcessor ip) {
		if (firstSlice) showDialog(ip);
		if (canceled) return;
		ip.setFont(font);
		ip.setColor(Toolbar.getForegroundColor());
		// String s = getString(time);
		final String s = getString2(time);
		ip.moveTo(x + maxWidth - ip.getStringWidth(s), y);
		ip.drawString(s);
		time += interval;
	}

	String getString(final double time) {
		if (interval == 0.0) return suffix;
		return (decimalPlaces == 0 ? "" + (int) time : IJ.d2s(time, decimalPlaces)) +
			" " + suffix;
	}

	String getString2(final double time) {
		if (time < 10) return "00:0" + (int) time;
		if (time < 60) return "00:" + (int) time;
		if (time >= 60) {
			final int hour = (int) time / 60;
			final int min = (int) time % 60;
			if (hour < 10 && min < 10) return "0" + hour + ":0" + min;
			if (hour < 10 && min >= 10) return "0" + hour + ":" + min;
			if (hour >= 10 && min < 10) return hour + ":0" + min;
			if (hour >= 10 && min >= 10) return hour + ":" + min;
		}
		return (decimalPlaces == 0 ? "" + (int) time : IJ.d2s(time, decimalPlaces)); // +" "+suffix;
	}

	void showDialog(final ImageProcessor ip) {
		firstSlice = false;
		final Rectangle roi = ip.getRoi();
		if (roi.width < ip.getWidth() || roi.height < ip.getHeight()) {
			x = roi.x;
			y = roi.y + roi.height;
			size = (int) ((roi.height - 1.10526) / 0.934211);
			if (size < 7) size = 7;
			if (size > 80) size = 80;
		}
		final GenericDialog gd = new GenericDialog("Time Stamper");
		gd.addNumericField("Starting Time:", start, 2);
		gd.addNumericField("Time Between Frames:", interval, 2);
		gd.addNumericField("X Location:", x, 0);
		gd.addNumericField("Y Location:", y, 0);
		gd.addNumericField("Font Size:", size, 0);
		gd.addNumericField("Decimal Places:", decimalPlaces, 0);
		gd.addStringField("Suffix:", suffix);
		gd.showDialog();
		if (gd.wasCanceled()) {
			canceled = true;
			return;
		}
		start = gd.getNextNumber();
		interval = gd.getNextNumber();
		x = (int) gd.getNextNumber();
		y = (int) gd.getNextNumber();
		size = (int) gd.getNextNumber();
		decimalPlaces = (int) gd.getNextNumber();
		suffix = gd.getNextString();
		font = new Font("SansSerif", Font.PLAIN, size);
		ip.setFont(font);
		time = start;
		if (y < size) y = size;
		maxWidth =
			ip.getStringWidth(getString(start + interval * imp.getStackSize()));
		imp.startTiming();
	}

}
