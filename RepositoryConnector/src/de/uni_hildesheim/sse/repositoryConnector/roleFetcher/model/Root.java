package de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Root of the XML file annotated with JAXB annotation to convert the XML into 
 * a java object.
 * 
 * @author Sass
 *
 */
@XmlRootElement(name = "root")
public class Root {
    
    private List<User> users;

    /**
     * The list contains all users in the XML file.
     * 
     * @return  A {@link List} with all {@link User}
     */
    @XmlElement(name = "user")
    public List<User> getUsers() {
        return users;
    }

    /**
     * The list contains all users in the XML file.
     * 
     * @param users List with {@link User}
     */
    public void setUsers(List<User> users) {
        this.users = users;
    }

}
