package de.uni_hildesheim.sse.qmApp.pipelineUtils;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.notation.impl.ConnectorImpl;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils.ConnectorWrapper;
import pipeline.PipelineElement;
import pipeline.impl.FlowImpl;

/**
 * Gets a ResourceSetChangeEvent and calls the suited method of {@link IPipelineEditorListener} according to the event.
 * ->node added/removed
 * ->flow added/removed.
 * 
 * @author Niko Nowatzki
 */
public class PipelineEditorListener {

    private static final String NODE_IDENTIFIER = "name: nodes";
    private static final String FLOW_IDENTIFIER = "name: flows";
    private static IPipelineEditorListener listener = NullPipelineEditorListener.INSTANCE;
    
    /**
     * Constructs an object which listens on the model changes and calls suited methods in order to react properly.
     * @param event Event that indicates a model-change like a added or removed node or flow.
     */
    public PipelineEditorListener(ResourceSetChangeEvent event) {
        for (Iterator<?> iter = event.getNotifications().iterator(); iter.hasNext();) {
            Notification notification = (Notification) iter.next();
            Object notifier = notification.getNotifier();
            
            if (notifier instanceof EObject) {
                EObject eObject = (EObject) notifier;
        
                EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
                
                // only respond to changes to structural features of the object
                if (feature instanceof EStructuralFeature) {
                    if (notification.getFeature().toString().contains(NODE_IDENTIFIER)) {       
                        if (Integer.compare(notification.getEventType(), Notification.REMOVE) == 0) {
                            listener.nodeRemoved(feature.getName());
                        }
                        if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {
                            listener.nodeAdded(feature.getName());
                        }
                    }
                    if (notification.getFeature().toString().contains(FLOW_IDENTIFIER)) {
                        if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {

                            FlowImpl newFlow = (FlowImpl) notification.getNewValue();
                            
                            PipelineElement node1 = newFlow.getSource();
                            PipelineElement node2 = newFlow.getDestination();
                            
                            String node1Name = node1.getName();
                            String node2Name = node2.getName();
                            
                            listener.flowAdded(node1Name, node2Name);
                        }
                    }
                    if (eObject instanceof ConnectorImpl && Integer.compare(notification.getEventType(),
                         Notification.UNSET) == 0) {
                    
                        //Find the flow which is missing now in list connections
                        List<ConnectorWrapper> connections = PipelineDiagramUtils.getConnectionInfoList();
                        
                        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench()
                                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                        
                        EObject element = diagram.getDiagram().getElement();
                        EList<EObject> eContents = element.eContents();
                        
                        for (int i = 0; i < connections.size(); i++) {
                            ConnectorWrapper wrapper = connections.get(i);
                            FlowImpl flow = wrapper.getFlow();
   
                            if (!eContents.contains(flow)) {
                                //The right flow is found
                                    
                                String source = wrapper.getSource();
                                String target = wrapper.getTarget();
                                    
                                listener.flowRemoved(source, target);
                                break;
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Sets the listener to translate to.
     * 
     * @param li the pipeline listener (may be <b>null</b> for no listener)
     */
    public static void setListener(IPipelineEditorListener li) {
        if (null == li) {
            listener = NullPipelineEditorListener.INSTANCE;
        } else {
            listener = li;
        }
    }
    
}
