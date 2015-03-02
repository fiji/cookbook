import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Two_Shot_Anaglyph implements PlugInFilter {

	private ImagePlus imp;
	private String titles[];
	private int wList[];
	private int leftImageIndex;
	private int rightImageIndex;
	private ImagePlus Left;
	private ImagePlus Right;

	public void Anaglyph(final ImagePlus imp, final ImagePlus imp2) {
		final int width = imp.getWidth();
		final int width2 = imp2.getWidth();
		final int height = imp.getHeight();
		final int height2 = imp2.getHeight();
		if (width != width2 && height != height2) {
			IJ.showMessage("Anaglyph", "Images must have equal dimensions");
			return;
		}
		final ColorProcessor cp = new ColorProcessor(width, height);
		final ImageProcessor ip = imp.getProcessor();
		final ImageProcessor ip2 = imp2.getProcessor();
		final int pixels[] = (int[]) cp.getPixels();
		final int pixels1[] = (int[]) ip.getPixels();
		final int pixels2[] = (int[]) ip2.getPixels();
		for (int i = 0; i < width * height; i++) {
			final int red = (pixels1[i] & 0xff0000) >> 16;
			final int green = (pixels2[i] & 0xff00ff00) >> 8;
			final int blue = pixels2[i] & 0xff;
			pixels[i] = ((red & 0xff) << 16) + ((green & 0xff) << 8) + (blue & 0xff);
		}

		cp.setPixels(pixels);
		new ImagePlus("Stereo Image", cp).show();
	}

	@Override
	public void run(final ImageProcessor ip) {
		wList = WindowManager.getIDList();
		if (wList == null || wList.length < 2) {
			IJ.showMessage("Anaglyph", "There must be at least two windows open");
			return;
		}
		titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null) {
				titles[i] = imp.getTitle();
			}
			else {
				titles[i] = "";
			}
		}

		if (!showDialog()) {
			return;
		}
		else {
			Anaglyph(Left, Right);
			return;
		}
	}

	@Override
	public int setup(final String s, final ImagePlus imp) {
		this.imp = imp;
		return DOES_RGB;
	}

	public boolean showDialog() {
		final GenericDialog gd = new GenericDialog("Anaglyph");
		gd.addMessage("Choose two images to make a stereo image.\n If the result doesn't work swap images.");
		gd.addChoice("Left image:", titles, titles[0]);
		gd.addChoice("Right image:", titles, titles[1]);
		gd.showDialog();
		if (gd.wasCanceled()) {
			return false;
		}
		else {
			final int i = gd.getNextChoiceIndex();
			final int j = gd.getNextChoiceIndex();
			Left = WindowManager.getImage(wList[i]);
			Right = WindowManager.getImage(wList[j]);
			return true;
		}
	}
}
