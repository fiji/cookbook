import ij.*;
import ij.process.*;
import ij.gui.*;
import java.awt.*;
import ij.plugin.*;

public class Zoomify_ implements PlugIn {

	public void run(String arg) {
		ImagePlus imp1 = WindowManager.getCurrentImage();
		Roi roi = imp1.getRoi();
		if(imp1.getRoi()==null) 
			{IJ.showMessage("Error", "No ROI");
			return;
			}	
		ImageProcessor ip1 = imp1.getProcessor();
		int iwidth = imp1.getWidth();
		int iheight = imp1.getHeight();
		ip1.setInterpolate(true);
		int rzwidth, rzheight, rzx, rzy=0;	
		Rectangle r =ip1.getRoi();
		int width =imp1.getWidth();
		int height=imp1.getHeight();
		int x=0;
		int y =0;
		int frames = 5;
		width = r.width;
		height = r.height;
		x=r.x;
		y=r.y;
		int xOffset,yOffset=0;
				
		GenericDialog gd = new GenericDialog("Zoom Movie");
		gd.addNumericField("Number of frames",frames,0);
		gd.showDialog();
        		if (gd.wasCanceled())     return ;
	       	frames= (int)gd.getNextNumber();
		
		double maxScale= 1/((double)iwidth/(double)width);
		double maxScale2 = 1/((double)iheight/(double)height);
		if (maxScale>maxScale2) maxScale  = maxScale2;
		double scaleStep = (1-maxScale)/(double)(frames-1);
		//double scales = maxScale;
		
		ImageStack stackZoom = new ImageStack(width,height);

		for (int z=0; z<frames; z++)
		//while(scales<2)
			{
			if (IJ.escapePressed()) 
				{IJ.beep();  return;}
			
		//calculate scale factor
			double scales = maxScale+(z*scaleStep);	
			IJ.showStatus("Zoomifying: "+ z +"/" + frames+".  ×"+IJ.d2s(scales*100,0)+"%. Esc to cancel");
		
		//create new ip
			ImageProcessor ipz=ip1;
			ipz.setInterpolate(true);

		//set enlarged ROI
			rzwidth = (int)((width)/(scales));
			rzheight = (int)((height)/(scales));
			rzx = (int)((double)x-((double)rzwidth/(double)2))+(width/2);
			rzy = (int)((double)y-((double)rzheight/(double)2))+(height/2);
			if(rzx+rzwidth>iwidth) rzx = iwidth-rzwidth;
			if(rzy+rzheight>iheight) rzy = iheight-rzheight;			
			if(rzx<=0) rzx=0;
			if(rzy<=0) rzy=0;
			if(rzheight>iheight) rzheight = iheight;
			if (rzwidth>iwidth) rzwidth =iwidth;
			ipz.setRoi(rzx, rzy ,rzwidth, rzheight );

			//crop to this size
         			ipz = ipz.crop();

			//scale
			ipz.scale(scales , scales );
			
			//set new ROI to crop scaled ip
			xOffset = (int)(rzwidth/2-(width/2));
			yOffset = (int)((rzheight/2)-(height/2));
			ipz.setRoi(xOffset,yOffset,width,height);
			//crop
			ipz= ipz.crop();
			
			//ensure there is a constant framesize 	
			ipz=ipz.resize(width,height);
			
			//add to stack
			stackZoom.addSlice("Zoom " + IJ.d2s(scales*100,0)+"%", ipz);
			//scales*=2;				
			}
		new ImagePlus("Zoomed Image",stackZoom).show();
		imp1=null;
		ip1=null;
		//ipz=null;
	}

}
