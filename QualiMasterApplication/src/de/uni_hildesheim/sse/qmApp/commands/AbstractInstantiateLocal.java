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
import de.uni_hildesheim.sse.qmApp.model.ProjectDescriptor;
import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.qmApp.model.SessionModel;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import eu.qualimaster.easy.extension.modelop.ModelModifier;
import eu.qualimaster.easy.extension.modelop.ModelModifier.QMPlatformProvider;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.instantiation.core.Bundle;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.instantiation.core.model.vilTypes.IProjectDescriptor;
import net.ssehub.easy.producer.core.persistence.standard.StandaloneProjectDescriptor;
import net.ssehub.easy.producer.ui.productline_editor.EasyProducerDialog;
import net.ssehub.easy.producer.ui.productline_editor.EclipseConsole;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * An abstract handler for local instantiation commands. This class supports the explicit selection
 * of the entry point of the main start rule. Please note that this class also assumes that all entry points
 * have the standard VIL parameters source, config and target.
 * 
 * @author Holger Eichelberger
 * @author El-Sharkawy
 */
public abstract class AbstractInstantiateLocal extends AbstractConfigurableHandler implements QMPlatformProvider {
    /**
     * Experimental: <tt>true</tt> use copied and cleaned up configuration for instantiation, 
     * <tt>false</tt> use underlying model and configuration.
     * @see ModelModifier
     */
    //private static final boolean PRUNE_CONFIG = false;
    
    /**
     * For testing purpose: <tt>true</tt> expected behavior, <tt>false</tt> Reasoning, model pruning,
     * but no instantiation.
     */
    private static final boolean ENABLE_INSTANTIATION = true;

    private static String lastTargetLocation = Location.getInstantiationFolder().getAbsolutePath();
    
    private static boolean isEnabled = true;
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        boolean modelValid = Reasoning.reasonOn(VariabilityModel.Definition.TOP_LEVEL, false);
        if (modelValid || instantiateAlways()) {
            instantiate();
        }
        return null;
    }
        
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * Set enabled-state for the context-menus.
     * @param enabled false if the items should be disabled, true if otherwise.
     */
    public static void setEnabled(boolean enabled) {
        isEnabled = enabled;
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
            final boolean pruneConfig = !ConfigurationProperties.DISABLE_PRUNING.getBooleanValue();
            setEnabled(false);
            Job job = new Job("QualiMaster Infrastructure Instantiation Process") {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    ModelModifier modifier = null;
                    try {
                        File trgFolder = new File(targetLocation);
                        Executor executor = null;
                        if (pruneConfig) {
                            // Maybe null in case of any error
                            modifier = new ModelModifier(trgFolder,
                                VariabilityModel.Definition.TOP_LEVEL.getConfiguration().getProject(),
                                Location.getModelLocationFile(),
                                AbstractInstantiateLocal.this);
                            executor = modifier.createExecutor();
                            if (null != executor) {
                                IProjectDescriptor source = modifier.getSourceDescriptor();
                                if (null != source) {
                                    IProjectDescriptor target = new StandaloneProjectDescriptor(
                                        source.getMainVilScript(), trgFolder);
                                    executor.addTarget(target);
                                }
                            }
                        }
                        if (!pruneConfig || null == executor) {
                            ProjectDescriptor source = new ProjectDescriptor();
                            ProjectDescriptor target = new ProjectDescriptor(source, trgFolder);
                            executor = new Executor(source.getMainVilScript())
                                .addConfiguration(source.getConfiguration());
                            executor.addSource(source).addTarget(target);
                        }
                        String startRuleName = getStartRuleName();
                        if (null != startRuleName) {
                            executor.addStartRuleName(startRuleName);
                        }
                        TracerFactory.setDefaultInstance(UiTracerFactory.INSTANCE);
                        if (ENABLE_INSTANTIATION) {
                            executor.execute();
                        }
                        
                        notifyInstantiationCompleted(shell);
                    } catch (ModelManagementException e) {
                        showExceptionDialog("Model resolution problem", e);
                        EASyLoggerFactory.INSTANCE.getLogger(this.getClass(), Bundle.ID).exception(e);
                    } catch (VilException e) {
                        showExceptionDialog("Instantiation problem", e);
                        EASyLoggerFactory.INSTANCE.getLogger(this.getClass(), Bundle.ID).exception(e);
                    } finally {
                        if (pruneConfig && null != modifier) {
                            modifier.clear();
                        }
                        setEnabled(true);
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
    public void showExceptionDialog(final String title, final Exception exception) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                EasyProducerDialog.showDialog(null, title, exception.getMessage(), true);
            }
        });
    }
    
    @Override
    public void reason(Configuration config) {
        Reasoning.reasonOn(false, config);
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