package de.uni_hildesheim.sse.qmApp.treeView;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;

/**
 * A simple factory for creating {@link ConfigurableElement configurable elements} within a model.
 * 
 * @author Holger Eichelberger
 */
public interface IConfigurableElementFactory {

    /**
     * Creates a configurable element.
     * 
     * @param parent the parent configurable element
     * @param variable the variable to create the element for. This variable may be used to obtain information,
     *   but no reference on it must be kept (consider it to be temporary)
     * @param creator the deferred editor input creator pointing to <code>variable</code>
     * @return the created configurable element (must be child of <code>parent</code>)
     */
    public ConfigurableElement createElement(ConfigurableElement parent, IDecisionVariable variable, 
        IVariableEditorInputCreator creator);

}