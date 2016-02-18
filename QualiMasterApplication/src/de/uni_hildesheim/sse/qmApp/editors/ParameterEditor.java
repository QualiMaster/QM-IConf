package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.DisplayNameProvider;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.DerivedDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.datatypes.TypeQueries;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * Implements a specific editor for collections of compounds. No cell editor
 * will be provided by now. The tables content can be modified by using provided
 * CellEditors. Via provided context-menus parameters can be added, removed.
 * Moreover the whole table can be cleared.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public class ParameterEditor extends AbstractContainerOfCompoundsTableEditor {

    public static final IEditorCreator CREATOR = new IEditorCreator() {

        @Override
        public Control createEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return new ParameterEditor(config, variable, parent);
        }

        @Override
        public CellEditor createCellEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return null;
        }
        
    };

    private static final int DATAINDEX_NAME = 0;
    private static final int DATAINDEX_TYPE = 1;
    private static final int DATAINDEX_VALUE = 2;
    
    private static final int DATAINDEX_COUNT = 3;

    
    private static final EASyLogger LOGGER = EASyLoggerFactory.INSTANCE
            .getLogger(ParameterEditor.class, Activator.PLUGIN_ID);

    // Fields needed for comparison with properties in CellEditorProvider
    private static final String NAME = "name";
    private static final String TYPE = "Type";
    private static final String VALUE = "value";

    // Properties -> CellEditors
    private static final String[] PROPS = {NAME, TYPE, VALUE};

    private static final Map<String, String> IVML_TO_DISPLAY = new HashMap<String, String>();
    private static final Map<String, String> DISPLAY_TO_IVML = new HashMap<String, String>();
    private static final List<String> TYPE_LIST = new ArrayList<String>();

    static {
        addNameMapping(QmConstants.TYPE_INTEGERPARAMETER, "INTEGER");
        addNameMapping(QmConstants.TYPE_BOOLEANPARAMETER, "BOOLEAN");
        addNameMapping(QmConstants.TYPE_REALPARAMETER, "REAL");
        addNameMapping(QmConstants.TYPE_STRINGPARAMETER, "STRING");
        addNameMapping(QmConstants.TYPE_LONGPARAMETER, "LONG");
    }

    // List which covers the ParameterObjects which are presented in table
    private ArrayList<ParameterObject> valueList = new ArrayList<ParameterObject>();
    private TableViewer tableViewer;
    private ContainerValueAccessor accessor = new ContainerValueAccessor();
    // don't store any model elements here (dynamic model change)
    
    /**
     * Wrapper-class for parameter-objects. Thus the already existing
     * functionalities can be used in order to populate the parameterList with
     * {@link ParameterObject}s and use LabelProviders, ContentProviders instead
     * of creating TableItems directly in the table.
     * 
     * @author Niko Nowatzki
     */
    public class ParameterObject extends TableObject {
        
        private String paraValue;
        
        /**
         * Constructor for a parameter.
         * 
         * @param type
         *            parameters type.
         * @param name
         *            name of the tuple.
         * @param paraValue
         *            the parameter default value.
         * @param accessor
         *            the value accessor
         */
        public ParameterObject(String name, String type, String paraValue, IValueAccessor accessor) {
            super(name, type, accessor);
            this.paraValue = paraValue;
        }

        // Set type of parameterobject
        @Override
        public void setType(String newType) {
            super.setType(newType);
            if (null != getCompoundValue()) {
                String targetTypeName = DISPLAY_TO_IVML.get(newType);
                if (null != targetTypeName) {
                    Project prj = getProject();
                    try {
                        IDatatype targetType = ModelQuery.findType(prj, targetTypeName, null);
                        CompoundValue cValue = getCompoundValue();
                        if (!TypeQueries.sameTypes(cValue.getType(), targetType)) {
                            Value tValue = ValueFactory.createValue(targetType, ValueFactory.EMPTY);
                            if (tValue instanceof CompoundValue) {
                                ((CompoundValue) tValue).configureValue(QmConstants.SLOT_PARAMETER_NAME, 
                                    cValue.getNestedValue(QmConstants.SLOT_PARAMETER_NAME));
                            }
                            ContainerValue container = getContainer();
                            int pos = container.indexOf(cValue);
                            if (pos >= 0) {
                                container.setValue(pos, tValue);
                            } else {
                                container.addElement(tValue);
                            }
                        }
                    } catch (ModelQueryException e) {
                        LOGGER.exception(e);
                    } catch (ValueDoesNotMatchTypeException e) {
                        LOGGER.exception(e);
                    }
                }
            }
        }
        
        /**
         * Returns the parameter value.
         * @return the parameter value
         */
        public String getParaValue() {
            return paraValue;
        }
        /**
         * Sets the parameter value.
         * @param paraValue the parameter value
         */
        public void setParaValue(String paraValue) {
            this.paraValue = paraValue;
            setValue(getCompoundValue(), QmConstants.SLOT_PARAMETER_DEFAULTVALUE, paraValue);
        }
    }
   

    /**
     * Creates an editor instance.
     * 
     * @param config
     *            the UI configuration
     * @param variable
     *            the variable to edit
     * @param parent
     *            the parent composite
     */
    private ParameterEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(config, variable, parent, SWT.NULL);
        setLayout(new FillLayout());
        tableViewer = setTableViewer(new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
        Table table = tableViewer.getTable();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        MenuManager popManager = new MenuManager();

        // Add new row
        IAction menuAction = new NewRowAction();
        popManager.add(menuAction);
        Menu menu = popManager.createContextMenu(table);
        table.setMenu(menu);

        // Delete row
        IAction deleteAction = new DeleteRowAction();
        popManager.add(deleteAction);

        // Clear table
        IAction clearAction = new ClearTableAction();
        popManager.add(clearAction);

        Compound compound = getCompound();
        if (null != compound) {
            createTemporaryModel(compound);
            createColumns(compound, table, layout);
            ContainerValue container = getContainer();
            if (null != container) {
                createRows(container, table, compound);
            }
        }

        // Set providers
        tableViewer.setLabelProvider(new ParamEditorLabelProvider());
        tableViewer.setContentProvider(new ParamEditorContentProvider());

        tableViewer.setColumnProperties(PROPS);

        // Set input
        tableViewer.setInput(valueList);
    }
    
    /**
     * Refreshes the TableViewer and rebuilds the rows.
     */
    public void refresh() {
        ContainerValue container = getContainer();
        if (null != tableViewer && null != container) {
            createRows(container, tableViewer.getTable(), getCompound());
            tableViewer.refresh();
        }
    }

    @Override
    protected Compound getCompound() {
        Compound result = null;
        IDatatype type = DerivedDatatype.resolveToBasis(getVariable().getDeclaration().getType());
        if (1 == type.getGenericTypeCount()) {
            IDatatype contained = type.getGenericType(0);
            Project prj = getProject();
            try {
                IDatatype pType = ModelQuery.findType(prj, QmConstants.TYPE_PARAMETER, null);
                if (pType.isAssignableFrom(contained)) {
                    result = (Compound) contained;
                }
            } catch (ModelQueryException e) {
                LOGGER.exception(e);
            }
        }
        return result;
    }
    
    @Override
    protected ContainerValue getContainer() {
        ContainerValue result = null;
        Value val = getVariable().getValue();
        if (val instanceof ContainerValue) {
            result = (ContainerValue) val;
        }
        return result;
    }
    
    /**
     * Returns the default parameter type.
     * 
     * @return the default parameter type
     */
    private IDatatype getDefaultParameterType() {
        IDatatype result;
        try {
            result = ModelQuery.findType(getProject(), QmConstants.TYPE_INTEGERPARAMETER, null);
        } catch (ModelQueryException e) {
            LOGGER.exception(e);
            result = StringType.TYPE; // just as a fallback
        }
        return result;
    }

    /**
     * LabelProvider for tuple-tables.
     * 
     * @author Niko Nowatzki
     */
    private class ParamEditorLabelProvider implements ITableLabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            ParameterObject elem = (ParameterObject) element;
            switch (columnIndex) {
            case DATAINDEX_NAME:
                result = elem.getName();
                break;
            case DATAINDEX_TYPE:
                result = elem.getType();
                break;
            case DATAINDEX_VALUE:
                result = elem.getParaValue();
                break;
            default:
                result = "?";
                break;
            }
            return result;
        }
    }

    /**
     * ContentProvider for tuples-table.
     * 
     * @author Niko Nowatzki
     */
    private class ParamEditorContentProvider implements
            IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            // Do something, thus operations on tables are effective with
            // contentprovider
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return valueList.toArray();
        }
    }

    /**
     * Action adds new row to table.
     * 
     * @author Niko Nowatzki
     */
    private class NewRowAction extends Action {
        /**
         * Constructor.
         */
        public NewRowAction() {
            super("Add a new parameter");
        }

        /**
         * Perfrom action to add a new row to the table.
         */
        public void run() {
            Compound compound = getCompound();
            ContainerValue container = getContainer();
            if (null == container && null != compound) {
                try {
                    IDecisionVariable variable = getVariable();
                    container = (ContainerValue) ValueFactory.createValue(variable.getDeclaration().getType(),
                        (Object[]) null);
                    variable.setValue(container, AssignmentState.ASSIGNED);
                } catch (ValueDoesNotMatchTypeException e) {
                    LOGGER.exception(e);
                } catch (ConfigurationException e) {
                    LOGGER.exception(e);
                }
            }
            if (null != container) {
                try {
                    IDatatype defaultParameterType = getDefaultParameterType();
                    CompoundValue val = (CompoundValue) ValueFactory.createValue(defaultParameterType, (Object[]) null);
                    container.addElement(val);
                    String displayTypeName = IVML_TO_DISPLAY.get(defaultParameterType.getType().getName());
                    if (null == displayTypeName) {
                        displayTypeName = "?";
                    }
                    ParameterObject obj = new ParameterObject(getCompoundSlot(val, NAME), displayTypeName,
                        getCompoundSlot(val, QmConstants.SLOT_INTEGERPARAMETER_DEFAULTVALUE), accessor);
                    accessor.associate(obj, container.getElementSize() - 1);
                    valueList.add(obj);
                    tableViewer.refresh();
                    notifyDirty();
                } catch (ValueDoesNotMatchTypeException e) {
                    LOGGER.exception(e);
                }
            }
        }
    }

    /**
     * Action deletes table-item.
     * 
     * @author Niko Nowatzki
     */
    private class DeleteRowAction extends Action {
        /**
         * Constructor.
         */
        public DeleteRowAction() {
            super("Delete this parameter");
        }

        /**
         * Perfrom Action by removing row.
         */
        public void run() {
            int selectedIndex = tableViewer.getTable().getSelectionIndex();
            if (selectedIndex >= 0) {
                Compound compound = getCompound();
                ContainerValue container = getContainer();
                container.removeElement(selectedIndex);
                accessor.dissociate(selectedIndex);
                valueList.clear();
                createRows(container, tableViewer.getTable(), compound);
                tableViewer.refresh();
                notifyDirty();
            }
        }
    }

    /**
     * Action clears whole table.
     * 
     * @author Niko Nowatzki
     */
    private class ClearTableAction extends Action {
        /**
         * Constructor.
         */
        public ClearTableAction() {
            super("Clear parameters");
        }

        /**
         * Perform action by clearing table.
         */
        public void run() {
            // Clear table
            Compound compound = getCompound();
            ContainerValue container = getContainer();
            container.clear();
            valueList.clear();
            createRows(container, tableViewer.getTable(), compound);
            tableViewer.refresh();
            notifyDirty();
        }
    }

    /**
     * Creates the columns.
     * 
     * @param compound
     *            the compound containing the data for the columns
     * @param table
     *            the table to be filled
     * @param layout
     *            the layout of <code>tab</code> to be modified as a side effect
     */
    private void createColumns(Compound compound, Table table, TableLayout layout) {
        TableViewerColumn tableViewerColumn;

        for (int e = 0; e < compound.getInheritedElementCount(); e++) {
            DecisionVariableDeclaration decl = compound.getInheritedElement(e);
            if (display(decl)) {
                String name = DisplayNameProvider.getInstance().getDisplayName(
                        decl);
                if (null == name) {
                    name = decl.getName();
                }

                if (name.equals(QmConstants.SLOT_PARAMETER_NAME)) {
                    tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                    layout.addColumnData(new ColumnWeightData(3, 100, true));

                    tableViewerColumn.getColumn().setText(name);
                    tableViewerColumn.getColumn().setResizable(true);

                    tableViewerColumn.setEditingSupport(new NameEditingSupport(tableViewer));
                }
            }
        }

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 100, true));
        tableViewerColumn.getColumn().setText("type");
        tableViewerColumn.getColumn().setResizable(true);
        tableViewerColumn.setEditingSupport(new ParameterTypeEditingSupport(tableViewer));

        // add the colum "value"
        TableViewerColumn tableViewerValueColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 100, true));
        tableViewerValueColumn.getColumn().setText("default value");
        tableViewerValueColumn.getColumn().setResizable(true);
        tableViewerValueColumn.setEditingSupport(new ParameterValueEditingSupport(tableViewer));
    }

    /**
     * Creates the rows.
     * 
     * @param cVal
     *            the container value to be turned into rows
     * @param table
     *            the table to be modified
     * @param compound
     *            the compound type representing the contained elements of the
     *            container
     */
    private void createRows(ContainerValue cVal, Table table, Compound compound) {
        int rows = cVal.getElementSize();
        //int cols = table.getColumnCount();
        for (int r = 0; r < rows; r++) {
            Value eVal = cVal.getElement(r);
            String[] row = new String[DATAINDEX_COUNT];
            Arrays.fill(row, "");
            String typeName = "";
            String value = null;
            if (eVal instanceof CompoundValue) {
                CompoundValue v = (CompoundValue) eVal;
                typeName = v.getType().getName();
                for (int e = 0; e < compound.getInheritedElementCount(); e++) {
                    DecisionVariableDeclaration decl = compound.getInheritedElement(e);
                    if (display(decl)) {
                        String fieldName = decl.getName();
                        if (QmConstants.SLOT_PARAMETER_NAME.equals(fieldName)) {
                            row[DATAINDEX_NAME] = getCompoundSlot(v, decl.getName());
                        }
                    }
                }
                // get the parameter value for the VALUE column
                value = getCompoundSlot(v, QmConstants.SLOT_PARAMETER_DEFAULTVALUE);
            }
            
            String displayTypeName = IVML_TO_DISPLAY.get(typeName);
            if (null == displayTypeName) {
                displayTypeName = "?";
            }
            row[DATAINDEX_TYPE] = displayTypeName;
            row[DATAINDEX_VALUE] = value;
            
            // Add parameter to list.
            ParameterObject param = new ParameterObject(row[0], row[1], row[2], accessor);
            accessor.associate(param, r);
            valueList.add(param);
        }
    }

    /**
     * CellProvider makes cells of table editable.
     * 
     * @author Niko Nowatzki
     */
    class TableCellModifier implements ICellModifier {

        @SuppressWarnings("unused")
        private Viewer viewer;

        /**
         * Constructor for a TableCellProvider.
         * 
         * @param viewer
         *            The given viewer we work on.
         */
        public TableCellModifier(Viewer viewer) {
            this.viewer = viewer;
        }

        /**
         * Returns whether the property can be modified.
         * 
         * @param element
         *            the element
         * @param property
         *            the property
         * @return boolean
         */
        public boolean canModify(Object element, String property) {
            // Allow editing of all values
            return true;
        }

        /**
         * Returns the value for the property.
         * 
         * @param element
         *            the element
         * @param property
         *            the property
         * @return Object
         */
        public Object getValue(Object element, String property) {

            String toReturn;

            ParameterObject param = (ParameterObject) element;

            if (ParameterEditor.NAME.equals(property)) {
                toReturn = param.getName();
            } else if (ParameterEditor.TYPE.equals(property)) {
                toReturn = param.getType();
            } else if (ParameterEditor.VALUE.equals(property)) {
                toReturn = param.getParaValue();
            } else {
                toReturn = null;
            }

            return toReturn;
        }

        @Override
        public void modify(Object element, String property, Object value) {

            if (element instanceof Item) {
                element = ((Item) element).getData();
            }

            ParameterObject param = (ParameterObject) element;

            if (ParameterEditor.NAME.equals(property)) {
                param.setName((String) value);
            } else if (ParameterEditor.TYPE.equals(property)) {
                param.setType((String) value);
            } else if (ParameterEditor.VALUE.equals(property)) {
                param.setParaValue((String) value);
            }
        }
    };

    @Override
    protected void augmentTemporaryModel(Project tmpModel) {
        // here we do not have a type field... just create one
        Project prj = getProject();
        try {
            IDatatype targetType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDTYPE, null);
            DecisionVariableDeclaration tmpDecl = new DecisionVariableDeclaration(SLOT_TYPE, targetType, tmpModel);
            tmpModel.add(tmpDecl);
        } catch (ModelQueryException e) {
            LOGGER.exception(e);
        }
    }

    /**
     * Editing support for the parameter default value.
     * 
     * @author qin
     * 
     */
    protected class ParameterValueEditingSupport extends EditingSupport {
        private final TableViewer viewer;

        /**
         * Creates the editing support of the parameter default value.
         * 
         * @param viewer
         *            the table viewer
         */
        public ParameterValueEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            CellEditor result = new TextCellEditor(viewer.getTable());
            return result;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            ParameterObject object = (ParameterObject) element;
            return object.getParaValue();
        }

        @Override
        protected void setValue(Object element, Object value) {
            ParameterObject object = (ParameterObject) element;
            if (value != null) {
                object.setParaValue(value.toString());
                viewer.update(element, null);
            }

        }

    }

    /**
     * Editing Support for type.
     * 
     * @author Niko
     */
    protected class ParameterTypeEditingSupport extends EditingSupport {

        private final TableViewer viewer;

        /**
         * Constructor.
         * 
         * @param viewer
         *            the table viewer
         */
        public ParameterTypeEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            String[] items = new String[TYPE_LIST.size()];
            TYPE_LIST.toArray(items);
            return new ComboBoxCellEditor(viewer.getTable(), items);
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            Object result = -1;
            if (element instanceof ParameterObject) {
                ParameterObject po = (ParameterObject) element;
                result = TYPE_LIST.indexOf(po.getType());
            }
            return result;
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value instanceof Integer && element instanceof ParameterObject) {
                int pos = (Integer) value;
                if (0 <= pos && pos < TYPE_LIST.size()) {
                    ParameterObject po = (ParameterObject) element;
                    po.setType(TYPE_LIST.get(pos));
                    viewer.update(element, null);
                }
            }
        }

    }
    
    /**
     * Returns the display name for a given parameter type.
     * 
     * @param ivmlType the IVML type
     * @return the display name (may be <b>null</b> if not known)
     */
    public static final String getDisplayNameForParameterType(String ivmlType) {
        return IVML_TO_DISPLAY.get(ivmlType);
    }

    /**
     * Returns the parameter type name for a given display name.
     * 
     * @param displayName the display name
     * @return the parameter type name (may be <b>null</b> if not known)
     */
    public static final String getParameterTypeForDisplayName(String displayName) {
        return DISPLAY_TO_IVML.get(displayName);
    }

    /**
     * Stores a mapping between the IVML types of the compounds and the enum
     * literals representing them.
     * 
     * @param ivmlType
     *            the IVML type name
     * @param enumLiteral
     *            the enum literal in FieldTypes
     */
    private static final void addNameMapping(String ivmlType, String enumLiteral) {
        IVML_TO_DISPLAY.put(ivmlType, enumLiteral);
        DISPLAY_TO_IVML.put(enumLiteral, ivmlType);
        TYPE_LIST.add(enumLiteral);
    }

}
