package de.uni_hildesheim.sse.qmApp.dialogs;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

//this shall be the Activator.PLUGIN_ID but then the roles are away
import de.uni_hildesheim.sse.easy_producer.instantiator.Bundle;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.repositoryConnector.IRepositoryConnector;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.ApplicationRole;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.RepositoryEventHandler;
import de.uni_hildesheim.sse.repositoryConnector.svnConnector.SVNConnector;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * Login Dialog that pops up when the applications starts.
 * 
 * @author Sass
 */
public class LoginDialog {
    
    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(LoginDialog.class, Bundle.ID);
    private static IRepositoryConnector repositoryConnector = new SVNConnector();
    private static final boolean OFFLINE_DEBUG = false; // requires conf.properties

    private Text password;
    private Text username;
    private Display display;
    private ProgressIndicator progress;
    
    private String localDataToolTip = "If this checkbox is selected you will be working with local data only."
            + "\n You will not be able to commit changes to the pipeline repository.";
    private String localDataUsedMessage = "Last time you used the application you worked with local data and you are "
            + "about to use the remote repository. This will lead to a synchronization of the configuration and "
            + "if you press 'No', local changes will be overridden with more recent settings from the repository. "
            + "Do you want to stay in local mode?";

    /**
     * Displays the login dialog.
     * 
     * @param display
     *            Display
     */
    public LoginDialog(Display display) {
        this.display = display;
    }

    /**
     * Creates the login pop up.
     */
    @SuppressWarnings("unused")
    public void createContents() {
        final Shell shell = new Shell(display);
        shell.setText("QualiMaster Infrastructure Configuration - Login");
        // Set the qualimaster-icon for the login-shell.
        Image icon = IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL);
        shell.setImage(icon);
        shell.addListener(SWT.Close, new Listener() {
            public void handleEvent(Event event) {
                System.exit(0);
            }
        });
        final FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 1;
        shell.setLayout(fillLayout);
        // Create a composite with grid layout.
        final Composite composite = createGridLayout(shell);
        // Setting the background of the composite with the image background for login dialog
        final Label imageLabel = createBackground(composite);
        // Creating the composite which will contain the login related widgets
        final Composite cmpLogin = new Composite(composite, SWT.NONE);
        final RowLayout rowLayout = new RowLayout();
        rowLayout.fill = true;
        rowLayout.type = SWT.VERTICAL;
        cmpLogin.setLayout(rowLayout);
        final GridData gridData = new GridData(GridData.FILL, GridData.FILL, false, false);
        cmpLogin.setLayoutData(gridData);
        // Label for the username
        final CLabel usernameLabel = createLabel(cmpLogin, "Username");
        // Textfield for the username
        username = createTextField(cmpLogin, false);
        // Label for the password
        final CLabel passwordLabel = createLabel(cmpLogin, "Password");
        // Textfield for the password
        password = createTextField(cmpLogin, true);
        final Button selectButton = createCheckBox(cmpLogin, "No login - local data");
        selectButton.setToolTipText(localDataToolTip);
        progress = createProgressIndicator(cmpLogin);
        final Button loginButton = createLoginButton(cmpLogin);
        addSelectButtonListener(loginButton, selectButton);
        // Adding login action to this button.
        addButtonListener(loginButton, shell, selectButton);
        shell.setDefaultButton(loginButton);
        // Adding ability to move shell around
        Listener listener = createMoveListener(shell);
        // Adding the listeners to all visible components
        composite.addListener(SWT.MouseDown, listener);
        composite.addListener(SWT.MouseUp, listener);
        composite.addListener(SWT.MouseMove, listener);
        imageLabel.addListener(SWT.MouseDown, listener);
        imageLabel.addListener(SWT.MouseUp, listener);
        imageLabel.addListener(SWT.MouseMove, listener);
        // Call pack before setting the location or else the location will be incorrect
        shell.pack();
        shell.setLocation(DialogsUtil.getXPosition(shell, display), DialogsUtil.getYPosition(shell, display));
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Allows switching the login button text based on the selection of the checkbox.
     * 
     * @param loginButton
     *            the login button
     * @param selectButton
     *            the selection button
     */
    private void addSelectButtonListener(final Button loginButton, final Button selectButton) {
        selectButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                if (selectButton.getSelection()) {
                    loginButton.setText("Start");
                } else {
                    loginButton.setText("Login");
                }
            }

        });
    }

    /**
     * Creates a CheckBox.
     * 
     * @param composite
     *            The composite of the CheckBox
     * @param text
     *            The text of the CheckBox
     * @return new CheckBox
     */
    private Button createCheckBox(Composite composite, String text) {
        Button checkBox = new Button(composite, SWT.CHECK);
        checkBox.setText(text);
        if (!localDataExists()) {
            checkBox.setEnabled(false);
        }
        return checkBox;
    }

    /**
     * Creates a {@link Listener} to move the {@link Shell} around.
     * 
     * @param shell
     *            The shell to be moved
     * @return new {@link Listener}
     */
    private Listener createMoveListener(final Shell shell) {
        Listener listener = new Listener() {
            private Point origin;

            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.MouseDown:
                    origin = new Point(event.x, event.y);
                    break;
                case SWT.MouseUp:
                    origin = null;
                    break;
                case SWT.MouseMove:
                    if (origin != null) {
                        Point p = display.map(shell, null, event.x, event.y);
                        shell.setLocation(p.x - origin.x, p.y - origin.y);
                    }
                    break;
                default:
                    break;
                }
            }
        };
        return listener;
    }

    /**
     * Implements the model update running in parallel to the user interface so that user interface updates can happen.
     * 
     * @author Holger Eichelberger
     */
    private class ModelUpdate implements Runnable, RepositoryEventHandler {

        private Shell shell;

        /**
         * Creates a model updater.
         * 
         * @param shell
         *            the shell of the calling window to be closed at the end of the update process
         */
        private ModelUpdate(Shell shell) {
            this.shell = shell;
            repositoryConnector.setUpdateEventHandler(this);
        }

        @Override
        public void run() {
            Display.getDefault().syncExec(new Runnable() {

                @Override
                public void run() {
                    progress.beginTask(repositoryConnector.getRepositoryEntryCount());
                }
            });
            logger.info("Loading model from repository");
            if (OFFLINE_DEBUG) {
                // fake access for testing
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.exception(e);
                }
                Location.setModelLocation(ConfigurationProperties.MODEL_LOCATION.getValue());
            } else {
                File wcPath = Utils.getDestinationFileForModel();
                final List<File> conflicts = repositoryConnector.getConflictingFilesInWorkspace(wcPath);
                if (conflicts.size() > 0) {
                    // we got some conflicts: notify the user
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            String conflictsAsString = "";
                            for (File file : conflicts) {
                                conflictsAsString = file + "\n";
                            }
                            MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
                            dialog.setText("Synchronize found conflicts");
                            dialog.setMessage("Conflicts in working copy found. "
                                    + "How do you want to handle these conflicts?"
                                    + "\n"
                                    + conflictsAsString
                                    + "\n"
                                    + "Press OK to update to override local changes. Press cancel "
                                    + "to keep local changes");
                            int response = dialog.open();
                            if (response == SWT.OK) {
                                Location.setModelLocation(Utils.getDestinationFileForModel().getAbsolutePath());
                                repositoryConnector.updateModel(Utils.getDestinationFileForModel());
                                repositoryConnector.resolveConflicts(Utils.getDestinationFileForModel(), false);
                            } else if (response == SWT.CANCEL) {
                                //TODO:  Update or better do nothing?
                                Location.setModelLocation(Utils.getDestinationFileForModel().getAbsolutePath());
                                completed();
                            }
                        }
                    });
                } else {
                    // update model
                    Location.setModelLocation(repositoryConnector.loadModel(Utils.getDestinationFileForModel())
                            .getAbsolutePath());
                }
                // Save user roles
                saveRoles();
                EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.LOCAL_DATA_PREF_KEY, "false");
            }
        }

        @Override
        public void progress(final double status) {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    progress.worked(status);
                }
            });

        }

        @Override
        public void completed() {
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    progress.done();
                    shell.dispose();

                }
            });
        }
    }

    /**
     * Adds a {@link Listener} to the login button.
     * 
     * @param button
     *            The login button
     * @param shell
     *            The shell
     * @param selectButton
     *            The select button to check if user wants to work with local data
     * 
     */
    private void addButtonListener(final Button button, final Shell shell, final Button selectButton) {
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                button.setEnabled(false);
                if (selectButton.getSelection()) {
                    // Load Roles and set location
                    loadRoles();
                    shell.dispose();
                } else {
                    if (authenticate(username.getText(), password.getText())) {
                        // check if local data was used before
                        checkIfLocalDataWasUsed(shell);
                        new Thread(new ModelUpdate(shell)).start();
                    } else {
                        showLoginError(shell);
                        logger.warn("Login failed");
                        button.setEnabled(true);
                    }
                }
            }

            private void showLoginError(final Shell shell) {
                MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.OK | SWT.CANCEL);
                dialog.setText("Login failed");
                dialog.setMessage("The login failed. Please make sure you have "
                        + "a working internet connection and check your credentials.");
                dialog.open();
            }
        });
    }
    
    /**
     * Check if local data was used before.
     * @param shell The shell
     */
    private void checkIfLocalDataWasUsed(Shell shell) {
        boolean localData = Boolean.valueOf(EclipsePrefUtils.INSTANCE.getPreference(
            EclipsePrefUtils.LOCAL_DATA_PREF_KEY));
        if (localData) {
            MessageBox dialog = new MessageBox(shell, SWT.ICON_INFORMATION | SWT.YES | SWT.NO);
            dialog.setText("Warning");
            dialog.setMessage(localDataUsedMessage);
            int buttonID = dialog.open();
            switch(buttonID) {
            case SWT.YES:
                break;
            case SWT.NO:
                repositoryConnector.revert(Utils.getDestinationFileForModel());
                break;
            default:
                break;
            }
        }
    }

    /**
     * Loads the roles from {@link IEclipsePreferences}.
     */
    private void loadRoles() {
        Set<Role> roles = new HashSet<Role>();
        // you might want to call prefs.sync() if you're worried about others changing your settings
        if (EclipsePrefUtils.INSTANCE.getPreference(ApplicationRole.ADMIN.getId()) != null) {
            roles.add(ApplicationRole.ADMIN);
        }
        if (EclipsePrefUtils.INSTANCE.getPreference(ApplicationRole.ADAPTATION_MANAGER.getId()) != null) {
            roles.add(ApplicationRole.ADAPTATION_MANAGER);
        }
        if (EclipsePrefUtils.INSTANCE.getPreference(ApplicationRole.INFRASTRUCTURE_ADMIN.getId()) != null) {
            roles.add(ApplicationRole.INFRASTRUCTURE_ADMIN);
        }
        if (EclipsePrefUtils.INSTANCE.getPreference(ApplicationRole.PIPELINE_DESIGNER.getId()) != null) {
            roles.add(ApplicationRole.PIPELINE_DESIGNER);
        }
        UserContext.INSTANCE.setRoles(roles);
        String repoURL = EclipsePrefUtils.INSTANCE.getPreference(EclipsePrefUtils.REPOSITORY_URL_PREF_KEY);
        if (null != repoURL) {
            Location.setModelLocation(Utils.getDestinationFileForModel(repoURL).getAbsolutePath());
        }
        // add preference entry that local data was used
        EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.LOCAL_DATA_PREF_KEY, "true");
    }
    
    /**
     * Saves the roles to {@link IEclipsePreferences}.
     */
    private void saveRoles() {
        // saves plugin preferences at the workspace level
        Set<Role> roles = UserContext.INSTANCE.getRoles();
        for (Role role : roles) {
            EclipsePrefUtils.INSTANCE.addPreference(role.getId(), role.getId());
        }
        EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.REPOSITORY_URL_PREF_KEY, 
            repositoryConnector.getUserContext().getRepositoryURL());
    }

    /**
     * Checks if there is local data that can be used.
     * 
     * @return true if a folder with local data exists
     */
    private boolean localDataExists() {
        boolean exists = Utils.getDestinationFileForModel().isDirectory();
        if (!exists) {
            String url = EclipsePrefUtils.INSTANCE.getPreference(EclipsePrefUtils.REPOSITORY_URL_PREF_KEY);
            if (url != null) {
                exists = Utils.getDestinationFileForModel(url).isDirectory();
            }
        }
        return exists;
    }

    /**
     * Creates the login button.
     * 
     * @param composite
     *            The composite for the button
     * @return new created Button
     */
    private Button createLoginButton(Composite composite) {
        Button button = new Button(composite, SWT.FLAT);
        button.setText("Login");
        return button;
    }

    /**
     * Creates the (pseudo) progress indicator for checkout.
     * 
     * @param composite
     *            the parent composite
     * @return the created progress indicator
     */
    private ProgressIndicator createProgressIndicator(Composite composite) {
        ProgressIndicator progress = new ProgressIndicator(composite, SWT.HORIZONTAL);
        progress.beginTask(10); // show it up, no progress
        return progress;
    }

    /**
     * Creates a {@link Text} for a given {@link Composite}.
     * 
     * @param composite
     *            The given Composite
     * @param password
     *            Indicates if the given text is a password and should be masked.
     * @return the new {@link Text}
     */
    private Text createTextField(final Composite composite, boolean password) {
        Text newText = new Text(composite, SWT.BORDER);
        final RowData rowData2 = new RowData();
        rowData2.width = 170;
        newText.setLayoutData(rowData2);
        if (password) {
            newText.setEchoChar('*');
        }
        return newText;
    }

    /**
     * Creates a {@link CLabel} for a given {@link Composite}.
     * 
     * @param composite
     *            The given Composite
     * @param text
     *            The text of the label
     * @return the new {@link CLabel}
     */
    private CLabel createLabel(final Composite composite, String text) {
        final CLabel cLabel = new CLabel(composite, SWT.NONE);
        final RowData rowData = new RowData();
        rowData.width = 180;
        cLabel.setLayoutData(rowData);
        cLabel.setText(text);
        return cLabel;
    }

    /**
     * Sets the background image of the {@link Composite}.
     * 
     * @param composite
     *            The composite of the shell
     * @return {@link Label} with the background image
     */
    private Label createBackground(final Composite composite) {
        final Label imageLabel = new Label(composite, SWT.NONE);
        imageLabel.setLayoutData(new GridData(195, 181));
        final Image img = IconManager.retrieveImage("icons/QMlogoMedium.png");
        final Image img2 = new Image(display, img.getImageData().scaledTo(195, 111));
        imageLabel.setImage(img2);
        return imageLabel;
    }

    /**
     * Creates the composite for the shell.
     * 
     * @param shell
     *            The shell
     * @return The created {@link Composite}
     */
    private Composite createGridLayout(final Shell shell) {
        final Composite composite = new Composite(shell, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);
        return composite;
    }

    /**
     * Authenticates the given user.
     * 
     * @return true or false
     * 
     * @param username
     *            The login for the user
     * @param password
     *            The password for the user
     */
    private boolean authenticate(String username, String password) {
        boolean result;
        repositoryConnector.setRepositoryURL(ConfigurationProperties.REPOSITORY_URL.getValue());
        if (OFFLINE_DEBUG) {
            result = true;
        } else {
            result = repositoryConnector.authenticate(username, password);
            Infrastructure.setUserName(username);
            EclipsePrefUtils.INSTANCE.addPreference(EclipsePrefUtils.USERNAME_PREF_KEY, username);
        }
        return result;
    }

}