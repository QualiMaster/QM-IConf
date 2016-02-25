package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.equinox.internal.p2.ui.sdk.UpdateHandler;

import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;

/**
 * Handler to enable or disable the P2 Update.
 * 
 * @author Sass
 *
 */
@SuppressWarnings("restriction")
public class P2UpdateHandler extends UpdateHandler {
    
    /**
     * The constructor.
     */
    public P2UpdateHandler() {
        // constructor
        boolean enableUpdate = !ConfigurationProperties.DEMO_MODE.getBooleanValue();
        super.setBaseEnabled(enableUpdate);
    }

}
