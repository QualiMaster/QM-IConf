package test.eu.qualimaster.manifestUtils.data;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;

/**
 * Test cases for the algorithm data class used by the manifestUtils.
 * @author pastuschek
 *
 */
public class AlgorithmTest {

    /**
     * Test case for the constructor.
     */
    @Test
    public void testConstructor() {
        
        Algorithm predAlg = new Algorithm();
        Algorithm alg = new Algorithm("Name", "Artifact", "Topology", predAlg);
        
        Assert.assertEquals(alg.getName(), "Name");
        Assert.assertEquals(alg.getArtifact(), "Artifact");
        Assert.assertEquals(alg.getAlgTopologyClass(), "Topology");
        Assert.assertEquals(alg.getPredecessor(), predAlg);
        
    }
    
    /**
     * Test case for the setter methods.
     */
    @Test
    public void testSetter() {
        
        Algorithm alg = new Algorithm();
        
        alg.setAlgTopologyClass("Topology");
        alg.setArtifact("Artifact");
        
        Assert.assertEquals(alg.getArtifact(), "Artifact");
        Assert.assertEquals(alg.getAlgTopologyClass(), "Topology");
        
    }
    
    /**
     * Test case for the 'has' methods.
     */
    @Test
    public void testHasMethods() {
        
        Item input = new Item();
        Item output = new Item();
        Parameter param = new Parameter("Test", ParameterType.BOOLEAN);
        
        Algorithm alg = new Algorithm();
        
        Assert.assertFalse(alg.hasInput(input));
        Assert.assertFalse(alg.hasOutput(output));
        Assert.assertFalse(alg.hasParameter(param));
        
        alg.addInput(input);
        alg.addOutput(output);
        alg.addParameter(param);
        
        Assert.assertTrue(alg.hasInput(input));
        Assert.assertTrue(alg.hasOutput(output));
        Assert.assertTrue(alg.hasParameter(param));
        
    }
    
}
