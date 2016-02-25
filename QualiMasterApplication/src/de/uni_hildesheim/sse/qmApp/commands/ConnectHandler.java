package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.ConnectDialog;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;

/**
 * Handler which opens up the Connect-Dialog.
 * @author Nowatzki
 */
public class ConnectHandler extends AbstractHandler implements IInfrastructureListener {

    /**
     * Creates a new connect handler.
     */
    public ConnectHandler() {
        boolean demo = ConfigurationProperties.DEMO_MODE.getBooleanValue();
        setBaseEnabled(!demo);
        if (!demo) { // do not update in demo mode
            Infrastructure.registerListener(this);
        }
    }
    
    @Override
    public void dispose() {
        super.dispose();
        Infrastructure.unregisterListener(this);
    };
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ConnectDialog dlg = new ConnectDialog(PlatformUI.getWorkbench().getDisplay().getActiveShell());
        dlg.open();
        return null; // see API
    }
    
    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
        setBaseEnabled(!hasConnection);
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg) {
    }

}
