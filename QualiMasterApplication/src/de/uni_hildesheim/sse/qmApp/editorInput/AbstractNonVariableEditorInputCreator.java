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

import java.util.List;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.treeView.IMenuContributor;

/**
 * Provides an abstract implementation of a non-variable-based editor input creator.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractNonVariableEditorInputCreator implements IEditorInputCreator {

    @Override
    public IDatatype getType() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public void createArtifacts() {
        // do nothing
    }

    @Override
    public boolean isWritable() {
        return false;
    }
    
    @Override
    public boolean isDeletable() {
        return false;
    }

    @Override
    public CloneMode isCloneable() {
        return CloneMode.NONE;
    }

    @Override
    public boolean isReadable() {
        return false;
    }

    @Override
    public void delete(Object source, IModelPart modelPart) {
    }
    
    @Override
    public List<IDecisionVariable> clone(int cloneCount) {
        return null;
    }
    
    @Override
    public boolean holds(IDecisionVariable variable) {
        return false;
    }

    @Override
    public boolean isReferencedIn(IModelPart modelPart, IModelPart... defining) {
        return false;
    }
    
    @Override
    public IMenuContributor getMenuContributor() {
        return null;
    }

}
