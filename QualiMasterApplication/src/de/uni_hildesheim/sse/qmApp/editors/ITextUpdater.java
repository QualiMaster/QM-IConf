package de.uni_hildesheim.sse.qmApp.editors;

/**
 * Interface in order to update a {@link Text} message.
 * @author Niko
 *
 */
public interface ITextUpdater {

    /**
     * Update the Text´s message.
     * @param message Message to set in the Textfield.
     */
    public void updateText(String message);
    
    /**
     * Updates the Text's message and pushes the changes into the model.
     * @param message Message to set in the Textfield.
     */
    public void updateTextAndModel(String message);
    
}
