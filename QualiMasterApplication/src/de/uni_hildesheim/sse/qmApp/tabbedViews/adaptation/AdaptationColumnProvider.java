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
package de.uni_hildesheim.sse.qmApp.tabbedViews.adaptation;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Column provider for the {@link AdaptationEventsLogView}, specifies what to show in which column.
 * @author El-Sharkawy
 *
 */
class AdaptationColumnProvider extends ColumnLabelProvider {
    
    private static final SimpleDateFormat FORMATER = new SimpleDateFormat("EEE, MMM d, ''yy 'at' HH:mm:ss z");
    
    private int column;
    
    /**
     * Single constructor for this class.
     * @param column The column for which this provider is created (for each column different data will be shown).
     */
    AdaptationColumnProvider(int column) {
        this.column = column;
    }

    @Override
    public String getText(Object element) {
        String text = null;
        if (element instanceof AdaptationViewItem) {
            AdaptationViewItem item = (AdaptationViewItem) element;
            switch (column) {
            case 0:
                text = FORMATER.format(new Date(item.getTimestamp()));
                break;
            case 1:
                text = item.getPipelineName();
                break;
            case 2:
                text = item.getType();
                break;
            case 3:
                text = item.getDescription();
                break;
            default:
                // No output by default for undefined columns
                break;
            }
        }
        return text;
    }

}
