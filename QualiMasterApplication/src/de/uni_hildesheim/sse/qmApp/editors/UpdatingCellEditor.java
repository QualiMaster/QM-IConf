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
package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

import de.uni_hildesheim.sse.easy.ui.productline_editor.IUpdateListener;
import de.uni_hildesheim.sse.easy.ui.productline_editor.IUpdateProvider;

/**
 * Implements a cell editor with basic update provider capabilities. Intended as a base class for implementing
 * cell editors for specific model types.
 * When overriding {@link #doSetValue(Object)}, please call the super class method,i.e., transitively the one of 
 * this class.
 * 
 * @author Holger Eichelberger
 */
public abstract class UpdatingCellEditor extends CellEditor implements IUpdateProvider {

    private IUpdateListener listener;

    /**
     * Creates a new cell editor with no control The cell editor has no cell
     * validator.
     */
    protected UpdatingCellEditor() {
        super();
    }

    /**
     * Creates a new cell editor under the given parent control. The cell editor
     * has no cell validator.
     *
     * @param parent the parent control
     */
    protected UpdatingCellEditor(Composite parent) {
        super(parent);
    }

    /**
     * Creates a new cell editor under the given parent control. The cell editor
     * has no cell validator.
     *
     * @param parent the parent control
     * @param style the style bits
     */
    protected UpdatingCellEditor(Composite parent, int style) {
        super(parent, style);
    }
    
    @Override
    public void refreshContents() {
        // not relevant here, the value change is the interesting part
    }

    @Override
    public void setUpdateListener(IUpdateListener listener) {
        this.listener = listener;
    }
    
    @Override
    protected void doSetValue(Object value) {
        notifyValueChanged();
    }
    
    /**
     * Called to notify about a value change.
     */
    protected void notifyValueChanged() {
        if (null != listener) {
            listener.valueChanged(this);
        }
    }

}
