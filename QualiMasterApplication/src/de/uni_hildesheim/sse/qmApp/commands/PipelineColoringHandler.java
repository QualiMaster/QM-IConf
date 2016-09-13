package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.PipelineColoringDialog;

/**
 * Handler for opening the pipeline-coloring-dialog.
 * @author nowatzlo
 *
 */
public class PipelineColoringHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // TODO Auto-generated method stub
        PipelineColoringDialog dlg = new PipelineColoringDialog(
                PlatformUI.getWorkbench().getDisplay().getActiveShell());
        dlg.open();
        return null; // see API
    }

}
