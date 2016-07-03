package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.io.Serializable;

/**
 * Wrapper class for describing combinations of an pipeline-element and a observable.
 * Both are represented by a string.
 * @author Niko nowatzki
 *
 */
public class PipelineElementObservableWrapper implements Serializable {

    private static final long serialVersionUID = 364112969961459338L;
    private String elementName;
    private String observableName;
    
    /**
     * Constructs a wrapper instance which holds the elements and observables name.
     * @param elementName name of the element.
     * @param observableName name of the observable.
     */
    public PipelineElementObservableWrapper(String elementName, String observableName) {
        this.elementName = elementName;
        this.observableName = observableName;
    }
    
    /**
     * Get the wrappers name.
     * @return elementName elements name.
     */
    public String getName() {
        return elementName;
    }
    
    /**
     * Get the wrappers observable.
     * @return observableName observables name.
     */
    public String getObservable() {
        return observableName;
    }
}
