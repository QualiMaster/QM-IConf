package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;

/**
 * CellEditor creator for the selection of default algorithms for family elements
 * inside the topological pipeline editor.
 * @author El-Sharkawy
 *
 */
public class DefaultAlgorithmCellEditor extends AbstractChangeableDropBoxCellEditorCreator {

    public static final IEditorCreator CREATOR = new DefaultAlgorithmCellEditor();
    
    /**
     * Singleton constructor.
     */
    private DefaultAlgorithmCellEditor() {}
    
    @Override
    protected List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree) {
        List<ConstraintSyntaxTree> cstValues = null;
        
        // Extract selected family from property editor
        String familyName = null;
        for (int i = 0, end = propertiesTree.getItems().length; i < end && null == familyName; i++) {
            TreeItem tmpItem = propertiesTree.getItems()[i];
            if (tmpItem.getText().contains("family")) {
                familyName = tmpItem.getText(1);            
            }
        }
        
        // Optimal solution: Filter for relevant algorithms (algorithms which are part of selected family)
        if (null != familyName && !familyName.isEmpty()) {
            Configuration familyConfig = ModelAccess.getConfiguration(VariabilityModel.Configuration.FAMILIES);
            Iterator<IDecisionVariable> varItr = familyConfig.iterator();
            boolean found = false;
            while (varItr.hasNext() && !found) {
                IDecisionVariable tmpVar = varItr.next();
                IDecisionVariable nameSlot = tmpVar.getNestedElement("name");
                if (null != nameSlot && familyName.equals(nameSlot.getValue().getValue())) {
                    found = true;
                    
                    // Convert all member references to cst values
                    IDecisionVariable members = tmpVar.getNestedElement("members");
                    if (null != members && null != members.getValue()) {
                        ContainerValue possibleAlgos = (ContainerValue) members.getValue();
                        cstValues = new ArrayList<ConstraintSyntaxTree>();
                        
                        for (int i = 0, end = possibleAlgos.getElementSize(); i < end; i++) {
                            ReferenceValue algoRefValue = (ReferenceValue) possibleAlgos.getElement(i);
                            ConstraintSyntaxTree cstValue = null;
                            if (null != algoRefValue.getValue()) {
                                cstValue = new Variable(algoRefValue.getValue());
                            } else {
                                cstValue = algoRefValue.getValueEx();
                            }
                            
                            if (null != cstValue) {
                                cstValues.add(cstValue);
                            }
                        }
                    }
                }
            }
        }
        return cstValues;
    }
}
