/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy.ui.productline_editor.DelegatingEasyEditorPage;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.values.ReferenceValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.qmApp.editors.EditorUtils;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;

/**
 * Implements a dialog to select a variable.
 * 
 * @author Holger Eichelberger
 */
public class VariableSelectorDialog extends AbstractDialog {

    private String inputHint;
    private IModelPart modelPart;
    private IDatatype variableType;

    private UIConfiguration uiCfg;
    private IDecisionVariable variable;
    
    /**
     * Creates the dialog.
     * 
     * @param parentShell the parent shell
     * @param modelPart the model part to take the values from
     * @param variableType the type of the variable to select from (i.e., determine a selection of
     *   currently possible values via <code>modelPart</code>)
     * @param inputHint hint on which kind of variable to select to be used as part of the title
     */
    public VariableSelectorDialog(Shell parentShell, IModelPart modelPart, IDatatype variableType, 
        String inputHint) {
        super(parentShell);
        this.inputHint = inputHint;
        this.modelPart = modelPart;
        this.variableType = variableType;
        setBlockOnOpen(true);
    }
    
    /**
     * Returns the temporary variable to derive UI editors from.
     * 
     * @return the temporary variable (may be <b>null</b> if the variable cannot be created
     */
    protected IDecisionVariable getTemporaryVariable() {
        IDecisionVariable result = null;
        try {
            Project prj = new Project("tmp");
            Project srcPrj = modelPart.getConfiguration().getProject();
            ProjectImport imp = new ProjectImport(srcPrj.getName());
            imp.setResolved(srcPrj);
            prj.addImport(imp);
            IDatatype varType = new Reference("", variableType, prj);
            DecisionVariableDeclaration decVar = new DecisionVariableDeclaration("tmp", varType, prj);
            decVar.setComment("Source element:");
            prj.add(decVar);
            Configuration cfg = new Configuration(prj);
            result = cfg.getDecision(decVar);
        } catch (ModelManagementException e) {
            getLogger().exception(e);
        }
        return result;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout();
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layout.numColumns = 2;
        composite.setLayout(layout);
        composite.setLayoutData(gridData);
        
        variable = getTemporaryVariable();
        if (null != variable) {
            uiCfg = ConfigurationTableEditorFactory.createConfiguration(variable.getConfiguration(), 
                new DelegatingEasyEditorPage(composite));
            Label label = EditorUtils.createLabel(uiCfg, variable);
            label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
            Control editor = ConfigurationTableEditorFactory.createEditor(uiCfg, variable);
            editor.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        } else {
            Label label = new Label(composite, SWT.NONE);
            label.setText("Currently no selection possible. Sorry.");
        }
        return composite;
    }
    
    /**
     * Returns the selected variable.
     * 
     * @return the selected variable, may be <b>null</b> if none was selected
     */
    public AbstractVariable getSelected() {
        AbstractVariable result = null;
        if (null != variable) {
            Value value = variable.getValue();
            if (value instanceof ReferenceValue) {
                result = ((ReferenceValue) value).getValue();
            }
        }
        return result; 
    }
    
    @Override
    protected String getTitle() {
        return "Select source for copying " + inputHint;
    }

    @Override
    protected Point getIntendedSize() {
        return new Point(300, 150);
    }

    @Override
    protected void okPressed() {
        uiCfg.commitValues(null);
        super.okPressed();
    }

}
