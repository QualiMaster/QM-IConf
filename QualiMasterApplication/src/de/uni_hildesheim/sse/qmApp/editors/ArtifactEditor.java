package de.uni_hildesheim.sse.qmApp.editors;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.dialogs.MavenArtifactSelectionDialog;

/**
 * The {@link ArtifactEditor} specializes {@link AbstractTextSelectionEditorCreator} triggering 
 * the {@link MavenArtifactSelectionDialog}.
 * 
 * @author Niko
 */
public class ArtifactEditor extends AbstractTextSelectionEditorCreator {

    public static final IEditorCreator CREATOR = new ArtifactEditor();
    
    /**
     * Prevents external creation.
     */
    private ArtifactEditor() {
    }
    
    @Override
    protected void browseButtonSelected(String text, IDecisionVariable context, ITextUpdater updater) {
        MavenArtifactSelectionDialog dlg = new MavenArtifactSelectionDialog(DialogsUtil.getActiveShell(), updater);
        if (null != text) {
            dlg.setInitialTreePath(text);
        }
        dlg.open();
    }

    @Override
    protected boolean isTextEditorEnabled(boolean cell, IDecisionVariable context) {
        return true;
    }
    
    @Override
    protected boolean isBrowseButtonActive(boolean cell, IDecisionVariable context) {
        return MavenArtifactSelectionDialog.isConfigured();
    }
    
}
