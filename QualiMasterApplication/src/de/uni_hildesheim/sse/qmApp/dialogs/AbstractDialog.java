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
package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * An abstract dialog.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public abstract class AbstractDialog extends Dialog {

    /**
     * Creates the dialog.
     * 
     * @param parentShell the parent shell
     */
    protected AbstractDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Method used to set the dialog in the centre of the monitor.
     * 
     * @param newShell The machine configurator.
    */
    protected void setDialogLocation(Shell newShell) {
        
        //parent 
        Rectangle monitorArea = newShell.getParent().getBounds();
        
        int x = monitorArea.x + (monitorArea.width - newShell.getBounds().width) / 2;
        int y = monitorArea.y + (monitorArea.height - newShell.getBounds().height) / 2;
        
        newShell.setLocation(x, y);
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.pack();
        newShell.setSize(getIntendedSize());
     
        super.configureShell(newShell);
        newShell.setText(getTitle());
        setDialogLocation(newShell);
    }
    
    /**
     * Returns the (initial) title.
     * 
     * @return the initial title
     */
    protected abstract String getTitle();

    /**
     * Returns the intended (initial) size.
     * 
     * @return the intended initial size
     */
    protected abstract Point getIntendedSize();

    /**
     * Returns the (class-dependent) logger.
     * 
     * @return the logger instance
     */
    protected EASyLogger getLogger() {
        return EASyLoggerFactory.INSTANCE.getLogger(getClass(), Activator.PLUGIN_ID);
    }
    
}
