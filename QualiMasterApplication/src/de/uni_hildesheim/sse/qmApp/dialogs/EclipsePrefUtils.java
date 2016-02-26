package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;

import de.uni_hildesheim.sse.easy_producer.instantiator.Bundle;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * Utility class for the {@link IEclipsePreferences}. This class provides access to the preferences. Also it allows to
 * save and add preferences.
 * 
 * @author Sass
 *
 */
public class EclipsePrefUtils {
    
    
    public static final EclipsePrefUtils INSTANCE = new EclipsePrefUtils();
    
    public static final String LOCAL_DATA_PREF_KEY = "local-data";
    public static final String REPOSITORY_URL_PREF_KEY = "repository-url";
    public static final String USERNAME_PREF_KEY = "user-name";
    public static final String APP_CONFIGURED_PREF_KEY = "app-configured";
    public static final String MANUAL_CONFIGURED_PREF_KEY = "app-configured-manually";
    
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(EclipsePrefUtils.class, Bundle.ID);
    private IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Bundle.ID);
    
    /**
     * Private Singleton constructor, should avoid multiple instances.
     */
    private EclipsePrefUtils() {
        // Exists only to defeat instantiation.
    }

    /**
     * Returns the value associated with the specified key in this node. 
     * Returns the specified default if there is no value associated with the key. The default will always be null.
     * 
     * @param key the specified key
     * @return the value associated with the key or null if no value is associated
     */
    public String getPreference(String key) {
        return prefs.get(key, null);
    }
    
    /**
     * Adds a preference to the {@link IEclipsePreferences} and saves it.
     * 
     * @param preferenceName the name of the preference as {@link String}
     * @param value the value of the preference as {@link String}
     */
    public void addPreference(String preferenceName, String value) {
        prefs.put(preferenceName, value);
        savePreferences();
    }
    
    /**
     * Saves the preferences.
     */
    private void savePreferences() {
        try {
            // prefs are automatically flushed during a plugin's "super.stop()".
            prefs.flush();
        } catch (BackingStoreException e) {
            logger.exception(e);
        }
    }

}
