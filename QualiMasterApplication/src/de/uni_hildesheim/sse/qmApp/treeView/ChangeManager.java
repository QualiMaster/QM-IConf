package de.uni_hildesheim.sse.qmApp.treeView;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIChangeListener;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;

/**
 * Implements a central change manager in order to keep UI elements up to date.
 * Please note that UI elements call and use this mechanism, not the model part!
 * 
 * @author Holger Eichelberger
 */
public class ChangeManager {

    /**
     * Denotes the three event kindes handled by this manager.
     * 
     * @author Holger Eichelberger
     */
    public enum EventKind {
        ADDED,
        CHANGED,
        DELETING,
        DELETED;
    }
    
    /**
     * Defines a change listener for decision variables.
     * 
     * @author Holger Eichelberger
     */
    public interface IChangeListener {

        /**
         * Notifies interested parties about a variable that is about to be deleted.
         * 
         * @param kind the event kind
         * @param variable the modified variable
         * @param globalIndex the global index of the variable (in its configured collection), 
         *   <code>-1</code> if no such index / collection exists, {@link #NO_INDEX} for 
         *   {@link EventKind#ADDED} and {@link EventKind#CHANGED}
         */
        public void variableChanged(EventKind kind, IDecisionVariable variable, int globalIndex);

    }

    public static final int NO_INDEX = -1;
    public static final ChangeManager INSTANCE = new ChangeManager();
    
    private List<IChangeListener> listeners = new ArrayList<IChangeListener>();
    
    private UIChangeListener uiChangeListener = new UIChangeListener() {

        @Override
        public void valueChanged(IDecisionVariable variable) {
            AbstractVariable decl = variable.getDeclaration();
            IDatatype type = decl.getType();
            // filter out irrelevant events
            if (VariabilityModel.isNameSlot(decl)) {
                if (variable.getParent() instanceof IDecisionVariable) {
                    variableChanged(null, (IDecisionVariable) variable.getParent());
                }
            } else if (Container.TYPE.isAssignableFrom(type)) {
                variableChanged(null, variable);
            }
        }
        
    };

    /**
     * Prevents external creation (Singleton9.
     */
    private ChangeManager() {
    }

    /**
     * Adds a change listener.
     * 
     * @param listener the listener to be added
     */
    public void addListener(IChangeListener listener) {
        if (null != listener) {
            listeners.add(listener);
        }
    }
    
    /**
     * Removes a change listener.
     * 
     * @param listener the listener to be removed
     */
    public void removeListener(IChangeListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Returns a usable UI change listener which informs this
     * class about changes on UI variables in EASy (see {@link net.ssehub.easy.producer.ui.productline_editor.
     * ConfigurationTableEditorFactory.UIConfiguration#commitValues(UIChangeListener)}).
     * 
     * @return the UI change listener
     */
    public UIChangeListener getUIChangeListener() {
        return uiChangeListener;
    }
    
    /**
     * Notifies interested parties about an added variable.
     * 
     * @param source the source of the event, required to avoid cycles among {@link IChangeListener listeners}
     * @param variable the added variable
     */
    public void variableAdded(Object source, IDecisionVariable variable) {
        notifyListeners(source, EventKind.ADDED, variable, NO_INDEX);
    }

    /**
     * Notifies interested listeners about a changed variable. Filters
     * for {@link eu.qualimaster.easy.extension.QmConstants#SLOT_NAME} and container variables.
     * 
     * @param source the source of the event, required to avoid cycles among {@link IChangeListener listeners}
     * @param variable the changed variable
     */
    public void variableChanged(Object source, IDecisionVariable variable) {
        notifyListeners(source, EventKind.CHANGED, variable, NO_INDEX);
    }

    /**
     * Notifies interested parties about a variable that is about to be deleted.
     * 
     * @param source the source of the event, required to avoid cycles among {@link IChangeListener listeners}
     * @param variable the variable being deleted
     * @param globalIndex the global index of the variable (in its configuring collection), 
     *   <code>-1</code> if no such index / collection exists
     */
    public void variableDeleting(Object source, IDecisionVariable variable, int globalIndex) {
        notifyListeners(source, EventKind.DELETING, variable, globalIndex);
    }

    /**
     * Notifies interested parties about a variable that has been deleted.
     * 
     * @param source the source of the event, required to avoid cycles among {@link IChangeListener listeners}
     * @param variable the variable being deleted
     * @param globalIndex the global index of the variable (in its configuring collection), 
     *   <code>-1</code> if no such index / collection exists
     */
    public void variableDeleted(Object source, IDecisionVariable variable, int globalIndex) {
        notifyListeners(source, EventKind.DELETED, variable, globalIndex);
    }
    
    /**
     * Used to notify all listeners (excluding <code>source</code>).
     * 
     * @param source the source of the event (may be <b>null</b>)
     * @param kind the event kind
     * @param variable the modified variable
     * @param globalIndex the global index of the variable (in its configured collection), 
     *   <code>-1</code> if no such index / collection exists, {@link #NO_INDEX} for 
     *   {@link EventKind#ADDED} and {@link EventKind#CHANGED}
     */
    private void notifyListeners(Object source, EventKind kind, IDecisionVariable variable, int globalIndex) {
        for (int l = 0; l < listeners.size(); l++) {
            IChangeListener listener = listeners.get(l);
            if (source != listener) { // yes, shall be the same in terms of reference
                listener.variableChanged(kind, variable, globalIndex);
            }
        }
    }

}
