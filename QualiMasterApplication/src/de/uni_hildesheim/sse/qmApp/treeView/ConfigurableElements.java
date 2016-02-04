package de.uni_hildesheim.sse.qmApp.treeView;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.editorInput.CompoundVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.ContainerVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.VarModelEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;

/**
 * Collects the set of {@link ConfigurableElement configurable elements} and provides
 * operations to create the set.
 * 
 * @author Holger Eichelberger
 */
public class ConfigurableElements {

    private List<ConfigurableElement> elements = new ArrayList<ConfigurableElement>();

    /**
     * An element referrer, which may create a subgrouping of configurable elements.
     * 
     * @author Holger Eichelberger
     */
    public interface IElementReferrer {

        /**
         * Returns the sub grouping model part.
         * 
         * @return the model part
         */
        public IModelPart getSubModelPart();
        
        /**
         * Turns the contents of <code>var</code> into configurable elements below <code>parent</code>.
         * 
         * @param var the variable to retrieve the elements from
         * @param parent the parent configurable element
         */
        public void variableToConfigurableElements(IDecisionVariable var, ConfigurableElement parent);

        /**
         * Returns the actual parent of the element <code>name</code> to be inserted.
         * 
         * @param name the name of the element
         * @param parent the current parent
         * @return <code>parent</code> or the actual parent
         */
        public ConfigurableElement getActualParent(String name, ConfigurableElement parent);
        
    }
       
    /**
     * Turns a <code>modelPart</code> into a sub-hierarchy of configurable elements.
     * The display name is taken form {@link QualiMasterDisplayNameProvider}.
     * 
     * @param modelPart the model part to iterate over
     * @param modelEditorId the editor id for the entire model
     * @return the parent element created for <code>modelName</code>
     */
    public ConfigurableElement variableToConfigurableElements(IModelPart modelPart, String modelEditorId) {
        return variableToConfigurableElements(modelPart, modelEditorId, null);
    }
    
    /**
     * Turns a <code>modelPart</code> into a sub-hierarchy of configurable elements.
     * The display name is taken form {@link QualiMasterDisplayNameProvider}.
     * 
     * @param modelPart the model part to iterate over
     * @param modelEditorId the editor id for the entire model
     * @param referrer an instance which leads to a subgrouping according to <code>modelPart</code>, but contained
     *   elements based on the elements returned by the referrer
     * @return the parent element created for <code>modelName</code>
     */
    public ConfigurableElement variableToConfigurableElements(IModelPart modelPart, String modelEditorId, 
        IElementReferrer referrer) {
        ConfigurableElement element = null;

        boolean readable = VariabilityModel.isReadable(modelPart); 
        IModelPart headPart = modelPart;
        if (null != referrer) {
            headPart = referrer.getSubModelPart();
            readable &= VariabilityModel.isReadable(headPart);
        }
        if (readable) {
            List<AbstractVariable> decls = modelPart.getPossibleValues();
            Configuration cfg = modelPart.getConfiguration();
            for (AbstractVariable decl : decls) {
                if (null == element) {
                    element = createParent(headPart, modelEditorId);
                }
                IDecisionVariable var = cfg.getDecision(decl);
                if (null != var) {
                    variableToConfigurableElements(modelPart, decl.getName(), var, element, 
                        modelPart.getElementFactory(), referrer);
                }
            }
        }
        
        // ensure that there is an element
        if (null == element) {
            element = createParent(headPart, modelEditorId);
        }
        elements.add(element);
        
        return element;
    }
    
    /**
     * Turns a list of <code>variables</code> into configurable elements.
     * 
     * @param modelPart the model part
     * @param modelEditorId the editor id
     * @param parent the parent element to add the elements below 
     * @param variables the variables to add
     * @return <code>parent</code> or the created parent element
     */
    public ConfigurableElement variableToConfigurableElements(IModelPart modelPart, String modelEditorId, 
        ConfigurableElement parent, List<IDecisionVariable> variables) {
        if (null != variables && variables.size() > 0) {
            if (null == parent) {
                parent = createParent(modelPart, modelEditorId);
            }
            for (int v = 0; v < variables.size(); v++) {
                IDecisionVariable var = variables.get(v);
                AbstractVariable decl = var.getDeclaration();
                variableToConfigurableElements(modelPart, decl.getName(), var, parent, 
                    modelPart.getElementFactory(), null);
            }
        }
        return parent;
    }

    /**
     * Creates a parent element.
     * 
     * @param modelPart the model part
     * @param modelEditorId the editor id
     * @return the configurable element
     */
    private ConfigurableElement createParent(IModelPart modelPart, String modelEditorId) {
        String displayName = QualiMasterDisplayNameProvider.INSTANCE.getModelPartDisplayName(modelPart);
        ConfigurableElement element = new ConfigurableElement(displayName, modelEditorId, 
                new VarModelEditorInputCreator(modelPart, displayName), modelPart);
        element.setImage(ImageRegistry.INSTANCE.getImage(modelPart));
        return element;
    }

    /**
     * Turns a variable into a sub-hierarchy of configurable elements below <code>elt</code>.
     * 
     * @param modelPart the model part to iterate over
     * @param variableName the name of the variable within <code>model</code> to start the iteration
     * @param elt the parent configurable element to attach the sub-hierarchy to
     * @param factory the factory for the individual elements
     * @return the parent element created for <code>modelName</code>
     */
    public ConfigurableElement variableToConfigurableElements(IModelPart modelPart, String variableName, 
        ConfigurableElement elt, IConfigurableElementFactory factory) {
        IDecisionVariable var = ModelAccess.obtainVariable(modelPart, variableName);
        return variableToConfigurableElements(modelPart, variableName, var, elt, factory, null);
    }
    
    // checkstyle: stop parameter number check
    
    /**
     * Turns a variable into a sub-hierarchy of configurable elements below <code>elt</code>.
     *
     * @param modelPart the model part to create the configurable elements for
     * @param varName the variable name to create the configurable elements for
     * @param var the variable to start the iteration
     * @param elt the parent configurable element to attach the sub-hierarchy to
     * @param factory the factory for the individual elements
     * @param referrer an instance which leads to a subgrouping according to <code>modelPart</code>, but contained
     *   elements based on the elements returned by the referrer
     * @return the parent element created for <code>modelName</code>
     */
    public static ConfigurableElement variableToConfigurableElements(IModelPart modelPart, String varName, 
        IDecisionVariable var, ConfigurableElement elt, IConfigurableElementFactory factory, 
        IElementReferrer referrer) {
        if (var instanceof ContainerVariable) {
            //Fill create the configurable elements and relate the editor/editor input to them
            for (int i = 0; i < var.getNestedElementsCount(); i++) {
                ContainerVariableEditorInputCreator creator 
                    = new ContainerVariableEditorInputCreator(modelPart, varName, i);
                IDecisionVariable nested = creator.getVariable();
                ConfigurableElement nestedElement = createElement(elt, nested, factory, creator, referrer);
                //ConfigurableElement nestedElement = factory.createElement(elt, nested, creator);
                //elt.addChild(nestedElement);
                if (null != referrer) {
                    referrer.variableToConfigurableElements(nested, nestedElement);
                }
            }
        } else if (var instanceof CompoundVariable) {
            //elt.addChild(factory.createElement(elt, var, new CompoundVariableEditorInputCreator(modelPart, varName)));
            createElement(elt, var, factory, new CompoundVariableEditorInputCreator(modelPart, varName), referrer);
        }
        return elt;
    }

    // checkstyle: resume parameter number check
    
    /**
     * Creates a configurable element.
     * 
     * @param parent the parent element
     * @param var the decision variable
     * @param factory the element factory
     * @param creator the editor input creator
     * @param referrer the optional referrer for sub-grouping (may be <b>null</b>)
     * @return the created element
     */
    public static ConfigurableElement createElement(ConfigurableElement parent, IDecisionVariable var, 
        IConfigurableElementFactory factory, IVariableEditorInputCreator creator, IElementReferrer referrer) {
        if (null != referrer) {
            String displayName = QualiMasterDisplayNameProvider.INSTANCE.getDisplayName(var);
            parent = referrer.getActualParent(displayName, parent);
        }
        ConfigurableElement nestedElement = factory.createElement(parent, var, creator);
        parent.addChild(nestedElement);
        return nestedElement;
    }
    
    /**
     * Get the elements.
     * @return result Array containing elements.
     */
    public ConfigurableElement[] elements() {
        ConfigurableElement[] result = new ConfigurableElement[elements.size()];
        elements.toArray(result);
        return result;
    }
    
    /**
     * Clears all elements.
     */
    public void clear() {
        elements.clear();
    }
    
    /**
     * Creates and adds a configuration element from the provided data.
     * 
     * @param displayName the display name
     * @param editorId the editor identification
     * @param eInput the editor input creator
     * @param modelPart the underlying model part
     * @return the created element
     */
    public ConfigurableElement addElement(String displayName, String editorId, IEditorInputCreator eInput, 
        IModelPart modelPart) {
        ConfigurableElement elt = new ConfigurableElement(displayName, editorId, eInput, modelPart);
        IDatatype type = eInput.getType();
        if (null != modelPart) {
            elt.setImage(ImageRegistry.INSTANCE.getImage(modelPart, type));
        }
        elements.add(elt);
        return elt;
    }

    /**
     * Returns the configurable element holding the given <code>variable</code>.
     * 
     * @param variable the variable to search for
     * @return the configurable element holding <code>variable</code> or <b>null</b> if none was found
     */
    public ConfigurableElement findElement(IDecisionVariable variable) {
        ConfigurableElement result = null;
        for (int e = 0; null == result && e < elements.size(); e++) {
            result = elements.get(e).findElement(variable);
        }
        return result;
    }

}
