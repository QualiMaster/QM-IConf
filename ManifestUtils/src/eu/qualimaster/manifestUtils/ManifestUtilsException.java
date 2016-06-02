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

    /**
     * Empty Constructor.
     */
    public ManifestUtilsException() {

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
    
}
