package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.qmApp.ApplicationWorkbenchAdvisor;

/**
 * The handler for switching to the initial perspective.
 * 
 * @author Holger Eichelberger
 */
public class SwitchToInitialPerspective extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IWorkbench workbench = Activator.getDefault().getWorkbench();
        if (null != workbench) {
            IPerspectiveRegistry registry = workbench.getPerspectiveRegistry();
            IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
            if (null != window && null != registry) {
                window.getActivePage().setPerspective(registry.findPerspectiveWithId(
                    ApplicationWorkbenchAdvisor.PERSPECTIVE_ID));
            }
        }
        return null;
    }

}
