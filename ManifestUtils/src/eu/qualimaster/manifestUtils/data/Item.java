package eu.qualimaster.manifestUtils.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Represents a singular Item (either input or output) of an Algorithm.
 * @author pastuschek
 *
 */
public class Item {

    private List<Field> field = new ArrayList<Field>();
    private String name = "Unknown Name";
    
    /**
     * Adds a Field to the Item.
     * @param field The Field to add.
     */
    public void addField(Field field) {
        this.field.add(field);
    }
    
    /**
     * Returns a Collection of Fields of the Item.
     * @return A Collection<Field> of the Items Fields.
     */
    public Collection<Field> getFields() {
        return Collections.unmodifiableList(field);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String result = name + "\n";
        for (Field f: field) {
            result += f.toString();
        }
        return result;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((field == null) ? 0 : field.hashCode());
        return result;
    }

    /**
     * Returns the name of this item.
     * @return The name of this item or "Unknown Name" if it was not set.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Sets the name for this item.
     * @param name The name for this item.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        boolean result = false;
        if (this == obj) {
            result = true;
        } else if (obj == null) {
            result = false;
        } else if (getClass() != obj.getClass()) {
            result = false;
        } else {
            Item other = (Item) obj;
            if (field == null) {
                if (other.field != null) {
                    result = false;
                }
            } else if (!field.equals(other.field)) {
                result = false;
            } else {
                result = true;
            }
        }
            
        return result;
    }
    
}
