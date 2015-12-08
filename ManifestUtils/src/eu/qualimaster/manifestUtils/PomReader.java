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
     * Returns the full classpath from a pom file.
     * @param file The pom file to read from.
     * @return The full classpath or NULL if no classpath was available.
     */
    public static String getFullClasspath(File file) {
        
        DocumentBuilderFactory factory = null;
        DocumentBuilder builder = null;
        //TransformerFactory transformerFactory = null;
        //Transformer transformer = null;
        Document doc = null;
        
        String groupId = null;
        String artifactId = null;
        
        String result = null;
        
        if (file != null && file.length() > 0) {
            
            try { 
                
                factory = DocumentBuilderFactory.newInstance();
                builder = factory.newDocumentBuilder();
                //transformerFactory = TransformerFactory.newInstance();
                //transformer = transformerFactory.newTransformer();
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
            
            for (int i = 0; i < list.getLength(); i++) {
                Node node = list.item(i);
                //System.out.println(node.getNodeName() + " = " + node.getTextContent());
                if (node.getNodeName().equalsIgnoreCase("groupId")) {
                    groupId = node.getTextContent();
                }
                if (node.getNodeName().equalsIgnoreCase("artifactId")) {
                    artifactId = node.getTextContent();
                }
            }
            
        } else {      
            System.out.println("FAILED!");   
        }
        
        if (null != groupId && null != artifactId) {
            result = groupId + "." + artifactId;
        } else {
            result = null;
        }
        
        return result;
        
    }
    
    /**
     * Main method for testing purposes.
     * @param args String arguments.
     */
    public static void main(String[] args) {
        
        String test = "";
        File file = new File("C:/Users/Patu/Desktop/test.pom");
        test = getFullClasspath(file);
        System.out.println(test);
        
    }
    
}
