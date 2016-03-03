package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.notation.impl.DiagramImpl;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.model.PipelineTranslationException;
import de.uni_hildesheim.sse.qmApp.model.PipelineTranslationOperations;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import pipeline.Pipeline;
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
        IEditorInput editorURI = getEditorInput();
        IPersistableElement persitable = editorURI.getPersistable();
        URIEditorInput editorInput = (URIEditorInput) persitable;
        
        try {
            String partName = null;
            boolean ivmlSaved = false;
            Object content = getDiagramDocument().getContent();
            if (content instanceof DiagramImpl) { // intended - try writing IVML first; if successful write also diagram
                EObject elt = ((DiagramImpl) content).getElement();
                if (elt instanceof Pipeline) {
                    List<Pipeline> pipelines = new ArrayList<Pipeline>();
                    pipelines.add((Pipeline) elt);
                    partName = PipelineTranslationOperations.translationFromEcoregraphToIVMLfile(
                        editorInput.getURI(), pipelines);
                    super.doSave(progressMonitor);
                    ivmlSaved = true;
                }
            }
            if (!ivmlSaved) { // fallback - write diagram first
                super.doSave(progressMonitor);
                partName = PipelineTranslationOperations.translationFromEcoregraphToIVMLfile(editorInput.getURI());
            }
            if (null != partName) {
                setPartName(partName);
            }
        } catch (PipelineTranslationException e) {
            Dialogs.showErrorDialog("Translating pipeline into configuration", e.getMessage());
        }
    }

    @Override
    public boolean isEditable() {
        return VariabilityModel.isWritable(VariabilityModel.Configuration.PIPELINES);
    }
    
}
