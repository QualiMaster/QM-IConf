package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.values.ConstraintValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.persistency.StringProvider;

/**
 * Implements a specific editor for collections of constraints, i.e., user constraints. No cell editor will be
 * provided by now. This is just a very basic editor without contained cell editors, means for adding/removing rows.
 * 
 * @author Holger Eichelberger
 */
public class ConstraintsEditor extends AbstractTableEditor {

    public static final IEditorCreator CREATOR = new IEditorCreator() {
        
        @Override
        public Control createEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new ConstraintsEditor(config, variable, parent);
        }
        
        @Override
        public CellEditor createCellEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
            return new ConstraintsCellEditor(config, variable, parent);
        }
        
    };
    
    private MenuManager manager;
    private UIConfiguration config;
    private TableViewer tableViewer;
    private ContainerValue variableValue;

    /**
     * Creates an editor instance.
     * 
     * @param config the UI configuration
     * @param variable the variable to edit
     * @param parent the parent composite
     */
    private ConstraintsEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(config, variable, parent, SWT.NULL);
        this.config = config;
        setLayout(new FillLayout());
        tableViewer = setTableViewer(createTableViewer(this, true));
        
        IDatatype type = DerivedDatatype.resolveToBasis(variable.getDeclaration().getType());
        if (1 == type.getGenericTypeCount()) {
            IDatatype contained = type.getGenericType(0);
            if (ConstraintType.TYPE.isAssignableFrom(contained)) {
                Value val = variable.getValue();
                if (val instanceof ContainerValue) {
                    variableValue = (ContainerValue) val;
                    fillTable(toConstraints((ContainerValue) val), tableViewer);
                }
            }
        }
        
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
                    ConstraintEditorDialog dlg = new ConstraintEditorDialog(getShell(), getVariable(), item.getText());
                    if (ConstraintEditorDialog.OK == dlg.open()) {
                        String constraint = dlg.getConstraintText();
                        if (null != variableValue) {
                            Value val = createConstraintValue(constraint);
                            if (null != val) {
                                try {
                                    int selIndex = tableViewer.getTable().getSelectionIndex();
                                    checkForDuplicates(val, selIndex);
                                    variableValue.setValue(selIndex, val);

                                    item.setText(constraint);
                                    tableViewer.refresh(item);
                                    setDirty();
                                } catch (ValueDoesNotMatchTypeException e) {
                                    exceptionDialog(e);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * Creates a constraint value from the given constraint text.
     * 
     * @param constraint the constraint text to create the value from
     * @return the constraint value or <b>null</b> in case of failures (displayed as a message)
     */
    private Value createConstraintValue(String constraint) {
        Value result = null;
        ConstraintSyntaxTree cst;
        try {
            cst = ModelUtility.INSTANCE.createExpression(constraint, getVariable().getDeclaration());
            result = ValueFactory.createValue(ConstraintType.TYPE, cst);
        } catch (CSTSemanticException e) {
            exceptionDialog(e);
        } catch (ConstraintSyntaxException e) {
            exceptionDialog(e);
        } catch (ValueDoesNotMatchTypeException e) {
            exceptionDialog(e);
        }
        return result;
    }
    
    /**
     * Adds a new constraint.
     */
    private void addConstraint() {
        IDecisionVariable variable = getVariable();
        ConstraintEditorDialog dlg = new ConstraintEditorDialog(getShell(), variable, null);
        if (ConstraintEditorDialog.OK == dlg.open()) {
            String constraint = dlg.getConstraintText();
            if (constraint.length() > 0) {
                try {
                    if (null == variableValue) {
                        variableValue = (ContainerValue) ValueFactory.createValue(variable.getDeclaration().getType(), 
                            ValueFactory.EMPTY);
                        variable.setValue(variableValue, AssignmentState.ASSIGNED);
                    }
                    if (null != variableValue) {
                        Value val = createConstraintValue(constraint);
                        checkForDuplicates(val, -1);
                        variableValue.addElement(val);

                        TableItem item = new TableItem(tableViewer.getTable(), SWT.NULL);
                        item.setText(constraint);
                        setDirty();
                    }
                } catch (ValueDoesNotMatchTypeException e) {
                    exceptionDialog(e);
                } catch (ConfigurationException e) {
                    exceptionDialog(e);
                }
            }
        }
    }
    
    /**
     * Opens an exception error dialog.
     * 
     * @param th the throwable to use for displaying the message
     */
    private void exceptionDialog(Throwable th) {
        Dialogs.showErrorDialog("Constraint error", th.getMessage());
    }
    
    /**
     * Checks for duplicates within {@link #variableValue}. This is a workaround for the problem that container
     * values currently do not check for duplicate values while adding/setting values.
     * 
     * @param value the new value
     * @param allowedPos an allowed position in case of value replacements, may be negative in order to indicate that
     *   there is no such position
     * @throws ValueDoesNotMatchTypeException in case of a duplicate in a Set
     */
    private void checkForDuplicates(Value value, int allowedPos) throws ValueDoesNotMatchTypeException {
        if (null != variableValue && Set.TYPE.isAssignableFrom(getVariable().getDeclaration().getType())) {
            int index = variableValue.indexOf(value);
            if (index >= 0) {
                if (allowedPos < 0 || index != allowedPos) {
                    throw new ValueDoesNotMatchTypeException("Duplicate constraint is not allowed", 
                        ValueDoesNotMatchTypeException.NOT_ALLOWED_VALUE_STRUCTURE);
                }
            }
        }
    }
    
    /**
     * Indicates a change of data and sets the (parent) editor dirty.
     */
    private void setDirty() {
        config.getParent().setDirty();
    }

    /**
     * Removes the selected constraint.
     */
    private void removeSelectedConstraint() {
        int index = tableViewer.getTable().getSelectionIndex();
        if (index >= 0) {
            if (null != variableValue) {
                variableValue.removeElement(index);

                tableViewer.getTable().remove(index);
                setDirty();
            }
        }
    }
    
    @Override
    public void dispose() {
        manager.dispose();
        super.dispose();
    }
    
    /**
     * Creates the table viewer for a set of constraints. The resulting table viewer will support single selection
     * only.
     * 
     * @param parent the parent composite
     * @param columnTitle display the column title
     * @return the table viewer
     */
    static TableViewer createTableViewer(Composite parent, boolean columnTitle) {
        TableViewer tableViewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        Table table = tableViewer.getTable();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(columnTitle);
        table.setLinesVisible(true);

        TableColumn objectColumn = new TableColumn(table, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 500, true));
        if (!columnTitle) {
            objectColumn.setText("constraint"); 
        }
        objectColumn.setResizable(true);
        return tableViewer;
    }
    
    /**
     * Fills the given table with constraints.
     * 
     * @param constraints the constraints in string representation
     * @param table the table to be filled
     */
    static void fillTable(String[] constraints, TableViewer table) {
        Table tab = table.getTable();
        for (int c = 0; c < constraints.length; c++) {
            TableItem item = new TableItem(tab, SWT.NULL);
            item.setText(constraints[c]);
        }
    }
    
    /**
     * Returns the contents of the given table in terms of Constraint string representations.
     * 
     * @param table the table to be turned into strings
     * @return the string representations
     */
    static String[] getConstraints(TableViewer table) {
        Table tab = table.getTable();
        String[] result = new String[tab.getItemCount()];
        for (int i = 0; i < result.length; i++) {
            result[i] = tab.getItem(i).getText();
        }
        return result;
    }

    /**
     * Turns a constraint container into string representations.
     * 
     * @param constraints the constraints
     * @return the string representations
     */
    String[] toConstraints(ContainerValue constraints) {
        List<String> tmp = new ArrayList<String>();
        AbstractVariable decl = getVariable().getDeclaration();
        IModelElement context = decl;
        while (null != context && !(context instanceof Compound)) {
            context = context.getParent();
        }
        if (!(context instanceof Compound)) {
            context = decl;
        }
        for (int t = 0; t < constraints.getElementSize(); t++) {
            Value cVal = constraints.getElement(t);
            if (cVal instanceof ConstraintValue) {
                ConstraintValue val = (ConstraintValue) cVal;
                tmp.add(StringProvider.toIvmlString(val.getValue(), context));
            }
        }
        return tmp.toArray(new String[tmp.size()]);
    }

    @Override
    public void refresh() {
        tableViewer.refresh();
    }
}
