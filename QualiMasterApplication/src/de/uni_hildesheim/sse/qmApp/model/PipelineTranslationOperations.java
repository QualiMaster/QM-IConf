/*
 * Copyright 2014-2016 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;

import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.qmApp.editors.QMPipelineEditor;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Definition;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.TypeQueries;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.persistency.IVMLWriter;
import pipeline.DataManagementElement;
import pipeline.FamilyElement;
import pipeline.Flow;
import pipeline.Pipeline;
import pipeline.PipelineElement;
import pipeline.PipelineNode;
import pipeline.ReplaySink;
import pipeline.Sink;
import pipeline.Source;
import qualimasterapplication.Activator;

/**
 * The operations of translating the Ecore pipeline diagram to IVML project.
 * 
 * @author Cui Qin
 * @author El-Sharkawy
 */
public class PipelineTranslationOperations {

    public static final String EXTENSION = ".ivml";
    
    // static imports
    private static ProjectImport basicsImport = new ProjectImport(QmConstants.PROJECT_BASICS, null);
    private static ProjectImport pipelinesImport = new ProjectImport(QmConstants.PROJECT_PIPELINES, null);
    private static ProjectImport familiesImport = new ProjectImport(QmConstants.PROJECT_FAMILIESCFG, null);
    private static ProjectImport datamanagementImport = new ProjectImport(QmConstants.PROJECT_DATAMGTCFG, null);
    
    // static model parts
    private static IModelPart familyModelPart = VariabilityModel.Configuration.FAMILIES;
    private static IModelPart datamanagementModelPart = VariabilityModel.Configuration.DATA_MANAGEMENT;
    private static IModelPart pipelineModelPart = VariabilityModel.Configuration.PIPELINES;
    
    // TODO: Move all this static stuff into PipelineSaveContext
    private static List<IFreezable> freezables;
    private static List<PipelineNode> pipNodes;
    private static List<PipelineNode> pipProcessedNodes;
    private static List<Flow> flows;
    private static List<Flow> processedflows;
    private static int sourceCount;
    private static int flowCount;
    private static int dataManagementElementCount;
    private static Pipeline pipeline;
    
    /**
     * grouping enumenation.
     * 
     */
    public enum Grouping {
        shuffleGrouping, fieldsGrouping, globalGrouping, directGrouping, allGrouping, customGrouping, noneGrouping
    };

    /**
     * Returns the static pipeline model part.
     * 
     * @return the static pipeline model part
     */
    public static IModelPart getPipelineModelPart() {
        return pipelineModelPart;
    }

    // TODO although it works, the next method is wrong because it does not put values in the configuration and saves
    // the configuration, but it re-does the entire work here   

    /**
     * Translates the Ecore pipeline diagram to IVML model file.
     * 
     * @param fileURI the Eclipse URI to create the IVML file
     * @return the name of the pipeline in <code>fileURI</code> (may be <b>null</b> if not found / configured)
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String translationFromEcoregraphToIVMLfile(URI fileURI) throws PipelineTranslationException {
        // get pipeline list - one editor for one pipeline
        List<Pipeline> pipelineList = PipelineDiagramUtils.getPipeline(fileURI);
        return translationFromEcoregraphToIVMLfile(fileURI, pipelineList);
    }
    
    /**
     * Translates the Ecore pipeline diagram to IVML model file.
     * 
     * @param fileURI the Eclipse URI to create the IVML file
     * @param pipelineList write the given pipelines
     * @return the name of the pipeline in <code>fileURI</code> (may be <b>null</b> if not found / configured)
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String translationFromEcoregraphToIVMLfile(URI fileURI, List<Pipeline> pipelineList) 
        throws PipelineTranslationException {

        Project modelProject = pipelineModelPart.getConfiguration().getProject();
        PipelineSaveContext context = new PipelineSaveContext(modelProject);
        sourceCount = 0;
        flowCount = 0;
        dataManagementElementCount = 0;
        pipNodes = new ArrayList<PipelineNode>();
        pipProcessedNodes = new ArrayList<PipelineNode>();
        flows = new ArrayList<Flow>();
        processedflows = new ArrayList<Flow>();
        // create a temporary IVML project (not registered with VarModel, no model listeners)
        Project newProject = new Project(PipelineDiagramUtils.getModelName(fileURI) + QmConstants.CFG_POSTFIX);
        // for collecting all freezables
        freezables = new ArrayList<IFreezable>();
        // add imports
        addImports(newProject);
        IVMLModelOperations.addRuntimeAttributeToProject(newProject, modelProject);
        DecisionVariableDeclaration pipelineVariable = null;
        if (!pipelineList.isEmpty() && pipelineList.size() == 1) {
            // add pipeline elements
            pipeline = pipelineList.get(0); //defautly only one pipeline in one editor
            pipelineVariable = addPipelineElement(pipeline, newProject, context);
            handleDisconnectedElements(newProject, context); //handle the disconnected elements in the pipeline
            String pVariableName = pipelineVariable.getName();
            String pDisplayName = pipeline.getName();
            Map<String, String> nameMap = QMPipelineEditor.getPipelineNameAndDisplayname();
            if (!nameMap.containsKey(pVariableName)) {
                nameMap.put(pVariableName, pVariableName);
            } else {
                if (!nameMap.containsValue(nameMap.get(pVariableName))) {
                    nameMap.remove(pVariableName);
                    nameMap.put(pVariableName, pDisplayName);
                }
            }
            IVMLModelOperations.addFreezeBlock(freezables, newProject, modelProject);
        }
        // adds the imports in the main pipeline project
        ProjectImport pImport = new ProjectImport(newProject.getName(), null);
        IVMLModelOperations.modifyImports(modelProject, IVMLModelOperations.ADD, pImport);
        // adds the pipeline to the main project
        IVMLModelOperations.addPipelineToMainProject(pipelineVariable);
        
        // writes the new IVML project to a file
        String filePath = PipelineDiagramUtils.getFolderDirectory(fileURI) + newProject.getName() + EXTENSION;
        File file = new File(filePath);
        boolean exists = file.exists();
        // Write
        try {
            FileWriter fWriter = new FileWriter(file);
            IVMLWriter iWriter = new QualiMasterIvmlWriter(fWriter);
            iWriter.setFormatInitializer(true);
            iWriter.forceComponundTypes(true);
            newProject.accept(iWriter);
            iWriter.flush();
        } catch (IOException e) {
            getLogger().info(e.getMessage());
        }
        // Reload model
        if (!exists) {
            try {
                VarModel.INSTANCE.locations().updateModelInformation();
            } catch (ModelManagementException e) {
                getLogger().info(e.getMessage());
            }
        }
        ModelAccess.store(pipelineModelPart.getConfiguration(), false, true);        
        Project prj = ModelAccess.reloadModel(newProject, file.toURI()); // reloads also pipelineModelPart
        return notifyUpdatePipeline(null == prj ? newProject : prj);
    }
    
    /**
     * Notifies the UI about a change of the pipeline in <code>prj</code>, in particular
     * the name of the pipeline.
     * 
     * @param prj the project to search the variable for
     * @return the name of the updated pipeline in <code>prj</code> (may be <b>null</b> if not found / configured)
     */
    private static String notifyUpdatePipeline(Project prj) {
        String name = null;
        IDecisionVariable changed = null;
        // find the variable declaration of type pipeline in prj (shall be only one due to storage assumption)
        try {
            // imports may not be resolved here...
            IDatatype type = ModelQuery.findType(ModelAccess.getModel(Definition.PIPELINES), "Pipeline", null);
            for (int e = 0; null == changed && e < prj.getElementCount(); e++) {
                ContainableModelElement elt = prj.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    DecisionVariableDeclaration dec = (DecisionVariableDeclaration) elt;
                    if (TypeQueries.sameTypes(type, dec.getType())) {
                        net.ssehub.easy.varModel.confModel.Configuration cfg = ModelAccess.getConfiguration(dec);
                        if (null != cfg) {
                            changed = cfg.getDecision(dec);
                        }
                    }
                }
            }
        } catch (ModelQueryException e) {
            getLogger().exception(e);
        }

        
        if (null != changed) {
            IDecisionVariable nameSlot = null;
            for (int n = 0; null == nameSlot && n < changed.getNestedElementsCount(); n++) {
                IDecisionVariable nested = changed.getNestedElement(n);
                if (VariabilityModel.isNameSlot(nested)) {
                    nameSlot = nested;
                }
            }
            if (null != nameSlot) {
                Value val = nameSlot.getValue();
                if (val instanceof StringValue) {
                    name = ((StringValue) val).getValue();
                }
            }
        }
        
        // notify about the change - do not send name slot as otherwise comparison in holds fails
        if (null != changed) {
            ChangeManager.INSTANCE.variableChanged(null, changed);
        }
        return name;
    }

    /**
     * Writes the IVML string to a file.
     * 
     * @param ivmlString
     *            the IVML string to be written
     * @param filePath
     *            the file path to create a IVML file
     * @return a file
     */
    public static File writeIVMLStringToFile(String ivmlString, String filePath) {

        File file = new File(filePath);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            Files.write(Paths.get(file.getPath()), ivmlString.getBytes());
        } catch (IOException e) {
            getLogger().exception(e);
        }
        return file;

    }
    /**
     * Handles the disconnected elements in the pipeline.
     * @param newProject the project to write
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static void handleDisconnectedElements(Project newProject,
        PipelineSaveContext context) throws PipelineTranslationException {
        
        flows.removeAll(processedflows); //remove all already-processed flows
        //handle the pipeline element linked to the unprocessed flows
        if (flows.size() > 0) {                
            for (Flow f : flows) {
                PipelineElement fSrc = f.getSource(); //get the source of the flow
                PipelineNode p = (PipelineNode) fSrc; //fSrc can be surely casted to a pipeline node.
                handlePipelineNodes(p, newProject, context);
            }
        }
        //hanlde disconnected pipeline nodes in the pipeline
        List<PipelineNode> pipUnprocessedNodes = obtainUnprocessedNodes(pipProcessedNodes);
        for (PipelineNode p : pipUnprocessedNodes) {
            handlePipelineNodes(p, newProject, context);
        }
    }
    /**
     * Obtains the unprocessed nodes by removing the processed nodes from the overall list of the pipeline nodes.
     * @param processedNodes the processed nodes
     * @return the unprocessed nodes
     */
    public static List<PipelineNode> obtainUnprocessedNodes(List<PipelineNode> processedNodes) {
        List<PipelineNode> result = new ArrayList<PipelineNode>();
        for (PipelineNode p : pipNodes) { //transform pipeline nodes to the actual type
            if (p instanceof Source) {
                Source src = (Source) p;
                result.add(src);
            }
            if (p instanceof FamilyElement) {
                FamilyElement fm = (FamilyElement) p;
                result.add(fm);
            }
            if (p instanceof DataManagementElement) {
                DataManagementElement dm = (DataManagementElement) p;
                result.add(dm);
            }
            if (p instanceof Sink) {
                Sink snk = (Sink) p;
                result.add(snk);
            }
        }
        result.removeAll(processedNodes);
        return result;
    }
    /**
     * Handle the pipeline node translating to IVML notation.
     * @param pipelineNode the pipeline node to be handled
     * @param destProject the project to be written
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static void handlePipelineNodes(PipelineNode pipelineNode, Project destProject, PipelineSaveContext context) 
        throws PipelineTranslationException {
        if ((pipelineNode instanceof Source) && !(pipProcessedNodes.contains((Source) pipelineNode))) {
            Source source = (Source) pipelineNode;
            addPipelineElement(source, destProject, context);
        }
        if ((pipelineNode instanceof FamilyElement) && !(pipProcessedNodes.contains((FamilyElement) pipelineNode))) {
            FamilyElement fm = (FamilyElement) pipelineNode;
            addPipelineElement(fm, destProject, context);
        }
        if ((pipelineNode instanceof DataManagementElement) 
                && !(pipProcessedNodes.contains((DataManagementElement) pipelineNode))) {
            DataManagementElement dm = (DataManagementElement) pipelineNode;
            addPipelineElement(dm, destProject, context);
        }
        if ((pipelineNode instanceof Sink) && !(pipProcessedNodes.contains((Sink) pipelineNode))) {
            Sink snk = (Sink) pipelineNode;
            addPipelineElement(snk, destProject, context);
        }
    }
    /**
     * Adds all necessary imports in the project.
     * 
     * @param project
     *            the ivml project to add
     */
    public static void addImports(Project project) {
        project.addImport(basicsImport); // for attributes
        project.addImport(pipelinesImport);
        project.addImport(familiesImport);
        project.addImport(datamanagementImport);
    }   
    
    /**
     * Turns an EList into a list in order to avoid accidental model write operations.
     * 
     * @param <T> the element type
     * @param list the list
     * @return the result list
     */
    private static <T> List<T> toList(EList<T> list) {
        List<T> result = new ArrayList<T>();
        for (int e = 0; e < list.size(); e++) {
            result.add(list.get(e));
        }
        return result;
    }
    
    /**
     * Adds <code>pipeline</code> to the project.
     * 
     * @param pipeline The pipeline to be added
     * @param destProject The project to add pipeline element to
     * @param context Context, which stores information about already translated elements of the translation of a
     * complete pipeline from ECORE to IVML.
     * @return the variable name of the pipeline
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static DecisionVariableDeclaration addPipelineElement(Pipeline pipeline, Project destProject,
        PipelineSaveContext context) throws PipelineTranslationException {

        // define pipeline
        String typeName = pipeline.getIsSubPipeline() ? QmConstants.TYPE_SUBPIPELINE : QmConstants.TYPE_PIPELINE;
        DecisionVariableDeclaration pipelineVariable = IVMLModelOperations.getDecisionVariable(
            context.getPipelineProject(), typeName, null, destProject);
        freezables.add(pipelineVariable);
        destProject.add(pipelineVariable);
        //get all pipeline nodes and flows
        pipNodes = toList(pipeline.getNodes());
        flows = toList(pipeline.getFlows());
        // construct the compound variables
        Map<String, Object> pipelineCompound = new HashMap<String, Object>();
        if (pipeline.getName() != null) {
            pipelineCompound.put("name", pipeline.getName());
        }
        if (pipeline.getNumworkers() > 0) {
            pipelineCompound.put("numworkers", pipeline.getNumworkers().toString());
        }
        if (pipeline.getArtifact() != null) {
            pipelineCompound.put("artifact", pipeline.getArtifact());
        }
        
        //turn the constraint strings from UI to a list of Constraint values
        ArrayList<Value> cstValList = new ArrayList<Value>();
        if (!pipeline.getConstraints().isEmpty()) {
            String[] constraints = ConstraintUtils.splitConstraints(pipeline.getConstraints());                    
            for (int i = 0; i < constraints.length; i++) {                  
                cstValList.add(IVMLModelOperations.obtainConstraintValue(constraints[i], 
                        pipelineVariable));                
            }
        }
        // EASy Editor convention :|
        pipelineCompound.put("debug", pipeline.getDebug() == 0 ? Boolean.TRUE : Boolean.FALSE); 
        pipelineCompound.put("fastSerialization", pipeline.getFastSerialization() == 0 ? Boolean.TRUE : Boolean.FALSE); 
        
        // get source
        ArrayList<String> sourceList = new ArrayList<String>();
        EList<PipelineNode> pipelineNodes = pipeline.getNodes();
        for (PipelineNode pipelineNode : pipelineNodes) {
            if (pipelineNode instanceof Source) {
                Source source = (Source) pipelineNode;
                String sourceName = addPipelineElement(source, destProject, context);
                if (sourceName != null) {
                    sourceList.add(sourceName);                    
                }
            }
        }
        //add the sources
        if (sourceList != null) {
            pipelineCompound.put("sources", sourceList.toArray());
        }
        
        // Handle connectors of a sub pipeline, must be done after element are processed
        handleConnectorsOfSubPipeline(pipeline, destProject, context, pipelineCompound);
        
        // add the compound variables in the project
        Object[] pipelineObject = IVMLModelOperations.configureCompoundValues(pipelineVariable, pipelineCompound);
       
        //add the constraints into the value array
        ArrayList<Object> objList = new ArrayList<Object>(Arrays.asList(pipelineObject));
        objList.add("constraints");
        objList.add(cstValList.toArray());
           
        destProject.add(IVMLModelOperations.getConstraint(objList.toArray(), pipelineVariable, destProject));
        return pipelineVariable;
    }

    /**
     * Part of the {@link #addPipelineElement(Pipeline, Project, PipelineSaveContext)} method to add all
     * connectors to a sub pipeline.<br/>
     * <font color="red">Attention:</font> A sub pipeline do not need a source. Further, connectors (family elements)
     * may also serve as a starting point. This must be considered, otherwise some elements may be skipped.
     * @param pipeline The pipeline to be added
     * @param destProject The project to add pipeline element to
     * @param context Context, which stores information about already translated elements of the translation of a
     * complete pipeline from ECORE to IVML.
     * @param pipelineCompound The values to be saved, will be changed as a side effect.
     * @throws PipelineTranslationException In case of illegal pipeline structures
     */
    private static void handleConnectorsOfSubPipeline(Pipeline pipeline, Project destProject,
        PipelineSaveContext context, Map<String, Object> pipelineCompound)
        throws PipelineTranslationException {
        
        if (pipeline.getIsSubPipeline()) {
            EList<PipelineNode> pipelineNodes = pipeline.getNodes();
            // In a sub pipeline connectors (family elements) may also serve as starting point
            for (PipelineNode pipelineNode : pipelineNodes) {
                if (pipelineNode instanceof FamilyElement && ((FamilyElement) pipelineNode).getIsConnector()) {
                    FamilyElement connector = (FamilyElement) pipelineNode;
                    if (!context.hasFamilyMapping(connector)) {
                        addPipelineElement(connector, destProject, context);
                    }
                }
            }
            
            pipelineCompound.put("connectors", context.getConnectorNames().toArray());
            
            // Family to which this sub pipeline belongs to
            String family = pipeline.getSubPipelineFamily();
            if (null != family) {
                pipelineCompound.put("subPipelineFamily", family);
            }
        }
    }

    /**
     * Generic part of the <tt>addPipelineElement</tt> methods to process the generic part of
     * {@link PipelineNode}s.
     * @param node The node to process, e.g., {@link Source} or {@link Sink}. Must not be <tt>null</tt>.
     * @param nodeValue A map which is later used to create the compound value. Must not be <tt>null</tt>.
     */
    private static void processPipelineNodeValue(PipelineNode node, Map<String, Object> nodeValue) {
        if (node.getName() != null) {
            nodeValue.put("name", node.getName());
        }
        if (node.getParallelism() > 0) {
            nodeValue.put(QmConstants.SLOT_PIPELINE_NODE_PARALLELISM, node.getParallelism());
        }
        if (node.getNumtasks() > 0) {
            nodeValue.put(QmConstants.SLOT_PIPELINE_NODE_NUMBER_OF_TAKS, node.getNumtasks());
        }
    }
    
    /**
     * Adds <code>Source</code> to the project.
     * 
     * @param source
     *            the source to be added
     * @param destProject
     *            the project to add to
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @return the variable name of the source
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static String addPipelineElement(Source source, Project destProject, PipelineSaveContext context)
        throws PipelineTranslationException {
        
        DecisionVariableDeclaration decisionVariable = null;
        // define source
        decisionVariable = IVMLModelOperations.getDecisionVariable(context.getPipelineProject(), "Source", 
                Integer.toString(sourceCount), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);
        sourceCount++;
        
        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable sourceVariable = ModelAccess.getFromGlobalIndex(
                datamanagementModelPart, nameAndTypeMap.get("source"),
                source.getSource());
        // construct the compound value from the pipeline editor graph
        Map<String, Object> sourceCompound = new HashMap<String, Object>();
        processPipelineNodeValue(source, sourceCompound);
        if (sourceVariable != null) {
            sourceCompound.put("source",
                IVMLModelOperations.getDeclaration(sourceVariable).getName());
        }
        List<Flow> srcOutput = getOutput(source);
        ArrayList<String> fList = new ArrayList<String>();
        for (Flow flow : srcOutput) {
            fList.add(addPipelineElement(flow, destProject, context));
        }
        if (!fList.isEmpty()) {
            sourceCompound.put("output", fList.toArray());
        }        

        addPipelineElementToProject(source, destProject, decisionVariable, sourceCompound);
        pipProcessedNodes.add(source);
        return decisionVariable.getName();
    }

    /**
     * Adds <code>Flow</code> to the project.
     * 
     * @param flow
     *            the flow to be added
     * @param destProject
     *            the project to add to
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @return the variable name of the flow
     * @throws PipelineTranslationException in case of illegal flows
     */
    private static String addPipelineElement(Flow flow, Project destProject, PipelineSaveContext context)
        throws PipelineTranslationException {

        // define flow
        DecisionVariableDeclaration flowVariable = IVMLModelOperations.getDecisionVariable(context.getPipelineProject(),
                "Flow", Integer.toString(flowCount), destProject);
        freezables.add(flowVariable);
        destProject.add(flowVariable);        
        flowCount++;

        // construct the compound variables
        Map<String, Object> flowCompound = new HashMap<String, Object>();
        Grouping[] grouping = Grouping.values();
        if (flow.getName() != null) {
            flowCompound.put("name", flow.getName());
        }
        if (0 <= flow.getGrouping()) {
            flowCompound.put("grouping", grouping[flow.getGrouping()].name());
        }
        String destination = null;
        
        if (flow.getDestination() instanceof Source) {
            throw new PipelineTranslationException("Illegal flow target (source).");
        } else if (flow.getSource() instanceof Sink) {
            throw new PipelineTranslationException("Illegal flow source (sink).");
        } else if (flow.getDestination() instanceof FamilyElement) {
            FamilyElement fe = (FamilyElement) flow.getDestination();
            if (context.hasFamilyMapping(fe)) {
                destination = context.getFamilyMapping(fe);
            } else {
                destination = addPipelineElement(fe, destProject, context);
            }
        } else if (flow.getDestination() instanceof DataManagementElement) {
            destination = addPipelineElement((DataManagementElement) flow.getDestination(), destProject, context);
        } else if (flow.getDestination() instanceof Source) {
            throw new PipelineTranslationException("Illegal flow target");
        } else {
            Sink sink = (Sink) flow.getDestination();
            if (context.hasSinkMapping(sink)) {
                destination = context.getSinkMapping(sink);
            } else {
                destination = addPipelineElement(sink, destProject, context);
            }
        }
        if (destination != null) {
            flowCompound.put("destination", destination);
        }
        
        String tupleTypeRef = flow.getTupleType();
        convertCSTBasedReference(flowCompound, tupleTypeRef, "tupleType");
        
        addPipelineElementToProject(flow, destProject, flowVariable, flowCompound);
        
        //removed the already-handled flow
        processedflows.add(flow);
        return flowVariable.getName();
    }

    /**
     * Converts a constraint saved as string into a reference and saves it.
     * @param compoundValue Is used to store the values for the different slots of the compound
     * @param tupleTypeRef The reference to be converted
     * @param slotName The (nested) slot of <tt>variable</tt> containing the reference to be saved
     */
    private static void convertCSTBasedReference(Map<String, Object> compoundValue, String tupleTypeRef,
        String slotName) {
        
        if (null != tupleTypeRef && !tupleTypeRef.isEmpty()) {
            try {
                ConstraintSyntaxTree cstValue = ModelUtility.INSTANCE.createExpression(tupleTypeRef,
                        ModelAccess.getModel(VariabilityModel.Definition.TOP_LEVEL));
                compoundValue.put(slotName, cstValue);
            } catch (CSTSemanticException e) {
                Activator.getLogger(PipelineTranslationOperations.class).exception(e);
            } catch (ConstraintSyntaxException e) {
                Activator.getLogger(PipelineTranslationOperations.class).exception(e);
            }
        }
    }

    /**
     * Adds <code>FamilyElement</code> to the project.
     * 
     * @param familyElement
     *            the family element to be added
     * @param destProject
     *            the project to add to
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @return the variable name of the family element
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static String addPipelineElement(FamilyElement familyElement, Project destProject,
        PipelineSaveContext context) throws PipelineTranslationException {
        
        DecisionVariableDeclaration decisionVariable = null;
        // define familyelement
        decisionVariable = IVMLModelOperations.getDecisionVariable(
                context.getPipelineProject(), "FamilyElement",
                Integer.toString(context.getFamilyCount()), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);
        
        if (decisionVariable != null) { //record the family element and its name in the context map
            context.addFamilyMapping(familyElement, decisionVariable.getName());
        }
        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable familyVariable = ModelAccess.getFromGlobalIndex(
                familyModelPart, nameAndTypeMap.get("family"),
                familyElement.getFamily());

        // construct the compound value from the pipeline editor graph
        Map<String, Object> familyElementCompound = new HashMap<String, Object>();
        processPipelineNodeValue(familyElement, familyElementCompound);
        if (familyVariable != null) {
            familyElementCompound.put("family",
                IVMLModelOperations.getDeclaration(familyVariable).getName());
        }
        List<Flow> fOutput = getOutput(familyElement);
        List<String> fList = new ArrayList<String>();
        for (Flow flow : fOutput) {
            fList.add(addPipelineElement(flow, destProject, context));
        }
        if (fList != null) {
            familyElementCompound.put("output", fList.toArray());
        }
        
        String algorithmRef = familyElement.getDefault();
        convertCSTBasedReference(familyElementCompound, algorithmRef, "default");
        
        addPipelineElementToProject(familyElement, destProject, decisionVariable, familyElementCompound);
        context.addFamilyMapping(familyElement, decisionVariable.getName());
        pipProcessedNodes.add(familyElement);
        
        // Handle connectors of a sub pipeline
        if (familyElement.getIsConnector()) {
            context.addConnector(decisionVariable.getName());
        }

        return decisionVariable.getName();
    }

    /**
     * Adds <code>DataManagementElement</code> to the project.
     * 
     * @param dataManagementElement
     *            the dataManagementElement to be added
     * @param destProject
     *            the project to add to
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @return the variable name of the dataManagementElement
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static String addPipelineElement(DataManagementElement dataManagementElement, Project destProject,
        PipelineSaveContext context) throws PipelineTranslationException {
        
        DecisionVariableDeclaration decisionVariable = null;
        // define dataManagementElement
        decisionVariable = IVMLModelOperations.getDecisionVariable(context.getPipelineProject(), 
                "DataManagementElement", Integer.toString(dataManagementElementCount), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);
        dataManagementElementCount++;

        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable dataManagementElementVariable = ModelAccess.getFromGlobalIndex(
                datamanagementModelPart, nameAndTypeMap.get("dataManagement"),
                dataManagementElement.getDataManagement());

        // construct the compound value from the pipeline editor graph
        Map<String, Object> dataManagementElementCompound = new HashMap<String, Object>();
        processPipelineNodeValue(dataManagementElement, dataManagementElementCompound);
        if (dataManagementElementVariable != null) {
            dataManagementElementCompound.put("dataManagement",
                IVMLModelOperations.getDeclaration(dataManagementElementVariable).getName());
        }
        List<Flow> fOutput = getOutput(dataManagementElement);
        List<String> fList = new ArrayList<String>();
        for (Flow flow : fOutput) {
            fList.add(addPipelineElement(flow, destProject, context));
        }
        if (fList != null) {
            dataManagementElementCompound.put("output", fList.toArray());
        }        

        addPipelineElementToProject(dataManagementElement, destProject, decisionVariable,
            dataManagementElementCompound);
        pipProcessedNodes.add(dataManagementElement);
        return decisionVariable.getName();
    }    
    
    /**
     * Adds <code>Sink</code> or <tt>ReplaySink</tt> to the project.
     * 
     * @param sink The sink or ReplaySink to be added
     * @param destProject
     *            the project to add to
     * @param context Context, which stores information about already translated elements of the translation of a
     *     complete pipeline from ECORE to IVML.
     * @return the variable name of the sink
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    private static String addPipelineElement(Sink sink, Project destProject, PipelineSaveContext context)
        throws PipelineTranslationException {
        
        DecisionVariableDeclaration decisionVariable = null;
        // define sink
        String typeName = (sink instanceof ReplaySink) ? "ReplaySink" : "Sink";
        decisionVariable = IVMLModelOperations.getDecisionVariable(context.getPipelineProject(), typeName, 
                Integer.toString(context.getSinkCount()), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);

        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable sinkVariable = ModelAccess.getFromGlobalIndex(
                datamanagementModelPart, nameAndTypeMap.get("sink"),
                sink.getSink());

        // construct the compound value from the pipeline editor graph
        Map<String, Object> sinkCompound = new HashMap<String, Object>();
        processPipelineNodeValue(sink, sinkCompound);
        //sinkCompound.put("input", sink.getInput().toArray()); // to be solved with actual variable name
        if (sinkVariable != null) {
            sinkCompound.put("sink",
                IVMLModelOperations.getDeclaration(sinkVariable).getName());
        }
        addPipelineElementToProject(sink, destProject, decisionVariable, sinkCompound);
        context.addSinkMapping(sink, decisionVariable.getName());
        pipProcessedNodes.add(sink);
        List<Flow> srcOutput = getOutput(sink);
        if (!srcOutput.isEmpty()) {
            throw new PipelineTranslationException("Illegal outgoing flows from sink '" + sink.getName() + "'");
        }
        return decisionVariable.getName();
    }

    /**
     * Generic part of the addPipelineElement methods to create a compound value, add constraints, and add this value
     * to the given project.
     * @param pipElement The node to process, e.g., {@link Source} or {@link Sink}. Must not be <tt>null</tt>.
     * @param destProject The project, where the compound value shall be saved to. Must not be <tt>null</tt>.
     * @param declaration The declaration of the compound variable.
     * @param compoundValueMapping The mapping of configured values (slot name, slot value), except the constraints.
     */
    private static void addPipelineElementToProject(PipelineElement pipElement, Project destProject,
        DecisionVariableDeclaration declaration, Map<String, Object> compoundValueMapping) {

        Object[] compoundValue = IVMLModelOperations.configureCompoundValues(declaration, compoundValueMapping);
        //add the configuration of constraints
        compoundValue = addConstraintToValues(pipElement, compoundValue, declaration);
        destProject.add(IVMLModelOperations.getConstraint(compoundValue, declaration, destProject));
    }
    
    /**
     * Adds the constraints to the array of Value.
     * @param pipelineElm
     *          the pipeline element to get its constraint string
     * @param preObjectList
     *          the object array consists of all configured values except for constraints
     * @param parent
     *          the parent element to add constraints
     * @return a list of Value with complete information of the configuration of the element
     */
    public static Object[] addConstraintToValues(PipelineElement pipelineElm, Object[] preObjectList, 
        DecisionVariableDeclaration parent) {
        
        //get constraintString
        String constraintString = pipelineElm.getConstraints();
        //turn the constraint strings from UI to a list of Constraint values        
        ArrayList<Value> cstValList = new ArrayList<Value>();
        if (!constraintString.isEmpty()) {
            String[] constraints = ConstraintUtils.splitConstraints(constraintString);                    
            for (int i = 0; i < constraints.length; i++) {                  
                cstValList.add(IVMLModelOperations.obtainConstraintValue(constraints[i], 
                        parent));                
            }
        }
        //add the constraints into the preObjectList
        Object[] postObjectList = new Object[preObjectList.length + 2];
        System.arraycopy(preObjectList, 0, postObjectList, 0, preObjectList.length);
        postObjectList[preObjectList.length] = "constraints";
        postObjectList[preObjectList.length] = cstValList.toArray();
        
//        ArrayList<Object> objList = new ArrayList<Object>(Arrays.asList(preObjectList));
//        objList.add("constraints");
//        objList.add(cstValList.toArray());
//        return objList.toArray();

        return postObjectList;
    }

    /**
     * Gets the pipeline variable name from the <code>nameAndDisplaynameMap</code>.
     * 
     * @param nameAndDisplaynameMap
     *            the map to search for
     * @param displayName
     *            the name as the value to search the pipeline variable name in the map
     * @return the pipeline variable name
     */
    public static String getPipelineVariableName(
            Map<String, String> nameAndDisplaynameMap, String displayName) {
        String result = null;
        if (nameAndDisplaynameMap.containsValue(displayName)) {
            Set<String> keySet = nameAndDisplaynameMap.keySet();
            for (String key : keySet) {
                if (displayName.equals(nameAndDisplaynameMap.get(key))) {
                    result = key;
                }
            }
        }
        return result;
    }

    /**
     * Returns the flow list from the node.
     * @param node the pipeline node
     * @return the flow list
     */
    public static List<Flow> getOutput(PipelineNode node) {
        EList<Flow> flows = pipeline.getFlows();
        ArrayList<Flow> output = new ArrayList<Flow>();
        for (Flow flow : flows) {
            if (node.equals(flow.getSource())) {
                output.add(flow);
            }
        }
        return output;
    }
    
    /**
     * Returns the logger for this class.
     * 
     * @return the logger
     */
    private static EASyLogger getLogger() {
        return EASyLoggerFactory.INSTANCE.getLogger(PipelineTranslationOperations.class, Activator.PLUGIN_ID);
    }

}
