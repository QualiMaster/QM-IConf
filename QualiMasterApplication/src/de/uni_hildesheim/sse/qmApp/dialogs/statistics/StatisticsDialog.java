/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.dialogs.statistics;

import java.util.ArrayList;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import eu.qualimaster.easy.extension.modelop.ModelStatistics;

/**
 * Prints the statistics of the QM (Meta-) Model.
 * @author El-Sharkawy
 *
 */
public class StatisticsDialog extends Dialog {
    
    private ModelStatistics statistics;
    private ArrayList<StatisticsItem> dataModel = new ArrayList<>();
    
    /**
     * Default constructor for the StatisticsDialo.
     * @param parentShell The parent shell.
     * @param statistics Collected statistics of the  QM (Meta-) Model, collected by the
     * {@link eu.qualimaster.easy.extension.modelop.QMConfigStatisticsVisitor}.
     */
    public StatisticsDialog(Shell parentShell, ModelStatistics statistics) {
        super(parentShell);
        this.statistics = statistics;
        setBlockOnOpen(true);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite body = (Composite) super.createDialogArea(parent);
        
        final TreeViewer viewer = new TreeViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        Tree table = viewer.getTree();
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setContentProvider(new StatisticsContentProvider());
        
        TreeViewerColumn column1 = new TreeViewerColumn(viewer, SWT.NONE);
        column1.setLabelProvider(new StatisticsLabelProvider(0));
        TreeColumn tc1 = column1.getColumn();
        TreeViewerColumn column2 = new TreeViewerColumn(viewer, SWT.NONE);
        column2.setLabelProvider(new StatisticsLabelProvider(1));
        TreeColumn tc2 = column2.getColumn();
        
        tc1.setText("Type");
        tc2.setText("No. of Elements");
        tc1.setWidth(375);
        tc2.setWidth(180);
        table.setHeaderVisible(true);
        
        // Variables
        fillTable();
        viewer.setInput(dataModel);
        viewer.expandAll();
        
        //Capture ESC-Key.
        body.addListener(SWT.Traverse, new Listener() {
            
                public void handleEvent(Event evt) {
                    if (evt.detail == SWT.TRAVERSE_ESCAPE) {
                        body.getShell().close();
                    }
                }
            });   
        return body;
    }

    /**
     * Reads the statistics and fills the table with that information.
     */
    private void fillTable() {
        addValue("Total Variables (Without Container)", statistics.noOfVariablesWithoutContainer());
        addNestedValue("Non Nested Variables", statistics.noOfToplevelVariables());
        addNestedValue("+ Annotation Instances (not Included Before)", statistics.noOfAnnotations());
        
        addValue("Total Variables", statistics.noOfVariables());
        StatisticsItem parent = dataModel.get(dataModel.size() - 1);
        StatisticsItem vars = new StatisticsItem("Non Constraint Variables (Nested & Non Nested)",
            String.valueOf(statistics.noOfNormalVariables()), parent);
        new StatisticsItem("Non Constraint Variables (not Nested in Container)",
            String.valueOf(statistics.noOfNormalVariablesNoContainer()), vars);
        StatisticsItem consVars = new StatisticsItem("Constraint Variables (Nested & Non Nested)",
            String.valueOf(statistics.noOfConstraintVariables()), parent);
        new StatisticsItem("Constraint Variables (not Nested in Container)",
                String.valueOf(statistics.noOfConstraintVariablesNoContainer()), consVars);
        
        addValue("All Pipelines", statistics.noOfPipelines() + statistics.noOfSubPipelines());
        addNestedValue("Pipelines", statistics.noOfPipelines());
        addNestedValue("Subpipelines", statistics.noOfSubPipelines());
        
        addValue("Pipeline Elements", statistics.noOfSources() + statistics.noOfFamilyElements()
            + statistics.noOfDataManagementElements() + statistics.noOfReplaySinks() + statistics.noOfSinks());
        addNestedValue("Sources", statistics.noOfSources());
        addNestedValue("Family Elements", statistics.noOfFamilyElements());
        addNestedValue("Data Mangement Elements", statistics.noOfDataManagementElements());
        addNestedValue("Replay Sinks", statistics.noOfReplaySinks());
        addNestedValue("Sinks", statistics.noOfSinks());
        
        addValue("Algorithms", statistics.noOfSWAlgorithms() + statistics.noOfHWAlgorithms()
            + statistics.noOfSPAlgorithms());
        addNestedValue("Software Algorithms", statistics.noOfSWAlgorithms());
        addNestedValue("Hardware Algorithms", statistics.noOfHWAlgorithms());
        addNestedValue("Subpipeline Algorithms", statistics.noOfSPAlgorithms());
        
        addValue("Other Model Elements");
        addNestedValue("General-purpose Machines", statistics.noOfGeneralMachines());
        addNestedValue("Families", statistics.noOfFamilies());
        
        addValue("Constraints", statistics.noOfConstraints() + statistics.noOfOperations()
            + statistics.noOfConstraintInstances() + statistics.noOfConstraintVariables());
        addNestedValue("Constraints Defined on Project Level", statistics.noOfConstraints());
        addNestedValue("Instantiated Constraint of Compounds", statistics.noOfConstraintInstances());
        addNestedValue("User Defined Operations", statistics.noOfOperations());
        addNestedValue("Constraint Variables", statistics.noOfConstraintVariables());
        
        addValue("Declarations", statistics.noOfToplevelDeclarations() + statistics.noOfNestedDeclarations()
            + statistics.noOfToplevelAnnotations() + statistics.noOfNestedAnnotations());
        addNestedValue("Non Nested Variable Declarations", statistics.noOfToplevelDeclarations());
        addNestedValue("Nested Variable Declarations", statistics.noOfNestedDeclarations());
        addNestedValue("Non Nested Annotations", statistics.noOfToplevelAnnotations());
        addNestedValue("Nested Variable Annotations", statistics.noOfNestedAnnotations());
    };
    
    /**
     * Creates an header element without a value.
     * @param name The header title.
     */
    private void addValue(String name) {
        StatisticsItem item = new StatisticsItem(name, "", dataModel);
        dataModel.add(item);
    }
    
    /**
     * Creates an expandable top level item.
     * @param name The name of the item to display in the first column.
     * @param value The value, to display in the second column.
     */
    private void addValue(String name, int value) {
        StatisticsItem item = new StatisticsItem(name, String.valueOf(value), dataModel);
        dataModel.add(item);
    }
    
    /**
     * Creates nested item.
     * @param name The name of the item to display in the first column.
     * @param value The value, to display in the second column.
     */
    private void addNestedValue(String name, int value) {
        if (!dataModel.isEmpty()) {
            StatisticsItem parent = dataModel.get(dataModel.size() - 1);
            new StatisticsItem(name, String.valueOf(value), parent);
        } else {
            addValue(name, value);
        }
    }
    
    @Override
    protected boolean isResizable() {
        return true;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        Image icon = IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL);
        newShell.setImage(icon);
        newShell.pack();
        newShell.setSize(600, 775);
        
        super.configureShell(newShell);
        newShell.setText("Model Statistics");
        DialogsUtil.centerShell(newShell);
    }

}
