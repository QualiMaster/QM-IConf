package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.qmApp.editors.EditorUtils;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.IChangeListener;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import net.ssehub.easy.producer.ui.productline_editor.DelegatingEasyEditorPage;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateListener;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateProvider;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IvmlKeyWords;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.MandatoryDeclarationClassifier;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.VariableContainer;
import pipeline.FamilyElement;
import pipeline.Pipeline;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.PipelineImpl;

/**
 * Implements a generic property editor creator for pipeline diagram elements.
 * This class may be even more generic. We defer this to later...
 * 
 * @author Holger Eichelberger
 */
public class PipelineDiagramElementPropertyEditorCreator implements IPropertyEditorCreator {

    public static final String ROOT_PARAMETER_NAME = "PipelineRoot";
    
    private static final String IMPL_SUFFIX = "Impl";
    
    private Class<?> reactsOn;
    private IModelPart modelPart = VariabilityModel.Configuration.PIPELINES;
    private LabelProviderCreator labelCreator;
    private Configuration cfg;
    private VariableContainer importance;

    /**
     * Creates a new pipeline diagram node property editor creator.
     * 
     * @param reactsOn the actual node class it reacts on
     */
    public PipelineDiagramElementPropertyEditorCreator(Class<?> reactsOn) {
        this.reactsOn = reactsOn;
        cfg = modelPart.getConfiguration();
        MandatoryDeclarationClassifier classifier = new MandatoryDeclarationClassifier(cfg, FilterType.ALL);
        cfg.getProject().accept(classifier);
        importance = classifier.getImportances();
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
     * Alternative {@link #getVariableName(String)} method if the compound <b>cannot</b> retrieved by the classical
     * way as it is done inside the {@link #getCompound()} method.
     * @param compound The concrete (sub) compound to which the slot belongs to.
     * @param propertyIdentifier the property/slot identifier
     * @return the variable declaration (may be <b>null</b> if not found)
     */
    private DecisionVariableDeclaration getVariableDeclaration(Compound compound, String propertyIdentifier) {
        DecisionVariableDeclaration result = null;
        if (propertyIdentifier.length() > 0) {
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
            if (null != importance && importance.isMandatory(decl)) {
                result = result + "*";
            }
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
        Compound compound = null;
        DecisionVariableDeclaration slot = null;
        if (data instanceof Pipeline && ((Pipeline) data).getIsSubPipeline()) {
            try {
                compound = (Compound) ModelQuery.findElementByName(getVarModel(), QmConstants.TYPE_SUBPIPELINE,
                    Compound.class);
                slot = getVariableDeclaration(compound, propertyIdentifier);
            } catch (ModelQueryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        } else {
            compound = getCompound();
            slot = getVariableDeclaration(propertyIdentifier);
        }
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
                cfg.shareQueryCacheWith(tmpConfig);
                IDecisionVariable tmpVar = tmpConfig.getDecision(tmpDecl);
                if (null != tmpVar) {
                    CompoundVariable cVar = (CompoundVariable) tmpVar;
                    DelegatingEasyEditorPage parent = new DelegatingEasyEditorPage(composite);
                    UIConfiguration uiCfg = ConfigurationTableEditorFactory.createConfiguration(
                        tmpConfig, parent, createParameters(data));
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
        Compound compound = null;
        DecisionVariableDeclaration slot = null;
        if (data instanceof Pipeline && ((Pipeline) data).getIsSubPipeline()) {
            try {
                compound = (Compound) ModelQuery.findElementByName(getVarModel(), QmConstants.TYPE_SUBPIPELINE,
                    Compound.class);
                slot = getVariableDeclaration(compound, propertyIdentifier);
            } catch (ModelQueryException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } 
        } else {
            compound = getCompound();
            slot = getVariableDeclaration(propertyIdentifier);
        }
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
        
        // Special handling for constraint based combo boxes
        boolean specialProperty = "tupleType".equals(propertyIdentifier) || "default".equals(propertyIdentifier)
            || "subPipelineFamily".equals(propertyIdentifier);
        if (specialProperty && null != value && value instanceof String) {
            
            try {
                ConstraintSyntaxTree cstValue = ModelUtility.INSTANCE.createExpression((String) value,
                    ModelAccess.getModel(VariabilityModel.Definition.TOP_LEVEL));
                String readableString = QualiMasterDisplayNameProvider.INSTANCE.getDisplayName(cstValue,
                    getConfiguration());
                result = new StaticLabelProvider(readableString, result.getImage(data));
            } catch (CSTSemanticException e) {
                result = new StaticLabelProvider((String) value, result.getImage(data));
            } catch (ConstraintSyntaxException e) {
                result = new StaticLabelProvider((String) value, result.getImage(data));
            }
            
        }
        
        return result;
    }

    /**
     * Facilitates updates of cell editors.
     * 
     * @author Holger Eichelberger
     */
    class EditorUpdater implements IUpdateListener, IChangeListener {

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
                provider.refresh();
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

    @Override
    public boolean isVisible(Object data, String propertyIdentifier) {
        boolean isVisible = true;
        // isConnector attribute become only relevant if pipeline is a sub pipeline
        if (data instanceof FamilyElement && "isConnector".equals(propertyIdentifier)) {
            isVisible = false;
            EObject parent = ((FamilyElement) data).eContainer();
            // The parent should be the complete pipeline
            if (parent instanceof Pipeline) {
                Pipeline pipeline = (Pipeline) parent;
                // Make it visible if pipeline is sub pipeline or if it is configured inconsistently
                isVisible = pipeline.getIsSubPipeline() || ((FamilyElement) data).getIsConnector();
            }
        }
        
        if (data instanceof Pipeline && "subPipelineFamily".equals(propertyIdentifier)) {
            isVisible = false;
            Pipeline pipeline = (Pipeline) data;
            isVisible = pipeline.getIsSubPipeline();
        }
        return isVisible;
    }

    @Override
    public boolean isFilterable() {
        // Currently, only elements of the family can be filtered.
        return reactsOn == FamilyElementImpl.class || reactsOn == PipelineImpl.class;
    }

    protected Map<UIParameter, Object> createParameters(final Object data) {
        UIParameter parameter = new UIParameter(ROOT_PARAMETER_NAME, ((EObject) data).eContainer());
        Map<UIParameter, Object> parameters = new HashMap<UIParameter, Object>();
        parameters.put(parameter, parameter.getDefaultValue());
        return parameters;
    }

}
