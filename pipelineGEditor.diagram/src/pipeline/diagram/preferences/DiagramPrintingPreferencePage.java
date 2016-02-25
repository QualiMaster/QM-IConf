/*
 * 
 */
package pipeline.diagram.preferences;

import org.eclipse.gmf.runtime.diagram.ui.preferences.PrintingPreferencePage;

import pipeline.diagram.part.PipelineDiagramEditorPlugin;

/**
 * @generated
 */
public class DiagramPrintingPreferencePage extends PrintingPreferencePage {

    /**
     * @generated
     */
    public DiagramPrintingPreferencePage() {
        setPreferenceStore(PipelineDiagramEditorPlugin.getInstance()
                .getPreferenceStore());
    }
}
