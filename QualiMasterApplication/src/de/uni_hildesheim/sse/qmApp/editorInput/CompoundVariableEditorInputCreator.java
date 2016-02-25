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
import org.eclipse.ui.IMemento;

import de.uni_hildesheim.sse.qmApp.editors.DecisionVariableEditorInput;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;

/**
 * Allows to defer the creation of an editor input instance for a compound variable.
 * 
 * @author Holger Eichelberger
 */
public class CompoundVariableEditorInputCreator extends AbstractVariableEditorInputCreator {

    /**
     * Creates the creator from the given <code>modelPart</code> and the variable denoted by <code>variableName</code>.
     * 
     * @param modelPart the model part
     * @param variableName the name of the variable to be used for creating the editor
     */
    public CompoundVariableEditorInputCreator(IModelPart modelPart, String variableName) {
        super(modelPart, variableName);
    }
    
    /**
     * Creates an instance from a given memento.
     * 
     * @param memento the memento to create the instance from
     */
    public CompoundVariableEditorInputCreator(IMemento memento) {
        super(memento);
    }

    @Override
    public IEditorInput create() {
        return new DecisionVariableEditorInput(getVariable(), this);
    }

    @Override
    public String getFactoryId() {
        return CompoundVariableEditorInputCreatorElementFactory.ID;
    }

}
