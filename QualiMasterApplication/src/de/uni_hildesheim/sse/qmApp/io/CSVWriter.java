/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_hildesheim.sse.qmApp.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Writes elements to an <a href="https://en.wikipedia.org/wiki/Comma-separated_values">CSV file</a>.
 * @author El-Sharkawy
 *
 */
public class CSVWriter {
    
    private BufferedWriter out;
    private String separator;
    private String linefeed;
    
    /**
     * Creates a new file and will write all output to the given file via the {@link #writeLine(String...)} method.
     * After all content was written to that file, this writer should be closed via the {@link #close()} method.
     * @param target The target file where the contents shall be saved to.
     * @param overwrite <tt>true</tt> existing files will be overwritten, <tt>false</tt> existing files will throw
     *  an {@link IOException}.
     * @param separator The separator which shall be used.
     * @param linefeed The line feed to be used.
     * @throws IOException If an I/O error occurs
     */
    public CSVWriter(File target, boolean overwrite, String separator, String linefeed) throws IOException {
        if (!target.exists()) {
            if (!overwrite) {
                throw new IOException("File \"" + target.getAbsolutePath() + "\" exist.");
            } else  {
                if (!target.createNewFile()) {
                    throw new IOException("Destination file \"" + target.getAbsolutePath()
                        + "\" could not be created.");
                }
            }
        }
        
        this.separator = separator;
        this.linefeed = linefeed;
        out = new BufferedWriter(new FileWriter(target));
        
        // Write pre-amble
        out.write("sep=");
        out.write(separator);
        out.write(linefeed);
    }
    
    /**
     * Appends a line to the currently written file.
     * @param items The line to be written, all items will be separated by the specified separator.
     * @throws IOException If an I/O error occurs
     */
    public void writeLine(String... items) throws IOException {
        if (null != items) {
            out.write(items[0]);
            for (int i = 1; i < items.length; i++) {
                out.write(separator);
                out.write(items[i]);
            }
            out.write(linefeed);
        }
    }
    
    /**
     * Closes this writer silently.
     */
    public void close() {
        if (null != out) {
            try {
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
