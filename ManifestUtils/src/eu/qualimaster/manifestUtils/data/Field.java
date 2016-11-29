package eu.qualimaster.manifestUtils.data;

/**
 * represents a singular Field of an Item.
 * @author pastuschek
 *
 */
public class Field {

    private String name;
    private FieldType type;
    private String literalType;
    
    /**
     * Simple Constructor for a field.
     * Needs a name and a FieldType, since the Field can not be changed later.
     * @param name The name of the Field as String.
     * @param type The type of  the Field.
     * @param literalType The literal name of the type.
     */
    public Field(String name, FieldType type, String literalType) {
        this.name = name;
        this.type = type;
        this.literalType = literalType;
    }
    
    /**
     * Returns the name of the Field.
     * @return The name of the Field as String.
     */
    public String getName() {
        return this.name;
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
            Field other = (Field) obj;
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

    /**
     * Returns the FieldType of the Field.
     * @return The type of the Field as FieldType.
     */
    public FieldType getFieldType() {
        return this.type;
    }
    
    /**
     * Returns the literal name of the type.
     * @return The name of the type as String.
     */
    public String getLiteralType() {
        return this.literalType;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ("Field: " + name + ", Type: " + type + "\n");
    }
    
}
