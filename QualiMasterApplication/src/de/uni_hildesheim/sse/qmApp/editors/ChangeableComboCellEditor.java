package de.uni_hildesheim.sse.qmApp.editors;

import java.util.List;

import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.swt.widgets.Composite;

import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.persistency.StringProvider;
import qualimasterapplication.Activator;

/**
 * A {@link ComboBoxCellEditor}, which uses Strings as values instead of Integers.
 * Thus, values do not change even if the list of possible values vary.
 * 
 * <b>Hints:</b>
 * <ul>
 *   <li>Values a String values and should be parseable with
 *   {@link de.uni_hildesheim.sse.ModelUtility
 *   #createExpression(String, net.ssehub.easy.varModel.model.IModelElement)}</li>
 *   <li>Values should be made readable with{@link de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider
 *   #getDisplayName(ConstraintSyntaxTree, net.ssehub.easy.varModel.confModel.Configuration)}, this should
 *   also considered by the label provider</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
class ChangeableComboCellEditor extends ComboBoxCellEditor {
    
    private List<ConstraintSyntaxTree> values;
    private String selectedValueLabel;
    private ConstraintSyntaxTree selectedValueRef;
    private IDecisionVariable variable;

    /**
     * Single constructor for this class.
     * @param variable The underlying {@link IDecisionVariable} which will be changed by this editor.
     * @param parent The table holding this cell editor
     * @param labels The human read-able values
     * @param values The select-able values, should be in same order and size as the labels.
     */
    ChangeableComboCellEditor(IDecisionVariable variable, Composite parent, String[] labels,
        List<ConstraintSyntaxTree> values) {
        
        super(parent, labels);
        this.values = values;
        this.variable = variable;
    }

    /**
     * Returns the parseable (not readable) selection instead of an index, which is the usual behavior
     * of a Combobox.
     * @return A parseable constraint as a String, or <tt>null</tt>
     */
    @Override
    public Object doGetValue() {
        Object value = selectedValueLabel;
        if (null == value) {
            value = super.doGetValue();

            if (null != value && value instanceof Integer) {
                Integer index = (Integer) value;
                if (index > -1 && index < values.size()) {
                    selectedValueRef = values.get((Integer) index);
                    selectedValueLabel = StringProvider.toIvmlString(selectedValueRef);
                    value = selectedValueLabel;
                }
            }
        }
        return value;
    }

    @Override
    public void doSetValue(Object oValue) {
        if (null == oValue) {
            // Maybe the default if nothing was selected
            super.doSetValue(-1);
        } else if (oValue instanceof Integer) {

            // Index based selection of value
            Integer iValue = (Integer) oValue;
            if (iValue > -1 && iValue < values.size()) {
                selectedValueRef = values.get((Integer) oValue);
                selectedValueLabel = StringProvider.toIvmlString(selectedValueRef);
                try {
                    Value value = ValueFactory.createValue(variable.getDeclaration().getType(), selectedValueRef);
                    variable.setValue(value, AssignmentState.ASSIGNED);
                    super.doSetValue(oValue);
                } catch (ValueDoesNotMatchTypeException e) {
                    Activator.getLogger(ChangeableComboCellEditor.class).exception(e);
                } catch (ConfigurationException e) {
                    Activator.getLogger(ChangeableComboCellEditor.class).exception(e);
                }
            }
        } else if (oValue instanceof String) {
            
            // String based selection of value
            boolean found = false;
            for (int i = 0; i < getItems().length && !found; i++) {
                if (getItems()[i].equals(oValue)) {
                    super.doSetValue(i);
                    found = true;
                }
            }
            // Fallback, but much worse performance
            for (int i = 0; i < getItems().length && !found; i++) {
                if (oValue.equals(StringProvider.toIvmlString(values.get(i)))) {
                    super.doSetValue(i);
                    found = true;
                }
            }
        }
    }
}