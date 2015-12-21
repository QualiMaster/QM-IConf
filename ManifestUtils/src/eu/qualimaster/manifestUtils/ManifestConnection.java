package eu.qualimaster.manifestUtils;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.Security;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

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

import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;

/**
 * Builds and handles connections to the repository server.
 * @author pastuschek
 *
 */
public class ManifestConnection {

    private static List<String> repositories = new ArrayList<String>();
    private Ivy ivy = null;
    private File out = null;
    private String output = "";
    private PrintStream sysOut;

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
        
        /**
         * Simple constructor.
         * @param output the output String.
         * @throws FileNotFoundException 
         */
        public Interceptor(String output) throws FileNotFoundException {
            super(System.out, true);
            this.output = output;           
        }
        
        /**
         * Redirect the intercepted output.
         */
        @Override
        public void print(String string) {
            this.output += (string);
        }
        
        /**
         * Redirect the intercepted output.
         */
        @Override
        public void println(String string) {
            this.output += ("\n" + string);
        }
        
        /**
         * Returns the intercepted output.
         * @return The intercepted output String.
         */
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
        
        this.out = new File("out");   
        
        try {
            sysOut = new Interceptor(output);
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
            System.out.println(resolver.getPattern());
            chainResolver.add(resolver);
        }
        
        ivySettings.addResolver(chainResolver);
        ivySettings.setDefaultResolver(chainResolver.getName());
        this.ivy = Ivy.newInstance(ivySettings);
        
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
     * Adds a Maven repository for artifact resolution.
     * 
     * @param repository the repository URL (<b>null</b> or empty is ignored) 
     */
    public static void addRepository(String repository) {
        addRepository(repository, repositories.size());
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
     * The main to test things out.
     * @param groupId the Id of the group.
     * @param artifactId the Id of the artifact.
     * @param version the target version of the artifact.
     */
    public void load(String groupId, String artifactId, String version) {

        System.setOut(sysOut);
        System.setErr(sysOut);
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
        ResolveReport rr = null; 
        ModuleDescriptor m = null; 
        try {
            rr = ivy.resolve(md, ro);
            m = rr.getModuleDescriptor();
            if (rr.hasError()) {
                throw new RuntimeException(rr.getAllProblemMessages().toString());
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
        System.out.println("INTERCEPTED OUTPUT: ");
        System.out.println(((Interceptor) sysOut).getOutput());
        System.out.println("INTERCEPTION END");
        
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
                 
                    result.add(new Parameter(m.getName(), ParameterType.valueOf(m.getParameterTypes()[0])));
                    
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
                String realName = name.split("\\.")[name.split("\\.").length - 1];        
                Class<?> base = loader.loadClass(name);
                
                Method[] methods = base.getMethods();      
                
                List<String> inOut = new ArrayList<String>();
                for (Method m : methods) {
                    if (m.getName().equals("calculate")) {
                        Class<?>[] param = m.getParameterTypes();
                        for (int i = 0; i < param.length; i++) {
                            inOut.add(param[i].getName());
                            int index = param[i].getName().lastIndexOf(realName);
                            if (getInput && param[i].getName().toLowerCase().contains("input")) {
                                io = loader.loadClass(param[i].getName());
                                itemName = param[i].getName().substring(
                                        index + realName.length(), 
                                        param[i].getName().length() - 5);
                            } else if (!getInput && param[i].getName().toLowerCase().contains("output")) {
                                io = loader.loadClass(param[i].getName());
                                itemName = param[i].getName().substring(
                                        index + realName.length(), 
                                        param[i].getName().length() - 6);
                            }         
                        }
                        System.out.println();
                    }
                }
                if (null != io) { 
                    result.add(getSetter(io, itemName));
                } else {
                    System.out.println("UNABLE TO FIND IN / OUTPUT!"); 
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
                item.addField(new Field(string, FieldType.valueOf(cls)));
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
     * Resolves a certain dependency.
     */
    public void resolve() {
        
        //TODO: move the resolve part here, for better overview...
        
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
     * Quick main method for testing purposes.
     * @param args The arguments of the main method.
     */
    public static void main(String[] args) {
          
        ManifestConnection con = new ManifestConnection();
        
        con.addCredentials("testuser", "nexustest");
        
        String groupId = "eu.qualimaster";
        String artifactId = "SwitchProcessor1";
        String version = "0.0.1-SNAPSHOT";
        
        con.load(groupId, artifactId, version);
        
        System.out.println("ARTIFACT DOWNLOADED!");
        ManifestParser parser = new ManifestParser();
        
        Manifest manifest = parser.parseFile(new File("H:/Desktop/manifest.xml"));
        String name = "SwitchProcessor1";

        parser.validate(name, groupId, artifactId, version, manifest);
        System.out.println("MANIFEST UPDATED!");
        
    }

}
