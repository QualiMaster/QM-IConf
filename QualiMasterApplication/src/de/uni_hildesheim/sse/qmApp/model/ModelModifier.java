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
package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import eu.qualimaster.easy.extension.ProjectFreezeModifier;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.basics.modelManagement.ModelManagementException;
import net.ssehub.easy.basics.modelManagement.Version;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.instantiation.core.model.buildlangModel.BuildModel;
import net.ssehub.easy.instantiation.core.model.buildlangModel.Script;
import net.ssehub.easy.instantiation.core.model.execution.Executor;
import net.ssehub.easy.producer.core.persistence.Configuration.PathKind;
import net.ssehub.easy.producer.core.persistence.IVMLFileWriter;
import net.ssehub.easy.producer.core.persistence.PersistenceUtils;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.Comment;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.rewrite.ProjectCopyVisitor;
import net.ssehub.easy.varModel.model.rewrite.ProjectRewriteVisitor;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenCompoundConstraintsOmitter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenConstraintVarFilter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenConstraintsFilter;
import net.ssehub.easy.varModel.model.rewrite.modifier.FrozenTypeDefResolver;
import net.ssehub.easy.varModel.model.rewrite.modifier.ModelElementFilter;
import qualimasterapplication.Activator;

/**
 * This class should modify and prune the model and it's configuration before instantiation.
 * Specifically, this modifier does the following:
 * <ul>
 *   <li>Dynamically freeze values ({@value #FREEZE})</li>
 *   <li>Stores propagated values inside the configuration ({@value #SAVE_VALUES})</li>
 *   <li>Optimizes the model for runtime (prune config) ({@value #PRUNE_CONFIG})</li>
 *   <li>Saves the modified configuration to {@value #COPIED_IVML_LOCATION} ({@value #WRITE_MODIFIED_CONFIG})</li>
 *   <li>Saves the VIL model to {@value #COPIED_VIL_LOCATION}</li>
 * </ul>
 * @author El-Sharkawy
 *
 */
public class ModelModifier {
    
    /*
     * Settings, which treatments shall be applied to the model during pruning.
     * These may be enabled/disabled for testing purposes.
     */
    
    /**
     * Adds freeze blocks to the configuration projects.
     */
    private static final boolean FREEZE = false;
    
    /**
     * Saves the configured values (stores the into the models), before pruning. <br/>
     * This is necessary as some values are set by constraints (which shall be removed).
     * 
     */
    private static final boolean SAVE_VALUES = true;
    
    /**
     * Saves the pruned configuration (writes it to disk).
     */
    private static final boolean WRITE_MODIFIED_CONFIG = true;
    
    /**
     * Specifies whether elements shall be deleted, which are not necessary for runtime:
     * <tt>true</tt> delete frozen and unused elements, <tt>false</tt> do not delete anything.
     */
    private static final boolean PRUNE_CONFIG = false;
    
    /**
     * Destination of the pruned configuration / projects.
     * @see #WRITE_MODIFIED_CONFIG
     */
    private static final String COPIED_IVML_LOCATION = "QM-Model";
    
    /**
     * Destination of the pruned configuration / projects.
     */
    private static final String COPIED_VIL_LOCATION = "Instantiation";
    
    private File targetFolder;
    private ModelInfo<Project> oldProjectInfo;
    
    /**
     * Single constructor for this class.
     * @param targetFolder The destination folder where to instantiate all artifacts
     */
    public ModelModifier(File targetFolder) {
        this.targetFolder = targetFolder;
        oldProjectInfo = null;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} and VIL, VTL {@link Script} models
     * for instantiation and generates a pruned and frozen {@link Configuration},
     * which should be used for the instantiation of the QM model.
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    public Executor createExecutor() {
        // Create frozen and pruned config
        Executor executor = null;
        Configuration config = prepareConfig(targetFolder);
        
        // Register copied model
        File destFolder = new File(targetFolder, COPIED_IVML_LOCATION);
        oldProjectInfo = VarModel.INSTANCE.availableModels().getModelInfo(config.getProject());
        VarModel.INSTANCE.updateModel(config.getProject(), destFolder.toURI());
        
        // Copy build model and load this temporarily
        File vilFolder = copyBuildModel();
        
        try {
            net.ssehub.easy.producer.core.persistence.Configuration pathConfig
                = PersistenceUtils.getConfiguration(targetFolder);
            try {
                pathConfig.setPath(PathKind.IVML, COPIED_IVML_LOCATION);
                pathConfig.setPath(PathKind.VIL, COPIED_VIL_LOCATION);
                pathConfig.setPath(PathKind.VTL, COPIED_VIL_LOCATION);
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
     * Restores the old state inside the tooling after instantiation (should be called after {@link #createExecutor()}
     * was used for instantiation). 
     */
    public void clear() {
        // Restore variability model
        if (null != oldProjectInfo && null != oldProjectInfo.getResolved() && null != oldProjectInfo.getLocation()) {
            VarModel.INSTANCE.updateModel(oldProjectInfo.getResolved(), oldProjectInfo.getLocation());
        }
        
        // TODO SE: Restore VIL 
    }

    /**
     * Creates a copy of the build model and place the files parallel to the copied variability model files.
     * @return The root folder of the copied model files.
     */
    private File copyBuildModel() {
        File srcFolder = new File(Location.getModelLocationFile(), "EASy");
        File vilFolder = new File(targetFolder, COPIED_VIL_LOCATION);
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
        return vilFolder;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} for instantiation and generates a pruned 
     * {@link Configuration}, which should be used for the instantiation of the QM model.
     * @param targetLocation The destination folder where to instantiate all artifacts
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    private Configuration prepareConfig(File targetLocation) {
        // Copy base project
        Project baseProject = VariabilityModel.Definition.TOP_LEVEL.getConfiguration().getProject();
        ProjectCopyVisitor copier = new ProjectCopyVisitor(baseProject, FilterType.ALL);
        baseProject.accept(copier);
        baseProject = copier.getCopiedProject();

        if (SAVE_VALUES) {
            saveValues(baseProject, new HashSet<Project>());
        }
        
        // Freeze the copy, except for the runtime elements.
        if (FREEZE) {
            freezeProject(baseProject);
        }
        
        // Prune Config to optimize runtime behaviour
        if (PRUNE_CONFIG) {
            ProjectRewriteVisitor rewriter = new ProjectRewriteVisitor(baseProject, FilterType.ALL);
            Configuration config = new Configuration(baseProject, true);
            rewriter.addModelCopyModifier(new ModelElementFilter(Comment.class));
            rewriter.addModelCopyModifier(new FrozenConstraintsFilter(config));
            rewriter.addModelCopyModifier(new FrozenTypeDefResolver(config));
            rewriter.addModelCopyModifier(new FrozenConstraintVarFilter(config));
            rewriter.addModelCopyModifier(new FrozenCompoundConstraintsOmitter(config));
            baseProject.accept(rewriter);
        }
        
        // Saved copied projects
        if (WRITE_MODIFIED_CONFIG) {
            try {
                File modelFolder = new File(targetLocation, COPIED_IVML_LOCATION);
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
        }
        
        // Return a configuration based on the copied, frozen and pruned project
        Configuration config = new Configuration(baseProject, true);
        Reasoning.reasonOn(false, config);
        return config;
    }
    
    /**
     * Saves the values of configuration projects(recursive function).
     * @param project The copied QM model (the starting point, which imports all the other models).
     * @param done The list of already saved projects, should be empty at the beginning.
     */
    private void saveValues(Project project, Set<Project> done) {
        if (!done.contains(project)) {
            done.add(project);
            
            if (project.getName().endsWith(QmConstants.CFG_POSTFIX)) {
                Activator.getLogger(ModelModifier.class).debug("Saving ", project.getName());
                try {
                    Configuration tmpConfig = new Configuration(project, true);
                    // Will change the underlying Project as a side effect
                    new QmPrunedConfigSaver(tmpConfig);
                } catch (ConfigurationException e1) {
                    Activator.getLogger(ModelModifier.class).exception(e1);
                }
            }
            
            for (int i = 0, end = project.getImportsCount(); i < end; i++) {
                saveValues(project.getImport(i).getResolved(), done);
            }
        }
    }

    /**
     * Adds freezes blocks to the configuration projects.
     * @param baseProject The copied QM model (the starting point, which imports all the other models).
     */
    private void freezeProject(Project baseProject) {
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
        ProjectFreezeModifier freezer = new ProjectFreezeModifier(baseProject, allDeclarations);
        rewriter.addProjectModifier(freezer);
        baseProject.accept(rewriter);
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
}
