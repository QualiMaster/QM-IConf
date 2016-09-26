package de.uni_hildesheim.sse.qmApp.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Shell;

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
    
    
    private final int baseMax = 500;
    private final int baseMin = 0;
    private final String baseMaxString = "500";
    private final String baseMinString = "0";
      
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
     * Run.
     */
    public void run() {
        setBlockOnOpen(true);
        open();
        //Display.getCurrent().dispose();
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

        //...
        lowToMediumDescription = new Label(composite, SWT.FILL);
        lowToMediumDescription.setLayoutData(data);
        lowToMediumDescription.setText("Low low Medium");
        
        lowToMediumLabel = new Label(composite, SWT.NULL);
        lowToMediumLabel.setText("XXX");
        
        lowToMediumScale = new Scale(composite, SWT.NONE);
        lowToMediumScale.setMaximum(baseMax);
        //lowToMediumScale.setEnabled(false);
        
        lowToMediumLabel2 = new Label(composite, SWT.NULL);
        lowToMediumLabel2.setText(baseMaxString);
        
        //...
        mediumToHighDescription = new Label(composite, SWT.FILL);
        mediumToHighDescription.setLayoutData(data);
        mediumToHighDescription.setText("Medium To High");
        
        mediumToHighLabel = new Label(composite, SWT.NULL);
        mediumToHighLabel.setText("XXX");
        
        mediumToHighScale = new Scale(composite, SWT.NONE);
        mediumToHighScale.setMinimum(baseMin);
        mediumToHighScale.setMaximum(baseMax);
        //mediumToHighScale.setEnabled(false);
        
        mediumToHighLabel2 = new Label(composite, SWT.NULL);
        mediumToHighLabel2.setText(baseMaxString);
        //...
        highToVEryhighDescription = new Label(composite, SWT.FILL);
        highToVEryhighDescription.setLayoutData(data);
        highToVEryhighDescription.setText("High To Very High");
        
        highToVeryhighLabel = new Label(composite, SWT.NULL);
        highToVeryhighLabel.setText("XXX");
        
        highToVeryhighScale =  new Scale(composite, SWT.NONE);
        //highToVeryhighScale.setEnabled(false);
        
        highToVeryhighLabel2 = new Label(composite, SWT.NULL);
        highToVeryhighLabel2.setText(baseMaxString);
        
        addListenersToScales(composite);
        addSavedValuesToScales();
        return composite;
    };
    
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
            
            lowToMediumScale.setMinimum(baseMin);
            lowToMediumScale.setMaximum(baseMax);
            lowToMediumScale.setSelection(lowToMediumInteger);
            
            mediumToHighScale.setMinimum(baseMin);
            mediumToHighScale.setMaximum(baseMax);
            mediumToHighScale.setSelection(mediumToHighInteger);
            
            highToVeryhighScale.setMinimum(baseMin);
            highToVeryhighScale.setMaximum(baseMax);
            highToVeryhighScale.setSelection(highToVeryHighInteger);
            
            lowToMediumLabel.setText(veryLowToLow);
            mediumToHighLabel.setText(lowToMedium);
            highToVeryhighLabel.setText(mediumToHigh);
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
              
                lowToMediumLabel.setText(String.valueOf(perspectiveValue));
                lowToMediumLabel2.setText(baseMaxString);  
                
                lowToMediumScale.setMinimum(perspectiveValue);
                lowToMediumScale.setMaximum(baseMax);
                lowToMediumScale.setSelection(perspectiveValue);
                //lowToMediumScale.setEnabled(true);
                
                verylowToLowLabel2.setText(String.valueOf(perspectiveValue));
            }
        });
        lowToMediumScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = lowToMediumScale.getSelection();
              
                mediumToHighLabel.setText(String.valueOf(perspectiveValue));
                mediumToHighLabel2.setText(baseMaxString);
                
                mediumToHighScale.setMinimum(perspectiveValue);
                mediumToHighScale.setMaximum(baseMax);
                mediumToHighScale.setSelection(perspectiveValue);
                //mediumToHighScale.setEnabled(true);
               
                lowToMediumLabel2.setText(String.valueOf(perspectiveValue));
            }
        });
        mediumToHighScale.addListener(SWT.MouseUp, new Listener() {
            public void handleEvent(Event event) {
                int perspectiveValue = mediumToHighScale.getSelection();
              
                highToVeryhighLabel.setText(String.valueOf(perspectiveValue));
                highToVeryhighLabel2.setText(baseMaxString);
                
                highToVeryhighScale.setMinimum(perspectiveValue);
                highToVeryhighScale.setMaximum(baseMax);
                highToVeryhighScale.setSelection(perspectiveValue);
                //highToVeryhighScale.setEnabled(true);
                
                mediumToHighLabel2.setText(String.valueOf(perspectiveValue));
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