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
package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.filter.mandatoryVars.VariableContainer;
import de.uni_hildesheim.sse.qmApp.WorkspaceUtils;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;

/**
 * Some helpful editor support functions.
 * 
 * @author Holger Eichelberger
 */
public class EditorUtils {

    /**
     * Prevents external creation.
     */
    private EditorUtils() {
    }
    
    /**
     * Creates a UI label for <code>var</code> in <code>parent</code>.
     * 
     * @param uiCfg the (UI) configuration.
     * @param var the decision variable to create the parent for
     * @param importances optional information whether variables are required to be configured, may be <b>null</b>
     * @return the created label
     */
    public static Label createLabel(UIConfiguration uiCfg, IDecisionVariable var, VariableContainer importances) {
        String labelText = ModelAccess.getLabelName(var.getDeclaration());
        if (null != importances) {
            if (importances.isMandatory(var)) {
                labelText += "*";
            }
        }
        Label label = new Label(uiCfg.getParent().getContentPane(), SWT.NONE);
        label.setText(labelText);
        assignHelpText(var, label);
        return label;
    }
    
    /**
     * Assigns the help text for <code>var</code> to <code>control</code>.
     * 
     * @param var the variable to obtain the help text for
     * @param control the control to assign the help text to 
     * @return <code>control</code>
     */
    public static Control assignHelpText(IDecisionVariable var, Control control) {
        final String helpText = ModelAccess.getHelpText(var);
        if (null != helpText && helpText.length() > 0) {
            // pipeline editor does not support tool tips in properties view, switch to status line manager
            //control.setToolTipText(helpText);
            control.addMouseTrackListener(new MouseTrackAdapter() {

                @Override
                public void mouseHover(MouseEvent evt) {
                    IStatusLineManager manager = WorkspaceUtils.getActiveStatusLineManager(true);
                    if (null != manager) {
                        manager.setMessage(helpText);
                    }
                }
                
                @Override
                public void mouseExit(MouseEvent evt) {
                    IStatusLineManager manager = WorkspaceUtils.getActiveStatusLineManager(true);
                    if (null != manager) {
                        manager.setMessage(""); // we do not have the actual message before...
                    }
                }
            
            });
        }
        return control;
    }

}
