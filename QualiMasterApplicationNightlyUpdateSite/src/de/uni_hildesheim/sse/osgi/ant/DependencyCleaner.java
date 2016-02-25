/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.osgi.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class DependencyCleaner extends Task {

    private File folder;
    private String version;
    private final static Set<String> NAMES = new HashSet<String>();
    
    static {
        NAMES.add("ReasonerCore");
        NAMES.add("QualiMasterApplication");
        NAMES.add("RepositoryConnector");
    }
    
    public void setVersion(String version) {
        this.version = version;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }
    
    @Override
    public void execute() throws BuildException {
        String versionReplace = "bundle-version=\"" + version + "\"";
        File[] files = folder.listFiles();
        if (null != files) {
            for (File f : files) {
                if (f.getName().endsWith(".jar")) {
                    processJarFile(f, versionReplace);
                }
            }
        }
    }
    
    private void processJarFile(File file, String versionReplace) {
        byte[] buf = new byte[1024];
        try {
            System.out.print("processing " + file + " ");
            JarInputStream jin = new JarInputStream(new FileInputStream(file));
            File tmpFile = new File(file.getParent(), file.getName() + ".tmp");
            Manifest mf = jin.getManifest();
            if (null != mf) {
                Attributes attributes = mf.getMainAttributes();
                String tmp = attributes.getValue("Require-Bundle");
                if (null != tmp) {
                    StringBuilder result = new StringBuilder();
                    String[] deps = tmp.split(",");
                    for (String dep : deps) {
                        int pos = dep.indexOf(';');
                        if (pos > 0) {
                            String name = dep.substring(0, pos);
                            if (isRelevant(name)) {
                                dep = dep.replaceAll("bundle-version=\"\\d+(\\.\\d+)*\"", versionReplace);
                            }
                        }
                        if (result.length() > 0) {
                            result.append(",");
                        }
                        result.append(dep);
                    }
                    attributes.putValue("Require-Bundle", result.toString());
                }
                JarOutputStream jou = new JarOutputStream(new FileOutputStream(tmpFile), mf);
                JarEntry entry;
                do {
                    entry = jin.getNextJarEntry();
                    if (null != entry) {
                        JarEntry jouEntry = new JarEntry(entry);
                        jou.putNextEntry(jouEntry);
                        int read = 0;
                        while ((read = jin.read(buf)) != -1) {
                            jou.write(buf, 0, read);
                        }
                        jou.closeEntry();
                        jin.closeEntry();
                    }
                } while (null != entry);
                jin.close();
                jou.close();
                file.delete();
                tmpFile.renameTo(file);
                System.out.println("done");
            } else {
                System.out.println("no manifest");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    
    private boolean isRelevant(String name) {
        return name.startsWith("de.uni_hild") || name.startsWith("de.uni-hild") 
            || name.startsWith("eu.qualimaster") || NAMES.contains(name);
    }
    
    public static void main(String[] args) {
        DependencyCleaner inst = new DependencyCleaner();
        inst.version = "4.202020.2002";
        inst.folder = new File("tests");
        inst.execute();
    }
    
}
