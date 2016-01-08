package de.uni_hildesheim.sse.qmApp.pipelineUtils;
import java.util.Iterator;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.Connector;
import org.eclipse.gmf.runtime.notation.impl.ConnectorImpl;

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
                            //add nodes which are connected to the newly added flow to the listener
                            //listener.flowAdded(node1, node2);
                            System.out.println("flow added " + feature.getName()); // TODO from flow to nodes??
                            
                            ConnectorImpl connector = (ConnectorImpl) eObject;
                            Object object = connector.getSource();
                            
//                            FlowImpl flow = (FlowImpl) eObject;
//                            System.out.println(flow);
                            
                        }
                    }
                    if (eObject instanceof ConnectorImpl && Integer.compare(notification.getEventType(),
                         Notification.UNSET) == 0) {
                    
                        Connector connector = (Connector) eObject;
                        System.out.println(connector);
                    
                        Object object = connector.getSource();
                        System.out.println(object);
                    
                        ConnectorImpl connector2 = (ConnectorImpl) eObject;
                        Object object2 = connector2.getSource();
//                        ConnectorImpl connector = (ConnectorImpl) eObject;
//                      View source = connector.getSource();
//                      View target = connector.getTarget();
                    
                        //listener.flowRemoved(node1, node2);
                        System.out.println("flow removed " + feature.getName()); // TODO from flow to nodes??
                    }
                    // get the name of the changed feature and the qualified name of
                    //    the object, substituting <type> for any element that has no name
                    System.out.println("The " + feature.getName() + " of the object \""
                            + EMFCoreUtil.getQualifiedName(eObject, true) + "\" has changed.");
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
