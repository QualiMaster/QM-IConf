package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;

/**
 * Handler which opens up the Connect-Dialog.
 * @author Nowatzki
 */
public class DisconnectHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        Infrastructure.disconnect();
        return null; // see API
    }
}
