package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

/**
 * The help-command implementation of the QM-IConf documentation.
 * This simple command opens the Eclipse help-system with the QM-IConf help
 * main page.
 * 
 * @author kroeher
 */
public class HelpCommand  extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (PlatformUI.getWorkbench().getHelpSystem().hasHelpUI()) {
            PlatformUI.getWorkbench().getHelpSystem()
                .displayHelpResource("/QualiMasterApplication/help/main_page.html");
        }
        return null;
    }
}
