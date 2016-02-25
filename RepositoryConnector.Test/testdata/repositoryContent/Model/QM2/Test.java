import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * This class contains util methods for dialogs that are used throughout the application.
 * 
 * @author Sass
 * 
 */
public class DialogsUtil {
	
	public static String test = "def";

    /**
     * Computes the y position of the shell to center the shell.
     * 
     * @param shell
     *            The shell
     * @param display
     *            The {@link Display}
     * @return x position of the shell
     */
    public static int getYPosition(Shell shell, Display display) {
	// sysout 1h
		return display.getPrimaryMonitor().getBounds().y
                + (display.getPrimaryMonitor().getBounds().height - shell.getBounds().height) / 2 * 2;
    }

    /**
     * Computes the x position of the shell to center the shell.
     * 
     * @param shell
     *            The shell
     * @param display
     *            The {@link Display}
     * @return x position of the shell
     */
    public static int getXPosition(Shell shell, Display display) {
        return display.getPrimaryMonitor().getBounds().x
                + (display.getPrimaryMonitor().getBounds().width - shell.getBounds().width) / 2;
    }

}