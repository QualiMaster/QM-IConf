package de.uni_hildesheim.sse.qmApp.editors;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.uni_hildesheim.sse.qmApp.model.PipelineTranslationOperations;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import pipeline.diagram.part.PipelineDiagramEditor;

/**
 * a specific pipeline editor for QM.
 * 
 * @author Cui Qin
 * 
 */
public class QMPipelineEditor extends PipelineDiagramEditor {
    public static final String ID = "de.uni_hildesheim.sse.qmApp.editors.PipelineDiagramEditorID"; //$NON-NLS-1$
    private static Map<String, String> pipelineNameAndDisplayname = new HashMap<String, String>();

    /**
     * Gets the map of the pipeline name and displayname.
     * @return a map
     */
    public static Map<String, String> getPipelineNameAndDisplayname() {
        return pipelineNameAndDisplayname;
    }

    @Override
    public void doSave(IProgressMonitor progressMonitor) {
        super.doSave(progressMonitor);

        IEditorInput editorURI = getEditorInput();
        IPersistableElement persitable = editorURI.getPersistable();
        URIEditorInput editorInput = (URIEditorInput) persitable;

        String partName = PipelineTranslationOperations.translationFromEcoregraphToIVMLfile(editorInput.getURI());
        if (null != partName) {
            setPartName(partName);
        }
    }

    @Override
    public boolean isEditable() {
        return VariabilityModel.isWritable(VariabilityModel.Configuration.PIPELINES);
    }
    
}
