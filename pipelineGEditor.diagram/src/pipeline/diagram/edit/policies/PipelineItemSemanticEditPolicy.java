package pipeline.diagram.edit.policies;

import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.commands.core.commands.DuplicateEObjectsCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.DuplicateElementsRequest;
import pipeline.diagram.edit.commands.DataManagementElementCreateCommand;
import pipeline.diagram.edit.commands.FamilyElementCreateCommand;
import pipeline.diagram.edit.commands.ReplaySinkCreateCommand;
import pipeline.diagram.edit.commands.SinkCreateCommand;
import pipeline.diagram.edit.commands.SourceCreateCommand;
import pipeline.diagram.providers.PipelineElementTypes;

/**
 * @generated
 */
public class PipelineItemSemanticEditPolicy extends
		PipelineBaseItemSemanticEditPolicy {

	/**
	 * @generated
	 */
	public PipelineItemSemanticEditPolicy() {
		super(PipelineElementTypes.Pipeline_1000);
	}

	/**
	 * @generated
	 */
	protected Command getCreateCommand(CreateElementRequest req) {
		if (PipelineElementTypes.ReplaySink_2007 == req.getElementType()) {
			return getGEFWrapper(new ReplaySinkCreateCommand(req));
		}
		if (PipelineElementTypes.FamilyElement_2005 == req.getElementType()) {
			return getGEFWrapper(new FamilyElementCreateCommand(req));
		}
		if (PipelineElementTypes.DataManagementElement_2006 == req
				.getElementType()) {
			return getGEFWrapper(new DataManagementElementCreateCommand(req));
		}
		if (PipelineElementTypes.Source_2001 == req.getElementType()) {
			return getGEFWrapper(new SourceCreateCommand(req));
		}
		if (PipelineElementTypes.Sink_2002 == req.getElementType()) {
			return getGEFWrapper(new SinkCreateCommand(req));
		}
		return super.getCreateCommand(req);
	}

	/**
	 * @generated
	 */
	protected Command getDuplicateCommand(DuplicateElementsRequest req) {
		TransactionalEditingDomain editingDomain = ((IGraphicalEditPart) getHost())
				.getEditingDomain();
		return getGEFWrapper(new DuplicateAnythingCommand(editingDomain, req));
	}

	/**
	 * @generated
	 */
	private static class DuplicateAnythingCommand extends
			DuplicateEObjectsCommand {

		/**
		 * @generated
		 */
		public DuplicateAnythingCommand(
				TransactionalEditingDomain editingDomain,
				DuplicateElementsRequest req) {
			super(editingDomain, req.getLabel(), req
					.getElementsToBeDuplicated(), req
					.getAllDuplicatedElementsMap());
		}

	}

}
