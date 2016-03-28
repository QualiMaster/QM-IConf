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
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Shell;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * A variable selector dialog based on a given configuration.
 * 
 * @author Holger Eichelberger
 */
public class ConfigurationVariableSelectorDialog extends AbstractVariableSelectorDialog {

    /**
     * Selects variables for display.
     * 
     * @author Holger Eichelberger
     */
    public interface IVariableSelector {

        /**
         * Whether the given <code>variable</code> is enabled for display.
         * 
         * @param variable the variable
         * @return <code>true</code> if the <code>variable</code> is enabled, <code>false</code> else
         */
        public boolean enabled(IDecisionVariable variable);
    }
    
    /**
     * A type-based variable selector, i.e., types and their sub-types are enabled.
     * 
     * @author Holger Eichelberger
     */
    public static class TypeBasedVariableSelector implements IVariableSelector {

        private IDatatype[] types;

        /**
         * Creates a type-based variable selector.
         * 
         * @param types the types which enable the selection
         */
        public TypeBasedVariableSelector(IDatatype[] types) {
            this.types = types;
        }
        
        /**
         * Creates a type-based variable selector.
         * 
         * @param types the types which enable the selection
         */
        public TypeBasedVariableSelector(Collection<IDatatype> types) {
            this.types = new IDatatype[types.size()];
            types.toArray(this.types);
        }
        
        @Override
        public boolean enabled(IDecisionVariable variable) {
            boolean enabled = false;
            IDatatype varType = variable.getDeclaration().getType();
            for (int t = 0; !enabled && t < types.length; t++) {
                enabled = types[t].isAssignableFrom(varType);
            }
            return enabled;
        }
        
    }
    
    private Configuration config;
    private IVariableSelector selector;
    
    /**
     * Creates a single-selection variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     * @param config the configuration to select from
     * @param selector a generic variable selector
     */
    public ConfigurationVariableSelectorDialog(Shell shell, String title, Configuration config, 
        IVariableSelector selector) {
        this(shell, title, config, selector, false);
    }

    /**
     * Creates a variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     * @param config the configuration to select from
     * @param selector a generic variable selector
     * @param multi whether multiple selections are allowed
     */
    public ConfigurationVariableSelectorDialog(Shell shell, String title, Configuration config, 
        IVariableSelector selector, boolean multi) {
        super(shell, title, multi);
        this.config = config;
        this.selector = selector;
    }

    @Override
    protected String getDialogSettingsName() {
        return "FilteredConfigurationVariableSelectorDialogSettings";
    }

    @Override
    protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
        IProgressMonitor progressMonitor) throws CoreException {
        progressMonitor.beginTask("Filling elements", config.getDecisionCount());
        TreeMap<String, IDecisionVariable> tmp = new TreeMap<String, IDecisionVariable>();
        Iterator<IDecisionVariable> iter = config.iterator();
        while (iter.hasNext()) {
            IDecisionVariable var = iter.next();
            if (null == selector || selector.enabled(var)) {
                tmp.put(ModelAccess.getDisplayName(var), var);
            }
            progressMonitor.worked(1);
        }
        for (Map.Entry<String, IDecisionVariable> entry : tmp.entrySet()) {
            contentProvider.add(entry.getValue(), itemsFilter);
        }
        progressMonitor.done();
    }

}
