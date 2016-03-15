
package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolTip;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import de.uni_hildesheim.sse.easy.ui.productline_editor.IRefreshableEditor;
import de.uni_hildesheim.sse.easy_producer.instantiator.Bundle;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.modelManagement.IModelListener;

/**
 * This class is responsible for providing Editors for given elements which are shown in the 
 * {@link de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElementsView}.
 * Requires {@link DecisionVariableEditorInput} as input.
 * 
 * @author Niko Nowatzki
 * @author Holger Eichelberger
 */
public class VariableEditor extends AbstractVarModelEditor implements IModelListener<Project> {

    public static final String ID = "de.uni_hildesheim.sse.qmApp.VariableEditor";
    
    private static final String COMPOSITE_STRING = "class org.eclipse.swt.widgets.Composite";
    private static final int MARGIN_RIGHT = 50;
    
    //minimum width. If the ScrolledComposites width violates this width, the scrollbar appears.
    //If ScrolledComposites width is greater than the minimum width, scrollbar is deactivated.
    private static final int MIN_WIDTH = 600;
    
    private HashMap<Control, ControlDecoration> flawedControls = new HashMap<Control, ControlDecoration>();
    private ScrolledComposite scroll;
    private Composite inner;
    
    private DecisionVariableEditorInput input;
    private IDecisionVariable var;
    
    private List<ToolTip> tooltipList = new ArrayList<ToolTip>();
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if (input instanceof DecisionVariableEditorInput) {
            this.input = (DecisionVariableEditorInput) input;
            var = this.input.getVariable();
            if (null != var) {
                VarModel.INSTANCE.events().addModelListener(var.getConfiguration().getProject(), this);
                setConfiguration(var.getConfiguration());
            } else {
                throw new PartInitException("Configurable element does not exist anymore. This may occur when this "
                    + "editor was open, QM-IConf was closed and stored the editor state, the model was changed outside "
                    + "QM-IConf, e.g., by a SVN revert, and then QM-IConf was then started again. Please close the "
                    + "editor.");
            }
        } else {
            throw new PartInitException("Implementation problem. Wrong editor input instance.");
        }
    }
    
    @Override
    public void dispose() {
        if (null != var) {
            VarModel.INSTANCE.events().removeModelListener(var.getConfiguration().getProject(), this);
        }
        for (Control key: flawedControls.keySet()) {
            ControlDecoration decoration = flawedControls.get(key);
            decoration.hide();
            decoration.dispose();
        }
        flawedControls.clear();
        super.dispose();
    }
    
    /**
     * Called by {@link #createPartControl(Composite)}.
     * 
     * @param parent the parent composite to add elements to (here, a two-colum grid layout composite)
     */
    protected void createAdditionalControls(Composite parent) {
    }
    
    @Override
    public final void createPartControl(Composite parent) {
    
        scroll = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        
        //Set expandHorizontal to true, thus the layout will work. Take up horizontal space.
        scroll.setExpandHorizontal(true);
        
        inner = new Composite(scroll, SWT.NONE);
        scroll.setContent(inner);
        super.createPartControl(inner);
        
        //GridLayout for the editor with two columns
        GridLayout layout = new GridLayout(2, false);
        
        //Set the margin thus the textfields wont fill completely. 
        //Thus error-decorators can be shown.
        layout.marginRight = MARGIN_RIGHT;
        
        inner.setLayout(layout);
        
        if (null != var) { // may not be there, e.g., in case of the wrong model
            createEditor(var);
        }
        setPartName(input.getName());
        ImageDescriptor iDesc = input.getImageDescriptor();
        if (null != iDesc) {
            setTitleImage(iDesc.createImage());
        }

        collectTextFields(inner);
        applyGridData(inner);
        considerReasoningResults(inner);
        Point p = inner.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        inner.setSize(p);
        inner.layout();
             
        scroll.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent exc) {

                //Set minimum width in order to preserve possibility of scrollbar
                scroll.setMinSize(MIN_WIDTH, SWT.DEFAULT);
                
            }
        });

        createAdditionalControls(inner);
       
    }

    /**
     * Refresh the {@link ScrolledComposite}.
     */
    public void refreshEditor() {

        if (scroll != null) {
            if (!scroll.isDisposed()) {
                considerReasoningResults(inner);
                scroll.layout();
                scroll.redraw();
            }
        }
    }
    
    /**
     * Refreshes all nested editors.
     */
    public void refreshNestedEditors() {
        for (int j = 0; j < getEditorCount(); j++) {
            Control editor = getEditor(j);
            if (editor instanceof IRefreshableEditor) {
                ((IRefreshableEditor) editor).refresh();
            }
        }
    }
    
    /**
     * Go through the {@link Text}�s and pair them with a control-Decoration which can be hidden or not.
     * @param ctrl parent control.
     */
    private void collectTextFields(Control ctrl) {

        if (!(ctrl instanceof Composite)) {
            final ControlDecoration decorator = new ControlDecoration(ctrl, SWT.RIGHT);
            Image img = IconManager.retrieveImage(IconManager.ERROR);
            decorator.setImage(img);
            decorator.hide();
            flawedControls.put(ctrl, decorator);
        }
        if (ctrl instanceof Composite) {
            Composite comp = (Composite) ctrl;
            
            for (Control control : comp.getChildren()) {

                if (control instanceof Table) {
                    final ControlDecoration tableDecorator = new ControlDecoration(control, SWT.RIGHT);
                    Image img = IconManager.retrieveImage(IconManager.ERROR);
                    tableDecorator.setImage(img);
                    tableDecorator.hide();
                    flawedControls.put(control, tableDecorator);
                }
                if (control instanceof Combo) {
                    final ControlDecoration comboDecorator = new ControlDecoration(control, SWT.RIGHT);
                    Image img = IconManager.retrieveImage(IconManager.ERROR);
                    comboDecorator.setImage(img);
                    comboDecorator.hide();
                    flawedControls.put(control, comboDecorator);
                }
                if (!control.getClass().toString().equals(COMPOSITE_STRING)) {
                    collectTextFields(control);
                }
            }
        }
    }
    
    /**
     * Start reasoning for this editor.
     * @param parent editors parent.
     */
    private void considerReasoningResults(Control parent) {
        // Reset all decorators -> set invisible
        for (ControlDecoration decorator : flawedControls.values()) {
            decorator.hide();
        }
        
        //Remove all tooltips
        for (ToolTip tip : tooltipList) {
            tip.dispose();
        }

        //Get map which contains errors(variables and messages)
        HashMap<IDecisionVariable, String> errors = Reasoning.getErrors();
        Set<IDecisionVariable> keys = errors.keySet();
 
        if (keys != null && !keys.isEmpty()) {
        
            for (IDecisionVariable variable : keys) {

                Control failedControl = getUIConfiguration().getEditorFor(variable);
                         
                if (failedControl != null) {
                    final String errorMessage = errors.get(variable);
                    ControlDecoration declaration = flawedControls.get(failedControl);
                    if (null != declaration) {
                        declaration.show();
    
                        Shell shell = this.getParent().getContentPane().getShell();
                        final ToolTip tip = new ToolTip(shell, SWT.BALLOON);
                        tip.setMessage(errorMessage);
                        tip.setAutoHide(false);
                            
                        failedControl.addListener(SWT.MouseHover, new Listener() {
                              
                            public void handleEvent(Event exc) {
                                //Set tooltip
                                if (!tip.isDisposed()) {
                                    tip.setVisible(true); 
                                }
                            }
                        });  
                        tooltipList.add(tip);
                        
                    }
                }
            }
        }
    }
    
    /**
     * Sets the griddata for Textfields. Instances of {@link IFixedLayout} will not be treated in this method.
     * 
     * @param ctrl The control to apply the grid data to.
     */
    private void applyGridData(Control ctrl) {
        if (!(ctrl instanceof IFixedLayout)) {
           
            if (ctrl instanceof Text || ctrl instanceof Combo || ctrl instanceof IQMEditor) {
                ctrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            } else if (ctrl instanceof Composite) {
                Composite comp = (Composite) ctrl;
                
                for (Control control : comp.getChildren()) {

                    if (control instanceof Table) {

                        Table table = (Table) control;

                        int itemHeight = table.getItemHeight();
                        final int itemCount = 4;

                        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
                        // Determine size for perfect fit
                        int proposedHeight = itemCount * itemHeight;

                        if (proposedHeight < itemCount * itemHeight) {
                            data.heightHint = proposedHeight;
                        } else {
                            // But if max height is violated, set set the height
                            // to a fixed value
                            data.heightHint = itemCount * itemHeight;
                        }
                        table.setLayoutData(data);
                    }
                    if (!control.getClass().toString().equals(COMPOSITE_STRING)) {
                        applyGridData(control);
                    }
                }
            }
        }
    }
    
    /**
     * Returns the actual variable processed by this editor.
     * 
     * @return the variable
     */
    protected IDecisionVariable getVariable() {
        return var;
    }

    @Override
    public void notifyReplaced(Project oldModel, Project newModel) {
        IDecisionVariable newVar = Configuration.mapVariable(var, var.getConfiguration());
        if (null != newVar) {
            var = newVar;
            setConfiguration(var.getConfiguration());
        } else {
            EASyLoggerFactory.INSTANCE.getLogger(getClass(), Bundle.ID)
                .error("No variable found in new configuration, i.e., discontinued mapping!");
        }
    }
}
