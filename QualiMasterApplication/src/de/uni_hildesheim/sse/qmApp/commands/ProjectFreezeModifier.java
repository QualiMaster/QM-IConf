/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uni_hildesheim.sse.qmApp.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.FreezeVariableType;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.rewrite.RewriteContext;
import net.ssehub.easy.varModel.model.rewrite.modifier.IProjectModifier;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import qualimasterapplication.Activator;

/**
 * This modifier is used to freeze all relevant declarations inside the CFG projects of Qualimaster. <br/>
 * This modifier is needed by the {@link AbstractInstantiateLocal} to automatically freeze all elements before
 * instantiation.
 * @author El-Sharkawy
 *
 */
class ProjectFreezeModifier implements IProjectModifier {
    private static final int RUNTIME_LEVEL = 2;
    private static final Set<String> BLACKLIST = new HashSet<String>();
    
    static {
        // Already frozen in Observables (not in config)
        BLACKLIST.add("qualityParameters");
    }
    
    private List<DecisionVariableDeclaration> declarations;
    private Map<String, Project> usedProjects;
    private Configuration config;
    
    /**
     * Single constructor instance for this class.
     * @param mainProject The main (copied) QM model, which imports all the other projects.
     * @param declarations The list of all declarations of the whole QM model, which should be frozen in all the
     * different projects.
     */
    ProjectFreezeModifier(Project mainProject, List<DecisionVariableDeclaration> declarations) {
        this.declarations = declarations;
        usedProjects = new HashMap<String, Project>();
        config = new Configuration(mainProject, true);
    }
    
    @Override
    public void modifyProject(Project project, RewriteContext context) {
        // Freeze only in configuration projects
        String pName = project.getName();
        if (pName.endsWith(QmConstants.CFG_POSTFIX) && !pName.equals(QmConstants.PROJECT_ADAPTIVITYCFG)) {
            String projectNS = pName.substring(0, pName.length() - QmConstants.CFG_POSTFIX.length());
            
            // Filter for relevant declarations
            List<IFreezable> toFreeze = new ArrayList<IFreezable>();
            for (int i = 0, end = declarations.size(); i < end; i++) {
                DecisionVariableDeclaration decl = declarations.get(i);
                // Include condition: Declaration was declared in <project> or <projectCfg>
                if (decl.getNameSpace().equals(projectNS) || decl.getNameSpace().equals(pName)) {
                    
                    // Exclude condition: Declaration is runtime variable or toplevel constraint variable
                    if (!ConstraintType.TYPE.isAssignableFrom(decl.getType()) && !isRuntimeVariable(config, decl)
                        && !BLACKLIST.contains(decl.getName())) {
                        
                        toFreeze.add((IFreezable) decl);
                    }
                }
            }
            IFreezable[] freezes = toFreeze.toArray(new IFreezable[0]);
            
            // Create selector
            DecisionVariableDeclaration itr = null;
            ConstraintSyntaxTree selector = null;
            Attribute annotation = getBindingTimeAnnotation(project);
            if (null != annotation && annotation.getType() instanceof OrderedEnum) {
                OrderedEnum btType = (OrderedEnum) annotation.getType();
                EnumLiteral lit = null;
                ConstantValue cVal = null;
                for (int i = 0, end = btType.getLiteralCount(); i < end && null == lit; i++) {
                    if (btType.getLiteral(i).getName().equals(QmConstants.CONST_BINDING_TIME_RUNTIME_MON)) {
                        lit = btType.getLiteral(i);
                        try {
                            cVal = new ConstantValue(ValueFactory.createValue(btType, lit));
                        } catch (ValueDoesNotMatchTypeException e) {
                            Activator.getLogger(ProjectFreezeModifier.class).exception(e);
                        }
                    }
                }
                if (null != cVal) {
                    IDatatype freezeType = new FreezeVariableType(freezes, project);
                    itr = new DecisionVariableDeclaration("var", freezeType, project);
                    AttributeVariable attrExpr = new AttributeVariable(new Variable(itr), annotation);
                    selector = new OCLFeatureCall(attrExpr, OclKeyWords.GREATER_EQUALS, cVal);
                    try {
                        selector.inferDatatype();
                    } catch (CSTSemanticException e) {
                        itr = null;
                        selector = null;
                    }
                }
            }
            
            FreezeBlock block = new FreezeBlock(freezes, itr, selector, project);
            project.add(block);
        }
    }
    
    /**
     * Checks whether the given declaration is a runtime variable.
     * @param config The configuration where the declaration belongs to.
     * @param declaration The declaration to test.
     * @return <tt>true</tt> if the variable is a runtime variable and should not be frozen, <tt>false</tt> else.
     */
    private boolean isRuntimeVariable(Configuration config, DecisionVariableDeclaration declaration) {
        boolean isRuntimeVar = false;

        if (null != config) {
            IDecisionVariable var = config.getDecision(declaration);
            IDecisionVariable annotationVar = null;
            
            for (int i = 0, end = var.getAttributesCount(); i < end && null == annotationVar; i++) {
                IDecisionVariable tmpVar = var.getAttribute(i);
                if (QmConstants.ANNOTATION_BINDING_TIME.equals(tmpVar.getDeclaration().getName())) {
                    annotationVar = tmpVar;
                }
            }
            
            if (null != annotationVar && null != annotationVar.getValue()
                && annotationVar.getValue() instanceof EnumValue) {
                
                EnumLiteral selectedLiteral = ((EnumValue) annotationVar.getValue()).getValue();
                isRuntimeVar = selectedLiteral.getOrdinal() >= RUNTIME_LEVEL;
            }
        }
        
        return isRuntimeVar;
    }
    
    /**
     * Returns the binding time annotation of the project to create the selector statement of the freeze block.
     * @param project The project for which the freeze block shall be created.
     * @return The binding time annotation or <tt>null</tt> it could not be found.
     */
    private Attribute getBindingTimeAnnotation(Project project) {
        Attribute btAnnotation = project.getAttribute(QmConstants.ANNOTATION_BINDING_TIME);
        
        // Try to build up the cache
        if (null == btAnnotation && project.getName().endsWith(QmConstants.CFG_POSTFIX)) {
            String baseName = project.getName().substring(0, project.getName().length()
                - QmConstants.CFG_POSTFIX.length());
            Project baseProject = usedProjects.get(baseName);
            if (null == baseProject) {
                for (int i = 0, end = project.getImportsCount(); i < end && null == baseProject; i++) {
                    Project importedProject = project.getImport(i).getResolved();
                    String importedName = importedProject.getName();
                    
                    if (!usedProjects.containsKey(importedName)) {
                        usedProjects.put(importedName, importedProject);
                    }
                    
                    if (baseName.equals(importedName)) {
                        baseProject = importedProject;
                    }
                }
            }
            
            if (null != baseProject) {
                for (int i = 0, end = baseProject.getAttributesCount(); i < end && btAnnotation == null; i++) {
                    btAnnotation = baseProject.getAttribute(QmConstants.ANNOTATION_BINDING_TIME);
                }
            }
        }
        
        return btAnnotation;
    }
}
