import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Microscope_Scale implements  PlugInFilter {

/* DESCRIPTION & CUSTOMIZATION INSTRUCTIONS
        This plugin lets you calibrate images spatially using hard-coded arrays of magnifications,
        calibration values and length units. The calibration can optionally be set as global. After
        spatial calibration the plugin will also optionally run Wayne's new scale-bar plugin, available
        in version 1.28h. To customize the plugin for your specific microscope, edit the arrays
        between the "START EDIT" and "END EDIT" comments below. Save anywhere in the
        "plugins" directory, then use "Compile and Run" option under the "plugins" menu to create
        a java class. Restart ImageJ.
**/
        ImagePlus imp;
        private static boolean addScaleBar = false;  //  if true run scalebar plugin - available in version 1.28h
        private static boolean isGlobalCal = false; // if true, set selected calibration as the global calibration
        private static int magIndex = 4; // index of initial selected magnification in dropdown menu
        private static int binIndex = 0; // index of initial selected magnification in dropdown menu
        private static int scopeIndex = 0; // index of initial selected magnification in dropdown menu

/* START EDIT
        Edit the following arrays using your microscope's nominal magnification steps, the
        corresponding spatial calibration and the length units of the spatial calibration.
        Make sure the arrays are of equal length.
**/
        // Objective descriptions 
	        private static String[] scopes =  { "Time-Lapse","Deconvolution", "Leica"};

        private static String[] mags =  { "TL ×2.5", "TL ×10", "TL ×16", "TL ×20","TL ×40 LWD", "TL ×40 oil","Dec ×2.5", "Dec ×10", "Dec ×20", "Dec ×40", "Dec ×65", "Dec ×100", "Leica ×5", "Leica ×10", "Leica ×20", "Leica ×40", "Leica ×100","TAT ×2","TAT ×10","TAT ×20","TAT ×40",};
private static String[] binning={"1×1","2×2","4×4","8×8"};
//

        // spatial calibration for the different objectives  - width of one pixel (pixelWidth)
        private static double[] xscales = 	{2.439024, 0.628141 ,0.380952 , 0.312500, 0.151492 , 0.155231, 
					2.56410, 0.67114, 0.32787, 0.16722, 0.10707, 0.07143,
					1.345, 0.66,  0.334,  0.166, 0.0751,
					1.087,0.2188,0.1109,0.05618};
private static int[] binScaling = {1,2,4,8};
private static int choiceOffset=0;

        // units for the spacial calibrations given in xscales array above
        private static String[] units =  { "µm",  "µm",  "µm",  "µm",  "µm", "µm","µm",  "µm",  "µm",  "µm",  "µm", "µm", "µm", "µm", "µm", "µm", "µm", "µm", "µm", "µm", "µm"};
/* END EDIT **/

        public int setup(String arg, ImagePlus imp) {
                this.imp = imp;
                if (imp==null)
                        {IJ.noImage(); return DONE;}
                return DOES_ALL;
        }

        public void run(ImageProcessor ip) {
                if (doDialog()) {

	

                        Calibration oc = imp.getCalibration().copy();
                        oc.setUnit(units[magIndex]);
		
                        oc.pixelWidth=xscales[magIndex]*binScaling[binIndex];
                        oc.pixelHeight=oc.pixelWidth;
                        if (isGlobalCal) {
                                imp.setGlobalCalibration(oc);
                                int[] list = WindowManager.getIDList();
                                if (list==null) return;
                                for (int i=0; i<list.length; i++) {
                                        ImagePlus imp2 = WindowManager.getImage(list[i]);
                                        if (imp2!=null) imp2.getWindow().repaint();
                                }
                                } else {
                                imp.setGlobalCalibration(null);
                                imp.setCalibration(oc);
                                imp.getWindow().repaint();
                        }
        //                if (addScaleBar){
          //                      IJ.run("Scale Bar...");
             //          }
                }
        }

        private boolean doDialog() {
                GenericDialog gd = new GenericDialog("Scale Microscope Image...");
         
                gd.addChoice("Objective", mags, mags[magIndex]);
	gd.addChoice("Camera Binning", binning, binning[binIndex]); 
                gd.addCheckbox("Global Calibration", isGlobalCal);
  //              gd.addCheckbox("Add Scale Bar", addScaleBar);
                gd.showDialog();
                if (gd.wasCanceled()) {return false;}

                magIndex=gd.getNextChoiceIndex();
	binIndex=gd.getNextChoiceIndex();
                isGlobalCal = gd.getNextBoolean();
      //          addScaleBar = gd.getNextBoolean();
                return true;
        }
}
