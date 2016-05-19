package qualimasterapplication;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
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
    
}
