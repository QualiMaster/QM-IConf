package de.uni_hildesheim.sse.qmApp.commands;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.UiTracerFactory;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.ProjectDescriptor;
import de.uni_hildesheim.sse.qmApp.model.Reasoning;
import de.uni_hildesheim.sse.qmApp.model.SessionModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.modelManagement.Version;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.common.VilException;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.instantiation.core.model.execution.TracerFactory;
import net.ssehub.easy.producer.core.persistence.Configuration.PathKind;
import net.ssehub.easy.producer.core.persistence.IVMLFileWriter;
import net.ssehub.easy.producer.core.persistence.PersistenceUtils;
import net.ssehub.easy.producer.ui.productline_editor.EclipseConsole;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.cst.AttributeVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Attribute;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.FreezeBlock;
import net.ssehub.easy.varModel.model.IFreezable;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.rewrite.ProjectCopyVisitor;
import net.ssehub.easy.varModel.model.rewrite.ProjectRewriteVisitor;
import net.ssehub.easy.varModel.model.rewrite.RewriteContext;
import net.ssehub.easy.varModel.model.rewrite.modifier.IProjectModifier;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * An abstract handler for local instantiation commands. This class supports the explicit selection
 * of the entry point of the main start rule. Please note that this class also assumes that all entry points
 * have the standard VIL parameters source, config and target.
 * 
 * @author Holger Eichelberger
 * @author El-Sharkawy
 */
public abstract class AbstractInstantiateLocal extends AbstractConfigurableHandler {
    
    /**
     * This modifier is used to freeze all relevant declarations inside the CFG projects of Qualimaster.
     * @author El-Sharkawy
     *
     */
    private static class ProjectFreezeModifier implements IProjectModifier {

        private List<DecisionVariableDeclaration> declarations;
        
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
                    if (decl.getNameSpace().equals(projectNS) || decl.getNameSpace().equals(pName)) {
                        toFreeze.add((IFreezable) decl);
                    }
                }
                IFreezable[] freezes = toFreeze.toArray(new IFreezable[0]);
                
                // Create selector
                DecisionVariableDeclaration itr = null;
                ConstraintSyntaxTree selector = null;
                Attribute annotation = null;
                for (int i = 0, end = project.getAttributesCount(); i < end && annotation == null; i++) {
                    Attribute tmpAnnotation = project.getAttribute(i);
                    if (tmpAnnotation.getName().equals(QmConstants.ANNOTATION_BINDING_TIME)) {
                        annotation = tmpAnnotation;
                    }
                }
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
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                    if (null != cVal) {
                        itr = new DecisionVariableDeclaration("var", null, null);
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
        
    }
    
    /**
     * Experimental: <tt>true</tt> use copied and cleaned up configuration for instantiation, 
     * <tt>false</tt> use underlying model and configuration.
     */
    private static final boolean PRUNE_CONFIG = false;

    private static String lastTargetLocation = Location.getModelLocation();
    
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        HandlerUtils.saveDirty(true);
        boolean modelValid = Reasoning.reasonOn(VariabilityModel.Definition.TOP_LEVEL, false);
        if (modelValid || instantiateAlways()) {
            instantiate();
        }
        return null;
    }
        
    /**
     * Implements the actual instantiation by calling VIL.
     */
    protected void instantiate() {        
        EclipseConsole.INSTANCE.clearConsole();
        EclipseConsole.INSTANCE.displayConsole();
        final String targetLocation = selectTargetFolder(getMessage());
        SessionModel.INSTANCE.setInstantiationFolder(targetLocation);
        final Shell shell = Dialogs.getDefaultShell(); 
        if (null != targetLocation) {
            Job job = new Job("QualiMaster Infrastructure Instantiation Process") {
                
                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        File trgFolder = new File(targetLocation);
                        ProjectDescriptor source = new ProjectDescriptor();
                        ProjectDescriptor target = new ProjectDescriptor(source, trgFolder);
                        Executor executor = null;
                        if (PRUNE_CONFIG) {
                            // Maybe null in case of any error
                            executor = prepareModels(trgFolder);
                        }
                        if (!PRUNE_CONFIG || null == executor) {
                            executor = new Executor(source.getMainVilScript())
                                .addConfiguration(source.getConfiguration());
                        }
                        executor.addSource(source).addTarget(target);
                        String startRuleName = getStartRuleName();
                        if (null != startRuleName) {
                            executor.addStartRuleName(startRuleName);
                        }
                        TracerFactory.setDefaultInstance(UiTracerFactory.INSTANCE);
                        executor.execute();
                        notifyInstantiationCompleted(shell);
                    } catch (ModelManagementException e) {
                        showExceptionDialog("Model resolution problem", e);
                    } catch (VilException e) {
                        showExceptionDialog("Instantiation problem", e);
                    }
                    return org.eclipse.core.runtime.Status.OK_STATUS;
                }
            };
            job.schedule();
        }
    }
    
    /**
     * Prepares the underlying IVML {@link Project} and VIL, VTL {@link Script} models
     * for instantiation and generates a pruned and frozen {@link Configuration},
     * which should be used for the instantiation of the QM model.
     * @param targetLocation The destination folder where to instantiate all artifacts
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    protected Executor prepareModels(File targetLocation) {
        // Create frozen and pruned config
        Executor executor = null;
        Configuration config = freezeAndPruneConfig(targetLocation);
        
        // Copy build model and load this temporarily
        File srcFolder = new File(Location.getModelLocationFile(), "EASy");
        File vilFolder = new File(targetLocation, "Instantiation");
        vilFolder.mkdirs();
        try {
            FileUtils.copyDirectory(srcFolder, vilFolder, new FileFilter() {
                
                @Override
                public boolean accept(File pathname) {
                    String fileName = pathname.getName();
                    return pathname.isDirectory() || fileName.endsWith("vil") || fileName.endsWith("vtl")
                        || fileName.endsWith("rtvtl");
                }
            });
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        try {
            net.ssehub.easy.producer.core.persistence.Configuration pathConfig
                = PersistenceUtils.getConfiguration(targetLocation);
            try {
                pathConfig.setPath(PathKind.IVML, "QM-Model");
                pathConfig.setPath(PathKind.VIL, "Instantiation");
                pathConfig.setPath(PathKind.VTL, "Instantiation");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            PersistenceUtils.addLocation(pathConfig, ProgressObserver.NO_OBSERVER);
        } catch (ModelManagementException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        URI vilURI = new File(vilFolder, QmConstants.PROJECT_TOP_LEVEL + "_0.vil").toURI();
        ModelInfo<Script> info = BuildModel.INSTANCE.availableModels().getModelInfo(QmConstants.PROJECT_TOP_LEVEL,
            new Version(0), vilURI);
        if (null != info) {
            try {
                executor = new Executor(BuildModel.INSTANCE.load(info));
            } catch (ModelManagementException e) {
                e.printStackTrace();
            }
            executor.addConfiguration(config);
        }
        return executor;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} for instantiation and generates a pruned 
     * {@link Configuration}, which should be used for the instantiation of the QM model.
     * @param targetLocation The destination folder where to instantiate all artifacts
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    protected Configuration freezeAndPruneConfig(File targetLocation) {
        // Copy base project
        Project baseProject = VariabilityModel.Definition.TOP_LEVEL.getConfiguration().getProject();
        ProjectCopyVisitor copier = new ProjectCopyVisitor(baseProject, FilterType.ALL);
        baseProject.accept(copier);
        baseProject = copier.getCopiedProject();
        
        // Freeze the copy, except for the runtime elements.
        DeclarationFinder finder = new DeclarationFinder(baseProject, FilterType.ALL, null);
        List<DecisionVariableDeclaration> allDeclarations = new ArrayList<DecisionVariableDeclaration>();
        List<AbstractVariable> tmpList = finder.getVariableDeclarations(VisibilityType.ALL);
        for (int i = 0, end = tmpList.size(); i < end; i++) {
            AbstractVariable declaration = tmpList.get(i);
            if (declaration instanceof DecisionVariableDeclaration
                && !(declaration.getNameSpace().equals(QmConstants.PROJECT_OBSERVABLESCFG)
                && declaration.getName().equals("qualityParameters"))) {
                
                allDeclarations.add((DecisionVariableDeclaration) declaration);
            }
        }
        ProjectRewriteVisitor rewriter = new ProjectRewriteVisitor(baseProject, FilterType.ALL);
        ProjectFreezeModifier freezer = new ProjectFreezeModifier();
        freezer.declarations = allDeclarations;
        //rewriter.addProjectModifier(freezer);
        baseProject.accept(rewriter);
        
        // Saved copied projects
        try {
            File modelFolder = new File(targetLocation, "QM-Model");
            if (!modelFolder.exists()) {
                modelFolder.mkdirs();
            }
            IVMLFileWriter writer = new IVMLFileWriter(modelFolder);
            writer.forceComponundTypes(true);
            writer.setFormatInitializer(true);
            writer.save(baseProject);
        } catch (IOException e) {
            showExceptionDialog("Model could not be saved", e);
        }
        // Return a configuration based on the copied, frozen and pruned project
        return new Configuration(baseProject, true);
    }
    
    /**
     * Notifies about a completed instantiation.
     * 
     * @param shell the parent shell
     */
    private static void notifyInstantiationCompleted(final Shell shell) {
        shell.getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                Dialogs.showInfoDialog(shell, "Platform instantiation", 
                    "Platform instantiation completed successfully");
            }
            
        });
        
    }
    
    /**
     * Allows selecting the target folder including checks for existence or whether it is empty.
     * 
     * @param message the message to be displayed in the directory selection dialog
     * @return the target folder or <b>null</b> if none was selected
     */
    private static String selectTargetFolder(String message) {
        DirectoryDialog dialog = new DirectoryDialog(Dialogs.getDefaultShell());
        dialog.setFilterPath(lastTargetLocation);
        dialog.setMessage(message);
        
        String targetLocation;
        while (true) {
            targetLocation = dialog.open();
            if (null != targetLocation) {
                lastTargetLocation = targetLocation;
                File file = new File(targetLocation);
                if (file.exists()) {
                    File[] contents = file.listFiles();
                    if (null != contents && contents.length > 0) {
                        String absTargetLoc = file.getAbsolutePath();
                        int dlgRes = Dialogs.showInfoConfirmDialog("Folder exists: " + absTargetLoc, "The selected "
                            + "target folder\n\n" + absTargetLoc + "\n\nexists "
                            + "and is not empty. Instantiation may overwrite files, so it is recommended to clear the "
                            + "folder before. Shall \n\n" + absTargetLoc + "\n\n be cleared, i.e., all files and "
                            + "folders in this folder will be deleted?");
                        if (SWT.YES == dlgRes) {
                            try {
                                FileUtils.cleanDirectory(file);
                                break; // target location is ok
                            } catch (IOException e) {
                                Dialogs.showErrorDialog("Clearing folder failed", e.getMessage());
                                // requires a new location
                            }
                        }
                    } else {
                        if (file.canRead() && file.canWrite()) {
                            break; // target location is ok
                        } else {
                            Dialogs.showErrorDialog("Cannot access folder", "Please grant read/write access to the "
                                + "selected traget folder or select a different folder.");
                        }
                    }
                } else {
                    Dialogs.showErrorDialog("Folder does not exist", "Selected folder does not exist.");
                }
            } else {
                break; // nothing selected, don't continue
            }
        }
        return targetLocation;
    }

    /**
     * Shows an exception dialog in the user interface thread.
     * 
     * @param title the dialog title
     * @param exception the causing exception
     */
    private static void showExceptionDialog(final String title, final Exception exception) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                Dialogs.showErrorDialog(title, exception.getMessage());
            }
        });
    }

    /**
     * Returns the start rule name. 
     * 
     * @return the start rule name (may be <b>null</b> if "main" shall be used)
     */
    protected abstract String getStartRuleName();
    
    /**
     * Returns the display message.
     * 
     * @return the display message
     */
    protected abstract String getMessage();
    
    /**
     * Whether instantiation shall consider failed reasoning or not.
     * 
     * @return <code>true</code> if reasoning result shall be considered, <code>false</code> else
     */
    protected boolean instantiateAlways() {
        return false;
    }

}
