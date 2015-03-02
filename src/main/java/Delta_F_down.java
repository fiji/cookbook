import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;

public class Delta_F_down implements PlugIn {

	@Override
	public void run(final String arg) {
		if (!IJ.altKeyDown()) IJ.run("Duplicate...", "title=result duplicate");
		final ImagePlus imp = WindowManager.getCurrentImage();
		if (imp == null) {
			IJ.noImage();
			return;
		}
		final ImageStack stack1 = imp.getStack();
		final int endslice = stack1.getSize();
		for (int s = 1; s < endslice - 1; s++) {

		}

	}

}
