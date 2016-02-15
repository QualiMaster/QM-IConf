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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.part.ViewPart;

/**
 * A moveable view to display events of the adaptation layer.
 * See <a href="https://eclipse.org/articles/viewArticle/ViewArticle2.html">
 * https://eclipse.org/articles/viewArticle/ViewArticle2.html</a> for how to add <b>Menus and Toolbars</b>.
 * @author El-Sharkawy
 *
 */
public class AdaptationEventsLogView extends ViewPart {
    private TableViewer viewer;
    
    /**
     * Creates the Adaptation Events Log View instance ad default position.
     * Instances will be created by means of the <tt>plugin.xml</tt> specification.
     */
    public AdaptationEventsLogView() {
        super();
    }
    
    @Override
    public void setFocus() {
        viewer.getTable().setFocus();
    }
    
    @Override
    public void createPartControl(Composite parent) {
        viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
        Table table = viewer.getTable();

        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        String[] titles = {"Time", "Pipeline", "Event Type", "Description"};
        int[] defColumnSizes = {180, 75, 75, 280};
        for (int i = 0; i < titles.length; i++) {
            TableViewerColumn column = new TableViewerColumn(viewer, SWT.NONE);
            column.getColumn().setText(titles[i]);
            column.getColumn().setWidth(defColumnSizes[i]);
            column.setLabelProvider(new AdaptationColumnProvider(i));
        }
        table.setSize(table.computeSize(SWT.DEFAULT, 200));
        
        viewer.setContentProvider(AdaptationEventsViewModel.INSTANCE);
        AdaptationEventsViewModel.INSTANCE.setViewer(viewer);
        viewer.setInput(AdaptationEventsViewModel.INSTANCE);
    }
}
