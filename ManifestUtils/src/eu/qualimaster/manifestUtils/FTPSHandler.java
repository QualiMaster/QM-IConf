package eu.qualimaster.manifestUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamException;
import org.apache.ivy.util.url.BasicURLHandler;

import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import net.ssehub.easy.basics.progress.ProgressObserver;

/**
 * Alternative Handler for use with POST instead of PUT.
 * Alternative Handler for use of SFTP for uploading purposes.
 * Maybe unnecessary.
 * @author pastuschek
 *
 */

public class FTPSHandler extends BasicURLHandler {

    private static final String IVY_XML_REGEX = "^ivy.*\\.xml$";
    private static final String EDIT_ACCESS_COMMAND = "chmod g+w ";
    private static final String SHA1 = "SHA1";
    private static final String MD5 = "MD5";
    
    private EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(FTPSHandler.class, 
            "eu.qualimaster.ManifestUtils");
    
    /**
     * Downloads a file from the FTPS URL and stores it to destination.
     * @param source The URL source.
     * @param destination The local file destination.
     * @return The downloaded file or null if the download failed.
     * @throws IOException If and IO handling failed.
     */
    public File download(URL source, File destination) throws IOException {
        return null;
    }
    
    /**
     * Uploads a file to a destination. This method is to circumvent ivy. Uploads Checksums.
     * @param source The source file.
     * @param dest The destination URL.
     * @param monitor The ProgressObserver.
     * @throws IOException If the upload fails.
     */
    public void uploadWithoutIvy(File source, URL dest, ProgressObserver monitor) throws IOException {
        uploadWithoutIvy(source, dest, monitor, true);
    }
    
   /**
    * Uploads a file to a destination. This method is to circumvent ivy.
    * @param source The source file.
    * @param dest The destination URL.
    * @param monitor The ProgressObserver.
    * @param checksums Set to true to upload md5 and sh1 checksums.
    * @throws IOException If the upload fails.
    */
    public void uploadWithoutIvy(File source, URL dest, ProgressObserver monitor, boolean checksums) 
            throws IOException {

        if (!source.getName().matches(IVY_XML_REGEX)) {
            FTPSClient client = FTPSConnector.getInstance().getClient();
            upload(source, dest, client, monitor, checksums);
        }
        
    }
    
    /**
     * Uploads a file to the destination via FTPS.
     * @param source The source to upload.
     * @param dest The destination for the file.
     * @param client The FTPSClient in use.
     * @param monitor The ProgressObserver.
     * @param checksums Set to true to upload md5 and sh1 checksums.
     * @throws IOException If an IO-Error occurred.
     * @throws FileNotFoundException If the file was not found.
     * @throws CopyStreamException If the copy process failed.
     */
    private void upload(File source, URL dest, FTPSClient client, ProgressObserver monitor, boolean checksums)
            throws IOException, FileNotFoundException, CopyStreamException {
        
        int replyCode = 0;
        String replyString = "";
        UploadListener listener = null;
        
        if (null != client) {
            //add progress monitor and listener to track the progress.
            if (null != monitor) {
                listener = new UploadListener(monitor, source);
                client.setCopyStreamListener(listener);
            }
            
            //get the actual destination for the file and traverse to it.
            String finalDestination = dest.getPath();          
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
            
            //start the upload for the file and send the necessary commands.
            try (InputStream input = new FileInputStream(source)) {
                client.setFileType(FTPClient.BINARY_FILE_TYPE);
                client.setConnectTimeout(5000);
                client.storeFile(fileName, input);
                client.sendSiteCommand(EDIT_ACCESS_COMMAND + fileName); //TODO
                replyCode = client.getReplyCode();
                replyString = client.getReplyString();
                
                if (null != listener) {
                    listener.endTask();
                }
                //also upload checksums if necessary.
                if (checksums) {
                    try {
                        generateAndUploadHash(source, dest, client, monitor, SHA1);
                        generateAndUploadHash(source, dest, client, monitor, MD5);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }
            } catch (CopyStreamException exc) {
                throw exc;
            }
            
            //2XX are successful codes -> if a different code is returned, open a dialog for the user.
            //if (replyCode < 200 || replyCode > 299) {
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new ManifestRuntimeException(replyString);
            } 

        } else {
            logger.error("FTPSClient has not been initialized correctly.");
        }
    }

    /**
     * Generates and uploads a hash value for a file.
     * @param source The original file.
     * @param dest The destination.
     * @param client The FTPSClient.
     * @param monitor The progressObserver to track progress.
     * @param hashType The type of hash.
     * @throws MalformedURLException If a URL is invalid.
     * @throws IOException If a file could not be read.
     * @throws FileNotFoundException If a file could not be found.
     * @throws CopyStreamException If the file could not be copied.
     * @throws NoSuchAlgorithmException If no algorithm for the hash exists.
     */
    private void generateAndUploadHash(File source, URL dest, FTPSClient client, ProgressObserver monitor,
        String hashType) throws MalformedURLException, IOException, FileNotFoundException, CopyStreamException,
        NoSuchAlgorithmException {
        
        //generate the name for the hash file.
        String name = null;
        try {
            String stringUrl = dest.toURI().toString();
            name = stringUrl.substring(stringUrl.lastIndexOf('/') + 1, stringUrl.length());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        
        if (null == name) {
            name = source.getName();
        }
          
        //upload the hash file.
        URL url = new URL(dest, name + "." + hashType.toLowerCase());
        upload(getHash(source, hashType), url, client, monitor, false);
        
    }
    
    /**
     * Calculates the SHA1 hash value of a file, writes it to a file and returns said file.
     * @param file The original file to analyze.
     * @param hashType The type of hash. Either SHA1 or MD5.
     * @return A file with the SHA1 hash value in it.
     * @throws NoSuchAlgorithmException If the (hash-)algorithm does not exist.
     * @throws IOException If one of the two files could not be accessed.
     */
    private File getHash(final File file, String hashType) throws NoSuchAlgorithmException, IOException {
        
        //get the hash type and add a suffix to the file name.
        MessageDigest messageDigest = MessageDigest.getInstance(hashType);
        String suffix;
        if (SHA1.equals(hashType)) {
            suffix = ".sha1";
        } else {
            suffix = ".md5";
        }
        File result = new File(file.getAbsolutePath() + suffix);
        if (!result.exists()) {
            result.createNewFile();
        }
        
        //read the original file into the messageDigest buffer.
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            final byte[] buffer = new byte[1024];
            for (int read = 0; (read != -1);) {
                messageDigest.update(buffer, 0, read);
                read = is.read(buffer); 
            }
        }
        String hash = "";
        
        // Convert the byte to hex format
        try (Formatter formatter = new Formatter()) {
            for (final byte b : messageDigest.digest()) {
                formatter.format("%02x", b);
            }
            hash = formatter.toString();
        }
        
        //write the file.
        PrintWriter writer = new PrintWriter(result, "UTF-8");
        writer.print(hash);
        writer.close();
        
        return result;
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
                
                //attempt to traverse to the next dir. May fail if the dir does not exist yet.
                if (null != split[i] && !split[i].isEmpty()) {
                    try {
                        client.sendSiteCommand(EDIT_ACCESS_COMMAND + split[i]);
                        client.changeWorkingDirectory(split[i]);
                    } catch (IOException e) {
                        //ignore since there is a second chance
                    }
                    //if unsucessful try to create the dir and then traverse to it.
                    if (client.getReplyCode() != 250) {
                        try {
                            client.makeDirectory(split[i]);
                            client.sendSiteCommand(EDIT_ACCESS_COMMAND + split[i]);
                            client.changeWorkingDirectory(split[i]);
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
