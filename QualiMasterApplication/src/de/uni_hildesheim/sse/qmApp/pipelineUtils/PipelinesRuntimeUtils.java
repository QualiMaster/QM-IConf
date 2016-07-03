package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.dslCore.TranslationResult;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * Utils class for the RuntimeView. This class holds some useful methods for crawling the 
 * pipelines-ivml-configuration.
 * 
 * @author Niko Nowatzki
 *
 */
public class PipelinesRuntimeUtils {

    public static final PipelinesRuntimeUtils INSTANCE = new PipelinesRuntimeUtils();
    
    
    private static final String EASY_STRING = "EASy";
    private static final String IVML_STRING = ".ivml";
    private static final String META_STRING = "meta";
    private static final String PIPELINE_IVML_FILE = "Pipelines.ivml";
    private static final String NAME_STRING = "name";
    
    private RuntimeEditorContentProvider contentProvider = new RuntimeEditorContentProvider();
    private RuntimeEditorLabelProvider labelProvider = new RuntimeEditorLabelProvider();
    
    private HashMap<String, CustomObservableList> observalesPipMapping
            = new HashMap<String, CustomObservableList>();

    private ArrayList<String> pipelines = new ArrayList<String>();
    private Map<String, ContainableModelElement> topLevelPipelineElements
            = new HashMap<String, ContainableModelElement>();
    private List<PipelineGraphColoringWrapper> pipelinesToDisplayInTable
            = new ArrayList<PipelineGraphColoringWrapper>();
    private  ArrayList<String> backupObservableItem = new ArrayList<String>();
   
    private CustomObservableList observablePipelines = new CustomObservableList();
    private CustomObservableList observablePipelineNodes = new CustomObservableList();
    private CustomObservableList observableFamily = new CustomObservableList();
    private CustomObservableList observableDatamanagement = new CustomObservableList();
    private CustomObservableList observableSink = new CustomObservableList();
    private CustomObservableList observableSource = new CustomObservableList();
    
    
    /**
     * // Exists only to avoid instantiation.
     */
    private PipelinesRuntimeUtils() {
    }
    
    /**
     * Get the contentprovider.
     * @return contentProvider the contentprovider.
     */
    public RuntimeEditorContentProvider getContentProvider() {
        return contentProvider;
    }
    /**
     * Get the Labelprovider.
     * @return labelprovidfer labelprovider.
     */
    public RuntimeEditorLabelProvider getLabelProvider() {
        return labelProvider;
    }
    /**
     * A ArrayList with useful contains-method.
     * 
     * @author Niko Nowatzki
     */
    public class CustomObservableList extends ArrayList<String> {

        private static final long serialVersionUID = -543700414925717790L;

        @Override
        public boolean contains(Object obj) {
            boolean toReturn = false;
            String paramStr = (String) obj;
            for (String s : this) {
                if (paramStr.toLowerCase().equalsIgnoreCase(s)) {
                    toReturn = true;
                }
            }
            return toReturn;
        }
    }
    
    /**
     * Wrapper which wraps up info about a pipelines element, its type, variable,
     * declaration, color and observable which should be drawn.
     * 
     * @author Niko Nowatzki
     */
    public static class PipelineGraphColoringWrapper implements Serializable {
        
        private static final long serialVersionUID = 998164785967639136L;
        private String elemName;
        private PipelineNodeType type;
        private IDecisionVariable var;
        private Color color;
        private String obs;
        private DecisionVariableDeclaration decl;
        private List<PipelineGraphColoringWrapper> descendants = new ArrayList<PipelineGraphColoringWrapper>();
        private String pipelineParent;

        /**
         * Create a new Wrapper which wraps up info about watned coloring.
         * @param elemName the elements name.
         * @param type the elements type.
         * @param pipelineParent the elements parent. In the tree this is the Pipeline.
         * @param var the elements variable.
         * @param decl the elements declaration.
         */
        public PipelineGraphColoringWrapper(String elemName, PipelineNodeType type,
                String pipelineParent, IDecisionVariable var, DecisionVariableDeclaration decl) {
            this.elemName = elemName;
            this.type = type;
            this.pipelineParent = pipelineParent;
            this.var = var;
            this.decl = decl;
        }
        /**
         * Create a new Wrapper which wraps up info about watned coloring.
         * @param elemName the elements name.
         * @param type the elements type.
         * @param pipelineParent the elements parent. In the tree this is the Pipeline.
         * @param var the elements variable.
         */
        public PipelineGraphColoringWrapper(String elemName, PipelineNodeType type,
                String pipelineParent, IDecisionVariable var) {
            this.elemName = elemName;
            this.type = type;
            this.pipelineParent = pipelineParent;
            this.var = var;
        }

        /**
         * Set the color of a pipeline wrapper.
         * @param color Color to set for the wrapper which stands for a pipelines.
         */
        public void setColor(Color color) {
            this.color = color;
        }
        /**
         * Set the observable of a pipeline wrapper.
         * @param obs Observable to set for the wrapper which stands for a pipelines. 
         */
        public void setObservable(String obs) {
            this.setObs(obs);
        }
        /**
         * Set the observables for this wrapper.
         * @param obs observable zo set.
         */
        private void setObs(String obs) {
            this.obs = obs;
        }
        /**
         * Set the variable of a pipeline wrapper.
         * @param desVar Variable to set for the wrapper which stands for a pipelines. 
         */
        public void setVar(IDecisionVariable desVar) {
            this.var = desVar;
        }
        /**
         * Set the declaration of a pipeline wrapper.
         * @param decl Declaration to set for the wrapper which stands for a pipelines. 
         */
        public void setDeclaration(DecisionVariableDeclaration decl) {
            this.setDecl(decl);
        }
        /**
         * Set the declaration fot this wrapper.
         * @param decl declaration to set.
         */
        private void setDecl(DecisionVariableDeclaration decl) {
            this.decl = decl;
        }
        /**
         * Add a treeElement to this wrapper.
         * @param newElem new element to add.
         */
        public void addTreeElement(PipelineGraphColoringWrapper newElem) {
            
            boolean contained = false;
            for (int i = 0; i < this.descendants.size(); i++) {
                PipelineGraphColoringWrapper wrapper = this.descendants.get(i);
                
                if (wrapper.getElemName().trim().equals(newElem.getElemName().trim())) {
                    contained = true;
                }
            }
            if (!contained) {
                descendants.add(newElem);
            }
        }
        /**
         * Method which checks whether a certain element is already contained within this wrapper.
         * @param newElem element which can be added.
         * @return true if newElem is not contained. Therefore it can be added. False if already contained.
         */
        public boolean alreadyContains(PipelineGraphColoringWrapper newElem) {
            
            boolean contains = false;
            for (int i = 0; i < this.descendants.size(); i++) {
                PipelineGraphColoringWrapper in = this.descendants.get(i);
                String inName = in.getElemName().replaceAll("\\s", "");
                String attendName = newElem.getElemName().replaceAll("\\s", "");
                
                if (inName.equals(attendName)) {
                    contains = true;
                }
            }
            return contains;
        }
        
        /**
         * Get the descendants of this wrapper.
         * @return descendants Alle children of this wrapper object.
         */
        public List<PipelineGraphColoringWrapper> getDecendant() {
            return descendants;
        }
        /**
         * Get the name of the wrapper.
         * @return elemName name of the wrapper,
         */
        public String getElemName() {
            return elemName;
        }
        /**
         * Get the type of the wrapper.
         * @return type the type of the wrapper.
         */
        public PipelineNodeType getType() {
            return type;
        }
        /**
         * Get the var of the wrapper.
         * @return var the variable of thw wrapper.
         */
        public IDecisionVariable getVar() {
            return var;
        }
        /**
         * Get the pipelineParent of the wrapper.
         * @return pipelineParent pipeline parent of the wrapper.
         */
        public String getPipelineParent() {
            return pipelineParent;
        }
        /**
         * Get the declaration of the wrapper.
         * @return declaration the wrappers declaration.
         */
        public DecisionVariableDeclaration getDecl() {
            return decl;
        }
        /**
         * Get the observables for the wrapper.
         * @return obs the wrappers observable.
         */
        public String getObs() {
            return obs;
        }
        /**
         * Get the wrappers color.
         * @return color the wrappers color.
         */
        public Color getColor() {
            return color;
        }
        
    }
    /**
     * Search for a specific file within the given folder, in this case a folder with the extension
     * ".ivml".
     * 
     * @param workspaceURI EASy-folder from the project we work on right now.
     * @return toReturn the File which is the EASy-folder of the project we work on right now.
     */
    public File getPipelineConfiguration(final File workspaceURI) {
        
        File toReturn = null;
        
        for (final File fileEntry : workspaceURI.listFiles()) {
 
            if (fileEntry.isDirectory()) {
                
                for (final File fileEntry2 : fileEntry.listFiles()) {
                    
                    if (fileEntry2.getName().equals(EASY_STRING)) {
                        File file = new File(fileEntry2.getPath());

                        for (final File fileEntry3 : file.listFiles()) {

                            if (fileEntry3.isDirectory() && fileEntry3.getName().endsWith(
                                    QmConstants.VAR_PIPELINES_PIPELINES)) { 
                                File file2 = new File(fileEntry3.getPath());

                                for (final File fileEntry4 : file2.listFiles()) {

                                    if (fileEntry4.isFile() && fileEntry4.toString().endsWith(IVML_STRING)) {

                                        savePipelineStructures(fileEntry4);
                                    }
                                }
                            }
                            if (fileEntry3.isDirectory() && fileEntry3.getName().endsWith(META_STRING)) {

                                File file2 = new File(fileEntry3.getPath());

                                for (final File fileEntry4 : file2.listFiles()) {

                                    if (fileEntry4.isFile() && fileEntry4.toString().endsWith(PIPELINE_IVML_FILE)) {

                                        saveObservableStructures(fileEntry4);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return toReturn;
    }
    
    /**
     * Save all observables which can be found in the configuration. Every observalbes and its mapping to
     * the pipeline-elements.type.
     * @param fileEntry fileEntry for the Pipelines.ivml.
     */
    private void saveObservableStructures(File fileEntry) {
        TranslationResult<Project> result;
        try {
            result = de.uni_hildesheim.sse.ModelUtility.INSTANCE.parse(fileEntry);
            Project project123 = result.getResult(0);
            
            Compound modelElement = (Compound) project123.getElement(QmConstants.TYPE_PIPELINE_NODE);
            
            for (int i = 0; i < modelElement.getAssignmentCount(); i++) {
                AttributeAssignment assignment = modelElement.getAssignment(i);
                for (int j = 0; j < assignment.getElementCount(); j++) {
                    DecisionVariableDeclaration declaration = assignment.getElement(j);

                    observablePipelineNodes.add(declaration.getName()); 
                }
            }
            //------------------
            Compound modelElement2 = (Compound) project123.getElement(QmConstants.TYPE_SOURCE);
            
            for (int i = 0; i < modelElement2.getAssignmentCount(); i++) {            
                AttributeAssignment assignment = modelElement2.getAssignment(i);
                
                for (int j = 0; j < assignment.getElementCount(); j++) {
                    DecisionVariableDeclaration declaration = assignment.getElement(j);
                    
                    String text = declaration.getName().toLowerCase();
                    text = text.replaceAll("[^a-zA-Z0-9]", "");
                    observableSource.add(text);
                }
            }
            CustomObservableList newList = new CustomObservableList();
            newList.addAll(observablePipelineNodes);
            newList.addAll(observableSource);
            observalesPipMapping.put(PipelineNodeType.Source.name(), newList);
            //------------------
            
            saveMoreObservableStructures(project123);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save observables.
     * @param project123 Project which contains the observables.
     */
    private void saveMoreObservableStructures(Project project123) {

        Compound modelElement3 = (Compound) project123.getElement(QmConstants.TYPE_SINK);
        
        for (int i = 0; i < modelElement3.getAssignmentCount(); i++) { 
            AttributeAssignment assignment = modelElement3.getAssignment(i);
            
            for (int j = 0; j < assignment.getElementCount(); j++) {
                DecisionVariableDeclaration declaration = assignment.getElement(j);
                
                observableSink.add(declaration.getName());
            }
        }
        CustomObservableList newList2 = new CustomObservableList();
        newList2.addAll(observablePipelineNodes);
        newList2.addAll(observableSink);
        observalesPipMapping.put(PipelineNodeType.Sink.name(), newList2);
        //------------------
        Compound modelElement4 = (Compound) project123.getElement(QmConstants.TYPE_FAMILYELEMENT);
        
        for (int i = 0; i < modelElement4.getAssignmentCount(); i++) {  
            AttributeAssignment assignment = modelElement4.getAssignment(i);
            
            for (int j = 0; j < assignment.getElementCount(); j++) {
                DecisionVariableDeclaration declaration = assignment.getElement(j);
                
                observableFamily.add(declaration.getName());
            }
        }
        CustomObservableList newList3 = new CustomObservableList();
        newList3.addAll(observablePipelineNodes);
        newList3.addAll(observableFamily);
        observalesPipMapping.put(PipelineNodeType.FamilyElement.name(), newList3);
        //------------------
        Compound modelElement5 = (Compound) project123.getElement(QmConstants.TYPE_DATAMANAGEMENTELEMENT);
        
        for (int i = 0; i < modelElement5.getAssignmentCount(); i++) {             
            AttributeAssignment assignment = modelElement5.getAssignment(i);
            
            for (int j = 0; j < assignment.getElementCount(); j++) {
                DecisionVariableDeclaration declaration = assignment.getElement(j);
                
                observableDatamanagement.add(declaration.getName());
            }
        }
        CustomObservableList newList4 = new CustomObservableList();
        newList4.addAll(observablePipelineNodes);
        newList4.addAll(observableDatamanagement);
        observalesPipMapping.put(PipelineNodeType.DataManagementElement.name(), newList4);
        //------------------
        Compound modelElement6 = (Compound) project123.getElement(QmConstants.TYPE_PIPELINE);
        
        for (int i = 0; i < modelElement6.getAssignmentCount(); i++) {  
            AttributeAssignment assignment = modelElement6.getAssignment(i);
            
            for (int j = 0; j < assignment.getElementCount(); j++) {
                DecisionVariableDeclaration declaration = assignment.getElement(j);
                
                observablePipelines.add(declaration.getName().trim().toLowerCase());
            }
        }
        CustomObservableList newList5 = new CustomObservableList();
        newList5.addAll(observablePipelines);
        observalesPipMapping.put(PipelineNodeType.Pipeline.name(), newList5);
    }

    /**
     * Save the pipelines and their nodes, thus they can be displayed in a tree.
     * @param fileEntry3 fileEntry for pipeline configurations. For example: PipelineVar_1Cfg.ivml
     */
    private void savePipelineStructures(File fileEntry3) {
        
        TranslationResult<Project> result;
        try {
            result = de.uni_hildesheim.sse.ModelUtility.INSTANCE.parse(fileEntry3);
            Project project = result.getResult(0);
            
            for (int i = 0; i < project.getElementCount(); i++) {

                ContainableModelElement modelElement = project.getElement(i);
                
                if (pipelines.contains(modelElement.getName())) {

                    if (modelElement instanceof DecisionVariableDeclaration) {
                        DecisionVariableDeclaration declaration = (DecisionVariableDeclaration) modelElement;
                        
                        if (declaration.getDefaultValue() == null) {
                            Configuration cfg = new Configuration(project);
                            
                            IDecisionVariable actualPipelinesVar = cfg.getDecision(declaration);
                            CompoundValue cmpValue = (CompoundValue) actualPipelinesVar.getValue();
                            
                            getPipelineSource(cmpValue, project, declaration);

                        } else {
                            if (declaration.getDefaultValue() instanceof ConstantValue) {
                                ConstantValue cmpValue = (ConstantValue) declaration.getDefaultValue();
                                CompoundValue cmp = (CompoundValue) cmpValue.getConstantValue();

                                getPipelineSource(cmp, project, declaration);
                            }
                        }
                    }

                }
            }  
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
    }

    /**
     * Start with the source node of the pipeline.
     * @param cmp compoundValue which is the configurations source node.
     * @param project pipelines icml-project will be used later in a method.
     * @param declaration declaration of current node.
     */
    private void getPipelineSource(CompoundValue cmp, Project project, DecisionVariableDeclaration declaration) {

        String value = (String) cmp.getNestedValue("name").getValue();
        
        PipelineNodeType type = checknodeType(declaration);
        
        Configuration cfg = new Configuration(project);
        
        IDecisionVariable actualPipelinesVar = cfg.getDecision(declaration);
        
        PipelineGraphColoringWrapper treeElem = new PipelineGraphColoringWrapper(
            value, type, value, actualPipelinesVar, declaration);
        
        treeElem.setVar(actualPipelinesVar);
        treeElem.setDeclaration(declaration);
 
        //add all children to this element and then store it in a list.
        
        ContainerValue sourceValue = (ContainerValue) cmp.getNestedValue(QmConstants.SLOT_PIPELINE_SOURCES);

        if (sourceValue.getElementSize() > 0) { 
            
            for (int i = 0; i < sourceValue.getElementSize(); i++) {
                
                String sourceToSearch = sourceValue.getElement(i).getValue().toString();
                
                recursiveOverPipeline(treeElem, sourceToSearch, project, value);
            }
        }
        getPipelinesToDisplayInTable().add(treeElem);

    }

    /**
     * Check for the pipeline nodes type.
     * @param declaration the given declaration if the node.
     * @return type The PipelinNodeType e.g family, datamanagementElement...
     */
    private PipelineNodeType checknodeType(DecisionVariableDeclaration declaration) {
        PipelineNodeType type = null;

        String typeName = declaration.getType().getName();
        
        if (typeName.equalsIgnoreCase(PipelineNodeType.DataManagementElement.name())) {
            type = PipelineNodeType.DataManagementElement;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.FamilyElement.name())) {
            type = PipelineNodeType.FamilyElement;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Sink.name())) {
            type = PipelineNodeType.Sink;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Source.name())) {
            type = PipelineNodeType.Source;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Pipeline.name())) {
            type = PipelineNodeType.Pipeline;
        }
        return type;
    }

    /**
     * Check for the pipeline nodes type.
     * @param actualPipelinesVar the given variable if the node.
     * @return type The PipelinNodeType e.g family, datamanagementElement...
     */
    private PipelineNodeType checknodeType(IDecisionVariable actualPipelinesVar) {
        
        PipelineNodeType type = null;
        
        AbstractVariable declaration = actualPipelinesVar.getDeclaration();
        String typeName = declaration.getType().getName();
        
        if (typeName.equalsIgnoreCase(PipelineNodeType.DataManagementElement.name())) {
            type = PipelineNodeType.DataManagementElement;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.FamilyElement.name())) {
            type = PipelineNodeType.FamilyElement;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Sink.name())) {
            type = PipelineNodeType.Sink;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Source.name())) {
            type = PipelineNodeType.Source;
        }
        if (typeName.equalsIgnoreCase(PipelineNodeType.Pipeline.name())) {
            type = PipelineNodeType.Pipeline;
        }

        return type;
    }

    /**
     * GO recursively through the pipeline-structures and save the needed information in wrapper-object for displaying.
     * @param treeElem Current top-Level TreeElement. These are the Pipeline-Knots in the tree.
     * @param sourceToSearch Current source to search for.
     * @param project Pipelines project.
     * @param value nodes name awhich will be later neede in order to set a pipelines.-wrappers name.
     */
    private void recursiveOverPipeline(PipelineGraphColoringWrapper treeElem, String sourceToSearch,
            Project project, String value) {
        
        for (int i = 0; i < project.getElementCount(); i++) {
            ContainableModelElement modelElement = project.getElement(i);
            topLevelPipelineElements.put(modelElement.getName(), modelElement);
        }
        for (int j = 0; j < topLevelPipelineElements.values().size(); j++) {
            ContainableModelElement modelElement = (ContainableModelElement)
                    topLevelPipelineElements.values().toArray()[j];

            if (modelElement.getName() != null & sourceToSearch != null) { 
                if (modelElement.getName().equals(sourceToSearch)) {

                    DecisionVariableDeclaration decl = (DecisionVariableDeclaration) modelElement;
                    
                    Configuration cfg = new Configuration(project);
                    
                    IDecisionVariable actualPipelinesVar = (IDecisionVariable) cfg.getDecision(decl);
                    
                    iterateOverNode(actualPipelinesVar, treeElem, cfg, value);

                }
            }
        }
        
        
    }

    /**
     * Follow the pipline structures by:
     * - Firstly take the last elements output
     * - moreover folow the accessed flow in order to get to the next node.
     * 
     * @param actualPipelinesVar current pipeline-variable.
     * @param treeElem current top-level-treeelement.
     * @param cfg Pipelines Configuration.
     * @param value value which will be later needed in orrder to name a pipelinewrapper.
     */
    private void iterateOverNode(IDecisionVariable actualPipelinesVar, PipelineGraphColoringWrapper treeElem,
            Configuration cfg, String value) {
        
        if (actualPipelinesVar.getNestedElement(QmConstants.SLOT_OUTPUT) != null) {
            if (actualPipelinesVar.getNestedElement(QmConstants.SLOT_OUTPUT).getNestedElementsCount() > 0
                    && actualPipelinesVar.getNestedElement(QmConstants.SLOT_OUTPUT).getNestedElement(0).hasValue()) { 
                
                    
                PipelineNodeType type = checknodeType(actualPipelinesVar);
                IDecisionVariable decNameVar = actualPipelinesVar.getNestedElement(NAME_STRING);
                String name = decNameVar.toString();
                
                if (name.contains("=") && name.contains(":")) {
                    name = name.substring(name.indexOf("="), name.indexOf(":"));
                    name = name.replaceAll("[^a-zA-Z0-9]", "");
                    name = name.trim();
                    PipelineGraphColoringWrapper newElem = new PipelineGraphColoringWrapper(
                            name, type, value, actualPipelinesVar);

                    if (!treeElem.alreadyContains(newElem)) {
                        treeElem.addTreeElement(newElem);
                    }
                    
                    IDecisionVariable reference = actualPipelinesVar.getNestedElement(QmConstants.SLOT_OUTPUT);
                    for (int i = 0; i < reference.getNestedElementsCount(); i++) {
                        
                        ReferenceValue nodeSet = (ReferenceValue) reference.getNestedElement(i).getValue();
                        String nextFlow = nodeSet.getValue().getName();
                        
                        iterateOverFlow(nextFlow, cfg, treeElem, value);
                    }
                
                }
            }
        }
        
    }

    /**
     * Follow the pipline structures by:
     * Follow the flows destination.
     * 
     * @param nextFlow current flow.
     * @param treeElem current top-level-treelement.
     * @param cfg Pipelines Configuration.
     * @param nameValue value which will be later needed in order to name a pipelinewrapper.
     */
    private void iterateOverFlow(String nextFlow, Configuration cfg,
           PipelineGraphColoringWrapper treeElem, String nameValue) {
        
        DecisionVariableDeclaration modelElement = (DecisionVariableDeclaration) topLevelPipelineElements.get(nextFlow);
        
        CompoundVariable node = (CompoundVariable) cfg.getDecision(modelElement);
        //PipelineNodeType type = checknodeType(node);
        ReferenceValue reference = (ReferenceValue) node.getNestedElement(QmConstants.SLOT_FLOW_DESTINATION).getValue();
        
        DecisionVariableDeclaration object = (DecisionVariableDeclaration) reference.getValue();
        IDecisionVariable variable = (IDecisionVariable) cfg.getDecision(object);
        PipelineNodeType type = checknodeType(variable);
        
        Value value = variable.getNestedElement(NAME_STRING).getValue();
        PipelineGraphColoringWrapper newElem = new PipelineGraphColoringWrapper(value.getValue().toString(), 
               type, treeElem.getElemName(), variable, object);

        if (!treeElem.alreadyContains(newElem)) {
            treeElem.addTreeElement(newElem);
        }
        
        iterateOverNode(variable, treeElem, cfg, nameValue);
        
    }

    /**
    * Check whether pipeline is already present in the color chooser table.
    * @param treeViewerColorChooser color chooser table.
    * @param name name of the new pipeline-element.
    * @return true if already contained/ false if not,.
    */
    public boolean pipNotExisting(Table treeViewerColorChooser, String name) {
        boolean toReturn = false;
     
        for (int i = 0; i < treeViewerColorChooser.getItemCount(); i++) {
            TableItem item = treeViewerColorChooser.getItem(i);
            String existingPipName = item.getText(0);
          
            if (name.equals(existingPipName)) {
                toReturn  = true;
            }
        }
        return toReturn;
    }
    
    /**
     * Add a pipeline to the list.
     * @param name name of the pipeline to add.
     */
    public void addPipeline(String name) {
        pipelines.add(name);
    }

    /**
     * Get the pipelines which will be displayed in the first table.
     * @return pipelinesToDisplayInTable List of pipeline-elements to display.
     */
    public List<PipelineGraphColoringWrapper> getPipelinesToDisplayInTable() {
        return pipelinesToDisplayInTable;
    }

    /**
     * Set the pipelines which will be displayed in the first table.
     * @param pipelinesToDisplayInTable List of pipelines tio display in the table.
     */
    public void setPipelinesToDisplayInTable(List<PipelineGraphColoringWrapper> pipelinesToDisplayInTable) {
        this.pipelinesToDisplayInTable = pipelinesToDisplayInTable;
    }

    /**
    * Check whether the combination of pipeline and observable is already exiting in the table.
    * @param savedObservablesTable table which holds the items.
    * @param pipParent parent of the new item.
    * @param name name of the new item.
    * @param observableName observable of the new item.
    * @return true if no existing/ false if alement is already present.
    */
    public boolean pipelineObservableCombinationIsNotExisting(Table savedObservablesTable, String pipParent,
         String name, String observableName) {
     
        boolean toReturn = false;
     
        for (int i = 0; i < savedObservablesTable.getItemCount(); i++) {
         
            TableItem item = savedObservablesTable.getItem(i);
         
            String existingItemText = item.getText(0);
         
            if (existingItemText.contains(pipParent) && existingItemText.contains(name)
                   && existingItemText.contains(observableName)) {
                
                if (existingItemText.length() > pipParent.length() + name.length()
                        + observableName.length()) {
                    
                    toReturn = true;
                }
            }
        }
        return toReturn;
    }

    /**
     * Update the content of the observalbesTable corresponding the the selected pipelines.
     * @param observablesTable Tabe for observale items.
     * @param type type of the currently selected pipeline element.
     */
    public void setObservablesTableSelections(Table observablesTable, PipelineNodeType type) {  
        observablesTable.removeAll();
        observablesTable.redraw();
        for (String item : backupObservableItem) {
            TableItem tableItem = new TableItem(observablesTable, SWT.CHECK);
            tableItem.setText(item);
        }
        switch (type) {
        case Source:
            for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

                if (!observableSource.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();
            break;               
        case Sink:
            for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableSink.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();
            break;           
        case FamilyElement:
            for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableFamily.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                } 
            }
            observablesTable.redraw();
            break;  
        case DataManagementElement:
            for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableDatamanagement.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                } 
            }
            observablesTable.redraw();
            break;  
        case Flow:    
        case ProcessingElement:     
        case Pipeline:
            for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!containsObservable(observablePipelines, toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();
            break;      
        default:
            break;
        }
    }

    /**
     * Check whether a list contains a String.
     * @param observablePipelines List of pipeline names aka Strings.
     * @param toContain The String object to be checked.
     * @return true if observablePipelines contains toContain, false otherwise.
     */
    private boolean containsObservable(CustomObservableList observablePipelines, String toContain) {
        
        boolean toReturn = false;
        
        for (int i = 0; i < observablePipelines.size(); i++) {
            
            String observableName = observablePipelines.get(i);
            observableName = observableName.trim().toLowerCase();
            
            toContain = toContain.trim().toLowerCase();
            
            if (observableName.contains(toContain) || toContain.contains(observableName)) {
                toReturn = true;
            }
            
        }
        return toReturn;
    }
    /**
     * Check whether items should be removed from the observables table because they arent supported by
     * the selected pipeline-element.
     * @param observablesTable observables table.
     * @param item Current item.
     */
//    private void checkTableItem(Table observablesTable, TableItem item) {
//        String text = item.getText();
//        String toContain = text.toLowerCase();
//        toContain = toContain.replaceAll("[^a-zA-Z0-9]", ""); 
//        
//        if (!observableSink.contains(toContain)) {
//            observablesTable.remove(observablesTable.indexOf(item));
//        }
//    }
    
    /**
     * This class provides the content for the tree in FileTree.
     */
    public class RuntimeEditorContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] result;
            if (inputElement instanceof List) {
                result = ((List<?>) inputElement).toArray();
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            Object[] result;
            if (parentElement instanceof PipelineGraphColoringWrapper) {
                PipelineGraphColoringWrapper treeElement = (PipelineGraphColoringWrapper) parentElement;
                result = treeElement.descendants.toArray();
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            boolean toReturn = false;
            if (element instanceof PipelineGraphColoringWrapper) {
                PipelineGraphColoringWrapper treeElement = (PipelineGraphColoringWrapper) element;
                
                if (treeElement.descendants.size() > 0) {
                    toReturn = true;
                } 
            }
            return toReturn;
        }
    }

    /**
     * This class provides the labels for the file tree.
     */
    public class RuntimeEditorLabelProvider implements ILabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getImage(Object element) {
            return null;
        }

        @Override
        public String getText(Object element) {
            String toReturn = "";
            if (element instanceof PipelineGraphColoringWrapper) {
                PipelineGraphColoringWrapper treeElem = (PipelineGraphColoringWrapper) element;
                toReturn = treeElem.getElemName();
            }
            return toReturn;
        }
    }
    
    /**
     * Get the backUp-List for the observable-Elements.
     * @return backupObservableItem backUp-List for the observable-Elements.
     */
    public ArrayList<String> getBackupObservableItem() {
        return backupObservableItem;
    }
    /**
     * Set the backUp-List for the observable-Elements.
     * @param backupObservableItem List for observalbe items.
     */
    public void setBackupObservableItem(ArrayList<String> backupObservableItem) {
        this.backupObservableItem = backupObservableItem;
    }
}