package de.uni_hildesheim.sse.qmApp.pipelineUtils;

/**
 * Interface in order to react on cha ges in the Pipeline-Models.
 * When a node/flow is added/removed one of the stated methods below will be called.
 * 
 * @author Niko Nowatzki
 */
public interface IPipelineEditorListener {

    /**
     * Called when a node is added.
     * @param name The name of the added node.
     */
    public void nodeAdded(String name);
    /**
     * Called when a node is removed.
     * @param name name of the removed node.
     */
    public void nodeRemoved(String name);
    /**
     * Called when a flow is added.
     * @param node1 First node which is connected to the newly created flow.
     * @param node2 Second node which is connected to the newly created flow.
     */
    public void flowAdded(String node1, String node2);
    /**
     * Called when a flow is removed.
     * @param node1 First node which was connected to the removed flow.
     * @param node2 Second node which was connected to the removed flow.
     */
    public void flowRemoved(String node1, String node2);
}
