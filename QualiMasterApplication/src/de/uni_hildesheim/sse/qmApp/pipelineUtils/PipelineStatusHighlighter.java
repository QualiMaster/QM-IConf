package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.gmf.runtime.lite.svg.SVGFigure;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.treeView.ElementStatusIndicator;
import pipeline.diagram.part.PipelineDiagramEditor;
import pipeline.impl.DataManagementElementImpl;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.SinkImpl;
import pipeline.impl.SourceImpl;

/**
 * Singleton-Class for highlighting Pipeline-Editors.
 * @author nowatzki
 */
public class PipelineStatusHighlighter {

    public static final PipelineStatusHighlighter INSTANCE = new PipelineStatusHighlighter();
    private List<PipelineDataflowInformationWrapper> pipelineDataflowList = 
            new ArrayList<PipelineDataflowInformationWrapper>();
    
    /**
     * // Exists only to avoid instantiation.
     */
    private PipelineStatusHighlighter() {
    }
    
//    /**
//     * Constructs an instance of PipelineStatusHighlighter.
//     * @return instance singelton instance.
//     */
//    public static PipelineStatusHighlighter getInstance() {
//        if (instance == null) {
//            instance = new PipelineStatusHighlighter();
//        }
//        return instance;
//    }
    
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
        boolean alreadySaved = false;
        
        for (int i = 0; i < pipelineDataflowList.size(); i++) {
            
            PipelineDataflowInformationWrapper existingWrapper = pipelineDataflowList.get(i);
            
            if (existingWrapper.pipelineName.equals(wrapper.pipelineName)
                    && existingWrapper.variableName.equals(wrapper.variableName)
                    && existingWrapper.indicator.equals(wrapper.indicator)) {
                alreadySaved = true;
            }
            
            //Farbe erneuern
            if (existingWrapper.pipelineName.equals(wrapper.pipelineName)
                    && existingWrapper.variableName.equals(wrapper.variableName)) {
                
                existingWrapper.indicator = indicator;
            }
            
            if (!alreadySaved) {
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
     * Mark the flawed pipeline-elements in the currently opened Pipeline-Diagram.
     */
    private static void annotateOpenedPipelineEditor() {
        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                instanceof DiagramEditor) {
        
            DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench().
                    getActiveWorkbenchWindow().getActivePage().getActiveEditor();
            if (diagram instanceof PipelineDiagramEditor) {
                PipelineDiagramUtils.addPipelineColor();
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
     * Highlight a given source element.
     * 
     * @param eobject
     *            Pipelines source element to be marked.
     * @param dataflow
     *            Given dataflow-information.
     */
    public void highlightDataFlowForSource(EObject eobject, ElementStatusIndicator dataflow) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
        IFigure figure = getTargetFigure(editPartForSemanticElement);

        SVGFigure svgFigure = (SVGFigure) figure;
        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
        
        if (eobject instanceof SourceImpl) {

            switch(dataflow) {
            case VERYHIGH:  
                uri += IconManager.SVG_SOURCE;
                break;
            case HIGH:    
                uri += IconManager.SVG_SOURCE2;
                break;
            case MEDIUM:            
                uri += IconManager.SVG_SOURCE3;
                break;
            case LOW:                 
                uri += IconManager.SVG_SOURCE4;
                break;
            case VERYLOW:                 
                uri += IconManager.SVG_SOURCE5;
                break;
            default:                
                uri += IconManager.SVG_SOURCE;
                break;

            }
            svgFigure.setURI(uri);
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
        IFigure figure = getTargetFigure(editPartForSemanticElement);

        SVGFigure svgFigure = (SVGFigure) figure;
        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
        
        if (eobject instanceof SinkImpl) {

            switch(dataflow) {
            case VERYHIGH:  
                uri += IconManager.SVG_SINK;
                break;
            case HIGH:    
                uri += IconManager.SVG_SINK2;
                break;
            case MEDIUM:            
                uri += IconManager.SVG_SINK3;
                break;
            case LOW:                 
                uri += IconManager.SVG_SINK4;
                break;
            case VERYLOW:                 
                uri += IconManager.SVG_SINK5;
                break;
            default:                
                uri += IconManager.SVG_SINK5;
                break;

            }
            svgFigure.setURI(uri);
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
        IFigure figure = getTargetFigure(editPartForSemanticElement);

        SVGFigure svgFigure = (SVGFigure) figure;
        
        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
        
        if (eobject instanceof FamilyElementImpl) {

            switch(dataflow) {
            case VERYHIGH:  
                uri += IconManager.SVG_FAMILYELEMENT;
                break;
            case HIGH:    
                uri += IconManager.SVG_FAMILYELEMENT2;
                break;
            case MEDIUM:            
                uri += IconManager.SVG_FAMILYELEMENT3;
                break;
            case LOW:                 
                uri += IconManager.SVG_FAMILYELEMENT4;
                break;
            case VERYLOW:                 
                uri += IconManager.SVG_FAMILYELEMENT5;
                break;
            default:                
                uri += IconManager.SVG_FAMILYELEMENT5;
                break;

            }
            svgFigure.setURI(uri);
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
        IFigure figure = getTargetFigure(editPartForSemanticElement);

        SVGFigure svgFigure = (SVGFigure) figure;
        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
        
        if (eobject instanceof DataManagementElementImpl) {

            switch(dataflow) {
            case VERYHIGH:  
                uri += "datamanagement.svg";
                break;
            case HIGH:    
                uri += "datamanagement2.svg";
                break;
            case MEDIUM:            
                uri += "datamanagement3.svg";
                break;
            case LOW:                 
                uri += "datamanagement4.svg";
                break;
            case VERYLOW:                 
                uri += "datamanagement5.svg";
                break;
            default:                
                uri += "datamanagement.svg";
                break;

            }
            svgFigure.setURI(uri);
        }
    }
}
