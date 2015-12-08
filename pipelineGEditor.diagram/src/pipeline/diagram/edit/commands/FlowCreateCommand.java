/*
 * 
 */
package pipeline.diagram.edit.commands;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.common.core.command.ICommand;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.type.core.commands.EditElementCommand;
import org.eclipse.gmf.runtime.emf.type.core.requests.ConfigureRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateElementRequest;
import org.eclipse.gmf.runtime.emf.type.core.requests.CreateRelationshipRequest;

import pipeline.Flow;
import pipeline.Node;
import pipeline.Pipeline;
import pipeline.PipelineElement;
import pipeline.PipelineFactory;
import pipeline.diagram.edit.policies.PipelineBaseItemSemanticEditPolicy;

/**
 * @generated
 */
public class FlowCreateCommand extends EditElementCommand {

    /**
     * @generated
     */
    private final EObject source;

    /**
     * @generated
     */
    private final EObject target;

    /**
     * @generated
     */
    private final Pipeline container;

    /**
     * @generated
     */
    public FlowCreateCommand(CreateRelationshipRequest request, EObject source,
            EObject target) {
        super(request.getLabel(), null, request);
        this.source = source;
        this.target = target;
        container = deduceContainer(source, target);
    }

    /**
     * @generated
     */
    public boolean canExecute() {
        if (source == null && target == null) {
            return false;
        }
        if (source != null && false == source instanceof PipelineElement) {
            return false;
        }
        if (target != null && false == target instanceof PipelineElement) {
            return false;
        }
        if (getSource() == null) {
            return true; // link creation is in progress; source is not defined yet
        }
        // target may be null here but it's possible to check constraint
        if (getContainer() == null) {
            return false;
        }
        return PipelineBaseItemSemanticEditPolicy.getLinkConstraints()
                .canCreateFlow_4001(getContainer(), getSource(), getTarget());
    }

    /**
     * @generated
     */
    protected CommandResult doExecuteWithResult(IProgressMonitor monitor,
            IAdaptable info) throws ExecutionException {
        if (!canExecute()) {
            throw new ExecutionException(
                    "Invalid arguments in create link command"); //$NON-NLS-1$
        }

        Flow newElement = PipelineFactory.eINSTANCE.createFlow();
        getContainer().getFlows().add(newElement);
        newElement.setSource(getSource());
        newElement.setDestination(getTarget());
        doConfigure(newElement, monitor, info);
        ((CreateElementRequest) getRequest()).setNewElement(newElement);
        return CommandResult.newOKCommandResult(newElement);

    }

    /**
     * @generated
     */
    protected void doConfigure(Flow newElement, IProgressMonitor monitor,
            IAdaptable info) throws ExecutionException {
        IElementType elementType = ((CreateElementRequest) getRequest())
                .getElementType();
        ConfigureRequest configureRequest = new ConfigureRequest(
                getEditingDomain(), newElement, elementType);
        configureRequest.setClientContext(((CreateElementRequest) getRequest())
                .getClientContext());
        configureRequest.addParameters(getRequest().getParameters());
        configureRequest.setParameter(CreateRelationshipRequest.SOURCE,
                getSource());
        configureRequest.setParameter(CreateRelationshipRequest.TARGET,
                getTarget());
        ICommand configureCommand = elementType
                .getEditCommand(configureRequest);
        if (configureCommand != null && configureCommand.canExecute()) {
            configureCommand.execute(monitor, info);
        }
    }

    /**
     * @generated
     */
    protected void setElementToEdit(EObject element) {
        throw new UnsupportedOperationException();
    }

    /**
     * @generated
     */
    protected PipelineElement getSource() {
        return (PipelineElement) source;
    }

    /**
     * @generated
     */
    protected PipelineElement getTarget() {
        return (PipelineElement) target;
    }

    /**
     * @generated
     */
    public Pipeline getContainer() {
        return container;
    }

    /**
     * Default approach is to traverse ancestors of the source to find instance of container.
     * Modify with appropriate logic.
     * @generated
     */
    private static Pipeline deduceContainer(EObject source, EObject target) {
        // Find container element for the new link.
        // Climb up by containment hierarchy starting from the source
        // and return the first element that is instance of the container class.
        for (EObject element = source; element != null; element = element
                .eContainer()) {
            if (element instanceof Pipeline) {
                return (Pipeline) element;
            }
        }
        return null;
    }

}
