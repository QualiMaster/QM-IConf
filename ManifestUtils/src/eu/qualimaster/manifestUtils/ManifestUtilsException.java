package eu.qualimaster.manifestUtils;

/**
 * A special Exception for failure cases of the ManifestUtils.
 * @author Pastuschek
 *
 */
public class ManifestUtilsException extends Exception {

    /**
     * Generated serial.
     */
    private static final long serialVersionUID = 3889887500063229198L;
    
    private String shortMessage;
    private String detailedMessage;
    
    /**
     * Constructor if a short description and a detailed message are available.
     * @param shortMessage The short description (can be used as a title).
     * @param detailedMessage The detailed cause for the exception.
     */
    public ManifestUtilsException(String shortMessage, String detailedMessage) {
        this.shortMessage = shortMessage;
        this.detailedMessage = detailedMessage;
    }

    /**
     * Constructor for a message.
     * @param message The message.
     */
    public ManifestUtilsException(String message) {
        super(message);
    }

    /**
     * Constructor for chaining.
     * @param cause The previous Throwable. 
     */
    public ManifestUtilsException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor for chaining with message.
     * @param message The message.
     * @param cause The previous Throwable.
     */
    public ManifestUtilsException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Returns a short message, which is suitable for a title.
     * @return The short message if available or {@link #getMessage()}.
     */
    public String getShortMessage() {
        return null != shortMessage ? shortMessage : getMessage();
    }
    
    /**
     * Returns a more detailed, additional message than {@link #getShortMessage()}.
     * @return The detailed message if available or the message from {@link #getCause()}.
     */
    public String getDetailedMessage() {
        String msg = null != detailedMessage ? detailedMessage : null;
        if (msg == null && null != getCause()) {
            msg = getCause().getMessage();
        }
        return msg;
    }
    
}
