package de.uni_hildesheim.sse.qmApp.commands;

/**
 * The handler for the "instantiate local" command.
 * 
 * @author Holger Eichelberger
 */
public class InstantiateLocal extends AbstractInstantiateLocal {

    /**
     * Creates the instantiate command.
     */
    public InstantiateLocal() {
        setBaseEnabled(true);
    }

    @Override
    protected String getStartRuleName() {
        return null; // use main
    }

    @Override
    protected String getMessage() {
        return "Select location for the generation of all configured source code artifacts";
    }

}
