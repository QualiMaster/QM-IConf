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
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.SubStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

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

    /**
     * Returns the active workbench window.
     * 
     * @return the active workbench window (may be <b>null</b> if there is none)
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        return PlatformUI.getWorkbench().getActiveWorkbenchWindow();
    }
    
    /**
     * Returns the active editor part.
     * 
     * @return the active editor part (may be <b>null</b> if there is none)
     * @see #getActiveWorkbenchWindow()
     */
    public static IEditorPart getActiveEditorPart() {
        IEditorPart editorPart = null;
        IWorkbenchPage wPage = null;
        IWorkbenchWindow wWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (null != wWindow) {
            wPage = wWindow.getActivePage();
        }
        if (null != wPage) {
            editorPart = wPage.getActiveEditor();
        }
        return editorPart;
    }

    /**
     * Returns the editor site of the active editor.
     * 
     * @return the editor site (may be <b>null</b> if there is none)
     * @see #getActiveEditorPart()
     */
    public static IEditorSite getActiveEditorSite() {
        IEditorSite editorSite = null;
        IEditorPart editorPart = getActiveEditorPart();
        if (null != editorPart) {
            editorSite = editorPart.getEditorSite();
        }
        return editorSite;
    }

    /**
     * Returns the action bars of the active editor.
     * 
     * @return the action bars (may be <b>null</b> if there is none)
     * @see #getActiveEditorSite()
     */
    public static IActionBars getActiveActionBars() {
        IActionBars actionBars = null;
        IEditorSite editorSite = getActiveEditorSite();
        if (null != editorSite) {
            actionBars = editorSite.getActionBars();
        }
        return actionBars;
    }

    /**
     * Returns the status line manager of the active editor.
     * 
     * @param setVisible turns the status line manager visible if needed
     * @return the status line manager (may be <b>null</b> if there is none)
     * @see #getActiveActionBars()
     */
    public static IStatusLineManager getActiveStatusLineManager(boolean setVisible) {
        IStatusLineManager statusManager = null;
        IActionBars actionBars = getActiveActionBars();
        if (null != actionBars) {
            statusManager = actionBars.getStatusLineManager();
            if (setVisible && statusManager instanceof SubStatusLineManager) {
                SubStatusLineManager mgr = (SubStatusLineManager) statusManager;
                if (!mgr.isVisible()) {
                    mgr.setVisible(true);
                }
            }
        }
        return statusManager;
    }
    
}
