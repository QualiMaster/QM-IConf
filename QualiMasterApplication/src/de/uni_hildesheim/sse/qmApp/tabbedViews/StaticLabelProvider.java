package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * A simple static label provider.
 * 
 * @author Holger Eichelberger
 */
public class StaticLabelProvider extends BaseLabelProvider implements ILabelProvider {
    private String labelText;
    private Image image;

    /**
     * Creates the label provider.
     * 
     * @param labelText the label text
     * @param image the label image
     */
    public StaticLabelProvider(String labelText, Image image) {
        this.labelText = labelText;
        this.image = image;
    }
    
    @Override
    public String getText(Object object) {
        return labelText;
    }

    @Override
    public Image getImage(Object object) {
        return image;
    }
}