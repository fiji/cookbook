import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.process.ImageProcessor;

/** Base class for {@link Delta_F_up} and {@link Delta_F_down}. */
public abstract class Delta {

	protected void run(final boolean isUp) {
		final ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}

		final ImageStack stack1 = imp.getStack();
		final String fileName = imp.getTitle();
		final int endslice = stack1.getSize();

		final ImagePlus imp2 = duplicateStack(imp);
		imp2.show();
		// IJ.showMessage("Box",fileName);
		final ImageStack stack2 = imp2.getStack();
		stack1.deleteSlice(1);
		stack2.deleteSlice(endslice);

		final String image2 = imp2.getTitle();

		final String calculatorstring = calculateString(fileName, image2, isUp);

		IJ.run("Image Calculator...", calculatorstring);
		final ImagePlus imp3 = WindowManager.getCurrentImage();
		imp3.setTitle(fileName + " DeltaF " + (isUp ? "up" : "down"));
		imp2.getWindow().close();
		imp.getWindow().close();
	}

	private ImagePlus duplicateStack(final ImagePlus img1) {
		final ImageStack stack1 = img1.getStack();
		final int n = stack1.getSize();
		ImageStack stack2 = img1.createEmptyStack();
		try {
			for (int i = 1; i <= n; i++) {
				final ImageProcessor ip1 = stack1.getProcessor(i);
				ip1.resetRoi();
				final ImageProcessor ip2 = ip1.crop();
				stack2.addSlice(stack1.getSliceLabel(i), ip2);
			}
		}
		catch (final OutOfMemoryError e) {
			stack2.trim();
			stack2 = null;
			return null;
		}
		return new ImagePlus("Duplicate", stack2);
	}

	private String calculateString(final String image1, final String image2,
		final boolean isUp)
	{
		final String arg1 = isUp ? image1 : image2;
		final String arg2 = isUp ? image2 : image1;
		return "image1='" + arg1 + "' operation=Subtract image2=" + arg2 +
			" create stack";
	}
}
