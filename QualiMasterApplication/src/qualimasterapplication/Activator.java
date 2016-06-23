package qualimasterapplication;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;

/**
 * The activator class controls the plug-in life cycle. 
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "QualiMasterApplication"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;
    private static BundleContext context;

    /**
     * The constructor.
     */
    public Activator() {
    }

    // checkstyle: stop exception type check
    @Override
    public void start(BundleContext contxt) throws Exception {
        super.start(contxt);
        plugin = this;
        context = contxt;
    }
    
    /**
     * Returns the bundle context.
     * 
     * @return the bundle context
     */
    public static BundleContext getContext() {
        return context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }
    
    //checkstyle: resume exception type check    

    /**
     * Returns the shared instance.
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path.
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }
    
    /**
     * Returns the EASy logger for the specified class.
     * 
     * @param cls the class to return the logger for
     * @return the logger
     */
    public static EASyLogger getLogger(Class<?> cls) {
        return EASyLoggerFactory.INSTANCE.getLogger(cls, PLUGIN_ID);        
    }
    
    /**
     * Returns the version of the plug-in as specified in its manifest.
     * @return The version in the following format: <tt>&lt;number&gt;.&lt;number&gt;.&lt;number&gt;</tt>
     */
    public static String getPluginVersion() {
        return getVersion(PLUGIN_ID);
    }
    
    /**
     * Returns the version of the product (RCP standalone application) as specified in its manifest.
     * @return The version in the following format: <tt>&lt;number&gt;.&lt;number&gt;.&lt;number&gt;</tt>
     */
    public static String getProductVersion() {
        return getVersion("de.uni-hildesheim.sse.qualiMasterApplication");
    }
    
    /**
     * Returns the version of the product and its main plugin as specified in their manifests.
     * @return The version in the following format:
     * <tt>&lt;number&gt;.&lt;number&gt;.&lt;number&gt; [&lt;number&gt;.&lt;number&gt;.&lt;number&gt;]</tt>
     */
    public static String getFullVersion() {
        String productVersion = getProductVersion();
        String pluginVersion = getPluginVersion();
        
        String version;
        if (!"<unknown version>".equals(productVersion)) {
            version = (productVersion != pluginVersion) ? productVersion + "[" + pluginVersion + "]" : productVersion;
        } else {
            version = pluginVersion;
        }
        
        return version;
    }
    
    /**
     * Returns the version of the specified bundle as specified in its manifest.
     * @param bundleID the ID of the bundle for which the version should be retrieved.
     * @return The version in the following format: <tt>&lt;number&gt;.&lt;number&gt;.&lt;number&gt;</tt>
     */
    private static String getVersion(String bundleID) {
        Bundle bundle = Platform.getBundle(bundleID);
        return (null != bundle) ? bundle.getVersion().toString() : "<unknown version>"; 
    }
    
}
