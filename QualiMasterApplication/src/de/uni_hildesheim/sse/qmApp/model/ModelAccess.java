package de.uni_hildesheim.sse.qmApp.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

import qualimasterapplication.Activator;
import de.uni_hildesheim.sse.BuildLangModelUtility;
import de.uni_hildesheim.sse.ModelUtility;
import de.uni_hildesheim.sse.dslCore.EclipseResourceInitializer;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.easy_producer.PLPWorkspaceListener;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.buildlangModel.BuildModel;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.rtVil.RtVilModel;
import de.uni_hildesheim.sse.easy_producer.instantiator.model.templateModel.TemplateModel;
import de.uni_hildesheim.sse.model.confModel.AllFreezeSelector;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.DisplayNameProvider;
import de.uni_hildesheim.sse.model.confModel.IConfigurationElement;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.ContainableModelElement;
import de.uni_hildesheim.sse.model.varModel.DecisionVariableDeclaration;
import de.uni_hildesheim.sse.model.varModel.IModelElement;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.BooleanType;
import de.uni_hildesheim.sse.model.varModel.datatypes.Compound;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.filter.ReferenceValuesFinder;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.persistency.IVMLWriter;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Definition;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.modelManagement.AvailableModels;
import de.uni_hildesheim.sse.utils.modelManagement.IModelListener;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import de.uni_hildesheim.sse.utils.modelManagement.ModelManagementException;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import de.uni_hildesheim.sse.vil.rt.RtVilModelUtility;
import de.uni_hildesheim.sse.vil.templatelang.TemplateLangModelUtility;

/**
 * Provides generic access to EASy models.
 * 
 * @author Holger Eichelberger
 */
public class ModelAccess {

    /**
     * A provider for the value of new elements.
     * 
     * @author Holger Eichelberger
     */
    private interface INewElementValueProvider {
        
        /**
         * Provides the initial value for a given <code>type</code>.
         * 
         * @param type the type to provide the value for
         * @return the new value
         * @throws ValueDoesNotMatchTypeException in case that the value does not match the given type
         */
        public Value provideValue(IDatatype type) throws ValueDoesNotMatchTypeException;
        
    }

    /**
     * Just creates an empty value for the given type.
     */
    private static final INewElementValueProvider EMPTY_VALUE_PROVIDER = new INewElementValueProvider() {
        
        @Override
        public Value provideValue(IDatatype type) throws ValueDoesNotMatchTypeException {
            return ValueFactory.createValue(type);
        }
    };
    
    private static final Map<String, Configuration> CONFIG_CACHE = new HashMap<String, Configuration>();

    private static final IModelListener<Project> CONFIG_LISTENER = new IModelListener<Project>() {

        @Override
        public void notifyReplaced(Project oldModel, Project newModel) {
            if (null != newModel) {
                CONFIG_CACHE.remove(newModel.getName());
            }
        }
        
    };

    private static boolean initialized = false;

    /**
     * Utility class.
     */
    private ModelAccess() {
    }

    static {
        DisplayNameProvider.setInstance(QualiMasterDisplayNameProvider.INSTANCE);
        ConfigurationTableEditorFactory.createUpdatableCellEditors(true);
    }

    /**
     * Initialize by setting the model-location. Therefore take the model-location from the properties-file and add this
     * location to the {@link VarModel}.
     */
    public static void initialize() {
        if (!initialized) {
            initialized = true;

            PLPWorkspaceListener.unregister(); // avoid disturbances
            ModelUtility.setResourceInitializer(new EclipseResourceInitializer());
            try {
                File modelLocation = Location.getModelLocationFile();
                String src = Location.getSourceLocation();
                // assumption for now - for working in the workspace we need a project
                IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
                if (modelLocation.toURI().toString().startsWith(wsRoot.getLocationURI().toString())) {
                    // config is configured into the workspace
                    IProject project = wsRoot.getProject(modelLocation.getName());
                    IProgressMonitor monitor = new NullProgressMonitor();
                    if (!project.exists()) {
                        try {
                            project.create(monitor);
                        } catch (CoreException e) {
                            Activator.getLogger(ModelAccess.class).exception(e);
                        }
                    }
                    if (!project.isOpen()) {
                        try {
                            project.open(monitor);
                        } catch (CoreException e) {
                            Activator.getLogger(ModelAccess.class).exception(e);
                        }
                    }
                    if (!modelLocation.exists()) {
                        modelLocation.mkdirs();
                    }
                    // project.refreshLocal does not work
                    if (null != src) {
                        File fSrc = new File(src);
                        if (fSrc.exists()) {
                            Utils.importFiles(project, fSrc, monitor);
                        }
                    }
                }
                for (IProject prj : wsRoot.getProjects()) {
                    try {
                        prj.refreshLocal(IResource.DEPTH_INFINITE, null);
                    } catch (CoreException e) {
                        EASyLoggerFactory.INSTANCE.getLogger(ModelAccess.class, Activator.PLUGIN_ID).info(
                            e.getMessage());
                    }
                }
                
                ProgressObserver obs = ProgressObserver.NO_OBSERVER;
                VarModel.INSTANCE.locations().addLocation(modelLocation, obs);
                BuildModel.INSTANCE.locations().addLocation(modelLocation, obs);
                TemplateModel.INSTANCE.locations().addLocation(modelLocation, obs);
                RtVilModel.INSTANCE.locations().addLocation(modelLocation, obs);

                VarModel.INSTANCE.loaders().registerLoader(ModelUtility.INSTANCE, obs);
                BuildModel.INSTANCE.loaders().registerLoader(BuildLangModelUtility.INSTANCE, obs);
                TemplateModel.INSTANCE.loaders().registerLoader(TemplateLangModelUtility.INSTANCE, obs);
                RtVilModel.INSTANCE.loaders().registerLoader(RtVilModelUtility.INSTANCE, obs);
            } catch (ModelManagementException e) {
                Dialogs.showErrorDialog("Model error", ModelAccess.class, e);
            }
        }
    }

    /**
     * Returns the configuration of the variability <code>modelPart</code>. For now, this method does not consider any
     * model version.
     * 
     * @param modelPart the model part denoting the model to return
     * @return the configuration of <code>modelPart</code> or, if it does not exist, an empty configuration
     */
    public static Configuration getConfiguration(IModelPart modelPart) {
        return getConfiguration(modelPart.getModelName());
    }

    /**
     * Returns the model information for the given <code>modelPart</code>.
     * 
     * @param modelPart the model part denoting the model to return
     * @return the model information of <code>modelPart</code> or <b>null</b> if it does not exist
     */
    public static ModelInfo<Project> getModelInfo(IModelPart modelPart) {
        return getModelInfo(modelPart.getModelName());
    }

    /**
     * Returns the IVML model for the given <code>modelPart</code>.
     * 
     * @param modelPart the model part denoting the model to return
     * @return the model of <code>modelPart</code> or <b>null</b> if it does not exist
     */
    public static Project getModel(IModelPart modelPart) {
        return getModel(modelPart.getModelName());
    }

    /**
     * Returns the model information for the given <code>modelName</code>.
     * 
     * @param modelName the name of the model
     * @return the model information of <code>modelPart</code> or <b>null</b> if it does not exist
     */
    private static ModelInfo<Project> getModelInfo(String modelName) {
        ModelInfo<Project> info = null;
        List<ModelInfo<Project>> infos = VarModel.INSTANCE.availableModels().getModelInfo(modelName);
        // primitive for now
        if (null != infos && infos.size() > 0) {
            info = infos.get(0);
        }
        return info;
    }

    /**
     * Returns the model for the given <code>modelName</code>.
     * 
     * @param modelName the name of the model
     * @return the model for <code>modelPart</code> or <b>null</b> if it does not exist
     */
    private static Project getModel(String modelName) {
        Project model = null;
        ModelInfo<Project> info = getModelInfo(modelName);
        if (null != info) {
            try {
                model = VarModel.INSTANCE.load(info);
                VarModel.INSTANCE.events().addModelListener(model, CONFIG_LISTENER);
            } catch (ModelManagementException e) {
                Dialogs.showErrorDialog("Model error", ModelAccess.class, e);
            }
        }
        return model;
    }
    
    /**
     * Forces the reload of a model part. Loads the model if it has not been loaded so far.
     * This method shall be preferred over the reload function of {@link VarModel} as internal
     * model listeners are established.
     * 
     * @param part the model part to reload
     * @return the related IVML project (the model of <code>part</code> if no reload happened)
     */
    public static Project reloadModel(IModelPart part) {
        Project model = part.getConfiguration().getProject();
        if (null != model) {
            model = reloadModel(model, null);
        } else {
            model = getModel(part);
        }
        return model;
    }

    /**
     * Forces the reload of a model.
     * This method shall be preferred over the reload function of {@link VarModel} as internal
     * model listeners are established.
     * 
     * @param model the model to reload
     * @param uri the actual URI of the model as fallback in case of a temporary model (may be <b>null</b>, is 
     *     ignored then)
     * @return the loaded IVML project (<code>model</code> if no reload happened)
     */
    public static Project reloadModel(Project model, URI uri) {
        AvailableModels<Project> avail = VarModel.INSTANCE.availableModels();
        ModelInfo<Project> info = avail.getModelInfo(model);
        if (null == info && null != uri) {
            info = avail.getInfo(uri);
            if (null != info && null != info.getResolved()) {
                model = info.getResolved();
            }
        }
        Project tmp = VarModel.INSTANCE.reload(model);
        if (tmp != model) {
            model = tmp;
            VarModel.INSTANCE.events().addModelListener(model, CONFIG_LISTENER);
        }
        return model;
    }
    
    /**
     * Returns the configuration of the variability model with name <code>modelName</code>. For now, this method does
     * not consider any model version.
     * 
     * @param modelName the name of the model
     * @return the configuration of <code>modelName</code> or, if it does not exist, an empty configuration
     */
    public static Configuration getConfiguration(String modelName) {
        Configuration result = CONFIG_CACHE.get(modelName);
        if (null == result) {
            Project model = getModel(modelName);
            if (null == model) {
                model = new Project(modelName);
                VarModel.INSTANCE.events().addModelListener(model, CONFIG_LISTENER);
            }
            result = new Configuration(model);
            CONFIG_CACHE.put(modelName, result);
        }
        return result;
    }
    
    /**
     * Removes a configuration from the cache. Handle with care!
     * 
     * @param modelName the name of the model
     */
    public static void removeConfiguration(String modelName) {
        CONFIG_CACHE.remove(modelName);
        ModelInfo<Project> info = getModelInfo(modelName);
        if (null != info) {
            VarModel.INSTANCE.clearModel(info);
        }
    }

    /**
     * Returns the configuration of the parent variability model of the given <code>variable</code>. For now, this
     * method does not consider any model version.
     * 
     * @param variable
     *            the variable to return the configuration for
     * @return the configuration of <code>variable</code> or, if it does not exist, an empty configuration
     */
    public static Configuration getConfiguration(AbstractVariable variable) {
        IModelElement pElt = variable.getTopLevelParent();
        String modelName = "";
        if (null != pElt) {
            modelName = pElt.getName();
            if (!modelName.endsWith(VariabilityModel.CFG_POSTFIX)) {
                modelName = modelName + VariabilityModel.CFG_POSTFIX;
            }
        }
        return getConfiguration(modelName);
    }

    /**
     * Obtains a (configured) decision variable.
     * 
     * @param modelPart
     *            the model part to access
     * @param varName
     *            the variable name
     * @return the queried decision variable (may be <b>null</b> if not found)
     */
    public static IDecisionVariable obtainVariable(IModelPart modelPart, String varName) {
        return obtainVariable(modelPart.getConfiguration(), varName);
    }

    /**
     * Obtains a (configured) decision variable.
     * 
     * @param config
     *            the configuration to access
     * @param varName
     *            the variable name
     * @return the queried decision variable (may be <b>null</b> if not found)
     */
    private static IDecisionVariable obtainVariable(Configuration config, String varName) {
        IDecisionVariable var = null;
        try {
            Project project = config.getProject();
            AbstractVariable machineElt = (AbstractVariable) ModelQuery.findElementByName(project, varName,
                    AbstractVariable.class);
            var = config.getDecision((AbstractVariable) machineElt);
        } catch (ModelQueryException e) {
            Dialogs.showErrorDialog("Model error", ModelAccess.class, e);
        }
        return var;
    }

    /**
     * Stores the given configuration. This method is for internal and debugging purposes. Please use
     * {@link #store(Configuration)} for storing a model.
     * 
     * @param config the configuration to be written
     * @param writer the (output) writer to store the configuration
     * @param uri the URI to update the EASy caches for enacting / linking the model (no update happens 
     *     if <code>uri</code> is <b>null</b>)
     * @return the stored project
     */
    public static Project store(Configuration config, Writer writer, URI uri) {
        Project result = null;
        try {
            QualiMasterConfigurationSaver saver = new QualiMasterConfigurationSaver(config);
            Project outProject = saver.getSavedConfiguration();
            IVMLWriter iWriter = new QualiMasterIvmlWriter(writer);
            iWriter.setFormatInitializer(true);
            iWriter.forceComponundTypes(true);
            outProject.accept(iWriter);
            //IVMLWriter.releaseInstance(iWriter);
            result = outProject;
            if (null != uri) {
                VarModel.INSTANCE.updateModel(result, uri);
            }
        } catch (ConfigurationException e) {
            Dialogs.showErrorDialog("Storing configuration", ModelAccess.class, e);
        }
        return result;
    }

    /**
     * Stores the given configuration and updates the internal caches and registries accordingly.
     * 
     * @param config the given configuration
     */
    public static void store(Configuration config) {
        Project project = config.getProject();
        ModelInfo<Project> info = VarModel.INSTANCE.availableModels().getModelInfo(project);
        if (null == info) {
            Dialogs.showErrorDialog("Model cannot be stored", "Model location for '" + config.getName()
                    + "' cannot be determined as it is temporary. Model cannot be stored.");
        } else {
            FileWriter writer = null;
            try {
                URI uri = info.getLocation();
                writer = new FileWriter(new File(uri));
                Project stored = store(config, writer, uri);
                writer.close();
                // replace in cache
                CONFIG_CACHE.put(project.getName(), config);
                // replace in VarModel
                VarModel.INSTANCE.updateModel(stored, uri);
            } catch (IOException e) {
                IOUtils.closeQuietly(writer);
                Dialogs.showErrorDialog("Storing configuration", ModelAccess.class, e);
            }
        }
    }
    
    /**
     * Derives the display name of <code>var</code>. This method follows the convention, that if <code>var</code> is a
     * compound and one of its nested elements is called <code>name</code> and is of type String, the value of that
     * variable is taken as display name, else the name of <code>var</code>.
     * 
     * @param var the variable to return the display name for
     * @return the display name
     */
    public static String getDisplayName(IDecisionVariable var) {
        String displayName = null;
        var = Configuration.dereference(var);
        if (null != var && var.getDeclaration().getType().isAssignableFrom(Compound.TYPE)) {
            for (int n = 0; null == displayName && n < var.getNestedElementsCount(); n++) {
                IDecisionVariable nVar = var.getNestedElement(n);
                AbstractVariable nDecl = nVar.getDeclaration();
                if (VariabilityModel.isNameSlot(nDecl)) {
                    Value value = nVar.getValue();
                    if (null != value) {
                        displayName = ((StringValue) value).getValue();
                    }
                }
            }
        }
        if (null == displayName && null != var) {
            displayName = getDisplayName(var);
        }
        return displayName;
    }

    /**
     * Returns the display name of <code>var</code>.
     * 
     * @param var the variable declaration
     * @return the display name
     */
    public static String getDisplayName(AbstractVariable var) {
        String displayName = getDescription(var);
        if (null == displayName) {
            displayName = var.getName();
        }
        return displayName;
    }

    /**
     * Returns whether <code>type</code> is a constraint.
     * 
     * @param type the type
     * @return <code>true</code> in case of a constraint <code>false</code> else
     */
    public static boolean isConstraint(IDatatype type) {
        return (type == ConstraintType.TYPE || type instanceof ConstraintType);
    }
    
    /**
     * Returns the help text of <code>var</code>.
     * 
     * @param var the variable to return the description for
     * @return the help text, an empty string or <b>null</b>
     * @see #getHelpText(IModelElement) 
     */
    public static String getHelpText(IDecisionVariable var) {
        return null == var ? null : getHelpText(var.getDeclaration());
    }

    /**
     * Returns the description of <code>var</code>.
     * 
     * @param var the variable to return the description for
     * @return the description, an empty string or <b>null</b>
     * @see #getDescription(IModelElement) 
     */
    public static String getDescription(IDecisionVariable var) {
        return getDescription(var.getDeclaration());
    }

    /**
     * Returns the help text of <code>elt</code>.
     * 
     * @param elt the model element to return the description for
     * @return the help text, an empty string or <b>null</b>
     */
    public static String getHelpText(IModelElement elt) {
        String result; 
        String comment = elt.getComment();
        if (null != comment) {
            int pos = comment.indexOf('|');
            if (pos > 0 && pos + 1 < comment.length()) {
                result = comment.substring(pos + 1);
            } else {
                result = ""; // description is (legacy) default, help text is new and optional
            }
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Returns the description of <code>elt</code>.
     * 
     * @param elt the model element to return the description for
     * @return the description, an empty string or <b>null</b> 
     */
    public static String getDescription(IModelElement elt) {
        String result; 
        String comment = elt.getComment();
        if (null != comment) {
            int pos = comment.indexOf('|');
            if (pos > 0) {
                result = comment.substring(0, pos);
            } else {
                result = comment;
            }
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the label name to be used for <code>var</code>.
     * 
     * @param var
     *            the variable to query for
     * @return the label name
     */
    public static String getLabelName(AbstractVariable var) {
        String labelText = getDescription(var);
        if (null == labelText || 0 == labelText.length()) {
            labelText = var.getName();
        }
        return labelText;
    }

    /**
     * Turns the given <code>typeNames</code> into IVML types defined by the model underlying <code>part</code>.
     * 
     * @param part the model part carrying the model information
     * @param typeNames the type names to be considered (may be <b>null</b>)
     * @return the IVML types (at least an empty array, an array containing all found types)
     */
    static IDatatype[] getTypes(IModelPart part, String[] typeNames) {
        return toArray(getTypesImpl(part, typeNames));
    }

    /**
     * Turns the given collection into an array.
     * 
     * @param tmp the collection to be turned into an array
     * @return the resulting array
     */
    private static IDatatype[] toArray(Collection<IDatatype> tmp) {
        IDatatype[] result = new IDatatype[tmp.size()];
        return tmp.toArray(result);
    }

    /**
     * Turns the given <code>typeNames</code> into IVML types defined by the model underlying <code>part</code>.
     * 
     * @param part the model part carrying the model information
     * @param typeNames the type names to be considered (may be <b>null</b>)
     * @return the IVML types (at least an empty array, an array containing all found types)
     */
    static List<IDatatype> getTypesImpl(IModelPart part, String[] typeNames) {
        List<IDatatype> result = new ArrayList<IDatatype>();
        if (null != typeNames) {
            Configuration cfg = getConfiguration(part.getModelName());
            Project project = cfg.getProject();
            for (int t = 0; t < typeNames.length; t++) {
                try {
                    IDatatype type = ModelQuery.findType(project, typeNames[t], null);
                    if (null != type) {
                        result.add(type);
                    }
                } catch (ModelQueryException e) {
                    Activator.getLogger(ModelAccess.class).exception(e);
                }
            }
        }
        return result;
    }
    
    /**
     * Returns those types from <code>part</code> based on <code>typeNames</code> that
     * are actually instantiable, i.e., no abstract compounds.
     * 
     * @param part the model part carrying the model information
     * @param typeNames the type names to be considered (may be <b>null</b>)
     * @return the instantiable IVML types (at least an empty array, an array containing all found types)
     */
    static IDatatype[] getInstantiableTypes(IModelPart part, String[] typeNames) {
        List<IDatatype> tmp = getTypesImpl(part, typeNames);
        if (!tmp.isEmpty()) {
            Configuration cfg = getConfiguration(part.getModelName());
            Project project = cfg.getProject();
            List<IDatatype> declaredTypes = new ArrayList<IDatatype>();
            for (int e = 0; e < project.getElementCount(); e++) {
                ContainableModelElement elt = project.getElement(e);
                if (elt instanceof IDatatype) {
                    declaredTypes.add((IDatatype) elt);
                }
            }
            int i = 0; 
            while (i < tmp.size()) {
                IDatatype type = tmp.get(i);
                if (type instanceof Compound) {
                    Compound cmp = (Compound) type;
                    for (int d = 0; d < declaredTypes.size(); d++) {
                        IDatatype decl = declaredTypes.get(d);
                        if (decl instanceof Compound && ((Compound) decl).getRefines() == cmp) {
                            declaredTypes.remove(d);
                            tmp.add(decl);
                        }
                    }
                    if (cmp.isAbstract()) {
                        tmp.remove(i);
                        continue; // keep i on the same position
                    }
                }
                i++;
            }
        }
        return toArray(tmp);
    }

    /**
     * Finds the possible values (in terms of variable declarations) for the given <code>modelPart</code> based on the
     * {@link IModelPart#getProvidedTypes() provided types} and the {@link IModelPart#getTopLevelVariables()}.
     * 
     * @param modelPart
     *            the model part to find the values for
     * @return the possible values or an empty list
     */
    static List<AbstractVariable> findPossibleValues(IModelPart modelPart) {
        List<AbstractVariable> result = new ArrayList<AbstractVariable>();
        Configuration cfg = modelPart.getConfiguration();
        Project project = cfg.getProject();

        if (modelPart.getSourceMode().useProvidedTypes()) {
            IDatatype[] types = modelPart.getProvidedTypes();
            if (null != types) {
                for (IDatatype type : types) {
                    List<AbstractVariable> tmp = ReferenceValuesFinder.findPossibleValues(project, type);
                    if (null != tmp) {
                        result.addAll(tmp);
                    }
                }
            }
        }

        if (modelPart.getSourceMode().useVariables()) {
            String[] variables = modelPart.getTopLevelVariables();
            if (null != variables) {
                for (String vName : variables) {
                    try {
                        AbstractVariable var = (AbstractVariable) ModelQuery.findElementByName(project, vName,
                                AbstractVariable.class);
                        if (null != var) {
                            result.add(var);
                        }
                    } catch (ModelQueryException e) {
                        Activator.getLogger(ModelAccess.class).exception(e);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Finds the top-element container matching the given <code>elementType</code>.
     * 
     * @param modelPart
     *            the model part to search within
     * @param elementType
     *            the element type to search for
     * @return the container or <b>null</b> if not found
     */
    public static IDecisionVariable findTopContainer(IModelPart modelPart, IDatatype elementType) {
        IDecisionVariable var = null;
        String[] topVarNames = modelPart.getTopLevelVariables();
        for (int n = 0; null == var && n < topVarNames.length; n++) {
            IDecisionVariable tmp = ModelAccess.obtainVariable(modelPart.getConfiguration(), topVarNames[n]);
            IDatatype tmpType = tmp.getDeclaration().getType();
            if (Container.isContainer(tmpType, elementType)) {
                var = tmp;
            } else {
                // Reference tmpRef = new Reference("", elementType, null); // TODO this is not nice!!!
                // if (Container.isContainer(tmpType, tmpRef)) {
                if (Container.isReferencesContainer(tmpType, elementType)) {
                    var = tmp;
                }
            }
        }
        return var;
    }

    /**
     * Returns if <code>var</code> is a container of references.
     * 
     * @param var
     *            the variable to consider
     * @return <code>true</code> if <code>var</code> is a container of references, <code>false</code> else
     */
    private static boolean isReferencesContainer(IDecisionVariable var) {
        return Container.isReferencesContainer(var.getDeclaration().getType());
    }

    /**
     * Adds a new element, either as value or as variable with reference.
     * 
     * @param modelPart the model part to add the element to
     * @param elementType the element type to create and link (may be <b>null</b> then it is inferred)
     * @return the create decision variable, <b>null</b> in case of failure
     */
    public static IDecisionVariable addNewElement(IModelPart modelPart, IDatatype elementType) {
        return addNewElement(findTopContainer(modelPart, elementType), elementType);
    }

    /**
     * Adds a new element to the container in <code>var</code>, either as value or as variable with reference.
     * 
     * @param var the container variable
     * @param elementType the element type to create and link (may be <b>null</b> then it is inferred) 
     * @return the create decision variable, <b>null</b> in case of failure
     */
    public static IDecisionVariable addNewElement(IDecisionVariable var, IDatatype elementType) {
        return addNewElement(var, elementType, EMPTY_VALUE_PROVIDER);
    }

    /**
     * Adds a new element to the container in <code>var</code>, either as value or as variable with reference.
     * 
     * @param var the container variable
     * @param elementType the element type to create and link (may be <b>null</b> then it is inferred)
     * @param valueProvider the value provider for initial values
     * @return the create decision variable, <b>null</b> in case of failure
     */
    private static IDecisionVariable addNewElement(IDecisionVariable var, IDatatype elementType, 
        INewElementValueProvider valueProvider) {
        IDecisionVariable result = null;
        if (null != var) {
            ContainerVariable con = (ContainerVariable) var;
            Configuration cfg = con.getConfiguration();
            Project prj = cfg.getProject();
            boolean conFrozen = isFrozen(con);
            if (conFrozen) {
                unfreeze(con);
            }
            result = con.addNestedElement();
            // simple case: !isReferencesContainer -> we are finished
            if (null != result && isReferencesContainer(var)) {
                // add variable to model
                if (null == elementType) {
                    elementType = con.getDeclaration().getType().getGenericType(0).getType();
                }
                String varName = obtainVarName(elementType, con);
                DecisionVariableDeclaration newVarDecl = new DecisionVariableDeclaration(varName, elementType, prj);
                try {
                    // force configuration to have the new variable
                    IDecisionVariable newVar = cfg.createDecision(newVarDecl);
                    if (null != newVar) {
                        // we do not need to add this new variable to the project as it is added in the relative project
                        // which will be imported in the main project.
                        // prj.add(newVarDecl); // this shall happen earlier but now it prevents dangling variables
                        // set the variable value
                        newVar.setValue(valueProvider.provideValue(newVarDecl.getType()), AssignmentState.ASSIGNED);
                        setNameSlot(newVar, varName);
                        // set the reference value
                        Value refValue = ValueFactory.createValue(result.getDeclaration().getType(), newVarDecl);
                        result.setValue(refValue, AssignmentState.ASSIGNED);
                        result = newVar;
                        result.freeze(AllFreezeSelector.INSTANCE);
                    }
                    prj.add(newVarDecl);
                } catch (ConfigurationException e) {
                    Dialogs.showErrorDialog("Creating element", e.getMessage());
                } catch (ValueDoesNotMatchTypeException e) {
                    Dialogs.showErrorDialog("Creating element", e.getMessage());
                }
            } else if (null != result) {
                if (null == elementType) {
                    elementType = result.getDeclaration().getType();
                }
                setNameSlot(result, obtainVarName(elementType, con));
            }
            if (conFrozen) {
                con.freeze(AllFreezeSelector.INSTANCE);
            }
            store(var.getConfiguration());
            result = Configuration.mapVariable(result, var.getConfiguration());
        }
        return result;
    }
    
    /**
     * Obtains the name of an automatically created variable.
     * 
     * @param elementType the type of the variable
     * @param con the container to put the variable into
     * @return the name of the created variable (depending on the number of elements in <code>prj</code>)
     */
    private static String obtainVarName(IDatatype elementType, ContainerVariable con) {
        final String varInfix = "Var_";
        int nextIndex = -1;
        for (int e = 0; e < con.getNestedElementsCount(); e++) {
            AbstractVariable var = IVMLModelOperations.getDeclaration(con.getNestedElement(e));
            if (null != var) {
                String varName = var.getName();
                int pos = varName.indexOf(varInfix);
                int startPos = pos + varInfix.length();
                if (pos > 0 && startPos <= varName.length() - 1) {
                    String tmp = varName.substring(startPos);
                    try {
                        nextIndex = Math.max(nextIndex, Integer.parseInt(tmp) + 1);
                    } catch (NumberFormatException ex) {
                        EASyLoggerFactory.INSTANCE.getLogger(ModelAccess.class, 
                            Activator.PLUGIN_ID).error(ex.getMessage());
                    }
                }
            }
        }
        if (nextIndex < 0) {
            nextIndex = con.getNestedElementsCount();
        }
        return elementType.getName() + varInfix + nextIndex;
    }

    /**
     * Creates a new unconfigured element but it does not add the related variable to the model (although needed).
     * This method is intended for pipeline creation.
     * 
     * @param modelPart the model part to add the element to
     * @param eltType the element type to create and link
     * @param createProject if <code>true</code> create a new project, use <code>modelPart</code> else
     * @return the create decision variable, <b>null</b> in case of failure
     */
    public static IDecisionVariable createNewElement(IModelPart modelPart, IDatatype eltType, boolean createProject) {
        return createNewElement(modelPart, eltType, createProject, EMPTY_VALUE_PROVIDER);
    }
    
    /**
     * Creates a new unconfigured element but it does not add the related variable to the model (although needed).
     * This method is intended for pipeline creation.
     * 
     * @param modelPart the model part to add the element to
     * @param eltType the element type to create and link
     * @param createProject if <code>true</code> create a new project, use <code>modelPart</code> else
     * @param valueProvider the value provider for initial values
     * @return the create decision variable, <b>null</b> in case of failure
     */
    public static IDecisionVariable createNewElement(IModelPart modelPart, IDatatype eltType, boolean createProject, 
        INewElementValueProvider valueProvider) {
        IDecisionVariable var = findTopContainer(modelPart, eltType);
        IDecisionVariable result = null;
        if (null != var) {
            ContainerVariable container = (ContainerVariable) var;
            if (isReferencesContainer(var)) {  
                IDatatype elementType = eltType; //container.getDeclaration().getType().getGenericType(0).getType();
                String varName = obtainVarName(elementType, container);
                Configuration containerConfiguration = container.getConfiguration();
                Configuration newElementConfiguration = containerConfiguration;
                String modelName = varName + VariabilityModel.CFG_POSTFIX;
                AvailableModels<Project> avail = VarModel.INSTANCE.availableModels();
                Project targetProject;
                if (createProject) {
                    newElementConfiguration = getConfiguration(modelName);
                    targetProject = newElementConfiguration.getProject();
                    ModelInfo<Project> prjInfo = avail.getModelInfo(targetProject);
                    if (null == prjInfo || null == prjInfo.getLocation()) {
                        ModelInfo<Project> info = avail.getModelInfo(modelPart.getConfiguration().getProject());
                        File infoFile = new File(info.getLocation());
                        File infoParent = infoFile.getParentFile();
                        File prjFile = new File(infoParent, modelName + ".ivml");
                        VarModel.INSTANCE.updateModel(targetProject, prjFile.toURI());
                    } 
                    newElementConfiguration = getConfiguration(modelName);
                    targetProject = newElementConfiguration.getProject();
                    addImport(targetProject, Definition.PIPELINES);
                    //freeze = IVMLModelOperations.addRuntimeAttributeToProject(targetProject, null);
                } else {
                    targetProject = newElementConfiguration.getProject();
                }
                try {
                    // create the variable for the target project (new project or existing) and fill it 
                    DecisionVariableDeclaration newVarDecl = new DecisionVariableDeclaration(
                        varName, elementType, targetProject);
                    targetProject.add(newVarDecl);
                    result = newElementConfiguration.createDecision(newVarDecl);
                    if (null == result) {
                        Dialogs.showErrorDialog("Cannot create pipeline", "Model still exists. Please clean up.");    
                    } else {
                        result.setValue(valueProvider.provideValue(newVarDecl.getType()), AssignmentState.ASSIGNED);
                        setNameSlot(result, varName);
                        if (containerConfiguration != newElementConfiguration) {
                            // store the new element configuration only, if we created a new project. 
                            // model update cycle will not apply, as the new project is not imported so far
                            store(newElementConfiguration);
                        } else {
                            result.freeze(AllFreezeSelector.INSTANCE); // exception for pipelines... editor overwrites
                        }
                        // add the reference to the new variable to the container
                        IDecisionVariable conVar = container.addNestedElement();
                        conVar.setValue(ValueFactory.createValue(conVar.getDeclaration().getType(), newVarDecl), 
                            AssignmentState.ASSIGNED);
                        // if a new project was created, import it into the existing project
                        if (containerConfiguration != newElementConfiguration) {
                            addImport(containerConfiguration.getProject(), targetProject);
                        }
                        // store the container configuration and update the models
                        store(containerConfiguration);
                    }
                } catch (ValueDoesNotMatchTypeException | ConfigurationException e) {
                    Dialogs.showErrorDialog("Creating element", e.getMessage());
                }
            }
        } 
        return result;
    }
    
    /**
     * Add an import of <code>imported</code> to <code>target</code>.
     * 
     * @param target the target project
     * @param imported the imported project
     */
    private static void addImport(Project target, IModelPart imported) {
        addImport(target, imported.getConfiguration().getProject());
    }
    
    /**
     * Add an import of <code>imported</code> to <code>target</code>.
     * 
     * @param target the target project
     * @param imported the imported project
     */
    private static void addImport(Project target, Project imported) {
        try {
            ProjectImport imp = new ProjectImport(imported.getName(), null);
            imp.setResolved(imported);
            target.addImport(imp);
        } catch (ModelManagementException e) {
            Dialogs.showErrorDialog("Importing element", e.getMessage());
        }
    }

    /**
     * Adds an element to the container in <code>var</code> with <code>varName</code>.
     * 
     * @param var
     *            the container variable
     * @param name
     *            the name of the variable to be added
     */
    public static void addPipelineElementWithName(IDecisionVariable var, String name) {
        IDecisionVariable result = null;
        if (null != var) {
            ContainerVariable con = (ContainerVariable) var;
            Configuration cfg = con.getConfiguration();
            Project prj = cfg.getProject();
            boolean conFrozen = isFrozen(con);
            if (conFrozen) {
                unfreeze(con);
            }
            // simple case: !isReferencesContainer -> we are finished
            if (isReferencesContainer(var)) {
                // add variable to model
                IDatatype elementType = con.getDeclaration().getType().getGenericType(0).getType();
                DecisionVariableDeclaration newVarDecl = new DecisionVariableDeclaration(name, elementType, prj);
                try {
                    result = con.addNestedElement();
                    // set the reference value
                    Value refValue = ValueFactory.createValue(result.getDeclaration().getType(), newVarDecl);
                    result.setValue(refValue, AssignmentState.ASSIGNED);
                } catch (ConfigurationException e) {
                    Dialogs.showErrorDialog("Creating element", e.getMessage());
                } catch (ValueDoesNotMatchTypeException e) {
                    Dialogs.showErrorDialog("Creating element", e.getMessage());
                }
            } 
            if (conFrozen) {
                con.freeze(AllFreezeSelector.INSTANCE);
            }
        }

    }


    /**
     * Sets the value of the name slot if it exists.
     * 
     * @param var
     *            the variable to set the name slot for
     * @param name
     *            the actual value for the name slot
     */
    private static void setNameSlot(IDecisionVariable var, String name) {
        if (var instanceof CompoundVariable) {
            IDecisionVariable nameVar = ((CompoundVariable) var).getNestedVariable(VariabilityModel.DISPLAY_NAME_SLOT);
            if (null != nameVar) {
                try {
                    Value nameVal = ValueFactory.createValue(StringType.TYPE, name);
                    nameVar.setValue(nameVal, AssignmentState.ASSIGNED);
                } catch (ValueDoesNotMatchTypeException e) {
                    // did not work - ignore
                    Activator.getLogger(ModelAccess.class).exception(e);
                } catch (ConfigurationException e) {
                    // did not work - ignore
                    Activator.getLogger(ModelAccess.class).exception(e);
                }
            }
        }
    }

    /**
     * Returns whether the given <code>variable</code> is frozen. Please note that this method may not correspond to the
     * EASy definition of frozen (all contained values), but it complies with the simple freezing protocol of this
     * application.
     * 
     * @param element
     *            the element to consider the state from
     * @return <code>true</code> if <code>element</code> is frozen, <code>false</code> else
     */
    private static boolean isFrozen(IConfigurationElement element) {
        return AssignmentState.FROZEN == element.getState();
    }

    /**
     * Default unfreeze of <code>element</code> (to {@link AssignmentState#ASSIGNED}).
     * 
     * @param element
     *            the element to be unfrozen
     */
    private static void unfreeze(IConfigurationElement element) {
        element.unfreeze(AssignmentState.ASSIGNED);
    }

    /**
     * Returns the global index for <code>variable</code>.
     * 
     * @param modelPart
     *            the part holding <code>variable</code> and the containing collection
     * @param variable
     *            the variable to be deleted
     * @return the global index of <code>variable</code> in its configuring collection before deletion
     */
    public static int getGlobalIndex(IModelPart modelPart, IDecisionVariable variable) {
        int globalIndex = -1;
        IDecisionVariable cont = findTopContainer(modelPart, variable.getDeclaration().getType());
        if (cont instanceof ContainerVariable) {
            globalIndex = ((ContainerVariable) cont).indexOf(variable);
        }
        return globalIndex;
    }

    /**
     * Turns an <code>IDecisionVariable</code> from the global index.
     * 
     * @param modelPart
     *            the part to search for <code>IDecisionVariable</code>
     * @param type
     *            the type of the variable to be searched
     * @param index
     *            the global index of the variable to be searched
     * @return a <code>IDecisionVariable</code> from the global index
     */
    public static IDecisionVariable getFromGlobalIndex(IModelPart modelPart, IDatatype type, int index) {
        IDecisionVariable variable = null;
        IDecisionVariable cont = findTopContainer(modelPart, type);
        if (0 <= index && index < cont.getNestedElementsCount()) {
            variable = cont.getNestedElement(index);
        }
        // TODO Auto-generated method stub
        return variable;
    }

    /**
     * Deletes an element from the model.
     * 
     * @param modelPart
     *            the containing model parts
     * @param variable
     *            the variable to be deleted
     */
    public static void deleteElement(IModelPart modelPart, IDecisionVariable variable) {
        if (isFrozen(variable)) {
            unfreeze(variable);
        }
        IConfigurationElement par = variable.getParent();
        if (par instanceof ContainerVariable) {
            removeFromContainer((ContainerVariable) par, variable);
        } else { // check whether there are other alternatives
            par = findTopContainer(modelPart, variable.getDeclaration().getType());
            if (par instanceof ContainerVariable) {
                removeFromContainer((ContainerVariable) par, variable);
            }                
            Configuration cfg = variable.getConfiguration();
            cfg.removeDecision(variable);
            cfg.getProject().removeElement(variable.getDeclaration());
        }
        store(modelPart.getConfiguration());
    }

    /**
     * Removes <code>variable</code> from container <code>cont</code>, considering that <code>cont</code>
     * may be a container of references.
     * 
     * @param cont the container to remove from
     * @param variable the variable to remove from <code>cont</code>
     */
    private static void removeFromContainer(ContainerVariable cont, IDecisionVariable variable) {
        IDecisionVariable toRemove = null;
        for (int i = 0; null == toRemove && i < cont.getNestedElementsCount(); i++) {
            IDecisionVariable nVar = cont.getNestedElement(i);
            if (Configuration.dereference(nVar).equals(variable)) {
                toRemove = nVar;
            }
        }
        if (null == toRemove) {
            toRemove = variable;
        }
        if (null != toRemove) {
            boolean isFrozen = isFrozen(cont);
            if (isFrozen) {
                unfreeze(cont);
            }
            cont.removeNestedElement(toRemove);
            if (isFrozen) {
                cont.freeze(AllFreezeSelector.INSTANCE);
            }
        }
    }

    /**
     * Clones the given element.
     * 
     * @param modelPart the model part to clone within, i.e., to find the top level container for <code>variable</code>
     * @param variable the element to be cloned
     * @param count the number of clones to be created
     * @param createProject if <code>true</code> create a new project, use <code>modelPart</code> else
     * @return the created clones (may be <b>null</b>)
     */
    public static List<IDecisionVariable> cloneElement(IModelPart modelPart, final IDecisionVariable variable, 
        boolean createProject, int count) {
        List<IDecisionVariable> result = null;
        final IDatatype elementType = variable.getDeclaration().getType();
        IDecisionVariable topCVar = findTopContainer(modelPart, elementType);
        if (topCVar instanceof ContainerVariable) {
            final ContainerVariable con = (ContainerVariable) topCVar;
            //String namePrefix = getDisplayName(variable);
            result = new ArrayList<IDecisionVariable>();
            INewElementValueProvider valueProvider = new INewElementValueProvider() {

                @Override
                public Value provideValue(IDatatype type) throws ValueDoesNotMatchTypeException {
                    Value value = variable.getValue().clone();
                    if (value instanceof CompoundValue) {
                        String newName = obtainVarName(elementType, con);
                        ((CompoundValue) value).configureValue(VariabilityModel.DISPLAY_NAME_SLOT, newName);
                    }
                    return value;
                }
                
            };
            for (int i = 0; i < count; i++) {
                IDecisionVariable newElement;
                if (isReferencesContainer(con)) {
                    newElement = createNewElement(modelPart, elementType, createProject, valueProvider);
                } else {
                    newElement = addNewElement(con, variable.getDeclaration().getType(), valueProvider);
                }
                /*boolean frozen = isFrozen(newElement);
                if (frozen) {
                    unfreeze(newElement);
                }
                /*try {
                    Value val = variable.getValue().clone();
                    newElement.setValue(val, AssignmentState.ASSIGNED);
                    if (val instanceof CompoundValue) {
                        ((CompoundValue) val).configureValue(VariabilityModel.DISPLAY_NAME_SLOT,
                                namePrefix + "_" + con.getNestedElementsCount());
                    }
                } catch (ConfigurationException e) {
                    Dialogs.showErrorDialog("Cloning element", e.getMessage());
                } catch (ValueDoesNotMatchTypeException e) {
                    Dialogs.showErrorDialog("Cloning element", e.getMessage());
                }
                if (frozen) {
                    newElement.freeze(AllFreezeSelector.INSTANCE);
                }*/
                result.add(newElement);
            }
        }
        //store(variable.getConfiguration());
        return result;
    }

    /**
     * Returns whether <code>variable</code> is referenced in the given model part.
     * 
     * @param part
     *            the part to search
     * @param variable
     *            the variable to search for
     * @param defining
     *            the model parts defining top-level variables (and may reference, i.e., shall be excluded)
     * @return <code>true</code> if <code>variable</code> is referenced, <code>false</code> else
     */
    public static boolean isReferencedIn(IModelPart part, IDecisionVariable variable, IModelPart... defining) {
        Set<AbstractVariable> excluded = new HashSet<AbstractVariable>();
        for (int d = 0; d < defining.length; d++) {
            IDecisionVariable top = findTopContainer(defining[d], variable.getDeclaration().getType());
            if (null != top) {
                excluded.add(top.getDeclaration());
            }
        }
        IsReferencedVisitor vis = new IsReferencedVisitor(variable, excluded);
        part.getConfiguration().accept(vis);
        return vis.isReferenced();
    }

    /**
     * Returns whether a decision variable is visible. Delegates 
     * to {@link VariabilityModel#isVisible(IDecisionVariable)}.
     * 
     * @param variable
     *            the variable to be considered
     * @return <code>true</code> if the variable is visible, <code>false</code> else
     */
    public static boolean isVisible(IDecisionVariable variable) {
        return VariabilityModel.isVisible(variable);
    }

    /**
     * Updates the given project, i.e., the project must have been loaded by EASy before calling this method.
     * Opens an error dialog in case of I/O problems.
     * 
     * @param project
     *            the project to be stored
     * @return <code>true</code> if successful, <code>false</code> else
     */
    public static boolean updateModel(Project project) {
        boolean done = false;
        ModelInfo<Project> infos = VarModel.INSTANCE.availableModels().getModelInfo(project);
        if (null != infos) {
            VarModel.INSTANCE.updateModel(project, infos.getLocation());
            done = true;
        }
        return done;
    }
    
    /**
     * Stores and updates the given project, i.e., the project must have been loaded by EASy before calling this method.
     * Opens an error dialog in case of I/O problems.
     * 
     * @param project
     *            the project to be stored
     * @return <code>true</code> if successful, <code>false</code> else
     */
    public static boolean storeAndUpdateModel(Project project) {
        // TODO delete if not needed
        boolean done = false;
        List<ModelInfo<Project>> infos = VarModel.INSTANCE.availableModels().getModelInfo(project.getName());
        if (null != infos && 1 == infos.size()) {
            URI uri = infos.get(0).getLocation();
            // turn it into configuration and use reference-based model saver?
            FileWriter writer = null;
            try {
                writer = new FileWriter(new File(uri));
                IVMLWriter iWriter = IVMLWriter.getInstance(writer);
                iWriter.setFormatInitializer(true);
                iWriter.forceComponundTypes(true);
                project.accept(iWriter);
                IVMLWriter.releaseInstance(iWriter);
                writer.close();
                VarModel.INSTANCE.updateModel(project, uri);
                done = true;
            } catch (IOException e) {
                IOUtils.closeQuietly(writer);
                Dialogs.showErrorDialog("Storing configuration", ModelAccess.class, e);
            }
        }
        return done;
    }
    
    /**
     * Provides file access to the main rt-VIL file.
     * 
     * @return file access (may be <b>null</b> if model / file is out of workspace)
     */
    public static IFile getMainRtVILfile() {
        // http://stackoverflow.com/questions/8239458/cannot-open-workspace-external-file-in-xtext-based-plugin
        IFile result;
        File modelLocation = Location.getModelLocationFile();
        IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
        if (modelLocation.toURI().toString().startsWith(wsRoot.getLocationURI().toString())) {
            IProject project = wsRoot.getProject(modelLocation.getName());
            IPath externalPath = new Path("EASy/" + getProjectName() + "_0.rtvil");
            result = project.getFile(externalPath);
        } else {
            result = null;
        }
        return result;
    }
    
    /**
     * Returns the overall project name.
     * 
     * @return the project name
     */
    public static String getProjectName() {
        return "QM";
    }
    
    /**
     * Returns whether the given type is considered to be visible (to the user, to IVML writing, ...).
     * 
     * @param type the type
     * @return <code>true</code> if it is visible, <code>false</code> else
     */
    public static boolean isVisibleType(IDatatype type) {
        return BooleanType.TYPE.isAssignableFrom(type) // ConstraintType implies Boolean to be assignable
            || !ConstraintType.TYPE.isAssignableFrom(type); 
    }

    /**
     * Returns the Maven URL where to receive artifacts from.
     * 
     * @return the Maven URL
     */
    public static String getRepositoryUrl() {
        String result = null;
        IDecisionVariable var = obtainVariable(VariabilityModel.Configuration.INFRASTRUCTURE, "repositoryURL");
        if (null != var) {
            Value val = var.getValue();
            if (val instanceof StringValue) {
                result = ((StringValue) val).getValue();
            }
        }
        return result;
    }

    /**
     * Returns the (Nexus) URL where to deploy pipelines to.
     * 
     * @return the deployment URL (may be empty, may be {@link #getRepositoryUrl()}
     */
    public static String getDeploymentUrl() {
        String result = null;
        IDecisionVariable var = obtainVariable(VariabilityModel.Configuration.INFRASTRUCTURE, "deploymentURL");
        if (null != var) {
            Value val = var.getValue();
            if (val instanceof StringValue) {
                result = ((StringValue) val).getValue();
            }
        }
        return result;
    }

}
