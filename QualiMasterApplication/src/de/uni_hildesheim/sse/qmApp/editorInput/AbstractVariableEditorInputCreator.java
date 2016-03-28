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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;

import de.uni_hildesheim.sse.qmApp.editors.DecisionVariableEditorInput;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.qmApp.treeView.IMenuContributor;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * An abstract variable editor input creator holding a specified model variable.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractVariableEditorInputCreator implements IVariableEditorInputCreator, IAdaptable {

    private static final String VARIABLE_NAME = "variableName";
    private static final String MODEL_PART_NAME = "modelPartName";
    
    private IModelPart modelPart;
    private String variableName;
    // don't store variable here!!!
    
    /**
     * Creates a variable-based editor input creator.
     * 
     * @param modelPart the model part containing the variable
     * @param variableName the name of the variable
     */
    protected AbstractVariableEditorInputCreator(IModelPart modelPart, String variableName) {
        this.modelPart = modelPart;
        this.variableName = variableName;
    }

    /**
     * Creates a variable input creator from a given memento.
     * 
     * @param memento the memento to create the instance from
     */
    protected AbstractVariableEditorInputCreator(IMemento memento) {
        variableName = memento.getString(VARIABLE_NAME);
        modelPart = VariabilityModel.findModelPart(memento.getString(MODEL_PART_NAME));
    }
    
    @Override
    public IEditorInput create() {
        return new DecisionVariableEditorInput(getVariable(), this); // use getVariable() as it may be overridden
    }

    @Override
    public IDecisionVariable getVariable() {
        return ModelAccess.obtainVariable(modelPart, variableName);
    }
    
    @Override
    public IDatatype getType() {
        IDecisionVariable var = getVariable();
        return null == var ? null : var.getDeclaration().getType();
    }
    
    @Override
    public String getName() {
        IDecisionVariable var = getVariable();
        return null == var ? "" : ModelAccess.getDisplayName(var);
    }

    @Override
    public boolean isEnabled() {
        return null != getVariable();
    }

    @Override
    public void createArtifacts() {
        // do nothing, model operations are sufficient
    }
    
    @Override
    public boolean isWritable() {
        return VariabilityModel.isWritable(modelPart);
    }
    
    @Override
    public boolean isDeletable() {
        return VariabilityModel.isDeletable(modelPart);
    }

    @Override
    public CloneMode isCloneable() {
        return VariabilityModel.isCloneable(modelPart);
    }
    
    @Override
    public boolean isReadable() {    
        return VariabilityModel.isReadable(modelPart);
    }

    @Override
    public void delete(Object source, IModelPart modelPart) {
        IDecisionVariable variable = getVariable();
        int globalIndex = ModelAccess.getGlobalIndex(modelPart, variable);
        ChangeManager.INSTANCE.variableDeleting(source, variable, globalIndex);
        ModelAccess.deleteElement(modelPart, variable);
        ChangeManager.INSTANCE.variableDeleted(source, variable, globalIndex);        
    }
    
    @Override
    public List<IDecisionVariable> clone(int cloneCount) {
        return ModelAccess.cloneElement(modelPart, getVariable(), false, cloneCount); 
    }
    
    @Override
    public boolean holds(IDecisionVariable variable) {
        return Utils.equalsOrContains(getVariable(), variable);
    }

    @Override
    public boolean isReferencedIn(IModelPart modelPart, IModelPart... defining) {
        return ModelAccess.isReferencedIn(modelPart, getVariable(), defining);
    }
    
    @Override
    public IMenuContributor getMenuContributor() {
        return null;
    }

    @Override
    public void saveState(IMemento memento) {
        memento.putString(VARIABLE_NAME, variableName);
        memento.putString(MODEL_PART_NAME, modelPart.getModelName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        return null;
    }

}
