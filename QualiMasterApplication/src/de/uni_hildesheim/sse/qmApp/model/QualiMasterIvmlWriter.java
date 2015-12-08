package de.uni_hildesheim.sse.qmApp.model;

import java.io.Writer;

import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.persistency.IVMLWriter;

/**
 * Implements an IVML writer which does not write constraint variables / values. Actually, this is just a workaround.
 * 
 * @author Holger Eichelberger
 */
public class QualiMasterIvmlWriter extends IVMLWriter {

    /**
     * Creates a new IVML writer.
     * 
     * @param writer the output writer
     */
    public QualiMasterIvmlWriter(Writer writer) {
        super(writer);
    }
    
    @Override
    public void visitDecisionVariableDeclaration(DecisionVariableDeclaration decl) {
        if (!ConstraintType.TYPE.isAssignableFrom(decl.getType())) {
            super.visitDecisionVariableDeclaration(decl);
        }
    }
    
    @Override
    protected boolean writeValue(Value value) {
        return ModelAccess.isVisibleType(value.getType());
    }

}
