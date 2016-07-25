package de.uni_hildesheim.sse.qmApp.model;

import java.util.ArrayList;
import java.util.List;

import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import eu.qualimaster.easy.extension.modelop.BasicIVMLModelOperations;
import net.ssehub.easy.varModel.confModel.ContainerVariable;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.Value;
import qualimasterapplication.Activator;

/**
 * The methods to operate the IVML model for obtaining IVML elements information.
 * These operations are split into two classes:
 * <ul>
 *   <li>{@link BasicIVMLModelOperations}: Operations without UI interactions</li>
 *   <li>{@link IVMLModelOperations}: Operations UI/Pipeline interactions</li>
 * </ul>
 * 
 * @author Cui Qin
 * @author El-Sharkawy
 *
 */
public class IVMLModelOperations {

    /**
     * Deletes the pipeline element with name in the project.
     * 
     * @param project
     *            the project to delete the pipeline element to
     * @param pipeline
     *            the pipeline element to be deleted
     * @return <code>True</code> if find the right element to delete, <code>false</code> otherwise
     */
    public static boolean deletePipelineElementFromMainProject(Project project, AbstractVariable pipeline) {
        IDatatype type = null;
        IDecisionVariable var = null;
        Value value = null;
        List<Value> values = new ArrayList<Value>();
        String vString = null;
        boolean result = false;
        String pipelineName = pipeline.getName();
        try {
            type = ModelQuery.findType(project, "Pipeline", null);
            var = ModelAccess.findTopContainer(VariabilityModel.Configuration.PIPELINES, type);
            if (null != var) {
                ContainerVariable con = (ContainerVariable) var;
                for (int i = 0; i < con.getNestedElementsCount(); i++) {
                    value = con.getNestedElement(i).getValue();
                    vString = value.toString();
                    vString = vString.substring(0, vString.lastIndexOf(":") - 1);
                    if (!vString.equals(pipelineName)) {
                        values.add(value);
                    }
                }
                ContainableModelElement e = null;
                ContainableModelElement eToChange = null;
                FreezeBlock freezeBlock = null;
                for (int i = 0; i < project.getElementCount(); i++) {
                    e = project.getElement(i);
                    if (null == eToChange && e instanceof Constraint) {
                        if (assignsTo((Constraint) e, "pipelines")) {
                            eToChange = e;
                        }
                    }
                    if (e instanceof FreezeBlock) { // this assumes that there is only one freeze block!
                        freezeBlock = (FreezeBlock) e;
                    }
                }
                boolean freezeOk = null != freezeBlock ? project.removeElement(freezeBlock) : true;
                if (project.removeElement(eToChange) && freezeOk) {
                    Constraint constraint = BasicIVMLModelOperations.getConstraint(values.toArray(),
                        var.getDeclaration(), project);
                    result = project.add(constraint) && project.add(freezeBlock);
                }
            }
        } catch (ModelQueryException e) {
            Activator.getLogger(IVMLModelOperations.class).exception(e);
        }
        return result;
    }
    
    /**
     * Returns whether <code>cstr</code> is an assignment to <code>varName</code>. [ugly]
     * 
     * @param cstr the constraint to test
     * @param varName the variable name to test for
     * @return <code>true</code> if it is the requested assignment, <code>false</code> else
     */
    private static boolean assignsTo(Constraint cstr, String varName) {
        boolean result = false;
        ConstraintSyntaxTree cst = cstr.getConsSyntax();
        if (cst instanceof OCLFeatureCall) {
            OCLFeatureCall fc = (OCLFeatureCall) cst;
            if (fc.getOperation().equals(OclKeyWords.ASSIGNMENT)) {
                ConstraintSyntaxTree operand = fc.getOperand();
                if (operand instanceof Variable) {
                    Variable opVar = (Variable) operand;
                    if (varName.equals(opVar.getVariable().getName())) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Modifies the pipeline elements in the main pipeline project <code>mainProject</code>.
     * 
     * @param pipeline
     *            the pipeline to be added
     */
    public static void addPipelineToMainProject(DecisionVariableDeclaration pipeline) {
        IDecisionVariable pipelineSequence = ModelAccess.findTopContainer(
                PipelineTranslationOperations.getPipelineModelPart(), pipeline.getType());
        String pipelineName = pipeline.getName();
        boolean isNotExisted = true;
        for (int i = 0; i < pipelineSequence.getNestedElementsCount(); i++) {
            String valueString = pipelineSequence.getNestedElement(i).getValue().toString();
            String name = valueString.substring(0, valueString.lastIndexOf(":") - 1);
            if (name.equals(pipelineName)) {
                isNotExisted = false;
            }
        }
        if (isNotExisted) {
            ModelAccess.addPipelineElementWithName(pipelineSequence, pipelineName);
        }
        ChangeManager.INSTANCE.variableChanged(new Object(), pipelineSequence);
    }    
}
