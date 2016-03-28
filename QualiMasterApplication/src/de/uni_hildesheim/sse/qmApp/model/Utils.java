package de.uni_hildesheim.sse.qmApp.model;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;

/**
 * Some utilities.
 * 
 * @author Holger Eichelberger
 */
public class Utils {

    /**
     * Lists the known configuration properties.
     * 
     * @author Holger Eichelberger
     */
    public enum ConfigurationProperties {

        /**
         * Disable login capabilities and work on values provided
         * by <code>app.properties</code> and <code>conf.properties</code> (local).
         */
        DISABLE_LOGIN("disable-login"),
        
        /**
         * The actual model location. May be determined by the repository
         * connector or as fallback from <code>conf.properties</code> (local).
         */
        MODEL_LOCATION("model-location"),

        /**
         * The source location for importing artifacts into {@link #MODEL_LOCATION}. 
         * This is required to set up the Eclipse resources correctly.
         * May be specified in <code>conf.properties</code> (local).
         */
        SOURCE_LOCATION("source-location"),
        
        /**
         * Switch to enable the demo mode.
         */
        DEMO_MODE("demo-mode"),
        
        /**
         * The actual repository URL to be used by the repository connector.
         * Basically given in <code>app.properties</code> and may be overridden
         * for testing purposes in <code>conf.properties</code> (local).
         */
        REPOSITORY_URL("repository-url");
        
        private String key;
        
        /**
         * Creates a property constant.
         * 
         * @param key the property key
         */
        private ConfigurationProperties(String key) {
            this.key = key;
        }
        
        /**
         * Returns the value of the property.
         * 
         * @return the value of the property
         * @see Utils#getProperty(String)
         */
        public String getValue() {
            return getProperty(key);
        }
        
        /**
         * Returns the value of the property as a Boolean.
         * 
         * @return the boolean value
         */
        public boolean getBooleanValue() {
            return Boolean.valueOf(getValue());
        }
        
        /**
         * Stores a property.
         * @param value the value of the property
         */
        public void store(Object value) {
            Utils.addProperty(key, value);
        }
    }
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(Utils.class, "");
    
    private static Properties properties;
    
    /**
     * Prevents external creation. Utility class.
     */
    private Utils() {
    }

    /**
     * Adds all elements from <code>array</code> to <code>collection</code>.
     * 
     * @param <T> the element type
     * @param collection the receiving collection, to be modified as a side effect
     * @param array the array to take the values from
     * @return <code>collection</code>
     */
    public static <T> Collection<T> addAll(Collection<T> collection, T[] array) {
        for (int i = 0; i < array.length; i++) {
            collection.add(array[i]);
        }
        return collection;
    }

    /**
     * Imports files from <code>project</code> into project (file corresponds to <code>project</code>).
     * 
     * @param project the project to import the files from <code>file</code>
     * @param file the file or folder to be considered (recursively)
     * @param monitor a progress monitor
     */
    static void importFiles(IProject project, File file, IProgressMonitor monitor) {
        String base = "";
        if (null != file) {
            base = file.getAbsolutePath();
            if (!base.endsWith(File.separator)) {
                base += File.separator;
            }
            if (file.isDirectory()) {
                File[] list = file.listFiles();
                if (null != list) {
                    for (int f = 0; f < list.length; f++) {
                        importFilesImpl(project, base, list[f], monitor);
                    }
                }
            } else {
                importFilesImpl(project, base, file, monitor);
            }
        }
    }

    /**
     * Imports files from <code>project</code> into project (file corresponds to <code>project</code>).
     * 
     * @param base the base directory to make files relative to <code>project</code>
     * @param project the project to import the files from <code>file</code>
     * @param file the file or folder to be considered (recursively)
     * @param monitor a progress monitor
     */
    private static void importFilesImpl(IProject project, String base, File file, IProgressMonitor monitor) {
        String rel = file.getAbsolutePath();
        if (rel.startsWith(base)) {
            rel = rel.substring(base.length());
        }
        
        if (file.isDirectory()) {
            IFolder folder = project.getFolder(rel);
            if (!folder.exists()) {
                try {
                    folder.create(true, true, monitor);
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
            File[] list = file.listFiles();
            if (null != list) {
                for (int f = 0; f < list.length; f++) {
                    importFilesImpl(project, base, list[f], monitor);
                }
            } 
        } else {
            IFile eFile = project.getFile(rel);
            if (!eFile.exists()) {
                try {
                    InputStream fis = new BufferedInputStream(new FileInputStream(file));
                    eFile.create(fis, true, monitor);
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CoreException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }
    
    /**
     * Gets an application property (conf.properties or app.properties as fallback).
     * Please avoid using this method directly. Use {@link ConfigurationProperties} instead.
     * 
     * @param key Configuration properties key
     * @return The value of the key
     */
    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }
    
    /**
     * Loads properties from a properties file.
     * 
     * @param resourceName the resource name of the properties file
     */
    private static void loadProperties(String resourceName) {
        try {
            InputStream stream = Utils.class.getClassLoader().getResourceAsStream(resourceName);
            if (null != stream) {
                properties.load(stream);
                stream.close();
            }
        } catch (IOException e) {
            logger.exception(e);
            Dialogs.showErrorDialog("Problem reading the program configuration", e.getMessage());
        }
    }
    
    /**
     * Loads the conf.properties file.
     * 
     * @return  The Properties from the conf.properties file
     */
    private static synchronized Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            // first load the properties that are deployed with the application
            loadProperties("app.properties");
            // then load the optional (local) properties that may override the application properties
            loadProperties("conf.properties");
        }
        return properties;
    }
    
    /**
     * Gets the name of the project defined in the repository URL. The URL path
     * is separated and the last substring will indicate the name of the project.
     * 
     * @return the file in the workspace where the model should be stored
     */
    public static File getDestinationFileForModel() {
        // get object which represents the workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        // get location of workspace (java.io.File)
        String repositoryURL = UserContext.INSTANCE.getRepositoryURL();
        // Fallback
        String projectName = "";
        if (repositoryURL != null) {
            projectName = repositoryURL.substring(repositoryURL.lastIndexOf('/') + 1);
        } else {
            projectName = "QM2-repository";
        }
        return new File(workspace.getRoot().getLocation() + "/" + projectName);
    }
    
    /**
     * Gets the name of the project defined in the repository URL. The URL path
     * is separated and the last substring will indicate the name of the project.
     * @param repositoryURL The URL of the repository
     * @return the file in the workspace where the model should be stored
     */
    public static File getDestinationFileForModel(String repositoryURL) {
        // get object which represents the workspace
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        // get location of workspace (java.io.File)
        String projectName = repositoryURL.substring(repositoryURL.lastIndexOf('/') + 1);
        return new File(workspace.getRoot().getLocation() + "/" + projectName);
    }
    
    /**
     * Returns whether <code>var1</code> and <code>var2</code> are considered to be the same. Actually, we compare
     * just the qualified names in order to avoid instance problems with reloaded models. We do not use the similar
     * method defined in {@link AbstractVariable} as this performs an object-level type comparison.
     * 
     * @param var1 the first variable
     * @param var2 the second variable
     * @return <code>true</code> if both variables are considered to be equal, <code>false</code> else
     */
    private static boolean isSame(AbstractVariable var1, AbstractVariable var2) {
        return var1.getQualifiedName().equals(var2.getQualifiedName());
    }
    
    /**
     * Returns whether <code>variable</code> is equal to <code>other</code> or whether 
     * <code>variable</code> contains <code>other</code> (via their declarations!).
     * 
     * @param variable the first variable to check equality with
     * @param other the second variable to check equality with
     * @return <code>true</code> if whether <code>variable</code> is equal to <code>other</code> or whether 
     * <code>variable</code> contains <code>other</code>, <code>false</code> else
     */
    public static boolean equalsOrContains(IDecisionVariable variable, IDecisionVariable other) {
        AbstractVariable otherDecl = other.getDeclaration();
        boolean holds = isSame(otherDecl, variable.getDeclaration());
        if (!holds) {
            for (int e = 0; !holds && e < variable.getNestedElementsCount(); e++) {
                IDecisionVariable elt = net.ssehub.easy.varModel.confModel.Configuration.dereference(
                    variable.getNestedElement(e));
                holds = isSame(otherDecl, elt.getDeclaration());
            }
        }
        return holds;
    }

    /**
     * Returns whether <code>array</code> contains <code>value</code>.
     * 
     * @param <T> the element type
     * @param array the array to be searched
     * @param value the value to be contained
     * @return <code>true</code> if <code>array</code> contains <code>value</code>, <code>false</code> else
     */
    public static <T> boolean contains(T[] array, T value) {
        boolean contains;
        if (array == null || array.length == 0) {
            contains = false;
        } else {
            contains = false;
            for (int i = 0; !contains && i < array.length; i++) {
                T elt = array[i];
                if (null == elt) {
                    contains = value == null;
                } else {
                    contains = elt.equals(value);
                }
            }
        }
        return contains;
    }
    
    /**
     * Adds a property to the properties. Use with care!
     * @param key the key to be added
     * @param value the value of the key
     */
    private static void addProperty(Object key, Object value) {
        getProperties().put(key, value);
    }
    
}
