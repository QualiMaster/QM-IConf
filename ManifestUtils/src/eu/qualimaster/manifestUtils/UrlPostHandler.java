package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.Credentials;
import org.apache.ivy.util.url.BasicURLHandler;

/**
 * Alternative Handler for use with POST instead of PUT.
 * Alternative Handler for use of SFTP for uploading purposes.
 * Maybe unnecessary.
 * @author pastuschek
 *
 */

public class UrlPostHandler extends BasicURLHandler {

//    private static final int BUFFER_SIZE = 64 * 1024;
    
//    private static final int TIMEOUT = 10000; //10 seconds?
    
    private static final int PORT = 21; //port 22 is SSH! port 21 should be correct!
    private static final int LOGIN_REPLY_CODE = 230;
    private static final String PROTOCOL = "SSL";
    
//    private String requestMethod = "POST";
    
    @Override
    public void upload(File source, URL dest, CopyProgressListener listener) throws IOException {
        
        Credentials cred = ManifestConnection.getCredentials();
      
        String username = null;
        String password = null;
        String host = null;
        int replyCode = 0;
        String replyString = "";
        
        if (null != cred) {
            username = cred.getUserName();
            password = cred.getPasswd();
            host = dest.getHost();
        } else {
            throw new ManifestRuntimeException("You are not logged in. Please log in to use this feature.");
        }
        
        System.out.println("Initializing FTPS-connection to " + host);      
        FTPSClient client = new FTPSClient(PROTOCOL, false);
        
        if (client.isConnected()) {
            client.disconnect();
        }
        client.connect(host, PORT);

        // Set protection buffer size and data channel protection to private
        client.execPBSZ(0);
        client.execPROT("P");
                
        try {
            client.login(username, password);
        } catch (IOException e) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
        if (LOGIN_REPLY_CODE != client.getReplyCode()) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
                
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();
        String finalDestination = dest.getPath();
        System.out.println("fDest: " + finalDestination);
        String workingDir = "";
        
        String[] split = finalDestination.split("/");
        for (int i = 3; i < split.length; i++) {
            if (!split[i].contains(".")) {
                workingDir += "/" + split[i];
            }
        }
        traverseToDir(client, workingDir);
        
        String fileName = split[split.length - 1];       
        System.out.println("Current Dir: " + client.printWorkingDirectory());      
        
        try (InputStream input = new FileInputStream(source)) {
            client.storeFile(fileName, input);
            replyCode = client.getReplyCode();
            replyString = client.getReplyString();
        } finally {
            client.disconnect();
        }        
        
        //2XX are successful codes -> if a different code is returned, open a dialog for the user.
        if (replyCode < 200 || replyCode > 299) {
            throw new ManifestRuntimeException(replyString);
        }       
        
    }
    
    /**
     * Traverses the client to the target dir and creates all directories that do not already exist.
     * @param client The FTPSClient in use.
     * @param dir The target dir.
     */
    private void traverseToDir(FTPSClient client, String dir) {
        
        if (null != client && null != dir) {
            
            String[] split = dir.split("/");
            
                
            for (int i = 0; i < split.length; i++) {
                
                if (null != split[i] && !split[i].isEmpty()) {
                    try {
                        client.changeWorkingDirectory(split[i]);
                    } catch (IOException e) {
                        //ignore since there is a second chance
                    }
                    System.out.println("Traversing to: " + split[i]);
                    if (client.getReplyCode() != 250) {
                        try {
                            client.makeDirectory(split[i]);
                            client.changeWorkingDirectory(split[i]);
                            System.out.println("Creating dir: " + split[i]);
                        } catch (IOException e) {
                            //kill the process or it will become inconsistent
                            e.printStackTrace();
                            throw new ManifestRuntimeException(
                                    "Please make sure you have permission to write to this dir.");
                        }
                        
                    }

                }
                
            }
            
        }
        
    }
    
}
