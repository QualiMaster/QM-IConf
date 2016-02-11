package eu.qualimaster.manifestUtils.data;

//TODO: default value!!!
/**
 * Represents a singular Parameter of an Algorithm.
 * @author pastuschek
 *
 */
public class Parameter {

    private String name;
    private ParameterType type;
    private String value;
    
    /**
     * Represents the type of the Parameter.
     * @author pastuschek
     */
    public enum ParameterType {
        
        STRING, BOOLEAN, REAL, INTEGER, LONG, UNKNOWN;
        
        /**
         * Returns a FieldType that fits the given FieldType class.
         * @param type The actual type of the field.
         * @return The converted FieldType.
         */
        public static ParameterType valueOf(Class<?> type) {
            
            ParameterType result = null;
            
            try {
                
                String name = type.getName().toUpperCase();
                
                if (name.equals("DOUBLE")) {
                    name = "REAL";
                }
                
                if (name.equals("INT")) {
                    name = "INTEGER";
                }
                
                result = ParameterType.valueOf(name);
                
            } catch (IllegalArgumentException exc) {
                //Means, that we have no type for this input (yet)...
                result = UNKNOWN;
            } catch (NullPointerException exc) {
                result = UNKNOWN;
            }
            
            return result;
        }
        
    }
    
    /**
     * Simple constructor, which needs a name and a ParameterType.
     * @param name The name of the Parameter as String.
     * @param type The type of the Parameter as ParameterType.
     */
    public Parameter(String name, ParameterType type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * Advanced constructor, which needs a name and a ParameterType and also a (default) value.
     * @param name The name of the Parameter as String.
     * @param type The type of the Parameter as ParameterType.
     * @param value The (default) value of the Paramater.
     */
    public Parameter(String name, ParameterType type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }
    
    /**
     * Sets the (default) value for this parameter.
     * @param value The default value.
     */
    public void setValue(String value) {
        this.value = value;
    }
    
    /**
     * Returns the (default) value of the parameter.
     * @return The (default) value as String.
     */
    public String getValue() {
        return this.value;
    }
    
    /**
     * Returns the name of the Parameter.
     * @return The name as String.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the name of the Parameter in normalized form (first letter capitalized, rest lowercase).
     * @return The name in normalized form.
     */
    public String getNormalizedTypeName() {
        return this.type.name().substring(0, 1).toUpperCase() + this.type.name().substring(1).toLowerCase();
    }

    /**
     * Returns the type of the Parameter.
     * @return The type as ParameterType.
     */
    public ParameterType getType() {
        return this.type;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {      
        return ("Parameter: " + name + ", Type: " + type + "\n");      
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
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
            Parameter other = (Parameter) obj;
            if (name == null) {
                if (other.name != null) {
                    result = false;
                }
            } else if (!name.equals(other.name)) {
                result = false;
            } else if (type != other.type) {
                result = false;
            } else {
                result = true;
            }
        }
        return result;
    }
    
}
