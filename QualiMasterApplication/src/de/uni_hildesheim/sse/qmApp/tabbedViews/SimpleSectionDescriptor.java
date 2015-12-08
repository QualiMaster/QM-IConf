package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.AbstractSectionDescriptor;
import org.eclipse.ui.views.properties.tabbed.ISection;

/**
 * Implements a simple section descriptor.
 * 
 * @author Holger Eichelberger
 */
public class SimpleSectionDescriptor extends AbstractSectionDescriptor {
    
    /**
     * Represents all possible inputs.
     */
    public static final Class<?>[] ALL = {Object.class};
    
    private String id;
    private ISection sectionClass;
    private String targetTab;
    private Class<?>[] input;
    private IFilter filter;

    /**
     * Creates a simple section descriptor.
     * 
     * @param id the section identifier
     * @param sectionClass the class implementing the section
     * @param targetTab the target tab identifier
     * @param filter filtering the possible classes
     */
    public SimpleSectionDescriptor(String id, ISection sectionClass, String targetTab, IFilter filter) {
        this.id = id;
        this.sectionClass = sectionClass;
        this.targetTab = targetTab;
        this.filter = filter;
    }
    
    /**
     * Creates a simple section descriptor.
     * 
     * @param id the section identifier
     * @param sectionClass the class implementing the section
     * @param targetTab the target tab identifier
     * @param input the classes limiting the input (may be {@link #ALL})
     */
    public SimpleSectionDescriptor(String id, ISection sectionClass, String targetTab, Class<?>... input) {
        this.id = id;
        this.sectionClass = sectionClass;
        this.targetTab = targetTab;
        this.input = input;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public ISection getSectionClass() {
        return sectionClass;
    }

    @Override
    public String getTargetTab() {
        return targetTab;
    }

    @Override
    public boolean appliesTo(IWorkbenchPart part, ISelection selection) {
        boolean applies = false;
        Object[] sel = null;
        if (selection instanceof IStructuredSelection) {
            sel = ((IStructuredSelection) selection).toArray();
        }
        if (null != input) {
            for (int i = 0; !applies && i < input.length; i++) {
                applies = input[i].isInstance(part);
                if (null != sel) {
                    for (int s = 0; !applies && s < sel.length; s++) {
                        applies = input[i].isInstance(sel[s]);
                    }
                }
            }
        }
        if (null != filter) {
            applies |= filter.select(part);
            if (null != sel) {
                for (int s = 0; !applies && s < sel.length; s++) {
                    applies |= filter.select(sel[s]);
                }
            }
        }
        if (!applies) {
            applies = super.appliesTo(part, selection);
        }
        return applies;
    }
}