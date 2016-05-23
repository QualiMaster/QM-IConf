package de.uni_hildesheim.sse.qmApp.editors;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;

import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.datatypes.Reference;

/**
 * CellEditor creator for creating {@link ChangeableComboCellEditor}s. This will not create any {@link Control}s
 * as this editor was specially designed for the Properties view of the Pipeline editor.
 * @author El-Sharkawy
 *
 */
public abstract class AbstractChangeableDropBoxCellEditorCreator implements IEditorCreator {

    /**
     * Constructor should only be visible for sub classes.
     */
    protected AbstractChangeableDropBoxCellEditorCreator() {}
    
    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
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