package de.uni_hildesheim.sse.qmApp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.editors.VariableEditor;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElementsView;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.messages.Status;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.Bundle;
import net.ssehub.easy.reasoning.core.frontend.ReasonerFrontend;
import net.ssehub.easy.reasoning.core.reasoner.Message;
import net.ssehub.easy.reasoning.core.reasoner.ReasonerConfiguration;
import net.ssehub.easy.reasoning.core.reasoner.ReasoningResult;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IConfigurationElement;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.ModelElement;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.persistency.StringProvider;
import pipeline.diagram.part.PipelineDiagramEditor;

/**
 * Implements the integration of reasoning with the configuration application.
 * Handles error messages and turns them into editors (TBD).
 * 
 * @author Holger Eichelberger
 */
public class Reasoning {
    
    public static final boolean ENABLED = true;
    
    public static final ReasonerConfiguration CONFIGURATION;
    
    private static final String MARKER_SOURCE_ID = "QM.reasoning";
       
    private static HashMap<IDecisionVariable, String> errors = new HashMap<IDecisionVariable, String>();
    private static List<PipelineWrapperObject> pipelineWrapperList = new ArrayList<PipelineWrapperObject>();
    private static Set<String> configurableElementsViewMapping = new TreeSet<String>();
    private static Set<String> configurableElementsViewMappingForPipelines = new TreeSet<String>();
    
    static {
        CONFIGURATION = new ReasonerConfiguration();
        CONFIGURATION.enableCustomMessages();
        CONFIGURATION.setFreshConfiguration(false);
    }
    
    /**
     * Prevents external instantiation.
     */
    private Reasoning() {
    }
    
    /**
     * Wrapper-class which binds the name of pipeline, a failing variable within this pipeline together with
     * a corresponding errormessage. Pipeline - Variable - ErrorMessage.
     * 
     * @author Niko
     */
    public class PipelineWrapperObject {
        private String pipelineName;
        private String variableName;
        private String conflictMessage;
        
        /**
         * Constructor.
         * @param pipelineName name of the pipeline.
         * @param variableName variable name.
         * @param conflictMessage conflict message for displaying.
         */
        public PipelineWrapperObject(String pipelineName, String variableName, String conflictMessage) {
            this.pipelineName = pipelineName;
            this.variableName = variableName;
            this.conflictMessage = conflictMessage;
        }
        /**
         * Get pipelines name.
         * @return pipelines name.
         */
        public String getPipelineName() {
            return pipelineName;
        }
        /**
         * Get variables name.
         * @return name of the variable.
         */
        public String getVariableName() {
            return variableName;
        }
        /**
         * Get the conflict-message.
         * @return conflict-message for displaying.
         */
        public String getConflictMessage() {
            return conflictMessage;
        }
    } 
        
    
    /**
     * Checks and propagates if required.
     * 
     * @param modelPart the model part to work on
     * @param showSuccessDialog whether a dialog shall be shown in case of success
     * @return <code>true</code> if successful, <code>false</code> else
     */
    public static boolean reasonOn(IModelPart modelPart, boolean showSuccessDialog) {
        boolean success;
        resetReasoningMarkers(); // Clear all markers and set only relevant markers afterwards
        deleteAllReasoningMarkers();
        clearConfigurableElementsMarkings();  
        errors.clear();
        CONFIGURATION.enableCustomMessages();
        Configuration cfg = modelPart.getConfiguration();
        success = reasonOn(true, showSuccessDialog, cfg);
        updateEditors();
        return success;
    }

    /**
     * Sub-part of {@link #reasonOn(IModelPart, boolean)}: Allows to check and propagate without the specification of a
     * model part, e.g., after creation of a copy.
     * @param runsInUI <tt>true</tt> errors will be propagated to editors (must be called from a UI thread),
     *     <tt>false</tt> otherwise.
     * @param showSuccessDialog  whether a dialog shall be shown in case of success.
     * @param cfg The configuration to check (and propagate).
     * @return <code>true</code> if successful, <code>false</code> else
     */
    private static boolean reasonOn(boolean runsInUI, boolean showSuccessDialog, Configuration cfg) {
        boolean success;
        Project project = cfg.getProject();
        ReasoningResult result = ReasonerFrontend.getInstance().check(project, cfg, CONFIGURATION, 
            ProgressObserver.NO_OBSERVER); // currently same as propagate, ... 
        if (result.hasConflict()) {
            IResource resource = ResourcesPlugin.getWorkspace().getRoot();
            StringBuilder message = new StringBuilder();
            CoreException markerException = null;
            for (int m = 0; m < result.getMessageCount(); m++) {
                Message msg = result.getMessage(m); //Save reasoner-results in Hashmap
                if (runsInUI) {
                    saveErrors(msg);
                }
                try {
                    List<String> labels = msg.getConflictLabels();
                    List<ModelElement> conflicts = msg.getConflicts();
                    if (null != labels && labels.size() > 0) {
                        for (String label: labels) {
                            createMarker(resource, msg.getStatus(), label);
                        }
                    } else if (null != conflicts && conflicts.size() > 0) {
                        for (ModelElement elt: conflicts) {
                            String text = ModelAccess.getDescription(elt);
                            if (null == ModelAccess.getDescription(elt)) {
                                text = StringProvider.toIvmlString(elt);
                            }
                            createMarker(resource, msg.getStatus(), msg.getDescription() + ":" + text);
                        }
                    } else {
                        createMarker(resource, msg.getStatus(), makeReadable(msg, cfg));
                    }
                } catch (CoreException e) {
                    markerException = e;
                }
                if (message.length() > 0) {
                    message.append("\n- ");
                }
                String text = makeReadable(result.getMessage(m), cfg);
                if (text.length() > 0) {
                    message.append(text);
                }
                try {
                    if (runsInUI) {
                    // just to be sure that the problem view opens
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(
                            org.eclipse.ui.IPageLayout.ID_PROBLEM_VIEW).setFocus();
                    }
                } catch (PartInitException e) {
                    EASyLoggerFactory.INSTANCE.getLogger(Reasoning.class, Bundle.ID).exception(e);    
                }
            }
            if (runsInUI) {
                if (null != markerException) {
                    EASyLoggerFactory.INSTANCE.getLogger(Reasoning.class, Bundle.ID).exception(markerException);
                    Dialogs.showErrorDialog("Validation problems", message.toString());
                } else {
                    Dialogs.showErrorDialog("Validation problems", "Please consult the 'Problems View'");
                }
            }
            success = false;
        } else {
            if (showSuccessDialog) {
                Dialogs.showInfoDialog("Validation successful", "Model is valid.");
            }
            success = true;
        }
        return success;
    }
    
    /**
     * Sub-part of {@link #reasonOn(IModelPart, boolean)}: Allows to check and propagate without the specification of a
     * model part, e.g., after creation of a copy.
     * @param showSuccessDialog  whether a dialog shall be shown in case of success
     * @param cfg The configuration to check (and propagate).
     * @return <code>true</code> if successful, <code>false</code> else
     */
    public static boolean reasonOn(boolean showSuccessDialog, Configuration cfg) {
        return reasonOn(false, showSuccessDialog, cfg);
    }

    /**
     * Reset marking in tree and possibly opened Pipeline-Editor.
     */
    private static void resetReasoningMarkers() {
        ConfigurableElementsView.revertConfigurableElementsViewMarking();
        resetOpenedPipelineEditorMarkings();
        pipelineWrapperList.clear();
    }
    
    /**
     * Mark the flawed pipeline-elements in the currently opened Pipeline-Diagram.
     */
    private static void resetOpenedPipelineEditorMarkings() {
        
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorReference[] allOpenEditors = page.getEditorReferences();
        for (int i = 0; i < allOpenEditors.length; i++) {
            IEditorPart editor = allOpenEditors[i].getEditor(false);
            
            if (editor instanceof PipelineDiagramEditor) {
                PipelineDiagramUtils.resetDiagramMarkings();
            }
        }
    }

    
    /**
     * Part of the {@link #reasonOn(IModelPart, boolean)} method: Forces all open {@link VariableEditor}s to update.
     */
    private static void updateEditors() {
        // Annotate opened Pipeline-Editor
        annotateOpenedPipelineEditor();

        // Update VariableEditors
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorReference[] allOpenEditors = page.getEditorReferences();
        for (int i = 0; i < allOpenEditors.length; i++) {
            IEditorPart editor = allOpenEditors[i].getEditor(false);
            if (editor instanceof VariableEditor) {
                VariableEditor varEditor = (VariableEditor) editor;
                informEditor(varEditor);
            }
        }
    }
    
    /**
     * Forces the given {@link VariableEditor} to refresh.
     * This is necessary for updating the reasoning marker.
     * @param editor An open {@link VariableEditor}, which shall be updated. Must not be <tt>null</tt>.
     */
    private static void informEditor(final VariableEditor editor) {
        Display.getDefault().syncExec(new Runnable() {
            
            @Override
            public void run() {
                editor.refreshEditor();
            }
        });
    }
    
    /**
     * Clears the marking of TreeItems in ConfigurableElementsView.
     */
    private static void clearConfigurableElementsMarkings() {
        configurableElementsViewMapping.clear();
        configurableElementsViewMappingForPipelines.clear();
    }
    
    /**
     * Save Errors in list, thus errors can be displayed.
     * @param msg {@link Message} containing conflicting variables and errormessages.
     */
    private static void saveErrors(Message msg) {
        //clearConfigurableElementsMarkings();
    
        for (int i = 0; i < msg.getProblemVariables().size(); i++)  {
            for (int j = 0; j < msg.getProblemVariables().get(i).size(); j++) {
                Set<IDecisionVariable> setdecisionVariable = msg.getProblemVariables().get(i);
               
                IDecisionVariable variable = (IDecisionVariable) setdecisionVariable.toArray()[j];
                ChangeManager.INSTANCE.variableChanged(msg, variable);
            
                String errorMessage = msg.getProblemVariables().get(i).toString();
                String conflictMessage = msg.getConflictComments().get(i).toString();
                errors.put(variable, errorMessage);
                
                //Get pipeline configuration
                Configuration configuration = de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration.
                        PIPELINES.getConfiguration();
                
                //if variable == pipelineNode
                if (Compound.TYPE.isAssignableFrom(variable.getDeclaration().getType())) {
   
                    AbstractVariable declaration = variable.getDeclaration();
                    Project project = (Project) declaration.getTopLevelParent();

                    if (project != null) {
                        for (int k = 0; k < project.getElementCount(); k++) {
                            //It can be a node or Pipeline - instance. In any case its a DecisionVariableDeclaration
                            if (project.getElement(k) instanceof DecisionVariableDeclaration) {
                                DecisionVariableDeclaration pipelineDeclaration = (DecisionVariableDeclaration)
                                        project.getElement(k);
                                //Get the actual variable which holds the pipelines name
                                IDecisionVariable pipelineVariable = configuration.getDecision(pipelineDeclaration);
                                savePipelineErrors(pipelineVariable, variable, conflictMessage);
                            }
                        }
                    }  
                } else {
                    if (variable != null) {
                        IConfigurationElement confElement = variable.getParent();
                        
                        if (confElement instanceof CompoundVariable) {
                            CompoundVariable failedVariable = (CompoundVariable) confElement;
                            String name = failedVariable.getNestedVariable("name").toString();
                            
                            //Extract the actual name.
                            int startPos = name.indexOf("=");
                            int endPos = name.indexOf(":");
                            if (-1 != startPos && -1 != endPos) {
                                name = name.substring(startPos, endPos);
                            }
                            name = name.replaceAll("[^a-zA-Z0-9]", "");
                            name = name.trim();
                            
                            if (name != null && !"".equals(name)) {
                                configurableElementsViewMapping.add(name);
                            }
                        }
                    }
                }
            }
        }
        
        ConfigurableElementsView.saveReasosiningInfoInTreeElements(configurableElementsViewMapping);
        ConfigurableElementsView.saveReasosiningInfoInTreeElementsForPipelines(
                configurableElementsViewMappingForPipelines);
        
    }
    
    /**
     * Mark the flawed pipeline-elements in the currently opened Pipeline-Diagram.
     */
    private static void annotateOpenedPipelineEditor() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                instanceof DiagramEditor) {
        
            
            IEditorReference[] editors = PlatformUI.getWorkbench()
                    .getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                    
            for (int i = 0; i < editors.length; i++) {
                Object object = editors[i].getEditor(false);
                if (object instanceof DiagramEditor) {
                    DiagramEditor editor = (DiagramEditor) object;

                    if (editor instanceof PipelineDiagramEditor) {
                        PipelineDiagramUtils.highlightDiagram();
                    }
                }
            }
        }
    }
    
    /**
     * Save the pipeline-errors.
     * @param pipelineVariable name of the pipleine.
     * @param variable variable itself.
     * @param conflictMessage conflict message.
     */
    private static void savePipelineErrors(IDecisionVariable pipelineVariable, IDecisionVariable variable,
            String conflictMessage) {

        String pipelineName = "";
        
        if (pipelineVariable != null) { 
            for (int k = 0; k < pipelineVariable.getNestedElementsCount(); k++) {

                IDecisionVariable innerVariable = pipelineVariable.getNestedElement(k);
                
                if (innerVariable.toString().contains("name")) {
                    String name = pipelineVariable.getNestedElement(k).getValue().toString();

                    //Prepare name
                    int endPos = name.indexOf(":");
                    if (-1 != endPos) {
                        pipelineName = name.substring(0, endPos).trim();
                    } else {
                        pipelineName = name;
                    }
                }
            }
        }
                                            
        //Now determine the actual variables name which can be compared
        //with the diagram elements.
        for (int k = 0; k < variable.getNestedElementsCount(); k++) {
          
            if (variable.getNestedElement(k).toString().startsWith("name")) {
                String name = variable.getNestedElement(k).toString();
                
                //Extract the actual name.
                int startPos = name.indexOf("=");
                int endPos = name.indexOf(":");
                if (-1 != startPos && -1 != endPos) {
                    name = name.substring(startPos, endPos);
                }
                name = name.replaceAll("[^a-zA-Z0-9]", "");
                name = name.trim();
                
                PipelineWrapperObject wrapper = new Reasoning().
                        new PipelineWrapperObject(pipelineName, name, conflictMessage);
                pipelineWrapperList.add(wrapper);
                
                if (name != null && !"".equals(name)) {
                    configurableElementsViewMappingForPipelines.add(pipelineName);
                }
            }
        }
    }

    /**
     * Get errors which Reasoner has produces.
     * @return errors Errors {@link HashMap} containing errors and corresponding error-messages.
     */
    public static HashMap<IDecisionVariable, String> getErrors() {
        return errors;
    }
    
    /**
     * Get the {@link PipelineWrapperObject}s which contain information about failing pipeline-elements.
     * @return wrapperList List of {@link PipelineWrapperObject}s.
     */
    public static List<PipelineWrapperObject> getPipelineErrors() {
        return pipelineWrapperList;
    }
    /**
     * Creates a marker.
     * 
     * @param resource the resource to create the marker for
     * @param status the status of the message
     * @param text the message text
     * @throws CoreException in case that creating the marker failed
     */
    private static void createMarker(IResource resource, Status status, String text) throws CoreException {
        IMarker marker = resource.createMarker(IMarker.PROBLEM);
        Object severity;
        switch (status) {
        case ERROR:
            severity = IMarker.SEVERITY_ERROR;
            break;
        case INFO:
            severity = IMarker.SEVERITY_INFO;
            break;
        case UNSUPPORTED:
            severity = IMarker.SEVERITY_ERROR;
            break;
        case WARNING:
            severity = IMarker.SEVERITY_WARNING;
            break;
        default:
            severity = IMarker.SEVERITY_WARNING;
            break;
        }
        marker.setAttribute(IMarker.SEVERITY, severity);
        marker.setAttribute(IMarker.MESSAGE, text);
        marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
        marker.setAttribute(IMarker.SOURCE_ID, MARKER_SOURCE_ID);
    }
    
    /**
     * Turns a message into something more or less readable (preliminary, to be revised).
     * 
     * @param msg the message to be turned into a user-readable description
     * @param cfg the configuration reasoning was applied to
     * @return the description (may be empty if showing the message to the user is not required)
     */
    private static String makeReadable(Message msg, Configuration cfg) {
        String result = "";
        switch (msg.getStatus()) {
        case ERROR:
            result += "Error: ";
            result += considerComments(msg, cfg);
            break;
        case UNSUPPORTED:
            result += "Unsupported: ";
            result += msg.getDescription();
            break;
        case WARNING:
            result += "Warning: ";
            result += considerComments(msg, cfg);
            break;
        case INFO:
            // ignore
            break;
        default:
            // ignore
            break;
        }
        return result;
    }
    
    /**
     * Returns the comment of a model element.
     * 
     * @param elt the element to return the comment for
     * @return the comment or <b>null</b> if there was no explicit comment
     */
    private static String getComment(IModelElement elt) {
        String result = ModelAccess.getDescription(elt);
        if (0 == result.length()) {
            result = null;
        }
        return result;
    }
    
    /**
     * Consider model comments or the original message description.
     * 
     * @param msg the message
     * @param cfg the configuration reasoning was applied to
     * @return the textual description of the message
     */
    private static String considerComments(Message msg, Configuration cfg) {
        String result = "";
        for (ModelElement elt : msg.getConflicts()) {
            String comment = null;
            if (elt instanceof AbstractVariable) {
                AbstractVariable var = (AbstractVariable) elt;
                if (ConstraintType.TYPE.isAssignableFrom(var.getType())) {
                    comment = getComment(var);
                }
            } // ignore constraint - no message given
            if (null != comment) {
                if (result.length() > 0) {
                    result += ", ";
                }
                result += comment;
            }
        }
        if (0 == result.length()) {
            result = msg.getDescription();
        }
        return result;
    }
    
    /**
     * Delete all reasoning markers.
     */
    private static void deleteAllReasoningMarkers() {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        try {
            int depth = IResource.DEPTH_INFINITE;
            IMarker[] problems = resource.findMarkers(IMarker.PROBLEM, true, depth);

            if (null != problems) {
                for (int i = 0; i < problems.length; i++) {
                    //IMarker marker = problems[i];
                    //if (MARKER_SOURCE_ID.equals(marker.getAttribute(IMarker.SOURCE_ID))) {
                    problems[i].delete();
                    //}
                }
            }
        } catch (CoreException e) {
            EASyLoggerFactory.INSTANCE.getLogger(Reasoning.class, Bundle.ID).exception(e);
        }    
    }

}