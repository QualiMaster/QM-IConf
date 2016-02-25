package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.swt.graphics.Image;

/**
 * A fallback image provider called for obtaining a default image if none
 * can be inferred for the specific property. This allows deferring the related
 * operations until they are actually required.
 * 
 * @author Holger Eichelberger
 */
public interface IFallbackImageProvider {

    /**
     * Returns the (default) image.
     * 
     * @return the default image
     */
    public Image getImage();
    
}
