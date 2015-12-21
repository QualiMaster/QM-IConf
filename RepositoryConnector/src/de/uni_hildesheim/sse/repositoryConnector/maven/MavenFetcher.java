package de.uni_hildesheim.sse.repositoryConnector.maven;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import de.uni_hildesheim.sse.repositoryConnector.Bundle;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;

/**
 * Class that collects the directories within the given Maven-Repository
 * and puts them in a List. This List will later be used to
 * populate a {@link TreeViewer}.
 * 
 * @author Niko
 */
public class MavenFetcher implements Serializable {

    private static final long serialVersionUID = -1242761422426313072L;
    private static String mavenRepositoryURL;
    private static List<TreeElement> elementTree = new ArrayList<TreeElement>();
    
    /**
     * Defines the repository URL to fetch Maven artifacts from.
     * 
     * @param url the URL (use <b>null</b> or empty to disable this mechanism)
     */
    public static void setRepositoryUrl(String url) {
        mavenRepositoryURL = url;
    }
    
    /**
     * Returns the actual Maven repository URL.
     * 
     * @return the actual URL (may be <b>null</b> or empty)
     */
    public static String getRepositoryUrl() {
        return mavenRepositoryURL;
    }
 
    /**
     * Class which represents a treeElement. Each element has a name and a list of descendants.
     * @author Niko
     */
    public static class TreeElement implements Serializable {
 
        private static final long serialVersionUID = 8802962244171892625L;
        private List<TreeElement> descendants = new ArrayList<TreeElement>();
        private String name = "";
        
        /**
         * Constructs a {@link TreeElement} with given name.
         * @param name Name of the {@link TreeElement}.
         */
        public TreeElement(String name) {
            this.name = name;
        }
 
        /**
         * Constructs a {@link TreeElement} with given name and {@link List} of descendants.
         * @param name Name of the {@link TreeElement}
         * @param descendants Descendants of the newly created {@link TreeElement}.
         */
        public TreeElement(String name, List<TreeElement> descendants) {
            this.name = name;
            this.descendants = descendants;
        }
 
        /**
         * Add decsendat to the {@link TreeElement}.
         * @param descendant This {@link TreeElement} will be added to its parent. 
         */
        public void addTreeElement(TreeElement descendant) {
            descendants.add(descendant);
        }
       
        /**
         * Getter for the name.
         * @return ame of the {@link TreeElement}
         */
        public String getName() {
            return name;
        }
       
        /**
         * Return the children of the {@link TreeElement}.
         * @return descendants Children of this TreeElement.
         */
        public List<TreeElement> getChildren() {
            return descendants;
        }
    }
    
    /**
     * main.
     * @param args .
     * @throws IOException .
     */
    public static void main(String[] args) throws IOException {
 
        collectMavenArtifacts();
    }
 
    /**
     * Collect directories in maven-repository by using {@link Jsoup}.
     * 
     * @throws IOException Exception.
     */
    public static void collectMavenArtifacts() throws IOException {
        if (isConfigured()) {
            //Connect to the repository
            Document doc = Jsoup.connect(mavenRepositoryURL).get();
            for (Element file : doc.select("tr")) {
                Elements img = file.select("td img");
                Elements f = file.select("td a");
                if (1 == img.size() && 1 == f.size()) {
                    String src = img.attr("src");
                    //Take a look at all "folders"
                    if (src.endsWith("folder.gif")) {
                        //Create a TreeElement for each folder.
                        TreeElement element = new TreeElement(f.attr("href"));
                        if (f.attr("href") != null) {
                            //Add all children to the created TreeElement by recursively calling "getDeeperElements.
                            getDeeperElements(mavenRepositoryURL + f.attr("href"), element);
                        }
                        //At last, add each top-level TreeElement to the list.
                        //The Tree is now complete when the recursion is finished.
                        elementTree.add(element);
                    }
                }
            }
        }
    }

    /**
     * Recursive function in order to get the full repository-tree.
     * @param url Current url in the repository.
     * @param treeElement TreeElement which will be extended.
     * @throws IOException Exception.
     */
    public static void getDeeperElements(String url, TreeElement treeElement) throws IOException {
        Document doc = Jsoup.connect(url).get();
        for (Element file : doc.select("tr")) {
            Elements img = file.select("td img");
            Elements f = file.select("td a");
            if (1 == img.size() && 1 == f.size()) {
                String src = img.attr("src");
                //If it is folder go deeper.
                if (src.endsWith("folder.gif")) {
                    if (f.attr("href") != null) {
                        if (!f.attr("href").matches("^\\d+(\\.\\d+) + (\\-([a-zA-Z])*){0,1}?$")) {
                            TreeElement newTreeElement = new TreeElement(f.attr("href"));
                            getDeeperElements(url + f.attr("href"), newTreeElement);
                            treeElement.addTreeElement(newTreeElement);
                        }

                    } 
                }
            }
        }
    }

    /**
     * Return the directory-tree.
     * @return toReturn List of directories in the maven-repository.s
     */
    public static List<TreeElement> getElementTree() {
        try {
            if (elementTree.isEmpty()) {
                collectMavenArtifacts();
            }
        } catch (IOException e) {
            EASyLoggerFactory.INSTANCE.getLogger(MavenFetcher.class, Bundle.ID).error(e.getMessage());
        }
        return elementTree;
    }
    
    /**
     * Check if repository is reachable.
     * 
     * @return whether there is connectivity
     */
    public static boolean checkRepositoryConnectivity() {
        boolean toReturn = true;
        if (!isConfigured()) {
            toReturn = false;
        } else {
            try {
                Jsoup.connect(mavenRepositoryURL).get();
            } catch (IOException exc) {
                toReturn = false;
            }
        }
        return toReturn;
    }
    
    /**
     * Returns whether the Maven artifact fetcher is configured.
     * 
     * @return <code>true</code> if configured, <code>false</code> else
     * @see #setRepositoryUrl(String)
     * @see #getRepositoryUrl()
     */
    public static boolean isConfigured() {
        return (null != mavenRepositoryURL && mavenRepositoryURL.length() > 0);
    }
    
}

