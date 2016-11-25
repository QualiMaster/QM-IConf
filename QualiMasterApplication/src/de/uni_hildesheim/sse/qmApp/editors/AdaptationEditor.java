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

import static eu.qualimaster.easy.extension.QmConstants.PROJECT_ADAPTIVITYCFG;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_OBSERVABLES;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_OBSERVABLESCFG;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_OBSERVABLE_TYPE;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_QPARAMWEIGHTING_PARAMETER;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_QPARAMWEIGHTING_WEIGHT;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_ADAPTIVITY_QPARAMWEIGHTING;
import static eu.qualimaster.easy.extension.QmConstants.VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_ADAPTIVITY_PIPELINEIMPORTANCE;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import de.uni_hildesheim.sse.qmApp.model.ConnectorUtils;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import eu.qualimaster.easy.extension.internal.VariableHelper;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.ContainerVariable;
import net.ssehub.easy.varModel.confModel.IConfigurationElement;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.confModel.SequenceVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import qualimasterapplication.Activator;

/**
 * Implements an extended editor for adaptation settings.
 * 
 * @author Holger Eichelberger
 * @author Patrik Pastuschek
 */
public class AdaptationEditor extends ProjectEditor {

    private IConfigurationElement link;
    private Map<IDecisionVariable, IDecisionVariable> observerMap = new HashMap<IDecisionVariable, IDecisionVariable>();
    
    @Override
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        ConnectorUtils.configure();
    }
    
    @Override
    public void variableChanged(EventKind kind, IDecisionVariable variable, int globalIndex) {
        super.variableChanged(kind, variable, globalIndex);
        
        if (isChangeEventProcessingEnabled()) {
            IModelElement par = variable.getDeclaration().getTopLevelParent();
            //dangerous, potential freeze/crash
            while (!(par instanceof Project)) {
                par = par.getParent();
            }
//            if (null != variable && EventKind.ADDED == kind) {
//                addDefaultValues(variable);
//            }
            if (par instanceof Project) {
                Project prj = (Project) par;
                Configuration config = this.getConfiguration();
                if (null != config) {
                    prj = config.getProject();
                    System.out.println(prj.getName());
                }
                if (PROJECT_OBSERVABLESCFG.equals(prj.getName())) {
                    String type = VariableHelper.getString(variable, SLOT_OBSERVABLE_TYPE);
                    if (null != type) {
                        handleVariableChangedEvent(kind, variable, type);
                    }
                } else if (PROJECT_OBSERVABLES.equals(prj.getName())) {
                    if (null == link && null != variable.getParent()) {
                        link = variable.getParent();
                    }
                    if (EventKind.DELETED == kind && link instanceof SequenceVariable) {
                        SequenceVariable sequence = (SequenceVariable) link;
                        deleteMappedAdaption(sequence, variable);
                    }                    
                } else if (PROJECT_ADAPTIVITYCFG.equals(prj.getName())) {
                    if (null == link && null != variable.getParent()) {
                        link = variable.getParent();
                    }
                    String type = VariableHelper.getString(variable, SLOT_OBSERVABLE_TYPE);
                    handleVariableChangedEvent(kind, variable, type);
//                    if (EventKind.DELETED == kind && link instanceof SequenceVariable) {
//                        SequenceVariable sequence = (SequenceVariable) link;
//                        deleteAdaption(sequence, variable);
//                    } else if (EventKind.ADDED == kind && link instanceof SequenceVariable) {
//                        SequenceVariable sequence = null;
//                        Iterator<IDecisionVariable> iter = config.iterator();
//                        while (iter.hasNext() && null == sequence) {
//                            IDecisionVariable iVar = iter.next();
//                            if (iVar.getQualifiedName().endsWith("pipelineImportance") 
//                                    && iVar instanceof SequenceVariable) {
//                                sequence = (SequenceVariable) iVar;
//                            }
//                        }
//                        
//                        addAdaption(sequence, variable);
//                    }
                }
            }
        }
    }
    
    /**
     * Adds a weight to the adaption part.
     * @param sequence The sequence to add to.
     * @param variable The actual variable to add to.
     */
    private void addAdaption(SequenceVariable sequence, IDecisionVariable variable) {
        try {
//            int i = AdaptationEditor.getPosition(variable);
            
            sequence.addNestedElement();
            int i = sequence.getNestedElementsCount();
            IDecisionVariable toAdd = sequence.getNestedElement(i - 1);
            IDecisionVariable example = sequence.getNestedElement(0);
            IDecisionVariable name = toAdd.getNestedElement("weight");
            toAdd.getNestedElement("weight")
                .setValue(ValueFactory.createValue(IntegerType.TYPE, "1"), AssignmentState.ASSIGNED);
            System.out.println(toAdd.toString());

//            System.out.println("nothing");
//            weight.setValue(ValueFactory.createValue(IntegerType.TYPE, "1"), AssignmentState.ASSIGNED);
//            Value newValue = ValueFactory.createValue(Compound.TYPE, "name", name, "weight", weight);
//            sequence.getNestedElement(i - 1).setValue(newValue, AssignmentState.ASSIGNED);
        } catch (ConfigurationException e) {
            e.printStackTrace();
        } catch (ValueDoesNotMatchTypeException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes the adaption part from the model if it is deleted via the UI.
     * @param sequence The sequence to delete from.
     * @param variable The actual variable to delete.
     */
    private void deleteAdaption(SequenceVariable sequence, IDecisionVariable variable) {
        
        int i = AdaptationEditor.getPosition(variable);
        IDecisionVariable toRemove = sequence.getNestedElement(i - 1);
        boolean success = sequence.removeNestedElement(toRemove);
    }
    
    /**
     * Deletes the adaption part from the model if it is deleted via the UI.
     * @param sequence The sequence to delete from.
     * @param variable The actual variable to delete.
     */
    private void deleteMappedAdaption(SequenceVariable sequence, IDecisionVariable variable) {
        int toDelete = -1;
        for (int i = 0; i < sequence.getNestedElementsCount(); i++) {
            if (sequence.getNestedElement(i) instanceof CompoundVariable) {
                CompoundVariable compound = (CompoundVariable) sequence.getNestedElement(i);
                IDecisionVariable derefName = Configuration.dereference(compound.getNestedElement(0));
                String name = derefName.getNestedElement(SLOT_OBSERVABLE_TYPE).getValue()
                        .toString().split(":")[0].trim();
                String varName = null;
                IDecisionVariable varTypeName = variable.getNestedElement(SLOT_OBSERVABLE_TYPE);
                if (null != varTypeName.getValue()) {
                    varName = variable.getNestedElement(SLOT_OBSERVABLE_TYPE).getValue()
                            .toString().split(":")[0].trim();
                }
                if (name.equals(varName)) {
                    toDelete = i;
                    break;
                }
            }
        }
        if (-1 != toDelete) {
            IDecisionVariable toRemove = sequence.getNestedElement(toDelete);
            sequence.removeNestedElement(toRemove);
        }
    }
    
    /**
     * Handles a variable changed event.
     * 
     * @param kind the event kind
     * @param variable the causing variable
     * @param observableType the observable type causing the variable change
     */
    private void handleVariableChangedEvent(EventKind kind, IDecisionVariable variable, String observableType) {
        Configuration adaptCfg = VariabilityModel.Configuration.ADAPTIVITY.getConfiguration();
        Project adaptPrj = adaptCfg.getProject();
        try {
            AbstractVariable pipImp = ModelQuery.findVariable(adaptPrj, VAR_ADAPTIVITY_PIPELINEIMPORTANCE, null);
            AbstractVariable crossPip = ModelQuery.findVariable(adaptPrj, VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS, null);
            handleVariableChangedEvent(adaptCfg.getDecision(pipImp), kind, variable, observableType);
            handleVariableChangedEvent(adaptCfg.getDecision(crossPip), kind, variable, observableType);
        } catch (ModelQueryException e) {
            Activator.getLogger(getClass()).exception(e);
        }
    }

    /**
     * Handles a variable changed event.
     * 
     * @param container the container to adjust
     * @param kind the event kind
     * @param variable the causing variable
     * @param observableType the observable type causing the variable change
     */
    private void handleVariableChangedEvent(IDecisionVariable container, EventKind kind, IDecisionVariable variable, 
        String observableType) {
        if (null != container && container instanceof ContainerVariable) {
            ContainerVariable cVar = (ContainerVariable) container;
            IDecisionVariable found = null;
            for (int n = 0; null == found && n < container.getNestedElementsCount(); n++) {
                IDecisionVariable var = Configuration.dereference(container.getNestedElement(n));
                String obsType = VariableHelper.getString(var, SLOT_OBSERVABLE_TYPE);
                if (null != observableType && observableType.equals(obsType)) {
                    found = var;
                }
            }
            switch (kind) {
            case ADDED:
                if (null == found && null != observableType) {
                    try {
                        IDatatype qParamType = ModelQuery.findType(container.getConfiguration().getProject(), 
                            TYPE_ADAPTIVITY_QPARAMWEIGHTING, null);
                        found = cVar.addNestedElement();
                        Value refValue = ValueFactory.createValue(
                                new Reference("", variable.getDeclaration().getType(),
                                       container.getConfiguration().getProject()), variable.getDeclaration()
                                );
                        Value newValue = ValueFactory.createValue(qParamType, new Object[]{
                            //SLOT_QPARAMWEIGHTING_PARAMETER, variable.getDeclaration(), 
                            SLOT_QPARAMWEIGHTING_PARAMETER, refValue, //refValue,
                            SLOT_QPARAMWEIGHTING_WEIGHT, 1});
                        found.setValue(newValue, AssignmentState.ASSIGNED);
//                        variable.getConfiguration().notifyReplaced(
//                                variable.getConfiguration().getProject(), found.getConfiguration().getProject());
//                        for (int k = 0; k < this.getEditorCount(); k++) {
//                            if (this.getEditor(k) instanceof QualityParameterWeightingEditor) {
//                                QualityParameterWeightingEditor editor = (QualityParameterWeightingEditor) 
//                                        this.getEditor(k);
//                                editor.getContainer().addElement(newValue);
//                            }
//                        }
                        System.out.println(found.toString());
                    } catch (ConfigurationException e) {
                        Activator.getLogger(getClass()).exception(e);
                    } catch (ValueDoesNotMatchTypeException e) {
                        Activator.getLogger(getClass()).exception(e);
                    } catch (ModelQueryException e) {
                        Activator.getLogger(getClass()).exception(e);
                    }
                }
                break;
            case CHANGED:
                break;
            case DELETED:
                //if an adaption element is deleted, we also need to delete the observables of that element.
                if (null != found) {
                    cVar.removeNestedElement(found);
                }
                break;
            case DELETING:
                break;
            default:
                break;
            }
        }
    }
    
    /**
     * Returns the position of the targeted variable in the sequence.
     * @param variable The variable.
     * @return The position or -1 if failed.
     */
    public static int getPosition(IDecisionVariable variable) {
        int result = -1;
        String number = variable.getQualifiedName().split("[\\[\\]]")[1];
        try {
            result = Integer.parseInt(number);
        } catch (NumberFormatException exc) {
            //do nothing for now
        }
        return result;
    }
    
    /**
     * Returns the position of the targeted variable in the sequence.
     * @param variable The variable.
     * @return The position or -1 if failed.
     */
    public static int getAcuratePosition(IDecisionVariable variable) {
        int result = -1;
        if (variable.getParent() instanceof IDecisionVariable) {
            IDecisionVariable parent = (IDecisionVariable) variable.getParent();
            for (int i = 0; i < parent.getNestedElementsCount(); i++) {
                //check if they are the same
                try {
                    if (parent.getNestedElement(i).equals(variable)) {
                        result = i;
                        break;
                    }
                } catch (NullPointerException exc) {
                    //do nothing
                }
            }
        } else {
            result = getPosition(variable);
        }
        return result;
    }
    
}
