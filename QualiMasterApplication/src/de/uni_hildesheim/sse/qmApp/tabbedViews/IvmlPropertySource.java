package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public IPropertyDescriptor[] getPropertyDescriptors() {
        IPropertyDescriptor[] descriptors = super.getPropertyDescriptors();
        
        boolean filter = descriptors != null && descriptors.length > 0
            && descriptors[0] instanceof IvmlPropertyDescriptor
            && ((IvmlPropertyDescriptor) descriptors[0]).isFilterable();
        if (filter) {
            List<IPropertyDescriptor> descriptorList = new ArrayList<IPropertyDescriptor>();
            for (int i = 0; i < descriptors.length; i++) {
                if (!(descriptors[i] instanceof IvmlPropertyDescriptor)
                    || ((IvmlPropertyDescriptor) descriptors[i]).isVisible()) {
                    
                    descriptorList.add(descriptors[i]);
                }
            }
            descriptors = descriptorList.toArray(new IPropertyDescriptor[descriptorList.size()]);
        }
        
        return descriptors;
    }
}
