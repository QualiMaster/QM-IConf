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
import org.junit.Test;

import de.uni_hildesheim.sse.repositoryConnector.AbstractRepositoryConnectorTest;
import de.uni_hildesheim.sse.repositoryConnector.Bundle;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * Test Case for the {@link SVNConnector}.
 * 
 * @author Sass
 * 
 */
public class SVNConnectorTest extends AbstractRepositoryConnectorTest {
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(SVNConnectorTest.class, Bundle.ID);

    /**
     * Test if a repository can be checked out.
     */
    @Test
    public void testloadModel() {
        getSvnConnector().loadModel(TEMP_DIR);
        Assert.assertEquals(true, TEMP_DIR.isDirectory());
        int localFiles = FileUtils.listFilesAndDirs(TEMP_DIR, TrueFileFilter.TRUE, 
                FileFilterUtils.makeSVNAware(null)).size();
        int repositoryFiles = getSvnConnector().getRepositoryEntryCount();
        Assert.assertEquals(repositoryFiles, localFiles - 1);
    }

    /**
     * Test if files can be committed to the repository.
     */
    @Test
    public void testStoreModel() {
        getSvnConnector().loadModel(TEMP_DIR);
        int numberOfFiles = TEMP_DIR.list().length;
        File file = new File(TEMP_DIR + "/testStoreModel.txt");
        try {
            file.createNewFile();
        } catch (IOException e) {
            getLogger().exception(e);
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
        svnConnector.updateModel(secondWorkingCopy);
        svnConnector.resolveConflicts(secondWorkingCopy, false);
        Assert.assertEquals(0, svnConnector.getChangesCount(secondWorkingCopy, false));
        // Check if local changes were overwritten
        try {
            Assert.assertEquals(true, FileUtils.contentEquals(file, file2));
        } catch (IOException e) {
            getLogger().exception(e);
        }
        // Clean up
        file.delete();
        getSvnConnector().storeModel(TEMP_DIR);
        FileUtils.deleteQuietly(secondWorkingCopy);
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
        getSvnConnector().loadModel(TEMP_DIR);
        try {
            file.createNewFile();
        } catch (IOException e) {
            getLogger().exception(e);
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
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
        }
        getSvnConnector().storeModel(TEMP_DIR);
        // Modify second file
        try {
            writer = new PrintWriter(file2, "UTF-8");
            writer.println("// The first line");
            writer.println("// The second line modified");
            writer.close();
        } catch (FileNotFoundException e) {
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
        }
        boolean conflict = svnConnector.storeModel(secondWorkingCopy);
        Assert.assertTrue(conflict);
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
        svnConnector.updateModel(secondWorkingCopy);
        svnConnector.resolveConflicts(secondWorkingCopy, true);
        svnConnector.storeModel(secondWorkingCopy);
        Assert.assertEquals(0, svnConnector.getChangesCount(secondWorkingCopy, false));
        // Check if local changes were overwritten
        try {
            Assert.assertEquals(false, FileUtils.contentEquals(file, file2));
        } catch (IOException e) {
            getLogger().exception(e);
        }
        // Clean up
        file2.delete();
        getSvnConnector().storeModel(secondWorkingCopy);
        FileUtils.deleteQuietly(secondWorkingCopy);
    }
    
    /**
     * Test if local changes will be overridden.
     */
    @Test
    public void testOverrideLocalChanges() {
        PrintWriter writer;
        String fileName = "testOverrideLocalChanges";
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
            getLogger().exception(e);
        }
        getSvnConnector().revert(file);
        try {
            Assert.assertEquals(true, FileUtils.contentEquals(file, copiedFile));
        } catch (IOException e) {
            getLogger().exception(e);
        }
        // Clean up
        file.delete();
        copiedFile.delete();
        getSvnConnector().storeModel(TEMP_DIR);
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
            getLogger().exception(e);
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
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
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
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
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
            getLogger().exception(e);
        }
        List<File> changesList = svnConnector.getStatus(secondWorkingCopy, false);
        Assert.assertEquals(0, changesList.size());
        
        // Check if there are conflicted files
        int localFilesCountSecond = FileUtils.listFilesAndDirs(secondWorkingCopy, TrueFileFilter.TRUE, 
            FileFilterUtils.makeSVNAware(null)).size();
        Assert.assertEquals(localFilesCount, localFilesCountSecond);
        
        // Clean up
        file.delete();
        getSvnConnector().storeModel(TEMP_DIR);
        FileUtils.deleteQuietly(secondWorkingCopy);
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
        getSvnConnector().loadModel(TEMP_DIR);
        try {
            file.createNewFile();
        } catch (IOException e) {
            getLogger().exception(e);
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
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
        }
        getSvnConnector().storeModel(TEMP_DIR);
        // Modify second file
        try {
            writer = new PrintWriter(file2, "UTF-8");
            writer.println("// The first line");
            writer.println("// The second line modified");
            writer.close();
        } catch (FileNotFoundException e) {
            logger.exception(e);
        } catch (UnsupportedEncodingException e) {
            logger.exception(e);
        }
        // Add file to svn control
        List<File> changes = svnConnector.getConflictingFilesInWorkspace(secondWorkingCopy);
        Assert.assertEquals(1, changes.size());
        // Clean up
        file.delete();
        getSvnConnector().storeModel(TEMP_DIR);
        FileUtils.deleteQuietly(secondWorkingCopy);
    }
    
}
