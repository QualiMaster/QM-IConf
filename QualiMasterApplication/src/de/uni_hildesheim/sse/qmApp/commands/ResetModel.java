package de.uni_hildesheim.sse.qmApp.commands;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.repositoryConnector.Bundle;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * The handler for the reset model command.
 * 
 * @author Sass
 * 
 */
public class ResetModel extends AbstractHandler {
    
    /**
     * Folder where the model is stored. Needs to be in a sub-directory of the workspace to hide it for eclipse.
     */
    private static final String MODEL_DIR_NAME = "backup/ConfModel";
    
    private static IWorkspace workspace = ResourcesPlugin.getWorkspace();
    
    private static File workspaceFolder = workspace.getRoot().getLocation().toFile();

    private static File srcDir = new File(workspaceFolder, MODEL_DIR_NAME);
    
    private static File destDir = new File(workspaceFolder, srcDir.getName());
    
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(ResetModel.class, Bundle.ID);
    
    /**
     * Creates the instantiate command.
     */
    public ResetModel() {
        setBaseEnabled(ConfigurationProperties.DEMO_MODE.getBooleanValue());
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            FileUtils.deleteQuietly(Utils.getDestinationFileForModel());
            FileUtils.copyDirectoryToDirectory(srcDir, workspaceFolder);
        } catch (IOException e) {
            logger.exception(e);
        }
        PlatformUI.getWorkbench().restart();
        return null;
    }
    
    /**
     * Initiates the demo mode. If the model is not found in the expected folder than it will be copied into the folder.
     */
    public static void initDemoMode() {
        Location.setModelLocation(destDir.getAbsolutePath());
        // Initial Setup. Model will be copied into the workspace. Model needs to be on top hierarchies of the workspace
        if (!destDir.exists()) {
            try {
                FileUtils.copyDirectoryToDirectory(srcDir, workspaceFolder);
            } catch (IOException e) {
                logger.exception(e);
            }
        }
    }

}
