package de.uni_hildesheim.sse.qmApp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.AttributeVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.cst.ConstantValue;
import de.uni_hildesheim.sse.model.cst.ConstraintSyntaxTree;
import de.uni_hildesheim.sse.model.cst.OCLFeatureCall;
import de.uni_hildesheim.sse.model.cst.Variable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.Attribute;
import de.uni_hildesheim.sse.model.varModel.Constraint;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.FreezeBlock;
import de.uni_hildesheim.sse.model.varModel.IFreezable;
import de.uni_hildesheim.sse.model.varModel.IModelElement;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.EnumLiteral;
import de.uni_hildesheim.sse.model.varModel.datatypes.FreezeVariableType;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.OclKeyWords;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.values.ReferenceValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.qmApp.treeView.ChangeManager;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;
import eu.qualimaster.easy.extension.QmConstants;

/**
 * The methods to operate the IVML model for obtaining IVML elements information.
 * 
 * @author Cui Qin
 * 
 *
 */
public class IVMLModelOperations {

    public static final String ADD = "add";
    public static final String DEL = "del";

    /**
     * Returns a variable declaration by the name searching in a specific project.
     * 
     * @param source
     *            the project where search for the variable
     * @param variableName
     *            the name of the searching variable
     * @param suffix
     *            the suffix for the given variable name
     * @param destination
     *            the project where to add the variable
     * @return a decision variable or null
     */
    public static DecisionVariableDeclaration getDecisionVariable(Project source, String variableName, String suffix,
            Project destination) {

        DecisionVariableDeclaration decisionVariable = null;
        String projectName = destination.getName();
        String givenName = projectName.substring(0, projectName.indexOf(QmConstants.CFG_POSTFIX));
        if (suffix != null) {
            givenName += "_" + variableName + suffix;
        }
        try {
            IDatatype dataType = ModelQuery.findType(source, variableName, null);
            decisionVariable = new DecisionVariableDeclaration(givenName, dataType, destination);
        } catch (ModelQueryException e) {
            getLogger().exception(e);
        }
        return decisionVariable;
    }
    
    /**
     * Return a ConstraintSyntayTree for Constraint setting.
     * @param value 
     *          the value related to the given IDatatype 
     * @param declarationVariable 
     *          the variable declaration
     * @return a ConstraintSyntayTree for Constraint setting
     */
    public static ConstraintSyntaxTree obtainAssignment(Value value, AbstractVariable declarationVariable) {
        ConstraintSyntaxTree assignment = new OCLFeatureCall(new Variable(declarationVariable),
                OclKeyWords.ASSIGNMENT, new ConstantValue(value));
        return assignment;
    }
    /**
     * Returns a constraint for the compound structure element.
     * 
     * @param objectValue
     *            the value for the compound element
     * @param declarationVariable
     *            the variable the constraint operates on
     * @param parent
     *            the object where the constraint is included
     * @return a tree-structured constraint
     */
    public static Constraint getConstraint(Object[] objectValue, AbstractVariable declarationVariable,
            IModelElement parent) {
        Constraint constraint = new Constraint(parent);
        try {
            Value value = ValueFactory.createValue(declarationVariable.getType(), objectValue);           
            constraint.setConsSyntax(obtainAssignment(value, declarationVariable));
        } catch (ValueDoesNotMatchTypeException e) {
            getLogger().exception(e);
        } catch (CSTSemanticException e) {
            getLogger().exception(e);
        }
        return constraint;
    }

    /**
     * Returns a map which key is the variable name, value is the variable type.
     * 
     * @param decisionDeclaration
     *            the compound decisionVariable
     * @return a map with variableName and variableType
     */
    public static Map<String, IDatatype> getCompoundNameAndType(AbstractVariable decisionDeclaration) {

        String compoundName = null;
        Map<String, IDatatype> map = new HashMap<String, IDatatype>();
        Compound compoundType = (Compound) decisionDeclaration.getType();
        for (int i = 0; i < compoundType.getInheritedElementCount(); i++) {
            DecisionVariableDeclaration nestedDecl = compoundType.getInheritedElement(i);
            compoundName = nestedDecl.getName();
            IDatatype type = nestedDecl.getType();
            if (type instanceof Reference) {
                Reference refType = (Reference) type;
                map.put(compoundName, refType);
            } else {
                map.put(compoundName, type);

            }
        }
        return map;
    }

    /**
     * Returns an Object array configured by corresponding values of the compound.
     * 
     * @param decisionVariable
     *            the compound variable this configuration works on
     * @param compound
     *            the compound map brings the information from the pipeline editor graph
     * @return an object array including the name and the variable of the compound
     */
    public static Object[] configureCompoundValues(DecisionVariableDeclaration decisionVariable,
            Map<String, Object> compound) {
        ArrayList<Object> sourceObject = new ArrayList<Object>();
        DecisionVariableDeclaration compoundVariable = null;
        Map<String, IDatatype> compoundVariablesMap = new HashMap<String, IDatatype>();
        compoundVariablesMap = getCompoundNameAndType(decisionVariable);
        Set<String> compoundNames = compound.keySet();
        for (String name : compoundNames) {
            // as output could be an array, check this condition here
            if (compound.get(name).getClass().isArray()) {
                sourceObject.add(name);
                Object[] arrayVar = (Object[]) compound.get(name);                              
                DecisionVariableDeclaration[] variables = new DecisionVariableDeclaration[arrayVar.length];
                for (int i = 0; i < arrayVar.length; i++) {
                    Object var = arrayVar[i];
                    variables[i] = new DecisionVariableDeclaration(var.toString(),
                        compoundVariablesMap.get(name), decisionVariable);
                }
                sourceObject.add(variables);               
            } else {
                compoundVariable = new DecisionVariableDeclaration(compound.get(name).toString(),
                        compoundVariablesMap.get(name), decisionVariable);
                sourceObject.add(name);
                sourceObject.add(compoundVariable);
            }           
        }
        Object[] object = sourceObject.toArray();
        return object;
    }
    
    /**
     * Turns a constraint string to a Constraint type of value.
     * @param constraint 
     *          the constraint string
     * @param parent
     *          the model element which the constraint belongs to
     * @return a constraint value
     */
    public static Value obtainConstraintValue(String constraint, IModelElement parent) {
        Value cstValue = null;
        try {
            ConstraintSyntaxTree cst = ModelUtility.INSTANCE.createExpression(constraint, parent);
            cstValue = ValueFactory.createValue(ConstraintType.TYPE, cst); // cstValue is a ConstraintValue
        } catch (CSTSemanticException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConstraintSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValueDoesNotMatchTypeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cstValue;
    }
    
    /**
     * Returns a declaration from <code>IDecisionVariable</code>.
     * 
     * @param variable
     *            the variable to be dereferenced
     * @return a declaration of <code>IDecisionVariable</code>
     */
    public static AbstractVariable getDeclaration(IDecisionVariable variable) {
        AbstractVariable decl = null;
        if (variable.getValue() != null && variable.getValue() instanceof ReferenceValue) {
            decl = ((ReferenceValue) variable.getValue()).getValue();
        } else {
            decl = variable.getDeclaration();
        }
        return decl;
    }

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
                if (project.removeElement(eToChange) && project.removeElement(freezeBlock)) {
                    Constraint constraint = IVMLModelOperations.getConstraint(values.toArray(), var.getDeclaration(),
                            project);
                    result = project.add(constraint) && project.add(freezeBlock);
                }
            }
        } catch (ModelQueryException e) {
            getLogger().exception(e);
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
            if (fc.getOperation().equals("=")) {
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

    /**
     * Modifies the imports in the main pipeline project <code>mainProject</code>.
     * 
     * @param mainProject
     *            the project to be modified
     * @param command
     *            the command how to modify the project, <code>ADD</code> or <code>DEL</code>
     * @param pImport
     *            the import to be added or deleted
     * @return <code>True</code> if modify successfully, <code>false</code> otherwise
     */
    public static boolean modifyImports(Project mainProject, String command, ProjectImport pImport) {
        boolean modified = false;
        switch (command) {
        case ADD: {
            modified = mainProject.addImport(pImport); // it will also check if the import existed before add
            break;
        }
        case DEL: {
            modified = mainProject.removeImport(pImport); // it will also check if the import existed before delete
            break;
        }
        default: {
            break;
        }
        }
        return modified;

    }
    
    /**
     * adds the binding time attribute to <code>project</code>.
     * 
     * @param project the IVML project to add the attribute to
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return <code>true</code> if successful, <code>false</code> else
     */
    public static boolean addRuntimeAttributeToProject(Project project, Project fallbackForType) {
        boolean success = false;
        try {
            //attribute BindingTime bindingTime = BindingTime.compile to PriorityPipCfg;
            de.uni_hildesheim.sse.model.varModel.datatypes.Enum type = ModelQuery.findEnum(project, "BindingTime");
            if (null == type && null != fallbackForType) {
                type = ModelQuery.findEnum(fallbackForType, "BindingTime");
            }
            EnumLiteral literal = type.get("compile");
            Attribute attr = new Attribute("bindingTime", type, project, project);
            attr.setValue(new ConstantValue(ValueFactory.createValue(type, literal)));
            project.add(attr);
            success = true;
        } catch (CSTSemanticException e) {
            getLogger().exception(e);
        } catch (ValueDoesNotMatchTypeException e) {
            getLogger().exception(e);
        } catch (ModelQueryException e) {
            getLogger().exception(e);
        }
        return success;
    }
    
    /**
     * Adds the freeze block to the project.
     * 
     * @param freezables the freezables
     * @param project the IVML project to add to (may be <b>null</b> if failed)
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     * @return the created freeze block
     */
    public static FreezeBlock createFreezeBlock(IFreezable[] freezables, Project project, Project fallbackForType) {
        FreezeBlock result = null;
        try {
            FreezeVariableType iterType = new FreezeVariableType(freezables, project);
            DecisionVariableDeclaration iter = new DecisionVariableDeclaration("f", iterType, project);
            de.uni_hildesheim.sse.model.varModel.datatypes.Enum type = ModelQuery.findEnum(project, "BindingTime");
            if (null == type && null != fallbackForType) {
                type = ModelQuery.findEnum(fallbackForType, "BindingTime");
            }
            String butOperation = "==";
            EnumLiteral literal = type.get("runtime");
            if (null == literal) { // newer version of the model
                literal = type.get("runtimeMon");
                butOperation = ">=";
            } 
            ConstraintSyntaxTree runtime = new ConstantValue(ValueFactory.createValue(type, literal));
            Variable iterEx = new AttributeVariable(new Variable(iter), iterType.getAttribute("bindingTime"));
            OCLFeatureCall op = new OCLFeatureCall(iterEx, butOperation, runtime);
            op.inferDatatype();
            result = new FreezeBlock(freezables, iter, op, project);
        } catch (CSTSemanticException e) {
            getLogger().exception(e);
        } catch (ValueDoesNotMatchTypeException e) {
            getLogger().exception(e);
        } catch (ModelQueryException e) {
            getLogger().exception(e);
        }
        return result;
    }

    /**
     * Adds the freeze block to the project.
     * 
     * @param freezables the freezables
     * @param project the IVML project to add to
     * @param fallbackForType in case that <code>project</code> is being written the first time, may be <b>null</b>
     */
    public static void addFreezeBlock(List<IFreezable> freezables, Project project, Project fallbackForType) {
        if (freezables.size() > 0) {
            IFreezable[] freezes = freezables.toArray(new IFreezable[freezables.size()]);
            FreezeBlock block = createFreezeBlock(freezes, project, fallbackForType);
            if (null != block) {
                project.add(block);
            }
        }
    }
    
    /**
     * Returns the logger for this class.
     * 
     * @return the logger
     */
    private static EASyLogger getLogger() {
        return EASyLoggerFactory.INSTANCE.getLogger(PipelineTranslationOperations.class, Activator.PLUGIN_ID);
    }
    
}
