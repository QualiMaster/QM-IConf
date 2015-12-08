package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
import java.util.List;

import de.uni_hildesheim.sse.easy_producer.core.persistence.standard.PersistenceConstants;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.BuildModel;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.Script;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.vilTypes.IProjectDescriptor;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;

/**
 * Implements a descriptor for the source and target VIL project locations.
 * The {@link #ProjectDescriptor() no-argument constructor}
 * obtains the relevant information for the model itself (i.e., the project
 * it is located in). The {@link #ProjectDescriptor(ProjectDescriptor, File) 
 * second constructor} allows to specify a different target location.
 * 
 * @author Holger Eichelberger
 */
public class ProjectDescriptor implements IProjectDescriptor {

    private ProjectDescriptor parent;
    private File base;
    private Script vilScript;

    /**
     * Creates the default project descriptor for the QM model to be instantiated.
     * 
     * @throws ModelManagementException in case that resolving the model, obtaining 
     *   information etc failed.
     */
    public ProjectDescriptor() throws ModelManagementException {
        this.parent = null;
        this.base = Location.getModelLocationFile();

        BuildModel repository = BuildModel.INSTANCE;
        IModelPart topLevel = VariabilityModel.Definition.TOP_LEVEL;
        // by convention the same name, ignore versions for now
        List<ModelInfo<Script>> vilScripts = repository.availableModels().getModelInfo(topLevel.getModelName());
        if (null == vilScripts || vilScripts.isEmpty()) {
            throw new ModelManagementException("Cannot resolve main instantiation script", 
                ModelManagementException.ID_CANNOT_RESOLVE);
        } else {
            ModelInfo<Script> info = vilScripts.get(0); // primitive, ok for now
            vilScript = repository.load(info);
        }
    }

    /**
     * Allows to instantiate the QM model into a given location.
     * 
     * @param parent the parent descriptor (also representing the source, to
     *   be obtained via {@link #ProjectDescriptor()})
     * @param base the folder to instantiate into
     */
    public ProjectDescriptor(ProjectDescriptor parent, File base) {
        this.parent = parent;
        this.base = base;
        if (!base.exists()) {
            base.mkdirs();
        }
        this.vilScript = parent.getMainVilScript();
    }
    
    @Override
    public File getBase() {
        return base;
    }

    @Override
    public int getPredecessorCount() {
        return null != parent ? 1 : 0;
    }

    @Override
    public IProjectDescriptor getPredecessor(int index) {
        if (index < 0 || index >= getPredecessorCount()) {
            throw new IndexOutOfBoundsException();
        }
        return parent;
    }

    @Override
    public Script getMainVilScript() {
        return vilScript;
    }

    @Override
    public ProgressObserver createObserver() {
        return ProgressObserver.NO_OBSERVER; // TODO check, preliminary
    }

    @Override
    public String getModelFolder(ModelKind kind) {
        String result;
        switch (kind) {
        case IVML:
            result = PersistenceConstants.EASY_FILES_DEFAULT;
            break;
        case VIL:
            result = PersistenceConstants.EASY_FILES_DEFAULT;
            break;
        case VTL:
            result = PersistenceConstants.EASY_FILES_DEFAULT;
            break;
        default:
            result = null;
            break;
        }
        return result;
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
