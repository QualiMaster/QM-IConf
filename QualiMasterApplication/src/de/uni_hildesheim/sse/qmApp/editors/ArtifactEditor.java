package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

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
import de.uni_hildesheim.sse.qmApp.dialogs.MavenArtifactEditor;

/**
 * The {@link ArtifactEditor} created a composites with a {@link Text} and a {@link Button}.
 * The {@link Button} within the composite triggers the Opening of the {@link ArtifactEditor}.
 * @author Niko
 */
public class ArtifactEditor {
      
    public static final IEditorCreator CREATOR = new ArtifactEditorCreator();
    /**
     * Creature actual editor with Text and Button.
     * @author Niko
     */
    private static class ArtifactEditorCreator implements IEditorCreator {
      
        @Override
        public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new ArtifactComposite(config, variable, parent, false);
        }
        
        @Override
        public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new ArtifactCellEditor(config, variable, parent);
        }
        
    };

    /**
     * Implements the artifact editor.
     * 
     * @author Holger Eichelberger
     */
    private static class ArtifactComposite extends Composite implements ITextUpdater, IDirtyableEditor, 
        IOverridingEditor {

        private Text textField;
        private Button button;
        private UIConfiguration config;
        private IDecisionVariable variable;

        /**
         * Creates an artifact editor instance.
         * 
         * @param config the UI configuration
         * @param variable the decision variable to create the editor for
         * @param parent the UI parent element
         * @param cell is this composite a standalone editor or a cell editor
         */
        ArtifactComposite(UIConfiguration config, IDecisionVariable variable, Composite parent, boolean cell) {
            super(parent, SWT.FILL);
            this.config = config;
            this.variable = variable;

            GridLayout layout = new GridLayout();
            layout.marginRight = -layout.marginWidth;
            layout.marginWidth = 0;
            if (cell) {
                layout.marginTop = -layout.marginHeight;
                layout.marginHeight = 0;
            }
            layout.numColumns = 3;
            
            GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            setLayout(layout);
            setLayoutData(gridData);
            
            textField = new Text(this, SWT.BORDER);
            final GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
            if (cell) {
                data.widthHint = 80;
            }
            textField.setLayoutData(data);
            updateFromValueText();
             
            button = new Button(this, SWT.NONE);
            button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            button.setText(cell ? "..." : "Browse...");

            button.addSelectionListener(new SelectionAdapter() {
           
                @Override
                public void widgetSelected(SelectionEvent exc) {
                    MavenArtifactEditor dlg = new MavenArtifactEditor(
                        PlatformUI.getWorkbench().getDisplay().getActiveShell(), ArtifactComposite.this);
                    String artifactSpec = textField.getText();
                    if (null != artifactSpec) {
                        dlg.setInitialTreePath(artifactSpec);
                    }
                    dlg.open();
                }
            });
            
        }
        
        

        /**
         * Update the text.
         * 
         * @param message to fill the textfield
         */
        public void updateText(String message) {
            textField.setText(message);
            if (null != config && null != config.getParent()) {
                config.getParent().setDirty();
            }
        }

        /**
         * Sets the value and causes the dialog to display the right selection.
         * 
         * @param artifactSpec the artifact spec (value)
         */
        public void setValue(String artifactSpec) {
            updateText(artifactSpec);
        }
        
        /**
         * Changes the focus to the text field of this component.
         */
        public void doSetFocus() {
            textField.setFocus();
        }
        
        /**
         * Returns the value of the contained text field.
         * 
         * @return the value of the contained text
         */
        @Override
        public Object getValue() {
            return textField.getText();
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
    private static class ArtifactCellEditor extends CellEditor {

        private UIConfiguration config;
        private IDecisionVariable variable;
        private ArtifactComposite composite;

        /**
         * Creates an artifact cell editor instance.
         * 
         * @param config the UI configuration
         * @param variable the decision variable to create the editor for
         * @param parent the UI parent element
         */
        ArtifactCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            super(parent);
            this.config = config;
            this.variable = variable;
        }
        
        @Override
        protected Control createControl(Composite parent) {
            this.composite = new ArtifactComposite(config, variable, parent, true);
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
            }
        }
        
    }
    
}
