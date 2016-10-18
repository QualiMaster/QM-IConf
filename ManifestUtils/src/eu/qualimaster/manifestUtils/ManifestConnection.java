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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.ivy.Ivy;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultExcludeRule;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ExcludeRule;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ArtifactId;
import org.apache.ivy.core.module.id.ModuleId;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.matcher.ExactOrRegexpPatternMatcher;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.ivy.util.Credentials;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import eu.qualimaster.manifestUtils.PomReader.PomInfo;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Metadata;
import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.basics.progress.ProgressObserver.ITask;

/**
 * Builds and handles connections to the repository server.
 * 
 * Instantiating the ManifestConnection will set up the connection with necessary settings to start downloading
 * an artifact via the load() method.
 * 
 * To load an artifact you need the groupId, artifactId and desired version. First, the artifact will be resolved.
 * Ivy will check for all necessary dependencies in a transitive way. Once that process is finished the actual 
 * dependencies will be downloaded from the available servers (which can be added with addRepository()).
 * 
 * The downloaded artifact and dependencies will be stored in the Maven repository aswell as in Ivys temp folder.
 * Information of the artifact can then be retrieved with the getOutput(), getInput() and getParameters() methods, 
 * which require the class name and the artifactId. Also the manifest can be extracted from the jar 
 * via extractManifest().
 * 
 * On top of that, an artifact can be uploaded with publish().
 * 
 * <br/>
 * Example manifest:
 * <pre><code>
 * <manifest>
 &lt;provides class="eu.qualimaster.algorithms.imp.correlation.Preprocessor"&gt;
  &lt;algorithm family="fPreprocessor"&gt;
      &lt;description&gt;The financial preprocessor&lt;/description&gt;
      &lt;input&gt;
          &lt;tuple name="springStream"&gt;
              &lt;field name="symbolTuple" type="STRING"/&gt;
          &lt;/tuple&gt;
      &lt;/input&gt;
      &lt;output&gt;
          &lt;tuple name="preprocessedStream"&gt;
              &lt;field name="value" type="DOUBLE"/&gt;
              &lt;field name="timestamp" type="LONG"/&gt;
              &lt;field name="symbolId" type="STRING"/&gt;
              &lt;field name="volume" type="INTEGER"/&gt;
              &lt;field name="taskId" type="INTEGER"/&gt;
          &lt;/tuple&gt;
      &lt;/output&gt;
  &lt;/algorithm&gt;
 &lt;/provides&gt;
&lt;/manifest&gt;
 * </code></pre>
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
    private static final String JAR_WITH_DEPENDENCIES = "-jar-with-dependencies.jar";
    private static final String SOURCES_JAR = "-sources.jar";
    private static final String JAR = ".jar";
    private static final String POM_NAME = "pom.xml";
    private static final String POM_DEST_NAME = ".pom";
    private static final String METAFILE_NAME = "maven-metadata.xml";
    
    private static File ivyOut = null;
    
    private static List<String> repositories = new ArrayList<String>();
    
    private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(ManifestConnection.class, 
            "eu.qualimaster.ManifestUtils");

    private Ivy ivy = null;
    private File out = null;
    private String output = "";
    private DownloadIntercepter sysOutDownload;
    private ResolveReport report = null;
    private String mainArtifactName = null;
    
    
    static {
        addDefaultRepositories();
    }
    
    /**
     * Simple constructor for a ManifestConnection.
     * Sets up needed resolver etc.
     */
    public ManifestConnection() {
        
        try {
            sysOutDownload = new DownloadIntercepter(output, new PrintStream(new FileOutputStream(FileDescriptor.out)));
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
        //ivy cache path and patterns.
        ivySettings.setDefaultCache(new File(mavenPath + "/ivy_cache"));
        ivySettings.setDefaultCacheArtifactPattern(
                "[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        ivySettings.setDefaultCacheIvyPattern(
                "[organisation]/[module]/[revision]/[module]-[revision](-[classifier]).[ext]");
        
        //initialize chain resolver to enable use of several repositories.
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
        
        //find the manifest of the artifact and parse it.
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
        System.out.println(username);
    }
    
    /**
     * Returns the credentials used to login during tool startup.
     * @return The used credentials. Can be null if started without login.
     */
    public static Credentials getCredentials() {
        return org.apache.ivy.util.url.CredentialsStore.INSTANCE.getCredentials("Sonatype Nexus Repository Manager",
            "nexus.sse.uni-hildesheim.de");
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
        sysOutDownload.setMonitor(monitor);
        System.setProperty("jsse.enableSNIExtension", "false");
        Security.insertProviderAt(new BouncyCastleProvider(), 1);      
        
        //clear our temp folder.
        deleteDir(this.out);
        setRetrievalFolder(this.out);
        logger.info("Retrieval dir: " + this.out);
        ResolveOptions ro = new ResolveOptions();

        //set the resolver to transitive.
        ro.setResolveMode(ResolveOptions.RESOLVEMODE_DYNAMIC);
        ro.setTransitive(true);
        ro.setDownload(true);  
        DefaultModuleDescriptor md = DefaultModuleDescriptor.newDefaultInstance(
            ModuleRevisionId.newInstance(groupId, artifactId + "-envelope", version)
        );
        ModuleRevisionId ri = ModuleRevisionId.newInstance(groupId, artifactId, version);
        DefaultDependencyDescriptor dd = this.getDefaultDependencyDescriptor(md, ri);
        
        ModuleId moduleId = new ModuleId("eu.qualimaster", "QualiMaster.Events");
        ArtifactId aId = new ArtifactId(moduleId, "QualiMaster.Events-tests", "jar", "jar");
        Map<String, String> map = new HashMap<String, String>();
        //map.put("classifier", "tests");
        ExcludeRule rule = new DefaultExcludeRule(aId, new ExactOrRegexpPatternMatcher(), map);
        System.out.println("##### " + rule.toString());
        md.addDependency(dd);
        //md.addExcludeRule(rule);
        ModuleDescriptor m = null; 
        
        //start the download process. Progress is tracked via an intercepter since ivy doesn't provide
        //direct access to progress.
        try {
            monitor.notifyStart(mainTask, 200);
            sysOutDownload.setMax(200);
            sysOutDownload.setTask(mainTask); 
            System.setOut(sysOutDownload);
            System.setErr(sysOutDownload);
            report = ivy.resolve(md, ro);
            m = report.getModuleDescriptor();
            if (report.hasError()) {
                throw new RuntimeException(report.getAllProblemMessages().toString());
            }        
            
        } catch (IOException | ParseException exc) {
            exc.printStackTrace();
        }
        
        try {
            ivy.retrieve(
                    m.getModuleRevisionId(),
                    out.getAbsolutePath() + "/[artifact](-[classifier]).[ext]", //
                    new RetrieveOptions().setConfs(new String[]{"default"})
            ); 
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        //end the process and disable the intercepter.
        ModuleRevisionId.newInstance(groupId, artifactId, version);
        PublishOptions options = new PublishOptions();
        options.setUpdate(false);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
        monitor.notifyEnd(mainTask);

    }
    
    /**
     * Returns a configured DependencyDescriptor.
     * @param md The ModuleDescriptor.
     * @param ri The ModuleRevisionId.
     * @return The configured DependencyDescriptor.
     */
    private DefaultDependencyDescriptor getDefaultDependencyDescriptor(ModuleDescriptor md, ModuleRevisionId ri) {
        
        //add all configurations, since ivy will fail to download some parts otherwise.
        DefaultDependencyDescriptor dd = new DefaultDependencyDescriptor(md, ri, false, false, true);
        dd.addDependencyConfiguration("default", "compile");
        dd.addDependencyConfiguration("default", "default");
        dd.addDependencyConfiguration("default", "master");
        dd.addDependencyConfiguration("default", "runtime");
        dd.addDependencyConfiguration("default", "provided");
        return dd;
        
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
            //load all necessary classes into the classloader.
            URL[] urls = new URL[1];
            loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls));         
            Class<?> base = loadClass(name, loader); 
            if (null != base) {
                //if the base class of the algorithm is available:
                Method[] methods = base.getMethods();
                for (Method m : methods) {
                    //analyze methods that set parameters.
                    if (m.getName().startsWith("setParameter")) {
                        String pName = m.getName().substring("setParameter".length(), m.getName().length());
                        pName = lowerFirstLetter(pName); 
                        Parameter param = new Parameter(pName, ParameterType.valueOf(m.getParameterTypes()[0]));
                        //add the parameter to the result.
                        if (null != m.getDefaultValue()) {
                            param.setValue(m.getDefaultValue().toString());
                        }
                        
                        //read the default value for the parameter from annotations.
                        try {
                            Class<?> defaultValueClass = loader.loadClass(ANNOTATION_CLASS);
                            @SuppressWarnings({ "unchecked", "rawtypes" })
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
                            logger.info("ERROR: Unable to load Annotation Class: '" + ANNOTATION_CLASS + "'");
                            logger.info("The used version of StormCommons does not support Annotations!");
                        } catch (ClassCastException e) {
                            e.printStackTrace();
                            logger.info("ERROR: Unable to cast: '" + ANNOTATION_CLASS + "' to Annotation.");
                        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        result.add(param); 
                    } 
                }
            }
        } catch (ClassNotFoundException | SecurityException | ManifestUtilsException e) {
            e.printStackTrace();
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
        
        //load all necessary classes for the analysis
        try {
            URL[] urls = new URL[1];
            loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls)); 
            Class<?> base = loadClass(name, loader);

            //TODO: remove?
            ManifestParser mp = new ManifestParser();
            Manifest genManifest = mp.createFromClass(base);
            mp.writeToFile(new File(out + "/generated_manifest.xml"), genManifest);
            
            //if the base class of the algorithm is available proceed to read in/output.
            if (null != base) {
                Method[] methods = base.getMethods();      
                List<String> inOut = new ArrayList<String>();
                result = getAlgorithmInOut(loader, methods, getInput, inOut);
                
                //if no in/output was found it is probably a sink and the information has to be read
                //from different methods.
                if (result.isEmpty()) {
                    result = getSinkInOut(loader, methods, getInput, inOut);
                }
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
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
            //get the input information.
            if (!getInput && m.getName().startsWith("get") 
                    && !m.getName().startsWith("getParameter")) {

                Class<?> param = m.getReturnType();
                inOut.add(param.getName());
                io = loader.loadClass(param.getName());
                itemName = decrypt(param);
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);

                    //prevent doubles.
                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                }                
                
            //get the output information.
            } else if (getInput && m.getName().startsWith("post")) {

                Class<?>[] param = m.getParameterTypes();
                for (int i = 0; i < param.length; i++) {
                    inOut.add(param[i].getName());
                    io = loader.loadClass(param[i].getName());
                    itemName = decrypt(param[i]);
                }
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);
                    
                    //prevent doubles.
                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                } else {
                    logger.info("No Input/Output found for sink-methods. May be an algorithm."); 
                    //not fatal since it could be an algorithm and not a sink.
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
                    //get input information
                    if (getInput && param[i].getName().toLowerCase().contains("input")) {
                        io = loader.loadClass(param[i].getName());
                        itemName = decrypt(param[i]);
                    //get output information
                    } else if (!getInput && param[i].getName().toLowerCase().contains("output")) {
                        io = loader.loadClass(param[i].getName());
                        itemName = decrypt(param[i]);
                    }         
                }
                if (null != io) { 
                    Item newItem = getSetter(io, itemName);
                    //prevent doubles.
                    if (!doubleItems(result, newItem)) {
                        result.add(newItem);
                        io = null;
                    }
                } else {
                    logger.info("No Input/Output found for algorithm-methods. May be a sink.");
                    //not fatal since it could be a sink and not an algorithm.
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
     * Returns a Field for a given item name.
     * @param io the IO class.
     * @param itemName the name for the item.
     * @return Item an item.
     */
    private Item getSetter(Class<?> io, String itemName) {
        
        Item result = null;

        Item item = new Item();
        item.setName(itemName);
        for (Method met : io.getMethods()) {
            if (met.getName().contains("set")) {
                //get the class type and get the name of that type
                Class<?> cls = met.getParameterTypes()[0];
                String string = met.getName().substring(3);
                char[] c = string.toCharArray();
                c[0] = Character.toLowerCase(c[0]);
                string = new String(c);
                
                FieldType fType = null;
                
                //find the corresponding type in our type list.
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
            
            logger.info("Found jar: " + file.getAbsolutePath());
            
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
     * @param file The file to publish (should be a *.jar) <br>
     *        Example: "C:/Artifacts/test.jar"
     * @param pomPath The pom file for publishing.
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param forceOverwrite forces an overwrite in the nexus.
     * @param monitor The ProgressObserver to track progress.
     */
    public void publishWithPom(String file, String pomPath, String repository, boolean forceOverwrite, 
            ProgressObserver monitor) {
        
        logger.info("Publishing with POM...");
        logger.info("Path of pom: " + pomPath); 
        
        PomInfo info = PomReader.getInfo(new File(pomPath));     
        if (null != info) {
            System.out.println("PomInfo: " + info.toString());
        }
                
        publish(file, repository, info, forceOverwrite, monitor);

    }
    
    /**
     * Publishes an artifact.
     * @param file The file to publish (should be a *.jar) <br>
     *        Example: "C:/Artifacts/test.jar"
     * @param repository The nexus repository. <br>
     *        Example: "https://nexus.sse.uni-hildesheim.de/content/repositories/qmproject/"
     * @param info Pom information.
     * @param forceOverwrite forces an overwrite in the nexus.
     * @param monitor The ProgressObserver to track progress.
     */
    public void publish(String file, String repository, 
            PomInfo info, boolean forceOverwrite, ProgressObserver monitor) {
        
        IBiblioResolver testRes = new IBiblioResolver();
        testRes.setName("publish_" + repository);
        testRes.setRoot(repository);     
        
        //calculate target paths.
        String targetPath = file.substring(0, file.lastIndexOf(File.separator));
        String pomPath = targetPath.substring(0, targetPath.lastIndexOf(File.separator));
        logger.info("Target path for pom: " + pomPath);
        
        //generate meta data for the artifact.
        MetaGenerator gen = new MetaGenerator();
        Metadata meta = new Metadata();
        meta.setLastUpdated(meta.generateTimestamp());
        meta.setArtifactId(info.getArtifactId());
        meta.setGroupId(info.getGroupId());
        meta.addVersion(info.getVersion());
        if (!info.getVersion().contains("SNAPSHOT")) {
            meta.setReleaseVersion(info.getVersion());
        }
        gen.writeMetaData(meta, new File(targetPath + "/" + METAFILE_NAME));
        
        //upload all files.
        try {
            URL dest = new URL(testRes.getRoot());
            
            String preDestination = dest + "/" + info.getGroupPath() 
                + info.getArtifactId() + "/";
            String destination = preDestination + info.getVersion() + "/";
            
            FTPSConnector.getInstance().initialize(dest);
            FTPSHandler urlPostHandler = new FTPSHandler();
            
            //upload the pom
            urlPostHandler.uploadWithoutIvy(new File(pomPath + "/" + POM_NAME), 
                new URL(destination + info.getArtifactId() + "-" + info.getVersion() 
                + POM_DEST_NAME), monitor);
            //upload the jar with dependencies
            urlPostHandler.uploadWithoutIvy(new File(targetPath + "/" + info.getArtifactId() + "-" + info.getVersion() 
                + JAR_WITH_DEPENDENCIES), new URL(destination + info.getArtifactId() + "-" + info.getVersion() 
                + JAR_WITH_DEPENDENCIES), monitor);
            //upload the jar
            urlPostHandler.uploadWithoutIvy(new File(targetPath + "/" + info.getArtifactId() + "-" + info.getVersion() 
                + JAR), new URL(destination + info.getArtifactId() + "-" + info.getVersion() 
                + JAR), monitor);
            //upload the sources
            urlPostHandler.uploadWithoutIvy(new File(targetPath + "/" + info.getArtifactId() + "-" + info.getVersion() 
                + SOURCES_JAR), new URL(destination + info.getArtifactId() + "-" + info.getVersion() 
                + SOURCES_JAR), monitor);
            //upload the metadata
            urlPostHandler.uploadWithoutIvy(new File(targetPath + "/" + METAFILE_NAME), 
                new URL(preDestination + METAFILE_NAME), monitor);

        } catch (IOException exc) {
            exc.printStackTrace();
        } catch (IllegalStateException exc) {
            exc.printStackTrace();
        } finally {
            FTPSConnector.getInstance().disconnect();
        }
        
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
            
            //generate the URL
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
                    // Which class does this zip entry represent?
                    String className = entry.getName().replace('/', '.'); // including ".class"
                    classNames.add(className.substring(0, className.length() - ".class".length()));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
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
                //do nothing!
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
              //do nothing!
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
              //do nothing!
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

                //some jars have the shaded suffix and can not be found otherwise.
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
                     // Which class does this zip entry represent?
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
     
        if (null != report && !report.getArtifacts().isEmpty()) {
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
    
}
