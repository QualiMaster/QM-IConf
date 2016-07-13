package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import qualimasterapplication.Activator;

/**
 * This class contains util methods for dialogs that are used throughout the application.
 * 
 * @author Sass
 * 
 */
public class DialogsUtil {

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
        return display.getPrimaryMonitor().getBounds().y
                + (display.getPrimaryMonitor().getBounds().height - shell.getBounds().height) / 2;
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
    
    /**
     * Center the given dialog / shell.
     * 
     * @param shell the dialog / shell to be centered.
    */
    public static void centerShell(Shell shell) {
        //parent 
        Rectangle monitorArea = shell.getParent().getBounds();
        
        int x = monitorArea.x + (monitorArea.width - shell.getBounds().width) / 2;
        int y = monitorArea.y + (monitorArea.height - shell.getBounds().height) / 2;
        
        shell.setLocation(x, y);
    }

    /**
     * Returns the dialog settings via the activator.
     * 
     * @param settingsName the dialog settings name
     * @return the dialog settings
     */
    public static IDialogSettings getDialogSettings(String settingsName) {
        IDialogSettings root = Activator.getDefault().getDialogSettings();
        IDialogSettings settings = root.getSection(settingsName);
        if (settings == null) {
            settings = root.addNewSection(settingsName);
        }
        return settings;
    }
    
    /**
     * Returns the active shell.
     * 
     * @return the active shell
     */
    public static Shell getActiveShell() {
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        if (null == shell) {
            shell = new Shell(PlatformUI.getWorkbench().getDisplay());
        }
        return shell;
    }

}
