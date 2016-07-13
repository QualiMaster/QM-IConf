package eu.qualimaster.manifestUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Security;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.apache.ivy.Ivy;
import org.apache.ivy.ant.IvyConvertPom;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.DependencyDescriptorMediator;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.PatternMatcher;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.tools.ant.Project;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import eu.qualimaster.manifestUtils.PomReader.PomInfo;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.JarInfo;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.basics.progress.ProgressObserver.ITask;

/**
 * Builds and handles connections to the repository server.
 * @author pastuschek
 * @author El-Sharkawy
 */
public class ManifestConnection {

    private static final String ANNOTATION_CLASS = "eu.qualimaster.base.algorithm.DefaultValue"; 
    private static final String DEFAULT_VALUE_METHOD_NAME = "value";
    /**
     * Classes, which shall not be offered to the user.
     * <ul>
     *   <li>package-info: This is an artificial class used for JavaDoc only</li>
     * </ul>
     */
    private static final String CLASS_NAMES_TO_OMIT = "^.*package-info.*$";
    
    private static File ivyOut = null;
    
    private static List<String> repositories = new ArrayList<String>();
    
    private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(ManifestConnection.class, 
            "eu.qualimaster.ManifestUtils");
    private Ivy ivy = null;
    private File out = null;
    private String output = "";
    private Interceptor sysOut;
    private ResolveReport report = null;
    private String mainArtifactName = null;
    
    static {
        addDefaultRepositories();
    }
    
    /**
     * This class is used to intercept any output of the ivy component.
     * @author pastuschek
     *
     */
    private class Interceptor extends PrintStream {
        
        private String output;
        private ProgressObserver monitor = null;
        private ITask mainTask = null;
        private int prog = 1;
        private int max = 1;
        private PrintStream original;
        
        /**
         * Simple constructor.
         * @param output the output String.
         * @param original the original PrintStream.
         * @throws FileNotFoundException 
         */
        public Interceptor(String output, PrintStream original) throws FileNotFoundException {
            super(System.out, true);
            this.output = output;     
            this.original = original;
        }
        
        /**
         * Returns whether output should be processed or just stacked.
         * @return True if output should be processed, false otherwise.
         */
        private boolean isProcessing() {
            return (null != monitor && null != mainTask);
        }
        
        /**
         * The String to process.
         * @param string The String to process.
         */
        private void process(String string) {
            if (null != string) {
                if (string.contains("found") || string.contains("downloading")) {
                    if (prog < max) {
                        monitor.notifyProgress(mainTask, prog);
                        ProgressObserver.ISubtask subtask = monitor.registerSubtask(string);
                        monitor.notifyStart(mainTask, subtask, 1);
                        this.prog = this.prog + 1;
                    } else {
                        monitor.notifyProgress(mainTask, prog, max + 1);
                    }
                }
            }
        }
        
        /**
         * Redirect the intercepted output.
         */
        @Override
        public void print(String string) {
            this.output += (string);
            this.original.print(string);
            if (isProcessing()) {
                process(string);
            }
        }
        
        /**
         * Redirect the intercepted output.
         */
        @Override
        public void println(String string) {
            this.output += ("\n" + string);
            this.original.println(string);
            if (isProcessing()) {
                process(string);
            }
        }
        
        /**
         * Sets the maximum progess for the monitor.
         * @param max The maximum for the monitor.
         */
        public void setMax(int max) {
            this.max = max;
        }
        
        /**
         * Sets the monitor that will be updated by intercepted information.
         * @param monitor The monitor.
         */
        public void setMonitor(ProgressObserver monitor) {
            this.monitor = monitor;
        }
        
        /**
         * Sets the main task. Can be null.
         * @param task The current task, can be null.
         */
        public void setTask(ITask task) {
            this.mainTask = task;
            this.prog = 1;
        }
        
        /**
         * Returns the intercepted output.
         * @return The intercepted output String.
         */
        @SuppressWarnings("unused")
        public String getOutput() {
            String buffer = this.output;
            this.output = "";
            return buffer;
        }
        
    }
    
    /**
     * Simple constructor for a ManifestConnection.
     * Sets up needed resolver etc.
     */
    public ManifestConnection() {
        
        try {
            sysOut = new Interceptor(output, new PrintStream(new FileOutputStream(FileDescriptor.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        IvySettings ivySettings = new IvySettings();
        
        //Set the maven repository as default.
        String mavenPath = MavenUtils.mavenRepository();
        
        if (null == ivyOut) {
            setRetrievalFolder(new File(mavenPath + "/ivy"));

        }
        this.out = ivyOut;
        
        if (!this.out.exists()) {
            try {
                this.out.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        if (!this.out.exists()) {
            try {
                this.out.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        ivySettings.setDefaultCache(new File(mavenPath + "/ivy_cache"));
        ivySettings.setDefaultCacheArtifactPattern(
                "[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        ivySettings.setDefaultCacheIvyPattern(
                "[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        
        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("chainResolver");
        
        List<IBiblioResolver> resolvers = ManifestParserUtils.getResolver(repositories);
        for (IBiblioResolver resolver : resolvers) {
            chainResolver.add(resolver);
        }
        
        chainResolver.setCheckmodified(true);
        ivySettings.addResolver(chainResolver);
        ivySettings.setDefaultResolver(chainResolver.getName());
        this.ivy = Ivy.newInstance(ivySettings);
        
    }
    
    /**
     * Returns the underlying manifest (i.e the one that was provided).
     * @param artifactId the id of the artifact for which the manifest is required.
     * @return The underlying manifest, or null.
     * @throws ManifestUtilsException If the manifest has errors.
     */
    public Manifest getUnderlyingManifest(String artifactId) throws ManifestUtilsException {
        Manifest manifest = null;
        
        if (null != artifactId && extractManifest(artifactId)) {
            ManifestParser mp = new ManifestParser();
            manifest = mp.parseFile(new File(out + "/manifest.xml"));
        } 
        
        return manifest;
    }
    
    /**
     * Adds a Maven repository for artifact resolution.
     * 
     * @param repository the repository URL (<b>null</b> or empty is ignored) 
     */
    public static void addRepository(String repository) {
        addRepository(repository, repositories.size());
    }
    
    /**
     * Sets the retrieval folder where jars are retrieved to, even if they exist in the local Maven repos, 
     * due to bugs in the naming conventions by ivy/maven.
     * @param folder The desired folder.
     */
    public static void setRetrievalFolder(File folder) {
        ivyOut = folder;
        if (!ivyOut.exists()) {
            ivyOut.mkdirs();
        }
    }
    
    /**
     * Returns the number of actual repositories.
     * 
     * @return the number of repositories
     */
    public static int getRepositoriesCount() {
        return repositories.size();
    }
    
    /**
     * Clears all repositories.
     */
    public static void clearRepositories() {
        repositories.clear();
    }
    
    /**
     * Adds default Maven repositories.
     */
    public static void addDefaultRepositories() {
        repositories.add("http://repo1.maven.org/maven2/");
    }
    
    /**
     * Adds a Maven repository for artifact resolution.
     * 
     * @param repository the repository URL (<b>null</b> or empty is ignored)
     * @param index the index where to add the repository
     */
    public static void addRepository(String repository, int index) {
        if (null != repository && repository.length() > 0) {
            repositories.add(index, repository);
        }
    }

    /**
     * Removes a Maven repository from artifact resolution.
     * 
     * @param repository the repository URL (<b>null</b> or empty is ignored) 
     */
    public static void removeRepository(String repository) {
        if (null != repository && repository.length() > 0) {
            repositories.remove(repository);
        }
    }
    
    
    /**
     * Adds credentials to the credential store of ivy.
     * These credentials are used to access the nexus repository.
     * @param username The username.
     * @param password The password.
     */
    public static void addCredentials(String username, String password) {
        
        org.apache.ivy.util.url.CredentialsStore.INSTANCE
            .addCredentials("Sonatype Nexus Repository Manager", "nexus.sse.uni-hildesheim.de", 
                username, password);
        
    }
    
    /**
     * Creates an String, which can be used to access elements of an JAR archive, via creating an URL object.
     * @param repositoryLocation The location of the repository, which is currently used.
     * @param artifactID The name of the jar, which shall be accessed.
     * @return An platform independent URL for accessing elements inside the archive. Elements of the archive
     * must be appended to the end of this String.
     */
    private static String createJarAccessorURLString(File repositoryLocation, String artifactID) {
        String outPath = repositoryLocation.getAbsolutePath();
        String accessURL = (outPath.charAt(0) == '/') ? "jar:file:" : "jar:file:/";
        accessURL += outPath + "/" + artifactID + ".jar!";
        return accessURL;
    }
    
    /**
     * The main to test things out.
     * @param monitor The monitor tracking the current progress.
     * @param groupId the Id of the group.
     * @param artifactId the Id of the artifact.
     * @param version the target version of the artifact.
     */
    public void load(ProgressObserver monitor, String groupId, String artifactId, String version) {
        
        ITask mainTask = monitor.registerTask("Loading Dependencies");
        sysOut.setMonitor(monitor);
        
        System.setProperty("jsse.enableSNIExtension", "false");
        Security.insertProviderAt(new BouncyCastleProvider(), 1);      
        
        deleteDir(this.out);
        setRetrievalFolder(this.out);
        System.out.println("RETRIEVAL: " + this.out);  
        ResolveOptions ro = new ResolveOptions();

        ro.setResolveMode(ResolveOptions.RESOLVEMODE_DYNAMIC);
        ro.setTransitive(true);
        ro.setDownload(true);  
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            ModuleRevisionId.newInstance(groupId, artifactId + "-envelope", version)
        );
        ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, true);
        dd.addDependencyConfiguration("default", "compile");
        dd.addDependencyConfiguration("default", "default");
        dd.addDependencyConfiguration("default", "master");
        dd.addDependencyConfiguration("default", "runtime");
        dd.addDependencyConfiguration("default", "provided");
//        dd.addDependencyConfiguration("*", "*");
        md.addDependency(dd);
        ModuleId modId = new ModuleId("eu.qualimaster", "QualiMaster.Events");
        PatternMatcher pattMatch = new org.apache.ivy.plugins.matcher.ExactPatternMatcher();
        DependencyDescriptorMediator ddm = new TestMediator();
        md.addDependencyDescriptorMediator(modId, pattMatch, ddm);
        ModuleDescriptor m = null; 
        try {
            monitor.notifyStart(mainTask, 200);
            sysOut.setMax(200);
            sysOut.setTask(mainTask);
            
            System.setOut(sysOut);
            System.setErr(sysOut);
            report = ivy.resolve(md, ro);
            m = report.getModuleDescriptor();
            if (report.hasError()) {
                throw new RuntimeException(report.getAllProblemMessages().toString());
            }        
            
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ParseException exc) {
            exc.printStackTrace();
        }      
        try {
            ivy.retrieve(
                    m.getModuleRevisionId(),
                    out.getAbsolutePath() + "/[artifact](-[classifier]).[ext]",
                    new RetrieveOptions().setConfs(new String[]{"default"})
            ); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        ModuleRevisionId.newInstance(groupId, artifactId, version);
        PublishOptions options = new PublishOptions();
        options.setUpdate(false);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
        monitor.notifyEnd(mainTask);

    }
    
    /**
     * Returns a list of parameters for a given class.
     * @param name The name of the original class.
     * @param artifactId The id of the artifact.
     * @return A list with parameters for the class. Can be empty but never null.
     */
    public List<Parameter> getParameters(String name, String artifactId) {
        List<Parameter> result = new ArrayList<Parameter>();
        URLClassLoader loader = null;
        try {
            URL[] urls = new URL[1];
            loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls));         
            Class<?> base = loadClass(name, loader); 
            if (null != base) {
                Method[] methods = base.getMethods();
                for (Method m : methods) {
                    if (m.getName().startsWith("setParameter")) {
                        String pName = m.getName().substring("setParameter".length(), m.getName().length());
                        pName = lowerFirstLetter(pName); 
                        Parameter param = new Parameter(pName, ParameterType.valueOf(m.getParameterTypes()[0]));
                        if (null != m.getDefaultValue()) {
                            param.setValue(m.getDefaultValue().toString());
                        }
                        try {
                            
                            Class<?> defaultValueClass = loader.loadClass(ANNOTATION_CLASS);
                            @SuppressWarnings("unchecked")
                            Class<Annotation> defaultValueAnnotationClass = (Class) defaultValueClass.getClass();
                            Annotation annotation = m.getAnnotation(defaultValueAnnotationClass);
                            if (null != annotation) {
                                Method method = annotation.getClass().getMethod(DEFAULT_VALUE_METHOD_NAME);
                                Object[] args = null;
                                Object invokeResult = method.invoke(annotation, args);
                                String defaultValue = (String) invokeResult;
                                param.setValue(defaultValue);
                            }
                            
                        } catch (ClassNotFoundException e) {
                            System.out.println("ERROR: Unable to load Annotation Class: '" + ANNOTATION_CLASS + "'");
                            logger.info("ERROR: Unable to load Annotation Class: '" + ANNOTATION_CLASS + "'");
                            System.out.println("The used version of StormCommons does not support Annotations!");
                            logger.info("The used version of StormCommons does not support Annotations!");
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                            System.out.println("ERROR: Unable to cast: '" + ANNOTATION_CLASS + "' to Annotation.");
                        } catch (NoSuchMethodException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        result.add(param); 
                    } 
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (ManifestUtilsException e1) {
            e1.printStackTrace();
        } finally {
            try {
                loader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return result;
        
    }
    
    /**
     * Returns all input fields of the given artifact.
     * @param name The name of the original class.
     * @param artifactId The id of the artifact.
     * @return A list with all input fields. Can be empty but never null.
     * @throws ManifestUtilsException if in/output could not be read.
     */
    public List<Item> getInput(String name, String artifactId) throws ManifestUtilsException {
        return getInOut(name, artifactId, true);
    }
    
    /**
     * Returns all output fields of the given artifact.
     * @param name The name of the original class.
     * @param artifactId The id of the artifact.
     * @return A list with all output fields. Can be empty but never null.
     * @throws ManifestUtilsException if in/output could not be read.
     */
    public List<Item> getOutput(String name, String artifactId) throws ManifestUtilsException {
        return getInOut(name, artifactId, false);
    }
    
    /**
     * Loads the specified class via the given class loader.
     * @param name The (binary) name of the class to load.
     * @param loader The class loader to use (should make use of the downloaded JARs from {@link #out}.
     * @return The loaded class or <tt>null</tt> if the name is <tt>null</tt>.
     * @throws ClassNotFoundException If the class was not found
     * @throws ManifestUtilsException in case that the specified class could not be loaded due
     *     to missing dependent classes
     */
    private Class<?> loadClass(String name, URLClassLoader loader) throws ClassNotFoundException,
        ManifestUtilsException {
        
        Class<?> result = null;
        
        if (null != name && !name.isEmpty()) {

            try {
                result = loader.loadClass(name);
            } catch (NoClassDefFoundError noDefError) {
                // Short description
                StringBuffer titleMsg = new StringBuffer("Error expected class \"");
                titleMsg.append(name);
                titleMsg.append("\" could not be loaded.");
                String title = titleMsg.toString();
                
                // Detailed reason
                StringBuffer errMsg = new StringBuffer(title);
                errMsg.append("\" Reason: Class \"");
                errMsg.append(noDefError.getMessage());
                errMsg.append("\" was not found");
                if (null != out) {
                    errMsg.append(" at \"");
                    errMsg.append(out.getAbsolutePath());
                    errMsg.append("\".");
                } else {
                    errMsg.append(".");
                }
                String msg = errMsg.toString();
                logger.error(msg);
                throw new ManifestUtilsException(title, msg);
            }
        }
        
        return result;
    }
    
    /**
     * Returns the class of given name of the resolved dependency.
     * @param name The name of the class. Can be null?
     * @param artifactId the id of the artifact.
     * @param getInput true if input should be returned, false for output.
     * @return A list of fields for in/output.
     * @throws ManifestUtilsException if in/output could not be read.
     */
    private List<Item> getInOut(String name, String artifactId, boolean getInput) throws ManifestUtilsException {
        
        List<Item> result = new ArrayList<Item>();
        URLClassLoader loader = null;
        
        try {
            URL[] urls = new URL[1];
            loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls)); 
            Class<?> base = loadClass(name, loader);
            
            if (null != base) {
                Method[] methods = base.getMethods();      
                List<String> inOut = new ArrayList<String>();
                result = getAlgorithmInOut(loader, methods, getInput, inOut);
                
                if (result.isEmpty()) {
                    result = getSinkInOut(loader, methods, getInput, inOut);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new ManifestUtilsException("Missing dependency!", e);
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            throw new ManifestUtilsException("Missing dependency!", e);
        } catch (SecurityException e) {
            e.printStackTrace();
        } finally {
            if (null != loader) {
                try {
                    loader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;      
    }
    
    /**
     * Retrieves the in/output for Sinks and Sources.
     * @param loader The used ClassLoader.
     * @param methods The list of methods to retrieve from.
     * @param getInput True if this is for input, false for output.
     * @param inOut  
     * @throws ClassNotFoundException If the resolved class does not exist.
     * @return a List of In/Output Items.
     */
    private List<Item> getSinkInOut(ClassLoader loader, Method[] methods, Boolean getInput, List<String> inOut) 
            throws ClassNotFoundException {
        
        List<Item> result = new ArrayList<Item>();

        String itemName = null;
        Class<?> io = null;
        
        for (Method m : methods) {
            if (!getInput && m.getName().startsWith("get") 
                    && !m.getName().startsWith("getParameter")) {

                Class<?> param = m.getReturnType();
                inOut.add(param.getName());
                io = loader.loadClass(param.getName());
                itemName = decrypt(param);
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);

                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                }                
                
            } else if (getInput && m.getName().startsWith("post")) {

                Class<?>[] param = m.getParameterTypes();
                for (int i = 0; i < param.length; i++) {
                    inOut.add(param[i].getName());
                    io = loader.loadClass(param[i].getName());
                    itemName = decrypt(param[i]);
                }
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);
                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                } else {
                    System.out.println("UNABLE TO FIND IN / OUTPUT!"); 
                } 
                
            }
        }   
        
        return result;
        
    }
    
    /**
     * Retrieves the in/output for Sinks.
     * @param loader The used ClassLoader.
     * @param methods The list of methods to retrieve from.
     * @param getInput True if this is for input, false for output.
     * @param inOut  
     * @throws ClassNotFoundException If the resolved class does not exist.
     * @return a List of In/Output Items.
     */
    private List<Item> getAlgorithmInOut(ClassLoader loader, Method[] methods, Boolean getInput, 
            List<String> inOut) throws ClassNotFoundException {
        
        List<Item> result = new ArrayList<Item>();
        Class<?> io = null;
        String itemName = null; 
        
        for (Method m : methods) {
            if (m.getName().equals("calculate")) {
                Class<?>[] param = m.getParameterTypes();
                for (int i = 0; i < param.length; i++) {
                    inOut.add(param[i].getName());
                    if (getInput && param[i].getName().toLowerCase().contains("input")) {
                        io = loader.loadClass(param[i].getName());
                        itemName = decrypt(param[i]);
                    } else if (!getInput && param[i].getName().toLowerCase().contains("output")) {
                        io = loader.loadClass(param[i].getName());
                        itemName = decrypt(param[i]);
                    }         
                }
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);
                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                } else {
                    System.out.println("UNABLE TO FIND IN / OUTPUT!"); 
                }  
            }
        }
        
        return result;
    }
    
    /**
     * Checks whether an item is already in a list of items.
     * @param list The list of items.
     * @param newItem The item to check for.
     * @return True if item does exist, false otherwise.
     */
    private boolean doubleItems(List<Item> list, Item newItem) {
        
        boolean result = false;
        
        for (Item item : list) {
            if (newItem.equals(item)) {
                result = true;
                break;
            }
        }
        
        return result;
        
    }
    
    /**
     * Decrypts the name of a parameter.
     * @param param The parameter Type.
     * @return The decrypted paramter name.
     */
    public static String decrypt(Class<?> param) {
        String itemName = "";
        try {
            String[] splited = param.getName().split("\\.");
            String[] splited2 = splited[splited.length - 1].split("\\$");
            itemName = param.getSimpleName().substring(
                    param.getSimpleName().indexOf(splited2[0]) + splited2[0].length());
            itemName = lowerFirstLetter(itemName);
        } catch (ArrayIndexOutOfBoundsException exc) {
            itemName = param.getSimpleName();
        } catch (StringIndexOutOfBoundsException exc) {
            itemName = param.getSimpleName();
        }
        
        if (itemName.contains("Input")) {
            itemName = itemName.substring(0, itemName.lastIndexOf("Input"));
        } else if (itemName.contains("Output")) {
            itemName = itemName.substring(0, itemName.lastIndexOf("Output"));
        }
        
        return itemName;
    }
    
    /**
     * Lowers the first letter of a String.
     * @param input The input String.
     * @return The original string, but with the first letter in lower case.
     */
    public static String lowerFirstLetter(String input) {
        return Character.toLowerCase(input.charAt(0))
            + (input.length() > 1 ? input.substring(1) : "");
    }
    
    /**
     * Does stuff.
     * @param io the io class.
     * @param itemName the name for the item.
     * @return Item an item.
     */
    private Item getSetter(Class<?> io, String itemName) {
        
        Item result = null;

        Item item = new Item();
        item.setName(itemName);
        for (Method met : io.getMethods()) {
            if (met.getName().contains("set")) {
                Class<?> cls = met.getParameterTypes()[0];
                String string = met.getName().substring(3);
                char[] c = string.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                string = new String(c);
                
                FieldType fType = null;
                
                Type[] genericParameterTypes = met.getGenericParameterTypes();
                for (int i = 0; i < genericParameterTypes.length; i++) {
                    if (genericParameterTypes[i] instanceof ParameterizedType) {
                        Type[] parameters = ((ParameterizedType) genericParameterTypes[i]).getActualTypeArguments();
                        for (Type t : parameters) {
                            String[] split = t.toString().split("\\.");
                            String newName = split[split.length - 1] + cls.getSimpleName();
                            fType = FieldType.value(newName.toUpperCase());
                        }
                    }
                }
                
                if (null == fType) {
                    fType = FieldType.valueOf(cls);
                }
                
                item.addField(new Field(string, fType));
            }
        }
        result = item;
        
        return result;
        
    }
    
    /**
     * Returns the actual class name inside an absolute class name.
     * @param name An absolute class name.
     * @return The actual class name.
     */
    public String getClassName(String name) {
        String[] parts = name.split("\\.");
        return parts[parts.length - 1];
    }
    
    /**
     * Load all jars from a given folder.
     * @param folder The folder to load from.
     * @return A list with jar names.
     */
    public List<URL> loadJars(String folder) {
        
        List<URL> list = new ArrayList<URL>();
        
        File dir = new File(folder);
        
        for (File file : dir.listFiles()) {
            
            System.out.println("FOUND: " + file.getAbsolutePath());
            
            if (!file.isDirectory()) {
                String outPath = out.getAbsolutePath();
                try {
                    list.add(new URL("file:" + (outPath.charAt(0) == '/' ? "" : "///" ) 
                        + outPath + "/" + file.getName()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
        return list;
        
    }
    
    /**
     * Publishes an artifact.
     * @param ri The ModuleRevisionId
     * @param srcArtifactPattern A Collection with artifact patterns.
     * @param resolverName The name of the Resolver as String.
     * @param options PublishOptions for the publish.
     * @param revision The revison id.
     */
    public void publish(ModuleRevisionId ri, Collection<Pattern> srcArtifactPattern, 
            String resolverName, PublishOptions options, String revision) {

        System.out.println("Attempting to publish...");
        System.out.println("ModuleRevisionId == " + ri.getName());
        System.out.println("Resolver Name == " + resolverName);
        
        IBiblioResolver testRes = new IBiblioResolver();
        testRes.setName("publisher");
        // TODO fixed URL?? -> shall be configured from outside
        testRes.setRoot("https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/");
        this.ivy.getSettings().addResolver(testRes);
        System.out.println("CACHE: " + this.ivy.getSettings().getDefaultCache());
        
        try {
            // TODO UNCLEAR C:/ no .m2 -> mavenPath?
            ivy.deliver(ri, revision, 
                    "C:/m2/repository/[organisation]/[module]/[revision]/[module]-[revision].[ext]");
            ivy.publish(ri, srcArtifactPattern, resolverName, options);
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Publishes an artifact to the target.
     * @param file The actual artifact file.
     * @param groupId The groupId.
     * @param artifactId The artifactId.
     * @param version The (new) version.
     * @param target The target server.
     */
    public void publish(String file, String groupId, String artifactId, String version, String target) {
        
        ModuleId modId = new ModuleId(groupId, artifactId);
        
        ModuleRevisionId mrId = new ModuleRevisionId(modId, version);
        List<String> srcArtifactPattern = new ArrayList<String>();
        srcArtifactPattern.add(file.substring(0, file.lastIndexOf("/")) + "/[module].[ext]");
        String resolverName = "publish_" + target;
        PublishOptions options = new PublishOptions();
        
        IBiblioResolver testRes = new IBiblioResolver();
        testRes.setName("publish_" + target);
        testRes.setRoot(target);
        testRes.setM2compatible(true);
        testRes.setUseMavenMetadata(true);
        
        System.out.println("Deploying artifact \"" + mrId + "\" to " + target);
        System.out.println("Artifact in: " + srcArtifactPattern.get(0));
        System.out.println("Resolver: " + resolverName);
        
        if (null == this.ivy.getSettings().getResolver("publish_" + target)) {
            this.ivy.getSettings().addResolver(testRes);
            System.out.println("Resolver initialized!");
        } else {
            System.out.println("Resolver found!");
        }
        
        try {
            ivy.publish(mrId, srcArtifactPattern, resolverName, options);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Publishes an artifact.
     * @param file The file to publish (should be a *.jar) <br>
     *        Example: "C:/Artifacts/test.jar"
     * @param pomFile The pom file for publishing.
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param version the version of the artifact.
     * @param forceOverwrite forces an overwrite in the nexus.
     */
    public void publishWithPom(String file, String pomFile, String repository, 
            String version, boolean forceOverwrite) {
        
        File ivyFile = new File(pomFile);
        ivyFile = ivyFile.getParentFile();
        ivyFile = new File(ivyFile.getAbsolutePath() + "/ivy.xml");
        
        System.out.println("Publishing with POM...");
        
        IvyConvertPom converter = new IvyConvertPom();
        converter.setPomFile(new File(pomFile));

        converter.setIvyFile(ivyFile);
        
        converter.setProject(new Project());
        converter.doExecute();
        
        System.out.println("Converted to ivy...");
        
        publish(file, ivyFile.getAbsolutePath(), repository, version, forceOverwrite);
        
    }
    
    /**
     * Publishes an artifact.
     * @param file The file to publish (should be a *.jar) <br>
     *        Example: "C:/Artifacts/test.jar"
     * @param ivyFile The ivy file for publishing.
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param version the version of the artifact.
     * @param forceOverwrite forces an overwrite in the nexus.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void publish(String file, String ivyFile, String repository, 
            String version, boolean forceOverwrite) {
        
        IBiblioResolver testRes = new IBiblioResolver();
        testRes.setName("publish_" + repository);
        testRes.setRoot(repository);
        
        if (null == this.ivy.getSettings().getResolver("publish_" + repository)) {
            this.ivy.getSettings().addResolver(testRes);
        }
        
        //Is undefined, because ivy needs a Collection<Pattern> with actually just Strings inside it...
        Collection srcArtifactPattern = new ArrayList();
        System.out.println(file.substring(0, file.lastIndexOf("/")) + "/[module].[ext]");
        srcArtifactPattern.add(file.substring(0, file.lastIndexOf("/")) + "/[module].[ext]");
        
        ModuleRevisionId ri = ModuleRevisionId.newInstance("", "", version);
        
        PublishOptions options = new PublishOptions();
        options.setHaltOnMissing(true);
        options.setOverwrite(forceOverwrite);
        options.setSrcIvyPattern(ivyFile);
        options.setUpdate(true);
            
        try {
            ivy.publish(ri, srcArtifactPattern, "publish_" + repository, options);
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (IllegalStateException exc) {
            exc.printStackTrace();
        }
        
    }
    
    /**
     * Publishes an artifact.
     * @param dir The folder to publish<br>
     *        Example: "C:/Artifacts/test.jar"
     * @param pomFile The pom file for publishing.
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param forceOverwrite forces an overwrite in the nexus.
     * @param monitor The ProgressObserver to monitor the current progress.
     * @throws ManifestUtilsException 
     */
    public void publishDirWithPom(String dir, String pomFile, String repository, 
            boolean forceOverwrite, ProgressObserver monitor) throws ManifestUtilsException {
        
        boolean failed = false;
        if (null != pomFile) {
            File ivyFile = new File(pomFile);
            if (null != ivyFile) {
                ivyFile = ivyFile.getParentFile();
                if (null != ivyFile) {
                    ivyFile = new File(ivyFile.getAbsolutePath() + "/ivy.xml");
                    
                    System.out.println("Publishing with POM...");
                    
                    IvyConvertPom converter = new IvyConvertPom();
                    converter.setPomFile(new File(pomFile));
            
                    converter.setIvyFile(ivyFile);
                    
                    converter.setProject(new Project());
            //        Project project = new Project();
            //        project.setBaseDir(new File("C:/TEST-thing/if-gen"));
            //        converter.setProject(project);
                    converter.doExecute();
                    
                    System.out.println("Converted to ivy...");
                    
                    publishDir(dir, ivyFile.getAbsolutePath(), repository, forceOverwrite, monitor);
                } else {
                    failed = true;
                }
            }
        } else {
            failed = true;
        }
        
        if (failed) {
            throw new ManifestUtilsException("Unable to convert pom: '" + pomFile + "'!");
        }
        
    }
    
    /**
     * Publishes an artifact.
     * @param dir The dir to publish<br>
     *        Example: "C:/Artifacts/target"
     * @param ivyFile The ivy file for publishing. For pom use the wrapper method.
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param forceOverwrite forces an overwrite in the nexus.
     * @param monitor The ProgressObserver to monitor the current progress.
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    public void publishDir(String dir,  String ivyFile, String repository, 
            boolean forceOverwrite, ProgressObserver monitor) {
        
        IBiblioResolver testRes = new IBiblioResolver();
        testRes.setName("publish_" + repository);
        testRes.setRoot(repository);
        
        List<File> files = getFilesFromDir(dir);
        
        if (null == this.ivy.getSettings().getResolver("publish_" + repository)) {
            this.ivy.getSettings().addResolver(testRes);
        }
        
        //Is undefined, because ivy needs a Collection<Pattern> with actually just Strings inside it...
        Collection srcArtifactPattern = new ArrayList();
        for (File file : files) {
            String filePath = file.getAbsolutePath();
            System.out.println(filePath.substring(0, filePath.lastIndexOf("/")) + "/[module].[ext]");
            srcArtifactPattern.add(filePath.substring(0, filePath.lastIndexOf("/")) + "/[module].[ext]");
        }
        
        ModuleRevisionId ri;
        ri = ModuleRevisionId.newInstance("", "", "");
 
        PublishOptions options = new PublishOptions();
        options.setHaltOnMissing(true);
        options.setOverwrite(forceOverwrite);
        options.setSrcIvyPattern(ivyFile);
        options.setUpdate(true);
            
        System.out.println("WOULD NOW PUBLISH!");
        
//        try {
//            ivy.publish(ri, srcArtifactPattern, "publish_" + repository, options);
//        } catch (IOException exc) {
//            exc.printStackTrace();
//        } catch (IllegalStateException exc) {
//            exc.printStackTrace();
//        }
        
    }
    
    /**
     * Returns all files inside a folder and its subfolders.
     * @param dir The root folder.
     * @return A list of all files inside the folder and its subfolders.
     */
    public static List<File> getFilesFromDir(String dir) {
        List<File> files = new ArrayList<File>();
        File directory = new File(dir);

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                files.add(file);
            } else if (file.isDirectory()) {
                files.addAll(getFilesFromDir(file.getAbsolutePath()));
            }
        }
        return files;
    }
    
    /**
     * Extracts the manifest from a jar file.
     * @param artifactId The name of the jar file.
     * @return false if the process failed.
     */
    private boolean extractManifest(String artifactId) {
        
        boolean success = true;
        URL url;
        OutputStream outStream = null;
        
        try {
            
            url = new URL(createJarAccessorURLString(out, artifactId) + "/manifest.xml");
            InputStream is;
            try {
                is = url.openStream();
            } catch (IOException e) {
                // fallback
                url = new URL(createJarAccessorURLString(out, artifactId) + "/META-INF/manifest.xml");
                is = url.openStream();
            }
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
         
            File targetFile = new File(out + "/manifest.xml");
            outStream = new FileOutputStream(targetFile);
            outStream.write(buffer);
            
        } catch (FileNotFoundException e) {
            
            //There is no manifest. No big deal, 
            //as long as a class name is provided in the editor.
            success = false;
            
        } catch (IOException e) {
            
            e.printStackTrace();
            success = false; 
            
        } finally {
            
            if (null != outStream) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
        }
        
        return success;
        
    }
    
    /**
     * Returns all output information stored.
     * Once called, all information (up to that point) will be deleted.
     * @return a Strings with all information.
     */
    public String getOutput() {
       
        String buffered = this.output;
        this.output = "";
        return buffered;
        
    }
    
    /**
     * Deletes a directory.
     * @param dir The dir to delete.
     * @return true if deletion was successful, false otherwise.
     */
    public static boolean deleteDir(File dir) { 
        
        boolean result = true;
        if (dir.isDirectory()) {
            String[] children = dir.list(); 
            for (int i = 0; i < children.length; i++) { 
                boolean success = deleteDir(new File(dir, children[i])); 
                if (!success) {  
                    result = false; 
                } 
            } 
        }
        
        // The directory is now empty or this is a file so delete it 
        result = dir.delete();
        return result;
        
    } 
    
    /**
     * Resolves an artifact.
     * @param groupId The group ID of the target artifact.
     * @param artifactId The artifact ID of the target artifact.
     * @param version The version of the target artifact.
     * @return ResolveReport The report of the resolving.
     */
    public ResolveReport resolve(String groupId, String artifactId, String version) {
        
        ResolveReport report = null;
        
//        System.setOut(sysOut);
//        System.setErr(sysOut);
        System.setProperty("jsse.enableSNIExtension", "false");
        Security.insertProviderAt(new BouncyCastleProvider(), 1);      
        
        //deleteDir(new File("ivy/cache"));
        //deleteDir(new File("out"));
        
        addCredentials("testuser", "nexustest");
        
        ResolveOptions ro = new ResolveOptions();
        ro.setResolveMode(ResolveOptions.RESOLVEMODE_DYNAMIC);
        ro.setTransitive(true);
        ro.setDownload(true);    
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            ModuleRevisionId.newInstance(groupId, artifactId + "-envelope", version)
        );
        ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, true);
        dd.addDependencyConfiguration("default", "compile"); //master
        dd.addDependencyConfiguration("default", "default");
        dd.addDependencyConfiguration("default", "master");
        dd.addDependencyConfiguration("default", "runtime");
        dd.addDependencyConfiguration("default", "provided");
        md.addDependency(dd); 
        try {
            report = ivy.resolve(md, ro);
            report.getModuleDescriptor();
            if (report.hasError()) {
                throw new RuntimeException(report.getAllProblemMessages().toString());
            }                
        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (ParseException exc) {
            exc.printStackTrace();
        }  
        
        return report;
        
    }
    
    /**
     * Returns all referenced jars for the target artifact.
     * @param groupId The group ID of the target artifact.
     * @param artifactId The artifact ID of the target artifact.
     * @param version The version of the target artifact.
     * @return A list with the names of the referenced jars.
     */
    public List<String> getJarList(String groupId, String artifactId, String version) {
        
        List<String> jarList = new ArrayList<String>();
        
        for (Object o : resolve(groupId, artifactId, version).getArtifacts()) {
            jarList.add(o.toString());
        }
        
        return jarList;
        
    }
    
    /**
     * Returns all referenced jar files for the target artifact.
     * @param groupId The group ID of the target artifact.
     * @param artifactId The artifact ID of the target artifact.
     * @param version The version of the target artifact.
     * @return A list of all referenced (and downloaded) jar files.
     */
    public List<File> getJarFiles(String groupId, String artifactId, String version) {
        
        List<File> fileList = new ArrayList<File>();
        List<String> list = getJarList(groupId, artifactId, version);
        
        for (String s : list) {
            System.out.println(s);
            
            String wip = s.substring(s.lastIndexOf('!') + 1);
            
            fileList.add(new File(this.out.getAbsolutePath() + "/" 
                    + wip.substring(0, wip.lastIndexOf(".jar") + 4)));
        }
        
        return fileList;
        
    }
    
    /**
     * Returns all referenced jar files for the target artifact.
     * @param pom a pom.xml
     * @return A list of all referenced (and downloaded) jar files.
     */
    public List<File> getJarFiles(File pom) {
        
        PomInfo info = PomReader.getInfo(pom);
        
        List<File> fileList = new ArrayList<File>();
        List<String> list = getJarList(info.getGroupId(), info.getArtifactId(), info.getVersion());
        
        for (String s : list) {
            System.out.println(s);
            
            String wip = s.substring(s.lastIndexOf('!') + 1);
            
            fileList.add(new File(this.out.getAbsolutePath() + "/" 
                    + wip.substring(0, wip.lastIndexOf(".jar") + 4)));
        }
        
        return fileList;
        
    }
    
    /**
     * Validates the jar list, aka checks if all files exist. 
     * Setting the boolean to true will cause the validator to try and fix any problems 
     * (ie. reflectasm-shaded is not found, since it is called reflectasm).
     * @param jars The file list of the jars.
     * @param fix Set to true if possible errors should be fixed (if possible).
     * @return A list of unvalid files. Can be empty if no errors occured.
     */
    @SuppressWarnings("unused")
    private List<File> validateJarList(List<File> jars, boolean fix) {
        
        List<File> errors = new ArrayList<File>();
        
        for (int i = 0; i < jars.size(); i++) {
            if (!jars.get(i).exists()) {
                
                if (fix) {

                    File replacement = new File(jars.get(i).getAbsolutePath().substring(0, 
                            jars.get(i).getAbsolutePath().length() - 4) + "-shaded.jar");
                    
                    if (replacement.exists()) {
                        
                        jars.remove(i);
                        jars.add(i, replacement);
                        
                    } else {
                        
                        errors.add(jars.get(i));
                        
                    }
                    
                } else {
                    
                    errors.add(jars.get(i));
                    
                }
                
            }
        }
        
        return errors;
        
    }
    
    /**
     * Returns a list of all classes for the pom.
     * @param pom The pom file to resolve by.
     * @return A list of Class<?>, can be empty.
     */
    public List<Class<?>> getClassList(File pom) {
        return null;
    }
    
    /**
     * Returns a list of all JarInfos for the pom.
     * @param pom The pom file to resolve by.
     * @return A list of JarInfo, can be empty.
     */
    public List<JarInfo> getJarInfo(File pom) {
        List<JarInfo> result = new ArrayList<JarInfo>();
        
        List<File> jars = getJarFiles(pom);
        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); i++) {
            try {
                urls[i] = jars.get(i).toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        
        return result;
    }
    
    /**
     * Returns a list of all class names inside a jar file.
     * @param file The jar file.
     * @return A List<String> with the class names inside the jar. Can be empty.
     */
    public List<String> getAllClasses(File file) {
        
        List<String> classNames = new ArrayList<String>();
        ZipInputStream zip;
        try {
            zip = new ZipInputStream(new FileInputStream(file.getAbsolutePath()));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    // This ZipEntry represents a class. Now, what class does it represent?
                    String className = entry.getName().replace('/', '.'); // including ".class"
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return classNames;

    }
    
    /**
     * Returns a list of loaded classes from source files inside a jar.
     * @param file The jar file.
     * @return A list of classes compiled from source files.
     */
    public List<Class<?>> compileClasses(File file) {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        return classes;
    }
    
    /**
     * Compiles a source file into a class.
     * @param file The source file.
     * @param className The name of the class.
     * @return The compiled class, or null.
     */
    public Class<?> compileClass(File file, String className) {
        
        List<URL> urls = loadJars(file.getParentFile().getAbsolutePath());
        String[] array = new String[urls.size() + 1];
        array[array.length - 1] = file.getPath();
        for (int i = 0; i < urls.size(); i++) {
            array[i] = urls.get(i).getPath().substring(1, urls.get(i).getPath().length());
        }
        Class<?> result = null;
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        compiler.run(null, null, null, array);
        
        @SuppressWarnings("unused")
        File dir = null;
        
        if (file.isDirectory()) {
            dir = file;
        } else {
            dir = file.getParentFile();
        }
        
//        URLClassLoader classLoader;
//        try {
//            classLoader = URLClassLoader.newInstance(new URL[] {dir.toURI().toURL()});
//            result = Class.forName(className, true, classLoader);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
        
        return result;
    }
    
    /**
     * Returns a list of all class names inside a jar file. Only classes with "Calculate" methods are returned.
     * @param file The jar file.
     * @return A List<String> with the class names inside the jar. Can be empty.
     */
    public List<String> getAllValidClasses(File file) {
        List<String> result = getAllClasses(file);
        
        return result;
    }
    
    /**
     * Retrieves the artifact Id from an artifact name.
     * @param artifact The full artifact name.
     * @return The retrieved artifactId, or null.
     */
    public static String getArtifactId(String artifact) {
        String result = null;
        if (null != artifact) {
            try {
                result = artifact.split(":")[1].trim();
            } catch (ArrayIndexOutOfBoundsException exc) {
                //Some kind of report is needed!
            }
        }
        return result;
    }
    
    /**
     * Retrieves the group Id from an artifat name.
     * @param artifact The full artifact name.
     * @return The retrieved groupId, or null.
     */
    public static String getGroupId(String artifact) {
        String result = null;
        if (null != artifact) {
            try {
                result = artifact.split(":")[0].trim();
            } catch (ArrayIndexOutOfBoundsException exc) {
                //Some kind of report is needed!
            }
        }
        return result;
    }
    
    /**
     * Retrieves the version from an artifat name.
     * @param artifact The full artifact name.
     * @return The retrieved version, or null.
     */
    public static String getVersion(String artifact) {
        String result = null;
        if (null != artifact) {
            try {
                result = artifact.split(":")[2].trim();
            } catch (ArrayIndexOutOfBoundsException exc) {
                //Some kind of report is needed!
            }
        }
        return result;
    }
    
    /**
     * Returns a list of all class names inside a jar file.
     * @param files The jar file.
     * @return A List<String> with the class names inside the jar. Can be empty.
     */
    public List<String> getAllClasses(List<File> files) {
        
        List<String> classNames = new ArrayList<String>();
        ZipInputStream zip;
        for (File file : files) {
            
            if (!file.exists()) {

                File replacement = new File(file.getAbsolutePath().substring(0, 
                        file.getAbsolutePath().length() - 4) + "-shaded.jar");
                
                if (replacement.exists()) {
                    file = replacement;
                }
                
            }
            
            try {
                zip = new ZipInputStream(new FileInputStream(file.getAbsolutePath()));
                for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        // This ZipEntry represents a class. Now, what class does it represent?
                        String className = entry.getName().replace('/', '.'); // including ".class"
                        className = className.substring(0, className.length() - ".class".length());
                        if (!className.matches(CLASS_NAMES_TO_OMIT)) {
                            classNames.add(className);
                        }
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return classNames;

    }
    
    /**
     * Returns a list of all class names inside a jar file. Only classes with "Calculate" methods are returned.
     * @param files The jar file.
     * @return A List<String> with the class names inside the jar. Can be empty.
     */
    public List<String> getAllValidClasses(List<File> files) {
        
        List<String> result = new ArrayList<String>();
        
        if (null != files && !files.isEmpty()) {
            
            List<String> classNames = getAllClasses(files);
            URLClassLoader loader = null;
            
            URL[] urls = new URL[files.size()];
            try {
                for (int i = 0; i < files.size(); i++) {
                    urls[i] = files.get(i).toURI().toURL();
                }
                loader = new URLClassLoader(urls);
                
                for (String s : classNames) {
                    
                    result.add(s);
                    try {
                        Class<?> cls = loader.loadClass(s);
                        for (Method method : cls.getMethods()) {
                            if (method.getName().equalsIgnoreCase("calculate")) {
                                result.add(cls.getCanonicalName());
                            }
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                    
                }
                
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
                if (null != loader) {
                    try {
                        loader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        
        }
        
        return result;
    }
    
    /**
     * Returns all valid classes for all jars inside the retrieve cache.
     * @return A list of classes with calculate methods.
     */
    public List<String> getAllValidClasses() {
     
        List<String> result = new ArrayList<String>();
        
        //getAllValidClasses would only choose classes with a calculate method, getAllClasses will get ALL classes.
        result.addAll(getAllClasses(this.getJarFiles(this.getLastReport())));
     
        return result;
     
    }
    
    /**
     * Returns all valid classes for the given artifactID.
     * i.e. Checks the manifest for information about the implementing class.
     * @param artifactId The name of the artifact.
     * @return A list of possible implementing classes (if the manifest specifies the implementing class,
     * then the list should only contain a single element). If the manifest does not specify an implementing class,
     * or the specified class does not exist, then all classes inside the main artifact(jar) are returned.
     * @throws ManifestUtilsException If the manifest has errors.
     */
    public List<String> getAllValidClasses(String artifactId) throws ManifestUtilsException {
        
        String className = null;
        List<String> result = new ArrayList<String>();
        List<String> preResult = getAllValidClasses();
        
        //Check for implementing classes defined by the manifest.
        if (null != artifactId && extractManifest(artifactId)) {
            ManifestParser mp = new ManifestParser();
            Manifest manifest = mp.parseFile(new File(out + "/manifest.xml"));
            if (null != manifest && null != manifest.getProvider()) {
                className = manifest.getProvider();
            }    
        } 
        
        //Check for defined implementing classes (if available)
        if (null != className) {
            for (int i = 0; i < preResult.size(); i++) {
                if (preResult.get(i).equals(className)) {
                    result.add(preResult.get(i));
                    break;
                }
            }
        }
        
        //if no implementing class was found/available show all possible classes.
        if (result.isEmpty()) {
            result.addAll(preResult);
        }
        
        return result;
        
    }
    
    /**
     * Returns all referenced jar files for the target artifact.
     * @param report The last ResolverReport.
     * @return A list of all referenced (and downloaded) jar files.
     */
    public List<File> getJarFiles(ResolveReport report) {
     
        List<File> fileList = new ArrayList<File>();
     
        if (!report.getArtifacts().isEmpty()) {
            String s = report.getArtifacts().get(0).toString();
         
            String wip = s.substring(s.lastIndexOf('!') + 1);
         
            fileList.add(new File(this.out.getAbsolutePath() + "/"
                 + wip.substring(0, wip.lastIndexOf(".jar") + 4)));
        }
        
        return fileList;
     
    }
    
    /**
     * Returns all referenced jars for the target artifact.
     * @param report The last ResolverReport.
     * @return A list with the names of the referenced jars.
     */
    public List<String> getJarList(ResolveReport report) {
        
        List<String> jarList = new ArrayList<String>();
        
        for (Object o : report.getArtifacts()) {
            jarList.add(o.toString());
        }
        
        return jarList;
        
    }
    
    /**
     * Returns the last resolveReport.
     * @return The last resolveReport or null.
     */
    public ResolveReport getLastReport() {
        return this.report;
    }
    
    /**
     * Returns the name of the main artifact (aka the name of the main jar).
     * @return The name of the main artifact or null.
     */
    public String getMainArtifactName() {
        return this.mainArtifactName;
    }

    /**
     * Main method for regular quick testing of singular functions.
     * @param args The default arguments.
     */
    public static void main(String[] args) {
        
        ManifestConnection con = new ManifestConnection();
        try {
            con.publishDirWithPom("C:/TEST-thing/if-gen/target", "C:/TEST-thing/if-gen/pom.xml", 
                    "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/", false, null);
        } catch (ManifestUtilsException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
