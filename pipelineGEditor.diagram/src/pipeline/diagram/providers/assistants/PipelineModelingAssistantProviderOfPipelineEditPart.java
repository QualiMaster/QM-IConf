package pipeline.diagram.providers.assistants;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import pipeline.diagram.providers.PipelineElementTypes;
import pipeline.diagram.providers.PipelineModelingAssistantProvider;

/**
 * @generated
 */
public class PipelineModelingAssistantProviderOfPipelineEditPart extends
		PipelineModelingAssistantProvider {

	/**
	 * @generated
	 */
	@Override
	public List<IElementType> getTypesForPopupBar(IAdaptable host) {
		List<IElementType> types = new ArrayList<IElementType>(5);
		types.add(PipelineElementTypes.ReplaySink_2007);
		types.add(PipelineElementTypes.FamilyElement_2005);
		types.add(PipelineElementTypes.DataManagementElement_2006);
		types.add(PipelineElementTypes.Source_2001);
		types.add(PipelineElementTypes.Sink_2002);
		return types;
	}

}
