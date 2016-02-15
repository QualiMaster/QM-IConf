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
package de.uni_hildesheim.sse.qmApp.tabbedViews.adaptation;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

/**
 * Model and content provider for the {@link AdaptationEventsLogView},
 * stores all adaptation events.
 * @author El-Sharkawy
 *
 */
public class AdaptationEventsViewModel implements IStructuredContentProvider  {
    
    public static final AdaptationEventsViewModel INSTANCE = new AdaptationEventsViewModel();
    
    private TableViewer viewer;
    
    /*
     * Used ArrayList for the toArray() method ;-)
     */
    private ArrayList<AdaptationViewItem> items;

    /**
     * Singleton constructor.
     */
    private AdaptationEventsViewModel() {
        viewer = null;
        items = new ArrayList<AdaptationViewItem>();
        items.add(new AdaptationViewItem(System.currentTimeMillis(), "PriorityPip", "Adaptation",
            "An element from Priority Pip was exchanged."));
    }
    
    /**
     * Adds a new event and refreshes the {@link AdaptationEventsLogView}.
     * @param timestamp The timestamp when the event was created.
     * @param pipelineName The pipeline where the event was created.
     * @param type The type of event
     * @param description A detailed description of this event.
     */
    public void addEvent(long timestamp, String pipelineName, String type, String description) {
        items.add(new AdaptationViewItem(timestamp, pipelineName, type, description));
        refresh();
    }
    
    /**
     * Deletes all events.
     */
    public void clearEvents() {
        items.clear();
        refresh();
    }
    
    /**
     * Refreshes the {@link AdaptationEventsLogView} if it was set via its setter method.
     */
    private void refresh() {
        if (null != viewer) {
            viewer.refresh();
        }
    }
    
    /**
     * Sets the {@link AdaptationEventsLogView} to allow editor updates whenever a new event was added to this model.
     * @param viewer The viewer to be refreshed.
     */
    void setViewer(TableViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void dispose() {
        // Not needed
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        // Not needed
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return items.toArray();
    }
}
