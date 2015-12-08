package de.uni_hildesheim.sse.qmApp.dialogs;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

import de.uni_hildesheim.sse.qmApp.images.IconManager;

/**
 * Dialog which presents information about the qualimaster - application.
 * 
 * @author Niko Nowatzki
 */
public class AboutDialog extends Dialog {

    private Link qualiMasterLink = null;
    
    private final String authorText = "QualiMaster Infrastructure Configuration Tool (QM-IConf)"
            + "\n\n" + "Developed by"
            + "\n" + "Stiftung University Hildesheim"
            + "\n" + "Software Systems Engineering"
            + "\n" + "Marienburger Platz 22"
            + "\n" + "30411 Hildesheim"
            + "\n" + "Germany"
            + "\n\n" + "in the QualiMaster project <a href=\"http://qualimaster.eu\">qualimaster.eu</a>"
            + " under Apache 2.0 license."
            + "\n" + "The research leading to these results has received funding from the European Union"
            + "\n" + "Seventh Framework Programme [FP7/2007-2013] under grant agreement nr. 619525.";
    
    private Label euLabel;
    private Image euLogo;

    private Label poweredBy;
    private final String poweredByText = "This application is powered by EASy-Producer:";
    
    private Label easyLabel;
    private Image easyLogo;
    
    private Label team;
    private final String teamText = "Contributions by:"
            + "\n" + "Cui Qin,"
            + "\n" + "Roman Sizonenko,"
            + "\n" + "Aike Sass,"
            + "\n" + "Niko Nowatzki,"
            + "\n" + "Dennis Konoppa,"
            + "\n" + "Bartu Dernek,"
            + "\n" + "Patrik Pastuschek,"
            + "\n" + "Holger Eichelberger";
    
    private Label versionNumber;
    
    /**
     * Default constructor for the AboutDialog.
     * @param parentShell The parent shell.
     */
    public AboutDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        //Set the qualimaster-icon for the login-shell.
        Image icon = IconManager.retrieveImage(IconManager.QUALIMASTER_SMALL);
        composite.getShell().setImage(icon);
        
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 5;
        fillLayout.marginWidth = 5;
        composite.setLayout(fillLayout);
        
        Composite outer = new Composite(composite, SWT.BORDER);  
        FormLayout formLayout = new FormLayout();
        formLayout.marginHeight = 5;
        formLayout.marginWidth = 5;
        formLayout.spacing = 5;
        outer.setLayout(formLayout);
        
        Composite innerLeft = new Composite(outer, SWT.BORDER);
        innerLeft.setLayout(new GridLayout());
        FormData fData = new FormData();
        fData.top = new FormAttachment( 0 );
        fData.left = new FormAttachment( 0 );
        fData.right = new FormAttachment( 60 ); // Locks on 60% of the view
        fData.bottom = new FormAttachment( 100 );
        innerLeft.setLayoutData(fData);

        Composite innerRight = new Composite( outer, SWT.BORDER );
        GridLayout innerRightLayout = new GridLayout();
        innerRightLayout.numColumns = 1;
        innerRight.setLayout(innerRightLayout);
        fData = new FormData();
        fData.top = new FormAttachment( 0 );
        fData.left = new FormAttachment( innerLeft );
        fData.right = new FormAttachment( 100 );
        fData.bottom = new FormAttachment( 100 );
        innerRight.setLayoutData(fData);
        
        addLink(innerLeft);
        
        euLogo = IconManager.retrieveImage(IconManager.EU_FLAG);
        euLabel = new Label(innerLeft, SWT.NONE);
        euLabel.setImage(euLogo);
        
        poweredBy = new Label(innerRight, SWT.FILL);
        poweredBy.setText(poweredByText);
        
        easyLogo = IconManager.retrieveImage(IconManager.EASY_MEDIUM);
        easyLabel = new Label(innerRight, SWT.FILL);
        easyLabel.setImage(easyLogo);
        
        team = new Label(innerRight, SWT.FILL);
        team.setText(teamText);
        
        Bundle bundle = Platform.getBundle(qualimasterapplication.Activator.PLUGIN_ID);
        versionNumber = new Label(innerRight, SWT.FILL);
        versionNumber.setText("Version number: " + bundle.getVersion().toString());
        
        //Capture ESC-Key.
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
     * Adds qualimaster-link to the info-dialog.
     * 
     * @param innerLeft Composite for the left part of the info-dialog.
     */
    private void addLink(Composite innerLeft) {
        qualiMasterLink = new Link(innerLeft, SWT.FILL);
        qualiMasterLink.setText(authorText);
        qualiMasterLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                try {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(evt.text));
                } catch (PartInitException ex) {
                    ex.printStackTrace();
                } catch (MalformedURLException ex) {
                    ex.printStackTrace();
                }
            }
        });
                    
    }
    
    @Override
    protected void createButtonsForButtonBar(final Composite composite) {
        //Create apply - button we will use instead of the original one
        final Button applyButton = new Button(composite, SWT.PUSH);
        applyButton.setText("OK");

        final int widthHint = convertHorizontalDLUsToPixels(IDialogConstants.BUTTON_WIDTH);
        final GridData data = new GridData(SWT.FILL, SWT.CENTER, false, false);
        final org.eclipse.swt.graphics.Point minButtonSize = applyButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
        
        data.widthHint = Math.max(widthHint, minButtonSize.x);
        applyButton.setLayoutData(data);

        applyButton.addSelectionListener(new SelectionAdapter() {
        
                @Override
                public void widgetSelected(final SelectionEvent exc) {
                    composite.getShell().close();
                }
            });

        final GridLayout layout = (GridLayout) composite.getLayout();
        layout.numColumns++;
    }
    
    @Override
    protected void configureShell(Shell newShell) {
    
        newShell.pack();
        newShell.setSize(800, 450);
        
        super.configureShell(newShell);
        newShell.setText("About QM-IConf");
        DialogsUtil.centerShell(newShell);
    }
    
    @Override
    protected void okPressed() {
        super.okPressed();
    }
    
}
