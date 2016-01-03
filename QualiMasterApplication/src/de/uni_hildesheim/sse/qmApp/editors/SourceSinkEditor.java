package de.uni_hildesheim.sse.qmApp.editors;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;

/**
 * A specialized editor for data sources and data sinks. This editor is not complete
 * as details on the algorithm packages are not agreed upon at the moment.
 * 
 * @author Holger Eichelberger
 */
public class SourceSinkEditor extends AbstractUploadEditor {

    public static final String ID = "de.uni_hildesheim.sse.qmApp.SourceSinkEditor";
    
    @Override
    protected void disableInput() {
    }

    @Override
    protected boolean disableKeyPart() {
        boolean disable = false;
        IDecisionVariable var = getVariable();
        if (null != var) {
            IDatatype varType = var.getDeclaration().getType();
            disable = varType.getName().equals("DataSink"); // not nice
        }
        return disable;
    }

}
