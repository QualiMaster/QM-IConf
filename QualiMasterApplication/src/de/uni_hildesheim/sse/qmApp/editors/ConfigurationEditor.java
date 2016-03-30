package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory;
import net.ssehub.easy.producer.ui.productline_editor.DelegatingEasyEditorPage;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.model.AbstractVisitor;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.AttributeAssignment;
import net.ssehub.easy.varModel.model.Comment;
import net.ssehub.easy.varModel.model.CompoundAccessStatement;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.OperationDefinition;
import net.ssehub.easy.varModel.model.PartialEvaluationBlock;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.ProjectInterface;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.MandatoryClassifierSettings;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.MandatoryDeclarationClassifier;
import net.ssehub.easy.varModel.model.filter.mandatoryVars.VariableContainer;

/**
 * This class is responsible for providing Editors for given elements which are shown in the 
 * {@link de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElementsView}.
 * Requires {@link VarModelEditorInput} as input.
 * 
 * @author Niko Nowatzki
 * @author Holger Eichelberger
 */
public class ConfigurationEditor extends EditorPart {

    private static final boolean SHOW_CONFIGURATION = false; // for demos
    private Configuration cfg;
    private DelegatingEasyEditorPage parent;
    private TreeViewer treeViewer;
    private VarModelEditorInput input;
    private VariableContainer importances;
    
    @Override
    public void doSave(IProgressMonitor monitor) {
    }

    @Override
    public void doSaveAs() {
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
        if (input instanceof VarModelEditorInput) {
            this.input = (VarModelEditorInput) input;
            cfg = this.input.getConfiguration();
            MandatoryClassifierSettings settings = new MandatoryClassifierSettings();
            settings.setDefaultValueConsideration(false);
            MandatoryDeclarationClassifier finder = new MandatoryDeclarationClassifier(cfg, FilterType.ALL, settings);
            cfg.getProject().accept(finder);
            importances = finder.getImportances();
        } else {
            throw new PartInitException("wrong editor input");
        }
    }

    @Override
    public boolean isDirty() {
        return null != parent ? parent.isDirty() : false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }
    
    @Override
    public void createPartControl(Composite parent) {
        if (SHOW_CONFIGURATION) {
            this.parent = new DelegatingEasyEditorPage(parent);
            treeViewer = ConfigurationTableEditorFactory.createConfigurationTableEditor(cfg, this.parent);
            treeViewer.getTree().setEnabled(false);
        } else {
            Browser browser = new Browser(parent, SWT.H_SCROLL | SWT.V_SCROLL);
            String html = "<html><body>";
            Project project = input.getConfiguration().getProject();
            Project defProject = null;
            if (project.getName().endsWith("Cfg")) {
                String prjName = project.getName();
                if (prjName.length() > 3) {
                    prjName = prjName.substring(0, prjName.length() - 3);
                }
                for (int i = 0; null == defProject && i < project.getImportsCount(); i++) {
                    ProjectImport imp = project.getImport(i);
                    if (imp.getName().equals(prjName)) {
                        defProject = imp.getResolved();
                    }
                }
            }
            if (null != defProject) {
                HtmlVisitor vis = new HtmlVisitor();
                defProject.accept(vis);
                html += vis.getHtml();
            } else {
                html += "Sorry. There is no documentation available for this Section.";
            }
            html += "</body></html>";
            browser.setText(html);
        }
        setPartName(input.getName());
    }

    @Override
    public void setFocus() {
        if (null != treeViewer) {
            treeViewer.getTree().setFocus();
        }
    }

    /**
     * Implements a visitor, which turns an IVML project into a simple HTML documentation.
     * 
     * @author Holger Eichelberger
     */
    private class HtmlVisitor extends AbstractVisitor {

        private StringBuilder actualCompound;
        private List<String> compounds = new ArrayList<String>();
        private List<String> variables = new ArrayList<String>();
        private String indentation = "";
        private IModelPart part;
        
        /**
         * Returns the visiting result in HTML.
         * 
         * @return the result
         */
        public String getHtml() {
            StringBuilder html = new StringBuilder();
            if (compounds.size() > 1) {
                html.append("In this section, you can configure the following structures:\n");
                html.append("<ul>\n");
                for (int c = 0; c < compounds.size(); c++) {
                    html.append(" <li>\n");
                    html.append(compounds.get(c));
                    html.append(" </li>\n");        
                }
                html.append("</ul>\n");
            } else if (1 == compounds.size()) {
                html.append("In this section you can configure the structure ");
                html.append(compounds.get(0));
                html.append("\n");
            }
            if (!variables.isEmpty()) {
                if (compounds.size() > 0) {
                    html.append("In addition,");
                } else {
                    html.append("In this section,");
                }
                html.append(" you can configure");
                if (variables.size() > 1) {
                    html.append(" the following settings:\n");
                    html.append("<ul>\n");
                    for (int v = 0; v < variables.size(); v++) {
                        html.append(" <li>\n");
                        html.append(variables.get(v));
                        html.append(" </li>\n");        
                    }
                    html.append("</ul>\n");
                } else if (1 == variables.size()) {
                    html.append(" the setting ");
                    html.append(variables.get(0));
                    html.append("\n");
                }
            }
            return html.toString();
        }
     
        @Override
        public void visitProject(Project project) {
            part = VariabilityModel.findModelPart(project.getName());
            super.visitProject(project);
        }

        @Override
        public void visitProjectImport(ProjectImport pImport) {
        }
        
        /**
         * Returns a safe string, i.e., an empty string instead of <b>null</b>.
         * 
         * @param string the string to be considered
         * @return the safe string of <code>string</code>
         */
        private String safeString(String string) {
            return null == string ? "" : string;
        }

        @Override
        public void visitDecisionVariableDeclaration(DecisionVariableDeclaration decl) {
            if (!Utils.contains(part.getTopLevelVariables(), decl.getName())) {            
                if (!ConstraintType.isConstraint(decl.getType())) {
                    String displayName = safeString(ModelAccess.getDisplayName(decl)).trim();
                    String helpText = safeString(ModelAccess.getHelpText(decl)).trim();
                    if (displayName.length() > 0 && helpText.length() > 0) {
                        StringBuilder tmp;
                        if (null != actualCompound) {
                            tmp = actualCompound;
                            appendIndentation(tmp);
                            tmp.append("<li>\n");
                            appendIndentation(tmp);
                        } else {
                            tmp = new StringBuilder();
                        }
                        tmp.append("<b>");
                        tmp.append(ModelAccess.getDisplayName(decl));
                        if (null != ConfigurationEditor.this.importances
                            && ConfigurationEditor.this.importances.isMandatory(decl)) {
                            tmp.append(" (mandatory)");
                        }
                        tmp.append("</b>: ");
                        tmp.append(ModelAccess.getHelpText(decl));
                        tmp.append("\n");
                        if (null == actualCompound) {
                            appendIndentation(tmp);
                            tmp.append("</li>\n");
                            variables.add(tmp.toString());
                        }
                    } else {
                        Logger.getLogger(ConfigurationEditor.class).info("Display name or help text missing for " 
                            + decl.getQualifiedName() + ", display name: " + displayName + ", help text:" + helpText);
                    }
                }
            }
        }
        
        @Override
        public void visitCompound(Compound compound) {
            if (Utils.contains(part.getProvidedTypes(), compound)) {
                boolean topLevel = false;
                if (null == actualCompound) {
                    topLevel = true;
                    actualCompound = new StringBuilder();
                }
                appendIndentation(actualCompound);
                actualCompound.append("<b>");
                actualCompound.append(compound.getName()); // TODO preliminary
                actualCompound.append("</b>:\n");
                appendIndentation(actualCompound);
                actualCompound.append("<ul>\n");
                increaseIndentation();
                Compound iter = compound.getRefines();
                while (null != iter) {
                    super.visitCompound(iter);
                    iter = iter.getRefines();
                }
                super.visitCompound(compound);
                decreaseIndentation();
                appendIndentation(actualCompound);
                actualCompound.append("</ul>\n");
                if (topLevel) {
                    compounds.add(actualCompound.toString());
                    actualCompound = null;
                }
            }
        }

        @Override
        public void visitAttribute(Attribute attribute) {
        }

        @Override
        public void visitConstraint(Constraint constraint) {
        }

        @Override
        public void visitFreezeBlock(FreezeBlock freeze) {
        }

        @Override
        public void visitOperationDefinition(OperationDefinition opdef) {
        }

        @Override
        public void visitPartialEvaluationBlock(PartialEvaluationBlock block) {
        }

        @Override
        public void visitProjectInterface(ProjectInterface iface) {
        }

        @Override
        public void visitComment(Comment comment) {
        }

        @Override
        public void visitAttributeAssignment(AttributeAssignment assignment) {
            // heuristic... these are runtime variables or shall not be visible
            /*for (int e = 0; e < assignment.getElementCount(); e++) {
                assignment.getElement(e).accept(this);
            }
            for (int a = 0; a < assignment.getAssignmentCount(); a++) {
                assignment.getAssignment(a).accept(this);
            }*/
        }

        @Override
        public void visitCompoundAccessStatement(CompoundAccessStatement access) {
        }

        @Override
        public void visitDerivedDatatype(DerivedDatatype datatype) {
        }

        @Override
        public void visitEnumLiteral(EnumLiteral literal) {
            // not so far
        }

        @Override
        public void visitReference(Reference reference) {
        }

        @Override
        public void visitSequence(Sequence sequence) {
        }

        @Override
        public void visitSet(Set set) {
        }
        
        /**
         * Increases the indentation for formatting the HTML code.
         */
        private void increaseIndentation() {
            indentation += " ";
        }

        /**
         * Decreases the indentation for formatting the HTML code.
         */
        private void decreaseIndentation() {
            if (indentation.length() > 0) {
                indentation = indentation.substring(0, indentation.length() - 1);
            }
        }
        
        /**
         * Appends the actual indentation to <code>builder</code> for formatting the HTML code.
         * 
         * @param builder the builder to append the indentation to
         */
        private void appendIndentation(StringBuilder builder) {
            builder.append(indentation);
        }
        
    }

}
