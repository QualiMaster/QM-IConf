package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;

/**
 * Implements an (cell) editor dialog for modifying a set of constraints. Constraints 
 * are given in IVML syntax separated by {@link #SEPARATOR}.
 * 
 * @author Holger Eichelberger
 */
class ConstraintsEditorDialog extends Dialog {

    static final String SEPARATOR = PipelineDiagramUtils.CONSTRAINT_SEPARATOR;
    
    private String constraints;
    private TableViewer tableViewer;
    private IDecisionVariable context;
    private MenuManager manager;

    /**
     * Creates the dialog.
     * 
     * @param context the IVML model element the constraint shall be defined in
     * @param parentShell the parent shell
     * @param constraints the constraints as text (IVML syntax, separated by ";")
     */
    ConstraintsEditorDialog(Shell parentShell, IDecisionVariable context, String constraints) {
        super(parentShell);
        this.constraints = constraints;
        this.context = context;
    }
    
    /**
     * Splits the (possibly multiple) <code>constraints</code> into individual strings.
     * 
     * @param constraints the constraints to split (IVML syntax, separated by {@link #SEPARATOR})
     * @return the individual constraints as array
     */
    static String[] splitConstraints(String constraints) {
        return constraints.split(SEPARATOR);
    }
    
    /**
     * Combines individual constraints given as Strings in IVML syntax.
     * 
     * @param constraints the constraints to be combined 
     * @return the combined constraints string (using {@link #SEPARATOR} as separator)
     */
    static String combineConstraints(String[] constraints) {
        StringBuilder tmp = new StringBuilder();
        for (int c = 0; c < constraints.length; c++) {
            String constraint = constraints[c].trim();
            if (constraint.length() > 0) {
                if (tmp.length() > 0) {
                    tmp.append(SEPARATOR);
                }
                tmp.append(constraint);
            }
        }
        return tmp.toString();
    }
    
    /**
     * Returns the constraints as text.
     * 
     * @return the constraints as text (IVML syntax, separated by ";")
     */
    public String getConstraintsText() {
        return constraints;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        Composite tablePanel = new Composite(composite, SWT.NONE);
        tablePanel.setLayout(new GridLayout(1, false));
        Label label = new Label(tablePanel, SWT.NONE);
        label.setText("constraints:");
        tableViewer = ConstraintsEditor.createTableViewer(tablePanel, false);
        GridData gridData = new GridData();
        gridData.widthHint = 400;
        gridData.heightHint = 150;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        tableViewer.getTable().setLayoutData(gridData);
        ConstraintsEditor.fillTable(splitConstraints(constraints), tableViewer);

        Composite buttonPanel = new Composite(composite, SWT.NONE);
        buttonPanel.setLayout(new GridLayout(1, false));
        Button add = new Button(buttonPanel, SWT.PUSH);
        add.setText("Add constraint");
        add.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent evt) {
                addConstraint();
            }
            
        });
        Button remove = new Button(buttonPanel, SWT.PUSH);
        remove.setText("Remove constraint");
        remove.addSelectionListener(new SelectionAdapter() {
            
            @Override
            public void widgetSelected(SelectionEvent evt) {
                removeSelectedConstraint();
            }
            
        });

        tableViewer.addDoubleClickListener(new IDoubleClickListener() {
            
            @Override
            public void doubleClick(DoubleClickEvent event) {
                // IStructuredSelection does not work well until content provider is used
                if (tableViewer == event.getSource()) {
                    editSelectedConstraint();
                }
            }
        });

        manager = new MenuManager();
        tableViewer.getControl().setMenu(manager.createContextMenu(tableViewer.getControl()));
        manager.add(new Action("Add constraint", null) {
            
            @Override
            public void run() {
                addConstraint();
            }
        });
        manager.add(new Action("Remove constraint", null) {
            @Override
            public void run() {
                removeSelectedConstraint();
            }  
        });
        
        return composite;
    }

    /**
     * Adds a new constraint.
     */
    private void addConstraint() {
        ConstraintEditorDialog dlg = new ConstraintEditorDialog(getShell(), context, null);
        if (ConstraintEditorDialog.OK == dlg.open()) {
            String constraint = dlg.getConstraintText();
            if (constraint.length() > 0) {
                TableItem item = new TableItem(tableViewer.getTable(), SWT.NULL);
                item.setText(constraint);
            }
        }
    }
    
    /**
     * Edits the selected constraint.
     */
    private void editSelectedConstraint() {
        TableItem[] selected = tableViewer.getTable().getSelection();
        if (selected.length > 0) {
            final TableItem item = selected[0]; // at max one from table
            getShell().getDisplay().asyncExec(new Runnable() {

                @Override
                public void run() {
                    ConstraintEditorDialog dlg = new ConstraintEditorDialog(getShell(), context, item.getText());
                    if (ConstraintEditorDialog.OK == dlg.open()) {
                        
                        String constraint = dlg.getConstraintText();
                        item.setText(constraint);
                        tableViewer.refresh(item);
                    }
                }
            });
        }
    }

    /**
     * Removes the selected constraint.
     */
    private void removeSelectedConstraint() {
        int index = tableViewer.getTable().getSelectionIndex();
        if (index >= 0) {
            tableViewer.getTable().remove(index);
        }
    }
    
    @Override
    protected void okPressed() {
        constraints = combineConstraints(ConstraintsEditor.getConstraints(tableViewer));
        super.okPressed();
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit constraints");
        DialogsUtil.centerShell(newShell);
    }

    @Override
    protected Point getInitialSize() {
        return new Point(650, 300);
    }

    @Override
    public boolean close() {
        manager.dispose();
        return super.close();
    }

}
