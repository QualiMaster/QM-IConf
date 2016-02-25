package de.uni_hildesheim.sse.repositoryConnector;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import de.uni_hildesheim.sse.dslCore.test.AbstractTest;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnector;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnectorTest;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * This class contains the @Before and @After test cases for all repository
 * connector test classes. It also contains static variables that can be used 
 * by all test classes.
 * 
 * @author Sass
 *
 */
public class AbstractRepositoryConnectorTest {
    
    public static final File TESTDATA = AbstractTest.determineTestDataDir(
        "de.uni_hildesheim.sse.svn.connector.test.testdata");
    
//    public static final String COMMIT_URL = "https://projects.sse.uni-hildesheim.de/svn/test_svn/Model/QM2";
    public static final String LOCAL_REPO = TESTDATA.getAbsolutePath() + "/svn";
    
    public static final String COMMIT_URL = "file:///" + TESTDATA.getAbsolutePath() + "/svn/Model/QM2";
    
    public static final File SRC_DATA = new File(TESTDATA.getAbsolutePath() + "/repositoryContent/Model");

    public static final File TEMP_DIR = new File(System.getProperty("user.dir") + "/" + TESTDATA + "/tmp");
    
    public static final File TEMP_DIR2 = new File(System.getProperty("user.dir") + "/" + TESTDATA + "/tmp2");

    public static final String USERNAME = "test";

    public static final String PASSWORD = "test";

    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(SVNConnectorTest.class, Bundle.ID);

    private SVNConnector svnConnector;

    /**
     * Connects to the SVN repository and authenticates the user.
     */
    @Before
    public void setUp() {
        setSvnConnector(new SVNConnector());
        getSvnConnector().setRepositoryURL(COMMIT_URL);
        getSvnConnector().authenticate(USERNAME, PASSWORD);
    }

    /**
     * Remove temporary directory.
     */
    @After
    public void tearDown() {
        FileUtils.deleteQuietly(TEMP_DIR);
        FileUtils.deleteQuietly(TEMP_DIR2);
    }
    
    /**
     * Set up the local repository.
     */
    @BeforeClass
    public static void setUpBeforeClass() {
        SVNConnector con = new SVNConnector();
        con.createLocalRepository(LOCAL_REPO);
        con.loadModel(TEMP_DIR);
        try {
            FileUtils.copyDirectoryToDirectory(SRC_DATA, TEMP_DIR);
        } catch (IOException e) {
            logger.exception(e);
        }
        con.storeModel(TEMP_DIR);
        FileUtils.deleteQuietly(TEMP_DIR);
        System.out.println("done");
    }
    
    /**
     * Delete the local repository.
     */
    @AfterClass
    public static void tearDownAfterClass() {
        FileUtils.deleteQuietly(new File(LOCAL_REPO));
    }

    /**
     * Getter for the {@link SVNConnector}.
     * 
     * @return  {@link SVNConnector}
     */
    public SVNConnector getSvnConnector() {
        return svnConnector;
    }

    /**
     * Setter for the {@link SVNConnector}.
     * 
     * @param svnConnector  {@link SVNConnector}
     */
    public void setSvnConnector(SVNConnector svnConnector) {
        this.svnConnector = svnConnector;
    }

    /**
     * Getter for the {@link EASyLogger}.
     * 
     * @return Instance of the logger
     */
    public static EASyLogger getLogger() {
        return logger;
    }

}
