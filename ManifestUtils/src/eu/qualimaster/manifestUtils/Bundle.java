package eu.qualimaster.manifestUtils;

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;

/**
 * An bundle descriptor, stores the ID from the manifest file and simplifies access to the logger.
 * @author El-Sharkawy
 *
 */
public class Bundle {
    
    public static final String PLUGIN_ID = "eu.qualimaster.ManifestUtils";
    
    /**
     * Returns the EASy logger for the specified class.
     * 
     * @param cls the class to return the logger for
     * @return the logger
     */
    public static EASyLogger getLogger(Class<?> cls) {
        return EASyLoggerFactory.INSTANCE.getLogger(cls, PLUGIN_ID);        
    }

}
