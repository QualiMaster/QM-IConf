/*
 * 
 */
package pipeline.diagram.edit.policies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.core.util.ViewUtil;
import org.eclipse.gmf.runtime.diagram.ui.commands.DeferredLayoutCommand;
import org.eclipse.gmf.runtime.diagram.ui.commands.ICommandProxy;
import org.eclipse.gmf.runtime.diagram.ui.commands.SetViewMutabilityCommand;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.CanonicalEditPolicy;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateConnectionViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.CreateViewRequest;
import org.eclipse.gmf.runtime.diagram.ui.requests.RequestConstants;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.Edge;
import org.eclipse.gmf.runtime.notation.Node;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.gmf.tooling.runtime.update.UpdaterLinkDescriptor;

import pipeline.PipelinePackage;
import pipeline.diagram.edit.parts.DataManagementElementEditPart;
import pipeline.diagram.edit.parts.FamilyElementEditPart;
import pipeline.diagram.edit.parts.FlowEditPart;
import pipeline.diagram.edit.parts.PipelineEditPart;
import pipeline.diagram.edit.parts.SinkEditPart;
import pipeline.diagram.edit.parts.SourceEditPart;
import pipeline.diagram.part.PipelineDiagramUpdater;
import pipeline.diagram.part.PipelineLinkDescriptor;
import pipeline.diagram.part.PipelineNodeDescriptor;
import pipeline.diagram.part.PipelineVisualIDRegistry;

/**
 * @generated
 */
public class PipelineCanonicalEditPolicy extends CanonicalEditPolicy {

    /**
     * @generated
     */
    protected void refreshOnActivate() {
        // Need to activate editpart children before invoking the canonical refresh for EditParts to add event listeners
        List<?> c = getHost().getChildren();
        for (int i = 0; i < c.size(); i++) {
            ((EditPart) c.get(i)).activate();
        }
        super.refreshOnActivate();
    }

    /**
     * @generated
     */
    protected EStructuralFeature getFeatureToSynchronize() {
        return PipelinePackage.eINSTANCE.getPipeline_Nodes();
    }

    /**
     * @generated
     */
    @SuppressWarnings("rawtypes")
    protected List getSemanticChildrenList() {
        View viewObject = (View) getHost().getModel();
        LinkedList<EObject> result = new LinkedList<EObject>();
        List<PipelineNodeDescriptor> childDescriptors = PipelineDiagramUpdater
                .getPipeline_1000SemanticChildren(viewObject);
        for (PipelineNodeDescriptor d : childDescriptors) {
            result.add(d.getModelElement());
        }
        return result;
    }

    /**
     * @generated
     */
    protected boolean isOrphaned(Collection<EObject> semanticChildren,
            final View view) {
        if (isShortcut(view)) {
            return PipelineDiagramUpdater.isShortcutOrphaned(view);
        }
        return isMyDiagramElement(view)
                && !semanticChildren.contains(view.getElement());
    }

    /**
     * @generated
     */
    private boolean isMyDiagramElement(View view) {
        int visualID = PipelineVisualIDRegistry.getVisualID(view);
        switch (visualID) {
        case FamilyElementEditPart.VISUAL_ID:
        case DataManagementElementEditPart.VISUAL_ID:
        case SourceEditPart.VISUAL_ID:
        case SinkEditPart.VISUAL_ID:
            return true;
        }
        return false;
    }

    /**
     * @generated
     */
    protected static boolean isShortcut(View view) {
        return view.getEAnnotation("Shortcut") != null; //$NON-NLS-1$
    }

    /**
     * @generated
     */
    protected void refreshSemantic() {
        if (resolveSemanticElement() == null) {
            return;
        }
        LinkedList<IAdaptable> createdViews = new LinkedList<IAdaptable>();
        List<PipelineNodeDescriptor> childDescriptors = PipelineDiagramUpdater
                .getPipeline_1000SemanticChildren((View) getHost().getModel());
        LinkedList<View> orphaned = new LinkedList<View>();
        // we care to check only views we recognize as ours and not shortcuts
        LinkedList<View> knownViewChildren = new LinkedList<View>();
        for (View v : getViewChildren()) {
            if (isShortcut(v)) {
                if (PipelineDiagramUpdater.isShortcutOrphaned(v)) {
                    orphaned.add(v);
                }
                continue;
            }
            if (isMyDiagramElement(v)) {
                knownViewChildren.add(v);
            }
        }
        // alternative to #cleanCanonicalSemanticChildren(getViewChildren(), semanticChildren)
        //
        // iteration happens over list of desired semantic elements, trying to find best matching View, while original CEP
        // iterates views, potentially losing view (size/bounds) information - i.e. if there are few views to reference same EObject, only last one 
        // to answer isOrphaned == true will be used for the domain element representation, see #cleanCanonicalSemanticChildren()
        for (Iterator<PipelineNodeDescriptor> descriptorsIterator = childDescriptors
                .iterator(); descriptorsIterator.hasNext();) {
            PipelineNodeDescriptor next = descriptorsIterator.next();
            String hint = PipelineVisualIDRegistry.getType(next.getVisualID());
            LinkedList<View> perfectMatch = new LinkedList<View>(); // both semanticElement and hint match that of NodeDescriptor
            for (View childView : getViewChildren()) {
                EObject semanticElement = childView.getElement();
                if (next.getModelElement().equals(semanticElement)) {
                    if (hint.equals(childView.getType())) {
                        perfectMatch.add(childView);
                        // actually, can stop iteration over view children here, but
                        // may want to use not the first view but last one as a 'real' match (the way original CEP does
                        // with its trick with viewToSemanticMap inside #cleanCanonicalSemanticChildren
                    }
                }
            }
            if (perfectMatch.size() > 0) {
                descriptorsIterator.remove(); // precise match found no need to create anything for the NodeDescriptor
                // use only one view (first or last?), keep rest as orphaned for further consideration
                knownViewChildren.remove(perfectMatch.getFirst());
            }
        }
        // those left in knownViewChildren are subject to removal - they are our diagram elements we didn't find match to,
        // or those we have potential matches to, and thus need to be recreated, preserving size/location information.
        orphaned.addAll(knownViewChildren);
        //
        ArrayList<CreateViewRequest.ViewDescriptor> viewDescriptors = new ArrayList<CreateViewRequest.ViewDescriptor>(
                childDescriptors.size());
        for (PipelineNodeDescriptor next : childDescriptors) {
            String hint = PipelineVisualIDRegistry.getType(next.getVisualID());
            IAdaptable elementAdapter = new CanonicalElementAdapter(
                    next.getModelElement(), hint);
            CreateViewRequest.ViewDescriptor descriptor = new CreateViewRequest.ViewDescriptor(
                    elementAdapter, Node.class, hint, ViewUtil.APPEND, false,
                    host().getDiagramPreferencesHint());
            viewDescriptors.add(descriptor);
        }

        boolean changed = deleteViews(orphaned.iterator());
        //
        CreateViewRequest request = getCreateViewRequest(viewDescriptors);
        Command cmd = getCreateViewCommand(request);
        if (cmd != null && cmd.canExecute()) {
            SetViewMutabilityCommand.makeMutable(
                    new EObjectAdapter(host().getNotationView())).execute();
            executeCommand(cmd);
            @SuppressWarnings("unchecked")
            List<IAdaptable> nl = (List<IAdaptable>) request.getNewObject();
            createdViews.addAll(nl);
        }
        if (changed || createdViews.size() > 0) {
            postProcessRefreshSemantic(createdViews);
        }

        Collection<IAdaptable> createdConnectionViews = refreshConnections();

        if (createdViews.size() > 1) {
            // perform a layout of the container
            DeferredLayoutCommand layoutCmd = new DeferredLayoutCommand(host()
                    .getEditingDomain(), createdViews, host());
            executeCommand(new ICommandProxy(layoutCmd));
        }

        createdViews.addAll(createdConnectionViews);

        makeViewsImmutable(createdViews);
    }

    /**
     * @generated
     */
    private Collection<IAdaptable> refreshConnections() {
        Domain2Notation domain2NotationMap = new Domain2Notation();
        Collection<PipelineLinkDescriptor> linkDescriptors = collectAllLinks(
                getDiagram(), domain2NotationMap);
        Collection existingLinks = new LinkedList(getDiagram().getEdges());
        for (Iterator linksIterator = existingLinks.iterator(); linksIterator
                .hasNext();) {
            Edge nextDiagramLink = (Edge) linksIterator.next();
            int diagramLinkVisualID = PipelineVisualIDRegistry
                    .getVisualID(nextDiagramLink);
            if (diagramLinkVisualID == -1) {
                if (nextDiagramLink.getSource() != null
                        && nextDiagramLink.getTarget() != null) {
                    linksIterator.remove();
                }
                continue;
            }
            EObject diagramLinkObject = nextDiagramLink.getElement();
            EObject diagramLinkSrc = nextDiagramLink.getSource().getElement();
            EObject diagramLinkDst = nextDiagramLink.getTarget().getElement();
            for (Iterator<PipelineLinkDescriptor> linkDescriptorsIterator = linkDescriptors
                    .iterator(); linkDescriptorsIterator.hasNext();) {
                PipelineLinkDescriptor nextLinkDescriptor = linkDescriptorsIterator
                        .next();
                if (diagramLinkObject == nextLinkDescriptor.getModelElement()
                        && diagramLinkSrc == nextLinkDescriptor.getSource()
                        && diagramLinkDst == nextLinkDescriptor
                                .getDestination()
                        && diagramLinkVisualID == nextLinkDescriptor
                                .getVisualID()) {
                    linksIterator.remove();
                    linkDescriptorsIterator.remove();
                    break;
                }
            }
        }
        deleteViews(existingLinks.iterator());
        return createConnections(linkDescriptors, domain2NotationMap);
    }

    /**
     * @generated
     */
    private Collection<PipelineLinkDescriptor> collectAllLinks(View view,
            Domain2Notation domain2NotationMap) {
        if (!PipelineEditPart.MODEL_ID.equals(PipelineVisualIDRegistry
                .getModelID(view))) {
            return Collections.emptyList();
        }
        LinkedList<PipelineLinkDescriptor> result = new LinkedList<PipelineLinkDescriptor>();
        switch (PipelineVisualIDRegistry.getVisualID(view)) {
        case PipelineEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getPipeline_1000ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        case FamilyElementEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getFamilyElement_2005ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        case DataManagementElementEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getDataManagementElement_2006ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        case SourceEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getSource_2001ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        case SinkEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getSink_2002ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        case FlowEditPart.VISUAL_ID: {
            if (!domain2NotationMap.containsKey(view.getElement())) {
                result.addAll(PipelineDiagramUpdater
                        .getFlow_4001ContainedLinks(view));
            }
            domain2NotationMap.putView(view.getElement(), view);
            break;
        }
        }
        for (Iterator children = view.getChildren().iterator(); children
                .hasNext();) {
            result.addAll(collectAllLinks((View) children.next(),
                    domain2NotationMap));
        }
        for (Iterator edges = view.getSourceEdges().iterator(); edges.hasNext();) {
            result.addAll(collectAllLinks((View) edges.next(),
                    domain2NotationMap));
        }
        return result;
    }

    /**
     * @generated
     */
    private Collection<IAdaptable> createConnections(
            Collection<PipelineLinkDescriptor> linkDescriptors,
            Domain2Notation domain2NotationMap) {
        LinkedList<IAdaptable> adapters = new LinkedList<IAdaptable>();
        for (PipelineLinkDescriptor nextLinkDescriptor : linkDescriptors) {
            EditPart sourceEditPart = getSourceEditPart(nextLinkDescriptor,
                    domain2NotationMap);
            EditPart targetEditPart = getTargetEditPart(nextLinkDescriptor,
                    domain2NotationMap);
            if (sourceEditPart == null || targetEditPart == null) {
                continue;
            }
            CreateConnectionViewRequest.ConnectionViewDescriptor descriptor = new CreateConnectionViewRequest.ConnectionViewDescriptor(
                    nextLinkDescriptor.getSemanticAdapter(),
                    PipelineVisualIDRegistry.getType(nextLinkDescriptor
                            .getVisualID()), ViewUtil.APPEND, false,
                    ((IGraphicalEditPart) getHost())
                            .getDiagramPreferencesHint());
            CreateConnectionViewRequest ccr = new CreateConnectionViewRequest(
                    descriptor);
            ccr.setType(RequestConstants.REQ_CONNECTION_START);
            ccr.setSourceEditPart(sourceEditPart);
            sourceEditPart.getCommand(ccr);
            ccr.setTargetEditPart(targetEditPart);
            ccr.setType(RequestConstants.REQ_CONNECTION_END);
            Command cmd = targetEditPart.getCommand(ccr);
            if (cmd != null && cmd.canExecute()) {
                executeCommand(cmd);
                IAdaptable viewAdapter = (IAdaptable) ccr.getNewObject();
                if (viewAdapter != null) {
                    adapters.add(viewAdapter);
                }
            }
        }
        return adapters;
    }

    /**
     * @generated
     */
    private EditPart getEditPart(EObject domainModelElement,
            Domain2Notation domain2NotationMap) {
        View view = (View) domain2NotationMap.get(domainModelElement);
        if (view != null) {
            return (EditPart) getHost().getViewer().getEditPartRegistry()
                    .get(view);
        }
        return null;
    }

    /**
     * @generated
     */
    private Diagram getDiagram() {
        return ((View) getHost().getModel()).getDiagram();
    }

    /**
     * @generated
     */
    private EditPart getSourceEditPart(UpdaterLinkDescriptor descriptor,
            Domain2Notation domain2NotationMap) {
        return getEditPart(descriptor.getSource(), domain2NotationMap);
    }

    /**
     * @generated
     */
    private EditPart getTargetEditPart(UpdaterLinkDescriptor descriptor,
            Domain2Notation domain2NotationMap) {
        return getEditPart(descriptor.getDestination(), domain2NotationMap);
    }

    /**
     * @generated
     */
    protected final EditPart getHintedEditPart(EObject domainModelElement,
            Domain2Notation domain2NotationMap, int hintVisualId) {
        View view = (View) domain2NotationMap.getHinted(domainModelElement,
                PipelineVisualIDRegistry.getType(hintVisualId));
        if (view != null) {
            return (EditPart) getHost().getViewer().getEditPartRegistry()
                    .get(view);
        }
        return null;
    }

    /**
     * @generated
     */
    @SuppressWarnings("serial")
    protected static class Domain2Notation extends HashMap<EObject, View> {
        /**
         * @generated
         */
        public boolean containsDomainElement(EObject domainElement) {
            return this.containsKey(domainElement);
        }

        /**
         * @generated
         */
        public View getHinted(EObject domainEObject, String hint) {
            return this.get(domainEObject);
        }

        /**
         * @generated
         */
        public void putView(EObject domainElement, View view) {
            if (!containsKey(view.getElement()) || !isShortcut(view)) {
                this.put(domainElement, view);
            }
        }

    }
}
