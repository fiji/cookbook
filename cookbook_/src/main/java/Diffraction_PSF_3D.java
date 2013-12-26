import ij.*;
import ij.plugin.PlugIn;
import ij.gui.*;
import ij.process.*;
/* Bob Dougherty, OptiNav, Inc.  Plugin to compute the 3D point spread function of a diffraction limited
microscope.
Version 0	May2, 2005
Version 1   May 4, 2005.  Fixed bug on center and large z, applied symmetry in z.
Version 1.1 May 4, 2005.  Simpson's rule instead of trapezoidal rule for more speed.
Version 1.2 May 5, 2005.  Changed inputs to n*sin(theta), lambda, and n
Version 2	May 6, 2005.  Added spherical aberration.
*/

public class Diffraction_PSF_3D implements PlugIn {
	//Choices for normalization
    private static final int PEAK1=0, PEAK255=1, AREA1=2;
   	private static String[] norm = {"Peak = 1", "Peak = 255", "Sum of pixel values = 1" };
	//Constants for Bessel function approximation.
	private static double[] t = new double[]{
		1,
		-2.2499997,
		1.2656208,
		-0.3163866,
		0.0444479,
		-0.0039444,
		0.0002100};
	private static double[] p = new double[]{
		-.78539816,
		-.04166397,
		-.00003954,
		0.00262573,
		-.00054125,
		-.00029333,
		.00013558};
	private static double[] f = new double[]{
		.79788456,
		-0.00000077,
		-.00552740,
		-.00009512,
		0.00137237,
		-0.00072805,
		0.00014476};

	public void run(String arg) {
		if (IJ.versionLessThan("1.32c"))
			return;

		double lambda = Prefs.get("difflimitpsf3d.lambda", 510);
		double indexRefr = Prefs.get("difflimitpsf3d.indexrefr", 1);
		double pixelSpacing = Prefs.get("difflimitpsf3d.pixelSpacing", 30);
		double sliceSpacing = Prefs.get("difflimitpsf3d.sliceSpacing", 30);
		double na = Prefs.get("difflimitpsf3d.na", 0.6);
		double sa = Prefs.get("difflimitpsf3d.sa", 0.);
		int w = (int)Prefs.get("difflimitpsf3d.w", 256);
		int h = (int)Prefs.get("difflimitpsf3d.h", 256);
		int d = (int)Prefs.get("difflimitpsf3d.d", 256);
		//int stepsPerCycle = (int)Prefs.get("difflimitpsf3d.stepspercycle", 8);
		int stepsPerCycle = 8;
		int normalization = (int)Prefs.get("difflimitpsf3d.normalization", 2);
		String title = Prefs.get("difflimitpsf3d.title", "PSF");
		boolean dB = Prefs.get("difflimitpsf3d.dB", false);

		GenericDialog gd = new GenericDialog("Specify psf", IJ.getInstance());
		gd.addMessage("Rayleigh resolution: 0.6*lambda/NA");
		gd.addNumericField("Index of refraction of the media", indexRefr, 3);
		gd.addNumericField("Numerical Aperture, n*sin(theta)", na, 2);
		gd.addNumericField("Wavelength (perhaps in nm)", lambda, 1);
		gd.addNumericField("Longitudinal Spherical Aberration at max. aperture, same units", sa, 2);
		gd.addNumericField("Image pixel spacing, same units (ccd cell spacing / magnification)", pixelSpacing, 2);
		gd.addNumericField("Slice spacing (z), same units", sliceSpacing, 2);
        gd.addNumericField("Width, pixels",w,0);
        gd.addNumericField("Height, pixels",h,0);
        gd.addNumericField("Depth, slices",d,0);
        //gd.addNumericField("Steps per cycle in integral (recommend 8)",stepsPerCycle,0);
		gd.addChoice("Normalization", norm, norm[normalization]);
		gd.addStringField("Title", title, 12);
		gd.addCheckbox("PSF in dB",dB);

        gd.showDialog();
        if (gd.wasCanceled())
            return;

 		indexRefr = gd.getNextNumber();
		na = gd.getNextNumber();
  		lambda = gd.getNextNumber();
  		sa = gd.getNextNumber();
		pixelSpacing = gd.getNextNumber();
		sliceSpacing = gd.getNextNumber();
		w = (int)gd.getNextNumber();
		h = (int)gd.getNextNumber();
		d = (int)gd.getNextNumber();
		//stepsPerCycle = (int)gd.getNextNumber();
		normalization = gd.getNextChoiceIndex();
		title = gd.getNextString();
		dB = gd.getNextBoolean();

		Prefs.set("difflimitpsf3d.lambda", lambda);
		Prefs.set("difflimitpsf3d.indexrefr", indexRefr);
		Prefs.set("difflimitpsf3d.sa", sa);
		Prefs.set("difflimitpsf3d.pixelSpacing", pixelSpacing);
		Prefs.set("difflimitpsf3d.sliceSpacing", sliceSpacing);
		Prefs.set("difflimitpsf3d.na", na);
		Prefs.set("difflimitpsf3d.w", w);
		Prefs.set("difflimitpsf3d.h", h);
		Prefs.set("difflimitpsf3d.d", d);
		//Prefs.set("difflimitpsf3d.stepspercycle", stepsPerCycle);
		Prefs.set("difflimitpsf3d.normalization", normalization);
		Prefs.set("difflimitpsf3d.title", title);
		Prefs.set("difflimitpsf3d.dB", dB);

		int ic = w/2;
		int jc = h/2;
		int kc = d/2;

		float a = (float)(2*Math.PI*na/lambda);
		double dRing = 0.6*lambda/(pixelSpacing*na);
				if (!IJ.showMessageWithCancel("PSF: peak to first dark ring (w/o sph. aber.)","Rayleigh resolution = "+IJ.d2s(dRing)+" pixels"))
			return;

		float[][] pixels = new float[d][w*h];
		int rMax = 2 + (int)Math.sqrt(ic*ic + jc*jc);
		float[] integral = new float[rMax];
		double upperLimit = Math.tan(Math.asin(na/indexRefr));
		double waveNumber = 2*Math.PI*indexRefr/lambda;
		//for (int k = 0; k <= d/2; k++){
		for (int k = 0; k < d; k++){
			//int kSym = (2*kc - k) % d;
			//if(kSym == k){
				IJ.showStatus("Computing psf slice "+(k+1));;
			//}else{
			//	IJ.showStatus("Computing psf slices "+(k+1)+" and "+(kSym+1));
			//}
			double kz = waveNumber*(k - kc)*sliceSpacing;
			for (int r = 0; r < rMax; r++){
				double kr = waveNumber*r*pixelSpacing;
				int numCyclesJ = 1 + (int)(kr*upperLimit/3);
				int numCyclesCos = 1 + (int)(Math.abs(kz)*0.36*upperLimit/6);
				int numCycles = numCyclesJ;
				if(numCyclesCos > numCycles)numCycles = numCyclesCos;
				int nStep = 2*stepsPerCycle*numCycles;
				int m = nStep/2;
				double step = upperLimit/nStep;
				double sumR = 0;
				double sumI = 0;
				//Simpson's rule
				//Assume that the sperical aberration varies with the  (% aperture)^4
				//f(a) = f(0) = 0, so no contribution
				double u = 0;
				double bessel = 1;
				double root = 1;
				double angle = kz;
				//2j terms
				for (int j = 1; j < m; j++){
					u = 2*j*step;
					kz = waveNumber*((k - kc)*sliceSpacing +
						sa*(u/upperLimit)*(u/upperLimit)*(u/upperLimit)*(u/upperLimit));
					root = Math.sqrt(1 + u*u);
					bessel = J0(kr*u/root);
					angle = kz/root;
					sumR += 2*Math.cos(angle)*u*bessel/2;
					sumI += 2*Math.sin(angle)*u*bessel/2;
				}

				//2j - 1 terms
				for (int j = 1; j <= m; j++){
					u = (2*j-1)*step;
					kz = waveNumber*((k - kc)*sliceSpacing +
						sa*(u/upperLimit)*(u/upperLimit)*(u/upperLimit)*(u/upperLimit));
					root = Math.sqrt(1 + u*u);
					bessel = J0(kr*u/root);
					angle = kz/root;
					sumR += 4*Math.cos(angle)*u*bessel/2;
					sumI += 4*Math.sin(angle)*u*bessel/2;
				}

				//f(b)
				u = upperLimit;
				kz = waveNumber*((k - kc)*sliceSpacing + sa);
				root = Math.sqrt(1 + u*u);
				bessel = J0(kr*u/root);
				angle = kz/root;
				sumR += Math.cos(angle)*u*bessel/2;
				sumI += Math.sin(angle)*u*bessel/2;

				integral[r] = (float)(step*step*(sumR*sumR + sumI*sumI)/9);
			}
			double uSlices = (k - kc);
			for (int j = 0; j < h; j++){
				IJ.showProgress((float)j/h);
				for (int i = 0; i < w; i++){
					double rPixels = Math.sqrt((i - ic)*(i-ic) + (j-jc)*(j - jc));
					pixels[k][i + w*j] = interp(integral,(float)rPixels);
					//pixels[kSym][i + w*j] = interp(integral,(float)rPixels);
				}
			}
		}
		int n = w*h;
		if(normalization == PEAK1){
			float peak = pixels[kc][ic + w*jc];
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					if(pixels[k][ind] > peak)
						peak = pixels[k][ind];
				}
			}
			float f = 1/peak;
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					pixels[k][ind] *= f;
				}
			}
		}else if(normalization == PEAK255){
			float peak = pixels[kc][ic + w*jc];
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					if(pixels[k][ind] > peak)
						peak = pixels[k][ind];
				}
			}
			float f = 255/peak;
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					pixels[k][ind] *= f;
					if(pixels[k][ind] > 255)pixels[k][ind] = 255;
				}
			}
		}else if(normalization == AREA1){
			float area = 0;
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					area += pixels[k][ind];
				}
			}
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					pixels[k][ind] /= area;
				}
			}
		}
		if(dB){
			double SCALE = 10/Math.log(10);
			for (int k = 0; k < d; k++){
				for (int ind = 0; ind < n; ind++){
					if(pixels[k][ind] > 0.000000001)
						pixels[k][ind] = (float)(SCALE*Math.log(pixels[k][ind]));
					else
						pixels[k][ind] = -90;
				}
			}
		}
		ImageStack stack = new ImageStack(w,h);
		for (int k = 0; k < d; k++){
			ImageProcessor ip = new FloatProcessor(w,h);
			ip.setPixels(pixels[k]);
			stack.addSlice(null,ip);
		}
		ImagePlus imp = new ImagePlus(title,stack);
		imp.setSlice(kc + 1);
		ImageProcessor ip = imp .getProcessor();
		ip.setMinAndMax(0,0);
		imp.show();
	}
	float interp(float[] y, float x){
		int i = (int)x;
		float fract = x - i;
		return (1 - fract)*y[i] + fract*y[i+1];
	}
	//Bessel function J0(x).  Uses the polynomial approximations on p. 369-70 of Abramowitz & Stegun
	//The error in J0 is supposed to be less than or equal to 5 x 10^-8.
	double J0(double xIn){
		double x = xIn;
		if (x < 0) x *= -1;
		double r;
		if (x <= 3){
			double y = x*x/9;
			r = t[0] + y*(t[1] + y*(t[2] + y*(t[3] + y*(t[4] + y*(t[5] + y*t[6])))));
		}else{
			double y = 3/x;
			double theta0 = x + p[0] + y*(p[1] + y*(p[2] + y*(p[3] + y*(p[4] + y*(p[5] + y*p[6])))));
			double f0 = f[0] + y*(f[1] + y*(f[2] + y*(f[3] + y*(f[4] + y*(f[5] + y*f[6])))));
			r = Math.sqrt(1/x)*f0*Math.cos(theta0);
		}
		return r;
	}
}