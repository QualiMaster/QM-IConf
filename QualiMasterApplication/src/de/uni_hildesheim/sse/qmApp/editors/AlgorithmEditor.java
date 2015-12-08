package de.uni_hildesheim.sse.qmApp.editors;

/**
 * A specialized editor for algorithms. This editor is not complete
 * as details on the algorithm packages are not agreed upon at the moment.
 * 
 * @author Holger Eichelberger
 */
public class AlgorithmEditor extends AbstractUploadEditor {

    public static final String ID = "de.uni_hildesheim.sse.qmApp.AlgorithmEditor";

    @Override
    protected boolean disableKeyPart() {
        return true;
    }
    
}
