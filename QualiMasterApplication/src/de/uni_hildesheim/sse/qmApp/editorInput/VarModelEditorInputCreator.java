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

import de.uni_hildesheim.sse.qmApp.editors.VarModelEditorInput;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;

/**
 * Represents a deferred editor input creator for variability models, i.e., related IVML configurations.
 * 
 * @author Holger Eichelberger
 */
public class VarModelEditorInputCreator extends AbstractNonVariableEditorInputCreator {

    private IModelPart modelPart;
    private String name;
    
    /**
     * Creates a var model editor input creator.
     * 
     * @param modelPart the name of the model part holding the configuration
     * @param name the display name
     */
    public VarModelEditorInputCreator(IModelPart modelPart, String name) {
        this.modelPart = modelPart;
        this.name = name;
    }
    
    @Override
    public IEditorInput create() {
        return new VarModelEditorInput(modelPart.getConfiguration(), name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
    
    @Override
    public void createArtifacts() {
        // do nothing, model operations are sufficient
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
        return true; // for demo
    }
    
}
