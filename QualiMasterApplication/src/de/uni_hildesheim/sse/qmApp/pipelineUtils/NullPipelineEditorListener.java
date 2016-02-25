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
package de.uni_hildesheim.sse.qmApp.pipelineUtils;

/**
 * A default listener doing nothing.
 * 
 * @author Holger Eichelberger
 */
public class NullPipelineEditorListener implements IPipelineEditorListener {

    public static final IPipelineEditorListener INSTANCE = new NullPipelineEditorListener();

    /**
     * Prevent external creation.
     */
    private NullPipelineEditorListener() {
    }
    
    @Override
    public void nodeAdded(String name) {
    }

    @Override
    public void nodeRemoved(String name) {
    }

    @Override
    public void flowAdded(String node1, String node2) {
    }

    @Override
    public void flowRemoved(String node1, String node2) {
    }

}
