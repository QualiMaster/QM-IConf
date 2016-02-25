/*
 * 
 */
package pipeline.diagram.providers.assistants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.diagram.ui.editparts.IGraphicalEditPart;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;

import pipeline.diagram.edit.parts.DataManagementElementEditPart;
import pipeline.diagram.edit.parts.FamilyElementEditPart;
import pipeline.diagram.edit.parts.SinkEditPart;
import pipeline.diagram.edit.parts.SourceEditPart;
import pipeline.diagram.providers.PipelineElementTypes;
import pipeline.diagram.providers.PipelineModelingAssistantProvider;

/**
 * @generated
 */
public class PipelineModelingAssistantProviderOfFamilyElementEditPart extends
        PipelineModelingAssistantProvider {

    /**
     * @generated
     */
    @Override
    public List<IElementType> getRelTypesOnSource(IAdaptable source) {
        IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
                .getAdapter(IGraphicalEditPart.class);
        return doGetRelTypesOnSource((FamilyElementEditPart) sourceEditPart);
    }

    /**
     * @generated
     */
    public List<IElementType> doGetRelTypesOnSource(FamilyElementEditPart source) {
        List<IElementType> types = new ArrayList<IElementType>(1);
        types.add(PipelineElementTypes.Flow_4001);
        return types;
    }

    /**
     * @generated
     */
    @Override
    public List<IElementType> getRelTypesOnSourceAndTarget(IAdaptable source,
            IAdaptable target) {
        IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
                .getAdapter(IGraphicalEditPart.class);
        IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
                .getAdapter(IGraphicalEditPart.class);
        return doGetRelTypesOnSourceAndTarget(
                (FamilyElementEditPart) sourceEditPart, targetEditPart);
    }

    /**
     * @generated
     */
    public List<IElementType> doGetRelTypesOnSourceAndTarget(
            FamilyElementEditPart source, IGraphicalEditPart targetEditPart) {
        List<IElementType> types = new LinkedList<IElementType>();
        if (targetEditPart instanceof FamilyElementEditPart) {
            types.add(PipelineElementTypes.Flow_4001);
        }
        if (targetEditPart instanceof DataManagementElementEditPart) {
            types.add(PipelineElementTypes.Flow_4001);
        }
        if (targetEditPart instanceof SourceEditPart) {
            types.add(PipelineElementTypes.Flow_4001);
        }
        if (targetEditPart instanceof SinkEditPart) {
            types.add(PipelineElementTypes.Flow_4001);
        }
        return types;
    }

    /**
     * @generated
     */
    @Override
    public List<IElementType> getTypesForTarget(IAdaptable source,
            IElementType relationshipType) {
        IGraphicalEditPart sourceEditPart = (IGraphicalEditPart) source
                .getAdapter(IGraphicalEditPart.class);
        return doGetTypesForTarget((FamilyElementEditPart) sourceEditPart,
                relationshipType);
    }

    /**
     * @generated
     */
    public List<IElementType> doGetTypesForTarget(FamilyElementEditPart source,
            IElementType relationshipType) {
        List<IElementType> types = new ArrayList<IElementType>();
        if (relationshipType == PipelineElementTypes.Flow_4001) {
            types.add(PipelineElementTypes.FamilyElement_2005);
            types.add(PipelineElementTypes.DataManagementElement_2006);
            types.add(PipelineElementTypes.Source_2001);
            types.add(PipelineElementTypes.Sink_2002);
        }
        return types;
    }

    /**
     * @generated
     */
    @Override
    public List<IElementType> getRelTypesOnTarget(IAdaptable target) {
        IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
                .getAdapter(IGraphicalEditPart.class);
        return doGetRelTypesOnTarget((FamilyElementEditPart) targetEditPart);
    }

    /**
     * @generated
     */
    public List<IElementType> doGetRelTypesOnTarget(FamilyElementEditPart target) {
        List<IElementType> types = new ArrayList<IElementType>(1);
        types.add(PipelineElementTypes.Flow_4001);
        return types;
    }

    /**
     * @generated
     */
    @Override
    public List<IElementType> getTypesForSource(IAdaptable target,
            IElementType relationshipType) {
        IGraphicalEditPart targetEditPart = (IGraphicalEditPart) target
                .getAdapter(IGraphicalEditPart.class);
        return doGetTypesForSource((FamilyElementEditPart) targetEditPart,
                relationshipType);
    }

    /**
     * @generated
     */
    public List<IElementType> doGetTypesForSource(FamilyElementEditPart target,
            IElementType relationshipType) {
        List<IElementType> types = new ArrayList<IElementType>();
        if (relationshipType == PipelineElementTypes.Flow_4001) {
            types.add(PipelineElementTypes.FamilyElement_2005);
            types.add(PipelineElementTypes.DataManagementElement_2006);
            types.add(PipelineElementTypes.Source_2001);
            types.add(PipelineElementTypes.Sink_2002);
        }
        return types;
    }

}
