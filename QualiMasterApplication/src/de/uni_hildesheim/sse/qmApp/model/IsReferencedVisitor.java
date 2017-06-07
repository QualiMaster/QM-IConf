package de.uni_hildesheim.sse.qmApp.model;

import java.util.Iterator;
import java.util.Set;

import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IConfigurationVisitor;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.BlockExpression;
import net.ssehub.easy.varModel.cst.Comment;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.CompoundInitializer;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ContainerInitializer;
import net.ssehub.easy.varModel.cst.ContainerOperationCall;
import net.ssehub.easy.varModel.cst.IConstraintTreeVisitor;
import net.ssehub.easy.varModel.cst.IfThen;
import net.ssehub.easy.varModel.cst.Let;
import net.ssehub.easy.varModel.cst.MultiAndExpression;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Parenthesis;
import net.ssehub.easy.varModel.cst.Self;
import net.ssehub.easy.varModel.cst.UnresolvedExpression;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ConstraintValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.IValueVisitor;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.MetaTypeValue;
import net.ssehub.easy.varModel.model.values.NullValue;
import net.ssehub.easy.varModel.model.values.RealValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.VersionValue;

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
    public void visitAnnotationVariable(AttributeVariable variable) {
        // TODO Check whether specific method is needed
        visitVariable(variable);
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
    public void visitMultiAndExpression(MultiAndExpression expression) {
        for (int e = 0; e < expression.getExpressionCount(); e++) {
            expression.getExpression(e).accept(this);
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
    
    @Override
    public void visitBlockExpression(BlockExpression block) {
        for (int e = 0, n = block.getExpressionCount(); e < n; e++) {
            block.getExpression(e).accept(this);
        }
    }

}
