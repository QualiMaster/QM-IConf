package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.AboutDialog;

/**
 * Handler which opens up the info-shell about the QualiMaster Application.
 * 
 * @author Niko Nowatzki
 */
public class AboutHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        AboutDialog dlg = new AboutDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        dlg.open();
        return null; // see API
    }
}
