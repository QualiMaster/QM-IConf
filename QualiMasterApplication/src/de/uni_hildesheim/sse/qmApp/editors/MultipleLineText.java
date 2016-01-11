package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.easy.ui.productline_editor.IOverridingEditor;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;

/**
 * A {@link Text} with multiple lines. The MultipleLineText provides a {@link Text}-field which can be
 * filled with multiple lines of text.
 * 
 * @author Niko
 */
public class MultipleLineText {
    
    public static final IEditorCreator CREATOR = new MultiLineEditorCreator();
    
    /**
     * The Creator in order to provide the needed Text-controls.
     * @author Niko
     */
    private static class MultiLineEditorCreator implements IEditorCreator {
      
        @Override
        public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new MultiLineComposite(parent, variable, false);
        }

        @Override
        public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new MultiLineCellEditor(config, variable, parent);
        }  
    };
    
    /**
     * Implements the artifact editor.
     * 
     * @author Niko
     */
    private static class MultiLineComposite extends Composite implements ITextUpdater, IFixedLayout, IDirtyableEditor, 
        IOverridingEditor {

        private Text textField;
        private IDecisionVariable variable;
        
        /**
         * Constructs a MultiLineComposite aka a multiple-line-Text-field.
         * 
         * @param parent The parent composite.
         * @param variable the variable holding the current value
         * @param cell is this composite a standalone editor or a cell editor
         */
        MultiLineComposite(Composite parent, IDecisionVariable variable, boolean cell) {
            super(parent, SWT.NONE);
            this.variable = variable;

            GridLayout layout = new GridLayout();
            layout.marginRight = -layout.marginWidth;
            layout.marginWidth = 0;
            if (cell) {
                layout.marginTop = -layout.marginHeight;
                layout.marginHeight = 0;
            }
            GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            gridData.heightHint = 80;
            setLayout(layout);
            setLayoutData(gridData);
            
            textField = new Text(this, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
            gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
            textField.setLayoutData(gridData);
            EditorUtils.assignHelpText(variable, textField);
            if (null != variable) {
                Value value = variable.getValue();
                if (value instanceof StringValue) {
                    String tmp = ((StringValue) value).getValue();
                    textField.setText(tmp);
                }
            }
        }

        @Override
        public void updateText(String message) {
            textField.setText(message);
        }
        
        /**
         * Returns the value of the contained text field.
         * 
         * @return the value of the contained text
         */
        public Object getValue() {
            return textField.getText();
        }
        
        /**
         * Sets the value and causes the dialog to display the right selection.
         * 
         * @param value the new value
         */
        public void setValue(String value) {
            updateText(value);
        }
        
        /**
         * Changes the focus to the text field of this component.
         */
        public void doSetFocus() {
            textField.setFocus();
        }

        @Override
        public void addDirtyListener(DirtyListener listener) {
            textField.addKeyListener(listener);
        }

        @Override
        public void removeDirtyListener(DirtyListener listener) {
            textField.removeKeyListener(listener);
        }
        
        @Override
        public Value getValueAssignment(Object value) throws ValueDoesNotMatchTypeException {
            Value val = null;
            IDatatype type = variable.getDeclaration().getType();
            if (null != value && type.isAssignableFrom(StringType.TYPE)) {
                val = ValueFactory.createValue(type, value);
            }
            return val;
        }

        @Override
        public String getValueText() {
            String result = null;
            if (null != variable) {
                Value value = variable.getValue();
                if (value instanceof StringValue) { // we know this from the model
                    result = ((StringValue) value).getValue();
                }
            }
            return result;
        }

        /**
         * Updates the editor from the {@link #getValueText()}.
         * 
         * @return the value of {@link #getValueText()}.
         */
        private String updateFromValueText() {
            String val = getValueText();
            if (null != val) {
                textField.setText(val);
            }
            return val;
        }

        @Override
        public void refreshContents() {
            updateFromValueText();
        }

    }
    
    /**
     * Implements the artifact cell editor.
     * 
     * @author Holger Eichelberger
     */
    private static class MultiLineCellEditor extends UpdatingCellEditor {

        private IDecisionVariable variable;
        private MultiLineComposite composite;

        /**
         * Creates an artifact cell editor instance.
         * 
         * @param config the UI configuration
         * @param variable the decision variable to create the editor for
         * @param parent the UI parent element
         */
        MultiLineCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            super(parent);
            this.variable = variable;
        }
        
        @Override
        protected Control createControl(Composite parent) {
            this.composite = new MultiLineComposite(parent, variable, true);
            return composite;
        }

        @Override
        protected Object doGetValue() {
            Object result;
            if (null != composite) {
                result = composite.getValue();
            } else {
                result = null;
            }
            return result;
        }

        @Override
        protected void doSetFocus() {
            if (null != composite) {
                composite.doSetFocus();
            }
        }

        @Override
        protected void doSetValue(Object value) {
            if (null != value && null != composite) {
                composite.setValue(value.toString());
                super.doSetValue(value);
            }
        }

        @Override
        public IDecisionVariable getVariable() {
            return variable;
        }
        
    }
}