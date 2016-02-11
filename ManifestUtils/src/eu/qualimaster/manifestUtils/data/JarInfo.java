package eu.qualimaster.manifestUtils.data;

import java.io.File;
import java.util.List;

/**
 * Contains all neccessary information regarding a single .jar file.
 * @author pastuschek
 *
 */
public class JarInfo {
    
    private File file;
    private List<Class<?>> classes;

    /**
     * Constructor for JarInfos with full information.
     * @param file The file (location) of the jar file.
     * @param classes A list of internal classes of the jar file.
     */
    public JarInfo(File file, List<Class<?>> classes) {
        this.file = file;
        this.classes = classes;
    }
    
    /**
     * Returns the file of the jar.
     * @return The file information of the jar. Can be null.
     */
    public File getFile() {
        return this.file;
    }
    
    /**
     * Returns all classes of the jar file.
     * @return A list of Class<?> inside the jar.
     */
    public List<Class<?>> getClasses() {
        return this.classes;
    }
    
}
