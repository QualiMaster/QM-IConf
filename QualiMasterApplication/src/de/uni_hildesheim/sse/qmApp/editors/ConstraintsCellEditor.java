package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import net.ssehub.easy.producer.ui.productline_editor.IUpdateListener;
import net.ssehub.easy.producer.ui.productline_editor.IUpdateProvider;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;

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
        ConstraintsEditorDialog dlg = new ConstraintsEditorDialog(cellEditorWindow.getShell(), context, valueToStr());
        if (Window.OK == dlg.open()) {
            setValue(dlg.getConstraintsText());
        }
        return null;
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
                sb.append(conValue.getElement(0).getValue().toString());
            }
            for (int i = 1; i < nConstraints; i++) {
                sb.append(ConstraintsEditorDialog.SEPARATOR);
                sb.append(conValue.getElement(1).getValue().toString());
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
