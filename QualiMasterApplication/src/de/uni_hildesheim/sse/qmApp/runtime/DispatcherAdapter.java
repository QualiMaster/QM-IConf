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

import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure.IClientDispatcher;
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
 * An empty dispatcher.
 * 
 * @author Holger Eichelberger
 */
public abstract class DispatcherAdapter implements IClientDispatcher {

    @Override
    public void handleAlgorithmChangedMessage(AlgorithmChangedMessage arg0) {
    }

    @Override
    public void handleChangeParameterRequest(ChangeParameterRequest<?> arg0) {
    }

    @Override
    public void handleDisconnectRequest(DisconnectRequest arg0) {
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage arg0) {
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage arg0) {
    }

    @Override
    public void handleLoggingFilterRequest(LoggingFilterRequest arg0) {
    }

    @Override
    public void handleLoggingMessage(LoggingMessage arg0) {
    }

    @Override
    public void handleMonitoringDataMessage(MonitoringDataMessage arg0) {
    }

    @Override
    public void handlePipelineMessage(PipelineMessage arg0) {
    }

    @Override
    public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest arg0) {
    }

    @Override
    public void handleInformationMessage(InformationMessage arg0) {
    }

}
