import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;
import ij.ImageStack.*;

public class Delta_F_down implements PlugIn {

	public void run(String arg) {
  		if (!IJ.altKeyDown()) IJ.run("Duplicate...", "title=result duplicate");
		ImagePlus imp = WindowManager.getCurrentImage();
   		if (imp==null)
			{IJ.noImage(); return;}
		String fileName = imp.getTitle();
		ImageStack stack1 = imp.getStack();
		int endslice = stack1.getSize();
		ImageProcessor ip1, ip2, ip3;

		for(int s=1; s<endslice-1; s++)
		{
		

		}

	}

}
