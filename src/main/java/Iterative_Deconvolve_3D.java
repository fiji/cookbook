import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/*3D Image deconvolution.  Bob Dougherty.

Uses code from ImageJ and several plugins.

The following notice is from the FHT source code in ImageJ:

		This class contains a Java implementation of the Fast Hartley Transform.
		It is based on Pascal code in NIH Image contributed by Arlo Reeves
		(http://rsb.info.nih.gov/ij/docs/ImageFFT/). The Fast Hartley Transform was
		restricted by U.S. Patent No. 4,646,256, but was placed in the public domain
		by Stanford University in 1995 and is now freely available.


Version 0 5/1/2005.
Version 1 5/2/2005  Improved performce for 2D. Option to not show iterations.

*/
public class Iterative_Deconvolve_3D implements PlugIn {

	@Override
	public void run(final String arg) {
		if (arg.equalsIgnoreCase("about")) {
			showAbout();
			return;
		}
		if (IJ.versionLessThan("1.32c")) return;
		final int[] wList = WindowManager.getIDList();
		if (wList == null) {
			IJ.noImage();
			return;
		}
		final String[] titles = new String[wList.length];
		for (int i = 0; i < wList.length; i++) {
			final ImagePlus imp = WindowManager.getImage(wList[i]);
			if (imp != null) titles[i] = imp.getTitle();
			else titles[i] = "";
		}
		final String titleImage =
			Prefs.get("iterativedeconvolve3d.titleImage", titles[0]);
		int imageChoice = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titleImage.equals(titles[i])) {
				imageChoice = i;
				break;
			}
		}
		final String titlePSF =
			Prefs.get("iterativedeconvolve3d.titlePSF", titles[0]);
		int psfChoice = 0;
		for (int i = 0; i < wList.length; i++) {
			if (titlePSF.equals(titles[i])) {
				psfChoice = i;
				break;
			}
		}
		int nIter = (int) Prefs.get("iterativedeconvolve3d.nIter", 100);
		// double filterSmallDia = Prefs.get("iterativedeconvolve3d.filterSmallDia",
		// 2);
		float gamma = (float) Prefs.get("iterativedeconvolve3d.gamma", 0.001);
		boolean normalize = Prefs.get("iterativedeconvolve3d.normalize", false);
		boolean logMean = Prefs.get("iterativedeconvolve3d.logmean", true);
		boolean showIteration =
			Prefs.get("iterativedeconvolve3d.showiteration", true);
		boolean termMean = Prefs.get("iterativedeconvolve3d.termmean", true);
		boolean dB = Prefs.get("iterativedeconvolve3d.dB", false);

		final GenericDialog gd =
			new GenericDialog("Deconvlove 3D", IJ.getInstance());
		gd.addChoice("Image", titles, titles[imageChoice]);
		gd.addChoice("Point Spread Function (Kernel)", titles, titles[psfChoice]);
		gd.addCheckbox("Normalize PSF", normalize);
		gd.addNumericField("gamma", gamma, 5);
		gd.addNumericField("Maximum number of iterations", nIter, 0);
		// gd.addNumericField("LP filter diameter, pixels", filterSmallDia,1);
		gd.addCheckbox("Show iteration", showIteration);
		gd.addCheckbox("Log mean pixel value to track convergence", logMean);
		gd.addCheckbox("Terminate if mean changes by < 0.1%", termMean);
		gd.addCheckbox("Data (image, psf and result) in dB", dB);
		gd.addStringField("Output title", "Deconvolved", 20);
		gd.showDialog();

		if (gd.wasCanceled()) return;
		final ImagePlus impY =
			WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		final ImagePlus impA =
			WindowManager.getImage(wList[gd.getNextChoiceIndex()]);
		gamma = (float) gd.getNextNumber();
		nIter = (int) gd.getNextNumber();
		// filterSmallDia = gd.getNextNumber();
		normalize = gd.getNextBoolean();
		showIteration = gd.getNextBoolean();
		logMean = gd.getNextBoolean();
		termMean = gd.getNextBoolean();
		dB = gd.getNextBoolean();
		final String titleOut = gd.getNextString();

		Prefs.set("iterativedeconvolve3d.titleImage", impY.getTitle());
		Prefs.set("iterativedeconvolve3d.titlePSF", impA.getTitle());
		Prefs.set("iterativedeconvolve3d.gamma", gamma);
		Prefs.set("iterativedeconvolve3d.nIter", nIter);
		// Prefs.set("iterativedeconvolve3d.filterSmallDia", filterSmallDia);
		Prefs.set("iterativedeconvolve3d.normalize", normalize);
		Prefs.set("iterativedeconvolve3d.showiteration", showIteration);
		Prefs.set("iterativedeconvolve3d.logmean", logMean);
		Prefs.set("iterativedeconvolve3d.logmean", termMean);
		Prefs.set("iterativedeconvolve3d.dB", dB);

		final ImageProcessor ipY = impY.getProcessor();
		final ImageProcessor ipA = impA.getProcessor();
		if (((ipY instanceof ColorProcessor) || (ipA instanceof ColorProcessor))) {
			IJ.showMessage("RGB images are not currently supported.");
			return;
		}

		final ImageStack stackY = impY.getStack();
		final int bw = stackY.getWidth();
		final int bh = stackY.getHeight();
		final int bd = impY.getStackSize();
		final float[][] dataYin = new float[bd][];
		if (ipY instanceof FloatProcessor) {
			for (int i = 0; i < bd; i++) {
				dataYin[i] = (float[]) stackY.getProcessor(i + 1).getPixels();
			}
		}
		else {
			for (int i = 0; i < bd; i++) {
				dataYin[i] =
					(float[]) stackY.getProcessor(i + 1).convertToFloat().getPixels();
			}
		}

		final ImageStack stackA = impA.getStack();
		final int kw = stackA.getWidth();
		final int kh = stackA.getHeight();
		final int kd = impA.getStackSize();
		final float[][] dataAin = new float[kd][];
		if (ipA instanceof FloatProcessor) {
			for (int i = 0; i < kd; i++) {
				dataAin[i] = (float[]) stackA.getProcessor(i + 1).getPixels();
			}
		}
		else {
			for (int i = 0; i < kd; i++) {
				dataAin[i] =
					(float[]) stackA.getProcessor(i + 1).convertToFloat().getPixels();
			}
		}

		double minA = 0;
		double minY = 0;
		if (dB) {
			minA = unDB(dataAin);
			minY = unDB(dataYin);
		}

		float scalePSF = 1;
		if (normalize) {
			float sum = 0;
			for (int k = 0; k < kd; k++) {
				for (int ind = 0; ind < kh * kw; ind++) {
					sum += dataAin[k][ind];
				}
			}
			if (sum != 0) scalePSF /= sum;
		}
		final int bwE = expandedSize(bw);
		final int bhE = expandedSize(bh);
		final int bdE = (bd == 1) ? 1 : expandedSize(bd);
		final int kwE = expandedSize(kw);
		final int khE = expandedSize(kh);
		final int kdE = (kd == 1) ? 1 : expandedSize(kd);
		// w and h will always be at least 4. d can be 1 as a special case.
		final int w = Math.max(bwE, kwE);
		final int h = Math.max(bhE, khE);
		final int d = Math.max(bdE, kdE);

		IJ.showStatus("Creating expanded arrays");
		final float[][] dataY = new float[d][w * h];
		copyDataMirror(bw, bh, bd, dataYin, w, h, d, dataY);
		final float[][] dataA = new float[d][w * h];
		copyDataMask(kw, kh, kd, dataAin, w, h, d, dataA);

		IJ.showStatus("Swapping quadrants of the PSF");
		swapQuadrants(w, h, d, dataA);

		final java.awt.image.ColorModel cmY =
			stackY.getProcessor(1).getColorModel();

		IJ.showStatus("Transforming PSF");
		FHT3D(dataA, w, h, d, false);
		final float[][] dataX = new float[d][w * h];
		final float[][] AX = new float[d][w * h];

		// Optional premultiplication step
		IJ.showStatus("Finding largest spectral element");
		final double magMax = findMagMax(w, h, d, dataA);
		IJ.showStatus("Transforming blurred image");
		FHT3D(dataY, w, h, d, false);
		IJ.showStatus("Premultiplying PSF and blured image");
		// Use dataX storage temporarily for FD PSF (could be more efficient)
		// Use AX storage temporarily to store FD Y (could be more efficient)
		copyData(w, h, d, dataA, dataX);
		deconvolveFD(gamma, magMax, w, h, d, dataX, dataX, dataA);
		copyData(w, h, d, dataY, AX);
		deconvolveFD(gamma, magMax, w, h, d, AX, dataX, dataY);
		IJ.showStatus("Inverse transforming blurred image");
		FHT3D(dataY, w, h, d, true);
		// Finished with optional premultiplication step

		final int wh = w * h;
		final int kOff = (d - bd + 1) / 2;
		final int jOff = (h - bh + 1) / 2;
		final int iOff = (w - bw + 1) / 2;

		// Convert PSF back to the spatial domain in order to
		// compute aSum after the premultiplication step
		FHT3D(dataA, w, h, d, true);
		float aSum = 0;
		for (int k = 0; k < d; k++) {
			for (int ind = 0; ind < wh; ind++) {
				aSum += Math.abs(dataA[k][ind]);
			}
		}
		// Apply scale factors
		if (scalePSF != 1) {
			IJ.showStatus("Normalizing");
			for (int k = 0; k < d; k++) {
				for (int ind = 0; ind < h * w; ind++) {
					dataY[k][ind] /= scalePSF;
				}
			}
		}
		FHT3D(dataA, w, h, d, false);
		copyData(w, h, d, dataY, dataX);
		ImageStack stackOutTemp = null;
		ImagePlus impOutTemp = null;
		if (showIteration) {
			stackOutTemp = new ImageStack(w, h);
			for (int k = 0; k < d; k++) {
				final ImageProcessor ip = new FloatProcessor(w, h);
				final float[] px = (float[]) ip.getPixels();
				for (int j = 0; j < h; j++) {
					for (int i = 0; i < w; i++) {
						px[i + w * j] = dataX[k][i + w * j];
					}
				}
				ip.setMinAndMax(0, 0);
				ip.setColorModel(cmY);
				ip.setRoi(iOff, jOff, bw, bh);
				stackOutTemp.addSlice(null, ip);
			}
			impOutTemp = new ImagePlus(titleOut + "Temp", stackOutTemp);
			impOutTemp.setSlice(d / 2 + 1);
			impOutTemp.show();
			final ImageProcessor ip2 = impOutTemp.getProcessor();
			ip2.setMinAndMax(0, 0);
			ip2.setColorModel(cmY);
			impOutTemp.updateAndDraw();
			impOutTemp.setRoi(iOff, jOff, bw, bh);
		}

		float oldMean = 0;
		final double changeThresh = 0.001;
		int iter;
		for (iter = 0; iter < nIter; iter++) {
			IJ.showProgress((float) iter / nIter);
			IJ.showStatus("Starting iteration " + (iter + 1) + " of " + nIter);
			FHT3D(dataX, w, h, d, false);
			convolveFD(w, h, d, dataA, dataX, AX);
			FHT3D(AX, w, h, d, true);
			FHT3D(dataX, w, h, d, true);
			for (int k = 0; k < d; k++) {
				for (int ind = 0; ind < wh; ind++) {
					dataX[k][ind] += (dataY[k][ind] - AX[k][ind]) / aSum;
				}
			}
			// filterSmall(ipX,filterSmallDia);
			for (int k = 0; k < d; k++) {
				for (int ind = 0; ind < wh; ind++) {
					if (dataX[k][ind] < 0) dataX[k][ind] = 0;
				}
			}
			if (showIteration) {
				for (int k = 0; k < d; k++) {
					final ImageProcessor ip = stackOutTemp.getProcessor(k + 1);
					final float[] px = (float[]) ip.getPixels();
					for (int j = 0; j < h; j++) {
						for (int i = 0; i < w; i++) {
							px[i + w * j] = dataX[k][i + w * j];
						}
					}
				}
				final ImageProcessor ip1 = impOutTemp.getProcessor();
				ip1.setMinAndMax(0, 0);
				ip1.setColorModel(cmY);
				impOutTemp.updateAndDraw();
				impOutTemp.setRoi(iOff, jOff, bw, bh);
			}
			// Energy sum to track convergence
			if (logMean | termMean) {
				float sumPixels = 0;
				for (int k = 0; k < bd; k++) {
					for (int j = 0; j < bh; j++) {
						for (int i = 0; i < bw; i++) {
							sumPixels += dataX[k + kOff][i + iOff + w * (j + jOff)];
						}
					}
				}
				final float newMean = sumPixels / (bd * bh * bw);
				if (logMean) IJ.log(Float.toString(newMean));
				double percentChange = changeThresh + 1;
				if (oldMean != 0) {
					percentChange = Math.abs((newMean - oldMean) / oldMean);
				}
				if (termMean && (percentChange < changeThresh)) {
					if (logMean) IJ.log("Automatically terminated after " + iter +
						" iterations.");
					break;
				}
				oldMean = newMean;
			}
			IJ.showStatus(iter + " iterations complete.");
		}

		if (dB) {
			toDB(dataAin, minA);
			toDB(dataYin, minY);
			toDB(dataX, -90);
		}

		// Crop the output to the size of Yin
		if (showIteration) impOutTemp.hide();
		final ImageStack stackOut = new ImageStack(bw, bh);
		for (int k = 0; k < bd; k++) {
			final ImageProcessor ip = new FloatProcessor(bw, bh);
			final float[] px = (float[]) ip.getPixels();
			for (int j = 0; j < bh; j++) {
				for (int i = 0; i < bw; i++) {
					px[i + bw * j] = dataX[k + kOff][i + iOff + w * (j + jOff)];
				}
			}
			stackOut.addSlice(null, ip);
		}
		final ImagePlus impOut = new ImagePlus(titleOut + "_" + iter, stackOut);
		impOut.setSlice(bd / 2 + 1);
		final ImageProcessor ip = impOut.getProcessor();
		ip.setMinAndMax(0, 0);
		ip.setColorModel(cmY);
		impOut.show();
	}// run

	void copyDataMask(final int w, final int h, final int d,
		final float[][] data, final int wE, final int hE, final int dE,
		final float[][] dataE)
	{
		final int kOff = (dE - d + 1) / 2;
		final int jOff = (hE - h + 1) / 2;
		final int iOff = (wE - w + 1) / 2;
		for (int k = 0; k < d; k++) {
			for (int j = 0; j < h; j++) {
				for (int i = 0; i < w; i++) {
					dataE[k + kOff][i + iOff + wE * (j + jOff)] = data[k][i + w * j];
				}
			}
		}
	}

	void copyData(final int w, final int h, final int d, final float[][] data,
		final float[][] data2)
	{
		final int wh = w * h;
		for (int k = 0; k < d; k++) {
			for (int ind = 0; ind < wh; ind++) {
				data2[k][ind] = data[k][ind];
			}
		}
	}

	void copyDataMirror(final int w, final int h, final int d,
		final float[][] data, final int wE, final int hE, final int dE,
		final float[][] dataE)
	{
		final int kOff = (dE - d + 1) / 2;
		final int jOff = (hE - h + 1) / 2;
		final int iOff = (wE - w + 1) / 2;
		int iIn, jIn, kIn, iOut, jOut, kOut;
		for (int k = -kOff; k < dE - kOff; k++) {
			kOut = k + kOff;
			kIn = mirror(k, d);
			for (int j = -jOff; j < hE - jOff; j++) {
				jOut = j + jOff;
				jIn = mirror(j, h);
				for (int i = -iOff; i < wE - iOff; i++) {
					iOut = i + iOff;
					iIn = mirror(i, w);
					dataE[kOut][iOut + wE * jOut] = data[kIn][iIn + w * jIn];
				}
			}
		}
	}

	int mirror(final int i, final int n) {
		final int ip = mod(i, 2 * n);
		if (ip < n) {
			return ip;
		}
		return n - (ip % n) - 1;
	}

	// A version of mod that is periodic for postive and negative i
	int mod(final int i, final int n) {
		return ((i % n) + n) % n;
	}

	int expandedSize(final int maxN) {
		// Expand this to a power of 2 that is at least 1.5* as large, to avoid wrap
		// effects
		// Start with 4 to avoid apparent normalization problems with n = 2
		int iN = 4;
		if (maxN > 1) {
			while (iN < 1.5 * maxN)
				iN *= 2;
		}
		return iN;
	}

	double unDB(final float[][] x) {
		double result = Float.MAX_VALUE;
		final int n = x.length;
		for (int i = 0; i < n; i++) {
			final double ri = unDB(x[i]);
			if (ri < result) result = ri;
		}
		return result;
	}

	double unDB(final float[] x) {
		final double SCALE = 10 / Math.log(10);
		final int n = x.length;
		double result = Float.MAX_VALUE;
		for (int i = 0; i < n; i++) {
			if (x[i] < result) result = x[i];
			x[i] = (float) Math.exp(x[i] / SCALE);
		}
		return result;
	}

	void toDB(final float[][] x, final double minDB) {
		final int n = x.length;
		for (int i = 0; i < n; i++) {
			toDB(x[i], minDB);
		}
	}

	void toDB(final float[] x, final double minDB) {
		final double SCALE = 10 / Math.log(10);
		final double minVal = Math.exp(minDB / SCALE);
		final int n = x.length;
		for (int i = 0; i < n; i++) {
			if (x[i] > minVal) x[i] = (float) (SCALE * Math.log(x[i]));
			else x[i] = (float) minDB;
		}
	}

	void swapQuadrants(final int w, final int h, final int d, final float[][] x) {
		int k1P, k2P, k3P;
		float temp;
		final int wHalf = w / 2;
		final int hHalf = h / 2;
		final int dHalf = d / 2;
		// Shift by half of the grid, less one pixel, in each direction
		for (int k3 = 0; k3 < dHalf; k3++) {
			k3P = k3 + dHalf;
			for (int k2 = 0; k2 < h; k2++) {
				for (int k1 = 0; k1 < w; k1++) {
					temp = x[k3][k1 + w * k2];
					x[k3][k1 + w * k2] = x[k3P][k1 + w * k2];
					x[k3P][k1 + w * k2] = temp;
				}
			}
		}
		for (int k2 = 0; k2 < hHalf; k2++) {
			k2P = k2 + hHalf;
			for (int k3 = 0; k3 < d; k3++) {
				for (int k1 = 0; k1 < w; k1++) {
					temp = x[k3][k1 + w * k2];
					x[k3][k1 + w * k2] = x[k3][k1 + w * k2P];
					x[k3][k1 + w * k2P] = temp;
				}
			}
		}
		for (int k1 = 0; k1 < wHalf; k1++) {
			k1P = k1 + wHalf;
			for (int k2 = 0; k2 < h; k2++) {
				for (int k3 = 0; k3 < d; k3++) {
					temp = x[k3][k1 + w * k2];
					x[k3][k1 + w * k2] = x[k3][k1P + w * k2];
					x[k3][k1P + w * k2] = temp;
				}
			}
		}
	}

	void convolveFD(final int w, final int h, final int d, final float[][] h1,
		final float[][] h2, final float[][] result)
	{
		int k1C, k2C, k3C;
		double h2e, h2o;
		for (int k3 = 0; k3 < d; k3++) {
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++) {
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++) {
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w * k2] + h2[k3C][k1C + w * k2C]) / 2;
					h2o = (h2[k3][k1 + w * k2] - h2[k3C][k1C + w * k2C]) / 2;
					result[k3][k1 + w * k2] =
						(float) (h1[k3][k1 + w * k2] * h2e + h1[k3C][k1C + w * k2C] * h2o);
				}
			}
		}
	}

	void deconvolveFD(final float gamma, final double magMax, final int w,
		final int h, final int d, final float[][] h1, final float[][] h2,
		final float[][] result)
	{
		int k1C, k2C, k3C;
		double mag, h2e, h2o;
		final double gammaScaled = gamma * magMax;
		for (int k3 = 0; k3 < d; k3++) {
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++) {
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++) {
					k1C = (w - k1) % w;
					h2e = (h2[k3][k1 + w * k2] + h2[k3C][k1C + w * k2C]) / 2;
					h2o = (h2[k3][k1 + w * k2] - h2[k3C][k1C + w * k2C]) / 2;
					mag =
						h2[k3][k1 + w * k2] * h2[k3][k1 + w * k2] + h2[k3C][k1C + w * k2C] *
							h2[k3C][k1C + w * k2C];
					final double tmp =
						h1[k3][k1 + w * k2] * h2e - h1[k3C][k1C + w * k2C] * h2o;
					result[k3][k1 + w * k2] = (float) (tmp / (mag + gammaScaled));
				}
			}
		}
	}

	double findMagMax(final int w, final int h, final int d, final float[][] h2) {
		int k1C, k2C, k3C;
		double magMax = 0;
		double mag;
		for (int k3 = 0; k3 < d; k3++) {
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 < h; k2++) {
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 < w; k1++) {
					k1C = (w - k1) % w;
					mag =
						h2[k3][k1 + w * k2] * h2[k3][k1 + w * k2] + h2[k3C][k1C + w * k2C] *
							h2[k3C][k1C + w * k2C];
					if (mag > magMax) magMax = mag;
				}
			}
		}
		return magMax;
	}

	boolean powerOf2Size(final int w) {
		int i = 2;
		while (i < w)
			i *= 2;
		return i == w;
	}

	public void FHT3D(final float[][] data, final int w, final int h,
		final int d, final boolean inverse)
	{
		final float[] sw = new float[w / 4];
		final float[] cw = new float[w / 4];
		final float[] sh = new float[h / 4];
		final float[] ch = new float[h / 4];
		makeSinCosTables(w, sw, cw);
		makeSinCosTables(h, sh, ch);
		for (int i = 0; i < d; i++) {
			rc2DFHT(data[i], w, h, sw, cw, sh, ch);
		}
		final float[] u = new float[d];
		if (powerOf2Size(d)) {
			final float[] s = new float[d / 4];
			final float[] c = new float[d / 4];
			makeSinCosTables(d, s, c);
			for (int k2 = 0; k2 < h; k2++) {
				for (int k1 = 0; k1 < w; k1++) {
					final int ind = k1 + k2 * w;
					for (int k3 = 0; k3 < d; k3++) {
						u[k3] = data[k3][ind];
					}
					dfht3(u, 0, d, s, c);
					for (int k3 = 0; k3 < d; k3++) {
						data[k3][ind] = u[k3];
					}
				}
			}
		}
		else {
			final float[] cas = hartleyCoefs(d);
			final float[] work = new float[d];
			for (int k2 = 0; k2 < h; k2++) {
				for (int k1 = 0; k1 < w; k1++) {
					final int ind = k1 + k2 * w;
					for (int k3 = 0; k3 < d; k3++) {
						u[k3] = data[k3][ind];
					}
					slowHT(u, cas, d, work);
					for (int k3 = 0; k3 < d; k3++) {
						data[k3][ind] = u[k3];
					}
				}
			}
		}
		// Convert to actual Hartley transform
		float A, B, C, D, E, F, G, H;
		int k1C, k2C, k3C;
		for (int k3 = 0; k3 <= d / 2; k3++) {
			k3C = (d - k3) % d;
			for (int k2 = 0; k2 <= h / 2; k2++) {
				k2C = (h - k2) % h;
				for (int k1 = 0; k1 <= w / 2; k1++) {
					k1C = (w - k1) % w;
					A = data[k3][k1 + w * k2C];
					B = data[k3][k1C + w * k2];
					C = data[k3C][k1 + w * k2];
					D = data[k3C][k1C + w * k2C];
					E = data[k3C][k1 + w * k2C];
					F = data[k3C][k1C + w * k2];
					G = data[k3][k1 + w * k2];
					H = data[k3][k1C + w * k2C];
					data[k3][k1 + w * k2] = (A + B + C - D) / 2;
					data[k3C][k1 + w * k2] = (E + F + G - H) / 2;
					data[k3][k1 + w * k2C] = (G + H + E - F) / 2;
					data[k3C][k1 + w * k2C] = (C + D + A - B) / 2;
					data[k3][k1C + w * k2] = (H + G + F - E) / 2;
					data[k3C][k1C + w * k2] = (D + C + B - A) / 2;
					data[k3][k1C + w * k2C] = (B + A + D - C) / 2;
					data[k3C][k1C + w * k2C] = (F + E + H - G) / 2;
				}
			}
		}
		if (inverse) {
			// float norm = (float)Math.sqrt(d*h*w);
			final float norm = d * h * w;
			for (int k3 = 0; k3 < d; k3++) {
				for (int k2 = 0; k2 < h; k2++) {
					for (int k1 = 0; k1 < w; k1++) {
						data[k3][k1 + w * k2] /= norm;
					}
				}
			}
		}
	}

	float[] hartleyCoefs(final int max) {
		final float[] cas = new float[max * max];
		int ind = 0;
		for (int n = 0; n < max; n++) {
			for (int k = 0; k < max; k++) {
				final double arg = (2 * Math.PI * k * n) / max;
				cas[ind++] = (float) (Math.cos(arg) + Math.sin(arg));
			}
		}
		return cas;
	}

	void slowHT(final float[] u, final float[] cas, final int max,
		final float[] work)
	{
		int ind = 0;
		for (int k = 0; k < max; k++) {
			float sum = 0;
			for (int n = 0; n < max; n++) {
				sum += u[n] * cas[ind++];
			}
			work[k] = sum;
		}
		for (int k = 0; k < max; k++) {
			u[k] = work[k];
		}
	}

	void makeSinCosTables(final int maxN, final float[] s, final float[] c) {
		final int n = maxN / 4;
		double theta = 0.0;
		final double dTheta = 2.0 * Math.PI / maxN;
		for (int i = 0; i < n; i++) {
			c[i] = (float) Math.cos(theta);
			s[i] = (float) Math.sin(theta);
			theta += dTheta;
		}
	}

	/** Row-column Fast Hartley Transform */
	void rc2DFHT(final float[] x, final int w, final int h, final float[] sw,
		final float[] cw, final float[] sh, final float[] ch)
	{
		for (int row = 0; row < h; row++)
			dfht3(x, row * w, w, sw, cw);
		final float[] temp = new float[h];
		for (int col = 0; col < w; col++) {
			for (int row = 0; row < h; row++) {
				temp[row] = x[col + w * row];
			}
			dfht3(temp, 0, h, sh, ch);
			for (int row = 0; row < h; row++) {
				x[col + w * row] = temp[row];
			}
		}
	}

	/* An optimized real FHT */
	void dfht3(final float[] x, final int base, final int maxN, final float[] s,
		final float[] c)
	{
		int stage, gpNum;
		int gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		Nlog2 = log2(maxN);
		BitRevRArr(x, base, Nlog2, maxN); // bitReverse the input array
		gpSize = 2; // first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum = 0; gpNum < numGps; gpNum++) {
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x[base + Ad1] + x[base + Ad2]; // a + b
			rt2 = x[base + Ad1] - x[base + Ad2]; // a - b
			rt3 = x[base + Ad3] + x[base + Ad4]; // c + d
			rt4 = x[base + Ad3] - x[base + Ad4]; // c - d
			x[base + Ad1] = rt1 + rt3; // a + b + (c + d)
			x[base + Ad2] = rt2 + rt4; // a - b + (c - d)
			x[base + Ad3] = rt1 - rt3; // a + b - (c + d)
			x[base + Ad4] = rt2 - rt4; // a - b - (c - d)
		}
		if (Nlog2 > 2) {
			// third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			// IJ.log("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage = 2; stage < Nlog2; stage++) {
				for (gpNum = 0; gpNum < numGps; gpNum++) {
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0; // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x[base + Ad1];
					x[base + Ad1] = x[base + Ad1] + x[base + Ad2];
					x[base + Ad2] = rt1 - x[base + Ad2];
					rt1 = x[base + Ad3];
					x[base + Ad3] = x[base + Ad3] + x[base + Ad4];
					x[base + Ad4] = rt1 - x[base + Ad4];
					for (bfNum = 1; bfNum < numBfs; bfNum++) {
						// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base + Ad2] * c[CSAd] + x[base + Ad4] * s[CSAd];
						rt2 = x[base + Ad4] * c[CSAd] - x[base + Ad2] * s[CSAd];

						x[base + Ad2] = x[base + Ad1] - rt1;
						x[base + Ad1] = x[base + Ad1] + rt1;
						x[base + Ad4] = x[base + Ad3] + rt2;
						x[base + Ad3] = x[base + Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
			} /* end for all stages */
		} /* end if Nlog2 > 2 */
	}

	int log2(final int x) {
		int count = 15;
		while (!btst(x, count))
			count--;
		return count;
	}

	private boolean btst(final int x, final int bit) {
		// int mask = 1;
		return ((x & (1 << bit)) != 0);
	}

	void BitRevRArr(final float[] x, final int base, final int bitlen,
		final int maxN)
	{
		int l;
		final float[] tempArr = new float[maxN];
		for (int i = 0; i < maxN; i++) {
			l = BitRevX(i, bitlen); // i=1, l=32767, bitlen=15
			tempArr[i] = x[base + l];
		}
		for (int i = 0; i < maxN; i++)
			x[base + i] = tempArr[i];
	}

	private int BitRevX(final int x, final int bitlen) {
		int temp = 0;
		for (int i = 0; i <= bitlen; i++)
			if ((x & (1 << i)) != 0) temp |= (1 << (bitlen - i - 1));
		return temp & 0x0000ffff;
	}

	static public void showAbout() {
		IJ.showMessage("About Iterative Decon 3D ...",
			"Iterative convolution and positive deconvolution\n" + "in 3D");
	}

}// Convolve_3D
