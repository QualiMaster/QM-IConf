package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.EventKind;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager.IChangeListener;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory;
import net.ssehub.easy.producer.ui.productline_editor.DelegatingEasyEditorPage;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import net.ssehub.easy.producer.ui.productline_editor.DelegatingEasyEditorPage.IDirtyListener;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.MandatoryClassifierSettings;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.MandatoryDeclarationClassifier;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.VariableContainer;

/**
 * Provides some abstract model editor capabilities for variability model related editors.
 * Call {@link #setConfiguration(Configuration)} in {@link #init(IEditorSite, IEditorInput)} and
 * override and call {@link #createPartControl(Composite)}.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractVarModelEditor extends EditorPart implements IChangeListener, IDirtyListener {

    private Configuration cfg;
    private UIConfiguration uiCfg;
    private DelegatingEasyEditorPage parent;
    private List<Control> editors = new ArrayList<Control>();
    private boolean enableChangeEventProcessing = true;
    private DirtyListener dirtyListener;
    private VariableContainer importances;

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }
    
    /**
     * The variability importances, i.e., whether variables shall be configured.
     * 
     * @return the importances, may be <b>null</b>
     */
    protected VariableContainer getImportances() {
        return importances;
    }
    
    /**
     * Create a specific editor for an element.
     * 
     * @param var The DecisionVariable wrapping the underlying elements.
     */
    protected void createEditor(IDecisionVariable var) {
        int nCount = var.getNestedElementsCount();
        if (nCount > 0) {
            for (int n = 0; n < nCount; n++) {
                IDecisionVariable nVar = var.getNestedElement(n);
                if (ModelAccess.isVisible(nVar)) {
                    EditorUtils.createLabel(uiCfg, nVar, importances);
                    addEditor(createEditorInstance(nVar));
                }
            }
        } else {
            if (ModelAccess.isVisible(var)) {
                EditorUtils.createLabel(uiCfg, var, importances);
                addEditor(createEditorInstance(var));
            }
        }
    }

    /**
     * Create a specific editor instance for an element.
     * 
     * @param var The DecisionVariable wrapping the underlying elements.
     * @return the created control instance
     */
    protected Control createEditorInstance(IDecisionVariable var) {
        Control control = ConfigurationTableEditorFactory.createEditor(uiCfg, var);
        if (!VariabilityModel.isWritable(var)) {
            control.setEnabled(false);
        }
        control.addKeyListener(dirtyListener);
        if (control instanceof org.eclipse.swt.widgets.List) {
            ((org.eclipse.swt.widgets.List) control).addSelectionListener(dirtyListener);
        }
        if (control instanceof Combo) {
            ((Combo) control).addSelectionListener(dirtyListener);
        }
        if (control instanceof Table) {
            ((Table) control).addSelectionListener(dirtyListener);
        }
        if (control instanceof IDirtyableEditor) {
            ((IDirtyableEditor) control).addDirtyListener(dirtyListener);
        }
        EditorUtils.assignHelpText(var, control);
        return control;
    }
    
    @Override
    public void setFocus() {
        if (!editors.isEmpty()) {
            editors.get(0).setFocus();
        }
    }
    
    @Override
    public void createPartControl(Composite parent) {
        this.parent = new DelegatingEasyEditorPage(parent, this);
        this.dirtyListener = new DirtyListener(this.parent);
        uiCfg = ConfigurationTableEditorFactory.createConfiguration(cfg, getParent(), getUiParameter());
        ChangeManager.INSTANCE.addListener(this);
        
        MandatoryClassifierSettings settings = new MandatoryClassifierSettings();
        settings.setDefaultValueConsideration(false);
        MandatoryDeclarationClassifier finder = new MandatoryDeclarationClassifier(cfg, FilterType.ALL, settings);
        cfg.getProject().accept(finder);
        importances = finder.getImportances();
    }
    
    /**
     * Returns the EASy parent page.
     * 
     * @return the EAS parent page
     * @see #createPartControl(Composite)
     */
    protected DelegatingEasyEditorPage getParent() {
        return parent;
    }
    
    @Override
    public boolean isDirty() {
        return null != parent ? parent.isDirty() : false;
    }
    
    /**
     * Adds an editor component.
     * 
     * @param control the editor control
     */
    protected void addEditor(final Control control) {
        if (null != control) {
            editors.add(control);
            if (control instanceof Text) {
                MenuManager mgr = new MenuManager();
                control.setMenu(mgr.createContextMenu(control));
                mgr.fill(control.getParent());
                mgr.add(new Action("reset configuration value (deconfigure)", null) {

                    @Override
                    public void run() {
                        uiCfg.deconfigure(control);
                    }
                    
                });
            }
        }
    }
        
    /**
     * Returns the number of detail editors in this editor.
     * 
     * @return the number of detail editors
     */
    protected int getEditorCount() {
        return editors.size();
    }
    
    /**
     * Returns the detail editors in this editor.
     * 
     * @param index the 0-based index of the detail editor to return
     * @return the detail editor
     * @throws IndexOutOfBoundsException if <code>index &lt;0 || index &gt;={@link #getEditorCount()}</code>
     */
    protected Control getEditor(int index) {
        return editors.get(index);
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false; // no saveAs
    }

    @Override
    public void doSaveAs() {
        doSave(new NullProgressMonitor());
    }
    
    /**
     * Defines the configuration. Shall be called in {@link #init(IEditorSite, IEditorInput)}.
     * 
     * @param cfg the configuration
     */
    protected void setConfiguration(Configuration cfg) {
        this.cfg = cfg;
    }

    /**
     * Returns the variability model configuration.
     * 
     * @return the configuration
     */
    protected Configuration getConfiguration() {
        return cfg;
    }
    
    /**
     * Returns the UI configuration holding the internal details of 
     * the EASy editors. Valid after {@link #createPartControl(Composite)}.
     * 
     * @return the UI configuration.
     */
    protected UIConfiguration getUIConfiguration() {
        return uiCfg;
    }
    
    @Override
    public void dispose() {
        ChangeManager.INSTANCE.removeListener(this);
        if (null != uiCfg) {
            uiCfg.release();
        }
        super.dispose();
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        enableChangeEventProcessing = false;
        uiCfg.commitValues(ChangeManager.INSTANCE.getUIChangeListener());
        enableChangeEventProcessing = true;
        ModelAccess.store(getConfiguration());
        setPartName(getEditorInput().getName());
        parent.unsetDirty(); // this is dirty listener -> fire property change
    }

    @Override
    public void variableChanged(EventKind kind, IDecisionVariable variable, int globalIndex) {
        if (enableChangeEventProcessing && EventKind.DELETING != kind) {
            AbstractVariable decl = variable.getDeclaration();
            IDatatype type = decl.getType();
            boolean relevant = false;
            // relevant is a compound variable slot of the correct name and type string
            //relevant |= variable.getParent() instanceof CompoundVariable 
            // && VariabilityModel.DISPLAY_NAME_SLOT.equals(decl.getName()) && StringType.TYPE.isAssignableFrom(type);
            // relevant are compounds so that comboboxes are up-to-date
            relevant |= Compound.TYPE.isAssignableFrom(type);
            // relevant are containers so that comboboxes are up-to-date
            relevant |= Container.TYPE.isAssignableFrom(type);
            if (relevant) {
                uiCfg.updateEditor(variable);
            }
        }
    }

    @Override
    public void notifyDirty(boolean dirtyState) {
        firePropertyChange(IEditorPart.PROP_DIRTY);
    }

    /**
     * Returns optional UI parameter for the initialization of the UI configurations. Shall be overridden
     * if more specific parameters shall be used.
     * 
     * @return key-value mapping or <b>null</b> if there are none
     */
    protected Map<UIParameter, Object> getUiParameter() {
        return null;
    }

}
