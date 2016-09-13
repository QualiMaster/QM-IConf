package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for setting the borders concerning the pipeline coloring.
 * The borders from very-low to medium to very high can be set via several sliders.
 * 
 * @author nowatzki
 *
 */
public class PipelineColoringDialog extends Dialog {

    private Label verylowToLowDescription;
    private Label lowToMediumDescription;
    private Label mediumToHighDescription;
    private Label highToVEryhighDescription;
    
    private Label verylowToLowLabel;
    private Scale verylowToLowScale;
    private Label verylowToLowLabel2;
    
    private Label lowToMediumLabel;
    private Scale lowToMediumScale;
    private Label lowToMediumLabel2;
    
    private Label mediumToHighLabel;
    private Scale mediumToHighScale;
    private Label mediumToHighLabel2;
    
    private Label highToVeryhighLabel;
    private Scale highToVeryhighScale;
    private Label highToVeryhighLabel2;
    
    
    private final int baseMax = 400;
    private final int baseMin = 0;
    private final String baseMaxString = "400";
    private final String baseMinString = "0";
    
    @SuppressWarnings("unused")
    private final double multiplicator = 0.8;
    
    /**
     * Constructor invoking super-constructor.
     * @param parentShell the parent shell.
     */
    public PipelineColoringDialog(Shell parentShell) {
        super(parentShell);
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
     * Create the Contents.
     * 
     * @param parent
     *            parent composite.
     * @return composite The parent composite.
     */
    protected Control createDialogArea(Composite parent) {
        
        final Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
       
        verylowToLowDescription = new Label(composite, SWT.FILL);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        verylowToLowDescription.setLayoutData(data);
        verylowToLowDescription.setText("Very low to Low");
        
        verylowToLowLabel = new Label(composite, SWT.NULL);
        verylowToLowLabel.setText(baseMinString);
       
        verylowToLowScale = new Scale(composite, SWT.NONE);
        verylowToLowScale.setMinimum(baseMin);
        verylowToLowScale.setMaximum(baseMax);
       
        verylowToLowLabel2 = new Label(composite, SWT.NULL);
        verylowToLowLabel2.setText(baseMaxString);

        lowToMediumDescription = new Label(composite, SWT.FILL);
        lowToMediumDescription.setLayoutData(data);
        lowToMediumDescription.setText("Low low Medium");
        
        lowToMediumLabel = new Label(composite, SWT.NULL);
        lowToMediumLabel.setText("XXX");
        
        lowToMediumScale = new Scale(composite, SWT.NONE);
        lowToMediumScale.setMaximum(baseMax);
        lowToMediumScale.setEnabled(false);
        
        lowToMediumLabel2 = new Label(composite, SWT.NULL);
        lowToMediumLabel2.setText(baseMaxString);

        mediumToHighDescription = new Label(composite, SWT.FILL);
        mediumToHighDescription.setLayoutData(data);
        mediumToHighDescription.setText("Medium To High");
        
        mediumToHighLabel = new Label(composite, SWT.NULL);
        mediumToHighLabel.setText("XXX");
        
        mediumToHighScale = new Scale(composite, SWT.NONE);
        mediumToHighScale.setMinimum(baseMin);
        mediumToHighScale.setMaximum(baseMax);
        mediumToHighScale.setEnabled(false);
        
        mediumToHighLabel2 = new Label(composite, SWT.NULL);
        mediumToHighLabel2.setText(baseMaxString);
        //...
        highToVEryhighDescription = new Label(composite, SWT.FILL);
        highToVEryhighDescription.setLayoutData(data);
        highToVEryhighDescription.setText("High To Very High");
        
        highToVeryhighLabel = new Label(composite, SWT.NULL);
        highToVeryhighLabel.setText("XXX");
        
        highToVeryhighScale =  new Scale(composite, SWT.NONE);
        highToVeryhighScale.setEnabled(false);
        
        highToVeryhighLabel2 = new Label(composite, SWT.NULL);
        highToVeryhighLabel2.setText(baseMaxString);
        
        addListenersToScales(composite);
        
        return composite;
    };
    
    /**
     * Add the listeners to all scales, thus the user interaction can be processed.
     * @param composite scales parent composite.
     */
    private void addListenersToScales(Composite composite) {

        verylowToLowScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = verylowToLowScale.getSelection();
              
                lowToMediumLabel.setText(String.valueOf(perspectiveValue));
                lowToMediumLabel2.setText(baseMaxString);  
                
                lowToMediumScale.setMinimum(perspectiveValue);
                lowToMediumScale.setMaximum(baseMax);
                lowToMediumScale.setEnabled(true);
            }
        });
        lowToMediumScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = lowToMediumScale.getSelection();
              
                mediumToHighLabel.setText(String.valueOf(perspectiveValue));
                mediumToHighLabel2.setText(baseMaxString);
                
                mediumToHighScale.setMinimum(perspectiveValue);
                mediumToHighScale.setMaximum(baseMax);
                mediumToHighScale.setEnabled(true);
                verylowToLowScale.setEnabled(false);
            }
        });
        mediumToHighScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = mediumToHighScale.getSelection();
              
                highToVeryhighLabel.setText(String.valueOf(perspectiveValue));
                highToVeryhighLabel2.setText(baseMaxString);
                
                highToVeryhighScale.setMinimum(perspectiveValue);
                highToVeryhighScale.setMaximum(baseMax);
                highToVeryhighScale.setEnabled(true);
                lowToMediumScale.setEnabled(false);
            }
        });
        
        highToVeryhighScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                
                mediumToHighScale.setEnabled(false);
            }
        });
    }

    @Override
    protected void okPressed() {
        super.okPressed();
        
        //...
    }
    
//    @Override
//    protected void configureShell(Shell newShell) {
//    
//        newShell.pack();
//        newShell.setSize(800, 500);
//        
//        super.configureShell(newShell);
//        newShell.setText("Pipeline Runtime Properties");
//        DialogsUtil.centerShell(newShell);
//    }
    
}
