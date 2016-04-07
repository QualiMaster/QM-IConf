package pipeline.diagram.providers;

import pipeline.diagram.part.PipelineDiagramEditorPlugin;

/**
 * @generated
 */
public class ElementInitializers {

	protected ElementInitializers() {
		// use #getInstance to access cached instance
	}

	/**
	 * @generated
	 */
	public static ElementInitializers getInstance() {
		ElementInitializers cached = PipelineDiagramEditorPlugin.getInstance()
				.getElementInitializers();
		if (cached == null) {
			PipelineDiagramEditorPlugin.getInstance().setElementInitializers(
					cached = new ElementInitializers());
		}
		return cached;
	}
}
