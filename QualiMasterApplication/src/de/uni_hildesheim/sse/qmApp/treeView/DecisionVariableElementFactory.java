package de.uni_hildesheim.sse.qmApp.treeView;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editors.VariableEditor;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;

/**
 * A default decision variable element factory using {@link ConfigurableElement#getDisplayName(IDecisionVariable)}
 * for the display name and {@link DecisionVariableEditorInput} as editor input.
 * 
 * @author Holger Eichelberger
 */
public class DecisionVariableElementFactory implements IConfigurableElementFactory {

    public static final IConfigurableElementFactory VARIABLE_EDITOR 
        = new DecisionVariableElementFactory(VariableEditor.ID);
    
    private String elementEditorId;

    /**
     * Creates a new factory instance using the given element editor id.
     * 
     * @param elementEditorId the editor id to be used
     */
    public DecisionVariableElementFactory(String elementEditorId) {
        this.elementEditorId = elementEditorId;
    }

    @Override
    public ConfigurableElement createElement(ConfigurableElement parent, IDecisionVariable variable, 
        IVariableEditorInputCreator creator) {
        ConfigurableElement result = new ConfigurableElement(parent, null, elementEditorId, creator);
        result.setImage(ImageRegistry.INSTANCE.getImage(parent.getModelPart(), variable.getDeclaration().getType()));
        return result;
    }
    
}