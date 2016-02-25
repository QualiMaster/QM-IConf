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
package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import de.uni_hildesheim.sse.easy.ui.productline_editor.DelegatingEasyEditorPage;

/**
 * A listener that turns the editor into the dirty state.
 * 
 * @author Holger Eichelberger
 */
public class DirtyListener implements KeyListener, SelectionListener {
    
    private DelegatingEasyEditorPage parent;
    
    /**
     * Creates a listener instance.
     * 
     * @param parent the parent page
     */
    public DirtyListener(DelegatingEasyEditorPage parent) {
        this.parent = parent;
    }
    
    @Override
    public void keyReleased(KeyEvent event) {
        boolean strgS = (SWT.CTRL == (event.stateMask & SWT.CTRL)) && ('s' == event.keyCode);
        // TODO check
        boolean prevent = event.keyCode == 262144 && event.stateMask == 262144; // occurs after strg+s, unkown
        if (!strgS && !prevent) {
            parent.setDirty(); // this is dirty listener -> fire property change
        }
    }
    
    @Override
    public void keyPressed(KeyEvent event) {
    }

    @Override
    public void widgetSelected(SelectionEvent event) {
        parent.setDirty(); // this is dirty listener -> fire property change
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent event) {
    }
    
};

