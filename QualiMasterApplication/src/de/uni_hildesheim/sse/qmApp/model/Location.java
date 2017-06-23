package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;

import de.uni_hildesheim.sse.qmApp.dialogs.EclipsePrefUtils;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;

/**
 * This utility class gets the model and the source location from the
 * conf.properties file.
 * 
 * @author Sass
 * @author El-Sharkawy
 */
public class Location {
    
    private static String modelLocation = ConfigurationProperties.MODEL_LOCATION.getValue(); 
    
    private static String sourceLocation = ConfigurationProperties.SOURCE_LOCATION.getValue();
    
    private static String instantiationLocation = EclipsePrefUtils.INSTANCE.getPreference(
        EclipsePrefUtils.LAST_INSTANTIATION_FOLDER_KEY);
    
    /**
     * Determines the folder used last time for instantiation. This uses the following mechanisms to determine
     * the folder:
     * <ol>
     *   <li>{@link EclipsePrefUtils#getPreference(String)}. Needs that the folder is saved via the
     *   {@link EclipsePrefUtils#LAST_INSTANTIATION_FOLDER_KEY} key.</li>
     *   <li>{@link #getModelLocation()}</li>
     * </ol>
     * @return A possible suggestion where to find old instantiated files or where to instantiate next time.
     * @see #hasInstantiated()
     */
    public static File getInstantiationFolder() {
        File result = null;
        
        // Default: Try to retrieve last instantiation folder from EclipsePrefUtils
        if (null != instantiationLocation) {
            result = new File(instantiationLocation);
        }
        
        // Fallback if there was no (valid) folder saved: Use model location
        if ((result == null || !result.exists()) && null != modelLocation) {
            result = getModelLocationFile();
        }
        
        return result;
    }
    
    /**
     * Checks whether at least at one time the model was instantiated.
     * @return <tt>true</tt> {@link #getInstantiationFolder()} returns the folder where last time elements where
     * instantiated in, <tt>false</tt>{@link #getInstantiationFolder()} is equal to {@link #getModelLocation()}.
     */
    public static boolean hasInstantiated() {
        File modelFolder = getModelLocationFile();
        return null != modelFolder && modelFolder != getInstantiationFolder(); 
    }
    
    /**
     * Getter for the model location. The model location will be loaded from
     * the conf.properties file.
     * 
     * @return  The location of the model as {@link File}
     */
    public static File getModelLocationFile() {
        return new File(null == modelLocation ? "" : modelLocation);
    }
    
    /**
     * Getter for the model location. The model location will be loaded from
     * the conf.properties file.
     * 
     * @return  The location of the model as {@link String}
     */
    public static String getModelLocation() {
        return modelLocation;
    }

    /**
     * Setter for the model location.
     * 
     * @param modelLocation The location of the model as {@link String}
     */
    public static void setModelLocation(String modelLocation) {
        Location.modelLocation = modelLocation;
    }

    /**
     * Getter for the source location. The source location will be loaded from
     * the conf.properties.file.
     * 
     * @return  The source location as {@link String}
     */
    public static String getSourceLocation() {
        return sourceLocation;
    }

    /**
     * Setter for the source location.
     * 
     * @param sourceLocation    The source location as {@link String}
     */
    public static void setSourceLocation(String sourceLocation) {
        Location.sourceLocation = sourceLocation;
    } 

}
