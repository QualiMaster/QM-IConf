package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.ui.provider.PropertySource;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Implements the property source in order to create own property descriptors allowing the 
 * selection of the property editor according to the underlying IVML type.
 * 
 * @author Holger Eichelberger
 */
public class IvmlPropertySource extends PropertySource {
    
    /**
     * An instance is constructed from an object and its item property source.
     * 
     * @param object the object
     * @param itemPropertySource the property source
     */
    public IvmlPropertySource(Object object, IItemPropertySource itemPropertySource) {
        super(object, itemPropertySource);
    }

    @Override
    protected IPropertyDescriptor createPropertyDescriptor(IItemPropertyDescriptor itemPropertyDescriptor) {
        return new IvmlPropertyDescriptor(object, itemPropertyDescriptor);
    }

}
