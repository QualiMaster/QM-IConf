package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;

/**
 * A TabItem which is able to display {@link LightweightSystem}´s. These tabs are used to display 
 * runtime-data as graphs.
 * 
 * @author nowatzki
 *
 */
public class RuntimeGraphTabItem extends CTabItem implements IInfrastructureListener {

    /**
     * Constructs a {@link RuntimeGraphTabItem}.
     * @param parent The parent TabFolder.
     * @param style swt style.
     */
    public RuntimeGraphTabItem(CTabFolder parent, int style) {
        super(parent, style);
    }

    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
        // TODO Auto-generated method stub 
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg) {
        // TODO Auto-generated method stub
    }

}
