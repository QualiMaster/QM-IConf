package eu.qualimaster.manifestUtils.data;


/**
 * Represents the type of a Field used by an Item.
 * @author pastuschek
 *
 */
public enum FieldType {
    
    INTEGER, STRING, BOOLEAN, REAL, DOUBLE, LONG, OBJECT, RISK, UNKNOWN;

    /**
     * Returns a FieldType that fits the given FieldType class.
     * @param type The actual type of the field.
     * @return The converted FieldType.
     */
    public static FieldType valueOf(Class<?> type) {
        
        FieldType result = null;
        
        try {

            String name = type.getSimpleName().toUpperCase();
            
            if (name.equals("INT")) {
                name = "INTEGER";
            }
            
            result = FieldType.valueOf(name);
            
        } catch (IllegalArgumentException exc) {
            //Means, that we have no type for this input (yet)...
            result = UNKNOWN;
        }
        
        return result;
    }
    
}
