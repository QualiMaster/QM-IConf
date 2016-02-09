package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.DisplayNameProvider;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.DerivedDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.filter.ReferenceValuesFinder;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.ReferenceValue;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.qmApp.dialogs.ConfigurationVariableSelectorDialog;
import de.uni_hildesheim.sse.qmApp.dialogs.ConfigurationVariableSelectorDialog.IVariableSelector;
import de.uni_hildesheim.sse.qmApp.dialogs.ConfigurationVariableSelectorDialog.TypeBasedVariableSelector;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * Implements a specific editor for collections of tuples. The tables content
 * can be modified by using provided CellEditors. Via provided context-menus
 * parameters can be added, removed. Moreover the whole table can be cleared.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public class TuplesEditor extends AbstractContainerOfCompoundsTableEditor {

    public static final IEditorCreator CREATOR = new IEditorCreator() {

        @Override
        public Control createEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return new TuplesEditor(config, variable, parent);
        }

        @Override
        public CellEditor createCellEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return ConfigurationTableEditorFactory.createCellEditor(config,
                    variable);
        }

    };
    
    /**
     * Defines a UI parameter for showing the key part column. The default value (even if absent) 
     * is true. Individual editors may disable the key part completely by setting this parameter to false.
     */
    public static final UIParameter SHOW_KEY_PART = new UIParameter("show key part", true);
    
    /**
     * If not already disabled via {@link #SHOW_KEY_PART}, here the name of the variable to show the key part
     * for can be given. No influence if not stated. 
     */
    public static final UIParameter SHOW_KEY_PART_VARIABLE = new UIParameter("show key part for variable", null);

    private static final EASyLogger LOGGER = EASyLoggerFactory.INSTANCE
            .getLogger(TuplesEditor.class, Activator.PLUGIN_ID);
    private static final String TUPLE_PREFIX = "$tuple_";

    // Fields needed comparison with properties in CellEditorProvider
    private static final String TUPLE = "tuple";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String KEYPART = "key";

    private static final int DATAINDEX_TUPLE_NAME = 0;
    private static final int DATAINDEX_ENTRY_KIND = 1;
    private static final int DATAINDEX_FIELD_NAME = 2;
    private static final int DATAINDEX_TYPE = 3;
    private static final int DATAINDEX_KEYPART = 4;
    
    private static final int DATAINDEX_COUNT = 5;
    
    private static final int TUPLEINDEX_CONTAINER_POS = 0;
    private static final int TUPLEINDEX_FIELD_POS = 1;
    private static final int TUPLEINDEX_COUNT = 2;
    
    // Properties -> CellEditors
    private static final String[] PROPS = {TUPLE, NAME, TYPE, KEYPART};

    // List which covers the TuplesObjects which are presented in table
    private ArrayList<TupleObject> valueList = new ArrayList<TupleObject>();

    private TableViewer tableViewer;
    private ContainerValueAccessor tupleAccessor = new ContainerValueAccessor();
    private ContainerValueAccessor fieldAccessor = new ContainerValueAccessor() {
        
        @Override
        public CompoundValue getValue(Object key) {
            CompoundValue result = null;
            Integer pos = getPosition(key);
            if (null != pos) {
                CompoundValue tuple = tupleAccessor.getValue(key);
                Value fields = tuple.getNestedValue(QmConstants.SLOT_TUPLE_FIELDS);
                if (fields instanceof ContainerValue) {
                    ContainerValue cVal = (ContainerValue) fields;
                    if (0 <= pos && pos < cVal.getElementSize()) {
                        Value tmp = cVal.getElement(pos);
                        if (tmp instanceof CompoundValue) {
                            result = (CompoundValue) tmp;
                        }
                    }
                }
            }
            return result;
        }

    };
    // don't store any model elements here (dynamic model change)

    /**
     * Wrapper-class for tuple-objects. Thus the already existing
     * functionalities can be used in order to populate the tupleList with
     * {@link TupleObject} and use LabelProviders, ContentProviders instead of
     * creating TableItems directly in the table.
     * 
     * @author Niko Nowatzki
     */
    public class TupleObject extends TableObject {

        private String tupleName;
        private Boolean keyPart;
        private IValueAccessor tupleAccessor;
        private String entryKind;

        /**
         * Constructor for a tuple.
         * 
         * @param data tuple data (sequence: tupleName, field name, field type, keyPart).
         * @param fieldAccessor the field value accessor
         * @param tupleAccessor the tuple value accessor
         */
        public TupleObject(String[] data, IValueAccessor fieldAccessor, IValueAccessor tupleAccessor) {
            super(data[DATAINDEX_FIELD_NAME], data[DATAINDEX_TYPE], fieldAccessor);
            this.tupleName = data[DATAINDEX_TUPLE_NAME];
            this.entryKind = data[DATAINDEX_ENTRY_KIND];
            this.tupleAccessor = tupleAccessor;
            this.keyPart = Boolean.parseBoolean(data[DATAINDEX_KEYPART]);
        }
        
        /**
         * Returns the entry kind.
         * 
         * @return the entry kind
         */
        public String getEntryKind() {
            return entryKind;
        }

        /**
         * Returns the tuple name.
         * 
         * @return the tuple name
         */
        public String getTupleName() {
            return tupleName;
        }

        /**
         * Get the tuples field "name" in the represented tuple.
         * 
         * @param tupleName
         *            new tuple name.
         */
        private void setTupleName(String tupleName) {
            this.tupleName = tupleName;
            setValue(getTuple(), QmConstants.SLOT_FIELD_NAME, tupleName);
        }

        /**
         * Returns the actual tuple.
         * 
         * @return the actual tuple
         */
        CompoundValue getTuple() {
            return tupleAccessor.getValue(this);
        }
        /**
         * Returns the key part.
         * @return the key part
         */
        public Boolean getKeyPart() {
            return keyPart;
        }

        /**
         * Sets the key part.
         * @param keyPart the key part
         */
        private void setKeyPart(Boolean keyPart) {
            this.keyPart = keyPart;
            setValue(getCompoundValue(), QmConstants.SLOT_FIELD_KEYPART, keyPart);
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
    private TuplesEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(config, variable, parent, SWT.NULL);
        setLayout(new FillLayout());
        tableViewer = setTableViewer(new TableViewer(this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER));
        tableViewer.refresh();
        Table table = tableViewer.getTable(); // TODO increase size to 3 lines
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        MenuManager popManager = new MenuManager();
        IAction newTupleAction = new NewTupleAction();
        popManager.add(newTupleAction);

        IAction menuAction = new NewFieldAction("Add a new field to this item (before selected field)", false);
        popManager.add(menuAction);
        Menu menu = popManager.createContextMenu(table);
        table.setMenu(menu);
        
        menuAction = new NewFieldAction("Add a new field to this item (after selected field)", true);
        popManager.add(menuAction);

        popManager.add(new DeleteFieldAction());
        popManager.add(new DeleteTupleAction());
        popManager.add(new CopyTuplesFromAction("input"));
        popManager.add(new CopyTuplesFromAction("output"));
        popManager.add(new ClearTableAction());

        Compound compound = getCompound();
        if (null != compound) {
            try {
                createTemporaryModel(compound);
                createColumns(getTupleType(), compound, table, layout);
                ContainerValue container = getContainer();
                if (null != container) {
                    createRows(container, table, compound);
                }
            } catch (ModelQueryException e) {
                LOGGER.exception(e);
            }
        }

        // Set Label- and ContentProviders thus the table gets populated.
        tableViewer.setLabelProvider(new TupleEditorLabelProvider());
        tableViewer.setContentProvider(new TupleEditorContentProvider());

        tableViewer.setColumnProperties(PROPS);

        // Set input
        tableViewer.setInput(valueList);
    }
    
    /**
     * Returns the IVML tuple type.
     * 
     * @return the tuple type
     * @throws ModelQueryException in case that the access fails
     */
    private Compound getTupleType() throws ModelQueryException {
        IDatatype tType = ModelQuery.findType(getProject(), QmConstants.TYPE_TUPLE, null);
        if (tType instanceof Compound) {
            return (Compound) tType;
        } else {
            throw new ModelQueryException("'Tuple' is not a compound", ModelQueryException.ACCESS_ERROR);
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
                IDatatype tType = getTupleType();
                if (tType.isAssignableFrom(contained)) {
                    IDatatype fType = ModelQuery.findType(prj, QmConstants.TYPE_FIELD, null);
                    result = (Compound) fType;
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

    @Override
    protected void augmentTemporaryModel(Project tmpModel) {
        Project prj = getProject();
        try {
            try {
                Project basicsCfg = ModelAccess.getModel(VariabilityModel.Configuration.BASICS);
                ProjectImport imp = new ProjectImport(basicsCfg.getName(), null);
                imp.setResolved(basicsCfg);
                tmpModel.addImport(imp);
            } catch (ModelManagementException e) {
                EASyLoggerFactory.INSTANCE.getLogger(getClass(),
                        Activator.PLUGIN_ID).exception(e);
            }
            IDatatype tType = ModelQuery.findType(prj, QmConstants.TYPE_TUPLE, null);
            DecisionVariableDeclaration decl = ((Compound) tType).getElement(QmConstants.SLOT_TUPLE_NAME);
            if (null != decl) { // legacy NPE??
                DecisionVariableDeclaration tmpDecl = new DecisionVariableDeclaration(
                        TUPLE_PREFIX + decl.getName(), decl.getType(), tmpModel);
                // take over .text information if given - this is a pseudo var
                // (name)
                String comment = ModelAccess.getDescription(decl);
                if (null == comment || 0 == comment.length()) {
                    comment = "item name";
                }
                tmpDecl.setComment(comment);
                tmpModel.add(tmpDecl);
            }
        } catch (ModelQueryException e) {
            LOGGER.exception(e);
        }
    }

    /**
     * LabelProvider for tuple-tables.
     * 
     * @author Niko Nowatzki
     */
    private class TupleEditorLabelProvider implements ITableLabelProvider {       
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
            Image image = null;            
            TupleObject elem = (TupleObject) element;
            if (columnIndex == DATAINDEX_KEYPART) {
                if (elem.getKeyPart()) {                   
                    image = IconManager.retrieveImage(IconManager.CHECKEDBOX);
                } else {
                    image = IconManager.retrieveImage(IconManager.UNCHECKEDBOX);
                }
            }
            return image;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            String result = "";
            TupleObject elem = (TupleObject) element;
            switch (columnIndex) {
            case DATAINDEX_TUPLE_NAME:
                result = elem.getTupleName();
                break;
            case DATAINDEX_ENTRY_KIND:
                result = elem.getEntryKind();
                break;
            case DATAINDEX_FIELD_NAME:
                result = elem.getName();
                break;
            case DATAINDEX_TYPE:
                result = elem.getType();
                break;
            case DATAINDEX_KEYPART: //show the image  
//                result = Boolean.toString(elem.getKeyPart());
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
    private class TupleEditorContentProvider implements
            IStructuredContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            return valueList.toArray();
        }
    }

    /**
     * Turns the selected UI index into a tuple index within the list of tuples.
     * Searches therefore the tuples according to the min/maximum field position
     * and whether the selected index lies within.
     * 
     * @param selectedIndex
     *            the selected index to search for (assumed to be valid if not
     *            negative)
     * @return an array containing the tuple index and the field index within
     *         the tuple (<b>null</b> if no matching tuple was found,
     *         <code>-1</code> if no matching was found)
     */
    private int[] getTupleIndex(int selectedIndex) {
        int containerPos = -1;
        int fieldPos = -1;
        if (selectedIndex >= 0) {
            try {
                Project prj = getProject();
                IDatatype tupleType = ModelQuery.findType(prj, QmConstants.TYPE_TUPLE, null);
                int minFieldPos = 0;
                int maxFieldPos = 0;
                ContainerValue container = getContainer();
                for (int i = 0; containerPos < 0 && i < container.getElementSize(); i++) {
                    Value value = container.getElement(i);
                    if (tupleType.isAssignableFrom(value.getType())) {
                        Value fields = ((CompoundValue) value).getNestedValue(QmConstants.SLOT_TUPLE_FIELDS);
                        IDatatype fieldsType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDS, null);
                        if (fieldsType.isAssignableFrom(fields.getType())) {
                            ContainerValue fContainer = (ContainerValue) fields;
                            maxFieldPos = minFieldPos + fContainer.getElementSize();
                            if (minFieldPos <= selectedIndex && selectedIndex < maxFieldPos) {
                                containerPos = i;
                                fieldPos = selectedIndex - minFieldPos;
                            }
                            minFieldPos = maxFieldPos;
                        }
                    }
                }
            } catch (ModelQueryException e) {
                LOGGER.exception(e);
            }
        }
        int[] result = null;
        if (containerPos >= 0) {
            result = new int[TUPLEINDEX_COUNT];
            result[TUPLEINDEX_CONTAINER_POS] = containerPos;
            result[TUPLEINDEX_FIELD_POS] = fieldPos;
        }
        return result;
    }

    /**
     * Turns the selected UI index into a tuple index within the list of tuples.
     * Searches therefore the tuples according to the min/maximum field position
     * and whether the selected index lies within.
     * 
     * @return an array containing the tuple index and the field index within
     *         the tuple (<b>null</b> if no matching tuple was found,
     *         <code>-1</code> if no matching was found)
     */
    private int[] getTupleIndex() {
        return getTupleIndex(tableViewer.getTable().getSelectionIndex());
    }

    /**
     * Action deletes table-item.
     * 
     * @author Niko Nowatzkid
     */
    private class DeleteTupleAction extends Action {

        /**
         * Constructor.
         */
        public DeleteTupleAction() {
            super("Delete this data item");
        }

        @Override
        public void run() {
            int[] containerPos = getTupleIndex();
            if (null != containerPos) {
                ContainerValue container = getContainer();
                container.removeElement(containerPos[TUPLEINDEX_CONTAINER_POS]);
                updateUi();
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
            super("Clear all items");
        }

        @Override
        public void run() {
            ContainerValue container = getContainer();
            container.clear();
            updateUi();
        }
    }

    /**
     * Updates the UI after changing the tuples.
     */
    private void updateUi() {
        Compound compound = getCompound();
        ContainerValue container = getContainer();

        valueList.clear();
        createRows(container, tableViewer.getTable(), compound);
        tableViewer.refresh();
        notifyDirty();
    }
    
    /**
     * Action for copying from another algorithm family.
     * 
     * @author Holger Eichelberger
     */
    private class CopyTuplesFromAction extends Action {

        private String tupleSource;
        
        /**
         * Creates the action instance.
         * 
         * @param tupleSource "input" or "output"
         */
        public CopyTuplesFromAction(String tupleSource) {
            super("Copy " + tupleSource + " field configuration from...");
            this.tupleSource = tupleSource;
        }
        
        /**
         * Resolves <code>typeName</code> from <code>cfg</code> and adds it to <code>types</code>.
         * 
         * @param cfg the configuration to resolve <code>typeName</code> on
         * @param typeName the name of the type to resolve
         * @param types the types list to be modified as a side effect if the type can be resolved
         */
        private void addType(Configuration cfg, String typeName, List<IDatatype> types) {
            try {
                IDatatype type = ModelQuery.findType(cfg.getProject(), typeName, Compound.class);
                if (null != type) {
                    types.add(type);
                }
            } catch (ModelQueryException e) {
                LOGGER.exception(e);
            }
        }
        
        
        @Override
        public void run() {
            Configuration cfg = VariabilityModel.Definition.TOP_LEVEL.getConfiguration();
            List<IDatatype> types = new ArrayList<IDatatype>();
            addType(cfg, QmConstants.TYPE_FAMILY, types);
            addType(cfg, QmConstants.TYPE_DATAELEMENT, types);
            IVariableSelector selector = new TypeBasedVariableSelector(types);
            ConfigurationVariableSelectorDialog dlg = new ConfigurationVariableSelectorDialog(getShell(), 
                "Select element to copy " + tupleSource + " fields from", cfg, selector);
            dlg.setInitialPattern("?");
            int res = dlg.open();
            IDecisionVariable decVar = dlg.getFirstResult();
            if (Window.OK == res && null != decVar) {
                if (decVar instanceof CompoundVariable) {
                    CompoundVariable cVar = (CompoundVariable) decVar;
                    IDecisionVariable nested = cVar.getNestedVariable(tupleSource);
                    if (null != nested) {
                        try {
                            getVariable().setValue(nested.getValue().clone(), AssignmentState.ASSIGNED);
                            updateUi();
                        } catch (ConfigurationException e) {
                            LOGGER.exception(e);
                        }
                    }
                }
            }
        }
        
    }

    /**
     * Delete selected field.
     * 
     * @author Niko Nowatzki
     */
    private class DeleteFieldAction extends Action {

        /**
         * Constructor.
         */
        public DeleteFieldAction() {
            super("Deletes the selected field");
        }

        @Override
        public void run() {
            int[] tupleIndex = getTupleIndex();
            if (null != tupleIndex && tupleIndex[TUPLEINDEX_FIELD_POS] >= 0) {
                ContainerValue container = getContainer();
                int tIndex = tupleIndex[TUPLEINDEX_CONTAINER_POS];
                Value value = container.getElement(tIndex);
                int fieldIndex = tupleIndex[TUPLEINDEX_FIELD_POS];
                Project prj = getProject();
                try {
                    IDatatype tupleType = ModelQuery.findType(prj, QmConstants.TYPE_TUPLE, null);
                    if (tupleType.isAssignableFrom(value.getType())) {
                        Value fields = ((CompoundValue) value).getNestedValue(QmConstants.SLOT_TUPLE_FIELDS);
                        IDatatype fieldsType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDS, null);
                        if (fieldsType.isAssignableFrom(fields.getType())) {
                            ContainerValue fContainer = (ContainerValue) fields;
                            fContainer.removeElement(fieldIndex);
                            if (0 == fContainer.getElementSize()) {
                                container.removeElement(tIndex);
                            }
                            updateUi();
                        }
                    }
                } catch (ModelQueryException e) {
                    LOGGER.exception(e);
                }
            }
        }

    }

    /**
     * Action adds new row to an existing tuple.
     * 
     * @author Niko Nowatzki
     */
    private class NewFieldAction extends Action {

        private boolean after;

        /**
         * Constructor.
         * 
         * @param text
         *            the action text
         * @param after
         *            after or before the actual item
         */
        public NewFieldAction(String text, boolean after) {
            super(text);
            this.after = after;
        }

        @Override
        public void run() {
            if (valueList.isEmpty()) {
                new NewTupleAction().run();
            } else {
                int[] tupleIndex = getTupleIndex();
                if (null != tupleIndex && tupleIndex[TUPLEINDEX_FIELD_POS] >= 0) {
                    ContainerValue container = getContainer();
                    Value value = container.getElement(tupleIndex[TUPLEINDEX_CONTAINER_POS]);
                    int fieldIndex = tupleIndex[TUPLEINDEX_FIELD_POS];
                    if (after) {
                        fieldIndex++;
                    }
                    Project prj = getProject();
                    try {
                        IDatatype tupleType = ModelQuery.findType(prj, QmConstants.TYPE_TUPLE, null);
                        if (tupleType.isAssignableFrom(value.getType())) {
                            Value fields = ((CompoundValue) value).getNestedValue(QmConstants.SLOT_TUPLE_FIELDS);
                            IDatatype fieldsType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDS, null);
                            if (fieldsType.isAssignableFrom(fields.getType())) {
                                IDatatype fieldType = ModelQuery.findType(prj, QmConstants.TYPE_FIELD, null);
                                ContainerValue fContainer = (ContainerValue) fields;
                                fContainer.addElement(fieldIndex,
                                    ValueFactory.createValue(fieldType, (Object[]) null));
                                updateUi();
                            }
                        }
                    } catch (ModelQueryException
                            | ValueDoesNotMatchTypeException e) {
                        LOGGER.exception(e);
                    }
                }
            }
        }
    }

    /**
     * Action adds a new tuple.
     * 
     * @author Niko Nowatzki
     */
    private class NewTupleAction extends Action {

        /**
         * Constructor.
         */
        public NewTupleAction() {
            super("Add a new data item");
        }

        @Override
        public void run() {
            try {
                Project prj = getProject();
                IDatatype fieldTypeType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDTYPE, null);
                IDatatype fieldType = ModelQuery.findType(prj, QmConstants.TYPE_FIELD, null);
                IDatatype fieldsType = ModelQuery.findType(prj, QmConstants.TYPE_FIELDS, null);
                IDatatype tupleType = ModelQuery.findType(prj, QmConstants.TYPE_TUPLE, null);

                List<Object> fieldValueEntries = new ArrayList<Object>();
                fieldValueEntries.add(QmConstants.SLOT_FIELD_NAME);
                fieldValueEntries.add("");
                List<AbstractVariable> possibleTypes = ReferenceValuesFinder.findPossibleValues(prj, fieldTypeType);
                if (null != possibleTypes && !possibleTypes.isEmpty()) {
                    AbstractVariable initialType = possibleTypes.get(0);
                    fieldValueEntries.add(QmConstants.SLOT_FIELD_TYPE);
                    fieldValueEntries.add(ValueFactory.createValue(new Reference("", fieldTypeType, prj), initialType));
                }
                Value fieldValue = ValueFactory.createValue(fieldType, fieldValueEntries.toArray());
                Value fieldsValue = ValueFactory.createValue(fieldsType, new Object[] {fieldValue});
                Value tupleValue = ValueFactory.createValue(tupleType, 
                    new Object[] {QmConstants.SLOT_TUPLE_FIELDS, fieldsValue});
                ContainerValue container = getContainer();
                if (null == container) {
                    IDecisionVariable variable = getVariable();
                    Value value = ValueFactory.createValue(variable.getDeclaration().getType(), (Object[]) null);
                    variable.setValue(value, AssignmentState.ASSIGNED);
                    container = (ContainerValue) value;
                }
                container.addElement(tupleValue);
                updateUi();
            } catch (ValueDoesNotMatchTypeException | ModelQueryException
                    | ConfigurationException e) {
                LOGGER.exception(e);
            }
        }
    }

    /**
     * Editing support for field.
     * 
     * @author qin
     * 
     */
    private class BooleanTypeEditingSupport extends EditingSupport {
        private final TableViewer viewer;

        /**
         * Creates a editing support for field.
         * 
         * @param viewer
         *            the table viewer
         */
        public BooleanTypeEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            CellEditor result = new CheckboxCellEditor(viewer.getTable());
            return result;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            TupleObject object = (TupleObject) element;
            return object.getKeyPart();
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value != null) {
                TupleObject object = (TupleObject) element;
                object.setKeyPart(Boolean.parseBoolean(value.toString()));                
                viewer.update(element, null);
            }
        }

    }

    /**
     * Implements specific name editing support for the tuple level.
     * 
     * @author Holger Eichelberger
     */
    private class TupleNameEditingSupport extends NameEditingSupport {

        /**
         * Creates a tuple name editing support.
         * 
         * @param viewer
         *            the table viewer
         * @param var
         *            the name decision variable
         */
        public TupleNameEditingSupport(TableViewer viewer, IDecisionVariable var) {
            super(viewer, var);
        }

        @Override
        protected Object getValue(Object element) {
            TupleObject object = (TupleObject) element;
            return object.getTupleName();
        }

        @Override
        protected void setValue(Object element, Object value) {
            if (value != null) {
                TupleObject object = (TupleObject) element;
                object.setTupleName(value.toString()); // goes to IVML tuple
                                                       // value
                getTableViewer().update(element, null);
            }
        }

        @Override
        protected boolean canEdit(Object element) {
            return isFirst((TupleObject) element);
        }

    }

    /**
     * Returns whether <code>object</code> is the first field entry for the same
     * tuple in the table.
     * 
     * @param object
     *            the tuple object to check
     * @return <code>true</code> if this is the first, <code>false</code> else
     */
    private boolean isFirst(TupleObject object) {
        Table table = getTableViewer().getTable();
        int count = table.getItemCount();
        boolean isFirst = false;
        boolean found = false;
        for (int i = 0; !found && i < count; i++) {
            TupleObject tmp = (TupleObject) table.getItem(i).getData();
            if (tmp.getTuple().equals(object.getTuple())) {
                isFirst = !found && tmp == object;
                found = true;
            }
        }
        return isFirst; // TODO edit only the first one or update all
    }

    /**
     * Creates the columns.
     * 
     * @param tupleType
     *            the tuple type
     * @param fieldCompound
     *            the compound containing the data for the columns
     * @param table
     *            the table to be filled
     * @param layout
     *            the layout of <code>tab</code> to be modified as a side effect
     */
    private void createColumns(Compound tupleType, Compound fieldCompound,
            Table table, TableLayout layout) {
        DisplayNameProvider dnp = DisplayNameProvider.getInstance();

        TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 100, true));
        IDecisionVariable nameVar = getTemporaryDecisionVariable(TUPLE_PREFIX + "name");
        tableViewerColumn.getColumn().setText(dnp.getDisplayName(nameVar.getDeclaration()));
        tableViewerColumn.setEditingSupport(new TupleNameEditingSupport(tableViewer, nameVar));
        tableViewerColumn.getColumn().setResizable(true);

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 100, true));
        tableViewerColumn.getColumn().setText("entry kind");
        tableViewerColumn.getColumn().setResizable(true);
        // no editing support
        
        for (int e = 0; e < fieldCompound.getElementCount(); e++) {
            DecisionVariableDeclaration decl = fieldCompound.getElement(e);
            if (display(decl)) {
                String name = DisplayNameProvider.getInstance().getDisplayName(decl);
                if (null == name) {
                    name = decl.getName();
                }
                if (name.equals(QmConstants.SLOT_FIELD_NAME)) {
                    tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                    layout.addColumnData(new ColumnWeightData(3, 100, true));
                    tableViewerColumn.getColumn().setText("field name");
                    tableViewerColumn.getColumn().setResizable(true);
                    tableViewerColumn.setEditingSupport(new NameEditingSupport(tableViewer));
                }

                if (name.equals(QmConstants.SLOT_FIELD_TYPE)) {
                    tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                    layout.addColumnData(new ColumnWeightData(3, 100, true));
                    tableViewerColumn.getColumn().setText("field type");
                    tableViewerColumn.getColumn().setResizable(true);
                    tableViewerColumn.setEditingSupport(new ReferenceTypeEditingSupport(tableViewer));
                }

                if (showKeyPart()) {
                    if (name.equals(QmConstants.SLOT_FIELD_KEYPART)) {
                        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                        layout.addColumnData(new ColumnWeightData(3, 100, true));
                        tableViewerColumn.getColumn().setText("Is retrieval key?");
                        tableViewerColumn.getColumn().setResizable(true);
                        tableViewerColumn.setEditingSupport(new BooleanTypeEditingSupport(tableViewer));
                    }
                }
            }
        }
    }

    /**
     * Returns whether the key part shall be shown.
     * 
     * @return <code>true</code> if the key part shall be shown, <code>false</code> else
     */
    private boolean showKeyPart() {
        boolean show = true;
        if (Boolean.FALSE == getUiParameter(SHOW_KEY_PART)) {
            show = false;
        } else {
            Object enabledVarName = getUiParameter(SHOW_KEY_PART_VARIABLE);
            if (null != enabledVarName) {
                show = getVariable().getDeclaration().getName().equals(enabledVarName);
            }
        }
        return show;
    }

    /**
     * Creates the rows.
     * 
     * @param tuples
     *            the container value to be turned into a row
     * @param tab
     *            the table to be modified
     * @param fieldCompound
     *            the compound type representing the contained elements of the
     *            container
     */
    private void createRows(ContainerValue tuples, Table tab, Compound fieldCompound) {
        tupleAccessor.clear();
        fieldAccessor.clear();
        for (int t = 0; t < tuples.getElementSize(); t++) {
            Value tVal = tuples.getElement(t);
            if (tVal instanceof CompoundValue) {
                CompoundValue tuple = (CompoundValue) tVal;
                String tupleName = String.valueOf(t + 1);
                Value tNameVal = tuple.getNestedValue(QmConstants.SLOT_TUPLE_NAME);
                if (tNameVal instanceof StringValue) {
                    tupleName = ((StringValue) tNameVal).getValue();
                }
                Value fVal = tuple.getNestedValue(QmConstants.SLOT_TUPLE_FIELDS);
                if (fVal instanceof ContainerValue) {
                    ContainerValue fields = (ContainerValue) fVal;
                    createFieldRows(tupleName, t, fields, tab, fieldCompound);
                }
            }
        }
    }
    
    // checkstyle: stop parameter number check

    /**
     * Creates the rows for the fields in a tuple.
     * 
     * @param tupleName the name of the tuple
     * @param tupleIndex the index of the tuple currently being processed
     * @param fields the container value to be turned into rows
     * @param table the table to be modified
     * @param fieldCompound the compound type representing the contained elements of the
     *     container
     */
    private void createFieldRows(String tupleName, int tupleIndex, ContainerValue fields,
            Table table, Compound fieldCompound) {
        int fieldCount = fields.getElementSize();

        for (int f = 0; f < fieldCount; f++) {
            Value fVal = fields.getElement(f);
            String[] row = new String[DATAINDEX_COUNT]; // due to dynamics, not table.getColumnCount
            Arrays.fill(row, "");
            CompoundValue v = null;

            row[DATAINDEX_TUPLE_NAME] = (0 == f) ? tupleName : "";
            row[DATAINDEX_ENTRY_KIND] = (0 == f) ? "item + 1st field" : nrToText(f + 1) + " field";
            if (fVal instanceof CompoundValue) {
                v = (CompoundValue) fVal;
                for (int e = 0; e < fieldCompound.getElementCount(); e++) {
                    DecisionVariableDeclaration decl = fieldCompound.getElement(e);
                    if (display(decl)) {
                        String fieldName = decl.getName();
                        if (QmConstants.SLOT_FIELD_NAME.equals(fieldName)) {
                            row[DATAINDEX_FIELD_NAME] = compoundSlotValue(v, fieldName);
                        } else if (QmConstants.SLOT_FIELD_TYPE.equals(fieldName)) {
                            row[DATAINDEX_TYPE] = compoundSlotValue(v, fieldName);
                        } else if (QmConstants.SLOT_FIELD_KEYPART.equals(fieldName)) {
                            row[DATAINDEX_KEYPART] = getCompoundSlot(v, QmConstants.SLOT_FIELD_KEYPART);
                        }
                    }
                }
            }
            // Create tuple and put in list.
            TupleObject tuple = new TupleObject(row, fieldAccessor, tupleAccessor);
            tupleAccessor.associate(tuple, tupleIndex);
            fieldAccessor.associate(tuple, f);
            valueList.add(tuple);
        }
    }
    
    /**
     * Returns the value of a (dereferenced) compound slot.
     * 
     * @param compoundValue the compound value
     * @param fieldName the field/slot name
     * @return the value as string or an empty string
     */
    private static String compoundSlotValue(CompoundValue compoundValue, String fieldName) {
        String result;
        Value nVal = dereferenceType(compoundValue.getNestedValue(fieldName));
        if (null != nVal) {
            result = toString(nVal);
        } else {
            result = "";
        }
        return result;
    }

    // checkstyle: resume parameter number check

    /**
     * Turns a number into (English) number text notation.
     * 
     * @param nr the number
     * @return the text notation
     */
    private static final String nrToText(int nr) {
        String result = "";
        switch (nr) {
        case 1:
            result = "1st";
            break;
        case 2:
            result = "2nd";
            break;
        case 3:
            result = "3rd";
            break;
        default:
            result = nr + "th";
            break;
        }
        return result;
    }

    /**
     * Dereferences the type to it's name.
     * 
     * @param value
     *            the value to dereference
     * @return the dereferenced value (name of the type)
     */
    private static Value dereferenceType(Value value) {
        if (value instanceof ReferenceValue) {
            IDecisionVariable var = VariabilityModel.Configuration.BASICS.getConfiguration()
                    .getDecision(((ReferenceValue) value).getValue());
            if (null != var) {
                value = var.getValue();
                if (value instanceof CompoundValue) {
                    value = ((CompoundValue) value).getNestedValue(QmConstants.SLOT_NAME);
                }
            } // TODO in case of a valueEx??
        }
        return value;
    }

}
