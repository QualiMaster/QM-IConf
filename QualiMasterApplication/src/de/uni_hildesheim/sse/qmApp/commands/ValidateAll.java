package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;

/**
 * The handler for the "validate all" command.
 * 
 * @author Holger Eichelberger
 */
public class ValidateAll extends AbstractHandler {

    /**
     * Creates the instantiate command.
     */
    public ValidateAll() {
        setBaseEnabled(Reasoning.ENABLED);
    }
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        Reasoning.reasonOn(VariabilityModel.Definition.TOP_LEVEL, true);
        return null;
    }

}
