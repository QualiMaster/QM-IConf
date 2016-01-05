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
import eu.qualimaster.adaptation.external.ClientEndpoint;
import eu.qualimaster.adaptation.external.DisconnectMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.IDispatcher;
import eu.qualimaster.adaptation.external.Message;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmMessage;

/**
 * Represents the actual connection to the infrastructure. Infrastructure messages
 * are dispatched further to the registered dispatchers. The reception of a disconnect message causes the 
 * active infrastructure to be disconnected.
 * 
 * @author Holger Eichelberger
 */
public class Infrastructure {

    private static ClientEndpoint endpoint;
    private static List<IDispatcher> dispatchers = Collections.synchronizedList(new ArrayList<IDispatcher>());

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
        public void handleDisconnect(DisconnectMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleDisconnect(message);
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
        public void handleMonitoringData(MonitoringDataMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleMonitoringData(message);
            }
        }

        @Override
        public void handlePipelineMessage(PipelineMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handlePipelineMessage(message);
            }
        }

        @Override
        public void handleSwitchAlgorithm(SwitchAlgorithmMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleSwitchAlgorithm(message);
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
                ep.schedule(new DisconnectMessage());
            }
            ep.stop();
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
    
}
