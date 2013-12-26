import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Font;
import java.awt.Rectangle;

public class Event_Stamper implements PlugInFilter
{
    private ImagePlus imp;
    private double time;
    private static int x = 2;
    private static int y = 15;
    private static int size = 20;
    private int maxWidth;
    private Font font;
    private int frame = 0;
    private static int startFrame = 1;
    private static int endFrame = 1;
    private static String eventText = "";
    private boolean firstSlice = true;
    private boolean canceled;
    private int n = 1;

    String getString(double time)
    {
        return eventText;
    }

    public void run(ImageProcessor ip)
    {
        ip.setAntialiasedText(true);
        if (firstSlice) {
            showDialog(ip);
        }
        if (canceled) {
            return;
        }
        ip.setFont(font);
        ip.setColor(Toolbar.getForegroundColor());
        String s = eventText;
        ip.moveTo(x, y);
        frame++;
        if(frame >= startFrame && frame <= endFrame) {
            ip.drawString(s);
        }
    }

    public int setup(String s, ImagePlus imp)
    {
        this.imp = imp;
        IJ.register(Event_Stamper.class);
        return DOES_STACKS | DOES_ALL;
    }

    void showDialog(ImageProcessor ip)
    {
        firstSlice = false;
        Rectangle roi = ip.getRoi();
        if (roi.width < ip.getWidth() || roi.height < ip.getHeight()) {
            x = roi.x;
            y = roi.y + roi.height;
        }
        int i = (int)(((double)roi.height - 1.105256) / 0.934211);
        int j = imp.getCurrentSlice();
        GenericDialog gd = new GenericDialog("Event Stamper");
        gd.addStringField("Text:", eventText);
        gd.addNumericField("Starting Frame:", j, 0);
        gd.addNumericField("End Frame:", j, 0);
        gd.addNumericField("X Location:", x, 0);
        gd.addNumericField("Y Location:", y, 0);
        gd.addNumericField("Font Size:", i, 0);
        gd.showDialog();
        if(gd.wasCanceled()) {
            canceled = true;
            return;
        }
        eventText = gd.getNextString();
        startFrame = (int)gd.getNextNumber();
        endFrame = (int)gd.getNextNumber();
        x = (int)gd.getNextNumber();
        y = (int)gd.getNextNumber();
        i = (int)gd.getNextNumber();
        font = new Font("SansSerif", 0, i);
        ip.setFont(font);
        time = startFrame;
        if (y < i) {
            y = i;
        }
        maxWidth = ip.getStringWidth(getString(startFrame + endFrame * imp.getStackSize()));
        imp.startTiming();
    }
}
