package de.uni_hildesheim.sse.qmApp.model;

import java.io.Writer;

import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.persistency.IVMLWriter;

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
