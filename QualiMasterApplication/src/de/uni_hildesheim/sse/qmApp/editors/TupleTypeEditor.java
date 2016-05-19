package de.uni_hildesheim.sse.qmApp.editors;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.datatypes.Reference;

/**
 * CellEditor creator for the selection of tuple types inside the topological pipeline editor.
 * @author El-Sharkawy
 *
 */
public class TupleTypeEditor implements IEditorCreator {

    public static final IEditorCreator CREATOR = new TupleTypeEditor();
    
    /**
     * Singleton constructor.
     */
    private TupleTypeEditor() {}
    
    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        Configuration cfg = variable.getConfiguration();
        List<ConstraintSyntaxTree> cstValues
            = cfg.getQueryCache().getPossibleValues((Reference) variable.getDeclaration().getType());
        String[] labels = new String[cstValues.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = QualiMasterDisplayNameProvider.INSTANCE.getDisplayName(cstValues.get(i), cfg);
        }
        
        return new ChangeableComboCellEditor(variable, parent, labels, cstValues);
    }

    @Override
    public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        // This provider shall only create editors for the topological pipeline editor -> only cell editors needed
        return null;
    }

}
