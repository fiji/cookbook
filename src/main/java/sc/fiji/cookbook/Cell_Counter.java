package sc.fiji.cookbook;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.StackWindow;
import ij.gui.Toolbar;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

public class Cell_Counter implements PlugInFilter {

	int type1Count, type2Count, type3Count, type4Count, totalCount;
	ImagePlus img, img2;
	int type;
	ResultsTable rt;
	Analyzer an;

	@Override
	public int setup(final String arg, final ImagePlus img) {
		this.img = img;
		reset();
		return DOES_ALL;
	} // end of setup

	@Override
	public void run(final ImageProcessor ipp) {
		final ImagePlus imp1 = WindowManager.getCurrentImage();
		if (imp1 == null) {
			IJ.showMessage("You need an image...");
		} // no image...
		else if (imp1.getStackSize() == 1) {
			ImageProcessor ip = imp1.getProcessor();
			ip.setRoi((Roi) null);
			ip = ip.crop();
			final ImagePlus img =
				new ImagePlus("Counter Window - " + imp1.getTitle(), ip);
			final CustomCanvas cc = new CustomCanvas(img);
			new CustomWindow(img, cc);
			img2 = WindowManager.getCurrentImage();
			resultSetup();
		} // making the counter window
		else if (imp1.getStackSize() > 1) {
			final ImageStack stack = imp1.getStack();
			final int size = stack.getSize();
			final ImageStack stack2 = imp1.createEmptyStack();
			for (int i = 1; i <= size; i++) {
				ImageProcessor ip = stack.getProcessor(i);
				ip.setRoi((Roi) null);
				ip = ip.crop();
				stack2.addSlice(stack.getSliceLabel(i), ip);
			}
			final CustomCanvas cc = new CustomCanvas(img);
			new CustomStackWindow(img, cc);
			img2 = WindowManager.getCurrentImage();
			resultSetup();
		} // Stacks supported
	} // end of run

	public void resultSetup() {
		IJ.setColumnHeadings("Active Type\t Type 1\tType 2\tType 3\tType 4\tTotal");
	} // the result table

	void reset() {
		type1Count = 0;
		type2Count = 0;
		type3Count = 0;
		type4Count = 0;
		totalCount = 0;
	} // end of results reset

	void report() {
		IJ.log("Type 1: " + type1Count);
		IJ.log("Type 2: " + type2Count);
		IJ.log("Type 3: " + type3Count);
		IJ.log("Type 4: " + type4Count);
		IJ.log("Total:  " + totalCount);
	} // end of result report method

	class CustomCanvas extends ImageCanvas {

		CustomCanvas(final ImagePlus imp) {
			super(imp);
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			if (Toolbar.getToolId() == Toolbar.MAGNIFIER) {
				super.mousePressed(e);
				return;
			}
			final int x = offScreenX(e.getX());
			final int y = offScreenY(e.getY());
			if (Toolbar.getToolId() != Toolbar.CROSSHAIR) Toolbar.getInstance()
				.setTool(Toolbar.CROSSHAIR);
			if (type == 1) { // button 1
				type1Count++;
				totalCount++;
				IJ.setForegroundColor(255, 255, 255);
				img2.setRoi(x - 4, y - 4, 9, 9);
				IJ.run("Fill", "slice");
				IJ.log(type + "\t" + type1Count + "\t" + type2Count + "\t" +
					type3Count + "\t" + type4Count + "\t" + totalCount);
			}
			if (type == 2) { // button 2
				type2Count++;
				totalCount++;
				IJ.setForegroundColor(0, 255, 0);
				img2.setRoi(x - 4, y - 4, 9, 9);
				IJ.run("Fill", "slice");
				IJ.log(type + "\t" + type1Count + "\t" + type2Count + "\t" +
					type3Count + "\t" + type4Count + "\t" + totalCount);
			}
			if (type == 3) { // button 3
				type3Count++;
				totalCount++;
				IJ.setForegroundColor(0, 0, 255);
				img2.setRoi(x - 4, y - 4, 9, 9);
				IJ.run("Fill", "slice");
				IJ.log(type + "\t" + type1Count + "\t" + type2Count + "\t" +
					type3Count + "\t" + type4Count + "\t" + totalCount);
			}
			if (type == 4) { // button4
				type4Count++;
				totalCount++;
				IJ.setForegroundColor(255, 255, 0);
				img2.setRoi(x - 4, y - 4, 9, 9);
				IJ.run("Fill", "slice");
				IJ.log(type + "\t" + type1Count + "\t" + type2Count + "\t" +
					type3Count + "\t" + type4Count + "\t" + totalCount);
			}
			if (type == 5) { // button 5
				return;
			}
			return;
		}
	} // end of customcanvas

	class CustomWindow extends ImageWindow implements ActionListener {

		private Button button1, button2, button3, button4, button5;

		CustomWindow(final ImagePlus imp, final ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			final Panel panel = new Panel();
			panel.setLayout(new BorderLayout());
			final Panel buttonPanel = new Panel();
			button1 = new Button(" Type 1 ");
			button1.addActionListener(this);
			buttonPanel.add(button1);
			button2 = new Button(" Type 2 ");
			button2.addActionListener(this);
			buttonPanel.add(button2);
			button3 = new Button(" Type 3 ");
			button3.addActionListener(this);
			buttonPanel.add(button3);
			button4 = new Button(" Type 4 ");
			button4.addActionListener(this);
			buttonPanel.add(button4);
			button5 = new Button(" Results ");
			button5.addActionListener(this);
			buttonPanel.add(button5);
			add(BorderLayout.NORTH, buttonPanel);
			add(panel);
			pack();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object b = e.getSource();
			if (b == button1) type = 1;
			else if (b == button2) type = 2;
			else if (b == button3) type = 3;
			else if (b == button4) type = 4;
			else if (b == button5) {
				report();
				reset();
			}
		}
	}// end of Custom Window adding the buttons

	class CustomStackWindow extends StackWindow {

		private Button button1, button2, button3, button4, button5;

		CustomStackWindow(final ImagePlus imp, final ImageCanvas ic) {
			super(imp, ic);
			addPanel();
		}

		void addPanel() {
			final Panel panel = new Panel();
			panel.setLayout(new BorderLayout());
			final Panel buttonPanel = new Panel();
			button1 = new Button(" Type 1 ");
			button1.addActionListener(this);
			buttonPanel.add(button1);
			button2 = new Button(" Type 2 ");
			button2.addActionListener(this);
			buttonPanel.add(button2);
			button3 = new Button(" Type 3 ");
			button3.addActionListener(this);
			buttonPanel.add(button3);
			button4 = new Button(" Type 4 ");
			button4.addActionListener(this);
			buttonPanel.add(button4);
			button5 = new Button(" Results ");
			button5.addActionListener(this);
			buttonPanel.add(button5);
			add(BorderLayout.NORTH, buttonPanel);
			add(panel);
			pack();
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			final Object b = e.getSource();
			if (b == button1) type = 1;
			else if (b == button2) type = 2;
			else if (b == button3) type = 3;
			else if (b == button4) type = 4;
			else if (b == button5) {
				report();
				reset();
			}
		}
	}// end of Custom Window adding the buttons
}// end of Counter Class
