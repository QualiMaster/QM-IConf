package de.uni_hildesheim.sse.qmApp.commands;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.UiTracerFactory;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.ModelModifier;
import de.uni_hildesheim.sse.qmApp.model.ProjectDescriptor;
import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.qmApp.model.SessionModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.producer.ui.productline_editor.EclipseConsole;

/**
 * An abstract handler for local instantiation commands. This class supports the explicit selection
 * of the entry point of the main start rule. Please note that this class also assumes that all entry points
 * have the standard VIL parameters source, config and target.
 * 
 * @author Holger Eichelberger
 * @author El-Sharkawy
 */
public abstract class AbstractInstantiateLocal extends AbstractConfigurableHandler {
    /**
     * Experimental: <tt>true</tt> use copied and cleaned up configuration for instantiation, 
     * <tt>false</tt> use underlying model and configuration.
     */
    private static final boolean PRUNE_CONFIG = false;

    private static String lastTargetLocation = Location.getModelLocation();
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        boolean modelValid = Reasoning.reasonOn(VariabilityModel.Definition.TOP_LEVEL, false);
        if (modelValid || instantiateAlways()) {
            instantiate();
        }
        return null;
    }
        
    /**
     * Implements the actual instantiation by calling VIL.
     */
    protected void instantiate() {        
        EclipseConsole.INSTANCE.clearConsole();
        EclipseConsole.INSTANCE.displayConsole();
        final String targetLocation = selectTargetFolder(getMessage());
        SessionModel.INSTANCE.setInstantiationFolder(targetLocation);
        final Shell shell = Dialogs.getDefaultShell(); 
        if (null != targetLocation) {
            Job job = new Job("QualiMaster Infrastructure Instantiation Process") {
                
                @SuppressWarnings("unused")
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        File trgFolder = new File(targetLocation);
                        ProjectDescriptor source = new ProjectDescriptor();
                        ProjectDescriptor target = new ProjectDescriptor(source, trgFolder);
                        Executor executor = null;
                        ModelModifier modifier = null;
                        if (PRUNE_CONFIG) {
                            // Maybe null in case of any error
                            modifier = new ModelModifier(trgFolder);
                            executor = modifier.createExecutor();
                        }
                        if (!PRUNE_CONFIG || null == executor) {
                            executor = new Executor(source.getMainVilScript())
                                .addConfiguration(source.getConfiguration());
                        }
                        executor.addSource(source).addTarget(target);
                        String startRuleName = getStartRuleName();
                        if (null != startRuleName) {
                            executor.addStartRuleName(startRuleName);
                        }
                        TracerFactory.setDefaultInstance(UiTracerFactory.INSTANCE);
                        //executor.execute();
                        
                        if (PRUNE_CONFIG && null != modifier) {
                            modifier.clear();
                        }
                        
                        notifyInstantiationCompleted(shell);
                    } catch (ModelManagementException e) {
                        showExceptionDialog("Model resolution problem", e);
//                    } catch (VilException e) {
//                        showExceptionDialog("Instantiation problem", e);
                    }
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                }
            };
            job.schedule();
        }
    }
    
    /**
     * Notifies about a completed instantiation.
     * 
     * @param shell the parent shell
     */
    private static void notifyInstantiationCompleted(final Shell shell) {
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                Dialogs.showInfoDialog(shell, "Platform instantiation", 
                    "Platform instantiation completed successfully");
            }
            
        });
        
    }
    
    /**
     * Allows selecting the target folder including checks for existence or whether it is empty.
     * 
     * @param message the message to be displayed in the directory selection dialog
     * @return the target folder or <b>null</b> if none was selected
     */
    private static String selectTargetFolder(String message) {
        DirectoryDialog dialog = new DirectoryDialog(Dialogs.getDefaultShell());
        dialog.setFilterPath(lastTargetLocation);
        dialog.setMessage(message);
        
        String targetLocation;
        while (true) {
            targetLocation = dialog.open();
            if (null != targetLocation) {
                lastTargetLocation = targetLocation;
                File file = new File(targetLocation);
                if (file.exists()) {
                    File[] contents = file.listFiles();
                    if (null != contents && contents.length > 0) {
                        String absTargetLoc = file.getAbsolutePath();
                        int dlgRes = Dialogs.showInfoConfirmDialog("Folder exists: " + absTargetLoc, "The selected "
                            + "target folder\n\n" + absTargetLoc + "\n\nexists "
                            + "and is not empty. Instantiation may overwrite files, so it is recommended to clear the "
                            + "folder before. Shall \n\n" + absTargetLoc + "\n\n be cleared, i.e., all files and "
                            + "folders in this folder will be deleted?");
                        if (SWT.YES == dlgRes) {
                            try {
                                FileUtils.cleanDirectory(file);
                                break; // target location is ok
                            } catch (IOException e) {
                                Dialogs.showErrorDialog("Clearing folder failed", e.getMessage());
                                // requires a new location
                            }
                        }
                    } else {
                        if (file.canRead() && file.canWrite()) {
                            break; // target location is ok
                        } else {
                            Dialogs.showErrorDialog("Cannot access folder", "Please grant read/write access to the "
                                + "selected traget folder or select a different folder.");
                        }
                    }
                } else {
                    Dialogs.showErrorDialog("Folder does not exist", "Selected folder does not exist.");
                }
            } else {
                break; // nothing selected, don't continue
            }
        }
        return targetLocation;
    }

    /**
     * Shows an exception dialog in the user interface thread.
     * 
     * @param title the dialog title
     * @param exception the causing exception
     */
    private static void showExceptionDialog(final String title, final Exception exception) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Dialogs.showErrorDialog(title, exception.getMessage());
            }
        });
    }

    /**
     * Returns the start rule name. 
     * 
     * @return the start rule name (may be <b>null</b> if "main" shall be used)
     */
    protected abstract String getStartRuleName();
    
    /**
     * Returns the display message.
     * 
     * @return the display message
     */
    protected abstract String getMessage();
    
    /**
     * Whether instantiation shall consider failed reasoning or not.
     * 
     * @return <code>true</code> if reasoning result shall be considered, <code>false</code> else
     */
    protected boolean instantiateAlways() {
        return false;
    }

}
