/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.DisplayNameProvider;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.RealType;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import qualimasterapplication.Activator;

/**
 * Specific editor for quality parameter weights.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public class QualityParameterWeightingEditor extends AbstractContainerOfCompoundsTableEditor {

    public static final IEditorCreator CREATOR = new IEditorCreator() {

        @Override
        public Control createEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return new QualityParameterWeightingEditor(config, variable, parent);
        }

        @Override
        public CellEditor createCellEditor(UIConfiguration config,
                IDecisionVariable variable, Composite parent) {
            return null;
        }
        
    };

    private static final int DATAINDEX_NAME = 0;
    private static final int DATAINDEX_WEIGHT = 1;

    private static final String[] PROPS = {"name", "weight"};
    private static final EASyLogger LOGGER = EASyLoggerFactory.INSTANCE
        .getLogger(QualityParameterWeightingEditor.class, Activator.PLUGIN_ID);

    private TableViewer tableViewer;
    private ArrayList<QualityParameterWeightObject> valueList = new ArrayList<QualityParameterWeightObject>();
    private ContainerValueAccessor accessor = new ContainerValueAccessor();

    /**
     * Wrapper-class for quality parameter weights.
     * 
     * @author Holger Eichelberger
     * @author Niko Nowatzki
     */
    public class QualityParameterWeightObject extends TableObject {
        
        private String weight;
        
        /**
         * Constructor for a parameter weight.
         * 
         * @param name name of the tuple.
         * @param weight the weight of the parameter.
         * @param accessor the value accessor
         */
        public QualityParameterWeightObject(String name, String weight, IValueAccessor accessor) {
            super(name, accessor);
            this.weight = weight;
        }

        /**
         * Returns the weight value.
         * @return the weight value
         */
        public String getWeight() {
            return weight;
        }
        /**
         * Sets the weight value.
         * @param weight the weight value
         */
        public void setWeight(String weight) {
            this.weight = weight;
            setValue(getCompoundValue(), QmConstants.SLOT_QPARAMWEIGHTING_WEIGHT, weight);
        }

    }
    
    /**
     * LabelProvider for tuple-tables.
     * 
     * @author Holger Eichelberger
     * @author Niko Nowatzki
     */
    private class QualityParameterWeightingLabelProvider implements ITableLabelProvider {

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
            QualityParameterWeightObject elem = (QualityParameterWeightObject) element;
            switch (columnIndex) {
            case DATAINDEX_NAME:
                result = elem.getName();
                break;
            case DATAINDEX_WEIGHT:
                result = elem.getWeight();
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
     * @author Holger Eichelberger
     * @author Niko Nowatzki
     */
    private class QualityParameterWeightingContentProvider implements IStructuredContentProvider {

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
     * Editing support for the weight value.
     * 
     * @author Holger Eichelberger
     */
    protected class QualityParameterWeightEditingSupport extends EditingSupport {
        private final TableViewer viewer;

        /**
         * Creates the editing support of the parameter default value.
         * 
         * @param viewer
         *            the table viewer
         */
        public QualityParameterWeightEditingSupport(TableViewer viewer) {
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
            QualityParameterWeightObject object = (QualityParameterWeightObject) element;
            return object.getWeight();
        }

        @Override
        protected void setValue(Object element, Object value) {
            QualityParameterWeightObject object = (QualityParameterWeightObject) element;
            if (value != null) {
                object.setWeight(value.toString());
                viewer.update(element, null);
            }

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
    private QualityParameterWeightingEditor(UIConfiguration config, IDecisionVariable variable, Composite parent) {
        super(config, variable, parent, SWT.NULL);
        GridLayout gLayout = new GridLayout();
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.heightHint = 80;
        setLayout(gLayout);
        setLayoutData(gridData);
        
        setLayout(new FillLayout());
        tableViewer = setTableViewer(new TableViewer(
                this, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION));
        Table table = tableViewer.getTable();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

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
        tableViewer.setLabelProvider(new QualityParameterWeightingLabelProvider());
        tableViewer.setContentProvider(new QualityParameterWeightingContentProvider());

        tableViewer.setColumnProperties(PROPS);

        // Set input
        tableViewer.setInput(valueList);
    }
    
    @Override
    public void refresh() {
        ContainerValue container = getContainer();
        if (null != tableViewer && null != container) {
            valueList.clear();
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
                IDatatype pType = ModelQuery.findType(prj, QmConstants.TYPE_ADAPTIVITY_QPARAMWEIGHTING, null);
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

                if (name.equals(QmConstants.SLOT_QPARAMWEIGHTING_PARAMETER)) {
                    tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
                    layout.addColumnData(new ColumnWeightData(3, 300, true));

                    tableViewerColumn.getColumn().setText(name);
                    tableViewerColumn.getColumn().setResizable(true);

                    //tableViewerColumn.setEditingSupport(new NameEditingSupport(tableViewer));
                }
            }
        }

        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 300, true));
        tableViewerColumn.getColumn().setText("parameter");
        tableViewerColumn.getColumn().setResizable(true);
        
        tableViewerColumn = new TableViewerColumn(tableViewer, SWT.CENTER);
        layout.addColumnData(new ColumnWeightData(3, 100, true));
        tableViewerColumn.getColumn().setText("weight");
        tableViewerColumn.getColumn().setResizable(true);
        tableViewerColumn.setEditingSupport(new QualityParameterWeightEditingSupport(tableViewer));
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
        Configuration config = getVariable().getConfiguration(); 
        boolean nameFound = false;
        for (int r = 0; r < rows; r++) {
            Value eVal = cVal.getElement(r);
            String name = "";
            String weight = null;
            nameFound = false;
            if (eVal instanceof CompoundValue) {
                CompoundValue v = (CompoundValue) eVal;
                for (int e = 0; e < compound.getInheritedElementCount(); e++) {
                    DecisionVariableDeclaration decl = compound.getInheritedElement(e);
                    if (display(decl)) {
                        String fieldName = decl.getName();
                        if (QmConstants.SLOT_QPARAMWEIGHTING_PARAMETER.equals(fieldName)) {
                            Value nVal = Configuration.dereference(config, v.getNestedValue(decl.getName()));
                            if (nVal instanceof CompoundValue) {
                                name = getCompoundSlot((CompoundValue) nVal, QmConstants.SLOT_OBSERVABLE_TYPE);
                                nameFound = true;
                            } 
                        }
                    }
                }
                // get the parameter value for the VALUE column
                weight = getCompoundSlot(v, QmConstants.SLOT_QPARAMWEIGHTING_WEIGHT);
                if (null == weight || weight.isEmpty()) {
//                    System.out.println(name + " was initialized incorrectly and has no weight!");
                    try {                        
                        Value compoundValue = ValueFactory.createValue(compound, "name", name, "weight", 0);
                        v.setValue(compoundValue);
                        weight = getCompoundSlot(v, QmConstants.SLOT_QPARAMWEIGHTING_WEIGHT);

                    } catch (ValueDoesNotMatchTypeException e) {
                        e.printStackTrace();
                    }
                    
                }     
                if (null == name || !nameFound) {
                    name = "null";
                }
                
            }
            
            // Add parameter to list.
            QualityParameterWeightObject param = new QualityParameterWeightObject(name, weight, accessor);
            accessor.associate(param, r);
            valueList.add(param);
        }
    }

}
