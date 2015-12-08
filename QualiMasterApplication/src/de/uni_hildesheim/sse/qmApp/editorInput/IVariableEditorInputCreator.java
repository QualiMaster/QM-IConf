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

import org.eclipse.ui.IPersistableElement;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;

/**
 * A specialized editor input creator for IVML decision variables.
 * 
 * @author Holger Eichelberger
 */
public interface IVariableEditorInputCreator extends IEditorInputCreator, IPersistableElement {

    /**
     * Returns the underlying variable.
     * 
     * @return the underlying variable
     */
    public IDecisionVariable getVariable();
    
}
