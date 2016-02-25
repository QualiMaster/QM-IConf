package test.eu.qualimaster.manifestUtils.data;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.data.Parameter;
import eu.qualimaster.manifestUtils.data.Parameter.ParameterType;

/**
 * Test cases for the parameter data class used by the manifestUtils.
 * @author pastuschek
 *
 */
public class ParameterTest {

    /**
     * Test case for the constructor.
     */
    @Test
    public void testConstructor() {
        
        Parameter param = new Parameter("Test", ParameterType.BOOLEAN);
        
        Assert.assertEquals(param.getName(), "Test");
        Assert.assertEquals(param.getType(), ParameterType.BOOLEAN);
        
    }
    
    /**
     * Test case for the overridden equals method.
     */
    @Test
    public void testEquals() {
        
        Parameter paramA = new Parameter("Test", ParameterType.BOOLEAN);
        Parameter paramB = new Parameter("Test", ParameterType.BOOLEAN);
        Parameter paramC = new Parameter("Different", ParameterType.BOOLEAN);
        Parameter paramD = new Parameter("Test", ParameterType.INTEGER);
        Parameter paramE = new Parameter("Different", ParameterType.INTEGER);
        
        Assert.assertEquals(paramA, paramA);
        Assert.assertEquals(paramA, paramB);
        Assert.assertNotEquals(paramA, paramC);
        Assert.assertNotEquals(paramA, paramD);
        Assert.assertNotEquals(paramA, paramE);
        
    }
    
}
