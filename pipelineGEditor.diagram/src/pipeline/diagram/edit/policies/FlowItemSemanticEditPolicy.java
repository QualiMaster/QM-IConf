/*
 * 
 */
package pipeline.diagram.edit.policies;

import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.emf.type.core.commands.DestroyElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DestroyElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.ReorientRelationshipRequest;

import pipeline.diagram.edit.commands.FlowCreateCommand;
import pipeline.diagram.edit.commands.FlowReorientCommand;
import pipeline.diagram.edit.parts.FlowEditPart;
import pipeline.diagram.providers.PipelineElementTypes;

/**
 * @generated
 */
public class FlowItemSemanticEditPolicy extends
        PipelineBaseItemSemanticEditPolicy {

    /**
     * @generated
     */
    public FlowItemSemanticEditPolicy() {
        super(PipelineElementTypes.Flow_4001);
    }

    /**
     * @generated
     */
    protected Command getDestroyElementCommand(DestroyElementRequest req) {
        return getGEFWrapper(new DestroyElementCommand(req));
    }

    /**
     * @generated
     */
    protected Command getCreateRelationshipCommand(CreateRelationshipRequest req) {
        Command command = req.getTarget() == null ? getStartCreateRelationshipCommand(req)
                : getCompleteCreateRelationshipCommand(req);
        return command != null ? command : super
                .getCreateRelationshipCommand(req);
    }

    /**
     * @generated
     */
    protected Command getStartCreateRelationshipCommand(
            CreateRelationshipRequest req) {
        if (PipelineElementTypes.Flow_4001 == req.getElementType()) {
            return getGEFWrapper(new FlowCreateCommand(req, req.getSource(),
                    req.getTarget()));
        }
        return null;
    }

    /**
     * @generated
     */
    protected Command getCompleteCreateRelationshipCommand(
            CreateRelationshipRequest req) {
        if (PipelineElementTypes.Flow_4001 == req.getElementType()) {
            return getGEFWrapper(new FlowCreateCommand(req, req.getSource(),
                    req.getTarget()));
        }
        return null;
    }

    /**
     * Returns command to reorient EClass based link. New link target or source
     * should be the domain model element associated with this node.
     * 
     * @generated
     */
    protected Command getReorientRelationshipCommand(
            ReorientRelationshipRequest req) {
        switch (getVisualID(req)) {
        case FlowEditPart.VISUAL_ID:
            return getGEFWrapper(new FlowReorientCommand(req));
        }
        return super.getReorientRelationshipCommand(req);
    }

}
