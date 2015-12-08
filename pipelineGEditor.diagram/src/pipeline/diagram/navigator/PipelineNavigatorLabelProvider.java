/*
 * 
 */
package pipeline.diagram.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.common.ui.services.parser.IParser;
import org.eclipse.gmf.runtime.common.ui.services.parser.ParserOptions;
import org.eclipse.gmf.runtime.emf.core.util.EObjectAdapter;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ITreePathLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.ViewerLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

import pipeline.Pipeline;
import pipeline.diagram.edit.parts.DataManagementElementEditPart;
import pipeline.diagram.edit.parts.DataManagementElementNameEditPart;
import pipeline.diagram.edit.parts.FamilyElementEditPart;
import pipeline.diagram.edit.parts.FamilyElementNameEditPart;
import pipeline.diagram.edit.parts.FlowEditPart;
import pipeline.diagram.edit.parts.FlowNameEditPart;
import pipeline.diagram.edit.parts.PipelineEditPart;
import pipeline.diagram.edit.parts.SinkEditPart;
import pipeline.diagram.edit.parts.SinkNameEditPart;
import pipeline.diagram.edit.parts.SourceEditPart;
import pipeline.diagram.edit.parts.SourceNameEditPart;
import pipeline.diagram.part.PipelineDiagramEditorPlugin;
import pipeline.diagram.part.PipelineVisualIDRegistry;
import pipeline.diagram.providers.PipelineElementTypes;
import pipeline.diagram.providers.PipelineParserProvider;

/**
 * @generated
 */
public class PipelineNavigatorLabelProvider extends LabelProvider implements
        ICommonLabelProvider, ITreePathLabelProvider {

    /**
     * @generated
     */
    static {
        PipelineDiagramEditorPlugin
                .getInstance()
                .getImageRegistry()
                .put("Navigator?UnknownElement", ImageDescriptor.getMissingImageDescriptor()); //$NON-NLS-1$
        PipelineDiagramEditorPlugin
                .getInstance()
                .getImageRegistry()
                .put("Navigator?ImageNotFound", ImageDescriptor.getMissingImageDescriptor()); //$NON-NLS-1$
    }

    /**
     * @generated
     */
    public void updateLabel(ViewerLabel label, TreePath elementPath) {
        Object element = elementPath.getLastSegment();
        if (element instanceof PipelineNavigatorItem
                && !isOwnView(((PipelineNavigatorItem) element).getView())) {
            return;
        }
        label.setText(getText(element));
        label.setImage(getImage(element));
    }

    /**
     * @generated
     */
    public Image getImage(Object element) {
        if (element instanceof PipelineNavigatorGroup) {
            PipelineNavigatorGroup group = (PipelineNavigatorGroup) element;
            return PipelineDiagramEditorPlugin.getInstance().getBundledImage(
                    group.getIcon());
        }

        if (element instanceof PipelineNavigatorItem) {
            PipelineNavigatorItem navigatorItem = (PipelineNavigatorItem) element;
            if (!isOwnView(navigatorItem.getView())) {
                return super.getImage(element);
            }
            return getImage(navigatorItem.getView());
        }

        // Due to plugin.xml content will be called only for "own" views
        if (element instanceof IAdaptable) {
            View view = (View) ((IAdaptable) element).getAdapter(View.class);
            if (view != null && isOwnView(view)) {
                return getImage(view);
            }
        }

        return super.getImage(element);
    }

    /**
     * @generated
     */
    public Image getImage(View view) {
        switch (PipelineVisualIDRegistry.getVisualID(view)) {
        case PipelineEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?Diagram?pipeline?Pipeline", PipelineElementTypes.Pipeline_1000); //$NON-NLS-1$
        case SourceEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?TopLevelNode?pipeline?Source", PipelineElementTypes.Source_2001); //$NON-NLS-1$
        case SinkEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?TopLevelNode?pipeline?Sink", PipelineElementTypes.Sink_2002); //$NON-NLS-1$
        case FamilyElementEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?TopLevelNode?pipeline?FamilyElement", PipelineElementTypes.FamilyElement_2005); //$NON-NLS-1$
        case DataManagementElementEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?TopLevelNode?pipeline?DataManagementElement", PipelineElementTypes.DataManagementElement_2006); //$NON-NLS-1$
        case FlowEditPart.VISUAL_ID:
            return getImage(
                    "Navigator?Link?pipeline?Flow", PipelineElementTypes.Flow_4001); //$NON-NLS-1$
        }
        return getImage("Navigator?UnknownElement", null); //$NON-NLS-1$
    }

    /**
     * @generated
     */
    private Image getImage(String key, IElementType elementType) {
        ImageRegistry imageRegistry = PipelineDiagramEditorPlugin.getInstance()
                .getImageRegistry();
        Image image = imageRegistry.get(key);
        if (image == null && elementType != null
                && PipelineElementTypes.isKnownElementType(elementType)) {
            image = PipelineElementTypes.getImage(elementType);
            imageRegistry.put(key, image);
        }

        if (image == null) {
            image = imageRegistry.get("Navigator?ImageNotFound"); //$NON-NLS-1$
            imageRegistry.put(key, image);
        }
        return image;
    }

    /**
     * @generated
     */
    public String getText(Object element) {
        if (element instanceof PipelineNavigatorGroup) {
            PipelineNavigatorGroup group = (PipelineNavigatorGroup) element;
            return group.getGroupName();
        }

        if (element instanceof PipelineNavigatorItem) {
            PipelineNavigatorItem navigatorItem = (PipelineNavigatorItem) element;
            if (!isOwnView(navigatorItem.getView())) {
                return null;
            }
            return getText(navigatorItem.getView());
        }

        // Due to plugin.xml content will be called only for "own" views
        if (element instanceof IAdaptable) {
            View view = (View) ((IAdaptable) element).getAdapter(View.class);
            if (view != null && isOwnView(view)) {
                return getText(view);
            }
        }

        return super.getText(element);
    }

    /**
     * @generated
     */
    public String getText(View view) {
        if (view.getElement() != null && view.getElement().eIsProxy()) {
            return getUnresolvedDomainElementProxyText(view);
        }
        switch (PipelineVisualIDRegistry.getVisualID(view)) {
        case PipelineEditPart.VISUAL_ID:
            return getPipeline_1000Text(view);
        case SourceEditPart.VISUAL_ID:
            return getSource_2001Text(view);
        case SinkEditPart.VISUAL_ID:
            return getSink_2002Text(view);
        case FamilyElementEditPart.VISUAL_ID:
            return getFamilyElement_2005Text(view);
        case DataManagementElementEditPart.VISUAL_ID:
            return getDataManagementElement_2006Text(view);
        case FlowEditPart.VISUAL_ID:
            return getFlow_4001Text(view);
        }
        return getUnknownElementText(view);
    }

    /**
     * @generated
     */
    private String getPipeline_1000Text(View view) {
        Pipeline domainModelElement = (Pipeline) view.getElement();
        if (domainModelElement != null) {
            return domainModelElement.getName();
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "No domain element for view with visualID = " + 1000); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getSource_2001Text(View view) {
        IParser parser = PipelineParserProvider.getParser(
                PipelineElementTypes.Source_2001,
                view.getElement() != null ? view.getElement() : view,
                PipelineVisualIDRegistry.getType(SourceNameEditPart.VISUAL_ID));
        if (parser != null) {
            return parser.getPrintString(new EObjectAdapter(
                    view.getElement() != null ? view.getElement() : view),
                    ParserOptions.NONE.intValue());
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "Parser was not found for label " + 5001); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getSink_2002Text(View view) {
        IParser parser = PipelineParserProvider.getParser(
                PipelineElementTypes.Sink_2002,
                view.getElement() != null ? view.getElement() : view,
                PipelineVisualIDRegistry.getType(SinkNameEditPart.VISUAL_ID));
        if (parser != null) {
            return parser.getPrintString(new EObjectAdapter(
                    view.getElement() != null ? view.getElement() : view),
                    ParserOptions.NONE.intValue());
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "Parser was not found for label " + 5002); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getFamilyElement_2005Text(View view) {
        IParser parser = PipelineParserProvider.getParser(
                PipelineElementTypes.FamilyElement_2005,
                view.getElement() != null ? view.getElement() : view,
                PipelineVisualIDRegistry
                        .getType(FamilyElementNameEditPart.VISUAL_ID));
        if (parser != null) {
            return parser.getPrintString(new EObjectAdapter(
                    view.getElement() != null ? view.getElement() : view),
                    ParserOptions.NONE.intValue());
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "Parser was not found for label " + 5005); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getDataManagementElement_2006Text(View view) {
        IParser parser = PipelineParserProvider.getParser(
                PipelineElementTypes.DataManagementElement_2006, view
                        .getElement() != null ? view.getElement() : view,
                PipelineVisualIDRegistry
                        .getType(DataManagementElementNameEditPart.VISUAL_ID));
        if (parser != null) {
            return parser.getPrintString(new EObjectAdapter(
                    view.getElement() != null ? view.getElement() : view),
                    ParserOptions.NONE.intValue());
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "Parser was not found for label " + 5006); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getFlow_4001Text(View view) {
        IParser parser = PipelineParserProvider.getParser(
                PipelineElementTypes.Flow_4001,
                view.getElement() != null ? view.getElement() : view,
                PipelineVisualIDRegistry.getType(FlowNameEditPart.VISUAL_ID));
        if (parser != null) {
            return parser.getPrintString(new EObjectAdapter(
                    view.getElement() != null ? view.getElement() : view),
                    ParserOptions.NONE.intValue());
        } else {
            PipelineDiagramEditorPlugin.getInstance().logError(
                    "Parser was not found for label " + 6001); //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * @generated
     */
    private String getUnknownElementText(View view) {
        return "<UnknownElement Visual_ID = " + view.getType() + ">"; //$NON-NLS-1$  //$NON-NLS-2$
    }

    /**
     * @generated
     */
    private String getUnresolvedDomainElementProxyText(View view) {
        return "<Unresolved domain element Visual_ID = " + view.getType() + ">"; //$NON-NLS-1$  //$NON-NLS-2$
    }

    /**
     * @generated
     */
    public void init(ICommonContentExtensionSite aConfig) {
    }

    /**
     * @generated
     */
    public void restoreState(IMemento aMemento) {
    }

    /**
     * @generated
     */
    public void saveState(IMemento aMemento) {
    }

    /**
     * @generated
     */
    public String getDescription(Object anElement) {
        return null;
    }

    /**
     * @generated
     */
    private boolean isOwnView(View view) {
        return PipelineEditPart.MODEL_ID.equals(PipelineVisualIDRegistry
                .getModelID(view));
    }

}
