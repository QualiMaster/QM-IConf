/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelineNodeType;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelinesRuntimeUtils.CustomObservableList;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.QmObservables;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.confModel.SequenceVariable;
import net.ssehub.easy.varModel.confModel.SetVariable;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Provides access to the observables.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public class Observables {

    private static boolean initialized = false;
    private static Map<PipelineNodeType, CustomObservableList> observables 
        = new HashMap<PipelineNodeType, CustomObservableList>();
    private static HashMap<String, String> observablesMap = new HashMap<String, String>();
    // not nice but I was not able to also clean that part up
    private static HashMap<String, String> fallback = new HashMap<String, String>();

    /**
     * Initializes the observables.
     */
    public static void initObservables() {
        if (!initialized) {
            initialized = true;
            Set<String> observablesSet = new HashSet<String>();
            Configuration config = VariabilityModel.Configuration.OBSERVABLES.getConfiguration();
            Iterator<IDecisionVariable> iter = config.iterator();
            while (iter.hasNext()) {
                IDecisionVariable var = iter.next();
                if (var instanceof SetVariable || var instanceof SequenceVariable) {
                    collectQualityParameters(var, observablesSet);
                }
            }
            determineQMObservables(observablesSet);
            Project project = VariabilityModel.Configuration.PIPELINES.getConfiguration().getProject();
            observables.put(PipelineNodeType.Pipeline, 
                gatherPipelineObservables(project, QmConstants.TYPE_PIPELINE_NODE, observablesSet));
            observables.put(PipelineNodeType.Source, 
                gatherPipelineObservables(project, QmConstants.TYPE_SOURCE, observablesSet));
            observables.put(PipelineNodeType.Sink, 
                gatherPipelineObservables(project, QmConstants.TYPE_SINK, observablesSet));
            observables.put(PipelineNodeType.FamilyElement, 
                gatherPipelineObservables(project, QmConstants.TYPE_FAMILYELEMENT, observablesSet));
            observables.put(PipelineNodeType.DataManagementElement, 
                gatherPipelineObservables(project, QmConstants.TYPE_DATAMANAGEMENTELEMENT, observablesSet));
            observables.put(PipelineNodeType.Pipeline, 
                gatherPipelineObservables(project, QmConstants.TYPE_PIPELINE, observablesSet));
            observables.put(PipelineNodeType.SubPipeline, 
                gatherPipelineObservables(project, QmConstants.TYPE_SUBPIPELINE, observablesSet));
            observables.put(PipelineNodeType.Flow, 
                gatherPipelineObservables(project, QmConstants.TYPE_FLOW, observablesSet));
        }
    }
    
    /**
     * Returns the observables for type.
     * 
     * @param type the type to return the observables for
     * @return the observables (may be <b>null</b>)
     */
    public static CustomObservableList getObservables(PipelineNodeType type) {
        return null == type ? null : observables.get(type);
    }
    
    /**
     * Gathers the pipeline observables from an IVML type.
     * 
     * @param project the project to gather the type declaration from
     * @param type the type name
     * @param observablesSet the configured (non-internal) observables
     * @return the observables
     */
    private static CustomObservableList gatherPipelineObservables(Project project, String type, 
        Set<String> observablesSet) {
        CustomObservableList result = new CustomObservableList();
        IDatatype decl = null;
        try {
            decl = ModelQuery.findType(project, type, null);
        } catch (ModelQueryException e) {
        }
        do {
            if (decl instanceof Compound) {
                Compound compound = (Compound) decl;
                for (int i = 0; i < compound.getAssignmentCount(); i++) {
                    AttributeAssignment assignment = compound.getAssignment(i);
                    for (int j = 0; j < assignment.getElementCount(); j++) {
                        DecisionVariableDeclaration declaration = assignment.getElement(j);
                        String name = declaration.getName();
                        String dType = declaration.getType().getName();
                        if (observablesSet.contains(dType)) {
                            name = name.replaceAll("[^a-zA-Z0-9]", "");
                            result.add(name);
                            fallback.put(name, dType);
                        }
                    }
                }
                if (compound.getRefinesCount() > 0) {
                    decl = compound.getRefines(0); // legacy, consider only the first one
                } else {
                    decl = null;
                }
            } else {
                decl = null;
            }
        } while (null != decl);
        return result;
    }
    
    /**
     * Collect the qualityParameters from the ivml-configuration, so they can be mapped upn pipeline-elements.
     * 
     * @param var decision variable holding the the observables
     * @param observablesSet the observables set to be modified as a side effect
     */
    private static void collectQualityParameters(IDecisionVariable var, Set<String> observablesSet) {
        String name = "";
        ConstantValue value = null;
        if (var instanceof SetVariable) {
            SetVariable set = (SetVariable) var;
            name = set.getDeclaration().getName();

            if (name.equals(QmConstants.VAR_OBSERVABLES_QUALITYPARAMS)) {
                value = (ConstantValue) set.getDeclaration().getDefaultValue();
            }
        }
        if (var instanceof SequenceVariable) {
            SequenceVariable sequence = (SequenceVariable) var;
            name = sequence.getDeclaration().getName();

            if (name.equals(QmConstants.VAR_OBSERVABLES_QUALITYPARAMS)) {
                value = (ConstantValue) sequence.getDeclaration().getDefaultValue();
            }
        }

        if (!"".equals(name) && value != null) {
            ContainerValue container = (ContainerValue) value.getConstantValue();
            for (int i = 0; i < container.getElementSize(); i++) {
                CompoundValue cmpValue = (CompoundValue) container.getElement(i);
                Compound cType = (Compound) cmpValue.getType();

                for (int j = 0; j < cType.getInheritedElementCount(); j++) {
                    DecisionVariableDeclaration slotDecl = cType.getInheritedElement(j);
                    Value nestedValue = cmpValue.getNestedValue(slotDecl.getName());
                    if (nestedValue instanceof StringValue) {
                        observablesSet.add(((StringValue) nestedValue).getValue());
                    }
                }
            }
        }
    }

    /**
     * Determine the QM observables with given String.
     * 
     * @param observablesSet the observables set
     */
    private static void determineQMObservables(Set<String> observablesSet) {
        ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();
        Iterator<String> obsIterator = observablesSet.iterator();
        while (obsIterator.hasNext()) {
            String iterObs = (String) obsIterator.next();
            for (int j = 0; j < allPossibleObservables.size(); j++) {
                String oldPossObsString = allPossibleObservables.get(j);
                String newIterObsString = iterObs.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
                String newPossObsString = oldPossObsString.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase();
                if (newIterObsString.equals(newPossObsString)
                        || newIterObsString.substring(0, newIterObsString.
                    length() - 1).equals(newPossObsString) || newPossObsString.substring(0, newPossObsString.
                            length() - 1).equals(newIterObsString)) {

                    observablesMap.put(iterObs, oldPossObsString);
                }
            }
        }
    }
    
    /**
     * Returns the (pseudo) IVML name for the given infrastructure observable name.
     * 
     * @param observableName the infrastructure observable name
     * @return the IVML name (may be <b>null</b>)
     */
    public static String getIvmlObservableName(String observableName) {
        String result = null;
        if (null != observableName) {
            result = observablesMap.get(observableName);
            if (null == result) {
                String tmp = fallback.get(observableName);
                if (null != tmp) {
                    result = observablesMap.get(tmp);
                }
            }
        }
        return result;
    }

}
