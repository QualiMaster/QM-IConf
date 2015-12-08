package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy.ui.productline_editor.DelegatingEasyEditorPage;
import de.uni_hildesheim.sse.model.confModel.Configuration;

/**
 * This class is responsible for providing Editors for given elements which are shown in the 
 * {@link ConfigurableElementsView}. Requires {@link VarModelEditorInput} as input.
 * 
 * @author Niko Nowatzki
 * @author Holger Eichelberger
 */
public class ConfigurationEditor extends EditorPart {

    private Configuration cfg;
    private DelegatingEasyEditorPage parent;
    private TreeViewer treeViewer;
    private VarModelEditorInput input;
    
    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        if (input instanceof VarModelEditorInput) {
            this.input = (VarModelEditorInput) input;
            cfg = this.input.getConfiguration();
        } else {
            throw new PartInitException("wrong editor input");
        }
    }

    @Override
    public boolean isDirty() {
        return null != parent ? parent.isDirty() : false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    @Override
    public void createPartControl(Composite parent) {
        this.parent = new DelegatingEasyEditorPage(parent);
        treeViewer = ConfigurationTableEditorFactory.createConfigurationTableEditor(cfg, this.parent);
        treeViewer.getTree().setEnabled(false);
        setPartName(input.getName());
    }

    @Override
    public void setFocus() {
        if (null != treeViewer) {
            treeViewer.getTree().setFocus();
        }
    }

}
