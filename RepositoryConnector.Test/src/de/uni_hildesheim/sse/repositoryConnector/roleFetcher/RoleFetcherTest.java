package de.uni_hildesheim.sse.repositoryConnector.roleFetcher;

import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import de.uni_hildesheim.sse.repositoryConnector.AbstractRepositoryConnectorTest;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.ApplicationRole;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;

/**
 * Test Case for the {@link RoleFetcher}.
 * 
 * @author Sass
 * 
 */
public class RoleFetcherTest extends AbstractRepositoryConnectorTest {

    /**
     * Test if all roles can be fetched from the XML file.
     */
    @Test
    public void testGetUserRoles() {
        Set<Role> roles = RoleFetcher.getUserRoles();
        Assert.assertNotNull("Could not fetch roles", roles);
        Assert.assertEquals(3, roles.size());
        Assert.assertEquals(true, roles.contains(ApplicationRole.ADMIN));
        Set<Role> interfaceRoles = getSvnConnector().getRoles();
        Assert.assertEquals(3, interfaceRoles.size());
        Assert.assertEquals(true, interfaceRoles.contains(ApplicationRole.ADMIN));
        Assert.assertEquals(false, interfaceRoles.contains("admin"));
        Role role = null;
        Assert.assertEquals(false, interfaceRoles.contains(role));
    }

}
