package de.uni_hildesheim.sse.qmApp.editors;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

/**
 * CellEditor creator for creating {@link ChangeableComboCellEditor}s. This will not create any {@link Control}s
 * as this editor was specially designed for the Properties view of the Pipeline editor.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractChangeableDropBoxCellEditorCreator implements IEditorCreator {
    
    private UIConfiguration config;
    
    /**
     * Constructor should only be visible for sub classes.
     */
    protected AbstractChangeableDropBoxCellEditorCreator() {}
    
    protected Iterator<Map.Entry<UIParameter, Object>> parameterIterator() {
        Map<UIParameter, Object> map = config.getParameters();
        return null != map ? map.entrySet().iterator() : null;
    }
    
    protected Pipeline getPipeline() {
        Pipeline pipeline = null;
        Iterator<Map.Entry<UIParameter, Object>> itr = parameterIterator();
        if (null != itr) {
            while (pipeline == null && itr.hasNext()) {
                Map.Entry<UIParameter, Object> entry = itr.next();
                UIParameter parameter = entry.getKey();
                if (PipelineDiagramElementPropertyEditorCreator.ROOT_PARAMETER_NAME.equals(parameter.getName())
                    && parameter.getDefaultValue() != null && parameter.getDefaultValue() instanceof Pipeline) {
                    
                    pipeline = (Pipeline) parameter.getDefaultValue();
                }
            }
        }
        
        return pipeline;
    }
    
    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        this.config = config;
        
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
