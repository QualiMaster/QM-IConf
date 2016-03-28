package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;

import eu.qualimaster.easy.extension.internal.QmProjectDescriptor;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Implements a descriptor for the source and target VIL project locations.
 * The {@link #ProjectDescriptor() no-argument constructor}
 * obtains the relevant information for the model itself (i.e., the project
 * it is located in). The {@link #ProjectDescriptor(ProjectDescriptor, File) 
 * second constructor} allows to specify a different target location.
 * 
 * @author Holger Eichelberger
 */
public class ProjectDescriptor extends QmProjectDescriptor {

    /**
     * Creates the default project descriptor for the QM model to be instantiated.
     * 
     * @throws ModelManagementException in case that resolving the model, obtaining 
     *   information etc failed.
     */
    public ProjectDescriptor() throws ModelManagementException {
        super(Location.getModelLocationFile());
    }

    /**
     * Allows to instantiate the QM model into a given location.
     * 
     * @param parent the parent descriptor (also representing the source, to
     *   be obtained via {@link #ProjectDescriptor()})
     * @param base the folder to instantiate into
     */
    public ProjectDescriptor(ProjectDescriptor parent, File base) {
        super(parent, base);
    }

    /**
     * Returns the top-level QM configuration.
     * 
     * @return the top-level configuration to be used for instantiation
     */
    public Configuration getConfiguration() {
        return VariabilityModel.Definition.TOP_LEVEL.getConfiguration();
    }

}
