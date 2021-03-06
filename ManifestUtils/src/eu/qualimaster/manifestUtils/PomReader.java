package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Implements some functions regarding pom files.
 * @author Patu
 *
 */
public class PomReader {

    /**
     * Simple wrapper class for extracted information.
     * @author Patu
     *
     */
    public static class PomInfo {
        
        private String fullPath = null;
        private String groupId = null;
        private String artifactId = null;
        private String version = null;
        private String timestamp = null;
        
        /**
         * Returns the fullPath.
         * @return String fullPath.
         */
        public String getFullPath() {
            return this.fullPath;
        }
        
        /**
         * Returns the GroupId.
         * @return String GroupId.
         */
        public String getGroupId() {
            return this.groupId;
        }
        
        /**
         * Returns the GroupId as path.
         * @return String GroupId as path (i.e. eu/qualimaster/).
         */
        public String getGroupPath() {
            String result = "";
            for (String s : this.groupId.split("\\.")) {
                result += s + "/";
            }
            return result;
        }
        
        /**
         * Returns the ArtifactId.
         * @return String ArtifactId.
         */
        public String getArtifactId() {
            return this.artifactId;
        }
        
        /**
         * Returns the Version.
         * @return String Version.
         */
        public String getVersion() {
            return this.version;
        }
        
        @Override
        public String toString() {
            return this.groupId + "#" + this.artifactId + "#" + this.version;
        }
        
        /**
         * Returns the timestamp.
         * @return The timestamp as String, can be null.
         */
        public String getTimestamp() {
            return this.timestamp;
        }
        
        /**
         * Sets a new timestamp.
         * @param timestamp The new timestamp.
         */
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
        
    }
    
    /**
     * Returns the full classpath from a pom file.
     * @param file The pom file to read from.
     * @return The full classpath or NULL if no classpath was available.
     */
    public static PomInfo getInfo(File file) {
        
        //initialize
        PomInfo result = new PomInfo();
        
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        Document doc = null;
        
        //parse the file
        if (file != null && file.exists() && file.length() > 0) {
            
            try { 
                
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                doc = builder.parse(file);
                
            } catch (SAXException exc) {
                
                exc.printStackTrace();
                
            } catch (IOException exc) {
                
                exc.printStackTrace();
                
            } catch (ParserConfigurationException e) {
                
                e.printStackTrace();
                
            }
            
            Element root = doc.getDocumentElement();
            NodeList list = root.getChildNodes();
            
            //read in the pom information if available.
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                if (node.getNodeName().equalsIgnoreCase("groupId")) {
                    result.groupId = node.getTextContent();
                }
                if (node.getNodeName().equalsIgnoreCase("artifactId")) {
                    result.artifactId = node.getTextContent();
                }
                if (node.getNodeName().equalsIgnoreCase("version")) {
                    result.version = node.getTextContent();
                }
            }
            
        } else {      
            System.out.println("FAILED!");   
        }
        
        //generate the full path if possible (example: de.uni-hildesheim.sse.ivml)
        if (null != result.groupId && null != result.artifactId) {
            result.fullPath = result.groupId + "." + result.artifactId;
        } else {
            result = null;
        }
        
        return result;
        
    }
    
}
