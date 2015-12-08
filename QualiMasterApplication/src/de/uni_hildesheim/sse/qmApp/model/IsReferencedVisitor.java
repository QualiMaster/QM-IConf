package de.uni_hildesheim.sse.qmApp.model;

import java.util.Iterator;
import java.util.Set;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IConfigurationVisitor;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.Comment;
import de.uni_hildesheim.sse.model.cst.CompoundAccess;
import de.uni_hildesheim.sse.model.cst.CompoundInitializer;
import de.uni_hildesheim.sse.model.cst.ConstantValue;
import de.uni_hildesheim.sse.model.cst.ContainerInitializer;
import de.uni_hildesheim.sse.model.cst.ContainerOperationCall;
import de.uni_hildesheim.sse.model.cst.IConstraintTreeVisitor;
import de.uni_hildesheim.sse.model.cst.IfThen;
import de.uni_hildesheim.sse.model.cst.Let;
import de.uni_hildesheim.sse.model.cst.OCLFeatureCall;
import de.uni_hildesheim.sse.model.cst.Parenthesis;
import de.uni_hildesheim.sse.model.cst.Self;
import de.uni_hildesheim.sse.model.cst.UnresolvedExpression;
import de.uni_hildesheim.sse.model.cst.Variable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.values.BooleanValue;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.ConstraintValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.EnumValue;
import de.uni_hildesheim.sse.model.varModel.values.IValueVisitor;
import de.uni_hildesheim.sse.model.varModel.values.IntValue;
import de.uni_hildesheim.sse.model.varModel.values.MetaTypeValue;
import de.uni_hildesheim.sse.model.varModel.values.NullValue;
import de.uni_hildesheim.sse.model.varModel.values.RealValue;
import de.uni_hildesheim.sse.model.varModel.values.ReferenceValue;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.VersionValue;

/**
 * Implements a visitor to figure out whether there is at least one reference
 * to a given decision variable.
 * 
 * @author Holger Eichelberger
 */
class IsReferencedVisitor implements IConfigurationVisitor, IValueVisitor, IConstraintTreeVisitor {

    private boolean isReferenced = false;
    private AbstractVariable decl;
    private Set<AbstractVariable> excluded;

    /**
     * Creates a visitor instance.
     * 
     * @param var the variable to search for
     */
    IsReferencedVisitor(IDecisionVariable var) {
        this(var, null);
    }
    
    /**
     * Creates a visitor instance.
     * 
     * @param var the variable to search for
     * @param excluded variable definitions for which shall be excluded from visiting
     */
    IsReferencedVisitor(IDecisionVariable var, Set<AbstractVariable> excluded) {
        decl = var.getDeclaration();
        this.excluded = excluded;
    }
    
    /**
     * Returns whether the given variable is referenced.
     * 
     * @return <code>true</code> if there is at least one reference, <code>false</code> else
     */
    boolean isReferenced() {
        return isReferenced;
    }
    
    /**
     * Visits a configuration.
     * 
     * @param configuration the configuration to visit
     */
    public void visitConfiguration(Configuration configuration) {
        Iterator<IDecisionVariable> variableIterator = configuration.iterator();
        while (!isReferenced && variableIterator.hasNext()) {
            IDecisionVariable var = variableIterator.next();
            if (null == excluded || !excluded.contains(var.getDeclaration())) {
                var.accept(this);
            }
        }
    }
    
    @Override
    public void visitDecisionVariable(IDecisionVariable variable) {
        Value val = variable.getValue();
        if (null != val) {
            val.accept(this);
        }
    }

    @Override
    public void visitConstraintValue(ConstraintValue value) {
        // not relevant here
    }

    @Override
    public void visitEnumValue(EnumValue value) {
        // no problem - no reference
    }

    @Override
    public void visitStringValue(StringValue value) {
        // no problem - no reference
    }

    @Override
    public void visitCompoundValue(CompoundValue value) {
        Compound comp = (Compound) value.getType();
        for (int e = 0; e < comp.getElementCount(); e++) {
            Value val = value.getNestedValue(comp.getElement(e).getName());
            if (null != val) {
                val.accept(this);
            }
        }
    }

    @Override
    public void visitContainerValue(ContainerValue value) {
        for (int e = 0; e < value.getElementSize(); e++) {
            value.getElement(e).accept(this);
        }
    }

    @Override
    public void visitIntValue(IntValue value) {
        // no problem - no reference
    }

    @Override
    public void visitRealValue(RealValue value) {
        // no problem - no reference
    }

    @Override
    public void visitBooleanValue(BooleanValue value) {
        // no problem - no reference
    }

    @Override
    public void visitReferenceValue(ReferenceValue referenceValue) {
        if (null == referenceValue.getValue()) {
            referenceValue.getValueEx().accept(this);
        } else {
            isReferenced |= referenceValue.getValue() == decl;
        }
    }

    @Override
    public void visitMetaTypeValue(MetaTypeValue value) {
        // no problem - no reference
    }

    @Override
    public void visitNullValue(NullValue value) {
        // no problem - no reference
    }

    @Override
    public void visitVersionValue(VersionValue value) {
        // no problem - no reference
    }

    @Override
    public void visitConstantValue(ConstantValue value) {
    }

    @Override
    public void visitVariable(Variable variable) {
        isReferenced |= variable.getVariable() == decl;
    }

    @Override
    public void visitParenthesis(Parenthesis parenthesis) {
        parenthesis.getExpr().accept(this);
    }

    @Override
    public void visitComment(Comment comment) {
    }

    @Override
    public void visitOclFeatureCall(OCLFeatureCall call) {
        if (null != call.getOperand()) {
            call.getOperand().accept(this);
        }
        for (int p = 0; p < call.getParameterCount(); p++) {
            call.getParameter(p).accept(this);
        }
    }

    @Override
    public void visitLet(Let let) {
        let.getInExpression().accept(this);
    }

    @Override
    public void visitIfThen(IfThen ifThen) {
        ifThen.getIfExpr().accept(this);
        if (null != ifThen.getElseExpr()) {
            ifThen.getElseExpr().accept(this);
        }
    }

    @Override
    public void visitContainerOperationCall(ContainerOperationCall call) {
        call.getExpression().accept(this);
        call.getContainer().accept(this);
    }

    @Override
    public void visitCompoundAccess(CompoundAccess access) {
        isReferenced |= access.getResolvedSlot() == decl;
    }

    @Override
    public void visitUnresolvedExpression(UnresolvedExpression expression) {
    }

    @Override
    public void visitCompoundInitializer(CompoundInitializer initializer) {
        for (int e = 0; e < initializer.getExpressionCount(); e++) {
            initializer.getExpression(e).accept(this);
        }
    }

    @Override
    public void visitContainerInitializer(ContainerInitializer initializer) {
        for (int e = 0; e < initializer.getExpressionCount(); e++) {
            initializer.getExpression(e).accept(this);
        }
    }

    @Override
    public void visitSelf(Self self) {
    }

}
