package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.gmf.runtime.lite.svg.SVGFigure;
import org.eclipse.gmf.runtime.notation.impl.ShapeImpl;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElement;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElementsView;
import de.uni_hildesheim.sse.qmApp.treeView.ElementStatusIndicator;
import pipeline.diagram.part.PipelineDiagramEditor;
import pipeline.impl.DataManagementElementImpl;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.SinkImpl;
import pipeline.impl.SourceImpl;

/**
 * TODO SE: There are a lot of possible performance improvements:
 * - To often toLowerCase is used (if possible, the element should only be turned only once into lower case and not
 *   in each iteration of the loop)
 * - For-Loops can be exited if the desired value is found
 * - In total, too many loops are used -> consider maps
 * Not an performance issue, but maintenance: Many duplicate code fragments -> consider to revise them.
 */
/**
 * Singleton-Class for highlighting Pipeline-Editors. <b>Assumes that the caller is in the correct UI-Thread!</b>
 * @author nowatzki
 */
public class StatusHighlighter {

    private static final Object LOCK = new Object();
    private static volatile StatusHighlighter instance;
    
    private List<PipelineDataflowInformationWrapper> pipelineDataflowList = 
            new ArrayList<PipelineDataflowInformationWrapper>();
    
    /**
     * // Exists only to avoid instantiation.
     */
    private StatusHighlighter() {
    }
    
    /**
     * Constructs an instance of PipelineStatusHighlighter.
     * @return instance singelton instance.
     */
    public static StatusHighlighter getInstance() {
        StatusHighlighter r = instance;
        if (r == null) {
            synchronized (LOCK) {    // While we were waiting for the lock, another 
                r = instance;        // thread may have instantiated the object.
                if (r == null) {  
                    r = new StatusHighlighter();
                    instance = r;
                }
            }
        }
        return r;
    }
    
    /**
     * Wraps up info about Pipeline-Elements and the status of them.
     * @author nowatzki
     *
     */
    public static class PipelineDataflowInformationWrapper {
        private String pipelineName;
        private String variableName;
        private ElementStatusIndicator indicator;
        
        /**
         * Contructs a Informationwrapper for Pipelineelements.
         * @param pipelineName name of the pipeline
         * @param variableName name of the variable.
         * @param indicator Indicator which indicated the situation of an element.
         */
        public PipelineDataflowInformationWrapper(String pipelineName, String variableName,
                ElementStatusIndicator indicator) {
            this.pipelineName = pipelineName;
            this.variableName = variableName;
            this.indicator = indicator;
        }
        
        /**
         * Get the pipelineName.
         * @return pipelineName
         */
        public String getPipelineName() {
            return pipelineName;
        }
        /**
         * Get the variableName.
         * @return variableName.
         */
        public String getVariableName() {
            return variableName;
        }
        /**
         * Get the indicator.
         * @return indicator The indicator for the element.
         */
        public ElementStatusIndicator getIndicator() {
            return indicator;
        }
    }
    
    /**
     * mark a specific variable within a pipeline given by the corresponding indicator.
     * @param pipelineName name of the pipeline.
     * @param variableName name of the variable.
     * @param indicator indicator which indicates the situation of the element.
     */
    public void markPipeline(String pipelineName, String variableName, ElementStatusIndicator indicator) {
        
        PipelineDataflowInformationWrapper wrapper = new PipelineDataflowInformationWrapper(
                pipelineName, variableName, indicator);
        
        //If no variablename is given, then mark the corresponding pipeline in the treeview
        if (variableName == null || variableName.length() < 1) {    
            
            markPipelineStatus(pipelineName, indicator);
            
        }
        
        //Go through all saved information
        for (int i = 0; i < pipelineDataflowList.size(); i++) {
            
            PipelineDataflowInformationWrapper existingWrapper = pipelineDataflowList.get(i);
            
            // if combination of pipeline and variable is found, change indicator of existing wrapper!
            if (existingWrapper.pipelineName.equals(wrapper.pipelineName)
                    && existingWrapper.variableName.equals(wrapper.variableName)) {
                
                existingWrapper.indicator = indicator;
                
            } else {
                //Otherwise create a new information-wrapper in order to highlight a pipeline-element.
                PipelineDataflowInformationWrapper newWrapper = 
                        new PipelineDataflowInformationWrapper(pipelineName, variableName, indicator);
                pipelineDataflowList.add(newWrapper);
            }
            
            annotateOpenedPipelineEditor();
        }
        
        if (pipelineDataflowList.size() == 0) {
            PipelineDataflowInformationWrapper newWrapper = 
                    new PipelineDataflowInformationWrapper(pipelineName, variableName, indicator);
            pipelineDataflowList.add(newWrapper);
            
            annotateOpenedPipelineEditor();
        }
    }

    /**
     * Mark an pipeline-element.
     * @param pipelineName name of the pipeline.
     * @param indicator indicator which indicates the pipelines status.
     */
    public void markPipelineStatus(String pipelineName, ElementStatusIndicator indicator) {
        
        ConfigurableElement[] elements = ConfigurableElementsView.getElements();
        for (int i = 0; i < elements.length; i++) {
            ConfigurableElement topLevel = elements[i];
            
            if (topLevel.getDisplayName().equals("Pipelines")) { // TODO more portable to search for the part type
                markPipelineElement(topLevel, pipelineName, indicator);
            }

        }
        
    }
    
    /**
     * Go through pipelineElements and mark the right one.
     * @param topLevel toplevel Treelement for pipelines.
     * @param pipelineName name of the pipeline.
     * @param indicator indicator which indicates the status of the pipeline.
     */
    private void markPipelineElement(ConfigurableElement topLevel, String pipelineName,
            ElementStatusIndicator indicator) {
        
        for (int i = 0; i < topLevel.getChildCount(); i++) {
            
            ConfigurableElement element = topLevel.getChild(i);
            
            if (element.getDisplayName().toLowerCase().equals(pipelineName.toLowerCase())) {
                
                element.setStatus(indicator);
                ConfigurableElementsView.forceTreeRefresh(element);
                break;
            }
        } 
    }
    
    /**
     * Set the status of a given ConfigurableElement.
     * @param elementName name of the element.
     * @param status status of the element.
     */
    public void markConfigurableElementsStatus(String elementName, ElementStatusIndicator status) {
        ConfigurableElement[] elements = ConfigurableElementsView.getElements();
        
        for (int i = 0; i < elements.length; i++) {
            ConfigurableElement element = elements[i];
            
            if (element.getDisplayName().toLowerCase().equals(elementName.toLowerCase())) {
                element.setStatus(status);
                ConfigurableElementsView.forceTreeRefresh(element);
            }
            
            for (int j = 0; j < element.getChildCount(); j++) {
                ConfigurableElement child = element.getChild(j);
                markConfigurableElement(child, elementName, status);
            }
            
        }
    }
    
    /**
     * Set the status of a given ConfigurableElement.
     * @param element current element in tree.
     * @param elementName name of the searched element.
     * @param status status to set.
     */
    public void markConfigurableElement(ConfigurableElement element, String elementName,
            ElementStatusIndicator status) {
        
        if (element.getDisplayName().toLowerCase().equals(elementName.toLowerCase())) {
            
            element.setStatus(status);
            ConfigurableElementsView.forceTreeRefresh(element);
        } else {
            if (element.hasChildren()) {
                for (int j = 0; j < element.getChildCount(); j++) {
                    markConfigurableElement(element.getChild(j), elementName, status);
                }
            }
        }
    }
    
    /**
     * Mark the flawed pipeline-elements in the currently opened Pipeline-Diagram.
     */
    private static void annotateOpenedPipelineEditor() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                instanceof DiagramEditor) {
        
            if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                    instanceof DiagramEditor) {
            
                IEditorReference[] editors = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                        
                for (int i = 0; i < editors.length; i++) {
                    Object object = editors[i].getEditor(false);
                    if (object instanceof DiagramEditor) {
                        StatusHighlighter.addPipelineColor();
                    }
                }
            }
        }
    }
    

     /**
     * Get target-figure for given editPart.
     * 
     * @param editPart
     *            Given editpart.
     * @return figure Figure for given editpart.
     */
    private IFigure getTargetFigure(IGraphicalEditPart editPart) {

        IFigure figure = editPart.getFigure();
        
        if (figure instanceof BorderedNodeFigure) {
            figure = (IFigure) figure.getChildren().get(0);
        }
        if (figure instanceof DefaultSizeNodeFigure) {
            figure = (IFigure) figure.getChildren().get(0);
        }
        return figure;
    }
    
    /**
     * Get info about pipeline-elements.
     * @return pipelineDataflowList List holding info about pipeline-elements.
     */
    public List<PipelineDataflowInformationWrapper> getPipelineFlowInfo() {
        return pipelineDataflowList;
    }
    
    /**
     * Clears the list which holds the info about data flow. Therefore no pipeline-element will be colored
     * anymore after a connection is closed.
     */
    public void resetPipelineFlowInfo() {
        pipelineDataflowList.clear();
    }
    
    /**
     * Get editpart for a given diagram-element {@link EObject}.
     * 
     * @param semanticElement
     *            Given diagram-element {@link EObject}.
     * @return editPart the editPart for given semanticElement {@link EObject}.
     */
    private IGraphicalEditPart getEditPartForSemanticElement(EObject semanticElement) {
        
        DiagramEditor editor = (DiagramEditor) PlatformUI.getWorkbench()
                 .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
         
        IGraphicalEditPart editPart = EditPartUtils.findEditPartForSemanticElement(
                 editor.getDiagramGraphicalViewer().getRootEditPart(), semanticElement);
        return editPart;
    }
     
    /**
     * Reset the dataflow-marking in all pipeline-diagrams.
     */
    public void resetPipelineDataFlowMarkings() {
        IEditorReference[] editors = PlatformUI.getWorkbench()
        .getActiveWorkbenchWindow().getActivePage().getEditorReferences();
        
        for (int i = 0; i < editors.length; i++) {
            Object object = editors[i].getEditor(false);
            if (object instanceof DiagramEditor) {
                DiagramEditor editor = (DiagramEditor) object;
                
                EList<EObject> elementList = editor.getDiagram().eContents();
                
                for (int j = 0; j < elementList.size(); j++) {
                    EObject eobject = elementList.get(j);
                    
                    if (eobject instanceof ShapeImpl) {
                        ShapeImpl shape = (ShapeImpl) eobject;
                        EObject element = shape.getElement();
                       
                        if (element instanceof SourceImpl) {
                            SourceImpl source = (SourceImpl) element;
                            StatusHighlighter.getInstance().highlightDataFlowForSource(source,
                                    ElementStatusIndicator.NONE);
                        }
                        if (element instanceof FamilyElementImpl) {
                            FamilyElementImpl source = (FamilyElementImpl) element;
                            StatusHighlighter.getInstance().highlightDataFlowForFamily(source,
                                    ElementStatusIndicator.NONE);
                        }
                        if (element instanceof SinkImpl) {
                            SinkImpl source = (SinkImpl) element;
                            StatusHighlighter.getInstance().highlightDataFlowForSink(source,
                                    ElementStatusIndicator.NONE);
                        }
                        if (element instanceof DataManagementElementImpl) {
                            DataManagementElementImpl source = (DataManagementElementImpl) element;
                            StatusHighlighter.getInstance().highlightDataFlowForDatamangement(source,
                                    ElementStatusIndicator.NONE);
                        }
                    }
                }
                
            }
            
        }
    }
    
    /**
     * Add color to pipeline.
     */
    public static void addPipelineColor() {
        
        List<de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter.PipelineDataflowInformationWrapper>
            wrapperList = StatusHighlighter.getInstance().getPipelineFlowInfo();
        
        IEditorReference[] editors = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                
        for (int i = 0; i < editors.length; i++) {
            Object object = editors[i].getEditor(false);
            if (object instanceof DiagramEditor) {
                DiagramEditor editor = (DiagramEditor) object;
                        
                EList<EObject> elementList = editor.getDiagram().eContents();
                        
                for (int j = 0; j < elementList.size(); j++) {
  
                    String pipelineName = editor.getTitle().toLowerCase();       
 
                    EObject element = editor.getDiagram().getElement();
                    EList<EObject> eContents = element.eContents();
                            
                    for (int k = 0; k < wrapperList.size(); k++) {
                                
                        PipelineDataflowInformationWrapper wrapper = wrapperList.get(k);
                    
                        if (wrapper.getPipelineName().toLowerCase().equals(pipelineName)) {
                    
                            for (int l = 0; l < eContents.size(); l++) {
                                            
                                String name = eContents.get(l).toString();
                                name = determineName(name);
                                       
                                if (wrapper.getVariableName().equals(name)) {
                                    highlightDataFlow(eContents.get(l), wrapper.getIndicator());
                                }    
                            }
                        }
                    }
                }
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
            StatusHighlighter.getInstance().highlightDataFlowForSource(source, indicator);
        }
        if (eobject instanceof FamilyElementImpl) {
            FamilyElementImpl source = (FamilyElementImpl) eobject;
            StatusHighlighter.getInstance().highlightDataFlowForFamily(source, indicator);
        }
        if (eobject instanceof SinkImpl) {
            SinkImpl source = (SinkImpl) eobject;
            StatusHighlighter.getInstance().highlightDataFlowForSink(source, indicator);
        }
        if (eobject instanceof DataManagementElementImpl) {
            DataManagementElementImpl source = (DataManagementElementImpl) eobject;
            StatusHighlighter.getInstance().highlightDataFlowForDatamangement(source, indicator);
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
     * Highlight a given source element.
     * 
     * @param eobject
     *            Pipelines source element to be marked.
     * @param dataflow
     *            Given dataflow-information.
     */
    public void highlightDataFlowForSource(EObject eobject, ElementStatusIndicator dataflow) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
        
        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);
    
            SVGFigure svgFigure = (SVGFigure) figure;
            String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
            
            if (eobject instanceof SourceImpl) {
    
                switch(dataflow) {
                case VERYHIGH:  
                    uri += IconManager.SVG_SOURCE_VERY_HIGH;
                    break;
                case HIGH:    
                    uri += IconManager.SVG_SOURCE_HIGH;
                    break;
                case MEDIUM:            
                    uri += IconManager.SVG_SOURCE_MEDIUM;
                    break;
                case LOW:                 
                    uri += IconManager.SVG_SOURCE_LOW;
                    break;
                case VERYLOW:                 
                    uri += IconManager.SVG_SOURCE_VERY_LOW;
                    break;
                case NONE:
                    uri = IconManager.SVG_SOURCE_STANDARD;
                    break;
                default:                
                    uri = IconManager.SVG_SOURCE_STANDARD;
                    break;
    
                }
                svgFigure.setURI(uri);
                svgFigure.repaint();
            }
        }
    }

    /**
     * Highlight a given sink element.
     * 
     * @param eobject
     *            Pipelines sink element to be marked.
     * @param dataflow
     *            Given dataflow-information.
     */
    public void highlightDataFlowForSink(EObject eobject, ElementStatusIndicator dataflow) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
        
        if (editPartForSemanticElement != null) {

            IFigure figure = getTargetFigure(editPartForSemanticElement);
    
            SVGFigure svgFigure = (SVGFigure) figure;
            String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
            
            if (eobject instanceof SinkImpl) {
    
                switch(dataflow) {
                case VERYHIGH:  
                    uri += IconManager.SVG_SINK_VERY_HIGH;
                    break;
                case HIGH:    
                    uri += IconManager.SVG_SINK_HIGH;
                    break;
                case MEDIUM:            
                    uri += IconManager.SVG_SINK_MEDIUM;
                    break;
                case LOW:                 
                    uri += IconManager.SVG_SINK_LOW;
                    break;
                case VERYLOW:                 
                    uri += IconManager.SVG_SINK_VERY_LOW;
                    break;
                case NONE:
                    uri = IconManager.SVG_SOURCE_STANDARD;
                    break;
                default:                
                    uri = IconManager.SVG_SINK_STANDARD;
                    break;
    
                }
                svgFigure.setURI(uri);
                svgFigure.repaint();
            }
        }
    }

    /**
     * Highlight a given family element.
     * 
     * @param eobject
     *            Pipelines family element to be marked.
     * @param dataflow
     *            Given dataflow-information.
     */
    public void highlightDataFlowForFamily(EObject eobject, ElementStatusIndicator dataflow) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
        
        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);
    
            SVGFigure svgFigure = (SVGFigure) figure;
            String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
            
            if (eobject instanceof FamilyElementImpl) {
    
                switch(dataflow) {
                case VERYHIGH:  
                    uri += IconManager.SVG_FAMILYELEMENT_VERY_HIGH;
                    break;
                case HIGH:    
                    uri += IconManager.SVG_FAMILYELEMENT_HIGH;
                    break;
                case MEDIUM:            
                    uri += IconManager.SVG_FAMILYELEMENT_MEDIUM;
                    break;
                case LOW:                 
                    uri += IconManager.SVG_FAMILYELEMENT_LOW;
                    break;
                case VERYLOW:                 
                    uri += IconManager.SVG_FAMILYELEMENT_VERY_LOW;
                    break;
                case NONE:
                    uri = IconManager.SVG_SOURCE_STANDARD;
                    break;
                default:                
                    uri = IconManager.SVG_FAMILYELEMENT_STANDARD;
                    break;
    
                }
                svgFigure.setURI(uri);
                svgFigure.repaint();
            }
        }
    }

    /**
     * Highlight a given datamangement element.
     * 
     * @param eobject
     *            Pipelines datamangement element to be marked.
     * @param dataflow
     *            Given dataflow-information.
     */
    public void highlightDataFlowForDatamangement(EObject eobject, ElementStatusIndicator dataflow) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
        
        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);
    
            SVGFigure svgFigure = (SVGFigure) figure;
            String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
            
            if (eobject instanceof DataManagementElementImpl) {
    
                switch(dataflow) {
                case VERYHIGH:  
                    uri += IconManager.SVG_DATAMANAGEMENT_VERY_HIGH;
                    break;
                case HIGH:    
                    uri += IconManager.SVG_DATAMANAGEMENT_HIGH;
                    break;
                case MEDIUM:            
                    uri += IconManager.SVG_DATAMANAGEMENT_MEDIUM;
                    break;
                case LOW:                 
                    uri += IconManager.SVG_DATAMANAGEMENT_LOW;
                    break;
                case VERYLOW:                 
                    uri += IconManager.SVG_DATAMANAGEMENT_VERY_LOW;
                    break;
                case NONE:
                    uri = IconManager.SVG_SOURCE_STANDARD;
                    break;
                default:                
                    uri = IconManager.SVG_DATAMANAGEMENT_STANDARD;
                    break;
    
                }
                svgFigure.setURI(uri);
                svgFigure.repaint();
            }
        }
    }
}
