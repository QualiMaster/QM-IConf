package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.gmf.tooling.runtime.sheet.DefaultPropertySection;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.IPropertySourceProvider;

/**
 * Implements the property section in order to enable the use of specific cell editors
 * in the properties view. However, the pipeline property section class of the generated
 * editor is not visible so we overwrite the only method there {@link #transformSelection(Object)}
 * in the same way in this class.
 * 
 * @author Holger Eichelberger
 */
public class IvmlPipelinePropertySection extends DefaultPropertySection implements IPropertySourceProvider {
    
    @Override
    protected Object transformSelection(Object selected) {
        selected = /*super.*/transformSelectionToDomain(selected);
        return selected;
    }
    
    @Override
    public IPropertySource getPropertySource(Object object) {
        // basically taken from DefaultPropertySource, but now returns IvmlPropertySource
        IPropertySource result = null;
        if (object instanceof IPropertySource) {
            result =  (IPropertySource) object;
        } else {
            AdapterFactory af = getAdapterFactory(object);
            if (af != null) {
                IItemPropertySource ips = (IItemPropertySource) af.adapt(object, IItemPropertySource.class);
                if (ips != null) {
                    result = new IvmlPropertySource(object, ips);
                }
            }
        }
        if (null == result) {
            result = super.getPropertySource(object);
        }
        return result;
    }

}
