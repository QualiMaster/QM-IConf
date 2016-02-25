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
package de.uni_hildesheim.sse.qmApp.treeView;

import java.util.Map;

import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.runtime.DispatcherAdapter;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import static eu.qualimaster.easy.extension.QmObservables.*;

/**
 * Implements a message dispatcher for configurable elements.
 * 
 * @author Holger Eichelberger
 */
class ConfigurableElementsDispatcher extends DispatcherAdapter implements IInfrastructureListener {

    private ConfigurableElements elements;
    
    /**
     * Creates a configurable elements dispatcher.
     * 
     * @param elements the elements
     */
    ConfigurableElementsDispatcher(ConfigurableElements elements) {
        this.elements = elements;
    }

    /**
     * Registers this instance with {@link Infrastructure}.
     */
    public void register() {
        Infrastructure.registerListener(this);
        Infrastructure.registerDispatcher(this);
    }

    /**
     * Unregisters this instance from {@link Infrastructure}.
     */
    public void unregister() {
        Infrastructure.unregisterListener(this);
        Infrastructure.unregisterDispatcher(this);
    }

    @Override
    public void handleMonitoringDataMessage(MonitoringDataMessage msg) {
        String part = msg.getPart();
        if (PART_INFRASTRUCTURE.equals(part)) {
            for (Map.Entry<String, Double> entry : msg.getObservations().entrySet()) {
                String key = entry.getKey();
                int prefixLen = 0;
                ConfigurableElement parent = null;
                if (key.startsWith(PREFIX_MACHINE)) {
                    prefixLen = PREFIX_MACHINE.length();
                    parent = findTopLevelElement(Configuration.HARDWARE);
                } else if (key.startsWith(PREFIX_HWNODE)) {
                    prefixLen = PREFIX_HWNODE.length();
                    parent = findTopLevelElement(Configuration.RECONFIG_HARDWARE);
                }
                if (null != parent && prefixLen > 0) {
                    String machine = key.substring(prefixLen);
                    String group = VariabilityModel.getHardwareGroup(machine);
                    if (VariabilityModel.DISPLAY_ALGORITHMS_NESTED && null != group) {
                        parent = findElement(parent, group, parent); // remains parent if not found  
                    }
                    ConfigurableElement node = findElement(parent, machine, null);
                    if (null != node) {
                        double val = entry.getValue();
                        ElementStatusIndicator indicator;
                        if (val > 0.5) { // there or not there
                            indicator = ElementStatusIndicator.LOW; // -> green
                        } else {
                            indicator = ElementStatusIndicator.NONE;
                        }
                        node.setStatus(indicator);
                    }
                }
            }
        }
    }
    
    /**
     * Finds a top-level element.
     * 
     * @param part the model part to search for
     * @return the element or <b>null</b> if it does not exist
     */
    private ConfigurableElement findTopLevelElement(IModelPart part) {
        ConfigurableElement result = null;
        for (int e = 0; null == result && e < elements.getElementsCount(); e++) {
            ConfigurableElement tmp = elements.getElement(e);
            if (tmp.getModelPart() == part) {
                result = tmp;
            }
        }
        return result;
    }
    
    /**
     * Finds an element by name (non-recursive).
     * 
     * @param parent the parent to search within
     * @param name the name of the element
     * @param dflt the default value
     * @return the element or if not found <code>dflt</code>
     */
    private ConfigurableElement findElement(ConfigurableElement parent, String name, ConfigurableElement dflt) {
        ConfigurableElement result = null;
        for (int c = 0; null == result && c < parent.getChildCount(); c++) {
            ConfigurableElement tmp = parent.getChild(c);
            if (tmp.getDisplayName().equals(name)) {
                result = tmp;
            }
        }
        if (null == result) {
            result = dflt;
        }
        return result;
    }

    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
        if (!hasConnection) {
            for (int e = 0; e < elements.getElementsCount(); e++) {
                clearStatus(elements.getElement(e), true);
            }
        }
    }
    
    /**
     * Clears the status of <code>element</code>.
     * 
     * @param element the element to clear
     * @param recursive whether clearing shall happen recursively
     */
    private void clearStatus(ConfigurableElement element, boolean recursive) {
        element.setStatus(ElementStatusIndicator.NONE);
        for (int c = 0; c < element.getChildCount(); c++) {
            element.getChild(c).setStatus(ElementStatusIndicator.NONE);
        }
    }
    
}
