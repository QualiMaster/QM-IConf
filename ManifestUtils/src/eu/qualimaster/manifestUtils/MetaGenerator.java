package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.qualimaster.manifestUtils.data.Metadata;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;

/**
 * Generates metadata for an artifact.
 * @author pastuschek
 *
 */
public class MetaGenerator {

    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private TransformerFactory transformerFactory;
    private Transformer transformer;
    private Document doc;
    
    private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(MetaGenerator.class, 
            "eu.qualimaster.ManifestUtils");
    
    /**
     * Simple Constructor.
     */
    public MetaGenerator() {
        try {
            factory = DocumentBuilderFactory.newInstance();
            builder = factory.newDocumentBuilder();
            transformerFactory = TransformerFactory.newInstance();
            transformer = transformerFactory.newTransformer();
        } catch (ParserConfigurationException | TransformerConfigurationException exc) {
            exc.printStackTrace();
        }
    }
    
    /**
     * Writes a Metadata set to a file.
     * @param metadata The metadata to write.
     * @param target The target file.
     */
    public void writeMetaData(Metadata metadata, File target) {
        
        Document doc = builder.newDocument();
        
        Element root = doc.createElement("metadata");
        doc.appendChild(root);
        
        //create necessary elements
        Element groupId = doc.createElement("groupId");
        groupId.setTextContent(metadata.getGroupId());
        Element artifactId = doc.createElement("artifactId");
        artifactId.setTextContent(metadata.getArtifactId());
        Element versioning = doc.createElement("versioning");
        Element release = doc.createElement("release");
        release.setTextContent(metadata.getReleaseVersion());
        Element versions = doc.createElement("versions");
        Element lastUpdated = doc.createElement("lastUpdated");
        lastUpdated.setTextContent(metadata.getLastUpdated());  
        
        //append all the elements in appropriate places
        root.appendChild(groupId);
        root.appendChild(artifactId);
        root.appendChild(versioning);
        versioning.appendChild(release);
        versioning.appendChild(versions);
        
        for (String ver : metadata.getVersions()) {
            Element version = doc.createElement("version");
            version.setTextContent(ver);
            versions.appendChild(version);
        }
        
        versioning.appendChild(lastUpdated);
        
        //create the actual XML
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(target);    
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        
        
    }
    
    /**
     * Reads the metadata from an existing file.
     * @param file The file of the manifest.
     * @return The read metadata.
     * @throws ManifestUtilsException if the Manifest has an error.
     */
    public Metadata readMetadata(File file) throws ManifestUtilsException {
        
        Metadata metadata = new Metadata();
        
        try { 
            
            if (file != null && file.length() > 0) {
                
                try { 
                    
                    doc = builder.parse(file);
                    metadata = analyzeMetadata(doc);
                    
                } catch (SAXException exc) {           
                    exc.printStackTrace();           
                } catch (IOException exc) {             
                    exc.printStackTrace();       
                } catch (IllegalArgumentException exc) {
                    exc.printStackTrace();
                }
                
            } else {
                
                logger.info("Unable to parse manifest!");
                
            }
        
        } catch (NullPointerException e) {
            throw new ManifestUtilsException("Manifest parsing error: " + e.getMessage());
        }
            
        return metadata;
        
    }
    
    /**
     * Actually analyzes the metadata.
     * @param doc The document to analyze.
     * @return The extracted metadata.
     */
    private Metadata analyzeMetadata(Document doc) {
        
        Metadata metadata = new Metadata();
        
        if (null != doc) {
            
            Element root = doc.getDocumentElement();
            
            if (root.getNodeName().equals("metadata")) {
                
                for (int i = 0; i < root.getChildNodes().getLength(); i++) {
                    
                    Node child = root.getChildNodes().item(i);
                    
                    //try to read available information
                    if (null != child && child.getNodeName().equals("groupId")) {
                        metadata.setGroupId(child.getTextContent());
                    } else if (null != child && child.getNodeName().equals("artifactId")) {
                        metadata.setArtifactId(child.getTextContent());
                    } else if (null != child && child.getNodeName().equals("versioning")) {
                        readVersioning(metadata, child);
                    } 
                    
                }
                
            }
            
        }
        
        return metadata;
        
    }
    
    /**
     * Reads the versioning part of the metadata.
     * @param metadata The metadata to modify.
     * @param node The versioning node.
     */
    private void readVersioning(Metadata metadata, Node node) {
        
        if (null != metadata && null != node) {
            
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                
                Node child = node.getChildNodes().item(i);
                
                if (null != child && child.getNodeName().equals("release")) {
                    metadata.setReleaseVersion(child.getTextContent());
                } else if (null != child && child.getNodeName().equals("versions")) {
                    readVersions(metadata, child);
                } else if (null != child && child.getNodeName().equals("lastUpdated")) {
                    metadata.setLastUpdated(child.getTextContent());
                }
                
            }
            
        }
        
    }
    
    /**
     * Reads the versions part of the metadata.
     * @param metadata The metadata to modify.
     * @param node The versions node.
     */
    private void readVersions(Metadata metadata, Node node) {
        
        if (null != metadata && null != node) {
            
            for (int i = 0; i < node.getChildNodes().getLength(); i++) {
                
                Node child = node.getChildNodes().item(i);
                
                if (null != child && child.getNodeName().equals("version")) {
                    metadata.addVersion(child.getTextContent());
                }
                
            }
            
        }
        
    }
    
}
