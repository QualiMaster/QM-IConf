package eu.qualimaster.manifestUtils;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.ivy.util.Credentials;

/**
 * Handles the FTPSClient-Connection to ensure persistence.
 * @author pastuschek
 *
 */
public class FTPSConnector {

    /**Singleton Instance.*/
    private static final FTPSConnector INSTANCE = new FTPSConnector();
    private static final String PROTOCOL = "SSL";
    private static final int PORT = 21; //port 22 is SSH! port 21 should be correct!
    private static final int LOGIN_REPLY_CODE = 230;
    
    private FTPSClient client;
    
    /**
     * Singleton pattern.
     */
    private FTPSConnector() {};
    
    /**
     * Provides the singleton instance.
     * @return The singleton instance. Never null.
     */
    public static FTPSConnector getInstance() {
        return INSTANCE;
    }
    
    /**
     * Returns the FTPSClient of this connector.
     * @return The FTPSClient. Can be null.
     */
    public FTPSClient getClient() {
        return this.client;
    }
    
    /**
     * Initializes the FTPSClient-Connection.
     * @param dest The host URL.
     * @throws IOException if connection failed on the IO-level.
     */
    public void initialize(URL dest) throws IOException {
        
        Credentials cred = ManifestConnection.getCredentials();
        
        String username = null;
        String password = null;
        String host = null;
        
        if (null != cred) {
            username = cred.getUserName();
            password = cred.getPasswd();
            host = dest.getHost();
        } else {
            throw new ManifestRuntimeException("You are not logged in. Please log in to use this feature.");
        }
        
        System.out.println("Initializing FTPS-connection to " + host);      
        client = new FTPSClient(PROTOCOL, false);
        
//        if (client.isConnected()) {
//            client.disconnect();
//        }
        client.connect(host, PORT);

        // Set protection buffer size and data channel protection to private
        client.execPBSZ(0);
        client.execPROT("P");
                
        try {
            client.login(username, password);
        } catch (IOException e) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
        if (!FTPReply.isPositiveCompletion(client.getReplyCode()) || !client.isConnected()) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
        
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();
        
    }
    
    /**
     * Disconnects the FTPSClient.
     */
    public void disconnect() {
        if (null != this.client) {
            try {
                client.logout();
                client.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
