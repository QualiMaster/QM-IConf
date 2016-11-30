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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

/**
 * Implements a {@link CellLabelProvider} for the treeview of the {@link StatisticsDialog}.
 * @author El-Sharkawy
 *
 */
class StatisticsLabelProvider extends CellLabelProvider {
    private int column;
    
    /**
     * Sole constructor.
     * @param column The column for which this label provider is created for (0 = name, 1 = value).
     */
    StatisticsLabelProvider(int column) {
        this.column = column;
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        
        if (element instanceof StatisticsItem) {
            StatisticsItem item = (StatisticsItem) element;
            if (column == 1) {
                cell.setText(item.getValue());
            } else {
                cell.setText(item.getLabel());
            }
        }
        
    }

}
