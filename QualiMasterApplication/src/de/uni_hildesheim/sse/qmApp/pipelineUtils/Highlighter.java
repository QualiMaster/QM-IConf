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
 * changing the color of elements or adding borders to elements.
 * 
 * @author Niko
 */
public class Highlighter implements IHighlighter {

    private final IDiagramWorkbenchPart diagramWorkbenchPart;

    /**
    * Constructs a Highlighter-instance.
    * 
    * @param diagramWorkbenchPart
    *            workbenchPart which contains the diagramelements.
    */
    public Highlighter(IDiagramWorkbenchPart diagramWorkbenchPart) {
        this.diagramWorkbenchPart = diagramWorkbenchPart;
    }

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

    /**
     * Remove an possibly existing tooltip from the given eObject.
     * The eObject can only be a {@link NodeImpl} or {@link FlowImpl}.
     * In both cases the tooltip-figure is set to null thus nothing tooltip is shown
     * in the editor.
     * @param eObject Given node or flow. The node´s or flow´s tooltip will be removed.
     */
    public void removeTooltip(EObject eObject) {

        IGraphicalEditPart editPartForSemanticElement = getEditPartForSemanticElement(eObject);

        if (editPartForSemanticElement != null) {
            IFigure figure = getTargetFigure(editPartForSemanticElement);

            figure.setToolTip(null);
            figure.invalidate();
        }
    }
}