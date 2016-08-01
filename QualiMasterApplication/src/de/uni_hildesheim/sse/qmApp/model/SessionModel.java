package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;

import de.uni_hildesheim.sse.qmApp.dialogs.EclipsePrefUtils;

/**
 * Stores temporary information about the current session, which shall not be saved permanently to
 * IVML/VIL.
 * @author El-Sharkawy
 *
 */
public class SessionModel {
    
    public static final SessionModel INSTANCE = new SessionModel();
    
    /**
     * Root folder, which was used for the instantiation of the QM-App.
     */
    private File instantiationTargetFolder = null;
    
    /**
     * Singleton constructor.
     */
    private SessionModel() { }

    /**
     * Sets the root folder of the instantiation process.
     * @param target The root folder where the pipelines are instantiated in.
     */
    public void setInstantiationFolder(String target) {
        File tmpFolder = null;
        try {
            tmpFolder = new File(target);
        } catch (NullPointerException npe) {
            // No action needed
        }
        
        if (null != tmpFolder && tmpFolder.exists() && tmpFolder.isDirectory()) {
            instantiationTargetFolder = tmpFolder;
            EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.LAST_INSTANTIATION_FOLDER_KEY, target);
        }
    }
    
    /**
     * Returns the root folder of the whole instantiation.
     * @return The root folder of the instantiation, or <tt>null</tt> if the instantiation
     * was not executed so far.
     * @see #wasInstantiated()
     * @see Location#DEFAULT_INSTANTIATION_FOLDER
     */
    public File getInstantationFolder() {
        return instantiationTargetFolder;
    }
    
    /**
     * Specifies whether the pipelines were already instantiated.
     * @return <tt>true</tt> if the pipelines were instantiated, <tt>false</tt> otherwise.
     * @see #getInstantationFolder()
     */
    public boolean wasInstantiated() {
        return null != instantiationTargetFolder && instantiationTargetFolder.exists()
            && instantiationTargetFolder.isDirectory();
    }
}
