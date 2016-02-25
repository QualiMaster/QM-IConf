package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import de.uni_hildesheim.sse.easy.ui.productline_editor.DelegatingEasyEditorPage;
import de.uni_hildesheim.sse.easy.ui.productline_editor.IRefreshableEditor;
import de.uni_hildesheim.sse.easy_producer.instantiator.Bundle;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.DisplayNameProvider;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.datatypes.Enum;
import de.uni_hildesheim.sse.model.varModel.datatypes.EnumLiteral;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.filter.ReferenceValuesFinder;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.persistency.StringProvider;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.modelManagement.IModelListener;
import eu.qualimaster.easy.extension.QmConstants;
import qualimasterapplication.Activator;

/**
 * Provides the basis for QualiMaster specific table editors.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractTableEditor extends Composite implements IQMEditor, IDirtyableEditor, 
    IModelListener<Project>, IRefreshableEditor {

    protected static final String SLOT_TYPE = QmConstants.SLOT_FIELD_TYPE;
    
    private TableViewer tableViewer;
    private int prefWidth = -1;
    private int prefHeight = 80;
    private de.uni_hildesheim.sse.model.confModel.Configuration tmpConfig;
    private UIConfiguration uiCfg;
    private List<SelectionListener> dirtyListener = new ArrayList<SelectionListener>();
    private Map<UIParameter, Object> parentParameters;
    private IDecisionVariable variable;
    
    /**
     * Creates an instance.
     * 
     * @param config the parent UI configuration
     * @param variable the variable to be edited
     * @param parent the parent composite
     * @param style the composite style
     */
    protected AbstractTableEditor(UIConfiguration config, IDecisionVariable variable, Composite parent, int style) {
        super(parent, style);
        this.variable = variable;
        VarModel.INSTANCE.events().addModelListener(variable.getConfiguration().getProject(), this);
        this.parentParameters = config.getParameters();
    }
    
    @Override
    public void dispose() {
        VarModel.INSTANCE.events().removeModelListener(variable.getConfiguration().getProject(), this);
        super.dispose();
    }
    
    /**
     * Returns the decision variable underlying this editor. Do not store this variable
     * permanently as it may change due to model reloads.
     * 
     * @return the variable to be edited
     */
    protected IDecisionVariable getVariable() {
        return variable;
    }

    /**
     * Returns the configuration containing the underlying decision variable. Do not store this variable
     * permanently as it may change due to model reloads.
     * 
     * @return the containing project
     */
    protected Configuration getConfiguration() {
        return variable.getConfiguration();
    }
    
    /**
     * Returns the project containing the underlying decision variable. Do not store this variable
     * permanently as it may change due to model reloads.
     * 
     * @return the containing project
     */
    protected Project getProject() {
        return getConfiguration().getProject();
    }

    /**
     * Defines the actual table viewer.
     * 
     * @param tableViewer the actual table viewer
     * @return <code>tableViewer</code>
     */
    protected TableViewer setTableViewer(TableViewer tableViewer) {
        this.tableViewer = tableViewer;
        return tableViewer;
    }
    
    /**
     * Defines the preferred size of this table editor.
     * 
     * @param prefWidth the preferred width (ignored if negative)
     * @param prefHeight the preferred height (ignored if negative)
     */
    protected void setPreferredSize(int prefWidth, int prefHeight) {
        this.prefWidth = prefWidth;
        this.prefHeight = prefHeight;
    }
    
    /**
     * Returns the actual table viewer. (call {@link #setTableViewer(TableViewer)} before)
     * 
     * @return the actual table viewer
     */
    protected TableViewer getTableViewer() {
        return tableViewer;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (null != tableViewer) {
            tableViewer.getTable().setEnabled(enabled);
        }
    }

    /**
     * Returns whether <code>decl</code> shall be displayed.
     * 
     * @param decl the declaration to be considered
     * @return <code>true</code> if <code>decl</code> shall be displayed, <code>false</code> else
     */
    protected static boolean display(DecisionVariableDeclaration decl) {
        return ModelAccess.isVisibleType(decl.getType()) && !ConstraintType.isConstraint(decl.getType());
    }

    /**
     * Turns a value into a string and aims at emulating the currently missing label provider relying on the 
     * actual value. This shall later be replaced by relying on GUIvariables! Then this method can be deleted.
     * 
     * @param value the value to be turned into a String
     * @return the String representation
     */
    protected static String toString(Value value) {
        String result = StringProvider.toIvmlString(value);
        IDatatype type = value.getType();
        if (StringType.TYPE.isAssignableFrom(type)) {
            while (result.startsWith("\"")) {
                result = result.substring(1);
            }
            while (result.endsWith("\"")) {
                result = result.substring(0, result.length() - 1);
            }
        } else if (Enum.TYPE.isAssignableFrom(type)) {
            int pos = result.lastIndexOf(".");
            if (pos > 0 && pos + 1 < result.length()) {
                result = result.substring(pos + 1);
            }
        } else if (Container.TYPE.isAssignableFrom(type)) {
            if (result.startsWith("{\"")) {
                result = result.substring(2);
            }
            while (result.endsWith("\"}")) {
                result = result.substring(0, result.length() - 2);
            }
        }
        return result;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point result = super.computeSize(wHint, hHint, changed);
        if (prefWidth >= 0 && result.x < prefWidth) {
            result.x = prefWidth;
        }
        if (prefHeight >= 0 && result.y < prefHeight) {
            result.y = prefHeight;
        }
        return result;
    }
    
    /**
     * Creates a temporary model to enable the EASy editors. EASy does not create deeper level decision
     * variables, otherwise we could just use them. This is an awful hack.
     * 
     * @param compound the compound to take the contained elements from
     */
    protected void createTemporaryModel(Compound compound) {
        Project tmpModel = new Project("tmp");
        for (int e = 0; e < compound.getElementCount(); e++) {
            DecisionVariableDeclaration elt = compound.getElement(e);
            DecisionVariableDeclaration tmpDecl = new DecisionVariableDeclaration(
                elt.getName(), elt.getType(), tmpModel);
            tmpDecl.setComment(elt.getComment()); // take over .text information
            tmpModel.add(tmpDecl);
        }
        augmentTemporaryModel(tmpModel);
        tmpConfig = new de.uni_hildesheim.sse.model.confModel.Configuration(tmpModel);
        uiCfg = ConfigurationTableEditorFactory.createConfiguration(tmpConfig, 
            new DelegatingEasyEditorPage(tableViewer.getTable()), parentParameters);
    }
    
    /**
     * Returns the specified UI parameter.
     * 
     * @param key the parameter (key)
     * @return the parameter value (may be <b>null</b>)
     */
    protected Object getUiParameter(UIParameter key) {
        return null == uiCfg ? null : uiCfg.getParameter(key);
    }
    
    /**
     * Allows to augment the temporary model.
     * 
     * @param tmpModel the temporary model
     */
    protected void augmentTemporaryModel(Project tmpModel) {
    }

    /**
     * Creates a cell editor for the given decision variable from the temporary model.
     * Call {@link #createTemporaryModel(Compound)} to initialize the model first.
     * 
     * @param var the variable to create the editor for
     * @return the cell editor (may be <b>null</b>)
     */
    protected CellEditor createCellEditor(IDecisionVariable var) {
        return ConfigurationTableEditorFactory.createCellEditor(uiCfg, var);
    }
    
    /**
     * Returns a decision variable from the temporary configuration.
     * Call {@link #createTemporaryModel(Compound)} to initialize the temporary configuration.     
     * 
     * @param name the name of the variable
     * @return <b>null</b> if no such variable exists or the temporary configuration was not initialized
     */
    protected IDecisionVariable getTemporaryDecisionVariable(String name) {
        IDecisionVariable var = null;
        ContainableModelElement elt = tmpConfig.getProject().getElement(name);
        if (elt instanceof AbstractVariable) {
            var = tmpConfig.getDecision((AbstractVariable) elt);
        }
        return var;
    }
    
    /**
     * Sets the value of a compound field if possible.
     * 
     * @param compound the compound to set the value for
     * @param field the field name
     * @param value the value
     */
    protected void setValue(CompoundValue compound, String field, Object value) {        
        String name = null;
        if (null != compound) {
            try {
                name = compound.getStringValue("name");
                DecisionVariableDeclaration element = ((Compound) compound.getType()).getElement(field);
                if (null != element) {
                    IDatatype type = element.getType();
                    if (null != type) {
                        value = referenceValue(type, value);
                        compound.configureValue(field, ValueFactory.createValue(type, value));
                        notifyDirty();
                    }
                }
            } catch (ValueDoesNotMatchTypeException e) {
                EASyLoggerFactory.INSTANCE.getLogger(AbstractTableEditor.class, Activator.PLUGIN_ID).exception(e);
                Dialogs.showErrorDialog("Illegal value", name + ":" + e.getMessage());            
            }
        }
    }

    /**
     * Turns the given value into an reference if required, e.g., for configurable types.
     * 
     * @param type the target type
     * @param value the value to be set
     * @return the referenced value or <code>value</code>
     */
    private static Object referenceValue(IDatatype type, Object value) {
        if (Reference.TYPE.isAssignableFrom(type)) {
            DisplayNameProvider nameProvider = DisplayNameProvider.getInstance();
            List<AbstractVariable> possibleDecls = ReferenceValuesFinder.findPossibleValues(
                de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration.INFRASTRUCTURE.getConfiguration()
                    .getProject(), (Reference) type);
            for (int i = 0; i < possibleDecls.size(); i++) {
                AbstractVariable declaration = possibleDecls.get(i);
                String name = nameProvider.getDisplayName(declaration);
                if (name.equals(value)) {
                    value = declaration;
                    break;
                }
            }
        } 
        return value;
    }
    
    /**
     * Provides access to a (nested) value of a variable in a generic way. This is needed to retrieve the value
     * on demand based on changing models.
     * 
     * @author Holger Eichelberger
     */
    protected interface IValueAccessor {
        
        /**
         * Returns the actual value.
         * 
         * @param key in case that the accessor holds more than one value, <code>key</code> provides access to the 
         *     right value
         * @return the actual value
         */
        public CompoundValue getValue(Object key);
        
    }

    /**
     * A basic table object representing a compound with name and type.
     * 
     * @author Holger Eichelberger
     */
    protected class TableObject {
        
        private String name;
        private String type;
        private IValueAccessor accessor;

        /**
         * Creates a table object.
         * 
         * @param name the name
         * @param type the type
         * @param accessor the value accessor
         */
        protected TableObject(String name, String type, IValueAccessor accessor) {
            this.name = name;
            this.type = type;
            this.accessor = accessor;
        }
        
        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getName() {
            return name;
        }
        
        /**
         * Changes the name (also in the compound value if possible).
         * 
         * @param name the new name
         */
        public void setName(String name) {
            this.name = name;
            setValue(getCompoundValue(), "name", name);
        }
        
        /**
         * Returns the name.
         * 
         * @return the name
         */
        public String getType() {
            return type;
        }
        
        /**
         * Return tuples type.
         * @param type the type to set
         */
        public void setType(String type) {
            this.type = type;
            setValue(getCompoundValue(), SLOT_TYPE, type);
        }
        
        /**
         * Returns the compound value.
         * 
         * @return the compound value
         */
        protected CompoundValue getCompoundValue() {
            return accessor.getValue(this);
        }
        
    }
    
    /**
     * EditingSUpport for name row assuming that table elements are of type {@link TableObject}.
     * 
     * @author Niko
     */
    protected class NameEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        private IDecisionVariable var;

        /**
         * Constructor.
         * 
         * @param viewer the parent table viewer
         */
        public NameEditingSupport(TableViewer viewer) {
            this(viewer, getTemporaryDecisionVariable("name"));
        }

        /**
         * Constructor.
         * 
         * @param viewer the parent table viewer
         * @param var the variable to edit
         */
        protected NameEditingSupport(TableViewer viewer, IDecisionVariable var) {
            super(viewer);
            this.viewer = viewer;
            this.var = var;
        }
        
        @Override
        protected CellEditor getCellEditor(Object element) {
            CellEditor result;
            if (var != null) {
                result = createCellEditor(var);
            } else {
                result = new TextCellEditor(viewer.getTable());
            }
            return result;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            TableObject object = (TableObject) element;
            return object.getName();
        }

        @Override
        protected void setValue(Object element, Object value) {
            TableObject object = (TableObject) element;
            if (value != null) {
                object.setName(value.toString());
                viewer.update(element, null);
            }
        }
        
        /**
         * Returns the table viewer.
         * 
         * @return the table viewer
         */
        public TableViewer getTableViewer() {
            return viewer;
        }
        
    }
    
    /**
     * Editing Support for enum-based types.
     * 
     * @author Niko
     */
    protected class EnumTypeEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        private IDecisionVariable var;
        private de.uni_hildesheim.sse.model.varModel.datatypes.Enum type;
    
        /**
         * Constructor.
         * @param viewer .
         */
        public EnumTypeEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            var = getTemporaryDecisionVariable(SLOT_TYPE);
            if (null != var) {
                IDatatype varType = var.getDeclaration().getType();
                if (varType instanceof de.uni_hildesheim.sse.model.varModel.datatypes.Enum) {
                    type = (de.uni_hildesheim.sse.model.varModel.datatypes.Enum) varType;
                }
            }
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            CellEditor result;
            if (var != null) {
                result = createCellEditor(var);
            } else {
                result = new TextCellEditor(viewer.getTable());
            }
            return result;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            Object result = -1;
            TableObject object = (TableObject) element;
            if (null != type && null != object.type) {
                EnumLiteral lit = type.get(object.type);
                if (null != lit) {
                    result = type.getLiteralIndex(lit);
                }
            }
            return result;
        }

        @Override
        protected void setValue(Object element, Object value) {
            TableObject object = (TableObject) element;
            if (value != null) {
                if (value instanceof Integer && null != type) {
                    EnumLiteral lit = type.getLiteral(((Integer) value).intValue());
                    if (null != lit) {
                        object.setType(lit.getName());
                        viewer.update(element, null);
                    }
                } else {
                    object.setType(value.toString());
                    viewer.update(element, null);
                }
            }    
        }
    }


    /**
     * Editing Support for ref-based types. This class requires proper resolution of the available types.
     * 
     * @author Niko
     */
    protected class ReferenceTypeEditingSupport extends EditingSupport {

        private final TableViewer viewer;
        private IDecisionVariable var;
        private String[] items;
    
        /**
         * Constructor.
         * @param viewer .
         */
        public ReferenceTypeEditingSupport(TableViewer viewer) {
            super(viewer);
            this.viewer = viewer;
            var = getTemporaryDecisionVariable(SLOT_TYPE);
        }

        @Override
        protected CellEditor getCellEditor(Object element) {
            CellEditor result;
            if (var != null) {
                result = createCellEditor(var);
            } else {
                result = new TextCellEditor(viewer.getTable());
            }
            if (result instanceof ComboBoxCellEditor) {
                ComboBoxCellEditor cEditor = (ComboBoxCellEditor) result;
                items = cEditor.getItems();
            }
            return result;
        }

        @Override
        protected boolean canEdit(Object element) {
            return true;
        }

        @Override
        protected Object getValue(Object element) {
            int result = -1;
            TableObject object = (TableObject) element;
            if (null != items && null != object.type) {
                for (int i = 0; -1 == result && i < items.length; i++) {
                    if (items[i].equals(object.type)) {
                        result = i;
                    }
                }
            }
            return result;
        }

        @Override
        protected void setValue(Object element, Object value) {
            TableObject object = (TableObject) element;
            if (null != items && value instanceof Integer) {
                int pos = ((Integer) value).intValue();
                if (pos >= 0 && pos < items.length) {
                    object.setType(items[pos]);
                    viewer.update(element, null);
                }
            }    
        }
    }

    /**
     * Returns the compound slot value as string.
     * 
     * @param value the value to return the slot from
     * @param slotName the slot name
     * @return the slot value
     */
    protected static String getCompoundSlot(CompoundValue value, String slotName) {
        String result;
        Value nVal = value.getNestedValue(slotName);
        if (null != nVal) {
            result = toString(nVal);
        } else {
            result = "";
        }
        return result;
    }

    @Override
    public void addDirtyListener(DirtyListener listener) {
        dirtyListener.add(listener);
        tableViewer.getTable().addSelectionListener(listener);
    }

    @Override
    public void removeDirtyListener(DirtyListener listener) {
        dirtyListener.remove(listener);
        tableViewer.getTable().removeSelectionListener(listener);
    }
    
    /**
     * Notifies the externally attached listeners that the editor became dirty.
     */
    protected void notifyDirty() {
        Event evt = new Event();
        evt.item = this;
        evt.widget = this;
        SelectionEvent event = new SelectionEvent(evt);
        for (SelectionListener listener : dirtyListener) {
            listener.widgetSelected(event);
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
