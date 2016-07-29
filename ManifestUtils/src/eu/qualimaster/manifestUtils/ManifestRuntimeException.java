package eu.qualimaster.manifestUtils;

/**
 * RuntimeException during ManifestUtils processes.
 * @author pastuschek
 *
 */
public class ManifestRuntimeException extends RuntimeException {

    /**
     * Default Constructor with a single String as message.
     * @param msg The message as String.
     */
    public ManifestRuntimeException(String msg) {
        super(msg);
    }
}
