package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * A creator for property editors to be registered in {@link PropertyEditorFactory}.
 * 
 * @author Holger Eichelberger
 */
public interface IPropertyEditorCreator {

    /**
     * Creates a property cell editor for the given property.
     * 
     * @param composite the parent composite
     * @param data the data object complying to {@link #reactsOn()}
     * @param propertyIdentifier an identifier specifying the property the editor shall be returned for 
     *   inside of <code>data</code>
     * @param fallback a fallback creator provided by the Eclipse framework (may be <b>null</b>)
     * @return the cell editor (or <b>null</b> if the default one shall be used)
     */
    public CellEditor createPropertyEditor(Composite composite, Object data, String propertyIdentifier, 
        IFallbackEditorCreator fallback);

    /**
     * Returns the display name for the given property.
     * 
     * @param data the data object complying to {@link #reactsOn()}
     * @param propertyIdentifier an identifier specifying the property the display name shall be returned for 
     *   inside of <code>data</code>
     * @return the display name (or <b>null</b> if the default one shall be used)
     */
    public String getDisplayName(Object data, String propertyIdentifier);

    /**
     * Returns the description for the given property.
     * 
     * @param data the data object complying to {@link #reactsOn()}
     * @param propertyIdentifier an identifier specifying the property the display name shall be returned for 
     *   inside of <code>data</code>
     * @return the description (or <b>null</b> if the default one shall be used)
     */
    public String getDescription(Object data, String propertyIdentifier);
    
    /**
     * The actual data class this creator reacts on (static information, must not change dynamically).
     * 
     * @return the class it reacts on (if <b>null</b> the creator cannot registered in {@link PropertyEditorFactory}
     */
    public Class<?> reactsOn();

    /**
     * Returns the label provider, i.e., the label describing the actual object value.
     * 
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the cell editor shall be returned for 
     *   inside of <code>data</code>
     * @param value the value to be set for <code>propertyIdentifier</code>
     * @param imageProvider an image provider for default images for the label to be created
     * @return the label provider or <b>null</b> if no label provider was created
     */
    public ILabelProvider getLabelProvider(Object data, String propertyIdentifier, Object value, 
        IFallbackImageProvider imageProvider);

}