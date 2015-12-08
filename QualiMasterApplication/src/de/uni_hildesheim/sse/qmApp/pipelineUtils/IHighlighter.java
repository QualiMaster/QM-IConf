package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import org.eclipse.emf.ecore.EObject;

/**
 * Interface for {@link Highlighter}, which is resppnsible for highlighting specific elements in
 * the {@link PipelineDiagramEditor}.
 * 
 * @author Niko
 */
public interface IHighlighter {

    /**
     * Method for highlighting diagram-elements.
     * @param element element which will be highlighted.
     * @param parameters Parameters which are used to highlight diagram-elements.
     */
    void highlight(EObject element, HighlighterParam parameters);
    
}
