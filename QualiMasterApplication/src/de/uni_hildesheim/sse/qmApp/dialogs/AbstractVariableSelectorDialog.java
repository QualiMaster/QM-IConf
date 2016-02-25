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

import java.util.Comparator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;

/**
 * Implements a generic variable selector dialog.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractVariableSelectorDialog extends FilteredItemsSelectionDialog {

    /**
     * Implements an empty variable selection history.
     * 
     * @author Holger Eichelberger
     */
    private class VariableSelectionHistory extends SelectionHistory {
        
        @Override
        protected Object restoreItemFromMemento(IMemento element) {
            return null; 
        }

        @Override
        protected void storeItemToMemento(Object item, IMemento element) {
        }
         
    }
    
    /**
     * Implements a list label provider for variables.
     * 
     * @author Holger Eichelberger
     */
    private class VariableListLabelProvider implements ILabelProvider {

        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        @Override
        public void dispose() {
        }

        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        @Override
        public void removeListener(ILabelProviderListener listener) {
        }

        @Override
        public Image getImage(Object element) {
            Image image = IconManager.retrieveImage(IconManager.ERROR);
            if (element instanceof IDecisionVariable) {
                IDatatype type = ((IDecisionVariable) element).getDeclaration().getType();
                image = IconManager.retrieveImage(type);
            }
            return image;
        }

        @Override
        public String getText(Object element) {
            return getElementName(element);
        }
        
    }
    
    /**
     * Creates a single-selection variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     */
    public AbstractVariableSelectorDialog(Shell shell, String title) {
        this(shell, title, false);
    }

    /**
     * Creates a variable selector dialog.
     * 
     * @param shell the parent shell
     * @param title the title
     * @param multi whether multiple selections are allowed
     */
    public AbstractVariableSelectorDialog(Shell shell, String title, boolean multi) {
        super(shell, multi);
        setTitle(title);
        ILabelProvider labelProvider = new VariableListLabelProvider(); 
        setListLabelProvider(labelProvider);
        setDetailsLabelProvider(labelProvider); // simplification for now
        setSelectionHistory(new VariableSelectionHistory());
    }
    
    /**
     * Returns the name of the persistent dialog settings.
     * 
     * @return the name
     */
    protected abstract String getDialogSettingsName();

    @Override
    protected Control createExtendedContentArea(Composite parent) {
        return null;
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        return DialogsUtil.getDialogSettings(getDialogSettingsName());
    }

    @Override
    protected IStatus validateItem(Object item) {
        return Status.OK_STATUS;
    }

    @Override
    protected ItemsFilter createFilter() {
        SearchPattern pattern = new SearchPattern();
        pattern.setPattern(getInitialPattern());
        return new ItemsFilter(pattern) {
            
            @Override
            public boolean matchItem(Object item) {
                String text = getElementName(item);
                if (item instanceof IDecisionVariable) {
                    text = ModelAccess.getDisplayName((IDecisionVariable) item);
                } 
                if (null == text) {
                    text = item.toString();
                }
                return matches(text);
            }
        
            @Override
            public boolean isConsistentItem(Object item) {
                return true;
            }
        };
    }

    @Override
    protected Comparator<Object> getItemsComparator() {
        return new Comparator<Object>() {
            public int compare(Object arg0, Object arg1) {
                String name0 = getElementName(arg0);
                String name1 = getElementName(arg1);
                return name0.compareTo(name1);
            }
        };       
    }

    @Override
    public String getElementName(Object item) {
        String name = null;
        if (item instanceof IDecisionVariable) {
            name = ModelAccess.getDisplayName((IDecisionVariable) item);
        } 
        if (null == name) {
            name = item.toString();
        }
        return name;
    }

    @Override
    public IDecisionVariable getFirstResult() {
        return (IDecisionVariable) super.getFirstResult();
    }
    
    @Override
    public IDecisionVariable[] getResult() {
        Object[] tmp = super.getResult();
        IDecisionVariable[] result;
        if (null != tmp) {
            result = new IDecisionVariable[tmp.length];
            for (int r = 0; r < tmp.length; r++) {
                result[r] = (IDecisionVariable) tmp[r];
            }
        } else {
            result = null;
        }
        return result;
    }
    
}