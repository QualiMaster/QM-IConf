package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

import de.uni_hildesheim.sse.qmApp.editors.EditorUtils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.producer.ui.confModel.IRangeRestriction;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.producer.ui.productline_editor.DelegatingEasyEditorPage;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateProvider;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import pipeline.impl.FlowImpl;

/**
 * Implements a specialized property editor creator for Flows.
 * 
 * @author Sascha El-Sharkawy
 * @author Holger Eichelberger
 */
public class FlowPropertyEditorCreator extends PipelineDiagramElementPropertyEditorCreator {

    public static final IRangeRestriction FLOW_TUPLE_FILTER = new FlowValueRestriction();
    
    /**
     * Restricts the possible values for the selection of tupleType in properties editor of a Flow.
     * @author El-Sharkawy
     *
     */
    private static class FlowValueRestriction implements IRangeRestriction {
        
        @Override
        public boolean appliesTo(IDecisionVariable variable) {
//            return "tupleType".equals(variable.getDeclaration().getName());
            return false;
        }

        @Override
        public boolean filterValue(Object value, String label) {
//            return label != null && (label.contains("output"));
            return false;
        }
        
    }
    
    /**
     * Creates a new Flow property editor creator.
     */
    public FlowPropertyEditorCreator() {
        super(FlowImpl.class);
    }
    
    @Override
    public CellEditor createPropertyEditor(Composite composite, final Object data, String propertyIdentifier, 
        IFallbackEditorCreator fallback) {
        CellEditor result = null;
        Compound compound = getCompound();
        DecisionVariableDeclaration slot = getVariableDeclaration(propertyIdentifier);
        if (null != compound && null != slot) {
            // find top-level type to exclude references
            Compound topCompoundType = CompoundUtil.getRefinementBasis(compound);
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
                getConfiguration().shareQueryCacheWith(tmpConfig);
                IDecisionVariable tmpVar = tmpConfig.getDecision(tmpDecl);
                if (null != tmpVar) {
                    CompoundVariable cVar = (CompoundVariable) tmpVar;
                    DelegatingEasyEditorPage parent = new DelegatingEasyEditorPage(composite);
                    List<IRangeRestriction> restrictors = new ArrayList<IRangeRestriction>();
//                    restrictors.add(new FlowValueRestriction(((Flow) data).getSource().getName()));
                    restrictors.add(FLOW_TUPLE_FILTER);
                    UIConfiguration uiCfg = ConfigurationTableEditorFactory.createConfiguration(
                        tmpConfig, parent, createParameters(data));
                    result = ConfigurationTableEditorFactory.createCellEditor(uiCfg, getSlot(cVar, propertyIdentifier),
                        restrictors);
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
}
