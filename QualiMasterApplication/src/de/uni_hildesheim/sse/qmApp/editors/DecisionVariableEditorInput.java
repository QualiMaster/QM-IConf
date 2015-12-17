package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;

/**
 * Wraps a configured IVML variable into an editor input.
 * 
 * @author Holger Eichelberger
 */
public class DecisionVariableEditorInput implements IEditorInput, IPersistableElement {

    private static final String CREATOR_FACTORY_ID = "creatorId";
    private IDecisionVariable variable;
    private IVariableEditorInputCreator creator;

    /**
     * Initializes the editor input from the given variable.
     * 
     * @param variable the variable to initialize the input from
     * @param creator the variable creator used to persist the input
     */
    public DecisionVariableEditorInput(IDecisionVariable variable, IVariableEditorInputCreator creator) {
        this.variable = variable;
        this.creator = creator;
    }
    
    /**
     * Creates a decision variable editor input form a memento.
     * 
     * @param memento the memento to create the input from
     */
    public DecisionVariableEditorInput(IMemento memento) {
        String factoryId = memento.getString(CREATOR_FACTORY_ID);
        IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryId);
        if (null != factory) {
            IAdaptable tmp = factory.createElement(memento);
            if (tmp instanceof IVariableEditorInputCreator) {
                creator = (IVariableEditorInputCreator) tmp;
                variable = creator.getVariable();
            }
        }
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Object getAdapter(Class adapter) {
        return null;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public ImageDescriptor getImageDescriptor() {
        ImageDescriptor result =  ImageDescriptor.getMissingImageDescriptor();
        Configuration cfg = variable.getConfiguration();
        IDatatype type = variable.getDeclaration().getType();
        String imgKey = ImageRegistry.obtainKey(cfg.getName(), type);
        Image img = ImageRegistry.INSTANCE.getImage(imgKey);
        if (null != img) {
            result = ImageDescriptor.createFromImage(img);
        }
        return result;
    }

    @Override
    public String getName() {
        return ModelAccess.getDisplayName(variable);
    }

    @Override
    public IPersistableElement getPersistable() {
        return this;
    }

    @Override
    public String getToolTipText() {
        return null;
    }
    
    /**
     * Returns the variable represented by this editor input.
     * 
     * @return the variable
     */
    public IDecisionVariable getVariable() {
        return variable;
    }

    @Override
    public void saveState(IMemento memento) {
        if (null != creator) {
            memento.putString(CREATOR_FACTORY_ID, creator.getFactoryId());
            creator.saveState(memento);
        }
    }

    @Override
    public String getFactoryId() {
        return DecisionVariableEditorInputFactory.ID;
    }
    
}