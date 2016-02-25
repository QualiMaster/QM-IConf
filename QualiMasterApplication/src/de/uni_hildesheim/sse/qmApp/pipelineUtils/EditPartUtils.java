package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.Connection;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;

/**
 * Methods for manipulating diagrams.
 * 
 * @author Niko
 */
public class EditPartUtils {

    /**
     * Get {@link IGraphicalEditPart} editpart for given {@link EObject} diagram-element.
     * @param editPart Given editpart.
     * @param semanticElement given EObject.
     * @return graphical edit part for given element.
     */
    @SuppressWarnings("unchecked")
    public static IGraphicalEditPart findEditPartForSemanticElement(
                        EditPart editPart, EObject semanticElement) {
    
        if (semanticElement == null) {
            return null;
        }
        if (editPart instanceof IGraphicalEditPart) {
            EObject resolveSemanticElement = ((IGraphicalEditPart) editPart)
                    .resolveSemanticElement();

            if (resolveSemanticElement != null && EcoreUtil.getURI(
                    resolveSemanticElement).equals(EcoreUtil.getURI(semanticElement))) {
                return (IGraphicalEditPart) editPart;
            }
        }

        for (Object child : editPart.getChildren()) {
            IGraphicalEditPart recursiveEditPart = findEditPartForSemanticElement(
                                (EditPart) child, semanticElement);
            if (recursiveEditPart != null) {
                return recursiveEditPart;
            }
        }

        if (editPart instanceof NodeEditPart) {
            List<Connection> connections = new ArrayList<Connection>();
            
            connections.addAll(((NodeEditPart) editPart).getSourceConnections());
            connections.addAll(((NodeEditPart) editPart).getTargetConnections());
            
            for (Object connection : connections) {
                EObject resolveSemanticElement = ((IGraphicalEditPart) connection)
                        .resolveSemanticElement();
                if (EcoreUtil.equals(resolveSemanticElement, semanticElement)) {
                    return (IGraphicalEditPart) connection;
                }
            }
        }
        return null;
    }
}
