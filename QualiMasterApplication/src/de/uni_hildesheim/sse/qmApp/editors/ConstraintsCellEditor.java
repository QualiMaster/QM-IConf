package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;

/**
 * Implements a cell editor for constraints.
 * 
 * @author Holger Eichelberger
 */
class ConstraintsCellEditor extends UpdatingCellEditor {

    private Label text;
    private Shell shell;
    private IDecisionVariable context;

    /**
     * Creates a constraint cell editor.
     * 
     * @param config the configuration
     * @param variable the decision variable
     * @param parent the parent composite
     */
    ConstraintsCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(parent);
        this.shell = parent.getShell();
        this.context = variable;
    }
    
    @Override
    protected Control createControl(Composite parent) {
        Composite panel = new Composite(parent, SWT.EMBEDDED);
        panel.setBounds(new Rectangle(0, 0, 0, 0));
        
        panel.setLayout(new GridLayout(2, false));
        text = new Label(panel, 0);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        text.setLayoutData(gridData);
        
        Button button = new Button(panel, SWT.NONE);
        button.setText("...");
        gridData = new GridData();
        gridData.heightHint = 14;
        button.setLayoutData(gridData);
        button.addSelectionListener(new SelectionListener() {
            
            @Override
            public void widgetSelected(SelectionEvent event) {
                openEditorDialog();
            }
            
            @Override
            public void widgetDefaultSelected(SelectionEvent event) {
            }
        });
        return panel;
    }

    /**
     * Opens the constraint editor dialog.
     */
    private void openEditorDialog() {
        ConstraintsEditorDialog dlg = new ConstraintsEditorDialog(shell, context, text.getText());
        if (Window.OK == dlg.open()) {
            setValue(dlg.getConstraintsText());
        }
    }

    @Override
    protected Object doGetValue() {
        return text.getText();
    }

    @Override
    protected void doSetFocus() {
        text.setFocus();
    }

    @Override
    protected void doSetValue(Object value) {
        if (null != text && null != value) {
            text.setText(value.toString());
            super.doSetValue(value);
        }
    }

    @Override
    public IDecisionVariable getVariable() {
        return context;
    }

}
