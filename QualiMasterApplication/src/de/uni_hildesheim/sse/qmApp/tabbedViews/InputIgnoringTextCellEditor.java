package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 * A text cell editor that ignores the input.
 * 
 * @author Holger Eichelberger
 */
public class InputIgnoringTextCellEditor extends TextCellEditor {

    private static Color backgroundDisabled;
    
    /**
     * Creates an editor instance.
     * 
     * @param parent the parent composite
     * @param reason the reason for ignoring the input (tooltip9
     */
    public InputIgnoringTextCellEditor(Composite parent, String reason) {
        super(parent);
        if (null == backgroundDisabled) {
            backgroundDisabled = new Color(Display.getCurrent(), 200, 200, 200);
        }
        Control control = getControl();
        control.setEnabled(false);
        if (null != reason && reason.length() > 0) {
            control.setToolTipText(reason);
        }
        control.setBackground(backgroundDisabled);
    }
    
    // disable events to prevent exceptions while shutdown
    
    @Override
    protected void doSetValue(Object value) {
        //ignore the input
    }

    @Override
    public void addListener(ICellEditorListener listener) {
        // do not notify
    }

    @Override
    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        // do not notify
    }
    
    @Override
    protected void fireApplyEditorValue() {
    }
    
    @Override
    protected void fireEditorValueChanged(final boolean oldValidState,
                    final boolean newValidState) {
    }
    
    @Override
    protected void fireEnablementChanged(final String actionId) {
    }
        
    @Override
    protected void markDirty() {
    }
    
    @Override
    protected Object doGetValue() {
        return null;
    }

}
