package de.uni_hildesheim.sse.repositoryConnector.svnConnector;

import java.net.UnknownHostException;

import org.tmatesoft.svn.core.SVNAuthenticationException;
import org.tmatesoft.svn.core.SVNException;

/**
 * Exception Wrapper to catch {@link SVNException}.
 * 
 * @author Sass
 *
 */
public class ConnectorException extends Exception {
    
    /**
     * Enum for the reasons.
     * @author Sass
     *
     */
    private enum Reason {
        
        NO_CONNECTION("The login failed. Please make sure you have a working internet connection."), 
        
        WRONG_CREDENTIALS("The login failed. Please check your credentials."), 
        
        UNKNOWN("An unknown error occured.");
        
        private String message;
        
        /**
         * Creates a reason message.
         * @param message the message
         */
        private Reason(String message) {
            this.message = message;
        }
        
        /**
         * Returns the message of the reason.
         * 
         * @return message as string
         */
        private String getMessage() {
            return message;
        }
    }
    
    /**
     * Serial ID.
     */
    private static final long serialVersionUID = 6587560728085914866L;

    private Reason reason;
    
    /**
     * Default constructor.
     * @param exception exception
     */
    ConnectorException(SVNException exception) {
        super(exception);
        reason = Reason.UNKNOWN;
        System.out.println(exception);
        if (null != exception && exception instanceof SVNAuthenticationException) {
            reason = Reason.WRONG_CREDENTIALS;
        } else if (exception.getCause() instanceof UnknownHostException) {
            reason = Reason.NO_CONNECTION;
        }
    }

    /**
     * Getter for the reason.
     * @return the Reason
     */
    public Reason getReason() {
        return reason;
    }
    
    @Override
    public String getMessage() {
        return (reason == Reason.UNKNOWN) ? super.getMessage() : reason.getMessage();
    }
}
