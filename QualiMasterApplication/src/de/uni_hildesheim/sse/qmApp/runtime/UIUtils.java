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
package de.uni_hildesheim.sse.qmApp.runtime;

import org.eclipse.jface.action.Action;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;

/**
 * Some runtime-related UI utilities.
 * 
 * @author Holger Eichelberger
 */
public class UIUtils {

    /**
     * Prevent external instantiation.
     */
    private UIUtils() {
    }
    
    /**
     * Customizes an administrative infrastructure action and adds the action to the manager.
     * 
     * @param action the action
     * @param name the name of the action
     */
    public static void customizeAdminInfraAction(Action action, String name) {
        action.setText(name);
        String tooltipReason;
        if (UserContext.INSTANCE.isInfrastructureAdmin()) {
            if (Infrastructure.isConnected()) {
                if (Infrastructure.isAuthenticated()) {
                    tooltipReason = null;
                } else {
                    tooltipReason = "infrastructure connection is not authenticated. Please log in first.";
                }
            } else {
                tooltipReason = "infrastructure is not connected. Further, for this action you must be logged in.";
            }
        } else {
            tooltipReason = "you do not have administrative permissions.";
        }
        if (null == tooltipReason) {
            action.setEnabled(true);
        } else {
            action.setEnabled(false);
            String text = "The " + name + " action is not enabled, because " + tooltipReason;
            // neither working in SWT ;(
            action.setToolTipText(text);
            action.setDescription(text);
        }
    }

}
