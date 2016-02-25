/*
 * Copyright 2009-2014 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.model;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Display;

import de.uni_hildesheim.sse.dslCore.TopLevelModelAccessor;
import de.uni_hildesheim.sse.dslcore.ui.ConfigurationEditorFactory;
import de.uni_hildesheim.sse.qmApp.treeView.IMenuContributor;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import de.uni_hildesheim.sse.vil.rt.ui.embed.EditorUtils;
import de.uni_hildesheim.sse.vil.rt.ui.embed.NamedViewerFilter;
import de.uni_hildesheim.sse.vil.rt.ui.embed.SimulatorUi;

/**
 * Implements the menu contributor for rt-VIL.
 * 
 * @author Holger Eichelberger
 */
public class RtVilMenuContributor implements IMenuContributor {

    private SimulatorUi simulator;

    /**
     * Creates a new simulator menu contributor including a new simulation environment.
     */
    public RtVilMenuContributor() {
        ModelInfo<?> info = TopLevelModelAccessor.getTopLevelModel(EditorUtils.EXTENSION, 
             ModelAccess.getProjectName(), null);
        if (null != info) {
            NamedViewerFilter[] filters = new NamedViewerFilter[1];
            filters[0] = new NamedViewerFilter("runtime variables", 
                ConfigurationEditorFactory.createAttributeFilter("bindingTime", "BindingTime\\.runtime", false));
            simulator = new SimulatorUi(Display.getDefault().getActiveShell(), 
                VariabilityModel.Definition.TOP_LEVEL.getConfiguration(), 
                Location.getModelLocationFile(), 
                info, filters);
        }
    }
    
    @Override
    public void contributeTo(IMenuManager manager) {
        if (null != simulator) {
            Action action = new Action() {
                @Override
                public void run() {
                    simulator.openSettingsDialog();
                }
            };
            action.setText("Simulation setup...");
            manager.add(action);
    
            action = new Action() {
                @Override
                public void run() {
                    simulator.configureOrSimulate();
                }
            };
            action.setText("Simulate");
            manager.add(action);
        }
    }

}
