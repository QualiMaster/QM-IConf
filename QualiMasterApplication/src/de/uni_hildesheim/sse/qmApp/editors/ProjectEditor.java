package de.uni_hildesheim.sse.qmApp.editors;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIConfiguration;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.Attribute;
import de.uni_hildesheim.sse.model.varModel.AttributeAssignment;
import de.uni_hildesheim.sse.model.varModel.Comment;
import de.uni_hildesheim.sse.model.varModel.CompoundAccessStatement;
import de.uni_hildesheim.sse.model.varModel.Constraint;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.FreezeBlock;
import de.uni_hildesheim.sse.model.varModel.IModelVisitor;
import de.uni_hildesheim.sse.model.varModel.OperationDefinition;
import de.uni_hildesheim.sse.model.varModel.PartialEvaluationBlock;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.ProjectInterface;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.DerivedDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Enum;
import de.uni_hildesheim.sse.model.varModel.datatypes.EnumLiteral;
import de.uni_hildesheim.sse.model.varModel.datatypes.OrderedEnum;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.datatypes.Sequence;
import de.uni_hildesheim.sse.model.varModel.datatypes.Set;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;

/**
 * This class is responsible for providing Editors for given elements which are shown in the 
 * {@link ConfigurableElementsView}.
 * 
 * @author Niko Nowatzki
 * @author Holger Eichelberger
 */
public class ProjectEditor extends AbstractVarModelEditor {

    private VarModelEditorInput input;
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);
        if (input instanceof VarModelEditorInput) {
            this.input = (VarModelEditorInput) input;
            setConfiguration(this.input.getConfiguration());
        } else {
            throw new PartInitException("wrong editor input");
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        parent.setLayout(new GridLayout(2, false));

        Configuration cfg = getConfiguration();
        UIConfiguration uiCfg = getUIConfiguration();
        Project project = cfg.getProject();
        if (project.getName().endsWith(VariabilityModel.CFG_POSTFIX)) {
            String pName = project.getName();
            pName = pName.substring(0, pName.length() - VariabilityModel.CFG_POSTFIX.length());
            Project imp = null;
            for (int i = 0; null == imp && i < project.getImportsCount(); i++) {
                ProjectImport tmp = project.getImport(i);
                if (pName.equals(tmp.getProjectName())) {
                    imp = tmp.getResolved();
                }
            }
            if (null != imp) {
                project = imp;
            }
        }
        EditorCreationVisitor vis = new EditorCreationVisitor(uiCfg, cfg);
        project.accept(vis);
        setPartName(input.getName());
    }
    

    /**
     * A visitor to create editors for the top-level variables.
     * 
     * @author Holger Eichelberger
     */
    private class EditorCreationVisitor implements IModelVisitor {

        private UIConfiguration uiCfg;
        private Configuration cfg;
        
        /**
         * Creates a visitor instance.
         * 
         * @param uiCfg the UI configuration
         * @param cfg the IVML configuration
         */
        EditorCreationVisitor(UIConfiguration uiCfg, Configuration cfg) {
            this.uiCfg = uiCfg;
            this.cfg = cfg;
        }
        
        @Override
        public void visitEnum(Enum eenum) {
            // nothing to do
        }

        @Override
        public void visitOrderedEnum(OrderedEnum eenum) {
            // nothing to do
        }

        @Override
        public void visitCompound(Compound compound) {
            // nothing to do
        }

        @Override
        public void visitDerivedDatatype(DerivedDatatype datatype) {
            // nothing to do
        }

        @Override
        public void visitEnumLiteral(EnumLiteral literal) {
            // nothing to do
        }

        @Override
        public void visitReference(Reference reference) {
            // nothing to do
        }

        @Override
        public void visitSequence(Sequence sequence) {
            // nothing to do
        }

        @Override
        public void visitSet(Set set) {
            // nothing to do
        }

        @Override
        public void visitProject(Project project) {
            for (int e = 0; e < project.getElementCount(); e++) {
                project.getElement(e).accept(this);
            }
        }

        @Override
        public void visitProjectImport(ProjectImport pImport) {
            // nothing to do - now
        }

        @Override
        public void visitDecisionVariableDeclaration(DecisionVariableDeclaration decl) {
            IDecisionVariable var = cfg.getDecision(decl); 
            if (null != var && !ConstraintType.TYPE.isAssignableFrom(decl.getType())) {
                EditorUtils.createLabel(uiCfg, var, getImportances());
                addEditor(createEditorInstance(var));
            }
        }

        @Override
        public void visitAttribute(Attribute attribute) {
            // nothing to do
        }

        @Override
        public void visitConstraint(Constraint constraint) {
            // nothing to do
        }

        @Override
        public void visitFreezeBlock(FreezeBlock freeze) {
            // nothing to do
        }

        @Override
        public void visitOperationDefinition(OperationDefinition opdef) {
            // nothing to do
        }

        @Override
        public void visitPartialEvaluationBlock(PartialEvaluationBlock block) {
            // nothing to do
        }

        @Override
        public void visitProjectInterface(ProjectInterface iface) {
            // nothing to do
        }

        @Override
        public void visitComment(Comment comment) {
            // nothing to do
        }

        @Override
        public void visitAttributeAssignment(AttributeAssignment assignment) {
            for (int a = 0; a < assignment.getAssignmentCount(); a++) {
                assignment.getAssignment(a).accept(this);
            }
            for (int e = 0; e < assignment.getElementCount(); e++) {
                assignment.getElement(e).accept(this);
            }
        }

        @Override
        public void visitCompoundAccessStatement(CompoundAccessStatement access) {
            // nothing to do
        }
        
    }

}
