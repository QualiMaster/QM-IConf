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

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage.ResultType;

/**
 * Implements the default handling of infrastructure execution messages. Actually,
 * we display them as dialogs, but also a list view (on the level of the console) would
 * be nice.
 * 
 * @author Holger Eichelberger
 */
class ExecutionMessageHandler extends InfrastructureListenerAdapter {

    static final IInfrastructureListener INSTANCE = new ExecutionMessageHandler();

    /**
     * Prevents external creation.
     */
    private ExecutionMessageHandler() {
    }
    
    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg) {
        final ResultType result = msg.getResult();
        final String description = msg.getDescription();
        final IWorkbench workbench = PlatformUI.getWorkbench();
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable() {
            public void run() {
                if (!display.isDisposed()) {
                    Shell shell = display.getActiveShell();
                    if (ResultType.FAILED == result) {
                        String desc = description;
                        if (null == desc || 0 == desc.length()) {
                            desc = "<unknown reason>";
                        }
                        Dialogs.showErrorDialog(shell, "Infrastructure command execution failed", desc);
                    } else {
                        String desc = description;
                        if (null == desc || 0 == desc.length()) {
                            desc = "Successful.";
                        }
                        Dialogs.showInfoDialog(shell, "Infrastructure command execution succeeded", desc);
                    }
                }
            }
        });
    }

}
