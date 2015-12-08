/*
 * 
 */
package pipeline.diagram.providers;

import org.eclipse.gmf.tooling.runtime.providers.DefaultEditPartProvider;

import pipeline.diagram.edit.parts.PipelineEditPart;
import pipeline.diagram.edit.parts.PipelineEditPartFactory;
import pipeline.diagram.part.PipelineVisualIDRegistry;

/**
 * @generated
 */
public class PipelineEditPartProvider extends DefaultEditPartProvider {

    /**
     * @generated
     */
    public PipelineEditPartProvider() {
        super(new PipelineEditPartFactory(),
                PipelineVisualIDRegistry.TYPED_INSTANCE,
                PipelineEditPart.MODEL_ID);
    }

}
