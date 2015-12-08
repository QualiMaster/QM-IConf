package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Class holding paramters for highlighting diagram-element.
 * @author Niko
 */
public class HighlighterParam {
    public static final HighlighterParam DEFAULT = new HighlighterParam();
    
    private Color diagramErrorColor = ColorConstants.red;

    /**
     * Constructor.
     */
    public HighlighterParam() {
    }

    /**
     * Constructor with given color.
     * @param diaramErrorColor color which will be used for the marking of diagram-elements.
     */
    public HighlighterParam(Color diaramErrorColor) {
        this.diagramErrorColor = diaramErrorColor;
    }
    
    /**
     * Get error color.
     * @return diagramErrorColor color which is used for marking errors in diagram.
     */
    public Color getDiagramErrorColor() {
        return diagramErrorColor;
    }
}
