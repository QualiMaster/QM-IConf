package de.uni_hildesheim.sse.qmApp.pipelineUtils;
import java.util.Iterator;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eclipse.gmf.runtime.notation.impl.ConnectorImpl;

/**
 * Gets a ResourceSetChangeEvent and calls the suited method according to the event.
 * ->node added/removed
 * ->flow added/removed.
 * 
 * @author Niko Nowatzki
 */
public class PipelineEditorListener implements IPipelineEditorListener {

    private static final String NODE_IDENTIFIER = "name: nodes";
    private static final String FLOW_IDENTIFIER = "name: nodes";
    
    /**
     * Contrcuts a object which listens on the model changes and calls suited methods in order to react properly.
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
                            //listener.nodeRemoved(feature.getName());
                            System.out.println("node removed");
                        }
                        if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {
                            //listener.nodeAdded(feature.getName());
                            System.out.println("node added");
                        }
                    }
                    if (notification.getFeature().toString().contains(FLOW_IDENTIFIER)) {
                        if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {
                            //add nodes which are connected to the newly added flow to the listener
                            //listener.flowAdded();
                            System.out.println("flow added");
                        }
                    }
                    if (eObject instanceof ConnectorImpl && Integer.compare(notification.getEventType(),
                             Notification.UNSET) == 0) {
                            //listener.flowRemoved();
                        System.out.println("flow removed");
                    }
                    // get the name of the changed feature and the qualified name of
                    //    the object, substituting <type> for any element that has no name
                    System.out.println("The " + feature.getName() + " of the object \""
                            + EMFCoreUtil.getQualifiedName(eObject, true) + "\" has changed.");
                }
            }
        }
    }

    @Override
    public void nodeAdded(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public void nodeRemoved(String name) {
        // TODO Auto-generated method stub
    }

    @Override
    public void flowAdded(String node1, String node2) {
        // TODO Auto-generated method stub
    }

    @Override
    public void flowRemoved(String node1, String node2) {
        // TODO Auto-generated method stub
    }
}
