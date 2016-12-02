package test.eu.qualimaster.manifestUtils.data;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;

/**
 * Test case for the field data class used by the manifestUtils.
 * @author pastuschek
 *
 */
public class FieldTest {
    
    /**
     * Test case for the constructor.
     */
    @Test
    public void testConstructor() {
        
        Field field = new Field("Test", FieldType.INTEGER, FieldType.INTEGER.name());
        
        Assert.assertEquals(field.getName(), "Test");
        Assert.assertEquals(field.getFieldType(), FieldType.INTEGER);
        
    }
    
    /**
     * Test case for the overridden equals method.
     */
    @Test
    public void testEquals() {
        
        Field fieldA = new Field("Test", FieldType.BOOLEAN, FieldType.BOOLEAN.name());
        Field fieldB = new Field("Test", FieldType.BOOLEAN, FieldType.BOOLEAN.name());
        Field fieldC = new Field("Test", FieldType.INTEGER, FieldType.INTEGER.name());
        Field fieldD = new Field("Different", FieldType.BOOLEAN, FieldType.BOOLEAN.name());
        Field fieldE = new Field("Different", FieldType.INTEGER, FieldType.INTEGER.name());
        
        Assert.assertEquals(fieldA, fieldA);
        Assert.assertEquals(fieldA, fieldB);
        
        Assert.assertNotEquals(fieldA, fieldC);
        Assert.assertNotEquals(fieldA, fieldD);
        Assert.assertNotEquals(fieldA, fieldE);
 
    }

}
