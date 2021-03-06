package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * Updates the internal metadata of a snapshot.
     * @param file The metadata.xml of the snapshots.
     * @param timestamp The new timestamp for the snapshot.
     * @return The new version number.
     */
    public String updateInternalMetadata(File file, String timestamp) {
        
        String version = null;
        String snapshotVersion = "";
            
        if (file != null && file.length() > 0) {
            
            try { 
                
                doc = builder.parse(file);   
                Element root = doc.getDocumentElement();
                
                if (root.getNodeName().equals("metadata")) {
                    
                    Node versionNode = getFirstChildByName(root, "version");
                    Node versioningNode = getFirstChildByName(root, "versioning");
                    Node snapshotNode = getFirstChildByName(versioningNode, "snapshot");
                    Node stampNode = getFirstChildByName(snapshotNode, "timestamp");
                    Node buildNode = getFirstChildByName(snapshotNode, "buildNumber");
                    Node lastUpdatedNode = getFirstChildByName(versioningNode, "lastUpdated");
                    Node snapshotVersionsNode = getFirstChildByName(versioningNode, "snapshotVersions");
                    
                    stampNode.setTextContent(timestamp);
                    snapshotVersion = versionNode.getTextContent();
                    snapshotVersion = snapshotVersion.substring(0, snapshotVersion.indexOf("-"));

                    try {
                        int versionNumber = Integer.valueOf(buildNode.getTextContent());
                        version = "" + (versionNumber + 1);
                    } catch (NumberFormatException exc) {
                        exc.printStackTrace();
                    }
                    buildNode.setTextContent(version);
                    lastUpdatedNode.setTextContent(timestamp.replace(".", ""));
                    List<Node> snapshotNodes = getAllChildrenByName(snapshotVersionsNode, "snapshotVersion");
                    
                    for (Node node : snapshotNodes) {                    
                        Node valueNode = getFirstChildByName(node, "value");
                        Node updateNode =  getFirstChildByName(node, "updated");               
                        valueNode.setTextContent(snapshotVersion + "-" + timestamp + "-" + version);
                        updateNode.setTextContent(timestamp.replace(".", ""));                    
                    }
                    
                    //update the actual XML
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(file);    
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");     
                    try {
                        transformer.transform(source, result);
                    } catch (TransformerException e) {
                        e.printStackTrace();
                    }   
                }
                
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
        return version;      
    }
    
    /**
     * Writes a complete and new metadata file.
     * @param artifactId the ID of the artifact.
     * @param groupId the ID of the group.
     * @param version The version.
     * @param target The file to write to.
     * @param timestamp The most recent time stamp.
     */
    public void writeNewInternalMetadata(String artifactId, String groupId, 
            String version, File target, String timestamp) {
        
        String timestampNoDot = timestamp.replace(".", "");
        Document doc = builder.newDocument();   
        Element root = doc.createElement("metadata");
        root.setAttribute("modelVersion", "1.1.0");
        doc.appendChild(root);
        //create necessary elements
        Element groupIdNode = doc.createElement("groupId");
        groupIdNode.setTextContent(groupId);
        Element artifactIdNode = doc.createElement("artifactId");
        artifactIdNode.setTextContent(artifactId);
        Element versionNode = doc.createElement("version");
        versionNode.setTextContent(version);
        Element versioning = doc.createElement("versioning"); //empty
        Element snapshot = doc.createElement("snapshot"); //empty
        Element timestampElement = doc.createElement("timestamp");
        timestampElement.setTextContent(timestamp);
        Element buildNumber = doc.createElement("buildNumber");
        buildNumber.setTextContent("1");
        Element lastUpdated = doc.createElement("lastUpdated");
        lastUpdated.setTextContent(timestampNoDot);
        Element snapshotVersions = doc.createElement("snapshotVersions"); //empty
        root.appendChild(groupIdNode);
        root.appendChild(artifactIdNode);
        root.appendChild(versionNode);
        root.appendChild(versioning);    
        versioning.appendChild(snapshot);
        snapshot.appendChild(timestampElement);
        snapshot.appendChild(buildNumber);
        versioning.appendChild(lastUpdated);
        versioning.appendChild(snapshotVersions);   
        String jarDep = "jar-with-dependenices";
        String source = "sources";
        String snapshotVer = "";
        snapshotVer = versionNode.getTextContent();
        snapshotVer = snapshotVer.substring(0, snapshotVer.indexOf("-"));
        
        for (int i = 0; i < 4; i++) {
 
            Element snapshotVersion = doc.createElement("snapshotVersion"); //empty  
            snapshotVersions.appendChild(snapshotVersion);
            String ext = "jar";
            String classif = null; 
            if (i == 1) {
                ext = "pom";
            }
            if (i == 2) {
                classif = source;
            } else if (i == 3) {
                classif = jarDep;
            }     
            if (null != classif) {
                Element classifier = doc.createElement("classifier");
                classifier.setTextContent(classif);
                snapshotVersion.appendChild(classifier);
            }
            Element extension = doc.createElement("extension");
            snapshotVersion.appendChild(extension);
            extension.setTextContent(ext);
            Element value = doc.createElement("value");
            snapshotVersion.appendChild(value);
            value.setTextContent(snapshotVer + "-" + timestamp + "-" + "1");
            Element updated = doc.createElement("updated");
            snapshotVersion.appendChild(updated);
            updated.setTextContent(timestampNoDot); 
        }

        writeToFile(target, doc);
    }
    
    /**
     * Writes an XML document to a file.
     * @param target The file.
     * @param doc The document.
     */
    private void writeToFile(File target, Document doc) {
        //create the actual XML
        DOMSource sourceDom = new DOMSource(doc);
        StreamResult result = new StreamResult(target);    
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        try {
            transformer.transform(sourceDom, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }    
    }
    
    /**
     * Returns the first child of a given node with a given name.
     * @param node The given node.
     * @param name The given name.
     * @return The first child that matches the name or null if none exists.
     */
    private Node getFirstChildByName(Node node, String name) {
        
        Node result = null;
        for (int i = 0; (result == null) && (i < node.getChildNodes().getLength()); i++) {
            
            if (node.getChildNodes().item(i).getNodeName().equals(name)) {
                
                result = node.getChildNodes().item(i);
                
            }
            
        }
        return result;
        
    }
    
    /**
     * Returns all children of a given node with a given name.
     * @param node The given node.
     * @param name The given name.
     * @return A List of all Nodes that match the name. Can be empty but never null.
     */
    private List<Node> getAllChildrenByName(Node node, String name) {
        
        List<Node> result = new ArrayList<Node>();
        
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            
            if (node.getChildNodes().item(i).getNodeName().equals(name)) {
                
                result.add(node.getChildNodes().item(i));
                
            }
            
        }
        
        return result;
        
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
