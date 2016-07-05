package eu.qualimaster.manifestUtils;

import org.apache.ivy.core.module.descriptor.DependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptorMediator;

/**
 * Mediates potential test dependencies and tags them in order to enable usage of multiple artifacts
 * with the same id but different classifiers.
 * @author pastuschek
 *
 */
public class TestMediator implements DependencyDescriptorMediator {

    @SuppressWarnings("unchecked")
    @Override
    public DependencyDescriptor mediate(DependencyDescriptor dd) {
        System.out.println("Mediating...");
        if (null != dd) {
            System.out.println("..." + dd.getDependencyId());
            if (dd.getDependencyId().toString().equals("Qualimaster.Events")) {
                dd.getAttributes().put("m:classifier", "tests");
            }
        }
        return dd;
    }

}
