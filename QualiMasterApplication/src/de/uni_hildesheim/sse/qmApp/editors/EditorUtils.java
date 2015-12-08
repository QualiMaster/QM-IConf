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

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
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
     * @return the created label
     */
    public static Label createLabel(UIConfiguration uiCfg, IDecisionVariable var) {
        String labelText = ModelAccess.getLabelName(var.getDeclaration());
        Label label = new Label(uiCfg.getParent().getContentPane(), SWT.NONE);
        label.setText(labelText);
        return label;
    }

}
