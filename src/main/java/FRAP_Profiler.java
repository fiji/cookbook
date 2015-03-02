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





public class FRAP_Profiler implements PlugInFilter, Measurements  {

	ImagePlus imp;
	DecimalFormat df3 = new DecimalFormat("##0.000");
	DecimalFormat df2 = new DecimalFormat("##0.00");
	DecimalFormat df1 = new DecimalFormat("##0.0");
	RoiManager rm;

	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES+ROI_REQUIRED;
	}

	public void run(ImageProcessor ip) {
		if (imp.getStackSize()<2) {
			IJ.showMessage("ZAxisProfiler", "This command requires a stack.");
			return;
		}

		rm = RoiManager.getInstance();
		if (rm==null)
			{IJ.error("Roi Manager is not open"); return;}
		rm.select(0);	
		Roi roi = imp.getRoi();

		Hashtable table = rm.getROIs();
		java.awt.List list = rm.getList();
		int roiCount = list.getItemCount();
		Roi[] rois = new Roi[roiCount];
		for (int i=0; i<roiCount; i++) {
			String label = list.getItem(i);
			Roi roi2 = (Roi)table.get(label);
			if (roi2==null) continue;
			rois[i] = roi2;
		}



		//Roi full = new Roi (0,0,imp.getWidth(),imp.getHeight());
		roi = rois[roiCount-2];
		Roi full = rois [roiCount-1];


		if ((full.getBounds().width*full.getBounds().width)<(roi.getBounds().width*roi.getBounds().width)) 
			{
			roi = rois[roiCount-1];
			full = rois [roiCount-2];	
			}
	
		Rectangle r = roi.getBoundingRect();
		
		if (roi.getType()>=Roi.LINE) {
			IJ.showMessage("ZAxisProfiler", "This command does not work with line selections.");
			return;
		}
		double minThreshold = ip.getMinThreshold();
	        	double maxThreshold = ip.getMaxThreshold();

		float[] yF = getZAxisProfile(roi, full, minThreshold, maxThreshold);

//even values in yF are the ROI, odd values the fill roi

		Calibration cal = imp.getCalibration();
		String timeUnit = cal.getTimeUnit();

//create array for full cell values
		float[]y2 = new float[(yF.length/2)]; 

//create array for ROI values
		float[]y3 = new float[(yF.length/2)]; 

		float[]y = new float[(yF.length/2)]; 
//get max	
		float yFmax=0;
		float yMax=0;
		float yFmin=65500;
		float yMin=65500;

		for (int v = 0; v<yF.length; v++)
			{
			if (v%2!=0) y2[v/2]=yF[v];
			if (v%2==0) y3[v/2]=yF[v];

			if((yFmax<yF[v])&&(v%2!= 0)) 	yFmax=yF[v];	
			if((yMax<yF[v]) &&(v%2== 0))	 yMax=yF[v];	

			if((yFmin>yF[v]) &&(v%2!= 0))	yFmin=yF[v];	
			if((yMin>yF[v]) &&(v%2== 0))	yMin=yF[v];	

			}


//normalise
	


yMin=0; yFmin=0;

		for (int u = 0; u<y.length; u++)
			{
		
			y[u] = (y2[u]/yFmax) / (y3[u]/yMax);

			

			}
		
		float timescale = 1;
		if (y!=null)
			{float[] x = new float[y.length];
			
			
		

			for (int i=0; i<x.length; i++) 
				{
			
				if (cal.frameInterval==0||Double.isNaN(cal.frameInterval))
					{x[i] = ((i));}
				else { x[i] = ((i)*(float)cal.frameInterval); 
					timescale = (float)cal.frameInterval;
					}
				}
			
			

			PlotWindow pwF = new PlotWindow("rawFRAP: "+ imp.getTitle()+"-x"+r.x+".y"+r.y+".w"+r.width+".h"+r.height, timeUnit, "Mean", x, y2);
			pwF.addPoints(x, y3,PlotWindow.LINE);
			

			PlotWindow pw = new PlotWindow("procFRAP: "+ imp.getTitle()+"-x"+r.x+".y"+r.y+".w"+r.width+".h"+r.height, timeUnit, "Mean", x, y);
			double [] a = Tools.getMinMax(x);
            		double xmin=a[0], xmax=a[1];
			pwF.setLimits(xmin,xmax,yFmin,yFmax);
			pwF.draw();
            		float [] values2 = new float [y.length];
           			int valsize = values2.length;
			//for (int j=0; j<valsize; j++) values2[j] = y[j];

            		a = Tools.getMinMax(y);
           			double ymin=a[0], ymax=a[1];
		            pw.setLimits(xmin,xmax,ymin,ymax);

		//fit curve
			int sliceMin=0;
			for (int i=0;i<y.length; i++)
				{
				if(y[i]==ymin) sliceMin=i+1;	
				}
			float[] f = new float[y.length-sliceMin];
			float[] x2 = new float[y.length-sliceMin];
			float[] x3 = new float[y.length-sliceMin];
			double[] fd = new double[y.length-sliceMin];
			double[] x2d = new double[y.length-sliceMin];
			for (int i=0;i<y.length-sliceMin; i++)
				{f[i]=y[i+sliceMin];	
				fd[i]=(double)f[i];	
				x2[i] = x[i]+(float)((float)sliceMin*timescale);	
				//x3[i]=x2[i]+(4*timescale );
				x2d [i] = (double)x2[i];	
				//IJ.log(i + "\t"+ f[i]);
				}
			CurveFitter cf = new CurveFitter(x2d, fd) ;
			cf.doFit(CurveFitter.EXP_RECOVERY);
			double[] p = cf.getParams();
			//p[0]*(1-Math.exp(-p[1]*x))+p[2])
			IJ.log("p[0]*(1-Math.exp(-p[1]*x)+p[2]):  "+ df2.format(p[0])+";  "+ df2.format(p[1]) +";  "+df2.format(p[2]));
			double tmp=0;
			float[] fit = new float[y.length];
			float fitY=0;
			for (int z=0; z<x2.length; z++)
				{
				tmp = x2[z]-p[2];
       			     	if (tmp<0.001) tmp = 0.001;
				fit[z]=(float)(p[0]*(1-Math.exp(-p[1]*z))+p[2]);
				}
			
			pw.setColor(Color.red);
			pw.addPoints(x2, fit, PlotWindow.LINE);
			pw.setColor(Color.black);
			pw.draw();

			}
		}

	float[] getZAxisProfile(Roi roi, Roi full, double minThreshold, double maxThreshold) {

		ImageStack stack = imp.getStack();
		int size = stack.getSize();
		float[] values = new float[(size*2)];
		imp.setRoi(roi);
		ImageProcessor mask = imp.getMask();
		//int[] mask = imp.getMask();
		Rectangle r = roi.getBoundingRect();
	
		imp.setRoi(full);
		ImageProcessor mask2 = imp.getMask();
		Rectangle rFull = full.getBoundingRect();

		Calibration cal = imp.getCalibration();
		Analyzer analyzer = new Analyzer(imp);
		
		int measurements = analyzer.getMeasurements();
		boolean showResults = measurements!=0 && measurements!=LIMIT;
        		boolean showingLabels = (measurements&LABELS)!=0 || (measurements&SLICE)!=0;

		measurements |= MEAN;
		if (showResults) {
		if (!analyzer.resetCounter())
				return null;
		float avg=0;
		
		}
		int k=0;
		for (int i=1; i<=size; i++) {
			ImageProcessor ip = stack.getProcessor(i);
			if (minThreshold!=ImageProcessor.NO_THRESHOLD)
		            ip.setThreshold(minThreshold,maxThreshold,ImageProcessor.NO_LUT_UPDATE);
			
			ip.setRoi(full);
			ip.setMask(mask2);
			ImageStatistics stats2 = ImageStatistics.getStatistics(ip, measurements, cal);
			values[k] = (float)stats2.mean; k++;
            		
			ip.setRoi(r);
			ip.setMask(mask);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, cal);
			analyzer.saveResults(stats, roi);
			values[k] = (float)stats.mean;k++;


			}
		return values;
			}

}


