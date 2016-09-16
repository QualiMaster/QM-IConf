package eu.qualimaster.manifestUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Manifest.ManifestType;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import eu.qualimaster.manifestUtils.data.Parameter;

/**
 * The ManifestParser analyzes manifest data of QM Algorithm Packages, and distributes the generated data. 
 * @author Patrik Pastuschek
 *
 */
public class ManifestParser {

    //The following finals define the XML node names for necessary data.
    private static final String MANIFEST = "manifest";
    private static final String PROVIDES = "provides";
    private static final String ALGORITHM = "algorithm";
    private static final String BYPASS = "bypass";
    private static final String DESCRIPTION = "description";
    private static final String INPUT = "input";
    private static final String OUTPUT = "output";
    private static final String PARAMETER = "parameter";
    private static final String REQUIRES = "requires";
    private static final String QUALITY = "quality";
    private static final String QUALITY_PARAMETER = "qualityParameter";
    private static final String CONSTANT = "constant";
    private static final String LEVEL = "level";
    private static final String FLOW = "flow";
    private static final String FIELD = "field";
    private static final String TUPLE = "tuple";
    private static final String COMPONENT = "component";
    
    //The following finals define XML attributes for necessary data.
    private static final String CLASS = "class";
    private static final String SOURCE = "source";
    private static final String SINK = "sink";
    private static final String COMMAND = "command";
    private static final String TOPOLOGY = "topology";
    private static final String FAMILY = "family";
    private static final String PARAM = "param";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String DEFAULT_VALUE = "defaultValue";
    private static final String TO = "to";
    private static final String CPU = "cpu";
    private static final String DFE = "dfe";
    private static final String VALUE = "value";
    private static final String LOAD = "load";
    
    //XML reader objects.
    private DocumentBuilderFactory factory;
    private DocumentBuilder builder;
    private TransformerFactory transformerFactory;
    private Transformer transformer;
    private Document doc;
    
    private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(ManifestParser.class, 
            "eu.qualimaster.ManifestUtils");
    
    /**
     * Constructor, initializes certain builder and factories used by w3c dom.
     */
    public ManifestParser() {
        //Initialize the XML reader.
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
     * Parses a manifest into a representative manifest data structure.
     * @param file The file of the manifest.
     * @return Manifest The representative manifest.
     * @throws ManifestUtilsException if the Manifest has an error.
     */
    public Manifest parseFile(File file) throws ManifestUtilsException {
        
        Manifest manifest = null;
        
        try { 
            
            if (file != null && file.length() > 0) {
                
                try { 
                    
                    doc = builder.parse(file);
                    manifest = analyseManifest(doc);
                    
                } catch (SAXException exc) {           
                    exc.printStackTrace();           
                } catch (IOException exc) {             
                    exc.printStackTrace();       
                } catch (IllegalArgumentException exc) {
                    exc.printStackTrace();
                }
                
            } else {
                
                logger.warn("Unable to parse manifest: " + file.getAbsolutePath());
                
            }
        
        } catch (NullPointerException e) {
            
            throw new ManifestUtilsException("Manifest parsing error: " + e.getMessage());
            
        }
            
        return manifest;
        
    }
    
    /**
     * Prints a XML-Node tree. Utility for bug fixing.
     * @param elem The root element.
     */
    @SuppressWarnings("unused")
    private void printTree(Node elem) {
        
        for (int i = 0; i < elem.getChildNodes().getLength(); i++) {
            
            System.out.print(elem.getChildNodes().item(i).getNodeName() + " ");
            System.out.println(elem.getChildNodes().item(i).getNodeValue());
            printTree(elem.getChildNodes().item(i));
            
        }
    }
    
    /**
     * Analyzes a tuple at a given node and returns a list with items within that tuple.
     * @param node The node of the tuple.
     * @return List<Item> a List of Items within the tuple.
     */
    private List<Item> getTuples(Node node) {
        
        List<Item> items = new ArrayList<Item>();
        
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            
            Node subNode = node.getChildNodes().item(i);
            
            //read all tuples
            if (TUPLE.equals(subNode.getNodeName())) {
                
                Item item = new Item();
                
                for (int j = 0; j < subNode.getChildNodes().getLength(); j++) {
                    
                    Node subNode2 = subNode.getChildNodes().item(j);
                    
                    //read all fields for the current tuple
                    if (FIELD.equals(subNode2.getNodeName())) {
                        
                        String name = subNode2.getAttributes().getNamedItem(NAME).getNodeValue();
                        String type = subNode2.getAttributes().getNamedItem(TYPE).getNodeValue();
                        
                        try {
                            
                            //try to create the actual Field object.
                            Field field = new Field(name, FieldType.valueOf(type.toUpperCase()));
                            item.addField(field);
                            
                        } catch (IllegalArgumentException exc) {
                            
                            logger.warn("[Error:] At Tuple-Node: " + j + ", name: " + name 
                                    + ", illegal field declaration!");
                            
                        }
                        
                    }
                    
                }
                
                items.add(item);
                
            }
            
        }
        
        return items;
        
    }
    
    /**
     * Actually analyzes the manifest-node-tree and returns a representative data structure.
     * @param doc The document that contains the manifest.
     * @return Family the representative data structure. Can be null.
     */
    private Manifest analyseManifest(Document doc) {
        
        Manifest family = new Manifest("Manifest");
        ManifestType mType = null;
        
        if (null != doc) {
        
            Element elem = doc.getDocumentElement();
            
            //read the manifest if available.
            if (null != elem && MANIFEST.equals(elem.getNodeName())) {
                
                for (int i = 0; i < elem.getChildNodes().getLength(); i++) {
                    
                    Node node = elem.getChildNodes().item(i);
                    
                    readProvides(mType, node, family);
                    
                    //this is actually not entirely defined yet:
                    if (null != node && REQUIRES.equals(node.getNodeName())) {
                        
                        try {
                            
                            node.getAttributes().getNamedItem(CPU).getNodeValue(); 
                            node.getAttributes().getNamedItem(DFE).getNodeValue(); 
                            
                        } catch (NullPointerException exc) {
                            //is optional, so nothing to worry here!
                        }
                        
                    }
                    
                    readQuality(node);
                    
                }
                
            }
            
            //mark the family as MultiHardware if several hardware definitions were found.
            if (null != family.getMembers() && family.getMembers().size() > 1 
                    && mType == ManifestType.SINGLE_HARDWARE) {
                
                mType = ManifestType.MULTI_HARDWARE;
                
            }
        
        }
        
        return family;
        
    }
    
    /**
     * Reads the class (type) of the manifest, from the given Node.
     * @param mType The ManifestType to save the information to.
     * @param item The item to analyse.
     * @param manifest The manifest to edit.
     * @return mType the type of manifest.
     */
    private ManifestType readClass(ManifestType mType, Node item, Manifest manifest) {
        
        if (null != item) {
            
            if (CLASS.equals(item.getNodeName())) {
                
                mType = ManifestType.SINGLE_JAVA;
                
            } else if (SOURCE.equals(item.getNodeName())) {
                
                mType = ManifestType.PIPELINE_SOURCE;
                
            } else if (SINK.equals(item.getNodeName())) {
                
                mType = ManifestType.PIPELINE_SINK;
                
            } else if (COMMAND.equals(item.getNodeName())) {
                
                mType = ManifestType.SINGLE_HARDWARE;
                
            } else if (TOPOLOGY.equals(item.getNodeName())) {
                
                mType = ManifestType.STORMBASED;
                
            }
            
        }
        
        return mType;
        
    }
    
    /**
     * Reads the PROVIDES information of the Manifest.
     * @param mType The ManifestType to store gained information.
     * @param node The node to read from.
     * @param manifest The family to store gained information to.
     */
    private void readProvides(ManifestType mType, Node node, Manifest manifest) {
        
        //first node defines the family type and gives additional information about the family.
        if (null != node && PROVIDES.equals(node.getNodeName())) {
            
            if (null != node.getAttributes() && node.getAttributes().getLength() > 0) {
                Node item = node.getAttributes().item(0);   
                mType = readClass(mType, item, manifest);
                if (null != mType && null != manifest) {
                    manifest.setType(mType);
                    manifest.setProvider(item.getNodeValue());
                }
            }
            
            //read actual algorithm information.
            for (int j = 0; j < node.getChildNodes().getLength(); j++) {      
                Node subNode = node.getChildNodes().item(j);
                if (null != subNode && ALGORITHM.equals(subNode.getNodeName())) {
                    
                    Algorithm algorithm = null;
                    if (null != subNode.getAttributes()) {
                        
                        String algName = "";
                        Node algNameNode = subNode.getAttributes().getNamedItem(FAMILY);
                        
                        if (null != algNameNode) {
                            algName = algNameNode.getNodeValue();
                        }
                        
                        algorithm = new Algorithm(algName, null, null, null);
                        
                        for (int k = 0; k < subNode.getChildNodes().getLength(); k++) {
                            
                            Node subNode2 = subNode.getChildNodes().item(k);
                            readAdditionalInfo(subNode2, algorithm);
                                              
                        }  
                        
                    }
             
                    if (null != algorithm && null != manifest) {
                        manifest.addMember(algorithm);
                    }       
                } else if (null != subNode && COMPONENT.equals(subNode.getNodeName())) { 
                    //this is not entirely defined yet.
                    subNode.getAttributes().getNamedItem(CLASS).getNodeValue();        
                } else if (null != subNode && DESCRIPTION.equals(subNode.getNodeName())) {
                    manifest.setDescription(ManifestParser.normalizeText(subNode.getTextContent()));
                }
                
            }
            
        }
        
    }
    
    /**
     * Analysis the In-/ Output, Parameter and some additional information from a node.
     * @param node The node to analyze.
     * @param algorithm Extracted info will be added to this algorithm.
     */
    private void readAdditionalInfo(Node node, Algorithm algorithm) {
        
        if (null != algorithm && null != node) {
            //read input tuples
            if (INPUT.equals(node.getNodeName())) {   
                for (Item it : getTuples(node)) {
                    algorithm.addInput(it);
                }     
            //read output tuples
            } else if (OUTPUT.equals(node.getNodeName())) {       
                for (Item it : getTuples(node)) {
                    algorithm.addOutput(it);
                }        
            //read parameters
            } else if (PARAMETER.equals(node.getNodeName())) {
                
                NamedNodeMap map = node.getAttributes();
                if (null != map) {
                    
                    String name = null;
                    String type = null;
                    String defaultValue = null;
                    
                    if (null != map.getNamedItem(NAME)) {
                        name = node.getAttributes().getNamedItem(NAME).getNodeValue();
                    }
                    
                    if (null != map.getNamedItem(TYPE)) {
                        type = node.getAttributes().getNamedItem(TYPE).getNodeValue();
                    }
                    
                    if (null != map.getNamedItem(DEFAULT_VALUE)) {
                        defaultValue = node.getAttributes().getNamedItem(DEFAULT_VALUE).getNodeValue();
                    }
                    
                    //try to create the parameter, can fail if type does not match.
                    try { 
                        if (null != name && null != type) {
                            Parameter param = new Parameter(name, Parameter.ParameterType
                                    .valueOf(type.toUpperCase()));
                            param.setValue(defaultValue);
                            algorithm.addParameter(param); 
                        }
                                      
                    } catch (IllegalArgumentException exc) {  
                        logger.warn("Illegal parameter type: " + type);   
                    } 
                    
                }      
                
            //flow and bypass are not entirely defined yet.
            } else if (FLOW.equals(node.getNodeName())) {       
                try {
                    node.getAttributes().getNamedItem(TO).getNodeValue();
                } catch (NullPointerException exc) {
                }                
            } else if (BYPASS.equals(node.getNodeName())) {              
                try {
                    node.getFirstChild().getNextSibling().getAttributes()
                            .getNamedItem(PARAM).getNodeValue(); 
                } catch (NullPointerException exc) {
                }              
            } else if (DESCRIPTION.equals(node.getNodeName())) {
                algorithm.setDescription(ManifestParser.normalizeText(node.getTextContent()));
            }
        }
        
    }
    
    /**
     * Reads the QUALITY information from a Node.
     * @param node The node to read from.
     */
    private void readQuality(Node node) {
        
        //these parameters are not entirely defined yet and are not used.
        if (null != node && QUALITY.equals(node.getNodeName())) {
            
            for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                
                Node subNode = node.getChildNodes().item(j);
            
                if (null != subNode && BYPASS.equals(subNode.getNodeName())) {
                    System.out.println("");   //TODO: handle this                 
                } else if (QUALITY_PARAMETER.equals(subNode.getNodeName())) {
                    
                    String name = subNode.getAttributes().getNamedItem(NAME).getNodeValue();
                    String value = "";
                    
                    if (CONSTANT.equals(subNode.getFirstChild().getNodeName())) {
                        
                        value = subNode.getFirstChild().getAttributes().getNamedItem(VALUE).getNodeValue();
                        
                    } else if (LEVEL.equals(subNode.getFirstChild().getNodeName())) {
                        
                        name = subNode.getAttributes().getNamedItem(LOAD).getNodeValue();
                        value = subNode.getFirstChild().getAttributes().getNamedItem(VALUE).getNodeValue();
                        
                    }
                    
                    try {
                        System.out.println("");   //TODO: handle this 
                        //Parameter param = new Parameter(name, Parameter.ParameterType.valueOf(value));
                    } catch (IllegalArgumentException exc) {
                        
                        logger.warn("Illegal quality parameter: " + "Name: " + name 
                                + ", Value: " + value);
                        
                    }
                    
                }
                
            }
                
        }
    }

    
    /**
     * Creates a Manifest from a class.
     * Is not currently used but could be useful in the future for automated manifest creation.
     * @param cs The class to parse.
     * @return the created Manifest.
     */
    public Manifest createFromClass(Class<?> cs) {
        
        //initialize an empty undefined manifest.
        Manifest manifest = new Manifest(cs.getSimpleName());
        manifest.setType(ManifestType.UNKNOWN);
        manifest.setProvider(cs.getName());
        Algorithm alg = new Algorithm(cs.getSimpleName(), cs.getName(), null, null);
        manifest.addMember(alg);
        
        //add the available information to the manifest.
        for (Method m : cs.getDeclaredMethods()) {
            
            //these methods define in and output for single java algorithms.
            if (m.getName().equals("calculate")) {
                
                manifest.setType(ManifestType.SINGLE_JAVA);
                
                getInOut(m, alg, cs);
                
            //these methods define parameters.
            } else if (m.getName().startsWith("setParameter")) {
                
                String pName = ManifestConnection.lowerFirstLetter(m.getName().substring(12));
                Class<?>[] param = m.getParameterTypes();
                
                for (Class<?> p : param) {
                    Parameter parameter = new Parameter(pName, Parameter.ParameterType.valueOf(p));
                    alg.addParameter(parameter);
                }
                
            //these methods define in and output for pipeline sinks.
            } else if (m.getName().startsWith("post")) {
                
                manifest.setType(ManifestType.PIPELINE_SINK);
                getInOut(m, alg, cs);
                
            }
            
        }  
        
        //if no manifest type could be detected yet, it is likely a pipeline source algorithm.
        //define it as such and get in and output information.
        if (manifest.getType().equals(ManifestType.UNKNOWN)) {
            manifest.setType(ManifestType.PIPELINE_SOURCE);
            
            for (Method m : cs.getDeclaredMethods()) {
                if (m.getName().startsWith("get")) {
                    
                    getInOut(m, alg, cs);
                    
                }
            }
        }
                
        return manifest;
        
    }
    
    /**
     * Gather in and output.
     * @param method The Method.
     * @param alg The Algorithm.
     * @param cs The class for the classloader.
     */
    private void getInOut(Method method, Algorithm alg, Class<?> cs) {
        Class<?>[] param = method.getParameterTypes();
        
        for (Class<?> p : param) {
            
            try {
                
                Class<?> ioClass = cs.getClassLoader().loadClass(p.getName());
                Method[] methods = ioClass.getMethods();
                
                //get input tuple / field information
                if (p.getName().endsWith("Input")) { 
                    Item item = new Item();
                    for (Method met : methods) {      
                        if (met.getName().startsWith("set")) {
                            
                            item.setName(ManifestConnection.decrypt(p));
                            Field field = new Field(ManifestConnection.lowerFirstLetter(met.getName().substring(3)), 
                                    FieldType.valueOf(met.getParameterTypes()[0]));
                            item.addField(field);
                            
                        }                
                    }
                    alg.addInput(item);
                } else {               
                    //get output tuple / field information.
                    Item item = new Item();
                    for (Method met : methods) {              
                        if (met.getName().startsWith("set")) {
                            
                            item.setName(ManifestConnection.decrypt(p));
                            Field field = new Field(ManifestConnection.lowerFirstLetter(met.getName().substring(3)), 
                                    FieldType.valueOf(met.getParameterTypes()[0]));
                            item.addField(field);
                            
                        }                 
                    }   
                    alg.addOutput(item);
                }
                
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    /**
     * Writes a Manifest to a file. Is not used but may become useful in the future.
     * @param file The file to write to.
     * @param manifest The Manifest to write.
     */
    public void writeToFile(File file, Manifest manifest) {
        
        Document doc = builder.newDocument(); 
        Element root = doc.createElement("manifest");
        doc.appendChild(root);
        
        Element provides = doc.createElement("provides");
        if (manifest.getProvider() != null) {
            String type = null;
            
            switch (manifest.getType().name()) {
            
            case "SINGLE_JAVA":
                type = "class";
                break;
            
            case "SINGLE_HARDWARE":
                type = "command";
                break;
                
            case "STORMBASED":
                type = "stormbased"; //?
                break;
                
            case "MULTI_HARDWARE":
                type = "cmd";
                break;
                
            case "PIPELINE_SOURCE":
                type = "source"; //?
                break;
                
            case "PIPELINE_SINK":
                type = "pipeline"; //?
                break;
                
            default:
                    
                type = "error";
                break;
            
            }
            provides.setAttribute(type, manifest.getProvider());
        }
        root.appendChild(provides);
        
        //add algorithms
        for (Algorithm alg : manifest.getMembers()) {
            Element algorithm = doc.createElement("algorithm");
            algorithm.setAttribute("family", alg.getName());
            provides.appendChild(algorithm);
            
            generateInput(alg, doc, algorithm);   
            generateOutput(alg, doc, algorithm);    
            generateParameters(alg, doc, algorithm);
        }
        
        //write the document.
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(file);    
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        try {
            transformer.transform(source, result);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * Generates the input xml parts for an algorithm. 
     * @param algorithm The algorithm to generate for.
     * @param doc The document to generate for.
     * @param element The parent element.
     */
    private void generateInput(Algorithm algorithm, Document doc, Element element) {
        
        if (!algorithm.getInput().isEmpty()) {
            
            Element input = doc.createElement("input");
            element.appendChild(input);
            
            for (Item item : algorithm.getInput()) {
                
                Element tuple = doc.createElement("tuple");
                tuple.setAttribute("name", item.getName());
                input.appendChild(tuple);
                
                for (Field f : item.getFields()) {
                    
                    Element field = doc.createElement("field");
                    field.setAttribute("name", f.getName());
                    field.setAttribute("type", f.getFieldType().name());     
                    tuple.appendChild(field);
                    
                }
                
            }
            
        }
        
    }
    
    /**
     * Generates the output xml parts for an algorithm. 
     * @param algorithm The algorithm to generate for.
     * @param doc The document to generate for.
     * @param element The parent element.
     */
    private void generateOutput(Algorithm algorithm, Document doc, Element element) {
        
        if (!algorithm.getOutput().isEmpty()) {
            
            Element output = doc.createElement("output");
            element.appendChild(output);
            
            for (Item item : algorithm.getOutput()) {
                
                Element tuple = doc.createElement("tuple");
                tuple.setAttribute("name", item.getName());
                output.appendChild(tuple);
                
                for (Field f : item.getFields()) {
                    
                    Element field = doc.createElement("field");
                    field.setAttribute("name", f.getName());
                    field.setAttribute("type", f.getFieldType().name());     
                    tuple.appendChild(field);
                    
                }
                
            }
            
        }
        
    }
    
    /**
     * Generates the parameter xml part for an algorithm.
     * @param algorithm The algorithm to generate for.
     * @param doc The doc to generate to.
     * @param element The parent element.
     */
    private void generateParameters(Algorithm algorithm, Document doc, Element element) {
        
        if (!algorithm.getParameters().isEmpty()) {
            
            for (Parameter param : algorithm.getParameters()) {
                
                Element parameter = doc.createElement("parameter");
                parameter.setAttribute("name", param.getName());
                parameter.setAttribute("type", param.getType().name());
                element.appendChild(parameter);
                
            }
            
        }
        
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
                    list.add(new URL("file:///" + dir.getAbsolutePath() + "\\" + file.getName()));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
            
        }
        
        return list;
        
    }
    
    /**
     * Normalizes XML text content.
     * @param text The original text content. Null will return null.
     * @return The normalized text content. If null is the input, null will be the output aswell.
     */
    public static String normalizeText(String text) {
        
        String result = "";
        
        if (null != text) {
            
            String[] splitted = text.split("\r?\n|\r");
            for (int i = 0; i < splitted.length; i++) {
                result += splitted[i].trim();
                if (i < splitted.length - 1) {
                    result += System.lineSeparator();
                }
            }
            
        } else {
            result = null;
        }
        
        return result;
        
    }
    
}
