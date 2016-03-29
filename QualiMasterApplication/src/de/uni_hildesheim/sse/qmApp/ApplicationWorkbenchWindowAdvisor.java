package de.uni_hildesheim.sse.qmApp;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import net.ssehub.easy.dslCore.EclipseResourceInitializer;

/**
 * Public class for configuring the window of the Qualimaster-App.
 * 
 * @author Holger Eichelberger
 */
public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    /**
     * Calls super constructor thus sets the given
     * {@link IWorkbenchWindowConfigurer} for this application.
     * 
     * @param configurer
     *            The given {@link IWorkbenchWindowConfigurer}.
     */
    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    /**
     * Creates {@link ApplicationActionBarAdvisor} with given
     * {@link IActionBarConfigurer} configurer.
     * 
     * @param configurer
     *            Given {@link IActionBarConfigurer} configurer.
     * @return The {@link ApplicationActionBarAdvisor} for the given configurer.
     */
    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }

    @Override
    public void preWindowOpen() {
        ModelUtility.setResourceInitializer(new EclipseResourceInitializer());
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(600, 400));
        configurer.setShowCoolBar(false);
        configurer.setShowStatusLine(true);
        configurer.setTitle("QualiMaster Infastructure Configuration Tool (QM-IConf) "
                + UserContext.INSTANCE.getRoles().toString());
        
        try {
            WorkspaceUtils.enableAutoBuild(false); // disable Java builds
        } catch (CoreException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void postWindowOpen() {
        getWindowConfigurer().getWindow().getShell().setMaximized(true);
        // Add QualiMaster-icon to the shell.
        getWindowConfigurer().getWindow().getShell().setImage(IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL));
        getWindowConfigurer().getWindow().getShell().addShellListener(new ShellListener() {

            @Override
            public void shellIconified(ShellEvent event) {
            }

            @Override
            public void shellDeiconified(ShellEvent event) {
            }

            @Override
            public void shellDeactivated(ShellEvent event) {
            }

            @Override
            public void shellClosed(ShellEvent event) {
                IWorkspace ws = ResourcesPlugin.getWorkspace();
                try {
                    ws.save(true, new NullProgressMonitor());
                } catch (CoreException e) {
                    Dialogs.showErrorDialog("While saving the workspace", e.getMessage());
                }
            }

            @Override
            public void shellActivated(ShellEvent event) {
            }
        });
    }

    @Override
    public boolean preWindowShellClose() {
        try {
            // save the full workspace before quit
            ResourcesPlugin.getWorkspace().save(true, new NullProgressMonitor());
        } catch (final CoreException e) {
            Dialogs.showErrorDialog("While saving the workspace", e.getMessage());
        }
        return true;
    }

}
