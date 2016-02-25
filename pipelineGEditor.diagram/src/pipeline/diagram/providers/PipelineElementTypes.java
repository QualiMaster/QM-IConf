/*
 * 
 */
package pipeline.diagram.providers;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.gmf.runtime.emf.type.core.ElementTypeRegistry;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.tooling.runtime.providers.DiagramElementTypeImages;
import org.eclipse.gmf.tooling.runtime.providers.DiagramElementTypes;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

import pipeline.PipelinePackage;
import pipeline.diagram.edit.parts.DataManagementElementEditPart;
import pipeline.diagram.edit.parts.FamilyElementEditPart;
import pipeline.diagram.edit.parts.FlowEditPart;
import pipeline.diagram.edit.parts.PipelineEditPart;
import pipeline.diagram.edit.parts.SinkEditPart;
import pipeline.diagram.edit.parts.SourceEditPart;
import pipeline.diagram.part.PipelineDiagramEditorPlugin;

/**
 * @generated
 */
public class PipelineElementTypes {

    /**
     * @generated
     */
    private PipelineElementTypes() {
    }

    /**
     * @generated
     */
    private static Map<IElementType, ENamedElement> elements;

    /**
     * @generated
     */
    private static DiagramElementTypeImages elementTypeImages = new DiagramElementTypeImages(
            PipelineDiagramEditorPlugin.getInstance()
                    .getItemProvidersAdapterFactory());

    /**
     * @generated
     */
    private static Set<IElementType> KNOWN_ELEMENT_TYPES;

    /**
     * @generated
     */
    public static final IElementType Pipeline_1000 = getElementType("pipelineGEditor.diagram.Pipeline_1000"); //$NON-NLS-1$
    /**
     * @generated
     */
    public static final IElementType FamilyElement_2005 = getElementType("pipelineGEditor.diagram.FamilyElement_2005"); //$NON-NLS-1$
    /**
     * @generated
     */
    public static final IElementType DataManagementElement_2006 = getElementType("pipelineGEditor.diagram.DataManagementElement_2006"); //$NON-NLS-1$
    /**
     * @generated
     */
    public static final IElementType Source_2001 = getElementType("pipelineGEditor.diagram.Source_2001"); //$NON-NLS-1$
    /**
     * @generated
     */
    public static final IElementType Sink_2002 = getElementType("pipelineGEditor.diagram.Sink_2002"); //$NON-NLS-1$
    /**
     * @generated
     */
    public static final IElementType Flow_4001 = getElementType("pipelineGEditor.diagram.Flow_4001"); //$NON-NLS-1$

    /**
     * @generated
     */
    public static ImageDescriptor getImageDescriptor(ENamedElement element) {
        return elementTypeImages.getImageDescriptor(element);
    }

    /**
     * @generated
     */
    public static Image getImage(ENamedElement element) {
        return elementTypeImages.getImage(element);
    }

    /**
     * @generated
     */
    public static ImageDescriptor getImageDescriptor(IAdaptable hint) {
        return getImageDescriptor(getElement(hint));
    }

    /**
     * @generated
     */
    public static Image getImage(IAdaptable hint) {
        return getImage(getElement(hint));
    }

    /**
     * Returns 'type' of the ecore object associated with the hint.
     * 
     * @generated
     */
    public static ENamedElement getElement(IAdaptable hint) {
        Object type = hint.getAdapter(IElementType.class);
        if (elements == null) {
            elements = new IdentityHashMap<IElementType, ENamedElement>();

            elements.put(Pipeline_1000, PipelinePackage.eINSTANCE.getPipeline());

            elements.put(FamilyElement_2005,
                    PipelinePackage.eINSTANCE.getFamilyElement());

            elements.put(DataManagementElement_2006,
                    PipelinePackage.eINSTANCE.getDataManagementElement());

            elements.put(Source_2001, PipelinePackage.eINSTANCE.getSource());

            elements.put(Sink_2002, PipelinePackage.eINSTANCE.getSink());

            elements.put(Flow_4001, PipelinePackage.eINSTANCE.getFlow());
        }
        return (ENamedElement) elements.get(type);
    }

    /**
     * @generated
     */
    private static IElementType getElementType(String id) {
        return ElementTypeRegistry.getInstance().getType(id);
    }

    /**
     * @generated
     */
    public static boolean isKnownElementType(IElementType elementType) {
        if (KNOWN_ELEMENT_TYPES == null) {
            KNOWN_ELEMENT_TYPES = new HashSet<IElementType>();
            KNOWN_ELEMENT_TYPES.add(Pipeline_1000);
            KNOWN_ELEMENT_TYPES.add(FamilyElement_2005);
            KNOWN_ELEMENT_TYPES.add(DataManagementElement_2006);
            KNOWN_ELEMENT_TYPES.add(Source_2001);
            KNOWN_ELEMENT_TYPES.add(Sink_2002);
            KNOWN_ELEMENT_TYPES.add(Flow_4001);
        }
        return KNOWN_ELEMENT_TYPES.contains(elementType);
    }

    /**
     * @generated
     */
    public static IElementType getElementType(int visualID) {
        switch (visualID) {
        case PipelineEditPart.VISUAL_ID:
            return Pipeline_1000;
        case FamilyElementEditPart.VISUAL_ID:
            return FamilyElement_2005;
        case DataManagementElementEditPart.VISUAL_ID:
            return DataManagementElement_2006;
        case SourceEditPart.VISUAL_ID:
            return Source_2001;
        case SinkEditPart.VISUAL_ID:
            return Sink_2002;
        case FlowEditPart.VISUAL_ID:
            return Flow_4001;
        }
        return null;
    }

    /**
     * @generated
     */
    public static final DiagramElementTypes TYPED_INSTANCE = new DiagramElementTypes(
            elementTypeImages) {

        /**
         * @generated
         */
        @Override
        public boolean isKnownElementType(IElementType elementType) {
            return pipeline.diagram.providers.PipelineElementTypes
                    .isKnownElementType(elementType);
        }

        /**
         * @generated
         */
        @Override
        public IElementType getElementTypeForVisualId(int visualID) {
            return pipeline.diagram.providers.PipelineElementTypes
                    .getElementType(visualID);
        }

        /**
         * @generated
         */
        @Override
        public ENamedElement getDefiningNamedElement(
                IAdaptable elementTypeAdapter) {
            return pipeline.diagram.providers.PipelineElementTypes
                    .getElement(elementTypeAdapter);
        }
    };

}
