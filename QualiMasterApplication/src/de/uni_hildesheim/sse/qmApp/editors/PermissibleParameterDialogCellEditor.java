package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.persistency.StringProvider;

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
        
        private Set<ConstraintSyntaxTree> selectedConstraints;

        /**
         * Single constructor.
         * @param parent The shell which will be the parent of the new instance
         */
        private SelectionDialog(Shell parent) {
            super(parent);
            selectedConstraints = new HashSet<>();
        }

        /**
         * Opens the dialog.
         * @return The selected constraints (references) as parseable Strings.
         */
        private List<String> open() {
            Shell parent = getParent();
            Shell dialog = new Shell(parent, SWT.SHELL_TRIM | SWT.APPLICATION_MODAL);
            dialog.setSize(100, 100);
            dialog.setText("Permissible Parameters for " + variable.getDeclaration().getName());
            dialog.setLayout(new RowLayout());
            for (int i = 0, end = cstValues.size(); i < end; i++) {
                final ConstraintSyntaxTree cstValue = cstValues.get(i);
                Button checkBox = new Button(dialog, SWT.CHECK);
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
            
            dialog.open();
            Display display = parent.getDisplay();
            while (!dialog.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
            
            List<String> selection = new ArrayList<>();
            for (ConstraintSyntaxTree selectedCST : selectedConstraints) {
                String parseableCSTString = StringProvider.toIvmlString(selectedCST);
                selection.add(parseableCSTString);
            }
            
            return selection;
        }
    }

    private List<ConstraintSyntaxTree> cstValues;
    private String[] labels;
    private IDecisionVariable variable;

    /**
     * Single constructor for this class.
     * @param variable The variable to be configured by this cell editor.
     * @param parent The parent control (should be the Tree editor).
     * @param labels The human readable values.
     * @param cstValues The possible values (has handled in IVML), must be the same amount as <tt>labels</tt>.
     */
    PermissibleParameterDialogCellEditor(IDecisionVariable variable, Composite parent, String[] labels,
        List<ConstraintSyntaxTree> cstValues) {
        
        super(parent);
        this.cstValues = cstValues;
        this.labels = labels;
        this.variable = variable;
    }

    @Override
    protected Object openDialogBox(Control cellEditorWindow) {
        SelectionDialog dialog = new SelectionDialog(cellEditorWindow.getShell());
        return dialog.open();
    }

}
