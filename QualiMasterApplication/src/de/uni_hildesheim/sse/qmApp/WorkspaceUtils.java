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
package de.uni_hildesheim.sse.qmApp;

import java.io.File;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

/**
 * Some workspace utilities.
 * 
 * @author Holger Eichelberger
 */
public class WorkspaceUtils {

    /**
     * Prevent instantiation.
     */
    private WorkspaceUtils() {
    }
    
    /**
     * Enables / disables auto building of the workspace.
     * 
     * @param enable enable or disable
     * @return the state before
     * @throws CoreException in case that auto build cannot be enabled/disabled
     */
    public static boolean enableAutoBuild(boolean enable) throws CoreException {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding != enable) {
            desc.setAutoBuilding(enable);
            workspace.setDescription(desc);
        }
        return isAutoBuilding;
    }
    
    /**
     * Returns the workspace root.
     * 
     * @return the workspace root
     */
    public static File getWorkspaceRoot() {
        return new File(ResourcesPlugin.getWorkspace().getRoot().getLocation().toString());
    }
    
    /**
     * Returns the metadata folder within {@link #getWorkspaceRoot() the workspace}.
     * 
     * @return the metadata folder
     */
    public static File getMetadataFolder() {
        return new File(getWorkspaceRoot(), ".metadata");
    }
    
}
