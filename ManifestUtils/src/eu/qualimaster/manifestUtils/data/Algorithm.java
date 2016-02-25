package eu.qualimaster.manifestUtils.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents an algorithm.
 * @author pastuschek
 *
 */
public class Algorithm {

    /**The name of the Algorithm.*/
    protected String name;
    /**The underlying artifact.*/
    protected String artifact;
    /**The topology class of the Algorithm.*/
    protected String algTopologyClass;
    
    /**The predecessor of the algorithm.*/
    private Algorithm predecessor;
    /**A list of input items.*/
    private List<Item> input = new ArrayList<Item>();
    /**A list of output items.*/
    private List<Item> output = new ArrayList<Item>();
    
    private List<Parameter> parameters = new ArrayList<Parameter>();
    
    /**Empty (default) constructor.*/
    public Algorithm() {}
    
    /**Constructor to create an Algorithm with a certain set of data.
     * @param name The name of the Algorithm.
     * @param artifact The underlying artifact.
     * @param algTopologyClass The topology class of the Algorithm.
     * @param predecessor The predecessor.
     */
    public Algorithm(String name, String artifact, String algTopologyClass, Algorithm predecessor) {
        
        this.name = name;
        this.artifact = artifact;
        this.algTopologyClass = algTopologyClass;
        this.predecessor = predecessor;
        
    }
    
    /**
     * Adds an Input Item.
     * @param item The Item to add.
     */
    public void addInput(Item item) {
        this.input.add(item);
    }
    
    /**
     * Adds an Output Item.
     * @param item The Item to add.
     */
    public void addOutput(Item item) {
        this.output.add(item);
    }
    
    /**
     * Adds a Parameter.
     * @param param The Parameter to add.
     */
    public void addParameter(Parameter param) {
        this.parameters.add(param);
    }
    
    /**
     * Sets the topology class for this Algorithm.
     * @param alg The topology class.
     */
    public void setAlgTopologyClass(String alg) {
        this.algTopologyClass = alg;
    }
    
    /**
     * Sets the underlying artifact.
     * @param artifact The artifact as String.
     */
    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }
    
    /**
     * Returns a Collection of Items for the Input of the Algorithm.
     * @return A Collection<Item> of Input-Items.
     */
    public Collection<Item> getInput() {
        return Collections.unmodifiableList(input);
    }
    
    /**
     * Returns a Collection of Items for the Output of the Algorithm.
     * @return A Collection<Item> of Output-Items.
     */
    public Collection<Item> getOutput() {
        return Collections.unmodifiableList(output);
    }
    
    /**
     * Returns a Collection of Parameters for the Algorithm.
     * @return A Collection<Parameter> of the Algorithms Parameters.
     */
    public Collection<Parameter> getParameters() {
        return Collections.unmodifiableList(parameters);
    }
    
    /**
     * Returns the name of the Algorithm as String.
     * @return String name of the Algorithm.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the Artifact of the Algorithm.
     * @return The Artifact as String.
     */
    public String getArtifact() {
        return this.artifact;
    }
    
    /**
     * Returns the topology class of the Algorithm.
     * @return The topology class as String.
     */
    public String getAlgTopologyClass() {
        return this.algTopologyClass;
    }
    
    /**
     * Returns the predecessor of this Algorithm.
     * @return The predecessor-Algorithm.
     */
    public Algorithm getPredecessor() {
        return this.predecessor;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() { 
        String result = "";
        result += ("Algorithm: " + this.name + "\n");
        result += ("Artifact: " + this.artifact + "\n");
        result += ("AlgTopologyClass: " + this.algTopologyClass + "\n");
        result += ("INPUT: \n");
        for (Item item : this.input) {
            result += item.toString();
        }
        result += ("OUTPUT: \n");
        for (Item item : this.output) {
            result += item.toString();
        }
        result += ("PARAMETERS: \n");
        for (Parameter param : this.parameters) {
            result += param.toString();
        }
        result += " ";
        return result;
    }
    
    /**
     * Returns true if this algorithm has a matching input.
     * @param item The item to compare.
     * @return True if there is a matching input, false otherwise.
     */
    public boolean hasInput(Item item) {
        
        boolean result = false;
        
        for (Item i : this.input) {
            if (i.equals(item)) {
                result = true;
                break;
            }
        }
        
        return result;
        
    }
    
    /**
     * Returns true if this algorithm has a matching output.
     * @param item The item to compare.
     * @return True if there is a matching output, false otherwise.
     */
    public boolean hasOutput(Item item) {
        
        boolean result = false;
        
        for (Item i : this.output) {
            if (i.equals(item)) {
                result = true;
                break;
            }
        }
        
        return result;
        
    }
    
    /**
     * Returns true if the algorithm has a matching parameter.
     * @param param The parameter to compare.
     * @return True if there is a matching parameter, false otherwise.
     */
    public boolean hasParameter(Parameter param) {
        
        boolean result = false;
        
        for (Parameter parameter : this.parameters) {
            if (param.equals(parameter)) {
                result = true;
                break;
            }
        }
        
        return result;
        
    }
    
}
