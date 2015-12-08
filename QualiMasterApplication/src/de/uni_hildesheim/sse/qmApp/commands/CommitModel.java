package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.repositoryConnector.IRepositoryConnector;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnector;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.RepositoryEventHandler;

/**
 * The handler for the "commit model" command.
 * 
 * @author Holger Eichelberger, Aike Sass
 */
public class CommitModel extends AbstractHandler {

    private IRepositoryConnector repositoryConnector;

    private ProgressIndicator progress;

    private Display display;

    private Shell shell;

    /**
     * Creates the instantiate command.
     */
    public CommitModel() {
        repositoryConnector = new SVNConnector();
        if (UserContext.INSTANCE.getUsername() == null) {
            setBaseEnabled(false);
        } else {
            repositoryConnector.setRepositoryURL(ConfigurationProperties.REPOSITORY_URL.getValue());
            repositoryConnector.authenticate(UserContext.INSTANCE.getUsername(), UserContext.INSTANCE.getPassword());
            setBaseEnabled(true);
        }
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        if (Reasoning.reasonOn(VariabilityModel.Definition.TOP_LEVEL, false)) {
            new Thread(new ModelUpdate()).start();
        }
        return null;
    }

    /**
     * Implements the model update running in parallel to the user interface so that user interface updates can happen.
     * 
     * @author Sass
     */
    private class ModelUpdate implements Runnable, RepositoryEventHandler {

        /**
         * Creates a model updater.
         */
        private ModelUpdate() {
            repositoryConnector.setCommitEventHandler(this);
            display = PlatformUI.getWorkbench().getDisplay();
            shell = new Shell(display);
            shell.setLayout(new GridLayout());
            shell.setSize(300, 100);
            shell.setText("Commiting");
            shell.setLocation(DialogsUtil.getXPosition(shell, display), DialogsUtil.getYPosition(shell, display));
            progress = new ProgressIndicator(shell, SWT.HORIZONTAL);
            progress.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
            if (repositoryConnector.getChangesCount(Location.getModelLocationFile(), false) > 0) {
                shell.open();
            } else {
                MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
                dialog.setText("Info");
                dialog.setMessage("There are no changes in your workspace.");
                dialog.open();
            }
        }

        @Override
        public void progress(final double status) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    progress.worked(status);
                }
            });
        }

        @Override
        public void completed() {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    progress.done();
                    shell.dispose();
                }
            });
        }

        @Override
        public void run() {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    progress.beginTask(repositoryConnector.getChangesCount(Location.getModelLocationFile(), false));
                }
            });
            boolean conflicts = repositoryConnector.storeModel(Utils.getDestinationFileForModel());
            if (conflicts) {
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        repositoryConnector.updateModel(Utils.getDestinationFileForModel());
                        repositoryConnector.resolveConflicts(Utils.getDestinationFileForModel(), false);
                        MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK );
                        dialog.setText("Info");
                        dialog.setMessage("There were some conflicts. Local changes were overwritten.");
                        int messageBox = dialog.open();
                        switch (messageBox) {
                        case SWT.OK:
                            completed();
                            break;
                        default:
                            // TODO
                            break;
                        }
                    }
                });
            }
        }

    }

}
