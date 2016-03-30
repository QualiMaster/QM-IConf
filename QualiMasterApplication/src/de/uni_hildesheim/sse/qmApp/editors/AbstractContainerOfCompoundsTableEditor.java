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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;

import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;

/**
 * A container of compounds editor.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractContainerOfCompoundsTableEditor extends AbstractTableEditor {

    /**
     * Implements a container value accessor for on-demand access to changing models.
     * 
     * @author Holger Eichelberger
     */
    protected class ContainerValueAccessor implements IValueAccessor  {
        
        private Map<Object, Integer> positions = new HashMap<Object, Integer>();
        
        /**
         * Associates a key (table object) with a position.
         * 
         * @param key the key
         * @param position the position
         */
        protected void associate(Object key, int position) {
            positions.put(key,  position);
        }
        
        /**
         * Dissociates the key (table object) associated with the given <code>position</code>.
         * 
         * @param position the position to dissociate
         */
        protected void dissociate(int position) {
            Object toRemove = null;
            for (Map.Entry<Object, Integer> ent : positions.entrySet()) {
                int entPos = ent.getValue();
                if (entPos == position) {
                    toRemove = ent.getKey();
                } else if (entPos > position) {
                    ent.setValue(entPos - 1);
                }
            }
            if (null != toRemove) {
                positions.remove(toRemove);
            }
        }
        
        /**
         * Returns the associated position for <code>key</code>.
         * 
         * @param key the key to return the position for
         * @return the associated position (may be <b>null</b>)
         */
        protected Integer getPosition(Object key) {
            return positions.get(key);
        }
        
        /**
         * Clears the associated positions.
         */
        protected void clear() {
            positions.clear();
        }

        @Override
        public CompoundValue getValue(Object key) {
            CompoundValue result = null;
            Integer pos = getPosition(key);
            if (null != pos) {
                ContainerValue cVal = getContainer();
                if (0 <= pos && pos < cVal.getElementSize()) {
                    Value tmp = cVal.getElement(pos);
                    if (tmp instanceof CompoundValue) {
                        result = (CompoundValue) tmp;
                    }
                }
            }
            return result;
        }
        
    }

    
    /**
     * Creates an instance.
     * 
     * @param config the parent UI configuration
     * @param variable the variable to be edited
     * @param parent the parent composite
     * @param style the composite style
     */
    protected AbstractContainerOfCompoundsTableEditor(UIConfiguration config, IDecisionVariable variable, 
        Composite parent, int style) {
        super(config, variable, parent, style);
    }
    
    /**
     * Returns the actual compound of {@link #getVariable()}.
     * 
     * @return the actual compound
     */
    protected abstract Compound getCompound();
    
    /**
     * Returns the actual container value of {@link #getVariable()}.
     * 
     * @return the container value
     */
    protected abstract ContainerValue getContainer();

}
