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
package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.TreeItem;

import de.uni_hildesheim.sse.qmApp.images.IconManager;
import eu.qualimaster.easy.extension.modelop.ModelStatistics;

/**
 * Prints the statistics of the QM (Meta-) Model.
 * @author El-Sharkawy
 *
 */
public class StatisticsDialog extends Dialog {
    
    private ModelStatistics statistics;
    private Tree table;
    private TreeItem currentItem = null;
    
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
        //Set the qualimaster-icon for the login-shell.
        
        final TreeViewer viewer = new TreeViewer(body, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        table = viewer.getTree();
        table.setLinesVisible(true);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        TreeColumn tc1 = new TreeColumn(table, SWT.CENTER);
        TreeColumn tc2 = new TreeColumn(table, SWT.CENTER);
        tc1.setText("Type");
        tc2.setText("No. of Elements");
        tc1.setWidth(380);
        tc2.setWidth(180);
        table.setHeaderVisible(true);
        
        // Variables
        fillTable();
        
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
        currentItem.setExpanded(true);
        
        addValue("Total Variables", statistics.noOfVariables());
        addNestedValue("Non Constraint Variables (Nested & Non Nested)", statistics.noOfNormalVariables());
        addNestedValue("Non Constraint Variables (not Nested in Container)",
            statistics.noOfNormalVariablesNoContainer());
        addNestedValue("Constraint Variables (Nested & Non Nested)", statistics.noOfConstraintVariables());
        addNestedValue("Constraint Variables (not Nested in Container)",
            statistics.noOfConstraintVariablesNoContainer());
        currentItem.setExpanded(true);
        
        addValue("All Pipelines", statistics.noOfPipelines() + statistics.noOfSubPipelines());
        addNestedValue("Pipelines", statistics.noOfPipelines());
        addNestedValue("Subpipelines", statistics.noOfSubPipelines());
        currentItem.setExpanded(true);
        
        addValue("Pipeline Elements", statistics.noOfSources() + statistics.noOfFamilyElements()
            + statistics.noOfDataManagementElements() + statistics.noOfReplaySinks() + statistics.noOfSinks());
        addNestedValue("Sources", statistics.noOfSources());
        addNestedValue("Family Elements", statistics.noOfFamilyElements());
        addNestedValue("Data Mangement Elements", statistics.noOfDataManagementElements());
        addNestedValue("Replay Sinks", statistics.noOfReplaySinks());
        addNestedValue("Sinks", statistics.noOfSinks());
        currentItem.setExpanded(true);
        
        addValue("Algorithms", statistics.noOfSWAlgorithms() + statistics.noOfHWAlgorithms()
            + statistics.noOfSPAlgorithms());
        addNestedValue("Software Algorithms", statistics.noOfSWAlgorithms());
        addNestedValue("Hardware Algorithms", statistics.noOfHWAlgorithms());
        addNestedValue("Sup Pipeline Algorithms", statistics.noOfSPAlgorithms());
        currentItem.setExpanded(true);
        
        addValue("Other Model Elements");
        addNestedValue("General Purpose Machines", statistics.noOfGeneralMachines());
        addNestedValue("Families", statistics.noOfFamilies());
        currentItem.setExpanded(true);
        
        addValue("Constraints", statistics.noOfConstraints() + statistics.noOfOperations()
            + statistics.noOfConstraintInstances() + statistics.noOfConstraintVariables());
        addNestedValue("Constraints Defined on Project Level", statistics.noOfConstraints());
        addNestedValue("Instantiated Constraint of Compounds", statistics.noOfConstraintInstances());
        addNestedValue("User Defined Operations", statistics.noOfOperations());
        addNestedValue("Constraint Variables", statistics.noOfConstraintVariables());
        currentItem.setExpanded(true);
        
        addValue("Declarations", statistics.noOfToplevelDeclarations() + statistics.noOfNestedDeclarations()
            + statistics.noOfToplevelAnnotations() + statistics.noOfNestedAnnotations());
        addNestedValue("Non Nested Variable Declarations", statistics.noOfToplevelDeclarations());
        addNestedValue("Nested Variable Declarations", statistics.noOfNestedDeclarations());
        addNestedValue("Non Nested Annotations", statistics.noOfToplevelAnnotations());
        addNestedValue("Nested Variable Annotations", statistics.noOfNestedAnnotations());
        currentItem.setExpanded(true);
    };
    
    /**
     * Creates an header element without a value.
     * @param name The header title.
     */
    private void addValue(String name) {
        currentItem = new TreeItem(table, SWT.NONE);
        currentItem.setText(name);
    }
    
    /**
     * Creates an expandable top level item.
     * @param name The name of the item to display in the first column.
     * @param value The value, to display in the second column.
     */
    private void addValue(String name, int value) {
        currentItem = new TreeItem(table, SWT.NONE);
        currentItem.setText(new String[] {name, String.valueOf(value)});
    }
    
    /**
     * Creates nested item.
     * @param name The name of the item to display in the first column.
     * @param value The value, to display in the second column.
     */
    private void addNestedValue(String name, int value) {
        if (null != currentItem) {
            TreeItem item = new TreeItem(currentItem, SWT.NONE);
            item.setText(new String[] {name, String.valueOf(value)});
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
        newShell.setSize(600, 700);
        
        super.configureShell(newShell);
        newShell.setText("Model Statistics");
        DialogsUtil.centerShell(newShell);
    }

}
