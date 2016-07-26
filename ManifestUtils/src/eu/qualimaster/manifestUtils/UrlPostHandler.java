package eu.qualimaster.manifestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import org.apache.ivy.util.CopyProgressListener;
import org.apache.ivy.util.FileUtil;
import org.apache.ivy.util.url.BasicURLHandler;
import org.apache.ivy.util.url.IvyAuthenticator;

/**
 * Alternative Handler for use with POST instead of PUT.
 * Maybe unnecessary.
 * @author pastuschek
 *
 */

public class UrlPostHandler extends BasicURLHandler {

    private static final int BUFFER_SIZE = 64 * 1024;
    
    private String requestMethod = "POST";
    
    @Override
    public void upload(File source, URL dest, CopyProgressListener listener) throws IOException {
        if (!"http".equals(dest.getProtocol()) && !"https".equals(dest.getProtocol())) {
            throw new UnsupportedOperationException(
                    "URL repository only support HTTP PUT at the moment");
        }

        // Install the IvyAuthenticator
        IvyAuthenticator.install();

        HttpURLConnection conn = null;
        try {
            dest = normalizeToURL(dest);
            conn = (HttpURLConnection) dest.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod(requestMethod);
            conn.setRequestProperty("User-Agent", getUserAgent());
            conn.setRequestProperty("Content-type", "application/octet-stream");
            conn.setRequestProperty("Content-length", Long.toString(source.length()));
            conn.setInstanceFollowRedirects(true);

            try (InputStream in = new FileInputStream(source)) {
                OutputStream os = conn.getOutputStream();
                FileUtil.copy(in, os, listener);
            }
            validatePutStatusCode(dest, conn.getResponseCode(), conn.getResponseMessage());
        } finally {
            disconnect(conn);
        }
    }
    
    /**
     * Disconnects the POST-connection.
     * @param con The connection to close.
     */
    private void disconnect(URLConnection con) {
        if (con instanceof HttpURLConnection) {
            if (!"HEAD".equals(((HttpURLConnection) con).getRequestMethod())) {
                // We must read the response body before disconnecting!
                // Cfr. http://java.sun.com/j2se/1.5.0/docs/guide/net/http-keepalive.html
                // [quote]Do not abandon a connection by ignoring the response body. Doing
                // so may results in idle TCP connections.[/quote]
                readResponseBody((HttpURLConnection) con);
            }

            ((HttpURLConnection) con).disconnect();
        } else if (con != null) {
            try {
                con.getInputStream().close();
            } catch (IOException e) {
                // ignored
            }
        }
    }
    
    /**
     * Read and ignore the response body.
     * @param conn The connection.
     */
    private void readResponseBody(HttpURLConnection conn) {
        byte[] buffer = new byte[BUFFER_SIZE];

        InputStream inStream = null;
        try {
            inStream = conn.getInputStream();
            while (inStream.read(buffer) > 0) {
                int dummy = 0;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        InputStream errStream = conn.getErrorStream();
        if (errStream != null) {
            try {
                while (errStream.read(buffer) > 0) {
                    int dummy = 0;
                }
            } catch (IOException e) {
                // ignore
            } finally {
                try {
                    errStream.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
