package de.uni_hildesheim.sse.repositoryConnector.roleFetcher;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.io.SVNRepository;

import de.uni_hildesheim.sse.repositoryConnector.Bundle;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Root;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.User;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * Fetches the users and the aligned roles from a XML file that is located in
 * the repository.
 * 
 * @author Sass
 * 
 */
public class RoleFetcher {

    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(RoleFetcher.class, Bundle.ID);

    // Expecting the file in
    // https://projects.sse.uni-hildesheim.de/svn/QualiMaster/trunk
    private static final String FILE = "../userAndRoles.xml";

    private static Root root;

    /**
     * Gets all roles for a specific user.
     * 
     * @return {@link Set} with {@link Role}s
     */
    public static Set<Role> getUserRoles() {
        Set<Role> roles = null;
        for (User user : getUserFromXML(UserContext.INSTANCE.getRepository())) {
            if (user.getName().equals(UserContext.INSTANCE.getUsername())) {
                roles = new HashSet<Role>(user.getRoles());
            }
        }
        return roles;
    }

    /**
     * Returns all users as {@link User}-Objects that are located in the XML
     * file.
     * 
     * @param repository
     *            The {@link SVNRepository}
     * @return {@link Set} with {@link User}.
     */
    private static Set<User> getUserFromXML(SVNRepository repository) {
        List<User> userList = getUserAndRoles(repository).getUsers();
        Set<User> users = null;
        if (userList != null) {
            users = new HashSet<User>(userList);
        }
        return users;
    }

    /**
     * Loads the XML file with users and roles that is located in the
     * repository.
     * 
     * @param repository
     *            The {@link SVNRepository}
     * @return {@link Root}
     */
    private static Root loadUserAndRolesFromXML(SVNRepository repository) {
        Root root = null;
        JAXBContext jaxbContext;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            jaxbContext = JAXBContext.newInstance(Root.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            String url = FILE;
            /* 
             * UGLY CODE: It seems like SvnKit cannot handle '..' within the local file system (i.e. "C:/User/../test").
             * Therefore it has to be removed manually since the userAndRoles.xml is in the parent directory of the
             * repository URL. This is done by comparing the given repository URL and the root URL of the repository.
             * Duplicate strings will be removed resulting in a relative path for the userAndRoles.xml.
             * 
             * This code only affects repositories that are in the local file system.
             */
            if (repository.getLocation().toString().contains("file:///")) {
                String repoRoot = repository.getRepositoryRoot(false).toString();
                // Remove 'file:///' at the beginning of the path for better comparison.
                repoRoot = repoRoot.replace("file:///", "");
                // Full URL including '..'
                String location = repository.getLocation().toString() + "/" + FILE;
                location = location.replace("file:///", "");
                /* 
                 * Create file object with full location so we can use the normalize() method which will parse 
                 * the URL and resolve the '..'
                 */
                File file = new File(location);
                url = file.toPath().normalize().toString();
                // Replace backslashes with slash so we can compare repoRoot with url
                url = url.replace("\\", "/");
                // If url contains the whole repoRoot then cut the string. Only the part that doesn't match is needed.
                if (url.regionMatches(0, repoRoot, 0, repoRoot.length())) {
                    url = url.substring(repoRoot.length(), url.length());
                }
            }
            repository.getFile(url, -1, new SVNProperties(), baos);
            InputStream decodedInput = new ByteArrayInputStream(((ByteArrayOutputStream) baos).toByteArray());
            root = (Root) jaxbUnmarshaller.unmarshal(decodedInput);
        } catch (JAXBException e) {
            logger.warn(e + "");
        } catch (SVNException e) {
            logger.warn(e + "");
        }
        return root;
    }

    /**
     * Loads the roles from a XML file and returns the root of the XML file.
     * 
     * @param repository
     *            {@link SVNRepository}
     * @return {@link Root}
     */
    private static Root getUserAndRoles(SVNRepository repository) {
        if (root == null) {
            root = loadUserAndRolesFromXML(repository);
        }
        return root;
    }
}
