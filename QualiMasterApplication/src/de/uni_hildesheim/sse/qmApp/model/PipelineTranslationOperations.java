package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
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

import pipeline.DataManagementElement;
import pipeline.FamilyElement;
import pipeline.Flow;
import pipeline.Pipeline;
import pipeline.PipelineElement;
import pipeline.PipelineNode;
import pipeline.Sink;
import pipeline.Source;
import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.IFreezable;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.TypeQueries;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.persistency.StringProvider;
import de.uni_hildesheim.sse.qmApp.editors.QMPipelineEditor;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Definition;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * The operations of translating the Ecore pipeline diagram to IVML project.
 * 
 * @author Cui Qin
 */
public class PipelineTranslationOperations {

    public static final String EXTENSION = ".ivml";
    
    // static imports
    private static ProjectImport basicsImport = new ProjectImport(
            "Basics", null);
    private static ProjectImport pipelinesImport = new ProjectImport(
            "Pipelines", null);
    private static ProjectImport familiesImport = new ProjectImport(
            "FamiliesCfg", null);
    private static ProjectImport datamanagementImport = new ProjectImport(
            "DataManagementCfg", null);
    
    // static model parts
    private static IModelPart familyModelPart = VariabilityModel.Configuration.FAMILIES;
    private static IModelPart datamanagementModelPart = VariabilityModel.Configuration.DATA_MANAGEMENT;
    private static IModelPart pipelineModelPart = VariabilityModel.Configuration.PIPELINES;
    
    private static Project modelProject = pipelineModelPart.getConfiguration()
            .getProject();
    private static List<IFreezable> freezables;
    private static Map<FamilyElement, String> familyNodesAndName = new HashMap<FamilyElement, String>();
    private static List<PipelineNode> pipNodes;
    private static List<PipelineNode> pipProcessedNodes;
    private static List<Flow> flows;
    private static List<Flow> processedflows;
    private static Map<Sink, String> sinkNodesAndName = new HashMap<Sink, String>();
    private static int sourceCount;
    private static int flowCount;
    private static int familyelementCount;
    private static int dataManagementElementCount;
    private static int sinkCount;
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
        sourceCount = 0;
        flowCount = 0;
        familyelementCount = 0;
        dataManagementElementCount = 0;
        sinkCount = 0;
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
            pipelineVariable = addPipelineElement(pipeline, newProject);
            handleDisconnectedElements(newProject); //handle the disconnected elements in the pipeline
            String pVariableName = pipelineVariable.getName();
            String pDisplayName = pipeline.getName();
            Map<String, String> nameMap = QMPipelineEditor
                            .getPipelineNameAndDisplayname();
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
        ProjectImport pImport = new ProjectImport(newProject.getName(), null);
        // adds the imports in the main pipeline project
        IVMLModelOperations.modifyImports(modelProject,
                IVMLModelOperations.ADD, pImport);
        // adds the pipeline to the main project
        IVMLModelOperations.addPipelineToMainProject(pipelineVariable);

        String output = StringProvider.toIvmlString(newProject);
        // writes the new IVML project to a file
        String filePath = PipelineDiagramUtils.getFolderDirectory(fileURI)
                + newProject.getName() + EXTENSION;
        File file = new File(filePath);
        boolean exists = file.exists();
        writeIVMLStringToFile(output, filePath);
        Project prj = ModelAccess.reloadModel(newProject, file.toURI());
        // print to IVML
        if (!exists) {
            try {
                VarModel.INSTANCE.locations().updateModelInformation();
            } catch (ModelManagementException e) {
                getLogger().info(e.getMessage());
            }
        }
        ModelAccess.store(pipelineModelPart.getConfiguration());        
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
                        de.uni_hildesheim.sse.model.confModel.Configuration cfg = ModelAccess.getConfiguration(dec);
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
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static void handleDisconnectedElements(Project newProject) throws PipelineTranslationException {
        flows.removeAll(processedflows); //remove all already-processed flows
        //handle the pipeline element linked to the unprocessed flows
        if (flows.size() > 0) {                
            for (Flow f : flows) {
                PipelineElement fSrc = f.getSource(); //get the source of the flow
                PipelineNode p = (PipelineNode) fSrc; //fSrc can be surely casted to a pipeline node.
                handlePipelineNodes(p, newProject);
            }
        }
        //hanlde disconnected pipeline nodes in the pipeline
        List<PipelineNode> pipUnprocessedNodes = obtainUnprocessedNodes(pipProcessedNodes);
        for (PipelineNode p : pipUnprocessedNodes) {
            handlePipelineNodes(p, newProject);
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
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static void handlePipelineNodes(PipelineNode pipelineNode, Project destProject) 
        throws PipelineTranslationException {
        if ((pipelineNode instanceof Source) && !(pipProcessedNodes.contains((Source) pipelineNode))) {
            Source source = (Source) pipelineNode;
            addPipelineElement(source, destProject);
        }
        if ((pipelineNode instanceof FamilyElement) && !(pipProcessedNodes.contains((FamilyElement) pipelineNode))) {
            FamilyElement fm = (FamilyElement) pipelineNode;
            addPipelineElement(fm, destProject);
        }
        if ((pipelineNode instanceof DataManagementElement) 
                && !(pipProcessedNodes.contains((DataManagementElement) pipelineNode))) {
            DataManagementElement dm = (DataManagementElement) pipelineNode;
            addPipelineElement(dm, destProject);
        }
        if ((pipelineNode instanceof Sink) && !(pipProcessedNodes.contains((Sink) pipelineNode))) {
            Sink snk = (Sink) pipelineNode;
            addPipelineElement(snk, destProject);
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
     * @param <T> thel element type
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
     * @param pipeline
     *            the pipeline to be added
     * @param destProject
     *            the project to add pipeline element to
     * @return the variable name of the pipeline
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static DecisionVariableDeclaration addPipelineElement(
            Pipeline pipeline, Project destProject) throws PipelineTranslationException {

        // define pipeline
        DecisionVariableDeclaration pipelineVariable = IVMLModelOperations
                .getDecisionVariable(modelProject, "Pipeline", null,
                        destProject);
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
        Source source = null;
        ArrayList<String> sourceList = new ArrayList<String>();
        EList<PipelineNode> pipelineNodes = pipeline.getNodes();
        for (PipelineNode pipelineNode : pipelineNodes) {
            if (pipelineNode instanceof Source) {
                source = (Source) pipelineNode;
                String sourceName = addPipelineElement(source, destProject);
                if (sourceName != null) {
                    sourceList.add(sourceName);                    
                }
            }
        }
        //add the sources
        if (sourceList != null) {
            pipelineCompound.put("sources", sourceList.toArray());
        }
        
        // add the compound variables in the project
        Object[] pipelineObject = IVMLModelOperations.configureCompoundValues(
                pipelineVariable, pipelineCompound);
       
        //add the constraints into the value array
        ArrayList<Object> objList = new ArrayList<Object>(Arrays.asList(pipelineObject));
        objList.add("constraints");
        objList.add(cstValList.toArray());
           
        destProject.add(IVMLModelOperations.getConstraint(objList.toArray(),
                pipelineVariable, destProject));
        return pipelineVariable;
    }

    /**
     * Adds <code>Source</code> to the project.
     * 
     * @param source
     *            the source to be added
     * @param destProject
     *            the project to add to
     * @return the variable name of the source
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String addPipelineElement(Source source, Project destProject) throws PipelineTranslationException {
        DecisionVariableDeclaration decisionVariable = null;
        // define source
        decisionVariable = IVMLModelOperations.getDecisionVariable(
                modelProject, "Source", Integer.toString(sourceCount),
                destProject);
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
        if (source.getName() != null) {
            sourceCompound.put("name", source.getName());
        }
        if (source.getParallelism() > 0) {
            sourceCompound.put("parallelism", source.getParallelism());
        }        
        if (sourceVariable != null) {
            sourceCompound.put("source",
                IVMLModelOperations.getDeclaration(sourceVariable).getName());
        }
        List<Flow> srcOutput = getOutput(source);
        ArrayList<String> fList = new ArrayList<String>();
        for (Flow flow : srcOutput) {
            fList.add(addPipelineElement(flow, destProject));
        }
        if (!fList.isEmpty()) {
            sourceCompound.put("output", fList.toArray());
        }        
        Object[] sourceObject = IVMLModelOperations.configureCompoundValues(
                decisionVariable, sourceCompound);
        //add the configuration of constraints
        sourceObject = addConstraintToValues(source, sourceObject, decisionVariable);
        destProject.add(IVMLModelOperations.getConstraint(sourceObject,
                decisionVariable, destProject)); 
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
     * @return the variable name of the flow
     * @throws PipelineTranslationException in case of illegal flows
     */
    public static String addPipelineElement(Flow flow, Project destProject) throws PipelineTranslationException {

        // define flow
        DecisionVariableDeclaration flowVariable = IVMLModelOperations
                .getDecisionVariable(modelProject, "Flow",
                        Integer.toString(flowCount), destProject);
        freezables.add(flowVariable);
        destProject.add(flowVariable);        
        flowCount++;

        /*
         * try { IDatatype dataType = ModelQuery.findType(modelProject, "Grouping", null); System.out.println(dataType);
         * } catch (ModelQueryException e) { // TODO Auto-generated catch block e.printStackTrace(); }
         */
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
            if (!(familyNodesAndName.containsKey(fe))) {
                destination = addPipelineElement(
                        fe, destProject);
            } else {
                destination = familyNodesAndName.get(fe);
            }
            
        } else if (flow.getDestination() instanceof DataManagementElement) {
            destination = addPipelineElement((DataManagementElement) flow.getDestination(),
                    destProject);
        } else if (flow.getDestination() instanceof Source) {
            throw new PipelineTranslationException("Illegal flow target");
        } else {
            Sink sink = (Sink) flow.getDestination();
            if (!(sinkNodesAndName.containsKey(sink))) {
                destination = addPipelineElement(sink, destProject);
            } else {
                destination = sinkNodesAndName.get(sink);
            }
        }
        if (destination != null) {
            flowCompound.put("destination", destination);
        }
        Object[] flowObject = IVMLModelOperations.configureCompoundValues(
                flowVariable, flowCompound);
        //add the configuration of constraints
        flowObject = addConstraintToValues(flow, flowObject, flowVariable);
        destProject.add(IVMLModelOperations.getConstraint(flowObject,
                flowVariable, destProject));
        //removed the already-handled flow
        processedflows.add(flow);
        return flowVariable.getName();
    }

    /**
     * Adds <code>FamilyElement</code> to the project.
     * 
     * @param familyElement
     *            the family element to be added
     * @param destProject
     *            the project to add to
     * @return the variable name of the family element
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String addPipelineElement(FamilyElement familyElement,
            Project destProject) throws PipelineTranslationException {
        DecisionVariableDeclaration decisionVariable = null;
        // define familyelement
        decisionVariable = IVMLModelOperations.getDecisionVariable(
                modelProject, "FamilyElement",
                Integer.toString(familyelementCount), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);
        familyelementCount++;

        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable familyVariable = ModelAccess.getFromGlobalIndex(
                familyModelPart, nameAndTypeMap.get("family"),
                familyElement.getFamily());

        // construct the compound value from the pipeline editor graph
        Map<String, Object> familyElementCompound = new HashMap<String, Object>();
        if (familyElement.getName() != null) {
            familyElementCompound.put("name", familyElement.getName());
        }
        if (familyElement.getParallelism() > 0) {
            familyElementCompound.put("parallelism", familyElement.getParallelism());
        }        
        if (familyVariable != null) {
            familyElementCompound.put("family",
                IVMLModelOperations.getDeclaration(familyVariable).getName());
        }
        List<Flow> fOutput = getOutput(familyElement);
        List<String> fList = new ArrayList<String>();
        for (Flow flow : fOutput) {
            fList.add(addPipelineElement(flow, destProject));
        }
        if (fList != null) {
            familyElementCompound.put("output", fList.toArray());
        }
        //familyElementCompound.put("input", familyElement.getInput().toArray()); // to be solved with actual variable
                                                                                // name
        Object[] familyElementObject = IVMLModelOperations
                .configureCompoundValues(decisionVariable,
                        familyElementCompound);
        //add the configuration of constraints
        familyElementObject = addConstraintToValues(familyElement, familyElementObject, decisionVariable);
        destProject.add(IVMLModelOperations.getConstraint(familyElementObject,
                decisionVariable, destProject));
        familyNodesAndName.put(familyElement, decisionVariable.getName());
        pipProcessedNodes.add(familyElement);
        return decisionVariable.getName();
    }

    /**
     * Adds <code>DataManagementElement</code> to the project.
     * 
     * @param dataManagementElement
     *            the dataManagementElement to be added
     * @param destProject
     *            the project to add to
     * @return the variable name of the dataManagementElement
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String addPipelineElement(DataManagementElement dataManagementElement, Project destProject) 
        throws PipelineTranslationException {
        DecisionVariableDeclaration decisionVariable = null;
        // define dataManagementElement
        decisionVariable = IVMLModelOperations.getDecisionVariable(
                modelProject, "DataManagementElement", Integer.toString(dataManagementElementCount), destProject);
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
        if (dataManagementElement.getName() != null) {
            dataManagementElementCompound.put("name", dataManagementElement.getName());
        }
        if (dataManagementElement.getParallelism() > 0) {
            dataManagementElementCompound.put("parallelism", dataManagementElement.getParallelism());
        } 
        if (dataManagementElementVariable != null) {
            dataManagementElementCompound.put("dataManagement",
                IVMLModelOperations.getDeclaration(dataManagementElementVariable).getName());
        }
        List<Flow> fOutput = getOutput(dataManagementElement);
        List<String> fList = new ArrayList<String>();
        for (Flow flow : fOutput) {
            fList.add(addPipelineElement(flow, destProject));
        }
        if (fList != null) {
            dataManagementElementCompound.put("output", fList.toArray());
        }        

        Object[] dataManagementElementObject = IVMLModelOperations.configureCompoundValues(
                decisionVariable, dataManagementElementCompound);
        //add the configuration of constraints
        dataManagementElementObject = addConstraintToValues(dataManagementElement, dataManagementElementObject, 
                decisionVariable);
        destProject.add(IVMLModelOperations.getConstraint(dataManagementElementObject,
                decisionVariable, destProject));
        pipProcessedNodes.add(dataManagementElement);
        return decisionVariable.getName();
    }    
    
    /**
     * Adds <code>Sink</code> to the project.
     * 
     * @param sink
     *            the sink to be added
     * @param destProject
     *            the project to add to
     * @return the variable name of the sink
     * @throws PipelineTranslationException in case of illegal pipeline structures
     */
    public static String addPipelineElement(Sink sink, Project destProject) throws PipelineTranslationException {
        DecisionVariableDeclaration decisionVariable = null;
        // define sink
        decisionVariable = IVMLModelOperations.getDecisionVariable(
                modelProject, "Sink", Integer.toString(sinkCount), destProject);
        freezables.add(decisionVariable);
        destProject.add(decisionVariable);
        sinkCount++;

        Map<String, IDatatype> nameAndTypeMap = IVMLModelOperations
                .getCompoundNameAndType(decisionVariable);
        IDecisionVariable sinkVariable = ModelAccess.getFromGlobalIndex(
                datamanagementModelPart, nameAndTypeMap.get("sink"),
                sink.getSink());

        // construct the compound value from the pipeline editor graph
        Map<String, Object> sinkCompound = new HashMap<String, Object>();
        if (sink.getName() != null) {
            sinkCompound.put("name", sink.getName());
        }
        if (sink.getParallelism() > 0) {
            sinkCompound.put("parallelism", sink.getParallelism());
        }        
        //sinkCompound.put("input", sink.getInput().toArray()); // to be solved with actual variable name
        if (sinkVariable != null) {
            sinkCompound.put("sink",
                IVMLModelOperations.getDeclaration(sinkVariable).getName());
        }
        Object[] sinkObject = IVMLModelOperations.configureCompoundValues(
                decisionVariable, sinkCompound);
        //add the configuration of constraints
        sinkObject = addConstraintToValues(sink, sinkObject, decisionVariable);
        destProject.add(IVMLModelOperations.getConstraint(sinkObject,
                decisionVariable, destProject));
        sinkNodesAndName.put(sink, decisionVariable.getName());
        pipProcessedNodes.add(sink);
        List<Flow> srcOutput = getOutput(sink);
        if (!srcOutput.isEmpty()) {
            throw new PipelineTranslationException("Illegal outgoing flows from sink '" + sink.getName() + "'");
        }
        return decisionVariable.getName();
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
        ArrayList<Object> objList = new ArrayList<Object>(Arrays.asList(preObjectList));
        objList.add("constraints");
        objList.add(cstValList.toArray());
        
        return objList.toArray();
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
