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

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Definition;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.StatusHighlighter;
import de.uni_hildesheim.sse.qmApp.runtime.DispatcherAdapter;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.easy.extension.QmObservables;
import eu.qualimaster.easy.extension.internal.PipelineHelper;
import eu.qualimaster.easy.extension.internal.VariableHelper;

import static eu.qualimaster.easy.extension.QmObservables.*;

/**
 * Implements a message dispatcher for configurable elements.
 * 
 * @author Holger Eichelberger
 */
class ConfigurableElementsDispatcher extends DispatcherAdapter implements IInfrastructureListener {

    private ConfigurableElements elements;
    private TreeViewer elementsViewer;
    
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
        de.uni_hildesheim.sse.model.confModel.Configuration cfg = Definition.TOP_LEVEL.getConfiguration();
        IDecisionVariable var = PipelineHelper.obtainPipelineByName(cfg, part);
        // TODO ugly extend message accordingly
        if (null == var) {
            IDecisionVariable actPipelines = ModelAccess.findTopContainer(Configuration.INFRASTRUCTURE, 
                Configuration.INFRASTRUCTURE.getProvidedTypes()[0]); // uhh
            for (int n = 0; n < actPipelines.getNestedElementsCount(); n++) {
                IDecisionVariable pip = actPipelines.getNestedElement(n);
                IDecisionVariable elt = PipelineHelper.obtainPipelineElementByName(pip, null, part);
                if (null != elt) {
                    String pipelineName = VariableHelper.getName(pip);
                    String variableName = VariableHelper.getName(elt);
                    if (null != pipelineName && null != variableName) {
                        StatusHighlighter.INSTANCE.markPipeline(pipelineName, variableName, 
                            getPipelineStatusIndicator(pipelineName, observations));
                    }
                }
            }
        } else {
            String pipelineName = VariableHelper.getName(var);
            if (null != pipelineName) {
                StatusHighlighter.INSTANCE.markPipelineStatus(pipelineName, 
                    getPipelineStatusIndicator(pipelineName, observations));
            }
        }
    }
    
    /**
     * Returns a heuristic pipeline status indicator.
     * 
     * @param part the part name
     * @param observations the actual observations to derive the indicator from
     * @return the status indicator
     */
    private ElementStatusIndicator getPipelineStatusIndicator(String part, Map<String, Double> observations) {
        ElementStatusIndicator result = ElementStatusIndicator.NONE;
        Double throughput = observations.get(QmObservables.TIMEBEHAVIOR_THROUGHPUT_ITEMS);
        Double items = observations.get(QmObservables.SCALABILITY_ITEMS);
        if (null == throughput || null == items) {
            result = ElementStatusIndicator.NONE;
        } else {
            if (null != items) {
                if (items < 800) {
                    result = ElementStatusIndicator.HIGH;
                } else if (items < 1000) {
                    result = ElementStatusIndicator.LOW;
                } else if (items < 1200) {
                    result = ElementStatusIndicator.MEDIUM;
                } else {
                    result = ElementStatusIndicator.HIGH;
                }
            } 
            if (null != throughput && throughput < 10) {
                result = ElementStatusIndicator.VERYLOW;
            }
        }
        return result;
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
            clearStatus(findTopLevelElement(Configuration.HARDWARE), true);
            clearStatus(findTopLevelElement(Configuration.RECONFIG_HARDWARE), true);
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
    
}
