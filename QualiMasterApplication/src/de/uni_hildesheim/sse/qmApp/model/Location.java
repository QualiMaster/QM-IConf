package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;

import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;

/**
 * This utility class gets the model and the source location from the
 * conf.properties file.
 * 
 * @author Sass
 *
 */
public class Location {
    
    private static String modelLocation = ConfigurationProperties.MODEL_LOCATION.getValue(); 
    
    private static String sourceLocation = ConfigurationProperties.SOURCE_LOCATION.getValue();

    /**
     * Getter for the model location. The model location will be loaded from
     * the conf.properties file.
     * 
     * @return  The location of the model as {@link File}
     */
    public static File getModelLocationFile() {
        return new File(modelLocation);
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
