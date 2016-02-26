package de.uni_hildesheim.sse.repositoryConnector.svnConnector;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.uni_hildesheim.sse.repositoryConnector.AbstractRepositoryConnectorTest;

/**
 * Test Case for the {@link SVNConnector}.
 * 
 * @author Sass
 * 
 */
public class SVNConnectorTest extends AbstractRepositoryConnectorTest {
    
    /**
     * Test if a repository can be checked out.
     */
    @Test
    public void testloadModel() {
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            Assert.assertEquals(true, TEMP_DIR.isDirectory());
            int localFiles = FileUtils.listFilesAndDirs(TEMP_DIR, TrueFileFilter.TRUE, 
                    FileFilterUtils.makeSVNAware(null)).size();
            int repositoryFiles = getSvnConnector().getRepositoryEntryCount();
            Assert.assertEquals(repositoryFiles, localFiles - 1);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if files can be committed to the repository.
     */
    @Test
    public void testStoreModel() {
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            int numberOfFiles = TEMP_DIR.list().length;
            File file = new File(TEMP_DIR + "/testStoreModel.txt");
            try {
                file.createNewFile();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            Assert.assertEquals(numberOfFiles + 1, TEMP_DIR.list().length);
            getSvnConnector().storeModel(TEMP_DIR);
            file.delete();
            Assert.assertEquals(numberOfFiles, TEMP_DIR.list().length);
            getSvnConnector().storeModel(TEMP_DIR);
            FileUtils.deleteQuietly(TEMP_DIR);
            Assert.assertEquals(TEMP_DIR.isDirectory(), false);
            getSvnConnector().loadModel(TEMP_DIR);
            Assert.assertEquals(numberOfFiles, TEMP_DIR.list().length);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Test if the user context is set correctly.
     */
    @Test
    public void testGetUserContext() {
        Assert.assertEquals(USERNAME, getSvnConnector().getUserContext().getUsername());
        Assert.assertEquals(PASSWORD, getSvnConnector().getUserContext().getPassword());
        String repoUrl = COMMIT_URL.replace("file://", "");
        repoUrl = repoUrl.replace("\\", "/");
        if (repoUrl.startsWith("//")) {
            repoUrl = repoUrl.substring(1);
        }
        Assert.assertEquals(repoUrl, getSvnConnector().getUserContext().getRepositoryURL());
    }
    
    /**
     * Test if all repository entries can be listed.
     */
    @Ignore("fails after migration to github -> aike")
    @Test
    public void testListEntries() {
        int count = getSvnConnector().getRepositoryEntryCount();
        Assert.assertEquals(63, count);
    }
    
    /**
     * Test if a file that is modified in the same line can be committed.
     */
    @Test
    public void testOutOfDateCommitOverrideLocalChanges() {
        File file = new File(TEMP_DIR + "/testOutOfDateCommit.java");
        File secondWorkingCopy = TEMP_DIR2;
        File file2 = new File(secondWorkingCopy + "/testOutOfDateCommit.java");
        SVNConnector svnConnector = createConflicts(file, secondWorkingCopy, file2);
        try {
            svnConnector.updateModel(secondWorkingCopy);
            svnConnector.resolveConflicts(secondWorkingCopy, false);
            Assert.assertEquals(0, svnConnector.getChangesCount(secondWorkingCopy, false));
            // Check if local changes were overwritten
            try {
                Assert.assertEquals(true, FileUtils.contentEquals(file, file2));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            // Clean up
            file.delete();
            getSvnConnector().storeModel(TEMP_DIR);
            FileUtils.deleteQuietly(secondWorkingCopy);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    /**
     * Creates conflicts in the repository.
     * 
     * @param file first conflict file
     * @param secondWorkingCopy second local working copy
     * @param file2 second conflict file
     * @return {@link SVNConnector}
     */
    private SVNConnector createConflicts(File file, File secondWorkingCopy, File file2) {
        PrintWriter writer;
        // Create second workspace with second user
        SVNConnector svnConnector = new SVNConnector();
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            try {
                file.createNewFile();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            getSvnConnector().storeModel(TEMP_DIR);
            svnConnector.setRepositoryURL(COMMIT_URL);
            svnConnector.authenticate("abc", "def");
            svnConnector.loadModel(secondWorkingCopy);
            Assert.assertEquals(true, file2.exists());
            // Modify file and commit
            try {
                writer = new PrintWriter(file, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            getSvnConnector().storeModel(TEMP_DIR);
            // Modify second file
            try {
                writer = new PrintWriter(file2, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line modified");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            boolean conflict = svnConnector.storeModel(secondWorkingCopy);
            Assert.assertTrue(conflict);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
        return svnConnector;
    }
    
    /**
     * Test if a file that is modified in the same line can be committed.
     */
    @Test
    public void testOutOfDateCommitOverrideRemoteChanges() {
        File file = new File(TEMP_DIR + "/testOutOfDateCommit.java");
        File secondWorkingCopy = TEMP_DIR2;
        File file2 = new File(secondWorkingCopy + "/testOutOfDateCommit.java");
        SVNConnector svnConnector = createConflicts(file, secondWorkingCopy, file2);
        try {
            svnConnector.updateModel(secondWorkingCopy);
            svnConnector.resolveConflicts(secondWorkingCopy, true);
            svnConnector.storeModel(secondWorkingCopy);
            Assert.assertEquals(0, svnConnector.getChangesCount(secondWorkingCopy, false));
            // Check if local changes were overwritten
            try {
                Assert.assertEquals(false, FileUtils.contentEquals(file, file2));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            // Clean up
            file2.delete();
            getSvnConnector().storeModel(secondWorkingCopy);
            FileUtils.deleteQuietly(secondWorkingCopy);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    /**
     * Test if local changes will be overridden.
     */
    @Test
    public void testOverrideLocalChanges() {
        PrintWriter writer;
        String fileName = "testOverrideLocalChanges";
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            File file = createLocalFile(TEMP_DIR, fileName);
            getSvnConnector().storeModel(TEMP_DIR);
            File copiedFile = new File(TEMP_DIR + "/" + fileName + "_copy.java");
            try {
                FileUtils.copyFile(file, copiedFile);
                writer = new PrintWriter(file, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line");
                writer.println("// The third line");
                writer.close();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            getSvnConnector().revert(file);
            try {
                Assert.assertEquals(true, FileUtils.contentEquals(file, copiedFile));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            // Clean up
            file.delete();
            copiedFile.delete();
            getSvnConnector().storeModel(TEMP_DIR);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Creates a local java file in the working copy with two lines of comment.
     * 
     * @param path Path to working copy
     * @param fileName  The filename
     * @return  created File.
     */
    private File createLocalFile(File path, String fileName) {
        PrintWriter writer;
        File file = new File(path + "/" + fileName + ".java");
        try {
            file.createNewFile();
            writer = new PrintWriter(file, "UTF-8");
            writer.println("// The first line");
            writer.println("// The second line");
            writer.close();
        } catch (IOException e) {
            Assert.fail(e.getMessage());
        }
        return file;
    }
    
    /**
     * Test if an update can be performed and conflicts can be resolved.
     */
    @Test
    public void testOverrideUpdate() {
        PrintWriter writer;
        String fileName = "testOverrideUpdate";
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            File file = createLocalFile(TEMP_DIR, fileName);
            int changes = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(1, changes);
            getSvnConnector().storeModel(TEMP_DIR);
            int localFilesCount = FileUtils.listFilesAndDirs(TEMP_DIR, TrueFileFilter.TRUE, 
                    FileFilterUtils.makeSVNAware(null)).size();
            // Create second workspace with second user
            File secondWorkingCopy = TEMP_DIR2;
            SVNConnector svnConnector = new SVNConnector();
            svnConnector.setRepositoryURL(COMMIT_URL);
            svnConnector.authenticate("abc", "def");
            svnConnector.loadModel(secondWorkingCopy);
            File file2 = new File(secondWorkingCopy + "/" + fileName + ".java");
            Assert.assertEquals(true, file2.exists());
            // Modify file and commit
            try {
                writer = new PrintWriter(file, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line");
                writer.println("// The third line");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            changes = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(1, changes);
            getSvnConnector().storeModel(TEMP_DIR);
            // Modify second file
            try {
                writer = new PrintWriter(file2, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line modified");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            // Update the second file and resolve conflicts
            List<File> conflicts = svnConnector.updateModel(secondWorkingCopy);
            Assert.assertEquals(1, conflicts.size());
            svnConnector.resolveConflicts(secondWorkingCopy, false);
            conflicts = svnConnector.updateModel(secondWorkingCopy);
            Assert.assertEquals(0, conflicts.size());
            // Check if local changes were overwritten
            try {
                Assert.assertEquals(true, FileUtils.contentEquals(file, file2));
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            List<File> changesList = svnConnector.getStatus(secondWorkingCopy, false);
            Assert.assertEquals(0, changesList.size());
            // Check if there are conflicted files
            int localFilesCountSecond = FileUtils.listFilesAndDirs(secondWorkingCopy, TrueFileFilter.TRUE, 
                    FileFilterUtils.makeSVNAware(null)).size();
            Assert.assertEquals(localFilesCount, localFilesCountSecond);
            file.delete(); // Clean up
            getSvnConnector().storeModel(TEMP_DIR);
            FileUtils.deleteQuietly(secondWorkingCopy);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }
   
    /**
     * Test if conflicting files are detected.
     */
    @Test
    public void testGetConflicts() {
        File file = new File(TEMP_DIR + "/testGetConflicts.java");
        File secondWorkingCopy = TEMP_DIR2;
        File file2 = new File(secondWorkingCopy + "/testGetConflicts.java");
        PrintWriter writer;
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            try {
                file.createNewFile();
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
            getSvnConnector().storeModel(TEMP_DIR);
            // Create second workspace with second user
            SVNConnector svnConnector = new SVNConnector();
            svnConnector.setRepositoryURL(COMMIT_URL);
            svnConnector.authenticate("abc", "def");
            svnConnector.loadModel(secondWorkingCopy);
            Assert.assertEquals(true, file2.exists());
            // Modify file and commit
            try {
                writer = new PrintWriter(file, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            getSvnConnector().storeModel(TEMP_DIR);
            // Modify second file
            try {
                writer = new PrintWriter(file2, "UTF-8");
                writer.println("// The first line");
                writer.println("// The second line modified");
                writer.close();
            } catch (FileNotFoundException e) {
                Assert.fail(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                Assert.fail(e.getMessage());
            }
            // Add file to svn control
            List<File> changes = svnConnector.getConflictingFilesInWorkspace(secondWorkingCopy);
            Assert.assertEquals(1, changes.size());
            // Clean up
            file.delete();
            getSvnConnector().storeModel(TEMP_DIR);
            FileUtils.deleteQuietly(secondWorkingCopy);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    /**
     * Test if the changes count is computed correctly.
     */
    @Test
    public void testGetChangesCount() {
        // Load model and check that the changescount is 0;
        try {
            getSvnConnector().loadModel(TEMP_DIR);
            int remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, true);
            int localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(0, remoteChangesCount);
            Assert.assertEquals(0, localChangesCount);
            // Create local file and check the changescount
            File localFile = createLocalFile(TEMP_DIR, "testGetChangesCount");
            remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, true);
            localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(1, localChangesCount);
            Assert.assertEquals(0, remoteChangesCount);
            // Delete the local file
            FileUtils.deleteQuietly(localFile);
            Assert.assertFalse(localFile.exists());
            remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, true);
            localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(0, localChangesCount);
            Assert.assertEquals(0, remoteChangesCount);
            // Create second working copy, create new file and upload it to the repository
            SVNConnector svnConnector = new SVNConnector();
            svnConnector.setRepositoryURL(COMMIT_URL);
            svnConnector.authenticate("abc", "def");
            svnConnector.loadModel(TEMP_DIR2);
            localFile = createLocalFile(TEMP_DIR2, "testGetChangesCount");
            remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR2, true);
            localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR2, false);
            Assert.assertEquals(1, localChangesCount);
            Assert.assertEquals(0, remoteChangesCount);
            svnConnector.storeModel(TEMP_DIR2);
            // Now check the remote changes count for the TEMP_DIR working copy
            remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, true);
            localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(0, localChangesCount);
            Assert.assertEquals(2, remoteChangesCount);
            // // Create local file and check the changescount
            localFile = createLocalFile(TEMP_DIR, "testGetChangesCount");
            remoteChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, true);
            localChangesCount = getSvnConnector().getChangesCount(TEMP_DIR, false);
            Assert.assertEquals(1, localChangesCount);
            Assert.assertEquals(2, remoteChangesCount);
            FileUtils.deleteQuietly(TEMP_DIR2);
        } catch (ConnectorException e) {
            Assert.fail(e.getMessage());
        }
    }
    
}
