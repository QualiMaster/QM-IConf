package de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * User in the XML file annotated with JAXB annotation to convert the XML into 
 * a java object.
 * 
 * @author Sass
 *
 */
@XmlRootElement(name = "user")
public class User {
    
    private String name;
    
    private List<Role> roles;

    /**
     * References to the name of the user.
     * 
     * @return  The name of the user
     */
    @XmlAttribute
    public String getName() {
        return name;
    }

    /**
     * References to the name of the user.
     * 
     * @param name  The name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The list contains all roles assigned to the user.
     * 
     * @return  A {@link List} with all {@link Role}s
     */
    @XmlElement(name = "role")
    public List<Role> getRoles() {
        return roles;
    }

    /**
     * The list contains all roles assigned to the user.
     * 
     * @param roles A {@link List} with all {@link Role}s
     */
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

}
