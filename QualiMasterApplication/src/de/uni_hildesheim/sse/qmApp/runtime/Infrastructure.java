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

import de.uni_hildesheim.sse.qmApp.dialogs.EclipsePrefUtils;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.AuthenticateMessage;
import eu.qualimaster.adaptation.external.ChangeParameterRequest;
import eu.qualimaster.adaptation.external.ClientEndpoint;
import eu.qualimaster.adaptation.external.CloudPipelineMessage;
import eu.qualimaster.adaptation.external.ConfigurationChangeMessage;
import eu.qualimaster.adaptation.external.ConnectedMessage;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.HilariousAuthenticationHelper;
import eu.qualimaster.adaptation.external.IDispatcher;
import eu.qualimaster.adaptation.external.IInformationDispatcher;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.Message;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.PipelineStatusRequest;
import eu.qualimaster.adaptation.external.PipelineStatusResponse;
import eu.qualimaster.adaptation.external.ReplayMessage;
import eu.qualimaster.adaptation.external.ResourceChangeMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;
import eu.qualimaster.adaptation.external.UpdateCloudResourceMessage;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.logger.EASyLoggerFactory.EASyLogger;
import qualimasterapplication.Activator;

/**
 * Represents the actual connection to the infrastructure. Infrastructure messages
 * are dispatched further to the registered dispatchers. The reception of a disconnect message causes the 
 * active infrastructure to be disconnected.
 * 
 * @author Holger Eichelberger
 */
public class Infrastructure {

    private static ClientEndpoint endpoint;
    private static List<IClientDispatcher> dispatchers 
        = Collections.synchronizedList(new ArrayList<IClientDispatcher>());
    private static List<IInfrastructureListener> listeners 
        = Collections.synchronizedList(new ArrayList<IInfrastructureListener>());

    /**
     * Prevents external creation.
     */
    private Infrastructure() {
    }

    /**
     * Defines a combining interface.
     * 
     * @author Holger Eichelberger
     */
    public interface IClientDispatcher extends IDispatcher, IInformationDispatcher {
    }
    
    /**
     * Implements a delegating dispatcher, i.e., a dispatcher that delegates to the registered
     * dispatchers.
     * 
     * @author Holger Eichelberger
     */
    private static class DelegatingDispatcher implements IDispatcher, IInformationDispatcher {

        @Override
        public void handleAlgorithmChangedMessage(AlgorithmChangedMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleAlgorithmChangedMessage(message);
            }
        }

        @Override
        public void handleDisconnectRequest(DisconnectRequest message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleDisconnectRequest(message);
            }
            disconnect(false);
        }

        @Override
        public void handleHardwareAliveMessage(HardwareAliveMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleHardwareAliveMessage(message);
            }
        }

        @Override
        public void handleMonitoringDataMessage(MonitoringDataMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleMonitoringDataMessage(message);
            }
        }

        @Override
        public void handlePipelineMessage(PipelineMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handlePipelineMessage(message);
            }
        }

        @Override
        public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleSwitchAlgorithmRequest(message);
            }
        }

        @Override
        public void handleChangeParameterRequest(ChangeParameterRequest<?> message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleChangeParameterRequest(message);
            }
        }

        @Override
        public void handleExecutionResponseMessage(ExecutionResponseMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleExecutionResponseMessage(message);
            }
            for (int l = 0, n = listeners.size(); l < n; l++) {
                listeners.get(l).handleExecutionResponseMessage(message);
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

        @Override
        public void handleInformationMessage(InformationMessage message) {
            for (int d = 0; d < dispatchers.size(); d++) {
                dispatchers.get(d).handleInformationMessage(message);
            }
        }

        @Override
        public void handlePipelineStatusRequest(PipelineStatusRequest message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handlePipelineStatusRequest(message);
            }
        }

        @Override
        public void handlePipelineStatusResponse(PipelineStatusResponse message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handlePipelineStatusResponse(message);
            }
        }

        @Override
        public void handleUpdateCloudResourceMessage(UpdateCloudResourceMessage message) {
            // do not forward
        }

        @Override
        public void handleCloudPipelineMessage(CloudPipelineMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleCloudPipelineMessage(message);
            }
        }

        @Override
        public void handleReplayMessage(ReplayMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleReplayMessage(message);
            }
        }

        @Override
        public void handleConfigurationChangeMessage(ConfigurationChangeMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleConfigurationChangeMessage(message);
            }
        }

        @Override
        public void handleResourceChangeMessage(ResourceChangeMessage message) {
            for (int d = 0, n = dispatchers.size(); d < n; d++) {
                dispatchers.get(d).handleResourceChangeMessage(message);
            }
        }
        
    }

    /**
     * Creates a client endpoint if necessary. Does not overwrite an existing endpoint.
     * 
     * @param address the IP address of the infrastructure (adaptation layer)
     * @param port the port number
     * @throws IOException in case that the endpoint cannot be created
     * @see #releaseEndpoint()
     */
    public static void connect(InetAddress address, int port) throws IOException {
        if (null == endpoint) {
            endpoint = new ClientEndpoint(new DelegatingDispatcher(), address, port);
            boolean isInfrastructureAdmin = UserContext.INSTANCE.isInfrastructureAdmin();
            String user = getUserName();
            boolean simpleConnect = true;
            if (isInfrastructureAdmin) {
                if (null != user) {
                    byte[] passphrase = HilariousAuthenticationHelper.obtainPassphrase(user);
                    if (null != passphrase) {
                        endpoint.schedule(new AuthenticateMessage(user, passphrase));
                        simpleConnect = false;
                    } else {
                        getLogger().info("No passphrase for user '" + user + "'. Connecting without authentication.");
                    }
                } else {
                    getLogger().info("No user name given to infrastructure. Connecting without authentication.");
                }
            } else {
                getLogger().info("Not running with admin permissions. Connecting without authentication.");
            }
            if (simpleConnect) {
                // send a ping so that we can show a dialog
                endpoint.schedule(new ConnectedMessage());
            }
            notifyConnectionChange(true);
        }
    }
    
    /**
     * Returns the logger.
     * 
     * @return the logger
     */
    private static EASyLogger getLogger() {
        return EASyLoggerFactory.INSTANCE.getLogger(Infrastructure.class, Activator.PLUGIN_ID);
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
     * Returns whether there is an established connection to the QM infrastructure , i.e., there is an actual endpoint.
     * 
     * @return <code>true</code> if connected, <code>false</code> else
     */
    public static boolean isConnected() {
        return null != endpoint;
    }
    
    /**
     * Returns whether there is an established authenticated connection to the QM infrastructure , i.e., there is an 
     * actual endpoint.
     * 
     * @return <code>true</code> if authenticated, <code>false</code> else
     */
    public static boolean isAuthenticated() {
        return null != endpoint && endpoint.isAuthenticated();
    }
    
    /**
     * Registers a dispatcher. Already registered dispatchers are ignored.
     * 
     * @param dispatcher the dispatcher to be registered (may be <b>null</b>, ignored then)
     */
    public static void registerDispatcher(IClientDispatcher dispatcher) {
        if (null != dispatcher && !dispatchers.contains(dispatcher)) {
            dispatchers.add(dispatcher);
        }
    }
    
    /**
     * Unregisters a dispatcher.
     * 
     * @param dispatcher the dispatcher to be unregistered (may be <b>null</b>, ignored then)
     */
    public static void unregisterDispatcher(IClientDispatcher dispatcher) {
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
    
    /**
     * Returns the actual user name from the user context / workspace / preferences.
     * 
     * @return the user name (may be <b>null</b>)
     */
    public static String getUserName() {
        return EclipsePrefUtils.INSTANCE.getPreference(EclipsePrefUtils.USERNAME_PREF_KEY);
    }
    
    /**
     * Registers default listeners.
     */
    public static void registerDefaultListeners() {
        registerListener(ExecutionMessageHandler.INSTANCE);
        registerDispatcher(ViewModelClientDispatcher.INSTANCE);
    }

    /**
     * Unregisters default listeners.
     */
    public static void unregisterDefaultListeners() {
        unregisterDispatcher(ViewModelClientDispatcher.INSTANCE);
        unregisterListener(ExecutionMessageHandler.INSTANCE);
    }
    
    /**
     * Returns whether a message indicates stopping of the pipeline given in the message.
     * 
     * @param msg the message
     * @return <code>true</code> for a stop message, <code>false</code> else
     */
    public static boolean isStopPipelineMessage(InformationMessage msg) {
        boolean result = false;
        if (null != msg.getPipeline() && null == msg.getPipelineElement()) { 
            result = null != msg && msg.getDescription().equals("stop pipeline successful"); // TODO ugly
        }
        return result;
    }

}
