package de.uni_hildesheim.sse.qmApp.commands;

/**
 * The handler for the "instantiate interfaces local" command.
 * 
 * @author Holger Eichelberger
 */
public class InstantiateInterfacesLocal extends AbstractInstantiateLocal {

    /**
     * Creates the instantiate command.
     */
    public InstantiateInterfacesLocal() {
        setBaseEnabled(true);
    }

    @Override
    protected String getStartRuleName() {
        return "interfaces";
    }

    @Override
    protected String getMessage() {
        return "Select location for the generation of programming interfaces for individual algorithms";
    }

}
