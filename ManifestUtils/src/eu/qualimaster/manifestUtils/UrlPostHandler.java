package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
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

    private static final int BUFFER_SIZE = 64 * 1024;
    
    private static final int TIMEOUT = 10000; //10 seconds?
    
    private static final int PORT = 21; //port 22 is SSH! port 21 should be correct!
    
    private String requestMethod = "POST";
    
    @Override
    public void upload(File source, URL dest, CopyProgressListener listener) throws IOException {
        
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
        
        FTPSClient client = new FTPSClient("SSL", false);
        
        if (client.isConnected()) {
            client.disconnect();
        }

        client.connect(host, PORT);

        
        try {
            client.login(username, password);
        } catch (IOException e) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
        if (200 != client.getReplyCode()) {
            throw new ManifestRuntimeException("The login failed. Please check your credentials.");
        }
        
        System.out.println(client.getReplyString());
                
        client.setFileType(FTPClient.BINARY_FILE_TYPE);
        client.enterLocalPassiveMode();

        try (InputStream input = new FileInputStream(source)) {
//            client.storeFile(dest.getPath(), input);
            System.out.println("Source: " + source.getAbsolutePath());
            System.out.println("Destination: " + dest);
        } finally {
            client.disconnect();
        }        
        
    }
    
//    /**
//     * Read and ignore the response body.
//     * @param conn The connection.
//     */
//    private void readResponseBody(HttpURLConnection conn) {
//        byte[] buffer = new byte[BUFFER_SIZE];
//
//        InputStream inStream = null;
//        try {
//            inStream = conn.getInputStream();
//            while (inStream.read(buffer) > 0) {
//                int dummy = 0;
//            }
//        } catch (IOException e) {
//            // ignore
//        } finally {
//            if (inStream != null) {
//                try {
//                    inStream.close();
//                } catch (IOException e) {
//                    // ignore
//                }
//            }
//        }
//
//        InputStream errStream = conn.getErrorStream();
//        if (errStream != null) {
//            try {
//                while (errStream.read(buffer) > 0) {
//                    int dummy = 0;
//                }
//            } catch (IOException e) {
//                // ignore
//            } finally {
//                try {
//                    errStream.close();
//                } catch (IOException e) {
//                    // ignore
//                }
//            }
//        }
//    }
}
