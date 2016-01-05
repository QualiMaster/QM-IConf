package de.uni_hildesheim.sse.qmApp.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.cst.ConstantValue;
import de.uni_hildesheim.sse.model.cst.ConstraintSyntaxTree;
import de.uni_hildesheim.sse.model.cst.CopyVisitor;
import de.uni_hildesheim.sse.model.cst.OCLFeatureCall;
import de.uni_hildesheim.sse.model.cst.Variable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.FreezeBlock;
import de.uni_hildesheim.sse.model.varModel.IFreezable;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.IntegerType;
import de.uni_hildesheim.sse.model.varModel.datatypes.OclKeyWords;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.datatypes.Sequence;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;
import de.uni_hildesheim.sse.utils.modelManagement.IVersionRestriction;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import de.uni_hildesheim.sse.utils.modelManagement.RestrictionEvaluationException;

/**
 * Specializes the default configuration saver. Create an instance and call {@link #getSavedConfiguration()}
 * for writing via {@link de.uni_hildesheim.sse.persistency.IVMLWriter}.
 * 
 * @author Holger Eichelberger
 */
public class QualiMasterConfigurationSaver extends de.uni_hildesheim.sse.model.confModel.ConfigurationSaver {

    private static final EASyLogger LOGGER = Activator.getLogger(QualiMasterConfigurationSaver.class);

    // not initialized due to method calls in super constructor
    private Map<AbstractVariable, AbstractVariable> varMapping;
    
    /**
     * Creates a configuration saver instance.
     * 
     * @param srcConfiguration The configuration which should be saved.
     * @throws ConfigurationException in case of any configuration errors
     */
    QualiMasterConfigurationSaver(Configuration srcConfiguration) throws ConfigurationException {
        super(srcConfiguration, true, true);
    }
    
    /**
     * Initializes attributes lazily due to work in constructor.
     */
    private void lazyInit() {
        if (null == varMapping) {
            varMapping = new HashMap<AbstractVariable, AbstractVariable>();            
        }
    }

    /**
     * Creates the project to store the configuration into.
     * 
     * @param srcConfiguration the configuration to be stored
     * @return the project to store the configuration into
     */
    protected Project createProject(Configuration srcConfiguration) {
        // leave out the EASy-specific _conf postfix
        return new Project(srcConfiguration.getProject().getName());
    }

    @Override
    protected void addLocalVariables(Project destProject, Configuration srcConfiguration) {
        lazyInit();
        Project srcProject = srcConfiguration.getProject();

        // copy the local variables - we need them
        for (int e = 0; e < srcProject.getElementCount(); e++) {
            ContainableModelElement elt = srcProject.getElement(e);
            if (elt instanceof DecisionVariableDeclaration) {
                DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                DecisionVariableDeclaration destDecl = new DecisionVariableDeclaration(
                    decl.getName(), decl.getType(), destProject);
                varMapping.put(decl, destDecl);
                destProject.add(destDecl);
            }
        }
        
        // this is a bit overkill as *currently* the defaults in Decision variables are constants only
        if (!varMapping.isEmpty()) {
            CopyVisitor vis = new CopyVisitor(varMapping);
            for (Map.Entry<AbstractVariable, AbstractVariable> entry : varMapping.entrySet()) {
                ConstraintSyntaxTree dflt = entry.getKey().getDefaultValue();
                if (null != dflt) {
                    dflt.accept(vis);
                    try {
                        entry.getKey().setValue(vis.getResult());
                    } catch (ValueDoesNotMatchTypeException e) {
                        LOGGER.exception(e);
                    } catch (CSTSemanticException e) {
                        LOGGER.exception(e);
                    }
                    vis.clear();
                }
            }
        }
    }
    
    /**
     * Tries to create an index access for <code>value</code> within <code>containerValue</code>.
     * 
     * @param decl the container variable declaration (must match <code>containerValue</code>)
     * @param containerValue the container value to access
     * @param value the value to search for within <code>containerValue</code>
     * @return the container access expression if the <code>value</code> can be found in <code>containerValue</code>
     *   and the access expression can be created, <b>null</b> else
     */
    private ConstraintSyntaxTree createIndexAccess(DecisionVariableDeclaration decl, Value containerValue, 
        Value value) {
        ConstraintSyntaxTree result = null;
        if (containerValue instanceof ContainerValue) {
            int pos = ((ContainerValue) containerValue).indexOf(value);
            if (pos >= 0) {
                try {
                    Value indexValue = ValueFactory.createValue(IntegerType.TYPE, pos);
                    result = new OCLFeatureCall(new Variable(decl), OclKeyWords.INDEX_ACCESS, 
                        new ConstantValue(indexValue));
                } catch (ValueDoesNotMatchTypeException ex) {
                    LOGGER.exception(ex);
                }
            }
        }
        return result;
    }

    /**
     * Searches the given <code>project</code> for a sequence that contains <code>value</code> and returns the sequence
     * index access expression if successful. 
     * 
     * @param project the project to start searching
     * @param value the value to be used as reference
     * @param config the actual configuration
     * @return the access expression or <b>null</b> if none can be created
     */
    private ConstraintSyntaxTree searchSequenceValue(Project project, Value value, Configuration config) {
        return searchSequenceValue(project, value, config, new HashSet<Object>());
    }
    
    /**
     * Searches the given <code>project</code> for a sequence that contains <code>value</code> and returns the sequence
     * index access expression if successful. This method shall not be called directly. Use 
     * {@link #searchSequenceValue(Project, Value, Configuration)} instead.
     * 
     * @param project the project to start searching
     * @param value the value to be used as reference
     * @param config the actual configuration
     * @param done all the projects that have been searched so far
     * @return the access expression or <b>null</b> if none can be created
     */
    private ConstraintSyntaxTree searchSequenceValue(Project project, Value value, Configuration config, 
        Set<Object> done) {
        ConstraintSyntaxTree result = null;
        if (!done.contains(project)) {
            done.add(project);
            IDatatype valueType = value.getType();
            // search for a sequence that has the value type as generic type, try to access the value
            // and if this works, create a index access expression
            for (int e = 0; e < project.getElementCount(); e++) {
                ContainableModelElement elt = project.getElement(e);
                if (elt instanceof DecisionVariableDeclaration) {
                    DecisionVariableDeclaration decl = (DecisionVariableDeclaration) elt;
                    IDatatype declType = decl.getType();
                    if (Sequence.isSequence(declType, valueType)) {
                        IDecisionVariable candidateVar = config.getDecision(decl);
                        result = createIndexAccess(decl, candidateVar.getValue(), value);
                    }
                }
            }
            // if not found, search in related projects
            if (null == result) {
                for (int i = 0; null == result && i < project.getImportsCount(); i++) {
                    Project imp = project.getImport(i).getResolved();
                    if (null != imp) {
                        result = searchSequenceValue(imp, value, config, done);
                    }
                }
            }
        }
        return result;
    }

    @Override
    protected ConstraintSyntaxTree createAssignmentConstraint(Project dstProject, AbstractVariable decl, 
        IDecisionVariable var, Value value) {
        Configuration config = var.getConfiguration();
        AbstractVariable dstDecl = varMapping.get(decl);
        if (null == dstDecl) {
            dstDecl = decl;
        }
        // do some referential integrity ;)
        ConstraintSyntaxTree rightSide = null;
        if (Reference.TYPE.isAssignableFrom(value.getType())) {
            // search for sequence in imported models
            rightSide = searchSequenceValue(dstProject, value, config);
        }
        if (null == rightSide) {
            // fallback
            rightSide = new ConstantValue(toSaveableValue(var, value));
        }
        ConstraintSyntaxTree constraint = new OCLFeatureCall(deriveOperand(dstDecl, var), 
            OclKeyWords.ASSIGNMENT, rightSide);
//        CopyVisitor vis = new CopyVisitor(varMapping);
//        constraint.accept(vis);
//        return vis.getResult();
        return constraint;
    }
    
    @Override
    protected void addImports(Project destProject, Configuration srcConfiguration) {
        Project srcProject = srcConfiguration.getProject();
        for (int i = 0; i < srcProject.getImportsCount(); i++) {
            ProjectImport srcImp = srcProject.getImport(i);
            try {
                IVersionRestriction restrictions = srcImp.copyVersionRestriction(destProject);
                ProjectImport dstImp = new ProjectImport(srcImp.getName(), srcImp.getInterfaceName(), 
                    srcImp.isConflict(), srcImp.isCopied(), restrictions);
                dstImp.setResolved(srcImp.getResolved()); // resolve dstImp
                destProject.addImport(dstImp);
            } catch (ModelManagementException e) {
                LOGGER.exception(e);
            } catch (RestrictionEvaluationException e) {
                LOGGER.exception(e);
            }
        }
    }
    
    @Override
    protected boolean isSavingEnabled(Project destProject, IDecisionVariable var) {
        // avoid that all imported config is saved over and over again
        // role separation
        boolean enabled;
        AbstractVariable decl = var.getDeclaration();
        IDatatype type = decl.getType();
        // QualiMaster convention
        if (type == ConstraintType.TYPE || type instanceof ConstraintType) {
            enabled = false;
        } else if (var.getParent() instanceof Configuration) {
            String decisionNamespace = var.getDeclaration().getNameSpace();
            String dstProjectNamespace = destProject.getName();
            enabled = dstProjectNamespace.equals(decisionNamespace);
    
            if (!enabled && dstProjectNamespace.endsWith(VariabilityModel.CFG_POSTFIX)) {
                String defProjectNamespace = dstProjectNamespace.substring(0, 
                    dstProjectNamespace.length() - VariabilityModel.CFG_POSTFIX.length());
                enabled = defProjectNamespace.equals(decisionNamespace);
            }
        } else {
            enabled = true;
        }
        return enabled;
    }
    
    @Override
    protected FreezeBlock createFreezeBlock(IFreezable[] freezables, Project parent) {
        FreezeBlock block = IVMLModelOperations.createFreezeBlock(freezables, parent, null);
        if (null == block) {
            block = super.createFreezeBlock(freezables, parent);
        }
        return block;
    }
    
}
