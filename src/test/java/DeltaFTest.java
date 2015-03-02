import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import org.junit.Test;

/**
 * Tests {@link Delta_F_up} and {@link Delta_F_down}.
 *
 * @author Aparna Pal
 * @author Curtis Rueden
 */
public class DeltaFTest {

	@Test
	public void testDeltaUp() {
		testDelta(true);
	}

	@Test
	public void testDeltaDown() {
		testDelta(false);
	}

	private void testDelta(final boolean up) {
		for (int i = 0; i < 10; i++)
			testDelta(up, i);
	}

	private void testDelta(final boolean up, final double value) {
		// Prep the image stack.
		IJ.newImage("Image", "32-bit ramp", 100, 100, 2);
		final ImagePlus imp = IJ.getImage();
		assertNotNull(imp);
		IJ.run(imp, "Add...", "value=" + value + " slice");

		// Execute the Delta F command.
		if (up) new Delta_F_up().run("");
		else new Delta_F_down().run("");

		// Validate the result.
		final ImagePlus result = IJ.getImage();
		assertNotNull(result);
		assertEquals(1, result.getStackSize());
		final ImageProcessor ip = result.getProcessor();
		for (int y = 0; y < result.getHeight(); y++) {
			for (int x = 0; x < result.getWidth(); x++) {
				assertEquals("(" + x + "," + y + ")", value, ip.getf(x, y), 0);
			}
		}
	}

}
