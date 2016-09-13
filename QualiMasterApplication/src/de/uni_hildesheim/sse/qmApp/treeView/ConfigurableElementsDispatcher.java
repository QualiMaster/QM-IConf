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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter;
import de.uni_hildesheim.sse.qmApp.runtime.DispatcherAdapter;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.easy.extension.QmObservables;

import static eu.qualimaster.easy.extension.QmObservables.*;

/**
 * Implements a message dispatcher for configurable elements.
 * 
 * @author Holger Eichelberger
 */
class ConfigurableElementsDispatcher extends DispatcherAdapter implements IInfrastructureListener {

    private ConfigurableElements elements;
    private TreeViewer elementsViewer;
    private Map<String, Map<String, ElementStatusIndicator>> pipelineElementStates 
        = new HashMap<String, Map<String, ElementStatusIndicator>>();
    
    /**
     * Creates a configurable elements dispatcher.
     * 
     * @param elements the elements
     * @param elementsViewer the viewer to update
     */
    ConfigurableElementsDispatcher(ConfigurableElements elements, TreeViewer elementsViewer) {
        this.elements = elements;
        this.elementsViewer = elementsViewer;
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
                        setStatus(node, indicator);
                    }
                }
            }
        } else {
            tryHighlightPipeline(msg.getPart(), msg.getObservations());
        }
    }
    
    /**
     * Tries to infer the actual pipeline from <code>part</code> and to highlight it. Actually,
     * the monitoring message does not contain enough data for direct access :(
     * 
     * @param part the part
     * @param observations the observations
     */
    private void tryHighlightPipeline(String part, Map<String, Double> observations) {
        int pos = part.indexOf(":");
        if (pos > 0 && pos + 1 < part.length()) {
            String pipelineName = part.substring(0, pos);
            String pipelineElementName = part.substring(pos + 1);
            markPipeline(pipelineName, pipelineElementName, 
                getPipelineStatusIndicator(pipelineName, pipelineElementName, observations));
        } else {
            ConfigurableElement node = findPipeline(part);
            if (null != node) {
                setStatus(node, getPipelineStatusIndicator(part, null, observations));
            }
        }
    }
    
    /**
     * Mark a specific variable within a pipeline given by the corresponding indicator. Avoids changing an indicator
     * if it already has been set.
     * 
     * @param pipelineName name of the pipeline.
     * @param elementName name of the pipeline element.
     * @param indicator indicator which indicates the situation of the element.
     */
    public void markPipeline(final String pipelineName, final String elementName, 
        ElementStatusIndicator indicator) {
        Map<String, ElementStatusIndicator> pipelineMap = pipelineElementStates.get(pipelineName);
        if (null == pipelineMap) {
            pipelineMap = new HashMap<String, ElementStatusIndicator>();
            pipelineElementStates.put(pipelineName, pipelineMap);
        }
        ElementStatusIndicator tmp = pipelineMap.get(elementName);
        if (tmp == indicator) {
            tmp = null;
        } else {
            pipelineMap.put(elementName, indicator);
            tmp = indicator;
        }
        if (null != tmp) {
            final ElementStatusIndicator eltIndicator = tmp;
            Display.getDefault().asyncExec(new Runnable() {
    
                @Override
                public void run() {
                    StatusHighlighter.getInstance().markPipeline(pipelineName, elementName, eltIndicator);
                }
            });
        }
    }
    
    /**
     * Returns a heuristic pipeline status indicator.
     * 
     * @param pipeline the name of the pipeline
     * @param element the pipeline element (may be <b>null</b>)
     * @param observations the actual observations to derive the indicator from
     * @return the status indicator
     */
    private ElementStatusIndicator getPipelineStatusIndicator(String pipeline, String element, 
        Map<String, Double> observations) {
        ElementStatusIndicator result = ElementStatusIndicator.NONE;
        Double throughput = observations.get(QmObservables.TIMEBEHAVIOR_THROUGHPUT_ITEMS);
        
        double center = 500;
        /*if ("SwitchPip".equals(pipeline)) {
            center = 500;
        } else {
            center = 1000;
        }*/
        double low = center * 0.8;
        double high = center * 0.8;
        
        Double items = observations.get(QmObservables.SCALABILITY_ITEMS);
        if (null == throughput || null == items) {
            result = ElementStatusIndicator.NONE;
        } else {
            if (null != items) {
                if (items < low) {
                    result = ElementStatusIndicator.LOW;
                } else if (items < center) {
                    result = ElementStatusIndicator.MEDIUM;
                } else if (items < high) {
                    result = ElementStatusIndicator.LOW;
                } else {
                    result = ElementStatusIndicator.VERYLOW;
                }
            } 
            if (null != throughput && throughput < 10) {
                result = ElementStatusIndicator.VERYLOW;
            }
        }
        return result;
    }
    
    /**
     * Finds a pipeline configurable node of a given <code>pipeline</code> name.
     * 
     * @param pipeline the pipeline name
     * @return the configurable element or <b>null</b> if it does not exist
     */
    private ConfigurableElement findPipeline(String pipeline) {
        return findElement(findTopLevelElement(Configuration.PIPELINES), pipeline, null);        
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
     * @param parent the parent to search within (may be <b>null</b>)
     * @param name the name of the element
     * @param dflt the default value
     * @return the element or if not found <code>dflt</code>
     */
    private ConfigurableElement findElement(ConfigurableElement parent, String name, ConfigurableElement dflt) {
        ConfigurableElement result = null;
        if (null != parent) {
            for (int c = 0; null == result && c < parent.getChildCount(); c++) {
                ConfigurableElement tmp = parent.getChild(c);
                if (tmp.getDisplayName().equals(name)) {
                    result = tmp;
                }
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
            clearStatus(findTopLevelElement(Configuration.HARDWARE), true);
            clearStatus(findTopLevelElement(Configuration.RECONFIG_HARDWARE), true);
            clearStatus(findTopLevelElement(Configuration.PIPELINES), true);
        }
    }
    
    /**
     * Clears the status of <code>element</code>.
     * 
     * @param element the element to clear
     * @param recursive whether clearing shall happen recursively
     */
    private void clearStatus(ConfigurableElement element, boolean recursive) {
        if (null != element) {
            setStatus(element, ElementStatusIndicator.NONE);
            if (recursive) {
                for (int c = 0; c < element.getChildCount(); c++) {
                    clearStatus(element.getChild(c), true);
                }
            }
        }
    }
    
    /**
     * Changes the status of a configurable elements and performs an UI update in case of a status change.
     * 
     * @param element the element
     * @param status the new status
     */
    private void setStatus(final ConfigurableElement element, ElementStatusIndicator status) {
        if (element.getStatus() != status) {
            element.setStatus(status);
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    elementsViewer.refresh(element, true);
                }
                
            });
        }
    }
    
    @Override
    public void handleInformationMessage(InformationMessage msg) {
        if (Infrastructure.isStopPipelineMessage(msg)) {
            String pipelineName = msg.getPipeline();
            ConfigurableElement element = findPipeline(pipelineName);
            if (null != element) {
                setStatus(element, ElementStatusIndicator.NONE);
            }
            pipelineElementStates.remove(pipelineName);
        }
    }

}