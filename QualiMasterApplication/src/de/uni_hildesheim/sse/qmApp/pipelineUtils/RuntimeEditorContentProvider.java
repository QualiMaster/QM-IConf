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

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * This class provides the content for the tree in FileTree.
 */
public class RuntimeEditorContentProvider implements ITreeContentProvider {

    @Override
    public void dispose() {
    }

    @Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        Object[] result;
        if (inputElement instanceof List) {
            result = ((List<?>) inputElement).toArray();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        Object[] result;
        if (parentElement instanceof PipelineGraphColoringWrapper) {
            PipelineGraphColoringWrapper treeElement = (PipelineGraphColoringWrapper) parentElement;
            result = treeElement.getDecsendants();
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public Object getParent(Object element) {
        return null;
    }

    @Override
    public boolean hasChildren(Object element) {
        boolean toReturn = false;
        if (element instanceof PipelineGraphColoringWrapper) {
            PipelineGraphColoringWrapper treeElement = (PipelineGraphColoringWrapper) element;
            
            if (treeElement.getDescendantsCount() > 0) {
                toReturn = true;
            } 
        }
        return toReturn;
    }
}