package eu.qualimaster.manifestUtils.data;

/**
 * Represents the type of a Field used by an Item.
 * @author pastuschek
 *
 */
public enum FieldType {
    
    INTEGER, STRING, STRINGLIST, BOOLEAN, REAL, DOUBLE, LONG, OBJECT, RISK, IFEVENTLIST, UNKNOWN;

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
    
    /**
     * Alternative to the implicit method.
     * @param name The name to check.
     * @return A fitting FieldType. UNKNOWN if no matching type was found.
     */
    public static FieldType value(String name) {
        FieldType result = null;
        
        try {
            result = FieldType.valueOf(name);
        } catch (IllegalArgumentException exc) {
            result = FieldType.UNKNOWN;
        }
        
        return result;
    }
    
    /**
     * Returns the normalized name of the FieldType.
     * @return The normalized name (capitalized first letter).
     */
    public String getNormalizedName() {
        return this.name().substring(0, 1).toUpperCase() + this.name().substring(1).toLowerCase();
    }
    
}
