package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;

/**
 * Handler which disconnects the infrastructure connection.
 * @author Nowatzki
 */
public class DisconnectHandler extends AbstractHandler implements IInfrastructureListener {

    /**
     * Creates a disconnect handler.
     */
    public DisconnectHandler() {
        setBaseEnabled(false);
        if (!ConfigurationProperties.DEMO_MODE.getBooleanValue()) { // do not update in demo mode
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
        Infrastructure.disconnect();
        return null; // see API
    }

    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
        setBaseEnabled(hasConnection);
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage msg) {
    }
    
}
