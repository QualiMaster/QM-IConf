package de.uni_hildesheim.sse.qmApp;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.easy_producer.instantiator.Bundle;
import de.uni_hildesheim.sse.easy_producer.persistency.ResourcesMgmt;
import de.uni_hildesheim.sse.qmApp.commands.ResetModel;
import de.uni_hildesheim.sse.qmApp.dialogs.BootstrappingDialog;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.LoginDialog;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.utils.logger.AdvancedJavaLogger;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.ILogger;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage.ResultType;

/**
 * This class controls all aspects of the application's execution.
 * 
 * @author Holger Eichelberger
 */
public class Application implements IApplication, IInfrastructureListener {

    @Override
    public Object start(IApplicationContext context) {
        ResourcesMgmt.INSTANCE.enableBackgroundTasks(false);
        Display display = PlatformUI.createDisplay();
     // Commented out due to issues with new Eclipse and Java 64bit
//        P2Utils.ensureUpdateURI();
        try {
            Object toReturn = null;
            // Bootstrapping
            BootstrappingDialog.INSTANCE.init(display);
            
            // Enable repository auth
            if (authenticate(display)) {
                int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
                if (returnCode == PlatformUI.RETURN_RESTART) {
                    saveWorkspace();
                    toReturn = IApplication.EXIT_RESTART;
                } else {
                    saveWorkspace();
                    toReturn = IApplication.EXIT_OK;
                }
            }
            return toReturn;
        } finally {
            display.dispose();
        }
    }
    
    /**
     * Saves the workspace before quitting the application.
     */
    private void saveWorkspace() {
        try {
            // save the full workspace before quit
            ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
        } catch (final CoreException e) {
            EASyLoggerFactory.INSTANCE.getLogger(this.getClass(), Bundle.ID).exception(e);
            // Can we use Dialogs here or is it to early?
//            Dialogs.showErrorDialog("While saving the workspace", e.getMessage());
        }
    }

    /**
     * Calls the LoginDisplay.
     * 
     * @param display
     *            Display
     * @return .
     */
    private boolean authenticate(Display display) {
        // it's too early for the default easy logger
        ILogger oldLogger = EASyLoggerFactory.INSTANCE.setLogger(new AdvancedJavaLogger());
        boolean disableLogin = Boolean.valueOf(ConfigurationProperties.DISABLE_LOGIN.getValue());
        boolean demoMode = ConfigurationProperties.DEMO_MODE.getBooleanValue();
        if (demoMode) {
            // Make backup copy
            ResetModel.initDemoMode();
        } else {
            if (!disableLogin) {
                LoginDialog loginDialog = new LoginDialog(display);
                loginDialog.createContents();
            }
        }
        if (null != oldLogger) {
            EASyLoggerFactory.INSTANCE.setLogger(oldLogger);
        }
        return true;
    }

    @Override
    public void stop() {
        if (Infrastructure.isConnected()) {
            Infrastructure.disconnect();
        }
        if (!PlatformUI.isWorkbenchRunning()) {
            return;
        }

        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {

            public void run() {

                if (!display.isDisposed()) {
                    workbench.close();
                }
            }
        });
    }

    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg) {
        final ResultType result = msg.getResult();
        final String description = msg.getDescription();
        if (null != description && description.length() > 0) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            final Display display = workbench.getDisplay();
            display.syncExec(new Runnable() {
                public void run() {
                    if (!display.isDisposed()) {
                        Shell shell = display.getActiveShell();
                        if (ResultType.FAILED == result) {
                            Dialogs.showErrorDialog(shell, "Command execution failed", description);
                        } else {
                            Dialogs.showInfoDialog(shell, "Command execution succeeded", description);
                        }
                    }
                }
            });
        }
    }

}
