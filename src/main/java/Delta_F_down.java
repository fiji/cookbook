import ij.plugin.PlugIn;

public class Delta_F_down implements PlugIn {

	@Override
	public void run(final String arg) {

		final Delta delta = new Delta();
		delta.run(false);
	}
}
