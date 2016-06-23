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
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.rewrite.ProjectCopyVisitor;
import net.ssehub.easy.varModel.model.rewrite.ProjectRewriteVisitor;
import qualimasterapplication.Activator;

/**
 * This class should modify and prune the model and it's configuration before instantiation.
 * @author El-Sharkawy
 *
 */
public class ModelPruner {
    
    /**
     * Settings, which treatments shall be applied to the model during pruning.
     * These may be enabled/disabled for testing purposes.
     * @author El-Sharkawy
     *
     */
    private static class Settings {
        /**
         * Adds freeze blocks to the configuration projects.
         */
        private static final boolean FREEZE = true;
        
        /**
         * Saves the configured values (stores the into the models), before pruning. <br/>
         * This is necessary as some values are set by constraints (which shall be removed).
         * 
         */
        private static final boolean SAVE_VALUES = true;
        
        /**
         * Saves the pruned configuration (writes it to disk).
         */
        private static final boolean WRITE_PRUNED_CONFIG = true;
        
        /**
         * Destination of the pruned configuration / projects.
         * @see #WRITE_PRUNED_CONFIG
         */
        private static final String COPIED_IVML_LOCATION = "QM-Model";
        
        /**
         * Destination of the pruned configuration / projects.
         */
        private static final String COPIED_VIL_LOCATION = "Instantiation";
    }
    
    
    
    private File targetFolder;
    
    /**
     * Single constructor for this class.
     * @param targetFolder The destination folder where to instantiate all artifacts
     */
    public ModelPruner(File targetFolder) {
        this.targetFolder = targetFolder;
    }
    
    /**
     * Prepares the underlying IVML {@link Project} and VIL, VTL {@link Script} models
     * for instantiation and generates a pruned and frozen {@link Configuration},
     * which should be used for the instantiation of the QM model.
     * @return {@link Configuration}, which should be used for the instantiation of the QM model
     */
    public Executor prepareModels() {
        // Create frozen and pruned config
        Executor executor = null;
        Configuration config = freezeAndPruneConfig(targetFolder);
        
        // Register copied model
        File destFolder = new File(targetFolder, Settings.COPIED_IVML_LOCATION);
        VarModel.INSTANCE.updateModel(config.getProject(), destFolder.toURI());
        
        // Copy build model and load this temporarily
        File srcFolder = new File(Location.getModelLocationFile(), "EASy");
        File vilFolder = new File(targetFolder, Settings.COPIED_VIL_LOCATION);
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
                = PersistenceUtils.getConfiguration(targetFolder);
            try {
                pathConfig.setPath(PathKind.IVML, Settings.COPIED_IVML_LOCATION);
                pathConfig.setPath(PathKind.VIL, Settings.COPIED_VIL_LOCATION);
                pathConfig.setPath(PathKind.VTL, Settings.COPIED_VIL_LOCATION);
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
    private Configuration freezeAndPruneConfig(File targetLocation) {
        // Copy base project
        Configuration cfg = VariabilityModel.Definition.TOP_LEVEL.getConfiguration();
        Project baseProject = VariabilityModel.Definition.TOP_LEVEL.getConfiguration().getProject();
        ProjectCopyVisitor copier = new ProjectCopyVisitor(baseProject, FilterType.ALL);
        baseProject.accept(copier);
        baseProject = copier.getCopiedProject();

        if (Settings.SAVE_VALUES) {
            saveValues(baseProject, new HashSet<Project>());
        }
        
        // Freeze the copy, except for the runtime elements.
        if (Settings.FREEZE) {
            freezeProject(baseProject);
        }
        
        // Saved copied projects
        if (Settings.WRITE_PRUNED_CONFIG) {
            try {
                File modelFolder = new File(targetLocation, Settings.COPIED_IVML_LOCATION);
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
                Activator.getLogger(ModelPruner.class).debug("Saving ", project.getName());
                try {
                    Configuration tmpConfig = new Configuration(project, true);
                    // Will change the underlying Project as a side effect
                    new QmPrunedConfigSaver(tmpConfig);
                } catch (ConfigurationException e1) {
                    Activator.getLogger(ModelPruner.class).exception(e1);
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
