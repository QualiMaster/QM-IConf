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

/**
 * Model object for the {@link StatisticsDialog}.
 * @author El-Sharkawy
 *
 */
class StatisticsItem {
    
    private String label;
    private String value;
    
    private Object parent;
    private ArrayList<StatisticsItem> nestedItems;

    /**
     * Sole constructor.
     * @param label The name of the item to display in the first column.
     * @param value The value, to display in the second column.
     * @param parent Either the parent {@link StatisticsItem} or the {@link ArrayList}
     *     containing the top level elements.
     */
    StatisticsItem(String label, String value, Object parent) {
        this.label = label;
        this.value = value;
        nestedItems = new ArrayList<>();
        this.parent = parent;
        if (parent instanceof StatisticsItem) {
            ((StatisticsItem) parent).add(this);
        }
    }
    
    /**
     * Returns the name of the item. 
     * @return The name of the item to display in the first column.
     */
    String getLabel() {
        return label;
    }
    
    /**
     * Returns the value.
     * @return The value, to display in the second column.
     */
    String getValue() {
        return value;
    }
    
    /**
     * Returns the parent as needed by Eclipse's content provider.
     * @return The parent model element.
     */
    Object getParent() {
        return parent;
    }
    
    /**
     * Adds a child item.
     * @param nested The child to add.
     */
    void add(StatisticsItem nested) {
        nestedItems.add(nested);
    }
    
    /**
     * Returns the list of nested elements as needed by Eclipse's content provider.
     * @return The list of nested elements, maybe empty.
     */
    Object[] getChildren() {
        return nestedItems.toArray();
    }
    
    /**
     * Returns whether this item has nested Items.
     * @return <tt>true</tt> it has children, <tt>false</tt> this is a leaf.
     */
    boolean hasChildren() {
        return !nestedItems.isEmpty();
    }
}
