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
package de.uni_hildesheim.sse.qmApp.model;

import de.uni_hildesheim.sse.repositoryConnector.maven.MavenFetcher;
import eu.qualimaster.manifestUtils.ManifestConnection;

/**
 * Utility methods for the connectors.
 * 
 * @author Holger Eichelberger
 */
public class ConnectorUtils {

    /**
     * Prevent external instantiation.
     */
    private ConnectorUtils() {
    }
    
    /**
     * Configures the connectors.
     */
    public static void configure() {
        String mavenURL = ModelAccess.getRepositoryUrl();
        if (null != mavenURL && mavenURL.length() > 0) {
            MavenFetcher.setRepositoryUrl(mavenURL);
            ManifestConnection.clearRepositories();
            ManifestConnection.addRepository(mavenURL);
            //ManifestConnection.addRepository("http://nexus.sse.uni-hildesheim.de/releases/Qualimaster/");
            ManifestConnection.addRepository("http://clojars.org/repo");
            ManifestConnection.addDefaultRepositories();
        }
    }

}
