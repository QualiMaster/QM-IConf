package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.ExtendedImageRegistry;
import org.eclipse.emf.edit.ui.provider.PropertyDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * Implements a property descriptor in order to adjust the property editor according to
 * the underlying IVML type.
 * 
 * @author Holger Eichelberger
 */
public class IvmlPropertyDescriptor extends PropertyDescriptor 
    implements IFallbackEditorCreator, IFallbackImageProvider {
    
    private static Color backgroundDisabled;
    
    /**
     * An instance is constructed from an object and its item property source.
     * 
     * @param object the object
     * @param itemPropertyDescriptor the property descriptor
     */
    public IvmlPropertyDescriptor(Object object, IItemPropertyDescriptor itemPropertyDescriptor) {
        super(object, itemPropertyDescriptor);
        if (null == backgroundDisabled) {
            backgroundDisabled = new Color(Display.getCurrent(), 200, 200, 200);
        }
    }

    /**
     * Returns the field identifier within the related object.
     * 
     * @return the field identifier
     */
    private String getFieldIdentifier() {
        return itemPropertyDescriptor.getId(object);
    }
    
    @Override
    public String getDisplayName() {
        String name = PropertyEditorFactory.getDisplayName(object, getFieldIdentifier());
        if (null == name) {
            name = super.getDisplayName();
        }
        return name;
    }
    
    @Override
    public String getDescription() {
        String description = PropertyEditorFactory.getDescription(object, getFieldIdentifier());
        if (null == description) {
            description = super.getDescription();
        }
        return description;
    }

    @Override
    public CellEditor createPropertyEditor(Composite composite) {
        CellEditor editor = PropertyEditorFactory.createPropertyEditor(composite, object, getFieldIdentifier(), this);
        if (null == editor) {
            editor = createFallbackPropertyEditor(composite);
        }
        return editor;
    }

    @Override
    public CellEditor createFallbackPropertyEditor(Composite composite) {
        return super.createPropertyEditor(composite);
    }
    
    @Override
    public ILabelProvider getLabelProvider() {
        Object value = itemPropertyDescriptor.getPropertyValue(object);
        if (value instanceof IItemPropertySource) {
            value = ((IItemPropertySource) value).getEditableValue(object);
        }
        ILabelProvider result = PropertyEditorFactory.getLabelProvider(object, getFieldIdentifier(), value, this);
        if (null == result) {
            result = super.getLabelProvider();
        }
        return result;
    }

    @Override
    public Image getImage() {
        IItemLabelProvider itemLabelProvider = itemPropertyDescriptor.getLabelProvider(object);
        return ExtendedImageRegistry.getInstance().getImage(itemLabelProvider.getImage(object));
    }
    
}
