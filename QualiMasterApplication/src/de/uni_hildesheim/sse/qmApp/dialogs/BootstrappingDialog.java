package de.uni_hildesheim.sse.qmApp.dialogs;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;

/**
 * Dialog for bootstrapping the application.
 * 
 * @author Sass
 *
 */
public class BootstrappingDialog {

    public static final BootstrappingDialog INSTANCE = new BootstrappingDialog();

    private static final String APPLICATION_NAME = "QualiMaster Infrastructure Configuration (QM-IConf)";

    private static final String INIT_INFO_TEXT = "Welcome to the " + APPLICATION_NAME + " bootstrapping. This guide "
            + "will help you to set up the application.";

    private static final String REPO_CONNECTOR_INFO_TEXT = "If you have access to a SVN repository containing the "
            + "model, all the remainder of QM-IConf configuration will be done automatically. Please choose how you "
            + "want to configure QM-IConf.";

    private static final String REPO_URL_CONNECTOR_INFO_TEXT = "Please provide URL url of the repository. "
            + "You can either use the default repository (" + ConfigurationProperties.REPOSITORY_URL.getValue() + ") "
            + "or you can configure your own repository url. If the field is left blank the default repository will "
            + "be used.";

    private static final String MODEL_LOCATION_INFO_TEXT = "Please provide the source location of the unzipped model. "
            + "Upon application start the model will be copied into the work space of QM-IConf. The default model "
            + "location in the work space will be\n\n " + Utils.getDestinationFileForModel();

    private static final String BUTTON_NEXT = "Next";

    private static final String BUTTON_REPOSITORY = "Repository";

    private static final String BUTTON_DEFAULT_REPOSITORY = "Use the default repository";

    private static final String BUTTON_OWN_REPOSITORY = "Configure own repository url";

    private static final String BUTTON_MANUAL = "Manually (I downloaded and unzipped the model)";

    private static final String BUTTON_SET_LOCATION = "Select model";

    private static final String BUTTON_BACK = "Back";

    private static final String BUTTON_CANCEL = "Cancel";

    private static final String INIT_TITLE = "Configuration assistant";

    private static final String REPO_CONNECTOR_TITLE = "Repository connector";

    private static final String REPO_URL_CONNECTOR_TITLE = "Configure repository url";

    private static final String MODEL_LOCATION_TITLE = "Set model location";

    private static final String MODEL_LINK = "http://jenkins.sse.uni-hildesheim.de/job/QM-Pipelines/lastSuccessfulBuild"
            + "/artifact/QM_Model.zip";

    private static final String INIT_MODEL_LINK_TEXT = "This application needs a working copy of the model. If you "
            + "have access to a SVN repository with the model you want to use, you don't have to download, unzip and "
            + "install an initially empty model. Otherwise you can get an actual copy of an empty model from ";

    private static final int PARENT_WIDTH = 400;

    private static final int PARENT_HEIGHT = 350;

    private static final int NUMBER_OF_COMPOSITES = 4;

    private Button repo = null;

    private Button manual = null;

    private Button next = null;

    private Button defaultRepo = null;

    private Button ownRepo = null;

    private Text urlField = null;

    private boolean sourceLocationConfigured = false;
    private boolean modelLocationConfigured = false;

    /**
     * A private Constructor prevents any other class from instantiating.
     */
    private BootstrappingDialog() {
    }

    /**
     * Initialize the bootstrapping.
     * 
     * @param display
     *            Display
     */
    public void init(final Display display) {
        boolean configured = Boolean
                .valueOf(EclipsePrefUtils.INSTANCE.getPreference(EclipsePrefUtils.APP_CONFIGURED_PREF_KEY));
        boolean manually = Boolean
                .valueOf(EclipsePrefUtils.INSTANCE.getPreference(EclipsePrefUtils.MANUAL_CONFIGURED_PREF_KEY));
        if (configured) {
            if (manually) {
                ConfigurationProperties.DISABLE_LOGIN.store(String.valueOf(true));
                ConfigurationProperties.MODEL_LOCATION.store(Utils.getDestinationFileForModel().getAbsolutePath());
            }
        } else {
            if (!ConfigurationProperties.DEMO_MODE.getBooleanValue()) {
                createDialog(display);
            }
        }
    }

    /**
     * Creates the bootstrapping dialog.
     * 
     * @param display
     *            Display
     */
    private void createDialog(final Display display) {
        final Shell shell = new Shell(display);
        Image icon = IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL);
        shell.setImage(icon);
        shell.setText(APPLICATION_NAME);
        shell.setLayout(new GridLayout(2, false));
        shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                System.exit(0);
            }
        });
        final Composite root = createLeftNavigation(display, shell);
        final Composite parent = new Composite(shell, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
        gridData.widthHint = PARENT_WIDTH;
        gridData.minimumHeight = PARENT_HEIGHT;
        parent.setLayoutData(gridData);
        final StackLayout layout = new StackLayout();
        parent.setLayout(layout);
        final Composite[] compositeArray = new Composite[NUMBER_OF_COMPOSITES];
        createWelcomeContent(shell, display, parent, compositeArray);
        createConnectorContent(shell, display, parent, compositeArray);
        createModelLocationContent(shell, parent, compositeArray);
        createRepositoryURLContent(shell, parent, compositeArray);
        // Set first page to be shown
        layout.topControl = compositeArray[0];
        new Label(shell, SWT.NONE);
        // Navigation
        createNavigation(shell, root, parent, layout, compositeArray);
        shell.pack();
        shell.setLocation(DialogsUtil.getXPosition(shell, display), DialogsUtil.getYPosition(shell, display));
        shell.open();
        while (shell != null && !shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Creates the navigation buttons and handles the navigation between the pages.
     * 
     * @param shell
     *            Shell
     * @param root
     *            root composite
     * @param parent
     *            parent composite
     * @param layout
     *            layout
     * @param compositeArray
     *            array with composites
     */
    private void createNavigation(final Shell shell, final Composite root, final Composite parent,
            final StackLayout layout, final Composite[] compositeArray) {
        final Composite navigation = createComposite(shell);
        Button back = createButton(navigation, BUTTON_BACK);
        next = createButton(navigation, BUTTON_NEXT);
        Button cancel = createButton(navigation, BUTTON_CANCEL);
        final int[] indexNextButton = new int[1];
        next.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                indexNextButton[0] = (indexNextButton[0] + 1) % NUMBER_OF_COMPOSITES;
                if ((indexNextButton[0] == 1 && !manual.getSelection()) || !defaultRepo.getSelection()) {
                    next.setEnabled(false);
                }
                if (indexNextButton[0] == 0) { // Close when last composite is reached.
                    // Validate settings.
                    validateSettings(shell, root, parent, layout, compositeArray, indexNextButton);
                } else {
                    if (repo.getSelection()) {
                        indexNextButton[0] = 3;
                        layout.topControl = compositeArray[indexNextButton[0]];
                        highlightLeftNavigation(shell.getDisplay(), root, indexNextButton);
                        parent.layout();
                    } else if (manual.getSelection() && modelLocationConfigured) {
                        validateSettings(shell, root, parent, layout, compositeArray, indexNextButton);
                    } else {
                        layout.topControl = compositeArray[indexNextButton[0]];
                        highlightLeftNavigation(shell.getDisplay(), root, indexNextButton);
                        parent.layout();
                    }
                }
            }
        });
        back.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                if (indexNextButton[0] > 0) {
                    next.setEnabled(true);
                    if (indexNextButton[0] == 3) {
                        indexNextButton[0] = indexNextButton[0] - 2;
                    } else {
                        indexNextButton[0] = indexNextButton[0] - 1;
                    }
                    highlightLeftNavigation(shell.getDisplay(), root, indexNextButton);
                    layout.topControl = compositeArray[indexNextButton[0]];
                    parent.layout();
                }
            }
        });
        cancel.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                System.exit(0);
            }
        });
        repo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (repo.getSelection()) {
                    ownRepo.setEnabled(true);
                    defaultRepo.setEnabled(true);
                    manual.setSelection(false);
                    next.setEnabled(true);
                } else {
                    next.setEnabled(false);
                }
            }
        });
        createManualSelectionListener();
    }

    /**
     * Creates the {@link SelectionListener} for manual.
     */
    private void createManualSelectionListener() {
        manual.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                if (manual.getSelection()) {
                    ownRepo.setSelection(false);
                    ownRepo.setEnabled(false);
                    defaultRepo.setSelection(false);
                    defaultRepo.setEnabled(false);
                    repo.setSelection(false);
                    next.setEnabled(true);
                } else {
                    next.setEnabled(false);
                }
            }
        });
    }

    /**
     * Validates the settings made by the user.
     * 
     * @param shell
     *            Shell
     * @param root
     *            root Composite
     * @param parent
     *            parent Composite
     * @param layout
     *            StockLayout
     * @param compositeArray
     *            array containing the stocks for the stock layout
     * @param indexNextButton
     *            index of the page
     */
    // checkstyle: stop parameter number check
    private void validateSettings(final Shell shell, final Composite root, final Composite parent,
            final StackLayout layout, final Composite[] compositeArray, final int[] indexNextButton) {
        if (validateConfiguration()) {
            EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.APP_CONFIGURED_PREF_KEY, "true");
            if (defaultRepo.getSelection() || ownRepo.getSelection()) {
                EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.MANUAL_CONFIGURED_PREF_KEY, "false");
                if (ownRepo.getSelection() && null != urlField.getText() && !urlField.getText().isEmpty()) {
                    ConfigurationProperties.REPOSITORY_URL.store(urlField.getText());
                }
            } else {
                EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.MANUAL_CONFIGURED_PREF_KEY, "true");
            }
            shell.dispose();
        } else {
            next.setEnabled(false);
            indexNextButton[0] = 2;
            layout.topControl = compositeArray[indexNextButton[0]];
            highlightLeftNavigation(shell.getDisplay(), root, indexNextButton);
            parent.layout();
            createMessage(shell);
        }
    }
    // checkstyle: resume parameter number check

    /**
     * Creates a button.
     * 
     * @param navigation
     *            parent composite
     * @param text
     *            button text
     * @return created button
     */
    private Button createButton(final Composite navigation, String text) {
        Button back = new Button(navigation, SWT.PUSH);
        back.setText(text);
        return back;
    }

    /**
     * Creates a composite.
     * 
     * @param shell
     *            Shell
     * @return created composite
     */
    private Composite createComposite(final Shell shell) {
        final Composite navigation = new Composite(shell, SWT.RIGHT);
        navigation.setLayout(new FillLayout());
        navigation.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false, 1, 1));
        return navigation;
    }

    /**
     * Creates a message dialog.
     * 
     * @param shell
     *            parent shell
     */
    private void createMessage(final Shell shell) {
        MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK);
        dialog.setText("Configuration not valid");
        dialog.setMessage(
                "Your configuration seems to be invalid. Please check that you provided " + "the model correctly.");
        dialog.open();
    }

    /**
     * Creates the left side navigation.
     * 
     * @param display
     *            Display
     * @param shell
     *            Shell
     * @return Composite
     */
    private Composite createLeftNavigation(final Display display, final Shell shell) {
        final Composite root = new Composite(shell, SWT.NONE);
        root.setLayout(new GridLayout(1, true));
        String[] labels = new String[] {INIT_TITLE, REPO_CONNECTOR_TITLE, MODEL_LOCATION_TITLE,
            REPO_URL_CONNECTOR_TITLE };
        for (String string : labels) {
            Label label = new Label(root, SWT.WRAP);
            if (string.equals(INIT_TITLE)) {
                label.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
                label.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
            }
            label.setText(string);
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        }
        return root;
    }

    /**
     * Highlights the left side navigation. The background of the labels and the text color will change.
     * 
     * @param display
     *            Display
     * @param root
     *            Composite
     * @param indexNextButton
     *            index on which page we are at
     */
    private void highlightLeftNavigation(final Display display, final Composite root, final int[] indexNextButton) {
        Control[] children = root.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[indexNextButton[0]] instanceof Label) {
                Label currentLabel = (Label) children[i];
                if (i == indexNextButton[0]) {
                    currentLabel.setBackground(display.getSystemColor(SWT.COLOR_BLUE));
                    currentLabel.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
                } else {
                    // reset colors
                    currentLabel.setBackground(null);
                    currentLabel.setForeground(null);
                }
                root.layout();
            }
        }
    }

    /**
     * Creates the content for inserting the repository url.
     * 
     * @param shell
     *            Shell
     * @param parent
     *            Composite
     * @param compositeArray
     *            array containing all composites
     */
    private void createRepositoryURLContent(Shell shell, final Composite parent, final Composite[] compositeArray) {
        compositeArray[3] = new Composite(parent, SWT.NONE);
        compositeArray[3].setLayout(new GridLayout(1, true));
        Label infoRepoConnector = new Label(compositeArray[3], SWT.WRAP);
        infoRepoConnector.setText(REPO_URL_CONNECTOR_INFO_TEXT);
        infoRepoConnector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        new Label(compositeArray[3], SWT.NONE);
        defaultRepo = new Button(compositeArray[3], SWT.CHECK);
        defaultRepo.setText(BUTTON_DEFAULT_REPOSITORY);
        defaultRepo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        ownRepo = new Button(compositeArray[3], SWT.CHECK);
        ownRepo.setText(BUTTON_OWN_REPOSITORY);
        ownRepo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        urlField = new Text(compositeArray[3], SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
        urlField.setLayoutData(data);
        urlField.setEnabled(false);
        ownRepo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (ownRepo.getSelection()) {
                    defaultRepo.setSelection(false);
                    urlField.setEnabled(true);
                    next.setEnabled(true);
                } else {
                    next.setEnabled(false);
                    urlField.setEnabled(false);
                }
            }
        });
        defaultRepo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (defaultRepo.getSelection()) {
                    ownRepo.setSelection(false);
                    urlField.setEnabled(false);
                    next.setEnabled(true);
                } else {
                    next.setEnabled(false);
                }
            }
        });
    }

    /**
     * Creates the content for the model location composite.
     * 
     * @param shell
     *            Shell
     * @param parent
     *            the parent composite
     * @param compositeArray
     *            array containing all composites
     */
    private void createModelLocationContent(final Shell shell, final Composite parent,
            final Composite[] compositeArray) {
        compositeArray[2] = new Composite(parent, SWT.NONE);
        compositeArray[2].setLayout(new GridLayout(1, true));
        Label infoModelLocation = new Label(compositeArray[2], SWT.WRAP);
        infoModelLocation.setText(MODEL_LOCATION_INFO_TEXT);
        infoModelLocation.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        new Label(compositeArray[2], SWT.NONE); // dummy
        Button setLocationButton = new Button(compositeArray[2], SWT.PUSH);
        setLocationButton.setText(BUTTON_SET_LOCATION);
        setLocationButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                DirectoryDialog modelDialog = new DirectoryDialog(shell, SWT.OPEN);
                String result = modelDialog.open();
                if (result != null) {
                    next.setEnabled(true);
                    File source = new File(result);
                    // Validate source. Note: Only checks if ivml files are provided in the folder.
                    String[] extensions = {"ivml"};
                    List<File> wanted = (List<File>) FileUtils.listFiles(source, extensions, true);
                    if (wanted.size() > 0) {
                        ConfigurationProperties.SOURCE_LOCATION.store(source.getAbsolutePath());
                        ConfigurationProperties.DISABLE_LOGIN.store(String.valueOf(true));
                        ConfigurationProperties.MODEL_LOCATION
                                .store(Utils.getDestinationFileForModel().getAbsolutePath());
                        sourceLocationConfigured = true;
                        modelLocationConfigured = true;
                        Location.setSourceLocation(source.getAbsolutePath());
                    } else {
                        createMessage(shell);
                    }
                }
            }
        });
    }

    /**
     * Creates the content for the connector selection.
     * 
     * @param shell
     *            Shell
     * @param display
     *            Display
     * @param parent
     *            the parent {@link Composite}
     * @param compositeArray
     *            array containing all composites
     */
    private void createConnectorContent(Shell shell, final Display display, final Composite parent,
            final Composite[] compositeArray) {
        compositeArray[1] = new Composite(parent, SWT.NONE);
        compositeArray[1].setLayout(new GridLayout(1, true));
        Label infoRepoConnector = new Label(compositeArray[1], SWT.WRAP);
        infoRepoConnector.setText(REPO_CONNECTOR_INFO_TEXT);
        infoRepoConnector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        new Label(compositeArray[1], SWT.NONE);
        repo = new Button(compositeArray[1], SWT.CHECK | SWT.WRAP);
        repo.setText(BUTTON_REPOSITORY);
        repo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        manual = new Button(compositeArray[1], SWT.CHECK | SWT.WRAP);
        manual.setText(BUTTON_MANUAL);
        manual.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
    }

    /**
     * Creates the content for the welcome composite.
     * 
     * @param shell
     *            Shell
     * @param display
     *            Display
     * @param parent
     *            the parent {@link Composite}
     * @param compositeArray
     *            array containing all composites
     */
    private void createWelcomeContent(Shell shell, final Display display, final Composite parent,
            final Composite[] compositeArray) {
        compositeArray[0] = new Composite(parent, SWT.NONE);
        compositeArray[0].setLayout(new GridLayout(1, true));
        Label info = new Label(compositeArray[0], SWT.WRAP);
        info.setText(INIT_INFO_TEXT);
        info.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        // Link to the model
        Label linkText = new Label(compositeArray[0], SWT.WRAP);
        linkText.setText(INIT_MODEL_LINK_TEXT);
        linkText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        Label link = new Label(compositeArray[0], SWT.WRAP);
        link.setText(MODEL_LINK);
        link.setCursor(display.getSystemCursor(SWT.CURSOR_HAND));
        link.setForeground(display.getSystemColor(SWT.COLOR_BLUE));
        link.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        link.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent event) {
                if (event.button == 1) { // Left button pressed & released
                    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
                    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
                        try {
                            desktop.browse(new URI(MODEL_LINK));
                        } catch (IOException | URISyntaxException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    /**
     * Validates the configuration.
     * 
     * @return true if configuration is valid else false.
     */
    private boolean validateConfiguration() {
        boolean result = false;
        if (defaultRepo.getSelection() || ownRepo.getSelection()) {
            result = true;
        } else if (modelLocationConfigured && sourceLocationConfigured) {
            result = true;
        }
        return result;
    }

}
