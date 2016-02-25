package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog asks the user for the number of clones to be created.
 * 
 * @author Niko Nowatzki
 */
public class CloneNumberInputDialog extends AbstractDialog {

    protected String input;

    private Label machineLabel;
    private Text machineField;
    private Label messageLabel;
    private String errorMessage = "";

    private int cloneCount;
    
    /**
     * Default constructor.
     * @param parentShell The parent shell.
     */
    public CloneNumberInputDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
    }
    
    /**
     * Returns the number of clones to be inserted.
     * 
     * @return the number of clones
     */
    public int getCloneCount() {
        return Math.max(0, cloneCount); // as takeover does not check for negative numbers
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);

        composite.setLayout(new GridLayout(2, true));

        //Label for dialog
        machineLabel = new Label(composite, SWT.NONE);
        machineLabel.setText("How many clones shall be created (positive number)? ");
        GridData data = new GridData();
        data.horizontalSpan = 2;
        machineLabel.setLayoutData(data);

        //Inputfield for dialog
        machineField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        machineField.setLayoutData(data);

        //messagelabel for dialog
        messageLabel = new Label(composite, SWT.NONE);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        messageLabel.setLayoutData(data);

        machineField.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent evt) {
                validateInput();
            }
        });

        /*
         * capture ESC key, It will throw an null Argument event
         * exception if not handle appropriately
         */
        composite.addListener(SWT.Traverse, new Listener() {
            
                public void handleEvent(Event evt) {
                    if (evt.detail == SWT.TRAVERSE_ESCAPE) {
                        input = null;
                        composite.getShell().close();
                    }
                }
            });
              
        return composite;
    };
    
    /**
     * Validates the input.
     * <p>
     * The default implementation of this framework method delegates the request
     * to the supplied input validator object; if it finds the input invalid,
     * the error message is displayed in the dialog's message line. This hook
     * method is called whenever the text changes in the input field.
     * </p>
     */
    protected void validateInput() {
        int currentInput = 0;
        
        if (machineField.getText() == null || "".equals(machineField.getText())) {
        
            errorMessage = "Please type in the number of clones";
            messageLabel.setText(errorMessage);
            getButton(OK).setEnabled(false);
        
        } else {
        
            try {
                currentInput = Integer.valueOf(machineField.getText());
            } catch (NumberFormatException exp) {
                /*
                 * It's OK to ignore "exp" here because returning a default value is
                 * the documented behaviour on invalid input.
                 */
            }
   
            if (currentInput < 1) {
                errorMessage = "Error: Your current input " + currentInput
                        + " is less than 1. Please enter a  number between 1 and 50.";
            } else if (currentInput > 50) {
                errorMessage = "Error: Your current input " + currentInput
                        + " is more than 50. Please enter a  number between 1 and 50.";
            } else {
                errorMessage = "";
            }
            
            messageLabel.setText(errorMessage);

            if ("".equals(errorMessage)) {

                //disable OK-button
                getButton(OK).setEnabled(true);
            } else {
                getButton(OK).setEnabled(false);
            }
            
        }
    }

    @Override
    protected Point getIntendedSize() {
        return new Point(700, 150);
    }
     
    /**
     * Save users input.
     */
    private void saveInput() {
        cloneCount = Integer.valueOf(machineField.getText());
    }

    @Override
    protected void okPressed() {
        saveInput();
        super.okPressed();
    }
    
    @Override
    protected String getTitle() {
        return "Clone element";
    }
    
}

