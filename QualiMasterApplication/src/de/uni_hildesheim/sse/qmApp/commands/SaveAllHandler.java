package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

/**
 * The handler for the "Save all" command.
 * 
 * @author Holger Eichelberger
 */
public class SaveAllHandler extends AbstractHandler {

    /**
     * Creates the handler.
     */
    public SaveAllHandler() {
        setBaseEnabled(true);
    }
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        return null;
    }

}
