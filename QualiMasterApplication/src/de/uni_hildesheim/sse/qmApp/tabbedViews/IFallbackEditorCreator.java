package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

/**
 * May be called to install the original editor implementation in certain cases. 
 * This allows deferring the related
 * operations until they are actually required.
 * 
 * @author Holger Eichelberger
 */
public interface IFallbackEditorCreator {

    /**
     * Shall return the original cell property editor.
     * 
     * @param composite the parent composite
     * @return the cell editor
     */
    public CellEditor createFallbackPropertyEditor(Composite composite);

}
