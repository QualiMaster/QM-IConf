/*
 * 
 */
package pipeline.diagram.edit.parts;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITextAwareEditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.tooling.runtime.directedit.locator.CellEditorLocatorAccess;

import pipeline.diagram.part.PipelineVisualIDRegistry;

/**
 * @generated
 */
public class PipelineEditPartFactory implements EditPartFactory {

    /**
     * @generated
     */
    public EditPart createEditPart(EditPart context, Object model) {
        if (model instanceof View) {
            View view = (View) model;
            switch (PipelineVisualIDRegistry.getVisualID(view)) {

            case PipelineEditPart.VISUAL_ID:
                return new PipelineEditPart(view);

            case FamilyElementEditPart.VISUAL_ID:
                return new FamilyElementEditPart(view);

            case FamilyElementNameEditPart.VISUAL_ID:
                return new FamilyElementNameEditPart(view);

            case DataManagementElementEditPart.VISUAL_ID:
                return new DataManagementElementEditPart(view);

            case DataManagementElementNameEditPart.VISUAL_ID:
                return new DataManagementElementNameEditPart(view);

            case SourceEditPart.VISUAL_ID:
                return new SourceEditPart(view);

            case SourceNameEditPart.VISUAL_ID:
                return new SourceNameEditPart(view);

            case SinkEditPart.VISUAL_ID:
                return new SinkEditPart(view);

            case SinkNameEditPart.VISUAL_ID:
                return new SinkNameEditPart(view);

            case FlowEditPart.VISUAL_ID:
                return new FlowEditPart(view);

            case FlowNameEditPart.VISUAL_ID:
                return new FlowNameEditPart(view);

            }
        }
        return createUnrecognizedEditPart(context, model);
    }

    /**
     * @generated
     */
    private EditPart createUnrecognizedEditPart(EditPart context, Object model) {
        // Handle creation of unrecognized child node EditParts here
        return null;
    }

    /**
     * @generated
     */
    public static CellEditorLocator getTextCellEditorLocator(
            ITextAwareEditPart source) {
        return CellEditorLocatorAccess.INSTANCE
                .getTextCellEditorLocator(source);
    }

}
