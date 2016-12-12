package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;

import eu.qualimaster.easy.extension.QmObservables;

/**
 * Dialog for setting borders from very low to low to medium to high to very high.
 * 
 * @author nowatzki
 */
public class PipelineColoringDialog extends Dialog {

    public static final String VERYLOW_TO_LOW_DES = "veryLowToLow";
    public static final String LOW_TO_MEDIUM_DES = "lowToMedium";
    public static final String MEDIUM_TO_HIGH_DES = "mediumToHigh";
    public static final String HIGH_TO_VERYHIGH_DES = "highToVeryhigh";
    
    private static String veryLowToLowValue = "";
    private static String lowToMediumValue = "";
    private static String mediumToHighValue = "";
    private static String highToVeryhighValue = "";
    
    private Label descriptionLabel;
    
    private Label verylowToLowDescription;
    private Label lowToMediumDescription;
    private Label mediumToHighDescription;
    private Label highToVEryhighDescription;
    
    private Spinner verylowToLowLabel;
    private Scale verylowToLowScale;
    private Spinner verylowToLowLabel2;
    
    private Spinner lowToMediumLabel;
    private Scale lowToMediumScale;
    private Spinner lowToMediumLabel2;
    
    private Spinner mediumToHighLabel;
    private Scale mediumToHighScale;
    private Spinner mediumToHighLabel2;
    
    private Spinner highToVeryhighLabel;
    private Scale highToVeryhighScale;
    private Spinner highToVeryhighLabel2;
     
    private final int baseMax = 500;
    private final int baseMin = 0;
      
    @SuppressWarnings("unused")
    private final double multiplicator = 0.8;
    
    /**
     * Sole constructor.
     * @param parentShell parent shell.
     */
    public PipelineColoringDialog(Shell parentShell) {
        super(parentShell);
    }

    /**
     * Set the dialog title.
     * @param shell The dialogs shell.
     */
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Color Selection for Pipelines");
    }
    
    /**
     * Run.
     */
    public void run() {
        setBlockOnOpen(true);
        open();
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
        descriptionLabel = new Label(composite, SWT.FILL);
        GridData data = new GridData();
        data.horizontalSpan = 3;
        descriptionLabel.setLayoutData(data);
        descriptionLabel.setText("The Observable: " + QmObservables.SCALABILITY_ITEMS
                + " is monitored" + "\n" + "with the max-value of: " + baseMax);
        
        verylowToLowDescription = new Label(composite, SWT.FILL);
        data = new GridData();
        data.horizontalSpan = 3;
        verylowToLowDescription.setLayoutData(data);
        verylowToLowDescription.setText("Very low to Low");
        
        verylowToLowLabel = new Spinner(composite, SWT.NULL);
        verylowToLowLabel.setMinimum(baseMin);
        verylowToLowLabel.setMaximum(baseMax);
        verylowToLowLabel.setSelection(baseMin);
        verylowToLowLabel.setEnabled(false);
        
        verylowToLowScale = new Scale(composite, SWT.NONE);
        verylowToLowScale.setMinimum(baseMin);
        verylowToLowScale.setMaximum(baseMax);

        verylowToLowScale.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(final MouseEvent evt) {
                
                Scale src = (Scale) evt.getSource();
                int sel = src.getSelection();
                src.setSelection(sel + evt.count);
                
                verylowToLowLabel2.setSelection(sel);
                lowToMediumLabel.setSelection(sel);

                lowToMediumLabel2.setMinimum(sel);
                
                lowToMediumScale.setMinimum(sel);
                lowToMediumScale.setSelection(sel);
            }
        });
        
        verylowToLowLabel2 = new Spinner(composite, SWT.NULL);
        verylowToLowLabel2.setMinimum(baseMin);
        verylowToLowLabel2.setMaximum(baseMax);
        verylowToLowLabel2.setSelection(baseMax);
        //...
        lowToMediumDescription = new Label(composite, SWT.FILL);
        lowToMediumDescription.setLayoutData(data);
        lowToMediumDescription.setText("Low to Medium");
        
        lowToMediumLabel = new Spinner(composite, SWT.NULL);
        lowToMediumLabel.setMinimum(baseMin);
        lowToMediumLabel.setMaximum(baseMax);
        lowToMediumLabel.setSelection(baseMin);
        
        lowToMediumScale = new Scale(composite, SWT.NONE);
        //lowToMediumScale.setMaximum(baseMax);
        
        createAdditionalUI(composite);
        return composite;
    };
    
    /**
     * Create more UI-Components for this dialog like scales and Spinners.
     * @param composite parent Composite.
     */
    private void createAdditionalUI(Composite composite) {
        GridData data = new GridData();
        data.horizontalSpan = 3;
        
        lowToMediumScale.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(final MouseEvent evt) {
                
                Scale src = (Scale) evt.getSource();
                int sel = src.getSelection();
                src.setSelection(sel + evt.count);

                
                lowToMediumLabel2.setSelection(sel);
                mediumToHighLabel.setSelection(sel);
                mediumToHighLabel2.setMinimum(sel);
                
                mediumToHighScale.setMinimum(sel);
                mediumToHighScale.setSelection(sel);
                
            }
        });
        
        lowToMediumLabel2 = new Spinner(composite, SWT.NULL);
        lowToMediumLabel2.setMinimum(baseMin);
        lowToMediumLabel2.setMaximum(baseMax);
        lowToMediumLabel2.setSelection(baseMax);
       
        //...
        mediumToHighDescription = new Label(composite, SWT.FILL);
        mediumToHighDescription.setLayoutData(data);
        mediumToHighDescription.setText("Medium To High");
        
        mediumToHighLabel = new Spinner(composite, SWT.NULL);
        mediumToHighLabel.setMinimum(baseMin);
        mediumToHighLabel.setMaximum(baseMax);
        mediumToHighLabel.setSelection(baseMin);
        
        mediumToHighScale = new Scale(composite, SWT.NONE);
        mediumToHighScale.setMinimum(baseMin);
        mediumToHighScale.setMaximum(baseMax);
        
        createAdditionalUIComponents2(composite);
        
    }
    /**
     Create more UI-Components for this dialog like scales and Spinners.
     * @param composite parent Composite.
     */
    private void createAdditionalUIComponents2(Composite composite) {
        
        GridData data = new GridData();
        data.horizontalSpan = 3;
        
        mediumToHighScale.addMouseWheelListener(new MouseWheelListener() {
            public void mouseScrolled(final MouseEvent evt) {
                
                Scale src = (Scale) evt.getSource();
                int sel = src.getSelection();
                src.setSelection(sel + evt.count);
                
                mediumToHighLabel2.setSelection(sel);
                highToVeryhighLabel.setSelection(sel);
                //highToVeryhighLabel2.setMinimum(sel);
                
                highToVeryhighScale.setMinimum(sel);
                highToVeryhighScale.setSelection(sel);
            }
        });
        
        mediumToHighLabel2 = new Spinner(composite, SWT.NULL);
        mediumToHighLabel2.setMinimum(baseMin);
        mediumToHighLabel2.setMaximum(baseMax);
        mediumToHighLabel2.setSelection(baseMax);
        //...
        highToVEryhighDescription = new Label(composite, SWT.FILL);
        highToVEryhighDescription.setLayoutData(data);
        highToVEryhighDescription.setText("High To Very High");
        
        highToVeryhighLabel = new Spinner(composite, SWT.NULL);
        highToVeryhighLabel.setMinimum(baseMin);
        highToVeryhighLabel.setMaximum(baseMax);
        highToVeryhighLabel.setSelection(baseMin);
        
        highToVeryhighScale =  new Scale(composite, SWT.NONE);
        
        highToVeryhighScale.setEnabled(false);
        highToVeryhighLabel2 = new Spinner(composite, SWT.NULL);
        highToVeryhighLabel2.setMinimum(baseMin);
        highToVeryhighLabel2.setMaximum(baseMax);
        highToVeryhighLabel2.setSelection(baseMax);
        highToVeryhighLabel2.setEnabled(false);
        
        addSpinnerListeners();
        addListenersToScales(composite);
        addSavedValuesToScales(); 
    }

    /**
     * A listeners to the {@Spinner}s so the scale-selections can be adjusted.
     */
    private void addSpinnerListeners() { 
        verylowToLowLabel2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = verylowToLowLabel2.getSelection();
              
                verylowToLowScale.setSelection(selection);
                lowToMediumLabel.setSelection(selection);
                
                //lowToMediumScale.setMinimum(selection);
                lowToMediumScale.setSelection(selection);
                //mediumToHighScale.setMinimum(selection);
                mediumToHighScale.setSelection(selection);
                //highToVeryhighScale.setMinimum(selection);
                highToVeryhighScale.setSelection(selection);
                //lowToMediumLabel2.setMinimum(selection);
            }
        });
        lowToMediumLabel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = lowToMediumLabel.getSelection();
              
                //lowToMediumLabel2.setMinimum(selection);
                verylowToLowScale.setSelection(selection);
                //verylowToLowLabel2.setSelection(selection);
                
                if (lowToMediumLabel2.getSelection() >= mediumToHighLabel.getSelection()) {
                    mediumToHighLabel.setSelection(selection);
                }
            }
        });
        lowToMediumLabel2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = lowToMediumLabel2.getSelection();
              
                lowToMediumScale.setSelection(selection);
                mediumToHighLabel.setSelection(selection);

                mediumToHighScale.setMinimum(selection);
                mediumToHighScale.setSelection(selection);
                highToVeryhighScale.setMinimum(selection);
                highToVeryhighScale.setSelection(selection);
                mediumToHighLabel2.setMinimum(selection);
            }
        });
        addMoreSpinnerListeners();
    }

    /**
     * Create more listeners for the needed Spinners.
     */
    private void addMoreSpinnerListeners() {
        mediumToHighLabel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = mediumToHighLabel.getSelection();
              
                mediumToHighLabel2.setMinimum(selection);
                lowToMediumLabel2.setSelection(selection);
                lowToMediumScale.setSelection(selection);
                
                if (mediumToHighLabel2.getSelection() >= highToVeryhighLabel.getSelection()) {
                    highToVeryhighLabel.setSelection(selection);
                }
            }
        });
        mediumToHighLabel2.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = mediumToHighLabel2.getSelection();
              
                mediumToHighScale.setSelection(selection);
                highToVeryhighLabel.setSelection(selection);

                highToVeryhighScale.setMinimum(selection);
                highToVeryhighScale.setSelection(selection);
                highToVeryhighLabel2.setMinimum(selection);
            }
        });
        highToVeryhighLabel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent evt) {
                int selection = highToVeryhighLabel.getSelection();
              
                highToVeryhighLabel2.setMinimum(selection);
                mediumToHighLabel2.setSelection(selection);
                highToVeryhighScale.setSelection(selection);
            }
        });
        
    }

    /**
     * Use previously saved values for the scales.
     */
    private void addSavedValuesToScales() {
       
        String veryLowToLow = EclipsePrefUtils.INSTANCE.getPreference(PipelineColoringDialog.VERYLOW_TO_LOW_DES);
            
        String lowToMedium = EclipsePrefUtils.INSTANCE.getPreference(PipelineColoringDialog.LOW_TO_MEDIUM_DES);
            
        String mediumToHigh = EclipsePrefUtils.INSTANCE.getPreference(PipelineColoringDialog.MEDIUM_TO_HIGH_DES);
    
        String highToVeryHigh = EclipsePrefUtils.INSTANCE.getPreference(PipelineColoringDialog.HIGH_TO_VERYHIGH_DES);
        
        if (veryLowToLow != null && lowToMedium != null && mediumToHigh != null && highToVeryHigh != null) {
            
            int veryLowToLowInteger = Integer.valueOf(veryLowToLow);
            int lowToMediumInteger = Integer.valueOf(lowToMedium);
            int mediumToHighInteger = Integer.valueOf(mediumToHigh);
            int highToVeryHighInteger = Integer.valueOf(highToVeryHigh);
            
            verylowToLowScale.setMinimum(baseMin);
            verylowToLowScale.setMaximum(baseMax);
            verylowToLowScale.setSelection(veryLowToLowInteger);
            verylowToLowLabel2.setSelection(veryLowToLowInteger);
            
            lowToMediumScale.setMinimum(baseMin);
            lowToMediumScale.setMaximum(baseMax);
            lowToMediumScale.setSelection(lowToMediumInteger);
            lowToMediumLabel2.setSelection(lowToMediumInteger);
            
            mediumToHighScale.setMinimum(baseMin);
            mediumToHighScale.setMaximum(baseMax);
            mediumToHighScale.setSelection(mediumToHighInteger);
            mediumToHighLabel2.setSelection(mediumToHighInteger);
            
            highToVeryhighScale.setMinimum(baseMin);
            highToVeryhighScale.setMaximum(baseMax);
            highToVeryhighScale.setSelection(highToVeryHighInteger);
            highToVeryhighLabel2.setSelection(baseMax);
            
            lowToMediumLabel.setSelection(Integer.valueOf(veryLowToLow));
            mediumToHighLabel.setSelection(Integer.valueOf(lowToMedium));
            highToVeryhighLabel.setSelection(Integer.valueOf(mediumToHigh));
        }
        
    }

    /**
     * Add the MouseUp-Listeners in oder to disable already used scales.
     * @param composite The parent composite.
     */
    private void addListenersToScales(Composite composite) {
        

        verylowToLowScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = verylowToLowScale.getSelection();
              
                lowToMediumLabel.setSelection(perspectiveValue);
                lowToMediumLabel2.setMaximum(baseMax);  
                
                lowToMediumScale.setMinimum(perspectiveValue);
                lowToMediumScale.setMaximum(baseMax);
                lowToMediumScale.setSelection(perspectiveValue);
                //lowToMediumScale.setEnabled(true);
                
                verylowToLowLabel2.setSelection(perspectiveValue);
            }
        });
        lowToMediumScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = lowToMediumScale.getSelection();
              
                mediumToHighLabel.setSelection(perspectiveValue);
                mediumToHighLabel2.setMaximum(baseMax);
                
                mediumToHighScale.setMinimum(perspectiveValue);
                mediumToHighScale.setMaximum(baseMax);
                mediumToHighScale.setSelection(perspectiveValue);
                //mediumToHighScale.setEnabled(true);
               
                lowToMediumLabel2.setSelection(perspectiveValue);
            }
        });
        mediumToHighScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = mediumToHighScale.getSelection();
              
                highToVeryhighLabel.setSelection(perspectiveValue);
                highToVeryhighLabel2.setSelection(baseMax);
                
                highToVeryhighScale.setMinimum(perspectiveValue);
                highToVeryhighScale.setMaximum(baseMax);
                highToVeryhighScale.setSelection(perspectiveValue);
                //highToVeryhighScale.setEnabled(true);
                
                mediumToHighLabel2.setSelection(perspectiveValue);
            }
        });
    }

    @Override
    protected void okPressed() {
        
        veryLowToLowValue = String.valueOf(verylowToLowScale.getSelection());
        lowToMediumValue = String.valueOf(lowToMediumScale.getSelection());
        mediumToHighValue = String.valueOf(mediumToHighScale.getSelection());
        highToVeryhighValue = String.valueOf(highToVeryhighScale.getSelection());
        
        EclipsePrefUtils.INSTANCE.addPreference(VERYLOW_TO_LOW_DES, veryLowToLowValue);
            
        EclipsePrefUtils.INSTANCE.addPreference(LOW_TO_MEDIUM_DES, lowToMediumValue);

        EclipsePrefUtils.INSTANCE.addPreference(MEDIUM_TO_HIGH_DES, mediumToHighValue);

        EclipsePrefUtils.INSTANCE.addPreference(HIGH_TO_VERYHIGH_DES, highToVeryhighValue);
        
        super.okPressed();
    }  
}