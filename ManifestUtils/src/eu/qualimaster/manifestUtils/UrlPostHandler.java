package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamException;
import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.url.BasicURLHandler;

/**
 * Alternative Handler for use with POST instead of PUT.
 * Alternative Handler for use of SFTP for uploading purposes.
 * Maybe unnecessary.
 * @author pastuschek
 *
 */

public class UrlPostHandler extends BasicURLHandler {

    private static final String IVY_XML_REGEX = "^ivy.*\\.xml$";
//    private static final int BUFFER_SIZE = 64 * 1024;
    
//    private static final int TIMEOUT = 10000; //10 seconds?
    
//    private static final int PORT = 21; //port 22 is SSH! port 21 should be correct!
//    private static final int LOGIN_REPLY_CODE = 230;
//    private static final String PROTOCOL = "SSL";
    
//    private String requestMethod = "POST";
    
    @Override
    public void upload(File source, URL dest, CopyProgressListener listener) throws IOException {

        if (!source.getName().matches(IVY_XML_REGEX)) {
            FTPSClient client = FTPSConnector.getInstance().getClient();
            upload(source, dest, client);
        }
        
    }

    /**
     * Uploads a file to the destination via FTPS.
     * @param source The source to upload.
     * @param dest The destination for the file.
     * @param client The FTPSClient in use.
     * @throws IOException If an IO-Error occurred.
     * @throws FileNotFoundException If the file was not found.
     * @throws CopyStreamException If the copy process failed.
     */
    private void upload(File source, URL dest, FTPSClient client)
            throws IOException, FileNotFoundException, CopyStreamException {
        int replyCode = 0;
        String replyString = "";
        boolean success = false;

        if (null != client) {
            String finalDestination = dest.getPath();          
            System.out.println("fDest: " + finalDestination);
            String workingDir = "";
            
            String[] split = finalDestination.split("/");
            for (int i = 3; i < split.length; i++) {
                if (!((i == split.length - 1) && split[i].contains("."))) {
                    workingDir += "/" + split[i];
                }
            }
            client.changeWorkingDirectory("/"); //reset the path.
            traverseToDir(client, workingDir);
            
            String fileName = split[split.length - 1];       
            System.out.println("Current Dir: " + client.printWorkingDirectory());  
            System.out.println("File name: " + fileName);
            
            try (InputStream input = new FileInputStream(source)) {
                System.out.println("Socket is available: " + client.isAvailable());
                client.setFileType(FTPClient.BINARY_FILE_TYPE);
                client.setConnectTimeout(5000);
                System.out.println("Timeout: " + client.getConnectTimeout());
                success = client.storeFile(fileName, input);
                client.sendSiteCommand("chmod g+w " + fileName); //TODO
                System.out.println("Success == " + success);
                replyCode = client.getReplyCode();
                replyString = client.getReplyString();
            } catch (CopyStreamException exc) {
                System.out.println(exc.getTotalBytesTransferred());
                throw exc;
            }
            
            //2XX are successful codes -> if a different code is returned, open a dialog for the user.
            //if (replyCode < 200 || replyCode > 299) {
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new ManifestRuntimeException(replyString);
            } else {
                System.out.println(replyString);
            }
        } else {
            System.out.println("FATAL ERROR: CLIENT == NULL!");
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
                        client.sendSiteCommand("chmod g+w " + split[i]);
                        client.changeWorkingDirectory(split[i]);
//                        System.out.println("Traversing to: " + split[i]);
                    } catch (IOException e) {
                        //ignore since there is a second chance
                    }
                    if (client.getReplyCode() != 250) {
                        try {
                            client.makeDirectory(split[i]);
//                            System.out.println("Creating dir: " + split[i]);
                            client.sendSiteCommand("chmod g+w " + split[i]);
                            client.changeWorkingDirectory(split[i]);
//                            System.out.println("Traversing to: " + split[i]);
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
