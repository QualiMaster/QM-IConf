package de.uni_hildesheim.sse.repositoryConnector;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.RoleFetcherTest;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnectorTest;

/**
 * Test Suite for running all test cases. 
 * 
 * @author Sass
 *
 */
@RunWith(Suite.class)
@SuiteClasses({
    SVNConnectorTest.class,
    RoleFetcherTest.class
    })
public class AllTests {

}

