   import ij.plugin.filter.*;
import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.util.*;
import ij.plugin.filter.Analyzer;
import java.awt.Rectangle;
import ij.text.*;
import java.text.DecimalFormat; 
import ij.plugin.PlugIn;
import java.awt.*;

public class Ratio_Profiler implements PlugInFilter, Measurements  {

	ImagePlus imp;
	StringBuffer sb = new StringBuffer();
	String timePoint="";
DecimalFormat df2 = new DecimalFormat("##0.00");
DecimalFormat df0 = new DecimalFormat("##0");
boolean isAltKey =IJ.altKeyDown();
	public int setup(String arg, ImagePlus imp) {
		this.imp = imp;
		return DOES_ALL+NO_CHANGES+ROI_REQUIRED;
	}

	public void run(ImageProcessor ip) {
//isAltKey = IJ.altKeyDown();

	//	if ((imp.getStackSize()<2)|| (imp.getStackSize()%2 != 0)) {
		//	IJ.showMessage("Ratio_Profiler", "This command requires a stack with even number of slices.");
		//	return;
		//}
		
		Roi roi = imp.getRoi();
		if (roi.getType()>=Roi.LINE) {
			IJ.showMessage("ZAxisProfiler", "This command does not work with line selections.");
			return;
		}
		  Calibration cal = imp.getCalibration();
		Rectangle r = imp.getRoi().getBoundingRect();

		String units="";
//IJ.write("x="+r.x+", y="+r.y+", width="+r.width+", height="+r.height);
		sb.append(r.x+"x\t"+r.y+"y\t"+r.width+"w\t"+r.height+"h\n");
		
		float[] y = getZAxisProfile(roi);
ImageStack stack = imp.getStack();
		int size = stack.getSize()/2;
		if (y!=null) {
			float[] x = new float[size];

			float[] ratio = new float[size];
			float[] ch2 = new float[size];
			float[] ch1 = new float[size];

		if (cal.frameInterval==0||Double.isNaN(cal.frameInterval))
				{ units = "Slice";
				for (int i=0; i<size;i++) x[i] = ((i));}
	
			else { units = "Seconds";
				for (int i=0; i<size; i++) x[i] = ((i)*(float)cal.frameInterval);}


			for (int i=0; i<4; i++) x[i] = 0;
			
			for (int i=0; i<size; i++) {
				 ratio [i]= y[i+4];
				ch1[i] = y[i+size+4];
 				ch2[i]=y[i+size+size+4];}
				
			
//plot ratio
			
			PlotWindow pw = new PlotWindow("Ratio: "+ imp.getTitle()+"-x"+r.x+".y"+r.y+".w"+r.width+".h"+r.height, units, "Mean Ratio", x, ratio);
			double [] a = Tools.getMinMax(x);
           			double xmin=a[0], xmax=a[1];
		          	float [] values2 = new float [(ratio.length)];
			int valsize = values2.length;
			int valsize2 = valsize/2;	
			int m=1;

			//get ratio range
			for (int j=0; j<(size); j++) 
				values2[j] = ratio[j];
			            a = Tools.getMinMax(values2);
			            double ymin=a[0], ymax=a[1];
		            	pw.setLimits(xmin,xmax,ymin,ymax);
			pw.setColor(Color.green);
         	      		pw.changeFont(new Font("Helvetica", Font.PLAIN, 12));
			if (!isAltKey)  pw.addLabel(0, 0, "Ch1÷Ch2");
			else  pw.addLabel(0, 0, "Ch2÷Ch1");
;
			pw.draw();


//plot raw data

			//float [] values2 = new float [(ch1.length)];
			//int valsize = values2.length;
			//int valsize2 = valsize/2;	
			m=1;
			for (int j=0; j<valsize; j++) 
				values2[j] = ch1[j];
			            double [] b = Tools.getMinMax(values2);
		        	  double ch1min=b[0];
			double ch1max=b[1];
		         
			for (int j=0; j<valsize; j++) 
				values2[j] = ch2[j];
			            double [] c = Tools.getMinMax(values2);
		        	double ch2min=c[0];
			double ch2max=c[1];
			
			if (ch1min>ch2min) ymin=ch1min;
			if (ch1min>ch2min) ymin=ch2min;

			if (ch1max>ch2max) ymax=ch1max;
			if (ch1max<ch2max) ymax=ch2max;

			PlotWindow pw2 = new PlotWindow("RAW: "+ imp.getTitle()+"-x"+r.x+".y"+r.y+".w"+r.width+".h"+r.height, units, "Mean", x, ch1);
			 pw2.setLimits(xmin, xmax, ymin, ymax);
			pw2.setColor(Color.red);
         	      		pw2.changeFont(new Font("Helvetica", Font.PLAIN, 12));
       		 	pw2.addLabel(0.07, 0, "Ch2");

			 pw2.addPoints(x,ch2,PlotWindow.LINE);

		
			//pw2.setLimits(xmin,xmax,ymin,ymax);
			//pw2.setLimits(xmin,xmax,0,150);

			pw2.setColor(Color.blue);
		       	pw2.addLabel( 0, 0, "Ch1");
			pw2.draw();		

	
	
	
	

    if (!isAltKey) new TextWindow( "Ratio_Profile: "+imp.getTitle(), units+"\tCh1\tCh2\tCh1÷Ch2", sb.toString(), 300, 400);

     if (isAltKey) new TextWindow( "Ratio_Profile: "+imp.getTitle(), units+"\tCh1\tCh2\tCh2÷Ch1", sb.toString(), 300, 400);

		}
	}

	float[] getZAxisProfile(Roi roi)	
		{
		
		ImageStack stack = imp.getStack();
		int size = stack.getSize()/2;
		float ratioValue=0;
		float[] values = new float[size+size+size+4];
		
		ImageProcessor mask = imp.getMask();
		//int[] mask = imp.getMask();
		Rectangle r = imp.getRoi().getBoundingRect();
		Calibration cal = imp.getCalibration();
		Analyzer analyzer = new Analyzer(imp);
		float ch1Prev =0;
		int k=1;
		values[0] = r.x;
		values[1] = r.y;
		values[2] = r.width;
		values[3] = r.height;
		int measurements = analyzer.getMeasurements();
		boolean showResults = measurements!=0 && measurements!=LIMIT;
		measurements |= MEAN;
		if (showResults) 
			{
			if (!analyzer.resetCounter())
			return null;
			}
		for (int i=1; i<=(size*2); i++) 
			{
			ImageProcessor ip = stack.getProcessor(i);
			ip.setRoi(r);
			ip.setMask(mask);
			ImageStatistics stats = ImageStatistics.getStatistics(ip, measurements, cal);
			analyzer.saveResults(stats, roi);
			//if (showResults)
			//analyzer.displayResults();
			
			timePoint=(df2.format(((i-1)/2)*cal.frameInterval));
			if (cal.frameInterval==0) timePoint=df0.format(i/2);
			if (i%2 != 0)
			 	{
				sb.append(timePoint+"\t"+(float)stats.mean);
				ch1Prev = (float)stats.mean;
				values [k+3+(size)]= (float)stats.mean;
				k++;
				}
			if (i%2 == 0)
				{
				if (isAltKey) ratioValue = (float)stats.mean/ch1Prev;
				if (!isAltKey) ratioValue = ch1Prev/(float)stats.mean;
				
				sb.append("\t"+(float)stats.mean+"\t"+ratioValue+"\n");
				values[k+2] = ratioValue;
  				values[k+2+size+size] = (float)stats.mean;
				}
			}
		return values;

		}

}


