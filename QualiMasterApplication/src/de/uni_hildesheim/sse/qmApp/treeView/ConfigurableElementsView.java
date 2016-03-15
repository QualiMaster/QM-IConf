package de.uni_hildesheim.sse.qmApp.treeView;

import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
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

import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.commands.AbstractConfigurableHandler;
import de.uni_hildesheim.sse.qmApp.commands.InstantiateLocal;
import de.uni_hildesheim.sse.qmApp.dialogs.CloneNumberInputDialog;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator.CloneMode;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.ConnectorUtils;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.model.QualiMasterDisplayNameProvider;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelineEditorListener;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.IChangeListener;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import eu.qualimaster.easy.extension.QmConstants;
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
    private static HashMap<Image, Image> originalErrorIconReminder = new HashMap<Image, Image>();
    private static HashMap<Image, Image> originalIndicatorIconReminder = new HashMap<Image, Image>();
    
    private static ConfigurableElements elements = new ConfigurableElements();
    private static TreeViewer viewer;
    private boolean enableChangeEventProcessing = true;
    private MenuManager menuManager;
    private ConfigurableElementsDispatcher elementsDispatcher;

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
        elementsDispatcher.unregister();
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
                            ConfigurableElement parent = selectedElement.getParent();
                            if (parent.isVirtualSubGroup()) {
                                parent = parent.getParent();
                            }
                            for (int e = 0; e < elements.size(); e++) {
                                viewer.add(parent, elements.get(e));
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
                            
                            PipelineDiagramUtils.addPipelineColor();
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
   
        diagram.getEditingDomain().addResourceSetListener(new ResourceSetListenerImpl() {
            
            public void resourceSetChanged(ResourceSetChangeEvent event) {
            
                PipelineDiagramUtils.saveConnections();
                @SuppressWarnings("unused")
                PipelineEditorListener listener = new PipelineEditorListener(event);
            }
        });
    }
    
    /**
     * Opens an editor for <code>elt</code>.
     * 
     * @param elt the element to open the editor for (ignored if <b>null</b>)
     */
    private void openEditor(ConfigurableElement elt) {
        if (null != elt && null != elt.getEditorInputCreator()) { // no editor input creator -> virtual grouping
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
    class ViewLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

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

                if (originalIndicatorIconReminder.containsKey(image)) {
                    image = originalIndicatorIconReminder.get(image);
                }
                
                Image newImage = IconManager.addErrorToImage(image);
                originalErrorIconReminder.put(newImage, image);
                image = newImage;
                
            } else if (!elem.getFlawedIndicator()) {

                if (originalErrorIconReminder.containsKey(image)) {
                    image = originalErrorIconReminder.get(image);
                }
                
                //In order to set the icon for elements indicator.
                if (!elem.getDisplayName().equals("Runtime")) {
                    
                    ElementStatusIndicator indicator = elem.getStatus();
                    Image newImage = IconManager.addErrorToImage(image, indicator);
                    originalIndicatorIconReminder.put(newImage, image);
                    image = newImage;
                }
            }
            
            return image;
        }

        @Override
        public Color getForeground(Object element) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Color getBackground(Object element) {

            //Or use this to set the Background of a item
//            ConfigurableElement elem = (ConfigurableElement) element;
//            Color color;
//            
//            ElementsStatusIndicator indicator = elem.getDataflowInfo();
//
//            switch (indicator) {
//            case VERYHIGH:
//                //Image littleErrorImage = IconManager...
//                //image = IconManager.addErrorToImage(image);
//                color = IconManager.DARK_RED;
//                break;
//            case HIGH:
//                //image = IconManager.addErrorToImage(image);
//                color = IconManager.LIGHT_RED;
//                break;
//            case MEDIUM: 
//                //image = IconManager.addErrorToImage(image);
//                color = IconManager.YELLOW;
//                break;
//            case LOW:    
//                //image = IconManager.addErrorToImage(image);
//                color = IconManager.DARK_GREEN;
//                break;
//            case VERYLOW:   
//                color = IconManager.LIGHT_GREEN;
//                //image =  IconManager.addErrorToImage(image);
//                break;
//            default:
//                break;
//            }
            return null;
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
        
        elementsDispatcher = new ConfigurableElementsDispatcher(elements, viewer);
        elementsDispatcher.register();
        
        setTitleImage(IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL));
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
     * Force the viewer to refresh its input.
     */
    public static void forceTreeRefresh() {
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
        if (enableChangeEventProcessing 
            && variable.getParent() instanceof de.uni_hildesheim.sse.model.confModel.Configuration) {
            ConfigurableElement element = elements.findElement(variable);
            boolean done;
            if (null != element) {
                switch (kind) {
                case ADDED:
                    done = VariabilityModel.DISPLAY_ALGORITHMS_NESTED && updateHardwareMachine(variable, element);
                    if (!done) {
                        viewer.add(element.getParent(), element);
                    }
                    break;
                case CHANGED:
                    done = VariabilityModel.DISPLAY_ALGORITHMS_NESTED && (updateAlgorithmMembers(variable, element) 
                        || updateHardwareMachine(variable, element));
                    if (!done) {
                        element.setDisplayName(ModelAccess.getDisplayName(variable));
                        viewer.refresh(element, true);
                    }
                    break;
                case DELETING:
                    break; // do nothing
                case DELETED:
                    deleteNested(variable, element, QmConstants.TYPE_MACHINE, Configuration.HARDWARE);
                    deleteNested(variable, element, QmConstants.TYPE_FAMILY, Configuration.ALGORITHMS);
                    deleteNested(variable, element, QmConstants.TYPE_ALGORITHM, Configuration.ALGORITHMS);
                    viewer.remove(element); // configurable element removes itself
                    break;
                default:
                    break; // cannot handle, shall not occur
                }
            }
        }
    }
    
    /**
     * Adds a hardware machine into within its grouping.
     * 
     * @param variable the variable representing the machine
     * @param element the configurable element representing the machine
     * @return <code>true</code> if done, <code>false</code> else
     */
    private boolean updateHardwareMachine(IDecisionVariable variable, ConfigurableElement element) {
        boolean done = false;
        AbstractVariable decl = variable.getDeclaration();
        if (QmConstants.TYPE_MACHINE.equals(decl.getType().getName())) {
            String name = ModelAccess.getDisplayName(variable);
            String group = VariabilityModel.getHardwareGroup(name);
            if (null != group) {
                updateHardware(element, group, name);
            }
        }
        return done;
    }
    
    /**
     * Deletes a nested element.
     * 
     * @param variable the variable representing the element
     * @param element the configurable element to be deleted (representing <code>variable</code>)
     * @param type the name of the type of variable to be deleted (filter)
     * @param part the model part to work on
     * @return <code>true</code> if at least one configurable element was deleted, <code>false</code> else
     */
    private boolean deleteNested(IDecisionVariable variable, ConfigurableElement element, String type, 
        IModelPart part) {
        boolean done = false;
        AbstractVariable decl = variable.getDeclaration();
        if (type.equals(decl.getType().getName())) {
            String displayPart = QualiMasterDisplayNameProvider.INSTANCE.getModelPartDisplayName(part);
            ConfigurableElement parent = getByName(displayPart);
            if (null != parent) {
                for (int c = 0; c < parent.getChildCount(); c++) {
                    ConfigurableElement child = parent.getChild(c);
                    if (child.deleteFromChildren(element)) {
                        viewer.remove(new TreePath(new Object[] {parent, child, element}));
                        done = true;
                    }
                }
            }
        }
        return done;
    }
        
    /**
     * Adds a hardware machine into its grouping.
     * 
     * @param element the configurable element representing the machine
     * @param group the group name (must not be <b>null</b>)
     * @param name the machine name 
     * @return <code>true</code> if done, <code>false</code> else
     */
    private boolean updateHardware(ConfigurableElement element, String group, String name) {
        boolean done = false;
        String hwDisplayPart = QualiMasterDisplayNameProvider.INSTANCE.getModelPartDisplayName(
            Configuration.HARDWARE);
        ConfigurableElement hwParent = getByName(hwDisplayPart);
        if (null != hwParent) {
            ConfigurableElement groupElement = null;
            for (int c = 0; c < hwParent.getChildCount(); c++) {
                ConfigurableElement grp = hwParent.getChild(c);
                String grpName = grp.getDisplayName();
                ConfigurableElement tmp = getByName(grp, name);
                if (name.equals(grpName)) {
                    // if grouped and still in general ungrouped list (children if hwParent) - remove from ungrouped
                    hwParent.deleteFromChildren(element);
                    viewer.refresh(hwParent, true);
                } else if (group.equals(grpName)) {
                    // if group found and not already in group, add
                    groupElement = grp;
                    if (null == tmp) {
                        grp.addChild(element);
                        viewer.add(grp, element);
                        done = true;
                    }
                } else {
                    // if it's not the right group but stored within, remove as name changed
                    if (null != tmp) {
                        grp.deleteFromChildren(tmp);
                        viewer.refresh(grp, true);
                        done = true;
                    }
                }
            }
            if (null == groupElement) {
                // if there is no group, create one and add it
                groupElement = new ConfigurableElement(group, null, null, Configuration.HARDWARE);
                groupElement.setImage(ImageRegistry.INSTANCE.getImage(Configuration.HARDWARE));
                hwParent.addChild(groupElement);
                groupElement.addChild(element);
                viewer.add(groupElement, element);
                viewer.add(hwParent, groupElement);
            }
        }
        return done;
    }

    /**
     * Returns a configurable child by its name.
     * 
     * @param element the element for return the child for
     * @param name the name of the element
     * @return the element or <b>null</b> if not found
     */
    private static ConfigurableElement getByName(ConfigurableElement element, String name) {
        ConfigurableElement result = null;
        for (int c = 0; null == result && c < element.getChildCount(); c++) {
            ConfigurableElement tmp = element.getChild(c);
            if (name.equals(tmp.getDisplayName())) {
                result = tmp;
            }
        }
        return result;
    }
    
    /**
     * Returns a top-level configurable element by its name.
     * 
     * @param name the name of the element
     * @return the element or <b>null</b> if not found
     */
    private static ConfigurableElement getByName(String name) {
        ConfigurableElement result = null;
        if (null != name) {
            for (int e = 0; null == result && e < elements.getElementsCount(); e++) {
                ConfigurableElement tmp = elements.getElement(e);
                if (name.equals(tmp.getDisplayName())) {
                    result = tmp;
                }
            }
        }
        return result;
    }

    /**
     * Updates the nested algorithm members of a family.
     * 
     * @param family the variable (supposed to be a changed family, ignored if not)
     * @param parent the parent
     * @return <code>true</code> if done, <code>false</code> else
     */
    private boolean updateAlgorithmMembers(IDecisionVariable family, ConfigurableElement parent) {
        boolean done = false;
        AbstractVariable decl = family.getDeclaration();
        if (QmConstants.TYPE_FAMILY.equals(decl.getType().getName())) {
            String algDisplayPart = QualiMasterDisplayNameProvider.INSTANCE.getModelPartDisplayName(
                Configuration.ALGORITHMS);
            ConfigurableElement algParent = getByName(algDisplayPart);
            if (null != algParent) {
                algParent = algParent.findElement(family);
            }
            IDecisionVariable algs = family.getNestedElement(QmConstants.SLOT_FAMILY_MEMBERS);
            if (null != algParent && algs instanceof ContainerVariable) {
                Set<ConfigurableElement> known = new HashSet<ConfigurableElement>();
                for (int c = 0; c < algParent.getChildCount(); c++) {
                    known.add(algParent.getChild(c));
                }
                for (int n = 0; n < algs.getNestedElementsCount(); n++) {
                    IDecisionVariable nested = VariabilityModel.dereference(algs.getNestedElement(n));
                    ConfigurableElement elt = algParent.findElement(nested);
                    if (null == elt) {
                        ConfigurableElements.variableToConfigurableElements(Configuration.ALGORITHMS, 
                            nested.getDeclaration().getName(), nested, algParent, 
                            Configuration.ALGORITHMS.getElementFactory(), null);
                    } else if (!known.contains(elt)) {
                        algParent.addChild(elt);
                    } else {
                        known.remove(elt);
                    }
                }
                for (ConfigurableElement elt : known) {
                    algParent.deleteFromChildren(elt);
                }
                viewer.refresh(algParent, true);
                done = true;
            }
        } 
        return done;
    }

    /**
     * Show errors in TreeViewer.
     * @param configurableElementsViewMapping info about flawed elements.
     */
    public static void saveReasosiningInfoInTreeElements(Set<String> configurableElementsViewMapping) {

        for (int i = 0; i < elements.elements().length; i++) {

            ConfigurableElement treeElement = elements.elements()[i];

            
            traverseTree(treeElement, configurableElementsViewMapping);
            
            
        }
    }

    /**
     * Visit all treeitems.
     * @param element one top-level parent
     * @param configurableElementsViewMapping list of items to search for.
     */
    private static void traverseTree(ConfigurableElement element, Set<String> configurableElementsViewMapping) {
        
        for (int k = 0; k < element.getChildCount(); k++) {
            
            ConfigurableElement innerTreeElement = element.getChild(k);

            String innerTreeElementName = innerTreeElement.toString();
            innerTreeElementName = innerTreeElementName.replaceAll("[^a-zA-Z0-9]", "");
            if (configurableElementsViewMapping.contains(innerTreeElementName)) {
                innerTreeElement.setFlawedIndicator(true);
                viewer.refresh(innerTreeElement);
            } else {

                innerTreeElement.setFlawedIndicator(false);
                viewer.refresh(innerTreeElement);
            }
            
            traverseTree(innerTreeElement, configurableElementsViewMapping);
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