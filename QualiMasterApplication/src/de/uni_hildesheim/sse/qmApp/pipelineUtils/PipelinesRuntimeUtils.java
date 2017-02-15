package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import de.uni_hildesheim.sse.qmApp.WorkspaceUtils;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.internal.PipelineContentsContainer;
import eu.qualimaster.easy.extension.internal.PipelineVisitor;
import eu.qualimaster.easy.extension.internal.Utils;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
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
    
    private ArrayList<String> pipelines = new ArrayList<String>();
    private List<PipelineGraphColoringWrapper> pipelinesToDisplayInTable
            = new ArrayList<PipelineGraphColoringWrapper>();

    /**
     * // Exists only to avoid instantiation.
     */
    private PipelinesRuntimeUtils() {
    }
    
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
