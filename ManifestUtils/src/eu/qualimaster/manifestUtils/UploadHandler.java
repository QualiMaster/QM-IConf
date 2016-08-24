package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.url.BasicURLHandler;

/**
 * Gathers the upload data (i.e. The files and their destination).
 * @author pastuschek
 *
 */
public class UploadHandler extends BasicURLHandler {    
    
    @Override
    public void upload(File source, URL dest, CopyProgressListener listener) throws IOException {

        //TODO: completely remove ivy from publishing process? Or at least only use for redirection purposes.
        
    }
    
}
