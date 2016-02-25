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

import eu.qualimaster.adaptation.external.ExecutionResponseMessage;

/**
 * A listener for changing infrastructure states.
 * 
 * @author Holger Eichelberger
 */
public interface IInfrastructureListener {

    /**
     * Is called when the infrastructure connection state changes.
     * 
     * @param hasConnection boolean whether the new connection state is connected or not
     */
    public void infrastructureConnectionStateChanged(boolean hasConnection);

    /**
     * Handles a command execution message.
     * 
     * @param msg the message
     */
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg);

}
