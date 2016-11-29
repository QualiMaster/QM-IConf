package de.uni_hildesheim.sse.qmApp.editors;

import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.dialogs.MavenArtifactSelectionDialog;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;

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
        MavenArtifactSelectionDialog dlg = 
                new MavenArtifactSelectionDialog(DialogsUtil.getActiveShell(), updater, context);
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
