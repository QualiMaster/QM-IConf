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

import org.apache.ivy.Ivy;
import org.apache.ivy.ant.IvyConvertPom;
import org.apache.ivy.core.module.descriptor.DefaultDependencyDescriptor;
import org.apache.ivy.core.module.descriptor.DefaultModuleDescriptor;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.publish.PublishOptions;
import org.apache.ivy.core.report.ResolveReport;
import org.apache.ivy.core.resolve.ResolveOptions;
import org.apache.ivy.core.retrieve.RetrieveOptions;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.resolver.ChainResolver;
import org.apache.ivy.plugins.resolver.IBiblioResolver;
import org.apache.tools.ant.Project;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver.ITask;
import eu.qualimaster.manifestUtils.PomReader.PomInfo;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.JarInfo;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;

/**
 * Builds and handles connections to the repository server.
 * @author pastuschek
 *
 */
public class ManifestConnection {

    private static File ivyOut = null;
    
    private static List<String> repositories = new ArrayList<String>();
    
    private Ivy ivy = null;
    private File out = null;
    private String output = "";
    private Interceptor sysOut;
    
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
                if (string.contains("found")) {
                    if (prog < max) {
                        monitor.notifyProgress(mainTask, prog);
                        this.prog = this.prog + 1;
                    } else {
                        //VERY DIRTY!!!
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
        
        if (null == ivyOut) {
            setRetrievalFolder(new File("C:/Test/out"));
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
        
        try {
            sysOut = new Interceptor(output, new PrintStream(new FileOutputStream(FileDescriptor.out)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        IvySettings ivySettings = new IvySettings();
        
        //Set the maven repository as default.
        String mavenPath = System.getenv("M2_HOME");
        if (null == mavenPath || mavenPath.isEmpty()) {
            mavenPath = "C:/.m2/repository";
        }
        ivySettings.setDefaultCache(new File(mavenPath));
        ivySettings.setDefaultCacheArtifactPattern("[organisation]/[module]/[revision]/[module]-[revision].[ext]");
        ivySettings.setDefaultCacheIvyPattern("[organisation]/[module]/[revision]/[module]-[revision].[ext]");
        
        ChainResolver chainResolver = new ChainResolver();
        chainResolver.setName("chainResolver");
        
        List<IBiblioResolver> resolvers = ManifestParserUtils.getResolver(repositories);
        for (IBiblioResolver resolver : resolvers) {
            chainResolver.add(resolver);
        }
        
        ivySettings.addResolver(chainResolver);
        ivySettings.setDefaultResolver(chainResolver.getName());
        this.ivy = Ivy.newInstance(ivySettings);
        
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
     * Sets the retireval folder where jars are retrieved to, even if they exist in the local Maven repos, 
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
    public void addCredentials(String username, String password) {
        
        org.apache.ivy.util.url.CredentialsStore.INSTANCE
            .addCredentials("Sonatype Nexus Repository Manager", "nexus.sse.uni-hildesheim.de", 
                username, password);
        
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
        ResolveReport report = null;
        ModuleDescriptor m = null; 
        try {
            monitor.notifyStart(mainTask, 100);
            sysOut.setMax(100);
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
        
        try {
            
            URL[] urls = new URL[1];
            URLClassLoader loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls));        
            
            Class<?> base = loader.loadClass(name);   
            
            Method[] methods = base.getMethods();
            
            for (Method m : methods) {

                if (m.getName().startsWith("setParameter")) {
                 
                    String pName = m.getName().substring("setParameter".length(), m.getName().length());
                    pName = lowerFirstLetter(pName);
                    
                    Parameter param = new Parameter(pName, ParameterType.valueOf(m.getParameterTypes()[0]));
                    if (null != m.getDefaultValue()) {
                        param.setValue(m.getDefaultValue().toString());
                    }
                    result.add(param);
                    
                }
                
            }
            
            loader.close();
            
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return result;
        
    }
    
    /**
     * Returns all input fields of the given artifact.
     * @param name The name of the original class.
     * @param artifactId The id of the artifact.
     * @return A list with all input fields. Can be empty but never null.
     */
    public List<Item> getInput(String name, String artifactId) {
        return getInOut(name, artifactId, true);
    }
    
    /**
     * Returns all output fields of the given artifact.
     * @param name The name of the original class.
     * @param artifactId The id of the artifact.
     * @return A list with all output fields. Can be empty but never null.
     */
    public List<Item> getOutput(String name, String artifactId) {
        return getInOut(name, artifactId, false);
    }
    
    /**
     * Returns the class of given name of the resolved dependecy.
     * @param name The name of the class. Can be null?
     * @param artifactId the id of the artifact.
     * @param getInput true if input should be returned, false for output.
     * @return A list of fields for in/output.
     */
    private List<Item> getInOut(String name, String artifactId, boolean getInput) {
        
        List<Item> result = new ArrayList<Item>();
        URLClassLoader loader = null;
        
        try {
            URL[] urls = new URL[1];
            loader = new URLClassLoader(loadJars(out.getAbsolutePath()).toArray(urls));
            
            if (extractManifest(artifactId)) {
                ManifestParser mp = new ManifestParser();
                Manifest manifest = mp.parseFile(new File(out + "/manifest.xml"));
                if (null != manifest.getProvider()) {
                    name = manifest.getProvider();
                }    
            } 
            
            if (null != name && !name.isEmpty()) {
                
                Class<?> io = null;
                String itemName = null;
                //String realName = name.split("\\.")[name.split("\\.").length - 1];        
                Class<?> base = loader.loadClass(name);
                
                Method[] methods = base.getMethods();      
                
                List<String> inOut = new ArrayList<String>();
                for (Method m : methods) {
                    if (m.getName().equals("calculate")) {
                        Class<?>[] param = m.getParameterTypes();
                        for (int i = 0; i < param.length; i++) {
                            inOut.add(param[i].getName());
                            //int index = param[i].getName().lastIndexOf(realName);
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
            }             
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
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
    private String decrypt(Class<?> param) {
        String itemName = "";
        try {
            String[] splited = param.getName().split("\\.");
            String[] splited2 = splited[splited.length - 1].split("\\$");
            itemName = param.getSimpleName().substring(
                    param.getSimpleName().indexOf(splited2[0]) + splited2[0].length());
            itemName = lowerFirstLetter(itemName);
        } catch (ArrayIndexOutOfBoundsException e) {
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
                            fType = FieldType.valueOf(newName.toUpperCase());
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
            
            if (!file.isDirectory()) {
                try {
                    list.add(new URL("file:///" + out.getAbsolutePath() + "\\" + file.getName()));
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
     */
    public void publish(ModuleRevisionId ri, Collection<Pattern> srcArtifactPattern, 
            String resolverName, PublishOptions options) {

        System.out.println("Attempting to publish...");
        System.out.println("ModuleRevisionId == " + ri.getName());
        System.out.println("Resolver Name == " + resolverName);
        
        try {
            ivy.publish(ri, srcArtifactPattern, resolverName, options);
        } catch (IOException exc) {
            exc.printStackTrace();
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
        ivyFile = new File(ivyFile.getAbsolutePath() + "\\ivy.xml");
        
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
     * Extracts the manifest from a jar file.
     * @param artifactId The name of the jar file.
     * @return false if the process failed.
     */
    private boolean extractManifest(String artifactId) {
        
        boolean success = true;
        URL url;
        OutputStream outStream = null;
        
        try {
            
            url = new URL("jar:file:/" + out.getAbsolutePath() + "/" + artifactId + ".jar!/manifest.xml");
            InputStream is = url.openStream();
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
     * Returns a list of all class names inside a jar file. Only classes with "Calculate" methods are returned.
     * @param file The jar file.
     * @return A List<String> with the class names inside the jar. Can be empty.
     */
    public List<String> getAllValidClasses(File file) {
        List<String> result = getAllClasses(file);
        
        return result;
    }
    
    /**
     * Quick main method for testing purposes.
     * @param args The arguments of the main method.
     */
    public static void main(String[] args) {
          
        ManifestConnection con = new ManifestConnection();
        
        ManifestConnection.addRepository("http://nexus.sse.uni-hildesheim.de/releases/Qualimaster/");
        ManifestConnection.addRepository("http://clojars.org/repo");
        con.addCredentials("testuser", "nexustest");
        
        File pom = new File("C:/Test/pom.xml");
        
        String groupId = "eu.qualimaster";
        String artifactId = "hy-preprocessor";
        String version = "3.0-SNAPSHOT";
        
        con.load(null, groupId, artifactId, version);
        //List<File> list = con.getJarFiles(groupId, artifactId, version);
        List<File> list = con.getJarFiles(pom);
        
        System.out.println("Jar Files:");
        
        con.validateJarList(list, true);
        
        for (File a : list) {
            System.out.println(a.getAbsolutePath());
            if (!a.exists()) {
                System.err.println(a.getAbsolutePath());
            }
        }
        
        System.out.println("Jar Files End!");
        System.out.println("\n\n--------------\n\n");
        System.out.println("Classes for " + list.get(0) + ": ");
        
        List<String> classes = con.getAllClasses(list.get(0));
        for (String s : classes) {
            System.out.println(s);
        }
        System.out.println("Classes End!");
        
//        con.load(groupId, artifactId, version);
//        
//        System.out.println("ARTIFACT DOWNLOADED!");
//        ManifestParser parser = new ManifestParser();
//        
//        Manifest manifest = parser.parseFile(new File("H:/Desktop/manifest.xml"));
//        String name = "SwitchProcessor1";
//
//        parser.validate(name, groupId, artifactId, version, manifest);
//        System.out.println("MANIFEST UPDATED!");
        
    }

}
