package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.repositoryConnector.IRepositoryConnector;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnector;

/**
 * Handler for the revert changes command.
 * 
 * @author Aike Sass
 *
 */
public class RevertModel extends AbstractHandler {
    
    private IRepositoryConnector repositoryConnector;
    
    /**
     * Creates the instantiate command.
     */
    public RevertModel() {
        if (UserContext.INSTANCE.getUsername() == null) {
            setBaseEnabled(false);
        } else {
            if (!ConfigurationProperties.DEMO_MODE.getBooleanValue()) {
                setBaseEnabled(true);
            }
        }
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Display display = PlatformUI.getWorkbench().getDisplay();
        Shell shell = new Shell(display);
        MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
        repositoryConnector = new SVNConnector();
        repositoryConnector.setRepositoryURL(ConfigurationProperties.REPOSITORY_URL.getValue());
        repositoryConnector.authenticate(UserContext.INSTANCE.getUsername(), 
                UserContext.INSTANCE.getPassword());
        dialog.setText("Info");
        if (repositoryConnector.getChangesCount(Location.getModelLocationFile(), false) == 0) {
            dialog.setMessage("Your model is up to date and no local changes were found.");
            dialog.open();
        } else {
            dialog.setMessage("In order to revert all changes the application will restart now.");
            int buttonID = dialog.open();
            switch(buttonID) {
            case SWT.OK:
                IEditorReference[] editors = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getEditorReferences();
                for (IEditorReference iEditorReference : editors) {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().closeEditor(iEditorReference.getEditor(false), true);
                }
                repositoryConnector.revert(Location.getModelLocationFile());
                PlatformUI.getWorkbench().restart();
                break;
            case SWT.CANCEL:
                break;
            default:
                break;
            }
        }
        return null;
    }

}
