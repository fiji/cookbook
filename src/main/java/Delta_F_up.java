import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Delta_F_up implements PlugIn {

	@Override
	public void run(final String arg) {
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
//IJ.showMessage("Box",fileName);
		final ImageStack stack2 = imp2.getStack();
		stack1.deleteSlice(1);
		stack2.deleteSlice(endslice);

		final String calculatorstring =
			("image1='" + fileName + "' operation=Subtract image2=" + imp2.getTitle() + " create stack");

		IJ.run("Image Calculator...", calculatorstring);
		final ImagePlus imp3 = WindowManager.getCurrentImage();
		imp3.setTitle(fileName + " DeltaF up");
		imp2.getWindow().close();
		imp.getWindow().close();

	}

	ImagePlus duplicateStack(final ImagePlus img1) {
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

}
