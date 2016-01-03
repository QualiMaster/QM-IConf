package de.uni_hildesheim.sse.qmApp.dialogs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import de.uni_hildesheim.sse.easy_producer.observer.EclipseProgressObserver;
import de.uni_hildesheim.sse.qmApp.editors.ITextUpdater;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.repositoryConnector.maven.MavenFetcher;
import de.uni_hildesheim.sse.repositoryConnector.maven.MavenFetcher.TreeElement;

/**
 * This editor allows the user to select a maven-artifact directory out of the
 * presented tree-structure.
 * 
 * @author Niko
 */
public class MavenArtifactEditor extends Dialog {

    private static final long ACTUAL_TREE_TIME_DIFF = 12 * 60 * 60 * 1000; // 12h in ms
    private static final String REGEX = "^\\d+(\\.\\d+)+(\\-([a-zA-Z])*){0,1}?$";
    private List<TreeElement> mavenList = new ArrayList<TreeElement>();
    private Composite treeContainer;
    private Text groupIDText;
    private Text artifactIDText;
    private Text versionText;
    private String groupIDToReturn = "";
    private TreeViewer viewer;
    private List<String> treePathString = new ArrayList<String>();
    private ITextUpdater artifactEditorUpdater;

    private String[] initialTreePath;
    private String initialGroupId;
    private String initialArtifactId;
    private String initialVersion;
    
    /**
     * Constructor.
     * 
     * @param parentShell the parent shell.
     * @param artifactEditorUpdater updates the textfield.
     *
     */
    public MavenArtifactEditor(Shell parentShell, ITextUpdater artifactEditorUpdater) {
        super(parentShell);
        this.artifactEditorUpdater = artifactEditorUpdater;
    }
    
    /**
     * Returns whether the Maven artifact editor is configured.
     * 
     * @return <code>true</code> if configured, <code>false</code> else
     */
    public static boolean isConfigured() {
        return MavenFetcher.isConfigured();
    }
 
    /**
     * Run.
     */
    public void run() {
        setBlockOnOpen(true);
        open();
        Display.getCurrent().dispose();
    }

    /**
     * Fill the list with all maven artifacts.
     * 
     * @param monitor the actual progress monitor
     */
    private void collectMavenArtifactsOnline(IProgressMonitor monitor) {
        EclipseProgressObserver obs = new EclipseProgressObserver();
        obs.register(monitor);
        mavenList = MavenFetcher.getElementTree(obs);
        obs.unregister(monitor);
    }

    /**
     * Operation for the ProgressDialog.
     * 
     * @author Niko
     */
    class ProgressDialogOperation implements IRunnableWithProgress {
        @Override
        public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
            // time consuming work here
            collectMavenArtifactsOnline(monitor);
            monitor.done();
        }
    }

    /**
     * Create progress Dialog.
     * 
     * @param parent
     *            parent composite.
     */
    private void createProgressDialog(Composite parent) {
        try {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(parent.getShell()) {

                @Override
                protected void setShellStyle(int newShellStyle) {
                    super.setShellStyle(SWT.CLOSE | SWT.INDETERMINATE
                            | SWT.BORDER | SWT.TITLE);
                    setBlockOnOpen(false);
                }
            };
            pmd.run(true, true, new ProgressDialogOperation());
        } catch (final InvocationTargetException e) {
            MessageDialog.openError(parent.getShell(), "Error", e.getMessage());
        } catch (final InterruptedException e) {
            MessageDialog.openInformation(parent.getShell(), "Cancelled",
                    e.getMessage());
        }
    }

    /**
     * Create the Contents.
     * 
     * @param parent
     *            parent composite.
     * @return composite The parent composite.
     */
    protected Control createContents(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        Image icon = IconManager.retrieveImage(IconManager.MAVEN_DIALOG_ICON);
        composite.getShell().setImage(icon);
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 5;
        fillLayout.marginWidth = 5;
        composite.setLayout(fillLayout);
        // outer composite
        Composite outer = new Composite(composite, SWT.BORDER);
        FormLayout formLayout = new FormLayout();
        formLayout.marginHeight = 5;
        formLayout.marginWidth = 5;
        formLayout.marginBottom = 5;
        formLayout.spacing = 5;
        outer.setLayout(formLayout);

        // Composite for the treeviewer
        treeContainer = new Composite(outer, SWT.BORDER);
        treeContainer.setLayout(new GridLayout(1, true));
        FormData fData = new FormData();
        fData.top = new FormAttachment(0);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(100);
        fData.bottom = new FormAttachment(65); // Locks on 65% of the view
        fData.width = 450;
        fData.height = 500;
        treeContainer.setLayoutData(fData);

        Composite labelsContainer = new Composite(outer, SWT.BORDER);
        GridLayout innerRightLayout = new GridLayout();
        innerRightLayout.numColumns = 1;
        labelsContainer.setLayout(innerRightLayout);
        fData = new FormData();
        fData.top = new FormAttachment(treeContainer);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(100);
        fData.bottom = new FormAttachment(93); // Locks on 93% of the view
        labelsContainer.setLayoutData(fData);

        Composite buttonsContainer = new Composite(outer, SWT.BORDER);
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginLeft = 270; // Positioning of the Buttons
        buttonsContainer.setLayout(rowLayout);
        fData = new FormData();
        fData.top = new FormAttachment(labelsContainer);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(100);
        fData.bottom = new FormAttachment(100);
        buttonsContainer.setLayoutData(fData);
        createUserInterface(labelsContainer);
        createButtons(buttonsContainer);
        createTreeViewer(parent);
        setViewerInput(parent);

        composite.addListener(SWT.Traverse, new Listener() {

            public void handleEvent(Event evt) {
                if (evt.detail == SWT.TRAVERSE_ESCAPE) {
                    composite.getShell().close();
                }
            }
        });
        return composite;
    };

    /**
     * Set the input for the viewer.
     * 
     * @param parent
     *            The viewers parent.
     */
    private void setViewerInput(Composite parent) {
        // if online
        boolean openLocal = false;
        if (MavenFetcher.checkRepositoryConnectivity()) {
            File file = getTreeFile();
            if (!file.exists() || System.currentTimeMillis() - file.lastModified() > ACTUAL_TREE_TIME_DIFF) {
                createProgressDialog(parent);
                createTreeViewer(treeContainer); // mavenlist is filled and input is set
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        viewer.setInput(mavenList);
                        if (null != initialTreePath) {
                            highlightTreePath(initialTreePath);
                        }
                    }
                });
                saveTreeLocally();
                openLocal = false;
            } else {
                openLocal = true;
            }
        } 
        if (openLocal) {
            // open error dialog
            createTreeViewer(treeContainer);
            loadTreeLocally();
            if (null != initialTreePath) {
                highlightTreePath(initialTreePath);
            }
        }
    }

    @Override
    protected boolean isResizable() {
        return false;
    }

    /**
     * Save the List of {@link TreeItem}. If the user is offline, the tree can
     * be loaded from the saved file.
     */
    private void saveTreeLocally() {
        try {
            FileOutputStream fileoutputstream = new FileOutputStream(getTreeFile());
            ObjectOutputStream outputstream = new ObjectOutputStream(fileoutputstream);
            outputstream.writeObject(mavenList);
            outputstream.close();
        } catch (FileNotFoundException e) {
            Dialogs.showErrorDialog("Error storing the Maven artifacts tree cache", e.getMessage());
        } catch (IOException e) {
            Dialogs.showErrorDialog("Error storing the Maven artifacts tree cache", e.getMessage());
        }
    }

    /**
     * Load the serialized tree. Use the extracted list to set the treeviewers
     * input.
     */
    @SuppressWarnings("unchecked")
    private void loadTreeLocally() {
        try {
            FileInputStream fileIn = new FileInputStream(getTreeFile());
            ObjectInputStream in = new ObjectInputStream(fileIn);
            mavenList = (ArrayList<TreeElement>) in.readObject();
            in.close();
            fileIn.close();
            viewer.setInput(mavenList);
        } catch (IOException | ClassNotFoundException e) {
            Dialogs.showErrorDialog("Error loading the Maven artifacts tree cache", e.getMessage());
        }
        viewer.refresh();
    }
    
    /**
     * Returns the file for storing the Maven tree persistently (offline use).
     * 
     * @return the file
     */
    private File getTreeFile() {
        String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
        return new File(workspace + "/.metadata/MavenTreeSaved.ser");
    }

    /**
     * Creates the {@link TreeViewer}.
     * 
     * @param treeContainer
     *            Composite for the {@link TreeViewer}.
     */
    private void createTreeViewer(Composite treeContainer) {

        viewer = new TreeViewer(treeContainer, SWT.NONE);

        viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        viewer.setContentProvider(new MyTreeContentProvider());

        viewer.setLabelProvider(new MyTreeLabelProvider());

        viewer.getTree().addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                TreeItem[] selection = viewer.getTree().getSelection();

                // cut of the "\"
                String selectionNew = selection[0].getText();

                // If name of the TreeItem has numbers separated by dots. Starts
                // and ends with a number
                // at least one dot

                if (selectionNew.matches(REGEX)) {

                    // Fill textfields
                    String groupID = "";
                    String artifactID = "";
                    String version = "";

                    // version is actually the selected items name
                    version = selectionNew;

                    // Get the parent item of the selected item. Thats the
                    // artifactID.
                    TreeItem parentItem = selection[0].getParentItem();
                    artifactID = parentItem.getText();

                    calculateRepoString(parentItem);
                    groupID = getGroupID();
                    treePathString.clear();

                    // Set the input for the Textfields
                    groupIDText.setText(groupID);
                    artifactIDText.setText(artifactID);
                    versionText.setText(version);
                }
            }
        });
    }

    /**
     * Get the top-level parent item of the given {@link TreeItem}.
     * 
     * @param item
     *            Given {@link TreeItem}.
     * @return the top-level parent {@link TreeItem}.
     */
    public TreeItem getParentItem(TreeItem item) {

        TreeItem toReturn;
        if (item.getParentItem() != null) {
            getParentItem(item.getParentItem());
        }
        toReturn = item;
        return toReturn;

    }

    /**
     * Build the group-ID-text out of the currently selected {@link TreeItem}
     * and the {@link TreeViewer}-structure.
     * 
     * @param item
     *            selected {@link TreeItem}
     */
    public void calculateRepoString(TreeItem item) {

        if (item.getParentItem() != null) {
            treePathString.add(item.getParentItem().getText());
            calculateRepoString(item.getParentItem());
        }
    }

    /**
     * Get the actual group-ID.
     * 
     * @return grouIDToReturn groupID of the selected maven-artifact.
     */
    private String getGroupID() {
        groupIDToReturn = "";
        for (int i = treePathString.size() - 1; i >= 1; i--) {
            groupIDToReturn += treePathString.get(i).toString() + ".";
        }
        groupIDToReturn += treePathString.get(0).toString();
        return groupIDToReturn;
    }
    
    /**
     * Returns the cache update tooltip text.
     * 
     * @return the text
     */
    private String getLastUpdateToolTipText() {
        String result = "Last cache update: ";
        File file = getTreeFile();
        if (file.exists()) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(file.lastModified());
            DateFormat format = DateFormat.getDateInstance();
            result += format.format(cal.getTime());
        } else {
            result += "-";
        }
        return result;
    }

    /**
     * Create the buttons for the dialog.
     * 
     * @param buttonsContainer
     *            Container for the buttons.
     */
    public void createButtons(final Composite buttonsContainer) {
        final Button refresh = new Button(buttonsContainer, SWT.RIGHT);
        refresh.setToolTipText(getLastUpdateToolTipText());
        refresh.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        refresh.setSize(SWT.DEFAULT, SWT.DEFAULT);
        refresh.setText(" Refresh  ");
        Button ok = new Button(buttonsContainer, SWT.RIGHT);
        ok.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        ok.setSize(SWT.DEFAULT, SWT.DEFAULT);
        ok.setText("   OK     ");
        ok.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent exc) {
                String putIn = "";
                putIn = groupIDText.getText().replaceAll("/", ".");

                if (putIn.endsWith("/") || putIn.endsWith(".")) {
                    putIn = putIn.substring(0, groupIDText.getText().length() - 1);
                }
                artifactEditorUpdater.updateText(putIn + ":" + artifactIDText.getText() + ":" + versionText.getText());
                
                closeEditor();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent exc) {
            }
        });

        Button cancel = new Button(buttonsContainer, SWT.RIGHT);
        cancel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        cancel.setSize(SWT.DEFAULT, SWT.DEFAULT);
        cancel.setText("  Cancel    ");

        cancel.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent exc) {
                getShell().dispose();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent exc) {
            }
        });
        refresh.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent exc) {

                if (MavenFetcher.checkRepositoryConnectivity()) {

                    createProgressDialog(buttonsContainer);
                    viewer.setInput(mavenList);
                    saveTreeLocally();
                    refresh.setToolTipText(getLastUpdateToolTipText());
                } else {
                    loadTreeLocally();
                }
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent exc) {
            }
        });
    }

    /**
     * Close the editor.
     */
    private void closeEditor() {
        Display.getCurrent().getActiveShell().close();    
    }
    
    /**
     * Create the user-interface.
     * 
     * @param labelsContainer
     *            Container for the labels.
     */
    public void createUserInterface(Composite labelsContainer) {
        Label header = new Label(labelsContainer, SWT.NONE);
        header.setText("Maven Artifact: ");
        Font boldFont = new Font(header.getDisplay(), new FontData("Arial", 11,
                SWT.BOLD));
        header.setFont(boldFont);

        Composite textArea = new Composite(labelsContainer, SWT.FILL);
        GridLayout layout = new GridLayout(2, false);
        textArea.setLayout(layout);
        layout.marginHeight = 25;
        layout.marginRight = 10;
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 350;
        Label groupIDLabel = new Label(textArea, SWT.NONE);
        groupIDLabel.setText("Group ID:"); 
        groupIDText = new Text(textArea, SWT.BORDER);  
        groupIDText.setLayoutData(data);
        if (null != initialGroupId) {
            groupIDText.setText(initialGroupId);
        }
        Label artifactIDLabel = new Label(textArea, SWT.NONE);
        artifactIDLabel.setText("Artifact ID:");
        artifactIDText = new Text(textArea, SWT.BORDER);
        artifactIDText.setLayoutData(data);
        if (null != initialArtifactId) {
            artifactIDText.setText(initialArtifactId);
        }
        Label versionLabel = new Label(textArea, SWT.NONE);
        versionLabel.setText("Version:");
        versionText = new Text(textArea, SWT.BORDER);
        versionText.setLayoutData(data);
        if (null != initialVersion) {
            versionText.setText(initialVersion);
        }
        createUserInterfaceListeners();
    }   

    /**
     * Creat5es the listeners for the user interface elements.
     */
    private void createUserInterfaceListeners() {
        groupIDText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent exc) {
                String text = groupIDText.getText().replaceAll("/", ".");
                String[] selectedItemPath = text.split("\\.");
                highlightTreePath(selectedItemPath);
            }
        });
        artifactIDText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent exc) {
                String groupText = groupIDText.getText().replaceAll("/", ".");
                String artifactText = artifactIDText.getText().replaceAll("/", ".");
                
                if (groupText.endsWith(".")) {
                    groupText = cutOfflastCharacter(groupText);
                }
                if (artifactText.endsWith(".")) {
                    artifactText = cutOfflastCharacter(artifactText); 
                }
                String itemPath = groupText + "." + artifactText;
                String[] selectedItemPath = itemPath.split("\\.");
                highlightTreePath(selectedItemPath);
            }
        });
        versionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent exc) {
                String groupText = groupIDText.getText().replaceAll("/", ".");
                String artifactText = artifactIDText.getText().replaceAll("/", ".");
                String verText = versionText.getText().replaceAll("/", ".");               
                if (groupText.endsWith(".")) {
                    groupText = cutOfflastCharacter(groupText);
                }
                if (artifactText.endsWith(".")) {
                    artifactText = cutOfflastCharacter(artifactText); 
                } //use _ as separator because the versionid contains dots. 
                String itemPath = groupText + "_" + artifactText + "_" + verText;
                String[] selectedItemPath = itemPath.split("\\_");
                highlightTreePath(selectedItemPath);
            }
        });
    }
    
    /**
     * Defines the optional (initial) tree path.
     * 
     * @param initialTreePath the selected tree path in terms of a Maven artifact spec
     */
    public void setInitialTreePath(String initialTreePath) {
        String[] selectedItemPath = initialTreePath.split(":");
        if (null != selectedItemPath && 3 == selectedItemPath.length) {
            initialGroupId = selectedItemPath[0];
            initialArtifactId = selectedItemPath[1];
            initialVersion = selectedItemPath[2];
            
            String[] selector = initialGroupId.split("\\.");
            if (null != selector && selector.length > 0) {
                List<String> tmp = new ArrayList<String>();
                for (int s = 0; s < selector.length; s++) {
                    tmp.add(selector[s]);
                }
                for (int s = 1; s < selectedItemPath.length; s++) { // selector = [0] -> start with 1
                    tmp.add(selectedItemPath[s]);
                }
                selectedItemPath = new String[tmp.size()];
                tmp.toArray(selectedItemPath);
            }
        }
        this.initialTreePath = selectedItemPath;
    }

    /**
     * Highlight a given path in the {@link TreeViewer}.
     * @param selectedItemPath Given path.
     */
    private void highlightTreePath(String[] selectedItemPath) {
        TreeElement[] treeList = find(viewer, selectedItemPath);
        if (treeList != null && treeList.length > 0) {
            viewer.setExpandedElements(treeList);
            viewer.reveal(treeList[treeList.length - 1]);
            viewer.setSelection(new TreeSelection(new TreePath(treeList)));
        }
    }
    
    /**
     * Cut off the last character if it is a ".".
     * @param input the input-String.
     * @return The String without ".".
     */
    private String cutOfflastCharacter(String input) {
        String toReturn = "";
        
        if (input.length() > 0 && input.endsWith(".")) {
            toReturn = input.substring(0, input.length() - 1);
        }
        
        return toReturn;
    }
    
    /**
     * Find the elements in the {@link Viewer} given by their names
     * in order to highlight their path.
     * @param viewer The {@link Tree}´s viewer.
     * @param searchStrings THe item-names to search for.
     * @return TreeElement[] the found Tree-elements.
     */
    private TreeElement[] find(TreeViewer viewer, String[] searchStrings) {
        List<TreeElement> tmp = new ArrayList<TreeElement>();
        
        boolean found = false;
        //List<TreeElement> mavenList = MavenFetcher.getElementTree();
        
        for (int i = 0; !found && i < mavenList.size(); i++) {
            found = find(mavenList.get(i), searchStrings, 0, tmp);
        }

        TreeElement[] result;
        if (!found) {
            result = null;
        } else {
            result = new TreeElement[tmp.size()];
            tmp.toArray(result);
            viewer.refresh();
        }
        return result;
    }

    /**
     * Recursive method in order to grab the whole tree.
     * In this recursion all tree-elements are compared with the Strings from the user-input.
     * Upon that a list with Tree-elements is built which functions as path for the tree.
     * This tree-path will then be openend.
     * 
     * @param item Current {@link TreeItem}.
     * @param searchStrings All the string to look for.
     * @param pos The current position in the tree.
     * @param path The path in which we store the several items.
     * @return result true if found. false if not.
     */
    private boolean find(TreeElement item, String[] searchStrings, int pos, List<TreeElement> path) {
        /* check this item */
        // if item matches searchStrings[pos]
        boolean result = false;
        String itemName = item.getName();
        
        if (itemName.endsWith("/")) {
            if (itemName.length() > 0) {
                itemName = itemName.substring(0, itemName.length() - 1);
            }
        }
        if (pos < searchStrings.length && itemName.equals(searchStrings[pos])) {
            path.add(item);
            result = pos == searchStrings.length - 1;
            for (int i = 0; !result && i < item.getChildren().size(); i++) {
                result = find(item.getChildren().get(i), searchStrings, pos + 1, path);
            }
        }
        return result;
    }
    
    @Override
    protected void configureShell(Shell newShell) {

        newShell.pack();
        newShell.setSize(500, 650);

        super.configureShell(newShell);
        newShell.setText("Maven Selector");
        DialogsUtil.centerShell(newShell);
    }

    @Override
    protected void okPressed() {
        super.okPressed();
    }

    /**
     * This class provides the content for the tree in FileTree.
     */
    class MyTreeContentProvider implements ITreeContentProvider {

        @Override
        public void dispose() {
        }

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        @Override
        public Object[] getElements(Object inputElement) {
            Object[] result;
            if (inputElement instanceof List) {
                result = ((List<?>) inputElement).toArray();
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Object[] getChildren(Object parentElement) {
            Object[] result;
            if (parentElement instanceof TreeElement) {
                TreeElement treeElement = (TreeElement) parentElement;
                result = treeElement.getChildren().toArray();
            } else {
                result = null;
            }
            return result;
        }

        @Override
        public Object getParent(Object element) {
            return null;
        }

        @Override
        public boolean hasChildren(Object element) {
            boolean toReturn;
            if (element instanceof TreeElement) {
                toReturn = true;
            } else {
                toReturn = false;
            }
            return toReturn;
        }
    }

    /**
     * This class provides the labels for the file tree.
     */
    class MyTreeLabelProvider implements ILabelProvider {

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
            Image toReturn = null;

            if (element instanceof TreeElement) {
                TreeElement treeElement = (TreeElement) element;

                if (treeElement.getName().matches(REGEX)
                        || treeElement
                                .getName()
                                .substring(0,
                                        treeElement.getName().length() - 1)
                                .matches(REGEX)) {
                    toReturn = IconManager.retrieveImage(IconManager.TREE_FILE);
                } else {
                    toReturn = IconManager
                            .retrieveImage(IconManager.TREE_FOLDER);
                }
            }
            return toReturn;
        }

        @Override
        public String getText(Object element) {
            String toReturn = "";
            if (element instanceof TreeElement) {
                TreeElement treeElement = (TreeElement) element;

                if (treeElement.getName() != null
                        && treeElement.getName().endsWith("/")) {
                    toReturn = treeElement.getName().substring(0,
                            treeElement.getName().length() - 1);
                }
            } else {
                toReturn = null;
            }
            return toReturn;
        }
    }
}