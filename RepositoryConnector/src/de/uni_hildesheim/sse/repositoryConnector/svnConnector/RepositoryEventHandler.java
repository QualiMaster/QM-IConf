package de.uni_hildesheim.sse.repositoryConnector.svnConnector;

/**
 * Interface for the SVN EventHandler.
 * 
 * @author Sass
 *
 */
public interface RepositoryEventHandler {
    
    /**
     * Method indicating the progress of an update.
     * 
     * @param progress Progress value
     */
    public void progress(double progress);
    
    /**
     * Indicates if the update is completed.
     */
    public void completed();

}
