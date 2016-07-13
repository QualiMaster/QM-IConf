package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.emf.core.GMFEditingDomainFactory;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.util.StringInputStream;

import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.Highlighter;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.HighlighterParam;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter.PipelineDataflowInformationWrapper;
import de.uni_hildesheim.sse.qmApp.treeView.ElementStatusIndicator;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;
import pipeline.Pipeline;
import pipeline.PipelinePackage;
import pipeline.diagram.part.PipelineDiagramEditorUtil;
import pipeline.impl.DataManagementElementImpl;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.FlowImpl;
import pipeline.impl.SinkImpl;
import pipeline.impl.SourceImpl;
import pipeline.presentation.PipelineModelWizard;
import qualimasterapplication.Activator;

/**
 * Some helpful methods working with pipeline diagrams.
 * 
 * @author Holger Eichelberger
 * @author Cui Qin
 * @author Niko Nowatzki
 */
public class PipelineDiagramUtils {

    /**
     * The constraint separator used in the editor model: <code>{@value}</code>.
     */
    public static final String CONSTRAINT_SEPARATOR = ";";
    public static final IProgressMonitor NULL_MONITOR = new NullProgressMonitor();

    private static List<ConnectorWrapper> connectionsList = new ArrayList<ConnectorWrapper>();
    /**
     * Wraps up gmf connectors with their corresponding nodes.
     * @author nowatzki
     */
    public static class ConnectorWrapper {
        private String sourceNode;
        private String targetNode;
        private FlowImpl flow;
        
        /**
         * Constructs a wrapper instance for connections.
         * @param sourceNode connectios source node.
         * @param targetNode connections target node.
         * @param flow the connection itself.
         */
        public ConnectorWrapper(String sourceNode, String targetNode, FlowImpl flow) {
            this.sourceNode = sourceNode;
            this.targetNode = targetNode;
            this.flow = flow;
        }
        /**
         * Get the flow.
         * @return flow in this wrapper-object.
         */
        public FlowImpl getFlow() {
            return flow;
        }
        /**
         * Get source in this wrapper-object.
         * @return sourceNode source in this wrapper.
         */
        public String getSource() {
            return sourceNode;
        }
        /**
         * Get target in this wrapper-object.
         * @return targetNode target in this wrapper.
         */
        public String getTarget() {
            return targetNode;
        }
    }
    
    /**
     * Returns the diagram URI for a given decision variable.
     * 
     * @param variable
     *            the variable representing the pipeline
     * @return the URI or <b>null</b> if the location of the underlying IVML
     *         model cannot be found
     */
    public static URI getDiagramURI(IDecisionVariable variable) {
        return toURI(getDiagramFile(variable));
    }

    /**
     * Returns the model URI for a given decision variable.
     * 
     * @param variable
     *            the variable representing the pipeline
     * @return the URI or <b>null</b> if the location of the underlying IVML
     *         model cannot be found
     */
    public static URI getModelURI(IDecisionVariable variable) {
        return toURI(getModelFile(variable));
    }

    /**
     * Returns the diagram URI for a given decision variable.
     * 
     * @param variable
     *            the variable representing the pipeline
     * @return the URI or <b>null</b> if the location of the underlying IVML
     *         model cannot be found
     */
    public static File getDiagramFile(IDecisionVariable variable) {
        return obtainFile(variable, getDiagramExtension());
    }

    /**
     * Returns the model URI for a given decision variable.
     * 
     * @param variable
     *            the variable representing the pipeline
     * @return the URI or <b>null</b> if the location of the underlying IVML
     *         model cannot be found
     */
    public static File getModelFile(IDecisionVariable variable) {
        return obtainFile(variable, getModelExtension());
    }

    /**
     * Delete a workspace file via its Java file. Please note that using
     * {@link #deleteWorkspaceFile(URI)} is much better as the URI does not need
     * to be resolved from <code>file</code>.
     * 
     * @param file
     *            the file to delete
     */
    public static void deleteWorkspaceFile(File file) {
        deleteWorkspaceFile(toURI(file));
    }

    /**
     * Delete a workspace file via its URI.
     * 
     * @param fileUri
     *            the file URI to delete
     */
    public static void deleteWorkspaceFile(URI fileUri) {
        IFile[] files = obtainResources(fileUri);
        if (null != files) {
            for (int c = 0; c < files.length; c++) {
                try {
                    files[c].delete(true, NULL_MONITOR);
                } catch (CoreException e) {
                    Activator.getLogger(PipelineDiagramUtils.class)
                            .exception(e);
                    // ignore for now
                }
            }
        } else {
            // fallback
            File file = new File(fileUri.toFileString());
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * Returns the URI for a given decision variable and a given file extension.
     * 
     * @param variable
     *            the variable representing the pipeline
     * @param extension
     *            the file extension
     * @return the URI or <b>null</b> if the location of the underlying IVML
     *         model cannot be found
     */
    private static File obtainFile(IDecisionVariable variable, String extension) {
        File result = null;
        String modelName = variable.getDeclaration().getName();
        String fileName = modelName + "." + extension;
        Configuration cfg = variable.getConfiguration();
        ModelInfo<Project> info = VarModel.INSTANCE.availableModels()
                .getModelInfo(cfg.getProject());
        if (null == info) { // fallback: new variable
            info = VarModel.INSTANCE
                    .availableModels()
                    .getModelInfo(
                            de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration.PIPELINES
                                    .getConfiguration().getProject());
        }
        if (null != info) {
            File file = new File(info.getLocation());
            result = new File(file.getParentFile(), fileName);
        }
        return result;
    }

    /**
     * Turns a file into a URI.
     * 
     * @param file
     *            the file
     * @return the representing URI
     */
    public static URI toURI(File file) {
        // see
        // http://wiki.eclipse.org/index.php/EMF/FAQ#How_do_I_map_between_an_EMF_Resource_and_an_Eclipse_IFile.3F
        URI result;
        java.net.URI uri = file.toURI();
        IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
                .findFilesForLocationURI(uri);
        if (null != files && 1 == files.length) {
            result = URI.createPlatformResourceURI(files[0].getFullPath()
                    .toString(), true);
        } else {
            // external URI
            result = URI.createURI(uri.toString());
        }
        return result;
    }

    /**
     * Returns the file name extension for the model file.
     * 
     * @return the file name extension
     */
    public static String getModelExtension() {
        return PipelineModelWizard.FILE_EXTENSIONS.get(0);
    }

    /**
     * Returns the file name extension for the diagram file.
     * 
     * @return the file name extension
     */
    public static String getDiagramExtension() {
        // there does not seem to be a constant / accessor for this extension
        return PipelineModelWizard.FILE_EXTENSIONS.get(0) + "_diagram";
    }

    /**
     * Creates the model and the diagram file at the specified URI representing
     * the diagram. This method does not utilize a progress monitor.
     * 
     * @param diagramURI
     *            the URI to the diagram
     * @return the created resource or <b>null</b> in case of problems
     */
    public static Resource createModel(URI diagramURI) {
        return createModel(diagramURI, NULL_MONITOR);
    }

    /**
     * Creates an Eclipse file quietly.
     * 
     * @param fileURI
     *            the file URI
     */
    private static void createFileQuietly(URI fileURI) {
        IFile[] files = obtainResources(fileURI);
        if (null != files) {
            for (int c = 0; c < files.length; c++) {
                if (!files[c].exists()) {
                    try {
                        files[c].create(new StringInputStream(""), true,
                                NULL_MONITOR);
                    } catch (CoreException e) {
                        Activator.getLogger(PipelineDiagramUtils.class)
                                .exception(e);
                        // ignore for now
                    }
                }
            }
        }
    }

    /**
     * Obtains the file resources for <code>fileURI</code>.
     * 
     * @param fileURI
     *            the URI to obtain the file resources for
     * @return the workspace files (may be <b>null</b> or empty)
     */
    private static IFile[] obtainResources(URI fileURI) {
        IFile[] files = null;
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (null != workspace) {
            URI resolvedFile = CommonPlugin.resolve(fileURI);
            java.net.URI uri = null;
            if (null != resolvedFile) {
                try {
                    uri = new java.net.URI(resolvedFile.toString());
                } catch (URISyntaxException e) {
                    // is null otherwise
                }
            }
            if (null == uri) {
                try {
                    // don't know how to handle this at the moment
                    uri = new java.net.URI(fileURI.toString());
                } catch (URISyntaxException e) {
                    // is null otherwise
                }
            }
            if (null != uri) {
                files = workspace.getRoot().findFilesForLocationURI(uri);
            }
        }
        return files;
    }

    /**
     * Creates the model and the diagram file at the specified URI representing
     * the diagram.
     * 
     * @param diagramURI
     *            the URI to the diagram
     * @param progressMonitor
     *            to monitor the progress
     * @return the created resource or <b>null</b> in case of problems
     */
    public static Resource createModel(URI diagramURI,
            IProgressMonitor progressMonitor) {
        URI modelURI = diagramURI.trimFileExtension().appendFileExtension(
                getModelExtension());
        // create files if possible
        createFileQuietly(diagramURI);
        createFileQuietly(modelURI);

        Resource resource = PipelineDiagramEditorUtil.createDiagram(diagramURI,
                modelURI, progressMonitor);

        Resource pResource = getPipelineResource(diagramURI);
        String modelName = getModelName(diagramURI);
        try {
            pResource.load(null);
            Iterator<EObject> iterator = pResource.getContents().iterator();
            while (iterator.hasNext()) {
                Pipeline pipeline = (Pipeline) iterator.next();
                String tmp = pipeline.getName();
                if (null == tmp || 0 == tmp.length()) {
                    pipeline.setName(modelName);
                }
            }
            pResource.save(null);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return resource;
    }

    /**
     * Returns the (ECore) pipeline element associated with
     * <code>variable</code>.
     * 
     * @param variable
     *            the variable to return the pipeline for
     * @return the pipeline or <b>null</b> if no pipeline can be found
     */
    public static Pipeline getPipeline(IDecisionVariable variable) {
        // taken partly from the PipelineDiagramEditorUtil
        Pipeline result = null;
        URI modelURI = getModelURI(variable);
        TransactionalEditingDomain editingDomain = GMFEditingDomainFactory.INSTANCE
                .createEditingDomain();
        Resource modelResource = editingDomain.getResourceSet().createResource(
                modelURI);
        List<EObject> contents = modelResource.getContents();
        if (null != contents && !contents.isEmpty()) {
            result = (Pipeline) contents.get(0);
        }
        return result;
    }

    /**
     * Obtains the pipeline resource.
     * 
     * @param uri
     *            the <code>URI</code> to load the pipeline diagram
     * @return the resource, may not be loaded
     */
    private static Resource getPipelineResource(URI uri) {
        // create resource set
        ResourceSet resourceSet = new ResourceSetImpl();

        // register the resource factory and the postfix is "pipeline"
        Resource.Factory.Registry registry = resourceSet
                .getResourceFactoryRegistry();
        registry.getExtensionToFactoryMap().put("pipeline",
                new EcoreResourceFactoryImpl());

        // register the namespace of the pipeline package
        EPackage.Registry reg = resourceSet.getPackageRegistry();
        reg.put(PipelinePackage.eNS_URI, PipelinePackage.eINSTANCE);

        // get resource from uri
        String modelURIPath = getModelURIPath(uri);
        URI fileURI = URI.createFileURI(modelURIPath);
        return resourceSet.getResource(fileURI, true);
    }

    /**
     * Returns the (Ecore) pipeline elements from <code>uri</code>.
     * 
     * @param uri
     *            the <code>URI</code> to load the pipeline diagram
     * @return the pipeline or <b>null</b> if no pipeline can be found
     */
    public static List<Pipeline> getPipeline(URI uri) {
        /*
         * // create resource set ResourceSet resourceSet = new
         * ResourceSetImpl();
         * 
         * // register the resource factory and the postfix is "pipeline"
         * Resource.Factory.Registry registry =
         * resourceSet.getResourceFactoryRegistry();
         * registry.getExtensionToFactoryMap().put("pipeline", new
         * EcoreResourceFactoryImpl());
         * 
         * // register the namespace of the pipeline package EPackage.Registry
         * reg = resourceSet.getPackageRegistry();
         * reg.put(PipelinePackage.eNS_URI, PipelinePackage.eINSTANCE);
         * 
         * // get resource from uri String modelURIPath = getModelURIPath(uri);
         * URI fileURI = URI.createFileURI(modelURIPath); Resource resource =
         * resourceSet.getResource(fileURI, true);
         */
        Resource resource = getPipelineResource(uri);
        List<Pipeline> pipelineList = new ArrayList<Pipeline>();
        try {
            resource.load(null);
            Iterator<EObject> iterator = resource.getContents().iterator();
            while (iterator.hasNext()) {
                Pipeline pipeline = (Pipeline) iterator.next();
                pipelineList.add(pipeline);
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return pipelineList;
    }

    /**
     * Returns the folder name of the diagram from a given
     * <code>diagramURI</code>.
     * 
     * @param diagramURI
     *            the Eclipse URI to be parsed
     * @return the folder name of the diagram
     */
    public static String getFolderDirectory(URI diagramURI) {
        String uriPath = getNetURIPath(diagramURI);
        int endIndex = uriPath.lastIndexOf("/");
        String folderName = uriPath.substring(0, endIndex + 1);
        return folderName;
    }

    /**
     * Returns the model path from the <code>diagramURI</code>.
     * 
     * @param diagramURI
     *            the URI to be parsed
     * @return the model path from the model URI
     */
    public static String getModelURIPath(URI diagramURI) {
        String uriPath = getNetURIPath(diagramURI);
        int endIndex = uriPath.lastIndexOf(".");
        String modelExtension = PipelineDiagramUtils.getModelExtension();
        String modelURIPath = uriPath.substring(0, endIndex) + "."
                + modelExtension;
        return modelURIPath;
    }

    /**
     * Returns the diagram path from the <code>diagramURI</code>.
     * 
     * @param diagramURI
     *            the URI to be parsed
     * @return the diagram path from the diagram URI
     */
    public static String getNetURIPath(URI diagramURI) {
        String netURIPath = null;
        try {
            netURIPath = ModelUtility.toNetUri(diagramURI).getPath();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return netURIPath;
    }

    /**
     * Returns the model name from the <code>diagramURI</code>.
     * 
     * @param diagramURI
     *            the URI to be parsed
     * @return the model name from the diagram URI
     */
    public static String getModelName(URI diagramURI) {
        String netURIPath = getNetURIPath(diagramURI);
        int start = netURIPath.lastIndexOf("/") + 1;
        int end = netURIPath.lastIndexOf(".");
        String modelName = netURIPath.substring(start, end);
        return modelName;
    }

    /**
     * Get diagram(Pipeline-Editor) from opened editor and highlight specific
     * elements.
     * 
     */
    public static void highlightDiagram() {

        resetDiagramMarkings();
    
        // Get diagram.
        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        
        List<Reasoning.PipelineWrapperObject> errors = Reasoning
                .getPipelineErrors();

        for (int i = 0; i < errors.size(); i++) {
            String pipelineName = errors.get(i).getPipelineName();

            if (diagram.getPartName().trim().equals(pipelineName.trim())) {

                EObject element = diagram.getDiagram().getElement();
                EList<EObject> eContents = element.eContents();

                // highlightAdapter for highlighting diagram-elements.
                Highlighter adapter = new Highlighter(diagram);
                HighlighterParam param = new HighlighterParam();

                for (int j = 0; j < eContents.size(); j++) {

                    if (eContents.get(j) instanceof FlowImpl) {
                        
                        if (EMFCoreUtil.getName(eContents.get(j)).equals(
                                errors.get(i).getVariableName().trim())) {
                            adapter.highlightFlow(eContents.get(j), param,
                                    errors.get(i).getConflictMessage());
                        }
                    } else {
                        String ecoreName = EMFCoreUtil
                                .getName(eContents.get(j));
                        String variableName = errors.get(i).getVariableName()
                                .trim();
                        if (ecoreName.equals(variableName)) {
                            adapter.highlight(eContents.get(j), param,
                                    errors.get(i).getConflictMessage());
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Reset the marking of an opened Pipeline-Editor by changingthe color of all
     * contained {@link EObject} to black.
     */
    public static void resetDiagramMarkings() {
    
        // Get diagram.
        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        // highlightAdapter for highlighting diagram-elements.
        Highlighter adapter = new Highlighter(diagram);
        HighlighterParam param = new HighlighterParam();
        
        EObject element = diagram.getDiagram().getElement();
        EList<EObject> eContents = element.eContents();
        
        for (int j = 0; j < eContents.size(); j++) {
        
            if (eContents.get(j) instanceof FlowImpl) {
                adapter.resetFlow(eContents.get(j), param);
                adapter.removeTooltip(eContents.get(j));
            } else {
                adapter.resetNode(eContents.get(j), param);
                adapter.removeTooltip(eContents.get(j));
            }
        }
    }
    

    /**
     * Highlight a given Pipeline-Editor concerning its dataflow characteristics.
     * The nodes will be coloured in different green tones.
     * 
     * @param eobject object to highlight.
     * @param indicator Indicator which indicates the elements status.
     */
    public static void highlightDataFlow(EObject eobject, ElementStatusIndicator indicator) {

        if (eobject instanceof SourceImpl) {
            SourceImpl source = (SourceImpl) eobject;
            StatusHighlighter.INSTANCE.highlightDataFlowForSource(source, indicator);
        }
        if (eobject instanceof FamilyElementImpl) {
            FamilyElementImpl source = (FamilyElementImpl) eobject;
            StatusHighlighter.INSTANCE.highlightDataFlowForFamily(source, indicator);
        }
        if (eobject instanceof SinkImpl) {
            SinkImpl source = (SinkImpl) eobject;
            StatusHighlighter.INSTANCE.highlightDataFlowForSink(source, indicator);
        }
        if (eobject instanceof DataManagementElementImpl) {
            DataManagementElementImpl source = (DataManagementElementImpl) eobject;
            StatusHighlighter.INSTANCE.highlightDataFlowForDatamangement(source, indicator);
        }
    }

    /**
     * Add color to pipeline.
     */
    public static void addPipelineColor() {
        
        // Get diagram.
        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        String pipelineName = diagram.getTitle().toLowerCase();
        
        List<de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter.PipelineDataflowInformationWrapper>
            wrapperList = StatusHighlighter.INSTANCE.getPipelineFlowInfo();

        EObject element = diagram.getDiagram().getElement();
        EList<EObject> eContents = element.eContents();
        
        for (int i = 0; i < wrapperList.size(); i++) {
            
            PipelineDataflowInformationWrapper wrapper = wrapperList.get(i);

            if (wrapper.getPipelineName().toLowerCase().equals(pipelineName)) {

                for (int j = 0; j < eContents.size(); j++) {
                        
                    String name = eContents.get(j).toString();
                    name = determineName(name);
                   
                    if (wrapper.getVariableName().equals(name)) {
                        highlightDataFlow(eContents.get(j), wrapper.getIndicator());
                    }    
                }
            }
        }
    }

    /**
     * determine the name of a pipeline.
     * @param name Given String.
     * @return Found name within the given String.
     */
    private static String determineName(String name) {
        name = name.substring(name.indexOf(":"), name.indexOf(","));
        name = name.replaceAll("[^a-zA-Z0-9]", "");
        name.replace(":", "");
        name = name.trim();
        
        return name;
    }
    /**
     * Save the information about the currently open PipelineEditor, aka which nodes 
     * belong to which flow.
     */
    public static void saveConnections() {
        
        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
        
        if (null != diagram && null != diagram.getDiagram()) { // in shutdown
            EObject element = diagram.getDiagram().getElement();
            EList<EObject> eContents = element.eContents();
            
            for (int j = 0; j < eContents.size(); j++) {
                
                if (eContents.get(j) instanceof FlowImpl) {
                    
                    FlowImpl flow = (FlowImpl) eContents.get(j);
                            
                    String source = flow.getSource().getName();
                    String target = flow.getDestination().getName();
                    
                    ConnectorWrapper wrapper = new ConnectorWrapper(source, target, flow);
                    connectionsList.add(wrapper);
                }
            }
        }
    }

    /**
     * Clear the information about the connections and their respective nodes in the former
     * Pipelineeditor.
     */
    public static void deleteConnectionsInfo() {
        connectionsList.clear();
    }
    
    /**
     * Get the info about current connections and thei nodes.
     * @return connectionsList List containing info about nodes and connectios.
     */
    public static List<ConnectorWrapper> getConnectionInfoList() {
        return connectionsList;
    }
}
