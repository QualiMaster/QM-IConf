package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.views.properties.tabbed.AbstractTabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISectionDescriptor;

/**
 * Implements a simple tab descriptor for tabbed (property) views.
 * 
 * @author Holger Eichelberger
 */
public class SimpleTabDescriptor extends AbstractTabDescriptor {

    private String category;
    private String id;
    private String label;

    /**
     * Creates a simple tab descriptor.
     * 
     * @param category the category name of the tab
     * @param id the tab identifier 
     * @param label the tab label
     * @param sectionDescriptors the section descriptors within the tab
     */
    public SimpleTabDescriptor(String category, String id, String label, 
        ISectionDescriptor... sectionDescriptors) {
        this.category = category;
        this.id = id;
        this.label = label;
        List<ISectionDescriptor> sDesc = new ArrayList<ISectionDescriptor>();
        for (int s = 0; s < sectionDescriptors.length; s++) {
            sDesc.add(sectionDescriptors[s]);
        }
        setSectionDescriptors(sDesc);
    }
    
    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getLabel() {
        return label;
    }
    
}