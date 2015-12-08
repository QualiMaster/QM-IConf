package test.eu.qualimaster.manifestUtils.data;

import org.junit.Assert;
import org.junit.Test;

import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.FieldType;
import eu.qualimaster.manifestUtils.data.Item;

/**
 * Test case for the Item data class.
 * @author pastuschek
 *
 */
public class ItemTest {
    
    /**
     * Test case for the constructor.
     */
    @Test
    public void testConstructor() {
        
        Item item = new Item();
        Field field = new Field("Test", FieldType.INTEGER);
        
        Assert.assertTrue(item.getFields().isEmpty());
        
        item.addField(field);
        
        Assert.assertFalse(item.getFields().isEmpty());
        
        Assert.assertTrue(item.getFields().size() == 1);
        
        Assert.assertEquals(item.getFields().iterator().next(), field);
        
    }
    
    /**
     * Test case for the overridden equals method.
     */
    @Test
    public void testEquals() {
        
        Item itemA = new Item();
        Item itemB = new Item();
        
        Field fieldA = new Field("Test", FieldType.INTEGER);
        Field fieldB = new Field("Test", FieldType.INTEGER);
        Field fieldC = new Field("Different", FieldType.BOOLEAN);
        
        Assert.assertEquals(itemA, itemB);
        
        itemA.addField(fieldA);
        
        Assert.assertNotEquals(itemA, itemB);
        
        itemB.addField(fieldA);
        
        Assert.assertEquals(itemA, itemB);
        
        itemB = new Item();
        itemB.addField(fieldB);
        
        Assert.assertEquals(itemA, itemB);
        
        itemB = new Item();
        itemB.addField(fieldC);
        
        Assert.assertNotEquals(itemA, itemB);
        
    }

}
