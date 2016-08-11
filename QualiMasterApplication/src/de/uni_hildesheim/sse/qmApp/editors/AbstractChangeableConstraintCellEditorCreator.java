package de.uni_hildesheim.sse.qmApp.editors;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import de.uni_hildesheim.sse.qmApp.tabbedViews.PipelineDiagramElementPropertyEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import pipeline.Pipeline;
import pipeline.PipelineElement;
import qualimasterapplication.Activator;

/**
 * CellEditor creator selecting constraints. This editor does not safe indexes, instead
 * it saves a parseable String to avoid conflicts if elements are added or removed to the model. <br/>
 * By default it returns {@link ChangeableComboCellEditor}s but sub classes may return other editors to
 * allow multiple selection. <br/>
 * This will not create any {@link Control}s
 * as this editor was specially designed for the Properties view of the Pipeline editor.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractChangeableConstraintCellEditorCreator implements IEditorCreator {
    
    private UIConfiguration config;
    private IDecisionVariable variable;
    
    /**
     * Constructor should only be visible for sub classes.
     */
    protected AbstractChangeableConstraintCellEditorCreator() {}
    
    /**
     * Returns (a copy) of the configuration parameters.
     * 
     * @return the parameters (may be <b>null</b>)
     */
    protected Iterator<Map.Entry<UIParameter, Object>> parameterIterator() {
        Map<UIParameter, Object> map = config.getParameters();
        return null != map ? map.entrySet().iterator() : null;
    }
    
    /**
     * Extracts the currently configured {@link PipelineElement} or {@link Pipeline} out of the {@link UIConfiguration},
     * if passed as parameter to it.
     * @param parameterName The identifier of the desired element. 
     * @param type One of the types specified in the EMF model.
     * @param <P> Will be <tt>type</tt>.
     * @return The desired element, which is currently configured, or <tt>null</tt> if not passed as parameter to the
     *     {@link UIConfiguration}.
     */
    @SuppressWarnings("unchecked")
    private <P extends EObject> P extractParameter(String parameterName, Class<P> type) {
        P pipelineElement = null;
        Class<?> expectedClass = (null != type) ? type : EObject.class;
        
        Iterator<Map.Entry<UIParameter, Object>> itr = parameterIterator();
        if (null != itr) {
            while (pipelineElement == null && itr.hasNext()) {
                Map.Entry<UIParameter, Object> entry = itr.next();
                UIParameter parameter = entry.getKey();
                if (parameterName.equals(parameter.getName())
                    && parameter.getDefaultValue() != null && expectedClass.isInstance(parameter.getDefaultValue())) {
                    
                    try {
                        pipelineElement = (P) parameter.getDefaultValue();
                    } catch (ClassCastException ccExc) {
                        Activator.getLogger(AbstractChangeableConstraintCellEditorCreator.class).exception(ccExc);
                    }
                }
            }
        }
        
        return pipelineElement;
    }
    
    /**
     * Extracts the {@link Pipeline} from the parameters, if passed to the {@link UIConfiguration}.
     * @return The {@link Pipeline} or <tt>null</tt>.
     */
    protected Pipeline getPipeline() {
        return extractParameter(PipelineDiagramElementPropertyEditorCreator.ROOT_PARAMETER_NAME, Pipeline.class);
    }
    
    /**
     * Extracts the {@link PipelineElement} from the parameters, if passed to the {@link UIConfiguration}.
     * @return The currently edited {@link PipelineElement} or <tt>null</tt>.
     */
    protected PipelineElement getPipelineElement() {
        return extractParameter(PipelineDiagramElementPropertyEditorCreator.PIPELINE_PARAMETER_NAME,
            PipelineElement.class);
    }
    
    /**
     * Returns the currently configured variable.
     * @return The variable to be configured.
     */
    protected IDecisionVariable getVariable() {
        return variable;
    }
    
    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        this.config = config;
        this.variable = variable;
        
        Configuration cfg = variable.getConfiguration();

        // Try to retrieve only relevant elements based on other selections of the property editor.
        List<ConstraintSyntaxTree> cstValues = (parent instanceof Tree) ? retrieveFilteredElements((Tree) parent)
            : null;
        
        // Fallback: Filter for all possible values, not only relevant ones
        if (null == cstValues) {
            cstValues = cfg.getQueryCache().getPossibleValues((Reference) variable.getDeclaration().getType());
        }
        
        // Create user readable labels
        String[] labels = new String[cstValues.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = QualiMasterDisplayNameProvider.INSTANCE.getDisplayName(cstValues.get(i), cfg);
        }
        
        assert labels == null || labels.length == cstValues.size() : "Different amount of labels and values specified";
        CellEditor editor = createCellEditor(parent, labels, cstValues);
        
        // Reset
        this.config = null;
        this.variable = null;
        
        return editor;
    }
    
    /**
     * Creates the editor.
     * @param parent the UI parent element
     * @param labels The human readable values to select.
     * @param cstValues The values as internally handled, must be the same amount as <tt>labels</tt>.
     * @return The editor for selecting constraints.
     */
    protected CellEditor createCellEditor(Composite parent, String[] labels, List<ConstraintSyntaxTree> cstValues) {
        // Default implementation: Return drop down menu
        return new ChangeableComboCellEditor(variable, parent, labels, cstValues);
    }

    /**
     * Tries to create a list of relevant values based on the current selection.
     * @param propertiesTree The holding property editor.
     * @return Relevant values as {@link ConstraintSyntaxTree}s or <tt>null</tt> if it could not be created.
     */
    protected abstract List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree);

    @Override
    public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        // This provider shall only create editors for the topological pipeline editor -> only cell editors needed
        return null;
    }
}
