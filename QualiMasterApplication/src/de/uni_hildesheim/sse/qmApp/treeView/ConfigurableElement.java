package de.uni_hildesheim.sse.qmApp.treeView;

import static eu.qualimaster.easy.extension.QmConstants.SLOT_OBSERVABLE_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.graphics.Image;

import de.uni_hildesheim.sse.qmApp.editorInput.CompoundVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.ContainerVariableEditorInputChangeListener;
import de.uni_hildesheim.sse.qmApp.editorInput.ContainerVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator.CloneMode;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.ContainerVariable;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.confModel.SequenceVariable;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * Class manages a list of configurable elements which we populate in order to show these elements
 * in the {@link ConfigurableElementsView} of the QualiMaster-App.
 * Using a given configuration we populate this list with machines, specialized Hardware, pipelines.
 * Moreover this class provides methods to manipulate this list.
 * Thus it is possible to remove certain elements from the list.
 * 
 * @author Niko Nowatzki
 * @author Holger Eichelberger
 */
public class ConfigurableElement { // unsure whether this shall be a resource

    private ElementStatusIndicator status;
    private String displayName;
    private String editorId;
    private IEditorInputCreator input;
    private ConfigurableElement parent;
    private List<ConfigurableElement> children;
    private IModelPart modelPart;
    private Image image;
    private IMenuContributor menuContributor;
    private boolean isFlawed;
    
    /**
     * Constructor for a ConfigurableElement with a parent as param.
     * @param parent The elements parent.
     * @param displayName Name of the element.
     * @param editorId The editorID.
     * @param input The editor input creator.
     * @param modelPart the underlying model part
     */
    public ConfigurableElement(ConfigurableElement parent, String displayName,
        String editorId, IEditorInputCreator input, IModelPart modelPart) {
        this.parent = parent;
        this.displayName = displayName;
        this.modelPart = modelPart;
        this.editorId = editorId;
        this.input = input;
        this.status = ElementStatusIndicator.NONE;
    }
    
    /**
     * Constructor for a ConfigurableElement with a parent as param.
     * @param parent The elements parent.
     * @param displayName Name of the element (may be <b>null</b> in order to ask the <code>input</code> for its name).
     * @param editorId The editorID.
     * @param input The editor input creator.
     */
    public ConfigurableElement(ConfigurableElement parent, String displayName,
            String editorId, IEditorInputCreator input) {
        this(parent, displayName, editorId, input, parent.getModelPart());
    }
    
    /**
     * Constructor for a top-level ConfigurableElement.
     * 
     * @param displayName Name of the element.
     * @param editorId The editorID.
     * @param input The editor input creator.
     * @param modelPart the underlying model part
     */
    public ConfigurableElement(String displayName, String editorId, IEditorInputCreator input, IModelPart modelPart) {
        this(null, displayName, editorId, input, modelPart);
    }
    
    /**
     * Set the indicator for falwed configurableElements.
     * @param newValue new value tre if element is falwed, false if not.
     */
    public void setFlawedIndicator(boolean newValue) {
        isFlawed = newValue;
    }
    /**
     * Get the information about this element. True if falwed, false if not.
     * @return isFlawed true -> Element is falwed, false -> Element is not falwed.
     */
    public boolean getFlawedIndicator() {
        return isFlawed;
    }
    
    /**
     * Returns the model part displayed (full or partially) by this configurable element.
     * 
     * @return  the model part
     */
    public IModelPart getModelPart() {
        return modelPart;
    }

    
    /**
     * Tries to cast the given Object in {@link ConfigurableElement}.
     * If this is successful, the method returns the {@link ConfigurableElement}.
     * Otherwise the method returns NULL!
     * @param object Given object which will be cast to {@link ConfigurableElement}.
     * @return result The resulted {@link ConfigurableElement}. Null if object was not instance of 
     * {@link ConfigurableElement}.
     */
    public static ConfigurableElement asConfigurableElement(Object object) {
        ConfigurableElement result;

        if (object instanceof ConfigurableElement) {
            result = (ConfigurableElement) object;
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Add a child to this configurable element. The parent of
     * <code>child</code> shall be <b>this</b>.
     * 
     * @param child the child to be added
     */
    public void addChildAtTop(ConfigurableElement child) {
        assert this == child.getParent();
        if (null == children) {
            children = new ArrayList<ConfigurableElement>();
        }
        children.add(0, child);
    }
    
    /**
     * Add a child to this configurable element. The parent of
     * <code>child</code> shall be <b>this</b>.
     * 
     * @param child the child to be added
     */
    public void addChild(ConfigurableElement child) {
        assert this == child.getParent();
        if (null == children) {
            children = new ArrayList<ConfigurableElement>();
        }
        children.add(child);
    }
    
    /**
     * Creates and adds a new child for <code>variable</code> (via {@link IModelPart#getElementFactory()}).
     * This method may issue change notification messages.
     * 
     * @param source the event source
     * @param variable the variable to create the element for
     * @return the created element (may be <b>null</b>)
     */
    public ConfigurableElement addChild(Object source, IDecisionVariable variable) {
        IVariableEditorInputCreator creator = null;
        IDatatype varType = variable.getDeclaration().getType();
        if (null != variable.getParent() && variable.getParent() instanceof SequenceVariable) {
            if ("configuredParameters".equals(variable.getParent().getDeclaration().getName())) {
                addDefaultValues(variable);
            }
        }
        if (Compound.TYPE.isAssignableFrom(varType)) {
            if (variable.getParent() instanceof ContainerVariable) {
                ContainerVariable cVar = (ContainerVariable) variable.getParent();
                creator = new ContainerVariableEditorInputCreator(modelPart, cVar.getDeclaration().getName(), 
                    cVar.indexOf(variable));
            } else {
                creator = new CompoundVariableEditorInputCreator(modelPart, 
                    variable.getDeclaration().getName());                
            }
        } else if (variable.getParent() instanceof ContainerVariable) {
            ContainerVariable cont = (ContainerVariable) variable.getParent();
            int index = cont.indexOf(variable);
            if (index >= 0) {
                creator = new ContainerVariableEditorInputCreator(modelPart, 
                    variable.getParent().getDeclaration().getName(), index);
            }
        }
        
        ConfigurableElement child = null;
        if (null != creator) {
            child = modelPart.getElementFactory().createElement(this, variable, creator);
            addChild(child);
            creator.createArtifacts();
            ChangeManager.INSTANCE.variableAdded(source, variable);
        }
        return child;
    }
    
    /**
     * Adds a default name to the Observable, if not was given yet. This is neccessary for the model to load correctly
     * and will lead to crashes and freezes if ignored.
     * @param var The variable that needs a default name.
     */
    private void addDefaultValues(IDecisionVariable var) {

        IDecisionVariable type = var.getNestedElement(SLOT_OBSERVABLE_TYPE);
        if (null != type && null == type.getValue()) {
            
            String defaultName = null;
            SequenceVariable parent = null;
            if (null != var.getParent() && var.getParent() instanceof SequenceVariable) {
                parent = (SequenceVariable) var.getParent();
            }
            
            if (null != parent) {
                defaultName = parent.getDeclaration().getName() + " [" 
                    + (parent.getNestedElementsCount() - 1) + "]";
            }
            
            Value newValue;
            try {
                newValue = ValueFactory.createValue(
                        type.getDeclaration().getType(), new Object[]{defaultName});
                type.setValue(newValue, AssignmentState.ASSIGNED);
                System.out.println("Set new Observable to generated values: " + var.toString());
                System.out.println("Proof: " + var.getParent().toString());
                
            } catch (ValueDoesNotMatchTypeException e) {
                e.printStackTrace();
            } catch (ConfigurationException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    /**
     * Get the displayName.
     * @return displayName The displayName.
     */
    public String getDisplayName() {
        String result;
        if (null == displayName && null != input) {
            result = input.getName();
        } else {
            result = displayName;
        }
        if (null == result) {
            result = "";
        }
        return result;
    }
    
    /**
     * Changes the display name.
     * 
     * @param displayName the new display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the number of children of this configurable element.
     * 
     * @return the number of children (non-negative)
     */
    public int getChildCount() {
        return null == children ? 0 : children.size();
    }
    
    /**
     * Returns the specified child.
     * 
     * @param index the 0-based index of the child
     * @return the specified child
     * @throws IndexOutOfBoundsException if <code>index &lt; 0 || index &gt;={@link #getChildCount()}</code>
     */
    public ConfigurableElement getChild(int index) {
        if (null == children) {
            throw new IndexOutOfBoundsException();
        }
        return children.get(index);
    }
    
    /**
     * Returns the children as an array.
     * 
     * @return the children as an array (new instance)
     */
    public ConfigurableElement[] getChildren() {
        ConfigurableElement[] result;
        if (hasChildren()) {
            result = new ConfigurableElement[children.size()];
            children.toArray(result);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Check whether there are children.
     * @return true if there are children.
     *            false if there are no children.
     */
    public boolean hasChildren() {
        return null != children && children.size() > 0;
    }
    
    /**
     * Returns whether the selected element is a top-level element, thus, represents a model part.
     * 
     * @return <code>true</code> if the selected element is a top-level element, <code>false</code> in case of 
     *     an inner node
     */
    public boolean isTopLevel() {
        return null == parent; // || parent.input instanceof VarModelEditorInputCreator;
    }
    
    /**
     * Get the parent.
     * @return parent The parent.
     */
    public ConfigurableElement getParent() {
        return parent;
    }

    /**
     * Return the editor input creator.
     * 
     * @return input The editor input creator.
     */
    public IEditorInputCreator getEditorInputCreator() {
        return input;
    }
    
    /**
     * Returns the editor id.
     * @return the editor id.
     */
    public String getEditorId() {
        return editorId;
    }
    
    /**
     * Returns the display name of the configurable element.
     * 
     * @return the display name of the configurable element.
     */
    public String toString() {
        return getDisplayName();
    }
    
    /**
     * Deletes <code>child</code> from the children of this element.
     * 
     * @param child the child to be deleted
     * @return <code>true</code> if removed, <code>false</code> else
     */
    public boolean deleteFromChildren(ConfigurableElement child) {
        boolean done = false;
        if (null != children) {
            if (!child.isTopLevel()) {
                int index = children.indexOf(child);
                if (index >= 0) {
                    ConfigurableElement parent = child.getParent();
                    String name = parent.getDisplayName();
                    if (child.input instanceof ContainerVariableEditorInputCreator) {
                        name = ((ContainerVariableEditorInputCreator) child.input).getVariableName();
                    }
                    
//                    String name = child.input.getName();
                    ContainerVariableEditorInputChangeListener.INSTANCE.notifyDeletetion(name, index);
                }
            } else {
                int index = children.indexOf(child);
                if (index >= 0) {
                    String name = child.getDisplayName();
//                    String name = child.input.getName();
                    ContainerVariableEditorInputChangeListener.INSTANCE.notifyDeletetion(name, index);
                }
            }
            done = children.remove(child);
        }
        return done;
    }

    /**
     * Deletes this element if possible. This method may issue change events.
     * 
     * @param source the source for this call to send change events.
     */
    public void delete(Object source) {
        input.delete(source, modelPart);
        if (null != parent) {
            
            parent.deleteFromChildren(this);
        }
    }
    
    /**
     * Returns the index of the given <code>element</code> in the set of children.
     * 
     * @param element the element to search for
     * @return the child index position, <code>-1</code> if not found
     */
    public int indexOf(ConfigurableElement element) {
        return children.indexOf(element);
    }
    
    /**
     * Clones this configurable element. This method causes sending
     * changed events via {@link ChangeManager}.
     * 
     * @param source the event source (for sending events)
     * @param count the number of clones to be created
     * @return the created clones (<b>null</b> if none were created)
     */
    public List<ConfigurableElement> clone(Object source, int count) {
        List<ConfigurableElement> result = null;
        if (!isTopLevel() && isCloneable().countAllowed(count)) {
            ConfigurableElement parent = getParent();
            if (parent.isVirtualSubGroup()) {
                parent = parent.getParent();
            }
            List<IDecisionVariable> clones = input.clone(count);
            if (null != clones && !clones.isEmpty()) {
                result = new ArrayList<ConfigurableElement>();
                for (int i = 0; i < clones.size(); i++) {
                    ConfigurableElement child = parent.addChild(source, clones.get(i));
                    if (null != child) {
                        result.add(child);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns whether this element is a virtual sub-group.
     * 
     * @return <code>true</code> for a sub-group, <code>false</code> else
     */
    public boolean isVirtualSubGroup() {
        return null == getEditorInputCreator() && null != getParent();
    }
    
    /**
     * Returns whether this element is cloneable.
     * 
     * @return the clone mode
     */
    public CloneMode isCloneable() {
        return (null != input) ? input.isCloneable() : CloneMode.NONE;
    }

    /**
     * Returns whether this element is (basically) deletable. This method does not
     * determine any use of references to the underlying element.
     * 
     * @return <code>true</code> if this element is deletable, <code>false</code> else
     */
    public boolean isDeletable() {
        // top-level elements are not deletable
        return null != getParent() && null != input && input.isDeletable() && !isVirtualSubGroup(); 
    }
    
    /**
     * Returns whether this element is (basically) writable.
     * 
     * @return <code>true</code> if this element is writable, <code>false</code> else
     */
    public boolean isWritable() {
        return (null == getParent() && VariabilityModel.isWritable(modelPart)) 
            || (null != input && input.isWritable());
    }

    /**
     * Returns whether this element is (basically) readable.
     * 
     * @return <code>true</code> if this element is readable, <code>false</code> else
     */
    public boolean isReadable() {
        return (null == getParent() && VariabilityModel.isReadable(modelPart)) || isVirtualSubGroup() 
            || input.isReadable();
    }
    
    /**
     * Returns the configurable element holding the given <code>variable</code>.
     * 
     * @param variable the variable to search for
     * @return the configurable element holding <code>variable</code> or <b>null</b> if none was found
     */
    public ConfigurableElement findElement(IDecisionVariable variable) {
        ConfigurableElement result = null;
        if (null != input && input.holds(variable)) {
            result = this;
        }
        if (null != children) {
            for (int e = 0; null == result && e < children.size(); e++) {
                result = children.get(e).findElement(variable);
            }
        }
        return result;
    }
    
    /**
     * Whether the given configurable element holds the same variable.
     * 
     * @param element the element to check for (may be <b>null</b>)
     * @return <code>true</code> if same, <code>false</code> else
     */
    public boolean holdsSame(ConfigurableElement element) {
        boolean result = false;
        if (null != input) {
            IDecisionVariable iVar = input.getVariable();
            if (null != iVar && null != element && null != element.input) {
                IDecisionVariable eVar = element.input.getVariable();
                result = eVar.equals(iVar);
            }
        }
        return result;
    }
    
    /**
     * Returns whether the underlying element is referenced in the given <code>modelPart</code>.
     * 
     * @param modelPart the part to search in
     * @return <code>true</code> if the underlying element is referenced in <code>modelPart</code>, <code>false</code> 
     *   else
     */
    public boolean isReferencedIn(IModelPart modelPart) {
        if (null == modelPart) {
            modelPart = this.modelPart;
        }
        return input.isReferencedIn(modelPart, this.modelPart);
    }
    
    /**
     * Returns the image of this configurable element.
     * 
     * @return the image (may be <b>null</b>, indicates that the default [platform] image shall be used)
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * Defines the image of this configurable element.
     * 
     * @param image the image
     * @return <b>this</b> (builder pattern)
     */
    public ConfigurableElement setImage(Image image) {
        this.image = image;
        return this;
    }
    
    /**
     * Defines the menu contributor.
     * 
     * @param menuContributor the contributor, may be <b>null</b> if disabled
     */
    public void setMenuContributor(IMenuContributor menuContributor) {
        this.menuContributor = menuContributor;
    }
    
    /**
     * Asks for contributions to the related popup menu.
     * 
     * @param manager the menu manager
     */
    public void contributeToPopup(IMenuManager manager) {
        if (null != input) {
            IMenuContributor contributor = input.getMenuContributor();
            if (null != contributor) {
                contributor.contributeTo(manager);
            }
        }
        if (null != menuContributor) {
            menuContributor.contributeTo(manager);
        }
    }
    
    /**
     * Get the current dataflow information for this configurable element.
     * @return dataflow The current dataflow information for this element.
     */
    public ElementStatusIndicator getStatus() {
        return status;
    }
    
    /**
     * Set the dataflow information for this configurable element.
     * @param indicator New assigned dataflow information for this element.
     */
    public void setStatus(ElementStatusIndicator indicator) {
        
        this.status = indicator;
    }
}
