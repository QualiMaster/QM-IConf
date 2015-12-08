package de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Roles in the XML file annotated with JAXB annotation to convert the XML into a java object.
 * 
 * @author Sass
 * 
 */
@XmlRootElement(name = "role")
public class Role {

    private String id;

    /**
     * Default constructor.
     */
    public Role() {
        // nothing to do here!
    }

    /**
     * Convenience Constructor. Use this constructor to create a new role object.
     * 
     * @param id
     *            The id of the role
     */
    public Role(String id) {
        this.id = id;
    }

    /**
     * References to the name of the role.
     * 
     * @return The name of the role
     */
    @XmlAttribute
    public String getId() {
        return id;
    }

    /**
     * References to the name of the role.
     * 
     * @param id
     *            The name of the role
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Using Guava to compare provided object for equality.
     * 
     * @param object
     *            Object to be compared for equality.
     * @return {@code true} if provided object is considered equal or {@code false} if provided object is not considered
     *         equal.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (getClass() != object.getClass()) {
            return false;
        }
        final Role other = (Role) object;

        return com.google.common.base.Objects.equal(this.id, other.id);
    }

    /**
     * Uses Guava to assist in providing hash code of this user instance.
     * 
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(this.id);
    }
    
    @Override
    public String toString() {
        return getId();
    }
}
