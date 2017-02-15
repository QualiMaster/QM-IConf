package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
//import java.util.Map;
//import java.util.Set;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import de.uni_hildesheim.sse.qmApp.WorkspaceUtils;
//import de.uni_hildesheim.sse.qmApp.editors.RuntimeEditor;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.PipelineContentsContainer;
import eu.qualimaster.easy.extension.internal.PipelineVisitor;
import eu.qualimaster.easy.extension.internal.Utils;
//import net.ssehub.easy.dslCore.TranslationResult;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
//import net.ssehub.easy.varModel.model.Project;
//import net.ssehub.easy.varModel.model.datatypes.Compound;
//import net.ssehub.easy.varModel.model.values.CompoundValue;
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
    
    public static final String FILENAME = "runtimeSavedItems";
    public static final String FILENAME_EXT = ".ser";
    
    //private static final String EASY_STRING = "EASy";
    //private static final String META_STRING = "meta";
    //private static final String PIPELINE_IVML_FILE = "Pipelines.ivml";
    
    //private RuntimeEditorContentProvider contentProvider = new RuntimeEditorContentProvider();
    //private RuntimeEditorLabelProvider labelProvider = new RuntimeEditorLabelProvider();
    
    //private HashMap<String, CustomObservableList> observalesPipMapping
    //        = new HashMap<String, CustomObservableList>();

    private ArrayList<String> pipelines = new ArrayList<String>();
    private List<PipelineGraphColoringWrapper> pipelinesToDisplayInTable
            = new ArrayList<PipelineGraphColoringWrapper>();
    //private  ArrayList<String> backupObservableItem = new ArrayList<String>();
   
    /*private CustomObservableList observablePipelineNodes = new CustomObservableList();
    private CustomObservableList observableFamily = new CustomObservableList();
    private CustomObservableList observableDatamanagement = new CustomObservableList();
    private CustomObservableList observableSink = new CustomObservableList();
    private CustomObservableList observableSource = new CustomObservableList();*/

    /**
     * // Exists only to avoid instantiation.
     */
    private PipelinesRuntimeUtils() {
    }
    
    /**
     * Get the contentprovider.
     * @return contentProvider the contentprovider.
     */
    /*public RuntimeEditorContentProvider getContentProvider() {
        return contentProvider;
    }*/
    /**
     * Get the Labelprovider.
     * @return labelprovidfer labelprovider.
     */
    /*public RuntimeEditorLabelProvider getLabelProvider() {
        return labelProvider;
    }*/
    /**
     * A ArrayList with useful contains-method.
     * 
     * @author Niko Nowatzki
     */
    public static class CustomObservableList extends ArrayList<String> {

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
     * Search for a specific file within the given folder, in this case a folder with the extension
     * ".ivml".
     * 
     * @param workspaceURI EASy-folder from the project we work on right now.
     */
    public void getPipelineConfiguration(final File workspaceURI) {
         
        /*for (final File fileEntry : workspaceURI.listFiles()) {
            if (fileEntry.isDirectory()) {   
                for (final File fileEntry2 : fileEntry.listFiles()) { 
                    if (fileEntry2.getName().equals(EASY_STRING)) {
                        File file = new File(fileEntry2.getPath());

                        for (final File fileEntry3 : file.listFiles()) {
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
        }*/

        // Find pipelines container
        Configuration config = de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration
            .PIPELINES.getConfiguration();
        IDecisionVariable allPipelinesVariable = null;
        Iterator<IDecisionVariable> varItr = config.iterator();
        while (varItr.hasNext() && null == allPipelinesVariable) {
            IDecisionVariable tmpVar = varItr.next();
            if (QmConstants.VAR_PIPELINES_PIPELINES.equals(tmpVar.getDeclaration().getName())) {
                allPipelinesVariable = tmpVar; 
            }
        }
        
        // Extract all single pipeline instances from pipeline set variable
        if (null != allPipelinesVariable && null != allPipelinesVariable.getValue()) {
            Value allPipelinesValue = allPipelinesVariable.getValue();
            
            if (allPipelinesValue instanceof ContainerValue) {
                ContainerValue pipelinesContainer = (ContainerValue) allPipelinesValue;
                for (int i = 0; i < pipelinesContainer.getElementSize(); i++) {
                    ReferenceValue referenceValue = (ReferenceValue) pipelinesContainer.getElement(i);
                    IDecisionVariable pipelineVar = Utils.extractVariable(referenceValue, config);
                    PipelineVisitor visitor = new PipelineVisitor(pipelineVar, false);
                    PipelineContentsContainer container = visitor.getPipelineContents();
                    
                    String pipelineName = pipelineVar.getDeclaration().getUniqueName();
                    INSTANCE.addPipeline(pipelineName);
                    
                    PipelineGraphColoringWrapper parentElem = new PipelineGraphColoringWrapper(pipelineVar, 
                            PipelineNodeType.Pipeline, "");
                    
                    createPipelineWrappers(parentElem, pipelineVar, container.getSources(), PipelineNodeType.Source);
                    createPipelineWrappers(parentElem, pipelineVar, container.getFamilyElements(),
                            PipelineNodeType.FamilyElement);
                    createPipelineWrappers(parentElem, pipelineVar, container.getDataManagementElements(),
                        PipelineNodeType.DataManagementElement);
                    createPipelineWrappers(parentElem, pipelineVar, container.getSinks(), PipelineNodeType.Sink);
                    
                    
                    getPipelinesToDisplayInTable().add(parentElem);
                }
            }
        }
    }
    
    /**
     * Create the wrapper objects for all pipeline-elements in the configuration.
     * @param parentElem The current parentElement from the pipelines-confguration.
     * @param pipelineVar The current pipeline-element-variable.
     * @param nodes A List of multiple nodes. Every parent can have several children.
     * @param type The type of the container. Source, Family, DataManagement or Sink.
     */
    private void createPipelineWrappers(PipelineGraphColoringWrapper parentElem, IDecisionVariable pipelineVar,
            List<IDecisionVariable> nodes, PipelineNodeType type) {
        
        for (IDecisionVariable node : nodes) {
            
            if (type != PipelineNodeType.Pipeline) {
                PipelineGraphColoringWrapper subTreeElem = new PipelineGraphColoringWrapper(node, type,
                        parentElem.getElemName());
                parentElem.addTreeElement(subTreeElem);
            } else {
                PipelineGraphColoringWrapper subTreeElem = new PipelineGraphColoringWrapper(node, type,
                    null);
                parentElem.addTreeElement(subTreeElem);
            }
        }
        
    }
    /**
     * Save all observables which can be found in the configuration. Every observalbes and its mapping to
     * the pipeline-elements.type.
     * @param fileEntry fileEntry for the Pipelines.ivml.
     */
    /*private void saveObservableStructures(File fileEntry) {
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
    }*/

    /**
     * Save observables.
     * @param project123 Project which contains the observables.
     */
    /*private void saveMoreObservableStructures(Project project123) {

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
    }*/

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
    * @param treeViewerColorChooser table which holds the items and colors.
    * @param pipParent parent of the new item.
    * @param name name of the new item.
    * @param observableName observable of the new item.
    * @return toReturn true if no existing/ false if alement is already present.
    */
    public boolean pipelineObservableCombinationIsNotExisting(Table treeViewerColorChooser, String pipParent,
         String name, String observableName) {
     
        boolean toReturn = false;
     
        for (int i = 0; i < treeViewerColorChooser.getItemCount(); i++) {
         
            TableItem item = treeViewerColorChooser.getItem(i);
         
            String existingItemText = item.getText(0);
         
            if (existingItemText.contains(pipParent) && existingItemText.contains(name)
                   && existingItemText.contains(observableName)) {   
                toReturn = true;
            }
        }
        return toReturn;
    }
    
    /*private void purgeTable(Table observablesTable, CustomObservableList list1, CustomObservableList list2) {
        for (int i = observablesTable.getItemCount() - 1; i >= 0; i--) {
            TableItem item = observablesTable.getItem(i);
            String text = item.getText();
            String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            if (!list1.contains(toContain) && (null == list2 || !list2.contains(toContain))) {
                observablesTable.remove(i);
            }
        }
        observablesTable.redraw();
    }*/

    /**
     * Update the content of the observalbesTable corresponding the the selected pipelines.
     * @param observablesTable Table for observable items.
     * @param type type of the currently selected pipeline element.
     * @param deliveringObservables 
     * @param selectedElementName name of the selected element.
     * @param connection 
     */
    /*public void setObservablesTableSelections(Table observablesTable, PipelineNodeType type, HashMap<String,
            Set<String>> deliveringObservables, String selectedElementName, boolean connection) {
        
        //if (deliveringObservables != null && deliveringObservables.size() > 0) {
        removeAndCollect(observablesTable);
        
        switch (type) {
        case Source:
            purgeTable(observablesTable, observableSource, observablePipelineNodes);*/
            /*for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");

                if (!observableSource.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
System.out.println("REMOVE " + observablesTable.indexOf(item));                    
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();*/
/*            break;               
        case Sink:
            purgeTable(observablesTable, observableSink, observablePipelineNodes);*/
            /*for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableSink.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
System.out.println("REMOVE " + observablesTable.indexOf(item));
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();*/
/*            break;           
        case FamilyElement:
            purgeTable(observablesTable, observableFamily, observablePipelineNodes);*/
            /*for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableFamily.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
System.out.println("REMOVE " + observablesTable.indexOf(item));
                    observablesTable.remove(observablesTable.indexOf(item));
                } 
            }
            observablesTable.redraw();*/
/*            break;  
        case DataManagementElement:
            purgeTable(observablesTable, observableDatamanagement, observablePipelineNodes);*/
            /*for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!observableDatamanagement.contains(toContain) && !observablePipelineNodes.contains(toContain)) {
                    observablesTable.remove(observablesTable.indexOf(item));
                } 
            }
            observablesTable.redraw();*/
/*            break;  
        case Flow:    
        case ProcessingElement: 
        case SubPipeline:
        case Pipeline:
            purgeTable(observablesTable, observablePipelines, null);
            /*for (TableItem item : observablesTable.getItems()) {
                String text = item.getText();
                String toContain = text.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
                
                if (!containsObservable(observablePipelines, toContain)) {
System.out.println("REMOVE " + observablesTable.indexOf(item));                    
                    observablesTable.remove(observablesTable.indexOf(item));
                }
            }
            observablesTable.redraw();*/
/*            break;      
        default:
            break;
        }
        disableNonDeliveringObservables(observablesTable, deliveringObservables, selectedElementName, connection);
    }*/

    /**
     * Remove all items from the observableTable and collect all qualityparameters again.
     * Then add these observables back to the table.
     * @param observablesTable The table which displays obserbalves- items.
     */
    /*private void removeAndCollect(Table observablesTable) {
        
        observablesTable.removeAll();
        observablesTable.redraw();
        
        net.ssehub.easy.varModel.confModel.Configuration config = de.uni_hildesheim.sse.qmApp.model.VariabilityModel.
                Configuration.OBSERVABLES.getConfiguration();
        Iterator<IDecisionVariable> iter = config.iterator();

        while (iter.hasNext()) {
            Object object = iter.next();
            if (object instanceof SetVariable || object instanceof SequenceVariable) {
                RuntimeEditor.collectQualityParameters(object);
            }
        }
        observablesTable.redraw();
    }*/

    /**
     * Remove the observables from the table which wont provide data.
     * @param observablesTable table which holds the observable items.
     * @param deliveringObservables the delivering items we dont want to remove.
     * @param selectedElementName currently selected element in pipeline selection table..
     * @param connection true if a connection to the infrastructure is on/ false otherwise.
     */
    /*private void disableNonDeliveringObservables(Table observablesTable,
            HashMap<String, Set<String>> deliveringObservables, String selectedElementName, boolean connection) {
        
        List<String> supportedObservables = new ArrayList<String>();
        
        Set<String> observations = deliveringObservables.get(selectedElementName);
        
        if (connection) {
            for (int i = 0; i < observablesTable.getItemCount(); i++) {
                TableItem item = observablesTable.getItem(i);
                String itemText = item.getText();
                
                if (observations != null) {
                                
                    Iterator<String> iterator = observations.iterator();
                    while (iterator.hasNext()) {
     
                        String iterObs = (String) iterator.next();
    
                        HashMap<String, String> observablesMap = RuntimeEditor.getObservablesMap();
                        
                        String observableToCompare = observablesMap.get(itemText);

                        if (iterObs.equals(observableToCompare)) {
                            supportedObservables.add(itemText.trim());
                        }
                    }
                }
            }         
        
            Set<String> supportedObservablesWithoutDuplicates = new HashSet<String>(supportedObservables);
            observablesTable.removeAll();
            observablesTable.redraw();
            
          
            Iterator<String> iterator = supportedObservablesWithoutDuplicates.iterator();
            
            while (iterator.hasNext()) {
                String text = (String) iterator.next();
                
                TableItem obsItem = new TableItem(observablesTable, SWT.CHECK);
                obsItem.setText(text);
            }
    
            observablesTable.redraw();
        }
    }*/

    /**
     * Check whether a list contains a String.
     * @param observablePipelines List of pipeline names aka Strings.
     * @param toContain The String object to be checked.
     * @return true if observablePipelines contains toContain, false otherwise.
     */
    /*private boolean containsObservable(CustomObservableList observablePipelines, String toContain) {
        
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
    }*/
    
    /**
     * Store selected items as metadata. They can be loaded with the next session.
     * @param wrapperList List of selected elements to save.
     */
    public static void storeInfoInMetadata(List<PipelineElementObservableWrapper> wrapperList) {
        try {
            boolean foundFile = false;
            int counter = 1;
            while (!foundFile) { 
                
                
                if (!new File(WorkspaceUtils.getMetadataFolder(), FILENAME + counter + FILENAME_EXT).exists()) {
                    foundFile = true;
                    
                    FileOutputStream fileoutputstream;
                    
                    fileoutputstream = new FileOutputStream(getItemsFile(FILENAME + counter + FILENAME_EXT));
     
                    ObjectOutputStream outputstream = new ObjectOutputStream(fileoutputstream);
                    outputstream.writeObject(wrapperList);
                    outputstream.close();
                }
                counter++;
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Returns the file for storing the Maven tree persistently (offline use).
     * @param name 
     * 
     * @return the file
     */
    public static File getItemsFile(String name) {
        File toReturn = null;
        toReturn = new File(WorkspaceUtils.getMetadataFolder(), name);
        
        return toReturn;
    }
    
    /**
     * Get the backUp-List for the observable-Elements.
     * @return backupObservableItem backUp-List for the observable-Elements.
     */
    /*public ArrayList<String> getBackupObservableItem() {
        return backupObservableItem;
    }*/
    /**
     * Set the backUp-List for the observable-Elements.
     * @param backupObservableItem List for observalbe items.
     */
    /*public void setBackupObservableItem(ArrayList<String> backupObservableItem) {
        this.backupObservableItem = backupObservableItem;
    }*/

    /**
     * Clear the list of pipelines.
     */
    public void clearPipelines() {   
        pipelines.clear();
        pipelinesToDisplayInTable.clear();
    }

    /**
     * Check if stored data exists and can be loaded. True if data is stored, false otherwise.
     * @return toReturn true if data is found, false otherwise.
     */
    public static boolean storedDataExist() {
        File file = getItemsFile(FILENAME + 0 + FILENAME_EXT);
        
        boolean toReturn = false;
        
        if (file != null) {
            toReturn = true;
        }
        return toReturn;
    }
}
