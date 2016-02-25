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
package de.uni_hildesheim.sse.qmApp.editorInput;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;

/**
 * Some editor input utilities.
 * 
 * @author Holger Eichelberger
 */
public class EditorInputUtils {

    /**
     * Returns the variable of the active editor if possible.
     * 
     * @return the variable of the active editor, <b>null</b> if no active editor or variable
     *   cannot be retrieved
     */
    public static IDecisionVariable getActiveEditorVariable() {
        IDecisionVariable result = null;
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        if (null != activePage) {
            IEditorPart activeEditor = activePage.getActiveEditor();
            if (null != activeEditor) {
                IEditorInput input = activeEditor.getEditorInput();
                if (input instanceof IVariableEditorInput) {
                    result = ((IVariableEditorInput) input).getVariable();
                }
            }
        }
        return result;
    }
    
    /**
     * Tries to identify <code>tmp</code> as contained variable of {@link #getActiveEditorVariable()}.
     * 
     * @param tmp the temporary variable to identify (may be <b>null</b>)
     * @return the corresponding contained variable of {@link #getActiveEditorVariable()} or 
     *     {@link #getActiveEditorVariable()}
     */
    public static IDecisionVariable getActiveEditorVariable(IDecisionVariable tmp) {
        IDecisionVariable result = getActiveEditorVariable();
        if (null != tmp && (result instanceof CompoundVariable)) {
            CompoundVariable comp = (CompoundVariable) result;
            AbstractVariable tmpVar = tmp.getDeclaration();
            for (int c = 0; c < comp.getNestedElementsCount(); c++) {
                IDecisionVariable nested = comp.getNestedElement(c);
                if (nested.getDeclaration().equals(tmpVar)) {
                    result = nested;
                    break;
                }
            }
        }
        return result;
    }
    
}
