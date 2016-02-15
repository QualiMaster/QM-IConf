package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.figures.BorderedNodeFigure;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.gef.ui.figures.DefaultSizeNodeFigure;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Highlighter is responsible for highlighting elements in the Pipeline by
 * changing the color of elements or adding boders to elements.
 * 
 * @author Niko
 */
public class Highlighter implements IHighlighter {

    private final IDiagramWorkbenchPart diagramWorkbenchPart;
//    private List<PipelineDataflowInformationWrapper> pipelineDataflowList = 
//            new ArrayList<PipelineDataflowInformationWrapper>();
//    
//    /**
//     * Wraps up info about Pipeline-Elements and the status of them.
//     * @author nowatzki
//     *
//     */
//    public static class PipelineDataflowInformationWrapper {
//        private String pipelineName;
//        private String variableName;
//        private ElementStatusIndicator indicator;
//        
//        /**
//         * Contructs a Informationwrapper for Pipelineelements.
//         * @param pipelineName name of the pipeline
//         * @param variableName name of the variable.
//         * @param indicator Indicator which indicated the situation of an element.
//         */
//        public PipelineDataflowInformationWrapper(String pipelineName, String variableName,
//                ElementStatusIndicator indicator) {
//            this.pipelineName = pipelineName;
//            this.variableName = variableName;
//            this.indicator = indicator;
//        }
//        
//        /**
//         * Get the pipelineName.
//         * @return pipelineName
//         */
//        public String getPipelineName() {
//            return pipelineName;
//        }
//        /**
//         * Get the variableName.
//         * @return variableName.
//         */
//        public String getVariableName() {
//            return variableName;
//        }
//        /**
//         * Get the indicator.
//         * @return indicator The indicator for the element.
//         */
//        public ElementStatusIndicator getIndicator() {
//            return indicator;
//        }
//    }
    
    /**
    * Constructs a Highlighter-instance.
    * 
    * @param diagramWorkbenchPart
    *            workbenchPart which contains the diagramelements.
    */
    public Highlighter(IDiagramWorkbenchPart diagramWorkbenchPart) {
        this.diagramWorkbenchPart = diagramWorkbenchPart;
    }

//    /**
//     * mark a specific variable within a pipeline given by the corresponding indicator.
//     * @param pipelineName name of the pipeline.
//     * @param variableName name of the variable.
//     * @param indicator indicator which indicates the situation of the element.
//     */
//    public void markPipeline(String pipelineName, String variableName, ElementStatusIndicator indicator) {
//        
//        PipelineDataflowInformationWrapper wrapper = new PipelineDataflowInformationWrapper(
//                pipelineName, variableName, indicator);
//        boolean alreadySaved = false;
//        
//        for (int i = 0; i < pipelineDataflowList.size(); i++) {
//            
//            PipelineDataflowInformationWrapper existingWrapper = pipelineDataflowList.get(i);
//            
//            if (existingWrapper.pipelineName.equals(wrapper.pipelineName)
//                    && existingWrapper.variableName.equals(wrapper.variableName)
//                    && existingWrapper.indicator.equals(wrapper.indicator)) {
//                alreadySaved = true;
//            }
//            
//            //Farbe erneuern
//            if (existingWrapper.pipelineName.equals(wrapper.pipelineName)
//                    && existingWrapper.variableName.equals(wrapper.variableName)) {
//                
//                existingWrapper.indicator = indicator;
//            }
//            
//            if (!alreadySaved) {
//                PipelineDataflowInformationWrapper newWrapper = 
//                        new PipelineDataflowInformationWrapper(pipelineName, variableName, indicator);
//                pipelineDataflowList.add(newWrapper);
//            }
//            
//            annotateOpenedPipelineEditor();
//        }
//    }
//
//    /**
//     * Mark the flawed pipeline-elements in the currently opened Pipeline-Diagram.
//     */
//    private static void annotateOpenedPipelineEditor() {
//        if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
//                instanceof DiagramEditor) {
//        
//            DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench().
//                    getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//            if (diagram instanceof PipelineDiagramEditor) {
//                PipelineDiagramUtils.addPipelineColor();
//            }
//        }
//    }
//    
//    /**
//     * Get info about pipeline-elements.
//     * @return pipelineDataflowList List holding info about pipeline-elements.
//     */
//    public List<PipelineDataflowInformationWrapper> getPipelineFlowInfo() {
//        return pipelineDataflowList;
//    }
    
    /**
    * Get editpart for a given diagram-element {@link EObject}.
    * 
    * @param semanticElement
    *            Given diagram-element {@link EObject}.
    * @return editPart the editPart for given semanticElement {@link EObject}.
    */
    private IGraphicalEditPart getEditPartForSemanticElement(EObject semanticElement) {
        IGraphicalEditPart editPart = EditPartUtils.findEditPartForSemanticElement(
               diagramWorkbenchPart.getDiagramGraphicalViewer().getRootEditPart(), semanticElement);
        return editPart;
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
    * Highlight given object in the diagram.
    * 
    * @param semanticElement
    *            Given element {@link EObject}.
    * @param parameters
    *            paramters which specify how elements will be adapted.
    */
    public void highlight(EObject semanticElement, HighlighterParam parameters) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);
        editPartForSemanticElement.getFigure().setBackgroundColor(new Color(Display.getCurrent(), 255, 0, 0));

        if (editPartForSemanticElement != null) {

            IFigure figure = getTargetFigure(editPartForSemanticElement);

            figure.setBorder(new LineBorder(parameters.getDiagramErrorColor()));
            figure.revalidate();
        }
    }
    
    /**
     * Highlight given object in the diagram.
     * 
     * @param semanticElement
     *            Given element {@link EObject}.
     * @param parameters
     *            paramters which specify how elements will be adapted.
     */
    public void resetNode(EObject semanticElement, HighlighterParam parameters) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);

        if (editPartForSemanticElement != null) {

            IFigure figure = getTargetFigure(editPartForSemanticElement);
            
            figure.setBorder(new LineBorder(parameters.getDiagramStandardNodeColor()));
            figure.revalidate();
        }
    }

    /**
    * Highlight given object in the diagram.
    * 
    * @param semanticElement
    *            Given element {@link EObject}.
    * @param parameters
    *            paramters which specify how elements will be adapted.
    * @param errorMessage the error message.
    */
    public void highlight(EObject semanticElement, HighlighterParam parameters,
            String errorMessage) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);

        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);

            figure.setBorder(new LineBorder(parameters.getDiagramErrorColor()));
            CustomTextFigure customFigure = new CustomTextFigure();
            customFigure.setText(errorMessage);
            figure.setToolTip(customFigure);
            figure.invalidate();
        }
    }

    /**
    * Reset given flow in pipeline-diagram.
    * 
    * @param semanticElement
    *            Given element which will be highlighted.
    * @param parameters
    *            Given paramters which determine the specific highlight.
    */
    public void highlightFlow(EObject semanticElement, HighlighterParam parameters) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);

        if (editPartForSemanticElement != null) {

            IFigure figure = getTargetFigure(editPartForSemanticElement);

            if (parameters != null) {

                figure.setForegroundColor(parameters.getDiagramErrorColor());
                figure.invalidate();
            }
        }
    }
    
    /**
     * Reset given flow in pipeline-diagram.
     * 
     * @param semanticElement
     *            Given element which will be highlighted.
     * @param parameters
     *            Given paramters which determine the specific highlight.
     */
    public void resetFlow(EObject semanticElement, HighlighterParam parameters) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);

        if (editPartForSemanticElement != null) {

            IFigure figure = getTargetFigure(editPartForSemanticElement);

            if (parameters != null) {

                figure.setForegroundColor(parameters.getDiagramStandardFlowColor());
                figure.invalidate();
            }
        }
    }

    /**
    * Highlight given flow in pipline-diagram.
    * 
    * @param semanticElement
    *            Given element which will be highlighted.
    * @param parameters
    *            Given paramters which determine the specific highlight.
    * @param errorMessage the error message.
    */
    public void highlightFlow(EObject semanticElement,
            HighlighterParam parameters, String errorMessage) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(semanticElement);
    
        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);
        
            figure.setForegroundColor(parameters.getDiagramErrorColor());
            CustomTextFigure customFigure = new CustomTextFigure();
            customFigure.setText(errorMessage);
            figure.setToolTip(customFigure);
            figure.invalidate();
        }
    }
    
//    /**
//     * Highlight a given source element.
//     * 
//     * @param eobject
//     *            Pipelines source element to be marked.
//     * @param dataflow
//     *            Given dataflow-information.
//     */
//    public void highlightDataFlowForSource(EObject eobject, ElementStatusIndicator dataflow) {
//
//        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
//        IFigure figure = getTargetFigure(editPartForSemanticElement);
//
//        SVGFigure svgFigure = (SVGFigure) figure;
//        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
//        
//        if (eobject instanceof SourceImpl) {
//
//            switch(dataflow) {
//            case VERYHIGH:  
//                uri += IconManager.SVG_SOURCE;
//                break;
//            case HIGH:    
//                uri += IconManager.SVG_SOURCE2;
//                break;
//            case MEDIUM:            
//                uri += IconManager.SVG_SOURCE3;
//                break;
//            case LOW:                 
//                uri += IconManager.SVG_SOURCE4;
//                break;
//            case VERYLOW:                 
//                uri += IconManager.SVG_SOURCE5;
//                break;
//            default:                
//                uri += IconManager.SVG_SOURCE;
//                break;
//
//            }
//            svgFigure.setURI(uri);
//        }
//    }
//
//    /**
//     * Highlight a given sink element.
//     * 
//     * @param eobject
//     *            Pipelines sink element to be marked.
//     * @param dataflow
//     *            Given dataflow-information.
//     */
//    public void highlightDataFlowForSink(EObject eobject, ElementStatusIndicator dataflow) {
//
//        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
//        IFigure figure = getTargetFigure(editPartForSemanticElement);
//
//        SVGFigure svgFigure = (SVGFigure) figure;
//        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
//        
//        if (eobject instanceof SinkImpl) {
//
//            switch(dataflow) {
//            case VERYHIGH:  
//                uri += IconManager.SVG_SINK;
//                break;
//            case HIGH:    
//                uri += IconManager.SVG_SINK2;
//                break;
//            case MEDIUM:            
//                uri += IconManager.SVG_SINK3;
//                break;
//            case LOW:                 
//                uri += IconManager.SVG_SINK4;
//                break;
//            case VERYLOW:                 
//                uri += IconManager.SVG_SINK5;
//                break;
//            default:                
//                uri += IconManager.SVG_SINK5;
//                break;
//
//            }
//            svgFigure.setURI(uri);
//        }
//    }
//
//    /**
//     * Highlight a given family element.
//     * 
//     * @param eobject
//     *            Pipelines family element to be marked.
//     * @param dataflow
//     *            Given dataflow-information.
//     */
//    public void highlightDataFlowForFamily(EObject eobject, ElementStatusIndicator dataflow) {
//
//        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
//        IFigure figure = getTargetFigure(editPartForSemanticElement);
//
//        SVGFigure svgFigure = (SVGFigure) figure;
//        
//        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
//        
//        if (eobject instanceof FamilyElementImpl) {
//
//            switch(dataflow) {
//            case VERYHIGH:  
//                uri += IconManager.SVG_FAMILYELEMENT;
//                break;
//            case HIGH:    
//                uri += IconManager.SVG_FAMILYELEMENT2;
//                break;
//            case MEDIUM:            
//                uri += IconManager.SVG_FAMILYELEMENT3;
//                break;
//            case LOW:                 
//                uri += IconManager.SVG_FAMILYELEMENT4;
//                break;
//            case VERYLOW:                 
//                uri += IconManager.SVG_FAMILYELEMENT5;
//                break;
//            default:                
//                uri += IconManager.SVG_FAMILYELEMENT5;
//                break;
//
//            }
//            svgFigure.setURI(uri);
//        }
//    }
//
//    /**
//     * Highlight a given datamangement element.
//     * 
//     * @param eobject
//     *            Pipelines datamangement element to be marked.
//     * @param dataflow
//     *            Given dataflow-information.
//     */
//    public void highlightDataFlowForDatamangement(EObject eobject, ElementStatusIndicator dataflow) {
//
//        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eobject);
//        IFigure figure = getTargetFigure(editPartForSemanticElement);
//
//        SVGFigure svgFigure = (SVGFigure) figure;
//        String uri = "platform:/plugin/QualiMasterApplication/icons/pipelineDataflow/";
//        
//        if (eobject instanceof DataManagementElementImpl) {
//
//            switch(dataflow) {
//            case VERYHIGH:  
//                uri += "datamanagement.svg";
//                break;
//            case HIGH:    
//                uri += "datamanagement2.svg";
//                break;
//            case MEDIUM:            
//                uri += "datamanagement3.svg";
//                break;
//            case LOW:                 
//                uri += "datamanagement4.svg";
//                break;
//            case VERYLOW:                 
//                uri += "datamanagement5.svg";
//                break;
//            default:                
//                uri += "datamanagement.svg";
//                break;
//
//            }
//            svgFigure.setURI(uri);
//        }
//    }
}