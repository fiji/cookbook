package fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.util.Tools;

import java.awt.Color;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.Hashtable;

public class FRAP_Profiler implements PlugInFilter, Measurements {

	ImagePlus imp;
	DecimalFormat df3 = new DecimalFormat("##0.000");
	DecimalFormat df2 = new DecimalFormat("##0.00");
	DecimalFormat df1 = new DecimalFormat("##0.0");
	RoiManager rm;

	@Override
	public int setup(final String arg, final ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL + NO_CHANGES + ROI_REQUIRED;
	}

	@Override
	public void run(final ImageProcessor ip) {
		if (imp.getStackSize() < 2) {
			IJ.showMessage("ZAxisProfiler", "This command requires a stack.");
			return;
		}

		rm = RoiManager.getInstance();
		if (rm == null) {
			IJ.error("Roi Manager is not open");
			return;
		}
		rm.select(0);
		Roi roi = imp.getRoi();

		final Hashtable<String, Roi> table = rm.getROIs();
		final java.awt.List list = rm.getList();
		final int roiCount = list.getItemCount();
		final Roi[] rois = new Roi[roiCount];
		for (int i = 0; i < roiCount; i++) {
			final String label = list.getItem(i);
			final Roi roi2 = table.get(label);
			if (roi2 == null) continue;
			rois[i] = roi2;
		}

		// Roi full = new Roi (0,0,imp.getWidth(),imp.getHeight());
		roi = rois[roiCount - 2];
		Roi full = rois[roiCount - 1];

		if ((full.getBounds().width * full.getBounds().width) < (roi.getBounds().width * roi
			.getBounds().width))
		{
			roi = rois[roiCount - 1];
			full = rois[roiCount - 2];
		}

		final Rectangle r = roi.getBoundingRect();

		if (roi.getType() >= Roi.LINE) {
			IJ.showMessage("ZAxisProfiler",
				"This command does not work with line selections.");
			return;
		}
		final double minThreshold = ip.getMinThreshold();
		final double maxThreshold = ip.getMaxThreshold();

		final float[] yF = getZAxisProfile(roi, full, minThreshold, maxThreshold);

//even values in yF are the ROI, odd values the fill roi

		final Calibration cal = imp.getCalibration();
		final String timeUnit = cal.getTimeUnit();

//create array for full cell values
		final float[] y2 = new float[(yF.length / 2)];

//create array for ROI values
		final float[] y3 = new float[(yF.length / 2)];

		final float[] y = new float[(yF.length / 2)];
//get max
		float yFmax = 0;
		float yMax = 0;
		float yFmin = 65500;
		float yMin = 65500;

		for (int v = 0; v < yF.length; v++) {
			if (v % 2 != 0) y2[v / 2] = yF[v];
			if (v % 2 == 0) y3[v / 2] = yF[v];

			if ((yFmax < yF[v]) && (v % 2 != 0)) yFmax = yF[v];
			if ((yMax < yF[v]) && (v % 2 == 0)) yMax = yF[v];

			if ((yFmin > yF[v]) && (v % 2 != 0)) yFmin = yF[v];
			if ((yMin > yF[v]) && (v % 2 == 0)) yMin = yF[v];

		}

//normalise

		yMin = 0;
		yFmin = 0;

		for (int u = 0; u < y.length; u++) {

			y[u] = (y2[u] / yFmax) / (y3[u] / yMax);

		}

		float timescale = 1;
		final float[] x = new float[y.length];

		for (int i = 0; i < x.length; i++) {

			if (cal.frameInterval == 0 || Double.isNaN(cal.frameInterval)) {
				x[i] = ((i));
			}
			else {
				x[i] = ((i) * (float) cal.frameInterval);
				timescale = (float) cal.frameInterval;
			}
		}

		final PlotWindow pwF =
			new PlotWindow("rawFRAP: " + imp.getTitle() + "-x" + r.x + ".y" + r.y +
				".w" + r.width + ".h" + r.height, timeUnit, "Mean", x, y2);
		pwF.addPoints(x, y3, PlotWindow.LINE);

		final PlotWindow pw =
			new PlotWindow("procFRAP: " + imp.getTitle() + "-x" + r.x + ".y" + r.y +
				".w" + r.width + ".h" + r.height, timeUnit, "Mean", x, y);
		double[] a = Tools.getMinMax(x);
		final double xmin = a[0], xmax = a[1];
		pwF.setLimits(xmin, xmax, yFmin, yFmax);
		pwF.draw();
		a = Tools.getMinMax(y);
		final double ymin = a[0], ymax = a[1];
		pw.setLimits(xmin, xmax, ymin, ymax);

		// fit curve
		int sliceMin = 0;
		for (int i = 0; i < y.length; i++) {
			if (y[i] == ymin) sliceMin = i + 1;
		}
		final float[] f = new float[y.length - sliceMin];
		final float[] x2 = new float[y.length - sliceMin];
		final double[] fd = new double[y.length - sliceMin];
		final double[] x2d = new double[y.length - sliceMin];
		for (int i = 0; i < y.length - sliceMin; i++) {
			f[i] = y[i + sliceMin];
			fd[i] = f[i];
			x2[i] = x[i] + sliceMin * timescale;
			// x3[i]=x2[i]+(4*timescale );
			x2d[i] = x2[i];
			// IJ.log(i + "\t"+ f[i]);
		}
		final CurveFitter cf = new CurveFitter(x2d, fd);
		cf.doFit(CurveFitter.EXP_RECOVERY);
		final double[] p = cf.getParams();
		// p[0]*(1-Math.exp(-p[1]*x))+p[2])
		IJ.log("p[0]*(1-Math.exp(-p[1]*x)+p[2]):  " + df2.format(p[0]) + ";  " +
			df2.format(p[1]) + ";  " + df2.format(p[2]));
		double tmp = 0;
		final float[] fit = new float[y.length];
		for (int z = 0; z < x2.length; z++) {
			tmp = x2[z] - p[2];
			if (tmp < 0.001) tmp = 0.001;
			fit[z] = (float) (p[0] * (1 - Math.exp(-p[1] * z)) + p[2]);
		}

		pw.setColor(Color.red);
		pw.addPoints(x2, fit, PlotWindow.LINE);
		pw.setColor(Color.black);
		pw.draw();
	}

	float[] getZAxisProfile(final Roi roi, final Roi full,
		final double minThreshold, final double maxThreshold)
	{

		final ImageStack stack = imp.getStack();
		final int size = stack.getSize();
		final float[] values = new float[(size * 2)];
		imp.setRoi(roi);
		final ImageProcessor mask = imp.getMask();
		// int[] mask = imp.getMask();
		final Rectangle r = roi.getBoundingRect();

		imp.setRoi(full);
		final ImageProcessor mask2 = imp.getMask();

		final Calibration cal = imp.getCalibration();
		final Analyzer analyzer = new Analyzer(imp);

		int measurements = Analyzer.getMeasurements();
		final boolean showResults = measurements != 0 && measurements != LIMIT;
		measurements |= MEAN;
		if (showResults) {
			if (!Analyzer.resetCounter()) return null;

		}
		int k = 0;
		for (int i = 1; i <= size; i++) {
			final ImageProcessor ip = stack.getProcessor(i);
			if (minThreshold != ImageProcessor.NO_THRESHOLD) ip.setThreshold(
				minThreshold, maxThreshold, ImageProcessor.NO_LUT_UPDATE);

			ip.setRoi(full);
			ip.setMask(mask2);
			final ImageStatistics stats2 =
				ImageStatistics.getStatistics(ip, measurements, cal);
			values[k] = (float) stats2.mean;
			k++;

			ip.setRoi(r);
			ip.setMask(mask);
			final ImageStatistics stats =
				ImageStatistics.getStatistics(ip, measurements, cal);
			analyzer.saveResults(stats, roi);
			values[k] = (float) stats.mean;
			k++;

		}
		return values;
	}

}
