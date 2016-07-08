package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateListener;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateProvider;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.ContainerVariable;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.persistency.StringProvider;
import qualimasterapplication.Activator;

/**
 * Implements a cell editor for constraints.
 * 
 * @author Holger Eichelberger
 * @author El-Sharkawy
 * @see <a href="http://stackoverflow.com/questions/14464611/jface-dialogcelleditor-how-to-make-buttons-always-appear">
 * http://stackoverflow.com/questions/14464611/jface-dialogcelleditor-how-to-make-buttons-always-appear</a>
 */
class ConstraintsCellEditor extends DialogCellEditor implements IUpdateProvider {

    private IDecisionVariable context;
    private IUpdateListener listener;
    private Button button;
    
    /**
     * Creates a constraint cell editor.
     * 
     * @param config the configuration
     * @param variable the decision variable
     * @param parent the parent composite
     */
    ConstraintsCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(parent);
        
        if (null != parent && parent instanceof Tree && variable instanceof ContainerVariable
            && ((ContainerValue) variable.getValue()).getElementSize() == 0) {
            
            Tree propertiesTree = (Tree) parent;
            ContainerVariable conVar = (ContainerVariable) variable;
            
            String constraintValues = null;
            for (int i = 0, end = propertiesTree.getItems().length; i < end && null == constraintValues; i++) {
                TreeItem tmpItem = propertiesTree.getItems()[i];
                if (tmpItem.getText().contains("constraints")) {
                    constraintValues = tmpItem.getText(1);
                    if (constraintValues != null && !constraintValues.isEmpty()) {
                        String[] tmpArray = constraintValues.split(ConstraintsEditorDialog.SEPARATOR);
                        for (int j = 0; j < tmpArray.length; j++) {
                            try {
                                ConstraintSyntaxTree cst = ModelUtility.INSTANCE.createExpression(tmpArray[j],
                                    variable.getDeclaration().getParent());
                                Value constraintValue = ValueFactory.createValue(ConstraintType.TYPE, cst);
                                conVar.addNestedElement().setValue(constraintValue, AssignmentState.ASSIGNED);
                            } catch (ValueDoesNotMatchTypeException e) {
                                Activator.getLogger(ConstraintsCellEditor.class).exception(e);
                            } catch (ConfigurationException e) {
                                Activator.getLogger(ConstraintsCellEditor.class).exception(e);
                            } catch (CSTSemanticException e) {
                                Activator.getLogger(ConstraintsCellEditor.class).exception(e);
                            } catch (ConstraintSyntaxException e) {
                                Activator.getLogger(ConstraintsCellEditor.class).exception(e);
                            }
                        }
                    }
                }
            }
        }
        
        
        this.context = variable;
    }
   
    
    @Override
    protected Button createButton(Composite parent) {
        button = super.createButton(parent);
        button.setText("...");
        return button;
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        // null means that dialog was canceled or no selectionw as made in dialog
        Object returnValue = null;
        ConstraintsEditorDialog dlg = new ConstraintsEditorDialog(cellEditorWindow.getShell(), context, valueToStr());
        if (Window.OK == dlg.open()) {
            setValue(dlg.getConstraintsText());
            returnValue = getValue();
        }
        return returnValue;
    }
    
    /**
     * Converts a (ContainerValue full of Constraints) value into a string representation as needed by the
     * {@link ConstraintsEditorDialog}.
     * @return A String representation of the current value, maybe an empty string, but not <tt>null</tt>.
     */
    private String valueToStr() {
        StringBuffer sb = new StringBuffer();
        Value value = context.getValue();
        if (null != value && value instanceof ContainerValue) {
            ContainerValue conValue = (ContainerValue) value;
            int nConstraints = conValue.getElementSize();
            if (nConstraints > 0) {
                ConstraintSyntaxTree cst = (ConstraintSyntaxTree) conValue.getElement(0).getValue();
                sb.append(StringProvider.toIvmlString(cst, (Compound) context.getDeclaration().getParent()));
            }
            for (int i = 1; i < nConstraints; i++) {
                sb.append(ConstraintsEditorDialog.SEPARATOR);
                ConstraintSyntaxTree cst = (ConstraintSyntaxTree) conValue.getElement(i).getValue();
                sb.append(StringProvider.toIvmlString(cst, (Compound) context.getDeclaration().getParent()));
            }
        }
        return sb.toString();
    }

    @Override
    protected void doSetValue(Object value) {
        super.doSetValue(value);
        notifyValueChanged();
    }

    @Override
    public IDecisionVariable getVariable() {
        return context;
    }

    @Override
    public void setUpdateListener(IUpdateListener listener) {
        this.listener = listener;
    }
    
    /**
     * Called to notify about a value change.
     */
    protected void notifyValueChanged() {
        if (null != listener) {
            listener.valueChanged(this);
        }
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        
    }

}
