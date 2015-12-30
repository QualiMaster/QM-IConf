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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.IConfigurationElement;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.images.IconManager;

/**
 * The {@link ArtifactEditor} specializes {@link AbstractTextSelectionEditorCreator} to select from a 
 * class list.
 * 
 * @author Holger Eichelberger
 */
public class ClassEditor extends AbstractTextSelectionEditorCreator {

    public static final boolean ENABLE_BROWSE = false; // TODO connect Manifest utils
    public static final IEditorCreator CREATOR = new ClassEditor();
    
    /**
     * Prevents external creation.
     */
    private ClassEditor() {
    }

    @Override
    protected void browseButtonSelected(String text, IDecisionVariable context, ITextUpdater updater) {
        List<String> classes = new ArrayList<String>();
        String artifact = getArtifact(context);
        if (null != artifact) {
            // TODO ask Manifest utils
            System.out.println("RETRIEVE CLASSES FROM " + artifact);
        }
        classes.add("java.util.String");
        classes.add("java.util.Object");
        ClassSelectorDialog dlg = new ClassSelectorDialog(DialogsUtil.getActiveShell(), "Select class", classes);
        dlg.setInitialPattern("?");
        int res = dlg.open();
        String cls = dlg.getFirstResult();
        if (Window.OK == res && null != cls) {
            updater.updateText(cls);
        }
    }
    
    @Override
    protected boolean isTextEditorEnabled(boolean cell, IDecisionVariable context) {
        return true;
    }
    
    @Override
    protected boolean isBrowseButtonActive(boolean cell, IDecisionVariable context) {
        return ENABLE_BROWSE && null != getArtifact(context); // TODO use Manifest utils
    }

    /**
     * Returns the artifact specification from the (parent) decision variable.
     * 
     * @param context the context for this editor
     * @return the artifact specification (if available, may be <b>null</b> if there is none)
     */
    private String getArtifact(IDecisionVariable context) {
        String result = null;
        if (null != context) {
            IConfigurationElement par = context.getParent();
            if (par instanceof CompoundVariable) {
                CompoundVariable artifactHolder = (CompoundVariable) par;
                IDecisionVariable artifactVar = artifactHolder.getNestedVariable("artifact");
                if (null != artifactVar) {
                    Value val = artifactVar.getValue();
                    if (val instanceof StringValue) {
                        result = ((StringValue) val).getValue();
                    }
                }
            }
        }
        return result;
    }
    
    /**
     * Keep this class private until the interface of Patrick is clear.
     * 
     * @author Holger Eichelberger
     */
    private static class ClassSelectorDialog extends FilteredItemsSelectionDialog {

        private List<String> classes;

        /**
         * Creates the selector dialog.
         * 
         * @param shell the parent shell
         * @param title the title of the dialog
         * @param classes the classes to select from
         */
        public ClassSelectorDialog(Shell shell, String title, List<String> classes) {
            super(shell);
            this.classes = classes;
            setTitle(title);
            setListLabelProvider(new ClassListLabelProvider(false));
            setDetailsLabelProvider(new ClassListLabelProvider(true));
            setSelectionHistory(new ClassSelectionHistory());
        }

        /**
         * Implements an empty variable selection history.
         * 
         * @author Holger Eichelberger
         */
        private static class ClassSelectionHistory extends SelectionHistory {
            
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
        private class ClassListLabelProvider implements ILabelProvider {

            private boolean qualified;
            
            /**
             * Creates a label provider.
             * 
             * @param qualified display qualified names or not
             */
            private ClassListLabelProvider(boolean qualified) {
                this.qualified = qualified;
            }
            
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
                return IconManager.retrieveImage(IconManager.CLASS);
            }

            @Override
            public String getText(Object element) {
                String name = getElementName(element);
                if (!qualified) {
                    int pos = name.lastIndexOf('.');
                    if (pos > 0 && pos < name.length() - 1) {
                        name = name.substring(pos + 1);
                    }
                }
                return name;
            }
            
        }
        
        @Override
        protected Control createExtendedContentArea(Composite parent) {
            return null;
        }

        @Override
        protected IDialogSettings getDialogSettings() {
            return DialogsUtil.getDialogSettings("FilteredClassSelectorDialogSettings");
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
                    return matches(item.toString());
                }
            
                @Override
                public boolean isConsistentItem(Object item) {
                    return true;
                }
            };
        }

        @Override
        protected Comparator<Object> getItemsComparator() {
            return Collator.getInstance();
        }

        @Override
        protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
            for (int c = 0; c < classes.size(); c++) {
                contentProvider.add(classes.get(c), itemsFilter);
            }
        }

        @Override
        public String getElementName(Object item) {
            return item.toString();
        }

        @Override
        public String getFirstResult() {
            return (String) super.getFirstResult();
        }

    }

}
