package de.uni_hildesheim.sse.repositoryConnector;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.io.SVNRepository;

import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.ApplicationRole;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * This class contains the user context. This includes the user name and the 
 * password for the repository. Also the repository itself and the roles of the
 * user are included.
 * 
 * @author Sass
 *
 */
public class UserContext {
    
    public static final UserContext INSTANCE = new UserContext();
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(UserContext.class, Bundle.ID);
    
    private static String username;
    
    private static String password;
    
    private static SVNRepository repository;
    
    private static final Set<Role> DEFAULT_ROLES = new HashSet<Role>();
    
    private Set<Role> roles;
    
    static {
        DEFAULT_ROLES.add(ApplicationRole.ADMIN);
    }

    /**
     * Private Singleton constructor, should avoid multiple instances.
     */
    private UserContext() {
        // Exists only to defeat instantiation.
    }

    /**
     * Initializes the UserContext with user name and password.
     * 
     * @param userName  The name of the user
     * @param passWord  The password of the user
     * @param repository    The {@link SVNRepository}
     */
    public void init(String userName, String passWord, SVNRepository repository) {
        setUsername(userName);
        setPassword(passWord);
        setRepository(repository);
    }

    /**
     * Getter for the user name.
     * 
     * @return  The user name as {@link String}
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for the user name.
     * 
     * @param userName  The name of the user as {@link String}
     */
    public static void setUsername(String userName) {
        username = userName;
    }

    /**
     * Getter for the password for the repository.
     * 
     * @return  The password as {@link String}
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for the password for the repository.
     * 
     * @param passWord  The password as {@link String}
     */
    public static void setPassword(String passWord) {
        password = passWord;
    }
    
    /**
     * Getter for the {@link SVNRepository}.
     * 
     * @return {@link SVNRepository}
     */
    public SVNRepository getRepository() {
        return repository;
    }
    
    /**
     * Setter for the {@link SVNRepository}.
     * 
     * @param svnRepository {@link SVNRepository}
     */
    public static void setRepository(SVNRepository svnRepository) {
        repository = svnRepository;
    }

    /**
     * Getter for the Roles of the user. If the {@link Set} is null a default set
     * will be returned including the role for the administrator.
     * 
     * @return {@link Set} with user roles
     */
    public Set<Role> getRoles() {
        return roles == null ? Collections.unmodifiableSet(DEFAULT_ROLES) : roles;
    }
    
    /**
     * Returns whether this user is an administrator. [convenience]
     * 
     * @return <code>true</code> if this user has the role, <code>false</code> else
     */
    public boolean isAdmin() {
        return hasRole(ApplicationRole.ADMIN);
    }

    /**
     * Returns whether this user is an infrastructure administrator. [convenience]
     * 
     * @return <code>true</code> if this user has the role, <code>false</code> else
     */
    public boolean isInfrastructureAdmin() {
        return hasRole(ApplicationRole.ADMIN);
    }
    
    /**
     * Returns whether this user is a pipeline designer. [convenience]
     * 
     * @return <code>true</code> if this user has the role, <code>false</code> else
     */
    public boolean isPipelineDesigner() {
        return hasRole(ApplicationRole.PIPELINE_DESIGNER);
    }

    /**
     * Returns whether this user is an adaptation manager. [convenience]
     * 
     * @return <code>true</code> if this user has the role, <code>false</code> else
     */
    public boolean isAdaptationManager() {
        return hasRole(ApplicationRole.PIPELINE_DESIGNER);
    }
    
    /**
     * Returns whether the current user has a certain role.
     * 
     * @param role the role to look for
     * @return <code>true</code> if the user has the role, <code>false</code> else
     */
    public boolean hasRole(Role role) {
        return getRoles().contains(role);
    }
    
    /**
     * Setter for the roles of the user.
     * 
     * @param roles {@link Set} with user roles
     */
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
    
    /**
     * Getter for the URL of the repository.
     * 
     * @return null or the location of the repository
     */
    public String getRepositoryURL() {
        return repository == null ? null : repository.getLocation().getPath();
    }
    
    /**
     * Setter for the URL of the repository.
     * 
     * @param url   The URL as {@link String}
     */
    public void setRepositoryURL(String url) {
        try {
            repository.setLocation(SVNURL.parseURIEncoded(url), false);
        } catch (SVNException e) {
            logger.exception(e);
        }
    }
}
