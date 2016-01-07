package de.uni_hildesheim.sse.qmApp.treeView;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.transaction.ResourceSetChangeEvent;
import org.eclipse.emf.transaction.ResourceSetListenerImpl;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.commands.AbstractConfigurableHandler;
import de.uni_hildesheim.sse.qmApp.commands.InstantiateLocal;
import de.uni_hildesheim.sse.qmApp.dialogs.CloneNumberInputDialog;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator.CloneMode;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.ConnectorUtils;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelineEditorListener;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.IChangeListener;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import pipeline.diagram.part.PipelineDiagramEditor;

/**
 * The View is responsible for the left part in the final Qualimaster-Application. A {@link TreeViewer} gets populated
 * with {@link ConfigurableElement}s. This class provides several methods to manipulate these elements/ arrays. Thus it
 * is possible to modify the treeviewer while the app is running. Therefore we just re - populate the treeviewer and
 * refresh the view.
 * 
 * @author Niko Nowatzki
 */
public class ConfigurableElementsView extends ViewPart implements IChangeListener {
    public static final String ID = "QualiMasterApplication.view";
    private static final boolean DIAGRAM_STATUS_LISTENER = true;
    //private static final String NODE_IDENTIFIER = "name: nodes";
    //private static final String FLOW_IDENTIFIER = "name: nodes";
    private static HashMap<Image, Image> originalIconReminder = new HashMap<Image, Image>();
    private static ConfigurableElements elements = new ConfigurableElements();
    private static TreeViewer viewer;
    private boolean enableChangeEventProcessing = true;
    private MenuManager menuManager;
    //private IPipelineEditorListener listener;
    /**
     * Mapping for icons in the {@link ConfigurableElementsView}. The original-image is not annotated.
     * The errorImage is marked with a little image indicating an error.
     *
     * @author Niko
     */
    public static class TreeImageReminder {
        private Image original;
        private Image errorImage;
        
        /**
         * Constructor.
         * @param original The original image.
         * @param errorImage The annotated error-image.
         */
        public TreeImageReminder(Image original, Image errorImage) {
            this.original = original;
            this.errorImage = errorImage;
        }
        
        /**
         * Get the original image.
         * @param errorImage Given errorImage which is annotated.
         * @return toReturn the original image.
         */
        public Image getOriginal(Image errorImage) {
            Image toReturn = null;
            if (errorImage.equals(this.errorImage)) {
                toReturn = original;
            }
            return toReturn;
        }
    } 
    
    /**
     * Implements a menu listener for handling the popup menu.
     * 
     * @author Niko Nowatzki
     */
    private final class MenuListener implements IMenuListener {

        /**
         * Creates a menu listener.
         */
        private MenuListener() {
        }

        @Override
        public void menuAboutToShow(IMenuManager manager) {
            // Get the users current selection in tree
            ISelection selection = viewer.getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;
                if (structuredSelection.getFirstElement() instanceof ConfigurableElement) {
                    ConfigurableElement selectedElement = (ConfigurableElement) structuredSelection.getFirstElement();
                    // top level
                    insertAddAction(manager, selectedElement);
                    //insertValidationAction(manager, selectedElement); // global validation is sufficient
                    selectedElement.contributeToPopup(manager);
                    // entry level
                    insertDeleteAction(manager, selectedElement);
                    insertCloneAction(manager, selectedElement);
                }
            }
        }
    }

    @Override
    public void dispose() {
        if (null != menuManager) {
            menuManager.dispose();
        }
        ChangeManager.INSTANCE.removeListener(this);
        super.dispose();
    }

    /**
     * Inserts a clone action for <code>selectedElement</code> into <code>manager</code>.
     * 
     * @param manager
     *            the menu manager to be modified as a side effect
     * @param selectedElement
     *            the selected element
     */
    private void insertCloneAction(IMenuManager manager, final ConfigurableElement selectedElement) {
        final CloneMode mode = selectedElement.isCloneable();
        if (!selectedElement.isTopLevel() && mode.cloneAllowed()) {
            Action cloneAction = new Action() {
                @Override
                public void run() {
                    int cloneCount = 1;
                    if (mode.requiresCountInput()) {
                        CloneNumberInputDialog dlg = new CloneNumberInputDialog(PlatformUI.getWorkbench().getDisplay()
                            .getActiveShell());
                        int dlgResult = dlg.open();
                        if (Window.OK == dlgResult) {
                            cloneCount = dlg.getCloneCount();
                        } else {
                            cloneCount = -1;
                        }
                    }
                    if (cloneCount > 0) {
                        // clone does the event notifications, notify others via ChangeManagement, avoid self-messages
                        List<ConfigurableElement> elements = selectedElement.clone(ConfigurableElementsView.this,
                                cloneCount);
                        if (null != elements) {
                            for (int e = 0; e < elements.size(); e++) {
                                viewer.add(selectedElement.getParent(), elements.get(e));
                            }
                        }
                    }
                }
            };

            String suffix = "";
            if (mode.requiresCountInput()) {
                suffix = " ...";
            }
            
            cloneAction.setText("Clone '" + selectedElement.getDisplayName() + "'" + suffix);
            manager.add(cloneAction);
        }
    }

    /**
     * Inserts a delete element action for <code>selectedElement</code> into <code>manager</code>.
     * 
     * @param manager
     *            the menu manager to be modified as a side effect
     * @param selectedElement
     *            the selected element
     */
    private void insertDeleteAction(IMenuManager manager, final ConfigurableElement selectedElement) {
        if (!selectedElement.isTopLevel()) {
            Action deleteAction = new Action() {
                @Override
                public void run() {
                    if (selectedElement.isReferencedIn(VariabilityModel.Configuration.INFRASTRUCTURE)) {
                        String name = selectedElement.getDisplayName();
                        Dialogs.showInfoDialog("Cannot delete '" + name + "'", "'" + name
                                + "' cannot be deleted as it is referenced by other parts of the model.");
                    } else {
                        // delete element, notify others via ChangeManager, avoid self-messages
                        closeEditor(selectedElement, false);
                        selectedElement.delete(ConfigurableElementsView.this);                     
                        // to not set data again/refresh - this collapses tree
                        viewer.remove(selectedElement);
                    }
                }
            };
            deleteAction.setText("Delete '" + selectedElement.getDisplayName() + "'");
            //deleteAction.setEnabled(!ConfigurationProperties.DEMO_MODE.getBooleanValue());
            manager.add(deleteAction);
        }
    }

    // global validation is sufficient
    /*
     * Inserts a validation action for <code>selectedElement</code> into <code>manager</code>.
     * 
     * @param manager
     *            the menu manager to be modified as a side effect
     * @param selectedElement
     *            the selected element
     */
    /*private void insertValidationAction(IMenuManager manager, final ConfigurableElement selectedElement) {
        if (selectedElement.isTopLevel()) {
            Action action = new Action() {
                @Override
                public void run() {
                    Reasoning.reasonOn(selectedElement.getModelPart(), true);
                }
            };
            action.setEnabled(Reasoning.ENABLED);
            action.setText("Validate '" + selectedElement.getDisplayName() + "'");
            manager.add(action);
        }
    }*/

    /**
     * Inserts an add element action for <code>selectedElement</code> into <code>manager</code>.
     * 
     * @param manager
     *            the menu manager to be modified as a side effect
     * @param selectedElement
     *            the selected element
     */
    private void insertAddAction(IMenuManager manager, final ConfigurableElement selectedElement) {
        if (selectedElement.isTopLevel()) {
            IModelPart modelPart = selectedElement.getModelPart();
            IDatatype[] providedTypes = modelPart.getProvidedTypes();
            if (null != providedTypes) {
                for (IDatatype providedType : modelPart.getProvidedTypes()) {
                    final IDatatype type = providedType;
                    Action addAction = new Action() {
                        @Override
                        public void run() {
                            IModelPart modelPart = selectedElement.getModelPart();
                            IDecisionVariable variable;
                            if (modelPart.addOnCreation()) {
                                variable = ModelAccess.addNewElement(modelPart, type);
                            } else {
                                variable = ModelAccess.createNewElement(modelPart, type, 
                                    Configuration.PIPELINES == modelPart);
                            }
                            if (null != variable) {
                                // notify others, avoid self-messages
                                ConfigurableElement newElt = selectedElement.addChild(ConfigurableElementsView.this,
                                    variable);
                                if (null != newElt) {
                                    viewer.add(selectedElement, newElt);
                                    openEditor(newElt);
                                }
                            }
                        }
                    };
                    addAction.setText("Add " + type.getName() + " to '" + selectedElement.getDisplayName() + "'");
                    manager.add(addAction);
                }
            }
        }
    }

    /**
     * Implements a double click listener for the part control.
     * 
     * @author Niko Nowatzki
     */
    private final class DoubleClickListener implements IDoubleClickListener {

        @Override
        public void doubleClick(DoubleClickEvent event) {
            if (event.getSelection() instanceof TreeSelection) {
                TreeSelection tSel = (TreeSelection) event.getSelection();
                if (tSel.getFirstElement() instanceof ConfigurableElement) {
                    openEditor((ConfigurableElement) tSel.getFirstElement());
                    
                    if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()
                            instanceof DiagramEditor) {
                    
                        DiagramEditor diagram = (DiagramEditor) PlatformUI.getWorkbench().
                                getActiveWorkbenchWindow().getActivePage().getActiveEditor();
                               
                        if (DIAGRAM_STATUS_LISTENER) {
                            listenOnDiagrm(diagram);
                        }
                        
                        if (diagram instanceof PipelineDiagramEditor) {
                            PipelineDiagramUtils.highlightDiagram();
                        }
                        
                       
                    }
                }
            }
        }
    }
    
    
    /**
     * Listen on changes in the emf-based model.
     * @param diagram Diagram which we listen to.
     */
    private void listenOnDiagrm(DiagramEditor diagram) {
   
//        listener = new IPipelineEditorListener() {
//            @Override
//            public void nodeRemoved(String name) {
//                // TODO Auto-generated method stub
//            }
//            @Override
//            public void nodeAdded(String name) {
//                // TODO Auto-generated method stub
//            }
//            @Override
//            public void flowRemoved(String node1, String node2) {
//                // TODO Auto-generated method stub
//            }
//            @Override
//            public void flowAdded(String node1, String node2) {
//                // TODO Auto-generated method stub
//            }
//        };  
        diagram.getEditingDomain().addResourceSetListener(new ResourceSetListenerImpl() {
            
            public void resourceSetChanged(ResourceSetChangeEvent event) {
            
                @SuppressWarnings("unused")
                PipelineEditorListener listener = new PipelineEditorListener(event);
//            
//                for (Iterator<?> iter = event.getNotifications().iterator(); iter.hasNext();) {
//                    Notification notification = (Notification) iter.next();
//                    Object notifier = notification.getNotifier();
//                    
//                    if (notifier instanceof EObject) {
//                        EObject eObject = (EObject) notifier;
//                
//                        EStructuralFeature feature = (EStructuralFeature) notification.getFeature();
//                        
//                        // only respond to changes to structural features of the object
//                        if (feature instanceof EStructuralFeature) {
//                            if (notification.getFeature().toString().contains(NODE_IDENTIFIER)) {       
//                                if (Integer.compare(notification.getEventType(), Notification.REMOVE) == 0) {
//                                    //listener.nodeRemoved(feature.getName());
//                                    System.out.println("node removed");
//                                }
//                                if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {
//                                    //listener.nodeAdded(feature.getName());
//                                    System.out.println("node added");
//                                }
//                            }
//                            if (notification.getFeature().toString().contains(FLOW_IDENTIFIER)) {
//                                if (Integer.compare(notification.getEventType(), Notification.ADD) == 0) {
//                                    //add nodes which are connected to the newly added flow to the listener
//                                    //listener.flowAdded();
//                                    System.out.println("flow added");
//                                }
//                            }
//                            if (eObject instanceof ConnectorImpl && Integer.compare(notification.getEventType(),
//                                     Notification.UNSET) == 0) {
//                                    //listener.flowRemoved();
//                                    System.out.println("Flow removed");
//                            }
//                            // get the name of the changed feature and the qualified name of
//                            //    the object, substituting <type> for any element that has no name
//                            System.out.println("The " + feature.getName() + " of the object \""
//                                    + EMFCoreUtil.getQualifiedName(eObject, true) + "\" has changed.");
//                        }
//                    }
//                }
            }
        });
    }
    /**
     * Opens an editor for <code>elt</code>.
     * 
     * @param elt the element to open the editor for (ignored if <b>null</b>)
     */
    private void openEditor(ConfigurableElement elt) {
        if (null != elt) {
            IEditorInput input = elt.getEditorInputCreator().create();
            String editorId = elt.getEditorId();
            if (null != input && null != editorId) {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IEditorReference foundRef = null;
                for (IEditorReference ref : page.getEditorReferences()) {
                    if (ref.getId().equals(editorId) && elt.getDisplayName().equals(ref.getName())) {
                        foundRef = ref;
                        break;
                    }
                }
                if (null == foundRef) {
                    try {
                        page.openEditor(input, editorId);
                    } catch (PartInitException e) {
                        Dialogs.showErrorDialog(getSite().getShell(), "Opening editor", getClass(), e);
                    }
                } else {
                    foundRef.getPage().bringToTop(foundRef.getPart(true));
                }
            }
        }
    }
    
    /**
     * Closes the editor for <code>elt</code> if it is actually open.
     * 
     * @param elt the element to close the editor for
     * @param confirmSave whether the user shall have the opportunity to save changes
     */
    private void closeEditor(ConfigurableElement elt, boolean confirmSave) {
        if (null != elt) {
            String editorId = elt.getEditorId();
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            for (IEditorReference ref : page.getEditorReferences()) {
                if (ref.getId().equals(editorId) && elt.getDisplayName().equals(ref.getName())) {
                    IEditorPart part = ref.getEditor(false);
                    if (null != part) {
                        page.closeEditor(part, confirmSave);
                    }
                }
            }
        }
    }
  
    /**
     * The content provider class is responsible for providing objects to the view. It can wrap existing objects in
     * adapters or simply return objects as-is. These objects may be sensitive to the current input of the view, or
     * ignore it and always show the same content (like Task List, for example).
     * 
     * @author Niko Nowatzki
     */
    class ViewContentProvider implements ITreeContentProvider {

        /**
         * Change input.
         * 
         * @param viewer
         *            The {@link TreeViewer} object.
         * @param oldInput
         *            The old input.
         * @param newInput
         *            new Input.
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {

        }

        /**
         * Dispose.
         */
        public void dispose() {
        }

        /**
         * Get the elements {@link ConfigurableElement}.
         * 
         * @param parent
         *            The elements parent.
         * @return elements The elements under the given parent.
         */
        public Object[] getElements(Object parent) {
            Object[] elements;
            if (parent instanceof Object[]) {
                elements = (Object[]) parent;
            } else {
                elements = new Object[0];
            }
            return elements;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            ConfigurableElement elt = ConfigurableElement.asConfigurableElement(parentElement);
            return null != elt ? elt.getChildren() : null;
        }

        @Override
        public Object getParent(Object element) {
            ConfigurableElement elt = ConfigurableElement.asConfigurableElement(element);
            return null != elt ? elt.getParent() : null;
        }

        @Override
        public boolean hasChildren(Object element) {
            ConfigurableElement elt = ConfigurableElement.asConfigurableElement(element);
            return null != elt ? elt.hasChildren() : false;
        }
    }

    /**
     * The {@link ViewLabelProvider} provides methods which can be used to access resources just like texts, images for
     * given objects.
     * 
     * @author Niko Nowatzki
     */
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * Get the text for the given object.
         * 
         * @param obj
         *            The {@link Object} which holds a text.
         * @param index
         *            The index.
         * @return The objects text.
         */
        public String getColumnText(Object obj, int index) {
            return getText(obj);
        }

        /**
         * Get the image for the given object and index.
         * 
         * @param obj
         *            The {@link Object} which holds an image.
         * @param index
         *            The index.
         * @return The objects image.
         */
        public Image getColumnImage(Object obj, int index) {
            return getImage(obj);
        }


        /**
         * Get the image for the given object.
         * 
         * @param obj
         *            The {@link Object} which holds an image..
         * @return The objects image.
         */
        public Image getImage(Object obj) {
            ConfigurableElement elem = (ConfigurableElement) obj;
            Image image;
            
            //If one config element is not readable or writable, their respective icons get filtered
            if (!elem.isReadable() || !elem.isWritable()) {
                image = IconManager.filterImage(elem.getImage());
            } else {
                image = elem.getImage();
            }
            
            if (null == image) {
                image = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
            }
           
            if (elem.getFlawedIndicator()) {
                //Check whether item is flawed. If true, annotate the corresponding icon with an error-marker.

                Image newImage = IconManager.addErrorToImage(image);
                originalIconReminder.put(newImage, image);
                image = newImage;
            } 
            if (!elem.getFlawedIndicator()) {

                if (originalIconReminder.containsKey(image)) {
                    image = originalIconReminder.get(image);
                }
            }
            return image;
        }
    }
    
    /**
     * Initializes the commands according to the user roles.
     */
    private static final void initializeCommands() {
        boolean isAdmin = UserContext.INSTANCE.isAdmin();
        boolean isInfrastructureAdmin = UserContext.INSTANCE.isInfrastructureAdmin();
        AbstractConfigurableHandler.setEnabled(InstantiateLocal.class, isAdmin || isInfrastructureAdmin);
    }
    
    @Override
    public void createPartControl(Composite parent) {
        ModelAccess.initialize();
        VariabilityModel.initializeImages();
        VariabilityModel.registerEditors();
        initializeCommands();
        createElements();
        ChangeManager.INSTANCE.addListener(this);
        ConnectorUtils.configure();

        viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

        viewer.setContentProvider(new ViewContentProvider());
        viewer.setLabelProvider(new ViewLabelProvider());
        // Provide the input to the ContentProvider
        viewer.setInput(null);
        viewer.setInput(elements.elements());
        viewer.refresh();

        viewer.addDoubleClickListener(new DoubleClickListener());

        // Add Menu to elements
        menuManager = new MenuManager();
        menuManager.setRemoveAllWhenShown(true);
        final Tree tree = viewer.getTree();
        
        menuManager.addMenuListener(new MenuListener());
        
        Menu contextMenu = menuManager.createContextMenu(tree);
        tree.setMenu(contextMenu);
    }

    /**
     * (Re)Creates the tree of configurable elements from the underlying variability model / configuration.
     */
    private void createElements() {
        elements.clear();
        VariabilityModel.createConfigurationElements(elements);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * Force the viewer to referesh its input.
     */
    public void forceTreeRefresh() {
        viewer.refresh();
    }

    /**
     * Get the elements.
     * 
     * @return elements containing configurable elements.
     */
    public static ConfigurableElement[] getElements() {
        return elements.elements();
    }
   
    @Override
    public void variableChanged(EventKind kind, IDecisionVariable variable, int globalIndex) {
        if (enableChangeEventProcessing) {
            ConfigurableElement element = elements.findElement(variable);
            if (null != element) {
                switch (kind) {
                case ADDED:
                    viewer.add(element.getParent(), element);
                    break;
                case CHANGED:
                    element.setDisplayName(ModelAccess.getDisplayName(variable));
                    viewer.refresh(element, true);
                    break;
                case DELETING:
                    break; // do nothing
                case DELETED:
                    viewer.remove(element);
                    break;
                default:
                    break; // cannot handle, shall not occur
                }
            }
        }
    }

    /**
     * Show errors in TreeViewer.
     * @param configurableElementsViewMapping info about falwed elements.
     */
    public static void saveReasosiningInfoInTreeElements(Set<String> configurableElementsViewMapping) {

        for (int i = 0; i < elements.elements().length; i++) {

            ConfigurableElement treeElement = elements.elements()[i];

            for (int k = 0; k < treeElement.getChildCount(); k++) {
                
                ConfigurableElement innerTreeElement = treeElement.getChild(k);

                String innerTreeElementName = innerTreeElement.getDisplayName();
                innerTreeElementName = innerTreeElementName.replaceAll("[^a-zA-Z0-9]", "");
                if (configurableElementsViewMapping.contains(innerTreeElementName)) {
                    innerTreeElement.setFlawedIndicator(true);
                    viewer.refresh(innerTreeElement);
                } else {

                    innerTreeElement.setFlawedIndicator(false);
                    viewer.refresh(innerTreeElement);
                }
            }
        }
    }

    /**
     * Change all icons to standard with no red markers.
     */
    public static void revertConfigurableElementsViewMarking() {
        for (int i = 0; i < elements.elements().length; i++) {

            ConfigurableElement treeElement = elements.elements()[i];

            for (int k = 0; k < treeElement.getChildCount(); k++) {
                
                ConfigurableElement innerTreeElement = treeElement.getChild(k);
                innerTreeElement.setFlawedIndicator(false);
                viewer.refresh();
            }
        }
    }
    
    /**
     * Show errors for Pipelines in Treeviewer.
     * @param configurableElementsViewMappingForPipelines pipelines-errors.
     */
    public static void saveReasosiningInfoInTreeElementsForPipelines(
        Set<String> configurableElementsViewMappingForPipelines) {

        for (int i = 0; i < elements.elements().length; i++) {

            ConfigurableElement treeElement = elements.elements()[i];

            if (treeElement.getDisplayName().equals("Pipelines")) {

                for (int k = 0; k < treeElement.getChildCount(); k++) {
                    ConfigurableElement innerTreeElement = treeElement.getChild(k);


                    if (configurableElementsViewMappingForPipelines.contains(innerTreeElement.getDisplayName())) {
                        innerTreeElement.setFlawedIndicator(true);
                        viewer.refresh(innerTreeElement);
                    } else {

                        innerTreeElement.setFlawedIndicator(false);
                        viewer.refresh(innerTreeElement);
                    }
                }
            }
        }
    }
}