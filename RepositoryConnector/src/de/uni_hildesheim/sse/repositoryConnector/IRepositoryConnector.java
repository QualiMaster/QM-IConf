package de.uni_hildesheim.sse.repositoryConnector;

import java.io.File;
import java.util.List;
import java.util.Set;

import de.uni_hildesheim.sse.repositoryConnector.svnConnector.ConnectorException;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.RepositoryEventHandler;

/**
 * This class contains all repository operations.
 * 
 * @author Sass
 * 
 */
public interface IRepositoryConnector {

    /**
     * Authenticates a user for a repository.
     * 
     * @param username
     *            The user name for the repository
     * @param password
     *            The password for the repository
     * 
     * @return true if successfully authenticated
     * @throws ConnectorException 
     */
    boolean authenticate(String username, String password) throws ConnectorException;

    /**
     * Loads the model from the repository and stores it to a given destination directory.
     * 
     * @param destinationFile
     *            The destination directory where the model should be stored.
     * 
     * @return The file where the model is stored.
     * @throws ConnectorException exception
     */
    public File loadModel(File destinationFile) throws ConnectorException;

//    /**
//     * Stores the model in the repository.
//     * 
//     * @param destinationFile
//     *            The file where the model is located on the local system.
//     * @param override
//     *            Should the remote file be overwritten
//     */
//    public void storeModel(File destinationFile, boolean override);
    
    /**
     * Stores the model in the repository. In case of conflict all local changes will be overwritten with remote file.
     * Use {@link #storeModel(File, boolean)} if you want to overwrite remote file.
     * 
     * @param destinationFile
     *            The file where the model is located on the local system.
     * @return true or false whether there are conflicts
     * @throws ConnectorException exception
     */
    public boolean storeModel(File destinationFile) throws ConnectorException;

    /**
     * Gets all roles.
     * 
     * @return {@link Set} with all roles.
     */
    public Set<?> getRoles();

    /**
     * Sets the repository to the given URL.
     * 
     * @param repoURL
     *            The URL of the repository
     */
    void setRepositoryURL(String repoURL);

    /**
     * Getter for the {@link UserContext}.
     * 
     * @return The {@link UserContext} of the application
     */
    public UserContext getUserContext();

    /**
     * Gets the total number of files in the repository.
     * 
     * @return number of files in the repository
     */
    int getRepositoryEntryCount();

    /**
     * Sets the EventHandler for the RepositoryConnector.
     * 
     * @param handler
     *            The EventHandler that should be set
     */
    void setUpdateEventHandler(RepositoryEventHandler handler);

    /**
     * Sets the EventHandler for the RepositoryConnector.
     * 
     * @param handler
     *            The EventHandler that should be set
     */
    void setCommitEventHandler(RepositoryEventHandler handler);

    /**
     * Gets the number of changes in the working copy.
     * 
     * @param destinationFile
     *            Path to the workspace
     * @param isRemote
     *            true to check up the status of the item in the repository, that will tell if the local item is
     *            out-of-date (like '-u' option in the SVN client's 'svn status' command), otherwise false
     * 
     * @return number of changes
     * @throws ConnectorException exception
     */
    int getChangesCount(File destinationFile, boolean isRemote) throws ConnectorException;

    /**
     * Updates workspace to HEAD.
     * 
     * @param destinationFile
     *            Path to the workspace
     * @return List with conflicting files
     * @throws ConnectorException exception
     */
    List<File> updateModel(File destinationFile) throws ConnectorException;
    
    /**
     * Resolves conflicts with given choice.
     * 
     * @param path Path to the workspace
     * @param keepMine if set to true the local changes will be kept 
     * @throws ConnectorException exception
     */
    void resolveConflicts(File path, boolean keepMine) throws ConnectorException;
    
    /**
     * Gets all conflicting files in the working copy.
     * 
     * @param wcPath    Path to working copy
     * @return List with all conflicting files
     * @throws ConnectorException exception
     */
    public List<File> getConflictingFilesInWorkspace(File wcPath) throws ConnectorException;
    
    /**
     * Reverts all changes in the workspace.
     * 
     * @param wcPath Path to the working copy
     * @throws ConnectorException exception
     */
    void revert(File wcPath) throws ConnectorException;
}
