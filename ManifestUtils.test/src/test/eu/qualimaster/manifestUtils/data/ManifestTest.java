package test.eu.qualimaster.manifestUtils.data;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.Manifest;

/**
 * Test cases for the manifest class used by the manifestUtils.
 * @author pastuschek
 *
 */
public class ManifestTest {
    
    /**
     * Test case for the constructor.
     */
    @Test
    public void testConstructor() {
        
        Manifest man = new Manifest("Test");
        
        Assert.assertEquals(man.getName(), "Test");
        Assert.assertNotEquals(man.getName(), "Noname");
        
    }
    
    /**
     * Test case for the 'has' methods.
     */
    @Test
    public void testHasMethods() {
        
        Manifest man = new Manifest("Test");
        Algorithm alg = new Algorithm("Algorithm", null, null, null);
        
        man.addMember(alg);
        
        Assert.assertTrue(man.hasAlgorithm("Algorithm"));
        
    }

}
