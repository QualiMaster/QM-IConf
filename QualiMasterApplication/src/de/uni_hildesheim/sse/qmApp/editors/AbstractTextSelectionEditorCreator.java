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

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.modelManagement.IModelListener;
import net.ssehub.easy.instantiation.core.Bundle;
import net.ssehub.easy.producer.ui.productline_editor.IOverridingEditor;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * The {@link AbstractTextSelectionEditorCreator} creates a composite with a text field and a button.
 * The button within the composite triggers a browse operation.
 * Subclasses shall not store information in attributes.
 * 
 * @author Niko
 * @author Holger Eichelberger
 */
public abstract class AbstractTextSelectionEditorCreator implements IEditorCreator {

    /**
     * Prevents external creation.
     */
    protected AbstractTextSelectionEditorCreator() {
    }
    
    @Override
    public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        return new ArtifactComposite(config, variable, parent, null);
    }
    
    @Override
    public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        return new ArtifactCellEditor(config, variable, parent);
    }

    /**
     * Returns the actual text for the browse button.
     * 
     * @param cell whether the button is for the cell editor (<code>true</code>) or the plain editor component
     * @param context the decision variable providing the context for the selection
     * @return the browse button text
     */
    protected String getBrowseButtonText(boolean cell, IDecisionVariable context) {
        return cell ? "..." : "Browse..."; // default
    }

    /**
     * Returns whether the browse button is active.
     * 
     * @param cell whether the button is for the cell editor (<code>true</code>) or the plain editor component
     * @param context the decision variable providing the context for the selection
     * @return <code>true</code> if active, <code>false</code> else
     */
    protected abstract boolean isBrowseButtonActive(boolean cell, IDecisionVariable context);

    /**
     * Returns whether the text editor is enabled.
     * 
     * @param cell whether the button is for the cell editor (<code>true</code>) or the plain editor component
     * @param context the decision variable providing the context for the selection
     * @return <code>true</code> if enabled, <code>false</code> else
     */
    protected abstract boolean isTextEditorEnabled(boolean cell, IDecisionVariable context);

    /**
     * Is called when the browse button is selected.
     * 
     * @param text the actual text from the text editor of the created editor component
     * @param context the decision variable providing the context for the selection
     * @param updater the updater for storing the selectionr result
     */
    protected abstract void browseButtonSelected(String text, IDecisionVariable context, ITextUpdater updater);
    
    /**
     * Implements the artifact editor.
     * 
     * @author Holger Eichelberger
     */
    public class ArtifactComposite extends Composite implements ITextUpdater, IDirtyableEditor, 
        IOverridingEditor, IModelListener<Project> {

        private Text textField;
        private Button button;
        private UIConfiguration config;
        private IDecisionVariable variable;
        private ArtifactCellEditor cellEditor;

        /**
         * Creates an artifact editor instance.
         * 
         * @param config the UI configuration
         * @param variable the decision variable to create the editor for
         * @param parent the UI parent element
         * @param cellEditor the cell editor instance in case that this class is used in a cell editor
         */
        ArtifactComposite(UIConfiguration config, IDecisionVariable variable, Composite parent, 
            ArtifactCellEditor cellEditor) {
            super(parent, SWT.FILL);
            this.config = config;
            this.variable = variable;
            VarModel.INSTANCE.events().addModelListener(variable.getConfiguration().getProject(), this);
            this.cellEditor = cellEditor;

            final boolean cell = null != cellEditor;
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
            EditorUtils.assignHelpText(variable, textField);
            updateFromValueText();
             
            button = new Button(this, SWT.NONE);
            button.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
            button.setText(getBrowseButtonText(cell, variable));
            button.setEnabled(isBrowseButtonActive(cell, variable));

            button.addSelectionListener(new SelectionAdapter() {
           
                @Override
                public void widgetSelected(SelectionEvent exc) {
                    AbstractTextSelectionEditorCreator.this.browseButtonSelected(textField.getText(), 
                        ArtifactComposite.this.variable, ArtifactComposite.this);
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
            if (null != cellEditor) {
                cellEditor.notifyValueChanged();
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
        public void refresh() {
            updateFromValueText();
        }

        @Override
        public void updateTextAndModel(String message) {
            updateText(message);
            Value value;
            try {
                value = ValueFactory.createValue(StringType.TYPE, message);
                variable.setValue(value, AssignmentState.ASSIGNED);
            } catch (ValueDoesNotMatchTypeException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        }

        @Override
        public void notifyReplaced(Project oldModel, Project newModel) {
            IDecisionVariable newVar = Configuration.mapVariable(variable, variable.getConfiguration());
            if (null != newVar) {
                variable = newVar;
            } else {
                EASyLoggerFactory.INSTANCE.getLogger(getClass(), Bundle.ID)
                    .error("No variable found in new configuratio, i.e., discontinued mapping!");
            }
        }
    }

    /**
     * Implements the artifact cell editor.
     * 
     * @author Holger Eichelberger
     */
    private class ArtifactCellEditor extends UpdatingCellEditor {

        private ArtifactComposite composite;

        /**
         * Creates an artifact cell editor instance.
         * 
         * @param config the UI configuration
         * @param variable the decision variable to create the editor for
         * @param parent the UI parent element
         */
        ArtifactCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            super(config, variable, parent);
        }
        
        @Override
        protected Control createControl(Composite parent) {
            this.composite = new ArtifactComposite(getUiConfiguration(), getVariable(), parent, this);
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
        
    }

}
