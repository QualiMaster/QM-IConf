package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.ConnectDialog;

/**
 * Handler which opens up the Connect-Dialog.
 * @author Nowatzki
 */
public class ConnectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ConnectDialog dlg = new ConnectDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        dlg.open();
        return null; // see API
    }
}
