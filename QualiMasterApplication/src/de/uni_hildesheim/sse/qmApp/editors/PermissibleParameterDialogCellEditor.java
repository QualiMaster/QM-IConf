package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.persistency.StringProvider;
import pipeline.FamilyElement;
import pipeline.PipelineElement;
import pipeline.Sink;
import pipeline.Source;

/**
 * DialogCellEditor for selecting permissible parameters of a <tt>Source</tt>, <tt>Sink</tt>, or <tt>FamilyElement</tt>.
 * @author El-Sharkawy
 *
 */
class PermissibleParameterDialogCellEditor extends DialogCellEditor {

    /**
     * The dialog created by the cell editor when pressing the ... button.
     * @author El-Sharkawy
     *
     */
    private class SelectionDialog extends Dialog {
        
        private List<String> selection;
        private Set<ConstraintSyntaxTree> selectedConstraints;
        private Shell dialog;

        /**
         * Single constructor.
         * @param parent The shell which will be the parent of the new instance
         */
        private SelectionDialog(Shell parent) {
            super(parent);
            selectedConstraints = new HashSet<>();
            selection = PermissibleParameterDialogCellEditor.this.currentSelectionAsList;
        }

        /**
         * Opens the dialog.
         * @return The selected constraints (references) as parseable Strings.
         */
        private List<String> open() {
            createDialog();

            // Create checkboxes for possible values
            ScrolledComposite scrollPane = new ScrolledComposite(dialog, SWT.H_SCROLL | SWT.V_SCROLL);
            Composite chkBoxContainer = new Composite(scrollPane, SWT.NONE);
            chkBoxContainer.setLayout(new GridLayout(2, true));
            createCheckboxArea(chkBoxContainer);
            chkBoxContainer.setSize(chkBoxContainer.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            scrollPane.setContent(chkBoxContainer);
            
            // Create OK and cancel button
            Composite btnArea = new Composite(dialog, SWT.NONE);
            btnArea.setLayout(new GridLayout(2, true));
            Button btnOK = new Button(btnArea, SWT.PUSH);
            btnOK.setText("OK");
            btnOK.addSelectionListener(new SelectionListener() {
                
                @Override
                public void widgetSelected(SelectionEvent evt) {
                    selection = new ArrayList<>();
                    for (ConstraintSyntaxTree selectedCST : selectedConstraints) {
                        String parseableCSTString = StringProvider.toIvmlString(selectedCST);
                        selection.add(parseableCSTString);
                    }
                    SelectionDialog.this.dialog.close();
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent evt) {}
            });
            Button btnCancel = new Button(btnArea, SWT.PUSH);
            btnCancel.setText("Cancel");
            btnCancel.addSelectionListener(new SelectionListener() {
                
                @Override
                public void widgetSelected(SelectionEvent evt) {
                    // Keep previous selection and close dialog
                    SelectionDialog.this.dialog.close();
                }
                
                @Override
                public void widgetDefaultSelected(SelectionEvent evt) {}
            });
            btnArea.setSize(btnArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            
            openDialog();
            return selection;
        }

        /**
         * Creates the checkboxes for selection and the listeners for the checkboxes.
         * @param parent The parent composite holding the elements.
         */
        private void createCheckboxArea(Composite parent) {
            if (!cstValues.isEmpty()) {
                for (int i = 0, end = cstValues.size(); i < end; i++) {
                    final ConstraintSyntaxTree cstValue = cstValues.get(i);
                    String parseableCSTString = StringProvider.toIvmlString(cstValue);
                    boolean currentlySelected
                        = PermissibleParameterDialogCellEditor.this.currentSelectionAsSet.contains(parseableCSTString);
                    if (currentlySelected) {
                        selectedConstraints.add(cstValue);
                    }
                    
                    Button checkBox = new Button(parent, SWT.CHECK);
                    checkBox.setSelection(currentlySelected);
                    checkBox.setText(labels[i]);
                    checkBox.addSelectionListener(new SelectionAdapter() {
    
                        @Override
                        public void widgetSelected(SelectionEvent event) {
                            Button btn = (Button) event.getSource();
                            if (btn.getSelection()) {
                                selectedConstraints.add(cstValue);
                            } else {
                                selectedConstraints.remove(cstValue);
                            }
                        }
                    });
                }
            } else {
                Label info = new Label(parent, SWT.NONE);
                info.setText("The referenced element defines no parameters.");
            }
        }

        /**
         * Makes the dialog visible and runs the dialog until it is closes.
         */
        private void openDialog() {
            dialog.open();
            Display display = getParent().getDisplay();
            while (!dialog.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        }

        /**
         * Creates the dialog.
         */
        private void createDialog() {
            Shell parent = getParent();
            dialog = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
            dialog.setText("Select permissible Parameters for " + pipElement.getName());
            dialog.setLayout(new FillLayout(SWT.VERTICAL));
            dialog.setSize(750, 155);
        }
    }

    private List<ConstraintSyntaxTree> cstValues;
    private String[] labels;
    private PipelineElement pipElement;
    private List<String> currentSelectionAsList;
    private Set<String> currentSelectionAsSet;

    /**
     * Single constructor for this class.
     * @param pipElement pipeline element which is currently configured inside the editor
     *     ({@link pipeline.Source}, {@link pipeline.Sink}, or {@link pipeline.FamilyElement}).
     * @param parent The parent control (should be the Tree editor).
     * @param labels The human readable values.
     * @param cstValues The possible values (has handled in IVML), must be the same amount as <tt>labels</tt>.
     */
    PermissibleParameterDialogCellEditor(PipelineElement pipElement, Composite parent, String[] labels,
        List<ConstraintSyntaxTree> cstValues) {
        
        super(parent);
        this.cstValues = cstValues;
        this.labels = labels;
        this.pipElement = pipElement;
        if (pipElement instanceof Source) {
            currentSelectionAsList = ((Source) pipElement).getPermissibleParameters();
        } else if (pipElement instanceof Sink) {
            currentSelectionAsList = ((Sink) pipElement).getPermissibleParameters();
        } else if (pipElement instanceof FamilyElement) {
            currentSelectionAsList = ((FamilyElement) pipElement).getPermissibleParameters();
        }
        currentSelectionAsSet = new HashSet<>(currentSelectionAsList);
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        SelectionDialog dialog = new SelectionDialog(cellEditorWindow.getShell());
        return dialog.open();
    }
}
