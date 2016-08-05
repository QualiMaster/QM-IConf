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
import eu.qualimaster.adaptation.external.CloudPipelineMessage;
import eu.qualimaster.adaptation.external.ConfigurationChangeMessage;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.PipelineStatusRequest;
import eu.qualimaster.adaptation.external.PipelineStatusResponse;
import eu.qualimaster.adaptation.external.ReplayMessage;
import eu.qualimaster.adaptation.external.ResourceChangeMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;
import eu.qualimaster.adaptation.external.UpdateCloudResourceMessage;

/**
 * An empty dispatcher.
 * 
 * @author Holger Eichelberger
 */
public abstract class DispatcherAdapter implements IClientDispatcher {

    @Override
    public void handleAlgorithmChangedMessage(AlgorithmChangedMessage message) {
    }

    @Override
    public void handleChangeParameterRequest(ChangeParameterRequest<?> message) {
    }

    @Override
    public void handleDisconnectRequest(DisconnectRequest message) {
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage message) {
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage message) {
    }

    @Override
    public void handleLoggingFilterRequest(LoggingFilterRequest message) {
    }

    @Override
    public void handleLoggingMessage(LoggingMessage message) {
    }

    @Override
    public void handleMonitoringDataMessage(MonitoringDataMessage message) {
    }

    @Override
    public void handlePipelineMessage(PipelineMessage message) {
    }

    @Override
    public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest message) {
    }

    @Override
    public void handleInformationMessage(InformationMessage message) {
    }

    @Override
    public void handlePipelineStatusRequest(PipelineStatusRequest message) {
    }

    @Override
    public void handlePipelineStatusResponse(PipelineStatusResponse message) {
    }

    @Override
    public void handleUpdateCloudResourceMessage(UpdateCloudResourceMessage message) {
    }

    @Override
    public void handleCloudPipelineMessage(CloudPipelineMessage message) {
    }

    @Override
    public void handleReplayMessage(ReplayMessage message) {
    }

    @Override
    public void handleConfigurationChangeMessage(ConfigurationChangeMessage message) {
    }

    @Override
    public void handleResourceChangeMessage(ResourceChangeMessage message) {
    }

}
