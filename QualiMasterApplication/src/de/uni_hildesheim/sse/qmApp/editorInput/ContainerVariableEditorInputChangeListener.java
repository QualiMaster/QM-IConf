package de.uni_hildesheim.sse.qmApp.editorInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part of the treeview of the QM App, is responsible for adapting index numbers if
 * an element was deleted.
 * @author El-Sharkawy
 *
 */
public class ContainerVariableEditorInputChangeListener {
    
    public static final ContainerVariableEditorInputChangeListener INSTANCE
        = new ContainerVariableEditorInputChangeListener();
    
    /**
     * Interface for index based editor inputs of the tree view.
     * @author El-Sharkawy
     *
     */
    static interface IContainerVariableEditorInputChangeListener {
        /**
         * Notification that an element was removed.
         * @param parentName The parent (the list itself) of which the element was removed.
         * @param index The index of the removed element, elements behind this index must move up.
         */
        void notifyDeletetion(String parentName, int index);
    }

//    private static final Map<String, String> BLACKLIST = new HashMap<String, String>();
    
    private Map<String, List<IContainerVariableEditorInputChangeListener>> listeners
        = new HashMap<String, List<IContainerVariableEditorInputChangeListener>>();
    
//    static {
//        BLACKLIST.put("observables", "configuredParameters"); //Adaptation
//    }
    
    /**
     * Registers a new listener for the given list.
     * @param parentName The parent item, i.e., the list for which the listener belongs to.
     * @param listener The index-based editor, which shall be informed.
     */
    void add(String parentName, IContainerVariableEditorInputChangeListener listener) {
        List<IContainerVariableEditorInputChangeListener> list = listeners.get(parentName.toLowerCase());
        if (null == list) {
            list = new ArrayList<IContainerVariableEditorInputChangeListener>();
            listeners.put(parentName, list);
        }
        list.add(listener);
    }
    
    /**
     * Notifies all listeners for the given list, that an element was removed from the list.
     * @param parentName The parent item, i.e., the list where the change happened
     * @param index The index of the deleted element, all elements behind this index must move up.
     */
    public void notifyDeletetion(String parentName, int index) {
        String name = parentName;
//        if (BLACKLIST.containsKey(parentName)) {
//            name = BLACKLIST.get(parentName);
//        } else {
//        name = parentName.toLowerCase();
//        }
        
        List<IContainerVariableEditorInputChangeListener> list = listeners.get(name);
        if (null == list) {
            list = listeners.get(name.toLowerCase());
        }
        if (null != list) {
            for (int i = 0, end = list.size(); i < end; i++) {
                list.get(i).notifyDeletetion(name, index);
            }
        }
    }
}
