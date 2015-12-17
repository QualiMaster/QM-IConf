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
package de.uni_hildesheim.sse.qmApp.editorInput;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * Creates no editor inputs - just in case that we don't know at some point which editor input to create.
 * 
 * @author Holger Eichelberger
 */
public class EmptyEditorInputCreator extends AbstractNonVariableEditorInputCreator {
    
    private String name;
    
    /**
     * Creates an empty editor input creator.
     * 
     * @param name the display name
     */
    public EmptyEditorInputCreator(String name) {
        this.name = name;
    }
    
    @Override
    public IEditorInput create() {
        return new IEditorInput() {
                
            @SuppressWarnings({ "rawtypes", "unchecked" })
            @Override
            public Object getAdapter(Class adapter) {
                return null;
            }
            
            @Override
            public String getToolTipText() {
                return null;
            }
            
            @Override
            public IPersistableElement getPersistable() {
                return null;
            }
            
            @Override
            public String getName() {
                return name;
            }
            
            @Override
            public ImageDescriptor getImageDescriptor() {
                return null;
            }
            
            @Override
            public boolean exists() {
                return false;
            }
        };
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isReadable() {
        return true;
    }
    
}
