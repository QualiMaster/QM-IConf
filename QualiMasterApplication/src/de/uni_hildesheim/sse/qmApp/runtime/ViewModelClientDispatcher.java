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

import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure.IClientDispatcher;
import de.uni_hildesheim.sse.qmApp.tabbedViews.adaptation.AdaptationEventsViewModel;
import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ChangeParameterRequest;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;

/**
 * A dispatcher for the view model, linking the view model with the runtime instance.
 * 
 * @author Holger Eichelberger
 */
public class ViewModelClientDispatcher implements IClientDispatcher {

    public static final ViewModelClientDispatcher INSTANCE = new ViewModelClientDispatcher();
    
    /**
     * Creates a dispatcher instance.
     */
    private ViewModelClientDispatcher() {
    }
    
    /**
     * Returns the current timestamp.
     * 
     * @return the current timestamp
     */
    private long getNow() {
        return System.currentTimeMillis();
    }
    
    @Override
    public void handleAlgorithmChangedMessage(final AlgorithmChangedMessage message) {
        // legacy, repeated via information message
    }

    @Override
    public void handleChangeParameterRequest(ChangeParameterRequest<?> arg0) {
        // not here
    }

    @Override
    public void handleDisconnectRequest(DisconnectRequest arg0) {
        // not here
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage arg0) {
        // not here
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage arg0) {
        // not here
    }

    @Override
    public void handleLoggingFilterRequest(LoggingFilterRequest arg0) {
        // not here
    }

    @Override
    public void handleLoggingMessage(LoggingMessage arg0) {
        // not here
    }

    @Override
    public void handleMonitoringDataMessage(MonitoringDataMessage arg0) {
        // not here
    }

    @Override
    public void handlePipelineMessage(PipelineMessage arg0) {
        // server side
    }

    @Override
    public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest arg0) {
        // server side
    }
    
    /**
     * Turns a string into a displayable string.
     * 
     * @param string the string
     * @return the displayable string
     */
    private static String toDisplay(String string) {
        return null == string ? "-" : string;
    }

    @Override
    public void handleInformationMessage(final InformationMessage message) {
        Display.getDefault().asyncExec(new Runnable() {
            
            @Override
            public void run() {
                String pip = toDisplay(message.getPipeline());
                String pipElt = toDisplay(message.getPipelineElement());
                AdaptationEventsViewModel.INSTANCE.addEvent(getNow(), pip, pipElt, message.getDescription());
            }
            
        });
    }

}
