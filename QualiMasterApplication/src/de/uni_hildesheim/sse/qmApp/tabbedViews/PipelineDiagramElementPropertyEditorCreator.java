package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.easy.ui.productline_editor.DelegatingEasyEditorPage;
import de.uni_hildesheim.sse.easy.ui.productline_editor.IUpdateListener;
import de.uni_hildesheim.sse.easy.ui.productline_editor.IUpdateProvider;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.IvmlKeyWords;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.qmApp.editors.EditorUtils;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.IChangeListener;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * Implements a generic property editor creator for pipeline diagram elements.
 * This class may be even more generic. We defer this to later...
 * 
 * @author Holger Eichelberger
 */
public class PipelineDiagramElementPropertyEditorCreator implements IPropertyEditorCreator {

    private static final String IMPL_SUFFIX = "Impl";
    
    private Class<?> reactsOn;
    private IModelPart modelPart = VariabilityModel.Configuration.PIPELINES;
    private LabelProviderCreator labelCreator;

    /**
     * Creates a new pipeline diagram node property editor creator.
     * 
     * @param reactsOn the actual node class it reacts on
     */
    public PipelineDiagramElementPropertyEditorCreator(Class<?> reactsOn) {
        this.reactsOn = reactsOn;
        labelCreator = new LabelProviderCreator(modelPart);
    }
    
    @Override
    public Class<?> reactsOn() {
        return reactsOn;
    }
    
    /**
     * Returns the underlying model part.
     * 
     * @return the underlying model part
     */
    protected IModelPart getModelPart() {
        return modelPart;
    }
    
    /**
     * Returns the underlying configuration.
     * 
     * @return the underlying configuration
     */
    protected Configuration getConfiguration() {
        return getModelPart().getConfiguration();
    }
    
    /**
     * Returns the underlying variability model.
     * 
     * @return the underlying variability model
     */
    protected Project getVarModel() {
        return getConfiguration().getProject();
    }
    
    /**
     * Returns the compound name matching the underlying node class.
     * Assumption: node classes end with Impl and directly correspond to the IVML model
     * 
     * @return the IVML compound name
     */
    protected String getCompoundName() {
        String name = reactsOn().getSimpleName();
        final int suffixLen = IMPL_SUFFIX.length();
        if (name.endsWith(IMPL_SUFFIX) && name.length() > suffixLen) {
            name = name.substring(0, name.length() - suffixLen);
        } else {
            name = null;
        }
        return name;
    }
    
    /**
     * Returns the variable name for a given slot name.
     * 
     * @param slotName the slot name
     * @return the (unqualified) variable name
     */
    protected String getVariableName(String slotName) {
        return getCompoundName() + IvmlKeyWords.COMPOUND_ACCESS + slotName;
    }
    
    /**
     * Normalizes the identifier for Java conventions (if required).
     * 
     * @param propertyIdentifier the identifier
     * @return the normalized identifier (or <code>propertyIdentifier</code>)
     */
    protected String normalizeIdentifier(String propertyIdentifier) {
        String name = propertyIdentifier;
        // try first with Java convention
        char start = name.charAt(0);
        if (Character.isUpperCase(start)) {
            String tmp = String.valueOf(Character.toLowerCase(start));
            if (name.length() > 1) {
                name = tmp + name.substring(1);
            } else {
                name = tmp;
            }
        }
        return name;
    }
    
    /**
     * Returns the compound type associated with this creator.
     * 
     * @return the compound type
     */
    protected Compound getCompound() {
        Compound result = null;
        try {
            result = (Compound) ModelQuery.findElementByName(getVarModel(), getCompoundName(), Compound.class);
        } catch (ModelQueryException e) {
            // not there
        }
        return result;
    }
    
    /**
     * Returns the variable declaration for <code>propertyIdentifier</code>.
     * 
     * @param propertyIdentifier the property/slot identifier
     * @return the variable declaration (may be <b>null</b> if not found)
     */
    protected DecisionVariableDeclaration getVariableDeclaration(String propertyIdentifier) {
        DecisionVariableDeclaration result = null;
        if (propertyIdentifier.length() > 0) {
            // try first with Java convention
            Compound compound = getCompound();
            if (null != compound) {
                String name = normalizeIdentifier(propertyIdentifier);
                result = compound.getElement(name);
                if (null == result && !name.equals(propertyIdentifier)) {
                    // just as fallback
                    result = compound.getElement(propertyIdentifier);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns the slot of the given compound.
     * 
     * @param compound the compound
     * @param propertyIdentifier the name of the slot
     * @return the slot or <b>null</b>
     */
    protected IDecisionVariable getSlot(CompoundVariable compound, String propertyIdentifier) {
        IDecisionVariable result = null;
        if (null != compound && propertyIdentifier.length() > 0) {
            String name = normalizeIdentifier(propertyIdentifier);
            result = compound.getNestedVariable(name);
            if (null == result && !name.equals(propertyIdentifier)) {
                // try again with original name if original name is not Java convention name
                result = compound.getNestedVariable(name);
            }
        }
        return result;
    }

    @Override
    public String getDisplayName(Object data, String propertyIdentifier) {
        String result = null;
        DecisionVariableDeclaration decl = getVariableDeclaration(propertyIdentifier);
        if (null != decl) {
            result = ModelAccess.getLabelName(decl);
        }
        return result;
    }
    
    @Override
    public String getDescription(Object data, String propertyIdentifier) {
        String result = null;
        DecisionVariableDeclaration decl = getVariableDeclaration(propertyIdentifier);
        if (null != decl) {
            result = ModelAccess.getHelpText(decl);
        }
        return result;
    }
    
    @Override
    public CellEditor createPropertyEditor(Composite composite, Object data, String propertyIdentifier, 
        IFallbackEditorCreator fallback) {
        CellEditor result = null;
        Compound compound = getCompound();
        DecisionVariableDeclaration slot = getVariableDeclaration(propertyIdentifier);
        if (null != compound && null != slot) {
            // find top-level type to exclude references
            Compound topCompoundType = compound.getRefinementBasis();
            IDatatype slotType = slot.getType();
            if (Reference.isReferenceTo(slotType, topCompoundType)) {
                // leave this to the graphical editor
                result = null; // fallback.createFallbackPropertyEditor(composite);
            } else {
                // problem: do not modify the underlying configuration, only when the diagram is saved
                // solution: create a temporary variability model with just one matching variable
                // data-propertyidentifier
                Project tmpModel = new Project(compound.getTopLevelParent().getName());
                tmpModel.addImport(new ProjectImport(getVarModel().getName(), null));
                DecisionVariableDeclaration tmpDecl = new DecisionVariableDeclaration(
                    "tmp", compound.getType(), tmpModel);
                tmpDecl.setComment(compound.getComment()); // take over .text information
                tmpModel.add(tmpDecl);
                Configuration tmpConfig = new Configuration(tmpModel, AssignmentState.ASSIGNED);
                IDecisionVariable tmpVar = tmpConfig.getDecision(tmpDecl);
                if (null != tmpVar) {
                    CompoundVariable cVar = (CompoundVariable) tmpVar;
                    DelegatingEasyEditorPage parent = new DelegatingEasyEditorPage(composite);
                    UIConfiguration uiCfg = ConfigurationTableEditorFactory.createConfiguration(
                        tmpConfig, parent, null);
                    result = ConfigurationTableEditorFactory.createCellEditor(uiCfg, getSlot(cVar, propertyIdentifier));
                    if (result instanceof IUpdateProvider && (Reference.TYPE.isAssignableFrom(tmpDecl.getType()) 
                        || VariabilityModel.isNameSlot(slot))) {
                        IUpdateProvider provider = (IUpdateProvider) result;
                        provider.setUpdateListener(new EditorUpdater(provider));
                    }
                    EditorUtils.assignHelpText(tmpVar, result.getControl());
                }
            }
        }
        if (null == result && QmConstants.SLOT_NAME.equals(normalizeIdentifier(propertyIdentifier))) {
            result = fallback.createFallbackPropertyEditor(composite);
        }
        return result;
    }

    /**
     * Returns the label provider, i.e., the label describing the actual object value.
     * 
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the cell editor shall be returned for 
     *   inside of <code>data</code>
     * @param value the value to be set for <code>propertyIdentifier</code>
     * @param imageProvider an image provider for default images for the label to be created
     * @return the label provider or <b>null</b> if no label provider was created
     */
    public ILabelProvider getLabelProvider(Object data, String propertyIdentifier, Object value, 
        IFallbackImageProvider imageProvider) {
        ILabelProvider result = null;
        Compound compound = getCompound();
        DecisionVariableDeclaration slot = getVariableDeclaration(propertyIdentifier);
        if (null != compound && null != slot) {
            Compound topCompoundType = compound.getRefinementBasis();
            IDatatype slotType = slot.getType();
            if (!Reference.isReferenceTo(slotType, topCompoundType)) {
                labelCreator.bind(value, slot, imageProvider);
                slot.getType().accept(labelCreator);
                result = labelCreator.getResult();
                labelCreator.clear();
            }
        }
        return result;
    }

    /**
     * Facilitates updates of cell editors.
     * 
     * @author Holger Eichelberger
     */
    private class EditorUpdater implements IUpdateListener, IChangeListener {

        private IUpdateProvider provider;
        
        /**
         * Creates an editor updater and registers it with {@link ChangeManager}. Unregistration
         * happens in {@link #dispose()}.
         * 
         * @param provider the underlying update provider
         */
        public EditorUpdater(IUpdateProvider provider) {
            this.provider = provider;
            ChangeManager.INSTANCE.addListener(this);
        }
        
        @Override
        public void variableChanged(EventKind kind, IDecisionVariable variable, int globalIndex) {
            if (EventKind.DELETING != kind && Reference.TYPE.isAssignableFrom(variable.getDeclaration().getType())) {
                provider.refreshContents();
            }
        }

        @Override
        public void dispose() {
            ChangeManager.INSTANCE.removeListener(this);
        }

        @Override
        public void valueChanged(IUpdateProvider provider) {
            IDecisionVariable var = provider.getVariable();
            if (VariabilityModel.isNameSlot(var)) {
                ChangeManager.INSTANCE.variableChanged(this, var);
            }
        }
        
    }

}
