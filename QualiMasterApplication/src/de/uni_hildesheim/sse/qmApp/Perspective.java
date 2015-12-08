package de.uni_hildesheim.sse.qmApp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * This class is responsible for providing the possibility to create several
 * layouts.
 * 
 */
public class Perspective implements IPerspectiveFactory {

    /**
     * Creates an Layout.
     * @param layout Bases on this {@link IPageLayout} the layout will be created.
     */
    public void createInitialLayout(IPageLayout layout) {
        layout.setEditorAreaVisible(true);
        //layout.setFixed(true);

        layout.addPerspectiveShortcut(ApplicationWorkbenchAdvisor.PERSPECTIVE_ID);
        IFolderLayout right = layout.createFolder(
            "right", IPageLayout.RIGHT, 0.8f, layout.getEditorArea()); //$NON-NLS-1$
        right.addView(IPageLayout.ID_OUTLINE);
        IFolderLayout bottomRight = layout.createFolder(
            "bottomRight", IPageLayout.BOTTOM, 0.6f, "right"); //$NON-NLS-1$     //$NON-NLS-2$
        bottomRight.addView(IPageLayout.ID_PROP_SHEET);
    }
}
