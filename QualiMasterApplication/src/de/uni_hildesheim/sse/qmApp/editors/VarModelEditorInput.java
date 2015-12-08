package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.management.VarModel;

/**
 * Wraps an IVML configuration into an editor input.
 * 
 * @author Niko Nowatzki
 */
public class VarModelEditorInput implements IEditorInput {

    private Configuration configuration;
    private boolean exists;
    private String displayName; // keep name for serialization of editors

    /**
     * Initializes the editor input from the given (projected) configuration.
     * 
     * @param configuration the configuration to derive the editor from
     * @param displayName the display name of the editor
     */
    public VarModelEditorInput(Configuration configuration, String displayName) {
        this.configuration = configuration;
        this.displayName = displayName;
        exists = null != VarModel.INSTANCE.availableModels().getModelInfo(configuration.getProject());
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public boolean exists() {
        return exists;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageDescriptor.getMissingImageDescriptor();
    }

    @Override
    public String getName() {
        return displayName;
    }

    @Override
    public IPersistableElement getPersistable() {
        return null;
    }

    @Override
    public String getToolTipText() {
        return null;
    }
    
    /**
     * Returns the configuration represented by this editor input.
     * 
     * @return the configuration
     */
    public Configuration getConfiguration() {
        return configuration;
    }


}