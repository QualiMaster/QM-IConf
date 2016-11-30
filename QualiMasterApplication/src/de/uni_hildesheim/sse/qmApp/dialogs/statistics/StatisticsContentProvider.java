/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.dialogs.statistics;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Content provider for the {@link StatisticsDialog}. Expects an {@link ArrayList} of {@link StatisticsItem}s as input.
 * @author El-Sharkawy
 *
 */
class StatisticsContentProvider implements ITreeContentProvider {

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
        Object[] items = null;
        
        if (inputElement instanceof ArrayList<?>) {
            items = ((ArrayList<?>) inputElement).toArray();
        }
        
        return items;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
        Object[] children = null;
        
        if (parentElement instanceof StatisticsItem) {
            children = ((StatisticsItem) parentElement).getChildren();
        }
        
        return children;
    }

    @Override
    public Object getParent(Object element) {
        Object parent = null;
        
        if (element instanceof StatisticsItem) {
            parent = ((StatisticsItem) element).getParent();
        }
        
        return parent;
    }

    @Override
    public boolean hasChildren(Object element) {
        boolean hasChildren = false;
        
        if (element instanceof StatisticsItem) {
            hasChildren = ((StatisticsItem) element).hasChildren();
        }
        
        return hasChildren;
    }

}
