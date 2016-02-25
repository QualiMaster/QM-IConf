/*
 * 
 */
package pipeline.diagram.providers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.service.AbstractProvider;
import org.eclipse.gmf.runtime.common.core.service.IOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.GetParserOperation;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParserProvider;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserService;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.emf.ui.services.parser.ParserHintAdapter;
import org.eclipse.gmf.runtime.notation.View;

import pipeline.PipelinePackage;
import pipeline.diagram.edit.parts.DataManagementElementNameEditPart;
import pipeline.diagram.edit.parts.FamilyElementNameEditPart;
import pipeline.diagram.edit.parts.FlowNameEditPart;
import pipeline.diagram.edit.parts.SinkNameEditPart;
import pipeline.diagram.edit.parts.SourceNameEditPart;
import pipeline.diagram.parsers.MessageFormatParser;
import pipeline.diagram.part.PipelineVisualIDRegistry;

/**
 * @generated
 */
public class PipelineParserProvider extends AbstractProvider implements
        IParserProvider {

    /**
     * @generated
     */
    private IParser familyElementName_5005Parser;

    /**
     * @generated
     */
    private IParser getFamilyElementName_5005Parser() {
        if (familyElementName_5005Parser == null) {
            EAttribute[] features = new EAttribute[] { PipelinePackage.eINSTANCE
                    .getPipelineElement_Name() };
            MessageFormatParser parser = new MessageFormatParser(features);
            familyElementName_5005Parser = parser;
        }
        return familyElementName_5005Parser;
    }

    /**
     * @generated
     */
    private IParser dataManagementElementName_5006Parser;

    /**
     * @generated
     */
    private IParser getDataManagementElementName_5006Parser() {
        if (dataManagementElementName_5006Parser == null) {
            EAttribute[] features = new EAttribute[] { PipelinePackage.eINSTANCE
                    .getPipelineElement_Name() };
            MessageFormatParser parser = new MessageFormatParser(features);
            dataManagementElementName_5006Parser = parser;
        }
        return dataManagementElementName_5006Parser;
    }

    /**
     * @generated
     */
    private IParser sourceName_5001Parser;

    /**
     * @generated
     */
    private IParser getSourceName_5001Parser() {
        if (sourceName_5001Parser == null) {
            EAttribute[] features = new EAttribute[] { PipelinePackage.eINSTANCE
                    .getPipelineElement_Name() };
            MessageFormatParser parser = new MessageFormatParser(features);
            sourceName_5001Parser = parser;
        }
        return sourceName_5001Parser;
    }

    /**
     * @generated
     */
    private IParser sinkName_5002Parser;

    /**
     * @generated
     */
    private IParser getSinkName_5002Parser() {
        if (sinkName_5002Parser == null) {
            EAttribute[] features = new EAttribute[] { PipelinePackage.eINSTANCE
                    .getPipelineElement_Name() };
            MessageFormatParser parser = new MessageFormatParser(features);
            sinkName_5002Parser = parser;
        }
        return sinkName_5002Parser;
    }

    /**
     * @generated
     */
    private IParser flowName_6001Parser;

    /**
     * @generated
     */
    private IParser getFlowName_6001Parser() {
        if (flowName_6001Parser == null) {
            EAttribute[] features = new EAttribute[] { PipelinePackage.eINSTANCE
                    .getPipelineElement_Name() };
            MessageFormatParser parser = new MessageFormatParser(features);
            flowName_6001Parser = parser;
        }
        return flowName_6001Parser;
    }

    /**
     * @generated
     */
    protected IParser getParser(int visualID) {
        switch (visualID) {
        case FamilyElementNameEditPart.VISUAL_ID:
            return getFamilyElementName_5005Parser();
        case DataManagementElementNameEditPart.VISUAL_ID:
            return getDataManagementElementName_5006Parser();
        case SourceNameEditPart.VISUAL_ID:
            return getSourceName_5001Parser();
        case SinkNameEditPart.VISUAL_ID:
            return getSinkName_5002Parser();
        case FlowNameEditPart.VISUAL_ID:
            return getFlowName_6001Parser();
        }
        return null;
    }

    /**
     * Utility method that consults ParserService
     * @generated
     */
    public static IParser getParser(IElementType type, EObject object,
            String parserHint) {
        return ParserService.getInstance().getParser(
                new HintAdapter(type, object, parserHint));
    }

    /**
     * @generated
     */
    public IParser getParser(IAdaptable hint) {
        String vid = (String) hint.getAdapter(String.class);
        if (vid != null) {
            return getParser(PipelineVisualIDRegistry.getVisualID(vid));
        }
        View view = (View) hint.getAdapter(View.class);
        if (view != null) {
            return getParser(PipelineVisualIDRegistry.getVisualID(view));
        }
        return null;
    }

    /**
     * @generated
     */
    public boolean provides(IOperation operation) {
        if (operation instanceof GetParserOperation) {
            IAdaptable hint = ((GetParserOperation) operation).getHint();
            if (PipelineElementTypes.getElement(hint) == null) {
                return false;
            }
            return getParser(hint) != null;
        }
        return false;
    }

    /**
     * @generated
     */
    private static class HintAdapter extends ParserHintAdapter {

        /**
         * @generated
         */
        private final IElementType elementType;

        /**
         * @generated
         */
        public HintAdapter(IElementType type, EObject object, String parserHint) {
            super(object, parserHint);
            assert type != null;
            elementType = type;
        }

        /**
         * @generated
         */
        public Object getAdapter(Class adapter) {
            if (IElementType.class.equals(adapter)) {
                return elementType;
            }
            return super.getAdapter(adapter);
        }
    }

}
