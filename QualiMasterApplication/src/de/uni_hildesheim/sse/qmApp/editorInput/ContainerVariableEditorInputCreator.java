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

import org.eclipse.ui.IMemento;

import de.uni_hildesheim.sse.qmApp.editorInput.ContainerVariableEditorInputChangeListener
    .IContainerVariableEditorInputChangeListener;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Allows to defer the creation of an editor input instance for a container variable.
 * 
 * @author Holger Eichelberger
 */
public class ContainerVariableEditorInputCreator extends AbstractVariableEditorInputCreator
    implements IContainerVariableEditorInputChangeListener {

    private static final String INDEX = "index";
    private int index;
    
    /**
     * Creates the creator from the given <code>modelPart</code> and the variable denoted by <code>variableName</code> 
     * and <code>index</code>.
     * 
     * @param modelPart the model part
     * @param variableName the name of the variable to be used for creating the editor
     * @param index the index access into the container
     */
    public ContainerVariableEditorInputCreator(IModelPart modelPart, String variableName, int index) {
        super(modelPart, variableName);
        this.index = index;
        ContainerVariableEditorInputChangeListener.INSTANCE.add(variableName, this);
    }
    
    /**
     * Creates an instance from a given memento.
     * 
     * @param memento the memento to create the instance from
     */
    public ContainerVariableEditorInputCreator(IMemento memento) {
        super(memento);
        index = memento.getInteger(INDEX);
    }

    @Override
    public IDecisionVariable getVariable() {
        IDecisionVariable var = super.getVariable();
        return (null != var && var.getNestedElementsCount() >= 1)
            ? Configuration.dereference(var.getNestedElement(index)) : null;
    }
    
    @Override
    public boolean holds(IDecisionVariable variable) {
        // not equals or contains!
        boolean equal = getVariable().equals(variable);
        if (!equal) {
            // Fall back compare parent variable name and index
            String varName = getVariableName() + " [" + index + "]";
            equal = varName.equals(variable.getDeclaration().getName());
        }
        return equal;
    }

    @Override
    public void saveState(IMemento memento) {
        super.saveState(memento);
        memento.putInteger(INDEX, index);
    }

    @Override
    public String getFactoryId() {
        return ContainerVariableEditorInputCreatorElementFactory.ID;
    }

    @Override
    public void notifyDeletetion(String parentName, int index) {
        String thisParentName = super.getVariable().getDeclaration().getName().toLowerCase();
        if (parentName.toLowerCase().equals(thisParentName) && index <= this.index) {
            this.index--;
        }
        
    }

}
