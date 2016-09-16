package eu.qualimaster.manifestUtils;

/**
 * RuntimeException during ManifestUtils processes.
 * @author pastuschek
 *
 */
public class ManifestRuntimeException extends RuntimeException {

    /**
     * Default generated Serial.
     */
    private static final long serialVersionUID = -4411407447138564570L;

    /**
     * Default Constructor with a single String as message.
     * @param msg The message as String.
     */
    public ManifestRuntimeException(String msg) {
        super(msg);
    }
}
