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

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

import net.ssehub.easy.varModel.confModel.IDecisionVariable;

/**
 * Implements a generic variable selector dialog.
 * 
 * @author Holger Eichelberger
 */
public class VariableSelectorDialog extends AbstractVariableSelectorDialog {

    private Collection<IDecisionVariable> data;
    
    /**
     * Creates a single-selection variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     * @param data the data to select from
     */
    public VariableSelectorDialog(Shell shell, String title, Collection<IDecisionVariable> data) {
        this(shell, title, data, false);
    }

    /**
     * Creates a variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     * @param data the data to select from
     * @param multi whether multiple selections are allowed
     */
    public VariableSelectorDialog(Shell shell, String title, Collection<IDecisionVariable> data, boolean multi) {
        super(shell, title, multi);
        this.data = data;
    }
    
    @Override
    protected String getDialogSettingsName() {
        return "FilteredVariableSelectorDialogSettings";
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
        IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("Filling elements", data.size());
        Iterator<IDecisionVariable> iter = data.iterator();
        while (iter.hasNext()) {
            IDecisionVariable var = iter.next();
            contentProvider.add(var, itemsFilter);
            progressMonitor.worked(1);
        }
        progressMonitor.done();
    }
    
}