package de.uni_hildesheim.sse.qmApp.editors;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;

import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.Reference;

/**
 * CellEditorCreator selecting permissible parameters of a <tt>Source</tt>, <tt>Sink</tt>, or <tt>FamilyElement</tt>.
 * @author El-Sharkawy
 *
 */
public class PermissibleParameterCellEditorCreator extends AbstractChangeableDropBoxCellEditorCreator {

    public static final IEditorCreator CREATOR = new PermissibleParameterCellEditorCreator();
    
    @Override
    protected List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        setUIConfiguration(config);
        
        Configuration cfg = variable.getConfiguration();

        // Try to retrieve only relevant elements based on other selections of the property editor.
        List<ConstraintSyntaxTree> cstValues = (parent instanceof Tree) ? retrieveFilteredElements((Tree) parent)
            : null;
        
        // Fallback: Filter for all possible values, not only relevant ones
        if (null == cstValues) {
            Container type = (Container) variable.getDeclaration().getType();
            cstValues = cfg.getQueryCache().getPossibleValues((Reference) type.getContainedType());
        }
        
        // Create user readable labels
        String[] labels = new String[cstValues.size()];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = QualiMasterDisplayNameProvider.INSTANCE.getDisplayName(cstValues.get(i), cfg);
        }
        
        return new PermissibleParameterDialogCellEditor(variable, parent, labels, cstValues);
    }
}
