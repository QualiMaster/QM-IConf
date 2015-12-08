package de.uni_hildesheim.sse.qmApp.commands;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;

/**
 * An abstract base class for handlers enabling external classes to 
 * change the enabled state.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractConfigurableHandler extends AbstractHandler {

    private static final Map<Class<? extends AbstractConfigurableHandler>, AbstractConfigurableHandler> INSTANCES 
        = new HashMap<Class<? extends AbstractConfigurableHandler>, AbstractConfigurableHandler>();
    
    /**
     * Creates an abstract menu handler and registers the created instance.
     */
    protected AbstractConfigurableHandler() {
        // be careful that only the first instance (created by Eclipse) is considered
        if (!INSTANCES.containsKey(getClass())) {
            INSTANCES.put(getClass(), this);
        }
    }
    
    /**
     * Enables or disables the specified handler.
     * 
     * @param handlerClass the handler to enable or disable
     * @param state the enabled state
     */
    public static void setEnabled(Class<? extends AbstractConfigurableHandler> handlerClass, boolean state) {
        AbstractConfigurableHandler instance = INSTANCES.get(handlerClass);
        if (null != instance) {
            instance.setBaseEnabled(state);
        }
    }
    
}
