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
package de.uni_hildesheim.sse.qmApp.runtime;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ChangeParameterRequest;
import eu.qualimaster.adaptation.external.ClientEndpoint;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.IDispatcher;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.Message;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;

/**
 * Represents the actual connection to the infrastructure. Infrastructure messages
 * are dispatched further to the registered dispatchers. The reception of a disconnect message causes the 
 * active infrastructure to be disconnected.
 * 
 * @author Holger Eichelberger
 */
public class Infrastructure {

    private static ClientEndpoint endpoint;
    private static List<IDispatcher> dispatchers 
        = Collections.synchronizedList(new ArrayList<IDispatcher>());
    private static List<IInfrastructureListener> listeners 
        = Collections.synchronizedList(new ArrayList<IInfrastructureListener>());

    /**
     * Prevents external creation.
     */
    private Infrastructure() {
    }
    
    /**
     * Implements a delegating dispatcher, i.e., a dispatcher that delegates to the registered
     * dispatchers.
     * 
     * @author Holger Eichelberger
     */
    private static class DelegatingDispatcher implements IDispatcher {

        @Override
        public void handleAlgorithmChangedMessage(AlgorithmChangedMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleAlgorithmChangedMessage(message);
            }
        }

        @Override
        public void handleDisconnectRequest(DisconnectRequest message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleDisconnectRequest(message);
            }
            disconnect(false);
        }

        @Override
        public void handleHardwareAliveMessage(HardwareAliveMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleHardwareAliveMessage(message);
            }
        }

        @Override
        public void handleMonitoringDataMessage(MonitoringDataMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleMonitoringDataMessage(message);
            }
        }

        @Override
        public void handlePipelineMessage(PipelineMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handlePipelineMessage(message);
            }
        }

        @Override
        public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleSwitchAlgorithmRequest(message);
            }
        }

        @Override
        public void handleChangeParameterRequest(ChangeParameterRequest<?> message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleChangeParameterRequest(message);
            }
        }

        @Override
        public void handleExecutionResponseMessage(ExecutionResponseMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleExecutionResponseMessage(message);
            }
        }

        @Override
        public void handleLoggingFilterRequest(LoggingFilterRequest message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleLoggingFilterRequest(message);
            }
        }

        @Override
        public void handleLoggingMessage(LoggingMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleLoggingMessage(message);
            }
        }
        
    }

    /**
     * Creates a client endpoint if necessary. Does not overwrite an existing endpoint.
     * 
     * @param address the IP address of the infrastructure (adaptation layer)
     * @param port the port numger 
     * @throws IOException in case that the endpoint cannot be created
     * @see #releaseEndpoint()
     */
    public static void connect(InetAddress address, int port) throws IOException {
        if (null == endpoint) {
            endpoint = new ClientEndpoint(new DelegatingDispatcher(), address, port);
            notifyConnectionChange(true);
        }
    }
    
    /**
     * Sends the given <code>msg</code> if there is an active endpoint. The message
     * will be ignored if there is no active endpoint.
     * 
     * @param msg the message to be scheduled
     */
    public static void send(Message msg) {
        if (null != endpoint) {
            endpoint.schedule(msg);
        }
    }
    
    /**
     * Releases the actual endpoint. Nothing happens if there is no actual endpoint. 
     */
    public static void disconnect() {
        disconnect(true);
    }
    
    /**
     * Releases the actual endpoint. Nothing happens if there is no actual endpoint.
     * 
     * @param sendMsg whether a {@link DisconnectMessage} shall be sent
     */
    public static void disconnect(boolean sendMsg) {
        if (null != endpoint) {
            ClientEndpoint ep = endpoint;
            endpoint = null; // -> message
            if (sendMsg) {
                ep.schedule(new DisconnectRequest());
            }
            ep.stop();
            notifyConnectionChange(false);
        }
    }

    /**
     * Returns whether the infrastructure is connected, i.e., there is an actual endpoint.
     * 
     * @return <code>true</code> if connected, <code>false</code> else
     */
    public static boolean isConnected() {
        return null != endpoint;
    }
    
    /**
     * Registers a dispatcher. Already registered dispatchers are ignored.
     * 
     * @param dispatcher the dispatcher to be registered (may be <b>null</b>, ignored then)
     */
    public static void registerDispatcher(IDispatcher dispatcher) {
        if (null != dispatcher && !dispatchers.contains(dispatcher)) {
            dispatchers.add(dispatcher);
        }
    }
    
    /**
     * Unregisters a dispatcher.
     * 
     * @param dispatcher the dispatcher to be unregistered (may be <b>null</b>, ignored then)
     */
    public static void unregisterDispatcher(IDispatcher dispatcher) {
        if (null != dispatcher) {
            dispatchers.remove(dispatcher);
        }
    }
    
    /**
     * Registers a listener. Already registered listeners are ignored.
     * 
     * @param listener the listener to be registered (may be <b>null</b>, ignored then)
     */
    public static void registerListener(IInfrastructureListener listener) {
        if (null != listener && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Unregisters a listener.
     * 
     * @param listener the listener to be unregistered (may be <b>null</b>, ignored then)
     */
    public static void unregisterListener(IInfrastructureListener listener) {
        if (null != listener) {
            listeners.remove(listener);
        }
    }

    /**
     * Notifies listeners about a connection state change.
     * 
     * @param hasConnection whether the new connection state is connected or not
     */
    private static void notifyConnectionChange(boolean hasConnection) {
        for (int l = 0; l < listeners.size(); l++) {
            listeners.get(l).infrastructureConnectionStateChanged(hasConnection);
        }
    }
    
}
