package de.uni_hildesheim.sse.qmApp.editors;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import de.uni_hildesheim.sse.ConstraintSyntaxException;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.dslcore.ui.Activator;
import de.uni_hildesheim.sse.dslcore.ui.editors.IEmbeddedEditor;
import de.uni_hildesheim.sse.dslcore.ui.editors.IEmbeddedEditor.IValidationStateListener;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.cst.CSTSemanticException;
import de.uni_hildesheim.sse.model.cst.ConstraintSyntaxTree;
import de.uni_hildesheim.sse.model.cst.Parenthesis;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.Constraint;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.IConstraintHolder;
import de.uni_hildesheim.sse.model.varModel.IDecisionVariableContainer;
import de.uni_hildesheim.sse.model.varModel.IModelElement;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.values.ConstraintValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.persistency.ConstraintSplitWriter.IConstraintFilter;
import de.uni_hildesheim.sse.persistency.IVMLWriter;
import de.uni_hildesheim.sse.persistency.StringProvider;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.ui.embed.EditorUtils;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;

/**
 * Implements an (cell) editor dialog for modifying a single constraints. 
 * 
 * @author Holger Eichelberger
 */
class ConstraintEditorDialog extends Dialog implements IValidationStateListener {

    private static File tmpFile;
    
    /**
     * Defines a (wrapping) interface for a constraint editor.
     * 
     * @author Holger Eichelberger
     */
    interface IConstraintEditor {
        
        /**
         * Changes the constraint.
         * 
         * @param constraint the new constraint in IVML syntax (may be <b>null</b> or empty)
         */
        public void setConstraint(String constraint);
        
        /**
         * Returns the constraint.
         * 
         * @return the constraint in IVML syntax, possibly empty
         */
        public String getConstraint();
        
        /**
         * Returns the underlying SWT control.
         * 
         * @return the SWT control
         */
        public Control getControl();

        /**
         * Defines the layout data.
         * 
         * @param layoutData the layout data
         */
        public void setLayoutData(Object layoutData);

        /**
         * Adds a validation state listener.
         * 
         * @param listener the listener
         */
        public void addValidationStateListener(IValidationStateListener listener);
        
        /**
         * Removes a given validation state listener.
         * 
         * @param listener the listener to be removed
         */
        public void removeValidationStateListener(IValidationStateListener listener);
        
        /**
         * Closes the editor.
         */
        public void close();
    }

    private String constraint;
    private IConstraintEditor editor;
    private IDecisionVariable context;

    /**
     * Creates the dialog.
     * 
     * @param context the variable the <code>constraint</code> shall be created in
     * @param parentShell the parent shell
     * @param constraint the initial constraint
     */
    ConstraintEditorDialog(Shell parentShell, IDecisionVariable context, String constraint) {
        super(parentShell);
        this.constraint = constraint;
        this.context = context;
    }
    
    /**
     * Returns the constraint as text.
     * 
     * @return the constraint as text (IVML syntax, separated by ";")
     */
    public String getConstraintText() {
        return constraint;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(1, false));

        Label label = new Label(composite, SWT.NONE);
        label.setText("Variables in current context:");
        TableViewer tableViewer = createContextVariablesTableViewer(composite);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.heightHint = 200;
        tableViewer.getTable().setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Constraint (OCL-like syntax, see IVML spec, <Strg>+<Space> = content assist):");
        
        editor = createConstraintEditor(composite, context, constraint);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.verticalAlignment = SWT.FILL;
        gridData.grabExcessVerticalSpace = true;
        editor.setLayoutData(gridData);
        return composite;
    }
    
    /**
     * Creates the context variables table viewer.
     * 
     * @param parent the parent composite
     * @return the table viewer
     */
    private TableViewer createContextVariablesTableViewer(Composite parent) {
        TableViewer viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        Table table = viewer.getTable();
        TableLayout layout = new TableLayout();
        table.setLayout(layout);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn column = new TableColumn(table, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 30, true));
        column.setText("variable name"); 
        column.setResizable(true);

        column = new TableColumn(table, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 30, true));
        column.setText("description"); 
        column.setResizable(true);
        
        column = new TableColumn(table, SWT.NONE);
        layout.addColumnData(new ColumnWeightData(3, 30, true));
        column.setText("help text"); 
        column.setResizable(true);

        AbstractVariable var = context.getParent().getDeclaration();
        Set<AbstractVariable> done = new HashSet<AbstractVariable>();
        if (var.getType() instanceof Compound) {
            addVariables((Compound) var.getType(), done);
        }
        IModelElement par = var.getTopLevelParent();
        if (par instanceof Project) {
            addVariables((Project) par, done);
        }
        AbstractVariable[] vars = new AbstractVariable[done.size()];
        done.toArray(vars);
        Arrays.sort(vars, new VariableNameComparator());

        for (int i = 0; i < vars.length; i++) {
            AbstractVariable v = vars[i];
            String comment = ModelAccess.getDescription(v);
            if (null != comment && comment.trim().length() > 0) {
                String[] text = new String[3];
                text[0] = v.getName();
                text[1] = comment;
                text[2] = ModelAccess.getHelpText(v);
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(text);
            }
        }
        return viewer;
    }
    
    /**
     * A comparator for abstract variable names.
     * 
     * @author Holger Eichelberger
     */
    private static class VariableNameComparator implements Comparator<AbstractVariable> {

        @Override
        public int compare(AbstractVariable o1, AbstractVariable o2) {
            return o1.getName().compareTo(o2.getName());
        }
        
    }
    
    /**
     * Adds variables considered as constraint context to <code>done</code>.
     * 
     * @param type the type to add variables from
     * @param done the variables
     */
    private void addVariables(IDecisionVariableContainer type, Set<AbstractVariable> done) {
        if (null != type) {
            for (int i = 0; i < type.getDeclarationCount(); i++) {
                AbstractVariable var = type.getDeclaration(i);
                if (!ConstraintType.TYPE.isAssignableFrom(var.getType()) 
                    && !Container.isContainer(var.getType(), ConstraintType.TYPE)) {
                    if (!done.contains(var)) {
                        done.add(type.getDeclaration(i));
                    }
                }
            }
            for (int a = 0; a < type.getAssignmentCount(); a++) {
                addVariables(type.getAssignment(a), done);
            }
            if (type instanceof Compound) {
                addVariables(((Compound) type).getRefines(), done);
            }
        }
    }

    /**
     * Adds variables considered as constraint context to <code>done</code>.
     * 
     * @param project the project to add variables from
     * @param done the variables
     */
    private void addVariables(Project project, Set<AbstractVariable> done) {
        for (int c = 0; c < project.getElementCount(); c++) {
            ContainableModelElement elt = project.getElement(c);
            if (elt instanceof DecisionVariableDeclaration) { // no attributes here for now
                done.add((DecisionVariableDeclaration) elt);
            }
        }
    }

    
    @Override
    protected Control createContents(Composite parent) {
        // create buttons first
        Control result = super.createContents(parent);
        editor.addValidationStateListener(this);
        return result;
    }

    @Override
    protected void okPressed() {
        try {
            String tmp = editor.getConstraint();
            ModelUtility.INSTANCE.createExpression(tmp, context.getDeclaration());
            constraint = tmp; // if ok, no exception
            super.okPressed();
        } catch (CSTSemanticException e) {
            Dialogs.showErrorDialog("Constraint error", e.getMessage());
        } catch (ConstraintSyntaxException e) {
            Dialogs.showErrorDialog("Constraint error", e.getMessage());
        }
    }
    
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Edit constraint");
        DialogsUtil.centerShell(newShell);
    }
    
    @Override
    protected Point getInitialSize() {
        return new Point(450, 600);
    }
    
    /**
     * Implements a constraint filter for textually given constraints in a certain context.
     * Having the actual constraint objects would be much more efficient, but the pipeline
     * editor model does not support this. Potentially, we can separate the implementation 
     * between the dialog having the constraint object and the pipeline editor somehow, but
     * this is currently the initial implementation.
     * 
     * @author Holger Eichelberger
     */
    private static class ConstraintFilter implements IConstraintFilter {

        private Constraint constraint;

        /**
         * Creates a constraint filter.
         * 
         * @param constraint the actual constraint to filter for
         */
        private ConstraintFilter(Constraint constraint) {
            this.constraint = constraint;
        }
        
        @Override
        public boolean splitAt(IModelElement context, ConstraintSyntaxTree constraint) {
            boolean split = false;
            // context = constraint variable
            if (null != this.constraint && constraint == this.constraint.getConsSyntax()) {
                split = true;
            } 
            return split;
        }
        
    }
    
    /**
     * Creates a constraint editor.
     * 
     * @param parent the parent composite
     * @param context the IVML element the <code>constraint</code> is located in
     * @param constraint the initial constraint (may be <b>null</b> for empty)
     * @return the constraint editor (a simple editor will be returned if the real constraint editor cannot be created)
     */
    static IConstraintEditor createConstraintEditor(Composite parent, final IDecisionVariable context, 
        String constraint) {
        AbstractVariable decl = context.getDeclaration();
        IConstraintEditor result = null;
        FakeContext tmp = createFakeContext(context, constraint);
        if (null != tmp) {
            result = createEmbeddedEditor(parent, tmp, decl, constraint);
        } else {
            result = createFallbackEditor(parent, constraint);
        }
        return result;
    }
    
    /**
     * Create a fake context project importing the original containing project. If the constraint(s)
     * in the dialog is/are on project level, they will be stated on project level. Else, a refining
     * compound is created and the constraint(s) is/are placed within. We place each constraint into an own 
     * parenthesis and cut this out of the model into prefix / postfix in order to implicitly narrow down the 
     * content assist 13:43proposal scope.
     * 
     * @param context the context of the constraint in terms of a variable (potentially a container of 
     *   constraint variables)
     * @param text the constraint text
     * @return the fake context
     */
    private static FakeContext createFakeContext(IDecisionVariable context, String text) {        
        FakeContext result = null;
        Project cfg = context.getConfiguration().getProject();
        AbstractVariable decl = context.getParent().getDeclaration();
        IModelElement parent = null;
        if (decl.getType() instanceof Compound) {
            parent = (Compound) decl.getType();
        }
        Project prj = new Project("tmp");
        Constraint constraint = null;
        try {
            ProjectImport imp = new ProjectImport(cfg.getName(), null); // interfaces are ignored for temp models :o ;)
            imp.setResolved(cfg);
            prj.addImport(imp);
            IConstraintHolder constraintParent;
            if (parent instanceof Compound) {
                Compound pCompound = (Compound) parent;
                Compound cmp = new Compound("cmp", prj, pCompound);
                prj.add(cmp);
                constraintParent = cmp;
            } else {
                constraintParent = prj;
            }
            if (Container.isContainer(decl.getType(), ConstraintType.TYPE)) {
                if (context.getValue() instanceof ContainerValue) {
                    ContainerValue val = (ContainerValue) context.getValue();
                    for (int e = 0; e < val.getElementSize(); e++) {
                        constraint = addConstraint(val.getElement(e), constraint, text, constraintParent);
                    }
                }
            } else if (ConstraintType.TYPE.isAssignableFrom(decl.getType())) {
                constraint = addConstraint(context.getValue(), constraint, text, constraintParent);
            }
            if (null == constraint) {
                if (null == text || 0 == text.length()) {
                    text = "(true)";
                }
                ConstraintSyntaxTree cst = ModelUtility.INSTANCE.createExpression(text, context.getDeclaration());
                constraint = new Constraint(cst, constraintParent);
                constraintParent.addConstraint(constraint);
            }
            if (null == tmpFile) {
                tmpFile = File.createTempFile("qmiconf", "cst");
                tmpFile.deleteOnExit();
            }
            FileWriter out = new FileWriter(tmpFile);
            IVMLWriter writer = IVMLWriter.getInstance(out);
            prj.accept(writer);
            IVMLWriter.releaseInstance(writer);
            out.close();
            result = new FakeContext();
            result.info = VarModel.INSTANCE.availableModels().createTempInfo(prj, tmpFile.toURI());
            result.constraint = constraint;
        } catch (ModelManagementException e) {
            EASyLoggerFactory.INSTANCE.getLogger(ConstraintEditorDialog.class, Activator.PLUGIN_ID).exception(e);
        } catch (CSTSemanticException e) {
            EASyLoggerFactory.INSTANCE.getLogger(ConstraintEditorDialog.class, Activator.PLUGIN_ID).exception(e);
        } catch (ConstraintSyntaxException e) {
            EASyLoggerFactory.INSTANCE.getLogger(ConstraintEditorDialog.class, Activator.PLUGIN_ID).exception(e);
        } catch (IOException e) {
            EASyLoggerFactory.INSTANCE.getLogger(ConstraintEditorDialog.class, Activator.PLUGIN_ID).exception(e);
        }
        return result;
    }
    
    /**
     * Represents the temporary project embedding the textual constraint into the right
     * context so that variables can be resolved properly. Contains the constraint object
     * representing the textual constraint.
     * 
     * @author Holger Eichelberger
     */
    private static class FakeContext {
        private ModelInfo<Project> info;
        private Constraint constraint;
    }
    
    /**
     * Adds a configured constraint to the fake context.
     * 
     * @param val the constraint value (may be <b>null</b>, ignored then)
     * @param actual the actual constraint corresponding to the initially given <code>text</code>
     * @param text the constraint text (may be <b>null</b> or empty)
     * @param constraintParent the parent element to store the constraints within
     * @return <code>actual</code> or the constraint created in this method
     */
    private static Constraint addConstraint(Value val, Constraint actual, String text, 
        IConstraintHolder constraintParent) {
        if (val instanceof ConstraintValue) {
            ConstraintValue cValue = (ConstraintValue) val;
            ConstraintSyntaxTree cst = cValue.getValue();
            if (null != cst) {
                try {
                    Constraint constraint = new Constraint(new Parenthesis(cst), constraintParent);
                    if (null != text && text.length() > 0 && null == actual) {
                        if (text.equals(StringProvider.toIvmlString(cst))) {
                            actual = constraint;
                        }
                    }
                    constraintParent.addConstraint(constraint);
                } catch (CSTSemanticException e) {
                    EASyLoggerFactory.INSTANCE
                        .getLogger(ConstraintEditorDialog.class, Activator.PLUGIN_ID).exception(e);
                }
            }
        }
        return actual;
    }
    
    /**
     * Finds a constraint in the fake context model.
     * 
     * @param info the fake context model
     * @param constraintText the constraint text to seach for
     * @return the resulting constraint
     */
    private static Constraint findConstraint(FakeContext info, String constraintText) {
        Constraint result = null;
        Project prj = info.info.getResolved();
        if (null != prj) {
            for (int e = 0; null == result && e < prj.getElementCount(); e++) {
                ContainableModelElement elt = prj.getElement(e);
                if (elt instanceof Compound) {
                    Compound cmp = (Compound) elt;
                    for (int c = 0; null == result && c < cmp.getConstraintsCount(); c++) {
                        result = checkConstraint(cmp.getConstraint(c), constraintText, result);
                    }
                } else if (elt instanceof Constraint) {
                    result = checkConstraint((Constraint) elt, constraintText, result);
                }
            }
        }
        return result;
    }
    
    /**
     * Checks a constraint in the fake context model against the given <code>constraintText</code>. Considers 
     * artificial inserted parenthesis.
     * 
     * @param constraint the constraint to check
     * @param constraintText the constraint text to search for
     * @param result the resulting constraint found so far (match checking happens only if <b>null</b>)
     * @return <code>constraint</code> if match or <code>result</code> otherways
     */
    private static Constraint checkConstraint(Constraint constraint, String constraintText, Constraint result) {
        if (null == result) {
            ConstraintSyntaxTree cst = constraint.getConsSyntax();
            if (cst instanceof Parenthesis) { // strip artificial parenthesis
                cst = ((Parenthesis) cst).getExpr();
            }
            if (StringProvider.toIvmlString(cst).equals(constraintText)) {
                result = constraint;
            }
        }
        return result;
    }
    
    /**
     * Creates the embedded xText constraint editor.
     * 
     * @param parent the parent composite
     * @param info the model info of the fake context
     * @param context the IVML element the <code>constraint</code> is located in
     * @param constraint the initial constraint (may be <b>null</b> for empty)
     * @return the embedded editor or, if not possible, {@link #createFallbackEditor(Composite)}
     */
    private static IConstraintEditor createEmbeddedEditor(Composite parent, final FakeContext info, 
        final IModelElement context, String constraint) {
        IConstraintEditor result;
        IConstraintFilter filter = new ConstraintFilter(info.constraint);
        final IEmbeddedEditor editor = EditorUtils.embedIvmlConstraintEditor(info.info, parent, filter, true);
        if (null != editor) {
            result = new IConstraintEditor() {
                @Override
                public void setConstraint(String constraintText) {
                    EditorUtils.setConstraint(editor, info.info, 
                        new ConstraintFilter(findConstraint(info, constraintText)), true);
                }
    
                @Override
                public String getConstraint() {
                    return editor.getEditableContent();
                }
    
                @Override
                public Control getControl() {
                    return editor.getViewer().getControl();
                }
                
                @Override
                public void setLayoutData(Object layoutData) {
                    getControl().setLayoutData(layoutData);
                }

                @Override
                public void addValidationStateListener(IValidationStateListener listener) {
                    editor.addValidationStateListener(listener);
                }

                @Override
                public void removeValidationStateListener(IValidationStateListener listener) {
                    editor.removeValidationStateListener(listener);
                }

                @Override
                public void close() {
                    VarModel.INSTANCE.availableModels().releaseTempInfo(info.info);
                }
                
            };
        } else {
            result = createFallbackEditor(parent, constraint);
        }
        return result; 
    }
    
    /**
     * Creates the fallback editor.
     * 
     * @param parent the parent composite
     * @param constraint the initial constraint (may be <b>null</b> for empty)
     * @return the fallback editor
     */
    private static IConstraintEditor createFallbackEditor(Composite parent, String constraint) {
        final Text xTextEditor = new Text(parent, SWT.BORDER);
        xTextEditor.setEnabled(false);  
        if (null != constraint) {
            xTextEditor.setText(constraint);
        }
        return new IConstraintEditor() {
            @Override
            public void setConstraint(String constraint) {
                String text = null == constraint ? "" : constraint;
                xTextEditor.setText(text);
            }

            @Override
            public String getConstraint() {
                return xTextEditor.getText();
            }

            @Override
            public Control getControl() {
                return xTextEditor;
            }
            
            @Override
            public void setLayoutData(Object layoutData) {
                xTextEditor.setLayoutData(layoutData);
            }

            @Override
            public void addValidationStateListener(IValidationStateListener listener) {
                // ignore
            }

            @Override
            public void removeValidationStateListener(IValidationStateListener listener) {
                // ignore
            }

            @Override
            public void close() {
                // ignore
            }
            
        }; 
    }

    @Override
    public void notifyValidationState(final boolean hasErrors) {
        getShell().getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                getButton(IDialogConstants.OK_ID).setEnabled(!hasErrors);
            }
            
        });
    }
    
    @Override
    public boolean close() {
        editor.removeValidationStateListener(this);
        return super.close();
    }

}
