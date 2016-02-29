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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

import de.uni_hildesheim.sse.qmApp.io.CSVWriter;

/**
 * Model and content provider for the {@link AdaptationEventsLogView},
 * stores all adaptation events.
 * @author El-Sharkawy
 *
 */
public class AdaptationEventsViewModel implements IStructuredContentProvider  {
    
    public static final AdaptationEventsViewModel INSTANCE = new AdaptationEventsViewModel();
    
    private TableViewer viewer;
    private boolean autoRefresh;
    
    /*
     * Used ArrayList for the toArray() method ;-)
     */
    private ArrayList<AdaptationViewItem> items;

    /**
     * Singleton constructor.
     */
    private AdaptationEventsViewModel() {
        viewer = null;
        autoRefresh = true;
        items = new ArrayList<AdaptationViewItem>();
        //addEvent(System.currentTimeMillis(), "PriorityPip", "elem", "An element from Priority Pip was exchanged.");
    }
    
    /**
     * Adds a new event and refreshes the {@link AdaptationEventsLogView}. If auto refreshing (on by default)
     * was not disabled via the {@link #setAutoRefresh(boolean)} method,
     * this will also force the {@link AdaptationEventsLogView} to show the new item directly.
     * @param timestamp The timestamp when the event was created.
     * @param pipelineName The pipeline where the event was created.
     * @param element The element of the pipeline, which was affected.
     * @param description A detailed description of this event.
     */
    public void addEvent(long timestamp, String pipelineName, String element, String description) {
        AdaptationViewItem item = new AdaptationViewItem(timestamp, pipelineName, element, description);
        items.add(item);
        if (autoRefresh && null != viewer) {
            // Update only row instead of whole table.
            viewer.add(item);
        }
    }
    
    /**
     * Deletes all events.
     */
    public void clearEvents() {
        items.clear();
        if (autoRefresh) {
            refresh();
        }
    }
    
    /**
     * Refreshes the {@link AdaptationEventsLogView}.
     * This should only be called from outside, if auto refreshing was disabled via the
     * {@link #setAutoRefresh(boolean)} method. 
     */
    public void refresh() {
        if (null != viewer) {
            viewer.refresh();
        }
    }
    
    /**
     * En-/disables auto refreshing the editor. Auto refreshing may be disabled for performance reasons.
     * @param autoRefresh <tt>true</tt> enable auto refresh (default setting), <tt>false</tt> disable auto refresh.
     */
    public void setAutoRefresh(boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }
    
    /**
     * Sets the {@link AdaptationEventsLogView} to allow editor updates whenever a new event was added to this model.
     * @param viewer The viewer to be refreshed.
     */
    void setViewer(TableViewer viewer) {
        this.viewer = viewer;
        if (autoRefresh) {
            refresh();
        }
    }
    
    /**
     * Saves all events to the specified file.
     * @param target The target file where to save the information. Existing files will be overwritten.
     * @throws IOException If an I/O error occurs
     */
    public void save(File target) throws IOException {
        CSVWriter writer = null;
        try {
            SimpleDateFormat dateFormater = new SimpleDateFormat("EEE, MMM d, ''yy 'at' HH:mm:ss z");
            writer = new CSVWriter(target, true, ";", "\n");
            for (int i = 0, end = items.size(); i < end; i++) {
                AdaptationViewItem item = items.get(i);
                writer.writeLine(dateFormater.format(new Date(item.getTimestamp())),
                    item.getPipelineName(), item.getElement(), item.getDescription());
            }
        } finally {
            if (null != writer) {
                writer.close();
            }
        }
        
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
