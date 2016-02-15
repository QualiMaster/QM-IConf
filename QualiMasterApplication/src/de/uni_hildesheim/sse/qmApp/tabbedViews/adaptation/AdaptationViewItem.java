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

/**
 * Row entry of the Adaptation Events Log (data element).
 * @author El-Sharkawy
 *
 */
class AdaptationViewItem {
    
    private long timestamp;
    private String pipelineName;
    private String type;
    private String description;
    
    /**
     * Single constructor of this class.
     * @param timestamp The timestamp when the event was created.
     * @param pipelineName The pipeline where the event was created.
     * @param type The type of event
     * @param description A detailed description of this event.
     */
    AdaptationViewItem(long timestamp, String pipelineName, String type, String description) {
        this.timestamp = timestamp;
        this.pipelineName = pipelineName;
        this.type = type;
        this.description = description;
    }

    /**
     * Getter for the time stamp.
     * @return When the event was created.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter for the pipeline.
     * @return The name of the pipeline.
     */
    public String getPipelineName() {
        return pipelineName;
    }

    /**
     * The kind of event.
     * @return The kind of event, maybe <tt>null</tt>.
     */
    public String getType() {
        return type;
    }

    /**
     * Getter for the detailed description.
     * @return The detailed description, maybe <tt>null</tt>.
     */
    public String getDescription() {
        return description;
    }
}
