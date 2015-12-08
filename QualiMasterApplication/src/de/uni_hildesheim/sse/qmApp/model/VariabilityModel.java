package de.uni_hildesheim.sse.qmApp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pipeline.impl.DataManagementElementImpl;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.FlowImpl;
import pipeline.impl.PipelineImpl;
import pipeline.impl.SinkImpl;
import pipeline.impl.SourceImpl;
import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.ConstraintType;
import de.uni_hildesheim.sse.model.varModel.datatypes.EnumLiteral;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.values.BooleanValue;
import de.uni_hildesheim.sse.model.varModel.values.EnumValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.qmApp.editorInput.EmptyEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator.CloneMode;
import de.uni_hildesheim.sse.qmApp.editorInput.RtVilEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.VarModelEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editors.AlgorithmEditor;
import de.uni_hildesheim.sse.qmApp.editors.ArtifactEditor;
import de.uni_hildesheim.sse.qmApp.editors.ConstraintsEditor;
import de.uni_hildesheim.sse.qmApp.editors.FamilyEditor;
import de.uni_hildesheim.sse.qmApp.editors.MultipleLineText;
import de.uni_hildesheim.sse.qmApp.editors.ParameterEditor;
import de.uni_hildesheim.sse.qmApp.editors.SourceSinkEditor;
import de.uni_hildesheim.sse.qmApp.editors.TuplesEditor;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.tabbedViews.PipelineDiagramElementPropertyEditorCreator;
import de.uni_hildesheim.sse.qmApp.tabbedViews.PropertyEditorFactory;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElement;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElements;
import de.uni_hildesheim.sse.qmApp.treeView.DecisionVariableElementFactory;
import de.uni_hildesheim.sse.qmApp.treeView.IConfigurableElementFactory;
import de.uni_hildesheim.sse.qmApp.treeView.PipelineElementFactory;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.ApplicationRole;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;

/**
 * Defines and initializes model-specific data and functionality. 
 * Methods in this class are being called from outside.<br/>
 * <br/>
 * 
 * This class may be created automatically later on.
 * 
 * @author Holger Eichelberger
 */
public class VariabilityModel {

    /**
     * The slot name denoting the display name in the IVML model. Empty string if no display name slot is provided by
     * the IVML model.
     */
    public static final String DISPLAY_NAME_SLOT = "name";

    /**
     * Model name postfix to distinguish model definition and configuration model parts.
     */
    public static final String CFG_POSTFIX = "Cfg";

    static final String BINDING_TIME_NAME = "bindingTime";
    static final String USER_VISIBLE_NAME = "userVisible";

    static final String BINDING_TIME_LITERAL_VISIBLE = "compile";

    private static final Map<IModelPart, CloneMode> CLONEABLES;
   
    static {
        CLONEABLES = new HashMap<IModelPart, CloneMode>();
        CLONEABLES.put(Configuration.HARDWARE, CloneMode.MULTI);
        CLONEABLES.put(Configuration.RECONFIG_HARDWARE, CloneMode.MULTI);
        CLONEABLES.put(Configuration.ADAPTIVITY, CloneMode.NONE);
        CLONEABLES.put(Configuration.ALGORITHMS, CloneMode.SINGLE);
        CLONEABLES.put(Configuration.BASICS, CloneMode.SINGLE); // types
        CLONEABLES.put(Configuration.DATA_MANAGEMENT, CloneMode.SINGLE);
        CLONEABLES.put(Configuration.FAMILIES, CloneMode.SINGLE);
        CLONEABLES.put(Configuration.INFRASTRUCTURE, CloneMode.NONE); // singleton
        CLONEABLES.put(Configuration.OBSERVABLES, CloneMode.NONE); // not now
        CLONEABLES.put(Configuration.PIPELINES, CloneMode.NONE); // not now
    }

    private static final IModelPart[] DELETABLES = Configuration.values(); // for the moment

    private static Map<String, Role[]> mapRead = new HashMap<>();

    private static Map<String, Role[]> mapWrite = new HashMap<>();

    /**
     * Prevents external creation.
     */
    private VariabilityModel() {
    }

    /**
     * Defines all definition parts of the underlying variability model.
     * 
     * @author Holger Eichelberger
     */
    public enum Definition implements IModelPart {
        BASICS("Basics",
            new String[] {"types"},
            new String[] {"FieldType"},
            SourceMode.VARIABLES),
        HARDWARE("Hardware", 
            new String[] {"machines"}, 
            new String[] {"Machine"}, 
            SourceMode.VARIABLES), 
        RECONFIG_HARDWARE("ReconfigurableHardware", 
            new String[] {"clusters"}, 
            new String[] {"HwNode"},
            SourceMode.VARIABLES), 
        DATA_MANAGEMENT("DataManagement", 
            new String[] {"dataSources", "dataSinks", "persistentDataElements"},
            new String[] {"DataSource", "DataSink", "PersistentDataElement"}, 
            SourceMode.VARIABLES, 
            new DecisionVariableElementFactory(SourceSinkEditor.ID)), 
        ALGORITHMS("Algorithms", 
            new String[] {"algorithms"}, 
            new String[] {"Algorithm"}, 
            SourceMode.VARIABLES,
            new DecisionVariableElementFactory(AlgorithmEditor.ID)), 
        FAMILIES("Families", 
            new String[] {"families"}, 
            new String[] {"Family"}, 
            SourceMode.VARIABLES, 
            new DecisionVariableElementFactory(FamilyEditor.ID)), 
        OBSERVABLES("Observables", 
            new String[] {"configuredParameters"}, 
            new String[] {"ConfiguredQualityParameter"}, 
            SourceMode.VARIABLES), // add provided types
        ADAPTIVITY("Adaptivity", 
            new String[] {"pipelineImportance", "crossPipelineTradeoffs"}, 
            new String[] {"QualityParameterWeighting", "QualityParameterWeighting"}, 
            SourceMode.VARIABLES), // add provided types
        PIPELINES("Pipelines", 
            new String[] {"pipelines"}, 
            new String[] {"Pipeline"}, 
            SourceMode.VARIABLES,
            PipelineElementFactory.INSTANCE), 
        INFRASTRUCTURE("Infrastructure", 
            new String[] {"pipelines"}, 
            null,
            SourceMode.VARIABLES), // no top-level type so far
        TOP_LEVEL("QM", 
            null, 
            null, 
            SourceMode.VARIABLES);

        private String modelName;
        private String[] topLevelVariables;
        private String[] providedTypeNames;
        private SourceMode sourceMode;
        private IConfigurableElementFactory factory;

        /**
         * Creates a definition "constant" value using {@link DecisionVariableElementFactory#VARIABLE_EDITOR} as
         * default.
         * 
         * @param modelName
         *            the model name
         * @param topLevelVariables
         *            the top level variables containing the configured elements (may be <b>null</b>)
         * @param providedTypeNames
         *            the top level type names provided by the model (may be <b>null</b>)
         * @param sourceMode
         *            the source mode, i.e., whether variables or types shall be used for deriving
         *            {@link #getPossibleValues()}.
         */
        private Definition(String modelName, String[] topLevelVariables, String[] providedTypeNames,
            SourceMode sourceMode) {
            this(modelName, topLevelVariables, providedTypeNames, sourceMode,
                DecisionVariableElementFactory.VARIABLE_EDITOR);
        }

        /**
         * Creates a definition "constant" value.
         * 
         * @param modelName
         *            the model name
         * @param topLevelVariables
         *            the top level variables containing the configured elements (may be <b>null</b>)
         * @param providedTypeNames
         *            the top level type names provided by the model (may be <b>null</b>)
         * @param sourceMode
         *            the source mode, i.e., whether variables or types shall be used for deriving
         *            {@link #getPossibleValues()}.
         * @param factory
         *            the configurable elements factory associated to the model
         */
        private Definition(String modelName, String[] topLevelVariables, String[] providedTypeNames,
            SourceMode sourceMode, IConfigurableElementFactory factory) {
            this.modelName = modelName;
            this.topLevelVariables = topLevelVariables == null ? new String[0] : topLevelVariables;
            this.providedTypeNames = providedTypeNames;
            this.sourceMode = sourceMode;
            this.factory = factory;
        }

        @Override
        public IModelPart getDefinition() {
            return this;
        }

        @Override
        public String getModelName() {
            return modelName;
        }

        @Override
        public de.uni_hildesheim.sse.model.confModel.Configuration getConfiguration() {
            return ModelAccess.getConfiguration(this);
        }

        @Override
        public IDatatype[] getProvidedTypes() {
            return ModelAccess.getInstantiableTypes(this, providedTypeNames);
        }

        @Override
        public String[] getProvidedTypeNames() {
            return providedTypeNames;
        }

        @Override
        public String[] getTopLevelVariables() {
            return topLevelVariables;
        }

        @Override
        public List<AbstractVariable> getPossibleValues() {
            return ModelAccess.findPossibleValues(this);
        }

        @Override
        public SourceMode getSourceMode() {
            return sourceMode;
        }

        @Override
        public IConfigurableElementFactory getElementFactory() {
            return factory;
        }

        @Override
        public boolean addOnCreation() {
            return this != Definition.PIPELINES;
        }

    }

    /**
     * Defines all configuration parts of the underlying variability model.
     * 
     * @author Holger Eichelberger
     */
    public enum Configuration implements IModelPart {
        BASICS(Definition.BASICS),
        HARDWARE(Definition.HARDWARE), 
        RECONFIG_HARDWARE(Definition.RECONFIG_HARDWARE), 
        DATA_MANAGEMENT(Definition.DATA_MANAGEMENT), 
        ALGORITHMS(Definition.ALGORITHMS), 
        FAMILIES(Definition.FAMILIES), 
        OBSERVABLES(Definition.OBSERVABLES), 
        ADAPTIVITY(Definition.ADAPTIVITY), 
        PIPELINES(Definition.PIPELINES), 
        INFRASTRUCTURE(Definition.INFRASTRUCTURE);

        private Definition definition;

        /**
         * Creates a configuration "constant" value.
         * 
         * @param definition
         *            the defining model part
         */
        private Configuration(Definition definition) {
            this.definition = definition;
        }

        @Override
        public IModelPart getDefinition() {
            return definition;
        }

        @Override
        public String getModelName() {
            return definition.getModelName() + CFG_POSTFIX;
        }

        @Override
        public de.uni_hildesheim.sse.model.confModel.Configuration getConfiguration() {
            return ModelAccess.getConfiguration(this);
        }

        @Override
        public String[] getProvidedTypeNames() {
            return definition.getProvidedTypeNames();
        }
        
        @Override
        public IDatatype[] getProvidedTypes() {
            return definition.getProvidedTypes();
        }

        @Override
        public String[] getTopLevelVariables() {
            return definition.getTopLevelVariables();
        }

        @Override
        public List<AbstractVariable> getPossibleValues() {
            return ModelAccess.findPossibleValues(this);
        }

        @Override
        public SourceMode getSourceMode() {
            return definition.getSourceMode();
        }

        @Override
        public IConfigurableElementFactory getElementFactory() {
            return definition.getElementFactory();
        }

        @Override
        public boolean addOnCreation() {
            return this != Configuration.PIPELINES;
        }

    }

    /**
     * Returns all model parts.
     * 
     * @return all model parts
     */
    public static IModelPart[] allModelParts() {
        IModelPart[] result;
        List<IModelPart> tmp = new ArrayList<IModelPart>();
        Utils.addAll(tmp, Configuration.values());
        Utils.addAll(tmp, Definition.values());
        result = new IModelPart[tmp.size()];
        return tmp.toArray(result);
    }
    
    /**
     * Returns the model part corresponding to the given model name.
     * 
     * @param modelName the model name to search for
     * @return the corresponding model part (may be <b>null</b> if none was found)
     */
    public static IModelPart findModelPart(String modelName) {
        IModelPart result = null;
        for (IModelPart mp : allModelParts()) {
            if (mp.getModelName().equals(modelName)) {
                result = mp;
                break;
            }
        }
        return result;
    }

    /**
     * Defines the default property editor creators.
     */
    public static void initPropertyEditorCreators() {
        // TODO move to Cui's part ;)
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(SourceImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(SinkImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(FamilyElementImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(
                DataManagementElementImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(FlowImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(PipelineImpl.class));
    }

    /**
     * Creates the configurable elements.
     * 
     * @param elements
     *            the elements data structure to be modified as a side effect
     */
    public static void createConfigurationElements(ConfigurableElements elements) {
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.BASICS, "Types");
        elements.variableToConfigurableElements(Configuration.BASICS, "de.uni_hildesheim.sse.qmApp.TypeEditor");
        
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.HARDWARE, 
            "General-purpose Machines");
        elements.variableToConfigurableElements(Configuration.HARDWARE, "de.uni_hildesheim.sse.qmApp.HardwareEditor");

        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.RECONFIG_HARDWARE, 
            "Reconfigurable Hardware Machines");
        elements.variableToConfigurableElements(Configuration.RECONFIG_HARDWARE, 
            "de.uni_hildesheim.sse.qmApp.ReconfigurableHardwareEditor");
        
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.DATA_MANAGEMENT, 
            "Data Management");
        elements.variableToConfigurableElements(Configuration.DATA_MANAGEMENT, 
            "de.uni_hildesheim.sse.qmApp.DataManagementEditor");
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.FAMILIES, 
            "Algorithm Families");
        elements.variableToConfigurableElements(Configuration.FAMILIES, "de.uni_hildesheim.sse.qmApp.FamiliesEditor");
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.ALGORITHMS, "Algorithms");
        elements.variableToConfigurableElements(Configuration.ALGORITHMS, 
             "de.uni_hildesheim.sse.qmApp.AlgorithmsEditor");

        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.PIPELINES, "Pipelines");
        elements.variableToConfigurableElements(Configuration.PIPELINES, "de.uni_hildesheim.sse.qmApp.PipelinesEditor");

        String tmp = "Infrastructure";
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.INFRASTRUCTURE, tmp);
        elements.addElement(tmp, "de.uni_hildesheim.sse.qmApp.InfrastructureEditor",
            new VarModelEditorInputCreator(Configuration.INFRASTRUCTURE, "Infrastructure"),
            Configuration.INFRASTRUCTURE);
        
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.OBSERVABLES, "Observables");
        elements.variableToConfigurableElements(Configuration.OBSERVABLES, 
                        "de.uni_hildesheim.sse.qmApp.ObservablesEditor");

        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.OBSERVABLES, "Adaptation");
        ConfigurableElement elt = elements.variableToConfigurableElements(Configuration.OBSERVABLES, 
                        "de.uni_hildesheim.sse.qmApp.AdaptationEditor");
        elt.setImage(IconManager.retrieveImage(IconManager.ADAPTATION));
        IEditorInputCreator editorInput = new RtVilEditorInputCreator();
        if (editorInput.isEnabled()) {
            ConfigurableElement rtVIL = new ConfigurableElement(elt, "rt-VIL", "de.uni_hildesheim.sse.vil.rt.RtVil", 
                editorInput);
            rtVIL.setImage(IconManager.retrieveImage(IconManager.RTVIL)); // TODO preliminary - take from rtVIL
            rtVIL.setMenuContributor(new RtVilMenuContributor());
            elt.addChild(rtVIL);
        }
        boolean demoMode = ConfigurationProperties.DEMO_MODE.getBooleanValue();
        if (!demoMode) {
            elt = elements.addElement("Runtime", "de.uni_hildesheim.sse.qmApp.RuntimeEditor", 
                    new EmptyEditorInputCreator("Runtime Input"), null);
            elt.setImage(IconManager.retrieveImage(IconManager.RUNTIME));
        }
    }

    /**
     * Returns whether <code>variable</code> is cloneable.
     * 
     * @param modelPart the modelPart to be checked
     * @return the clone mode
     */
    public static CloneMode isCloneable(IModelPart modelPart) {
        CloneMode mode = CLONEABLES.get(modelPart);
        if (null == mode) {
            mode = CloneMode.NONE;
        }
        return mode;
    }

    /**
     * Returns whether <code>modelPart</code> is deletable.
     * 
     * @param modelPart the variable to be checked
     * @return <code>true</code> if this element is deletable, <code>false</code> else
     */
    public static boolean isDeletable(IModelPart modelPart) {
        return Utils.contains(DELETABLES, modelPart);
    }

    /**
     * Returns whether the given variable is a name slot.
     * 
     * @param variable
     *            the variable to check
     * @return <code>true</code> if it is a name slot, <code>false</code> else
     */
    public static boolean isNameSlot(IDecisionVariable variable) {
        return isNameSlot(variable.getDeclaration());
    }

    /**
     * Returns whether the given variable declaration is a name slot.
     * 
     * @param decl
     *            the declaration
     * @return <code>true</code> if it is a name slot, <code>false</code> else
     */
    public static boolean isNameSlot(AbstractVariable decl) {
        return DISPLAY_NAME_SLOT.equals(decl.getName()) && StringType.TYPE.isAssignableFrom(decl.getType());
    }

    /**
     * Assigns roles to a specified {@link IModelPart} for {@link #isReadable(IModelPart)}.
     * 
     * @param part The specified {@link IModelPart}
     * @param roles List with roles
     */
    public static void addReadRole(IModelPart part, Role... roles) {
        mapRead.put(part.getModelName(), roles);
    }

    /**
     * Assigns roles to a specified {@link IModelPart} for {@link #isWritable(IDecisionVariable)}.
     * 
     * @param part The specified {@link IModelPart}
     * @param roles List with roles
     */
    public static void addWriteRole(IModelPart part, Role... roles) {
        mapWrite.put(part.getModelName(), roles);
    }

    static {
        Role[] allRoles = {
            ApplicationRole.ADMIN, 
            ApplicationRole.INFRASTRUCTURE_ADMIN,
            ApplicationRole.ADAPTATION_MANAGER, 
            ApplicationRole.PIPELINE_DESIGNER 
        };
        addReadRole(Configuration.BASICS, allRoles);
        addReadRole(Configuration.HARDWARE, allRoles);
        addReadRole(Configuration.PIPELINES, allRoles);
        addReadRole(Configuration.ADAPTIVITY, allRoles);
        addReadRole(Configuration.ALGORITHMS, allRoles);
        addReadRole(Configuration.DATA_MANAGEMENT, allRoles);
        addReadRole(Configuration.FAMILIES, allRoles);
        addReadRole(Configuration.INFRASTRUCTURE, allRoles);
        addReadRole(Configuration.OBSERVABLES, allRoles);
        addReadRole(Configuration.RECONFIG_HARDWARE, allRoles);
        addWriteRole(Configuration.BASICS, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.HARDWARE, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.PIPELINES, ApplicationRole.ADMIN, ApplicationRole.PIPELINE_DESIGNER);
        addWriteRole(Configuration.ADAPTIVITY, ApplicationRole.ADMIN, ApplicationRole.ADAPTATION_MANAGER);
        addWriteRole(Configuration.ALGORITHMS, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.DATA_MANAGEMENT, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.FAMILIES, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.INFRASTRUCTURE, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
        addWriteRole(Configuration.OBSERVABLES, ApplicationRole.ADMIN, ApplicationRole.ADAPTATION_MANAGER);
        addWriteRole(Configuration.RECONFIG_HARDWARE, ApplicationRole.ADMIN, ApplicationRole.INFRASTRUCTURE_ADMIN);
    }

    /**
     * Returns whether the given modelPart is readable.
     * 
     * @param modelPart
     *            the model part to check
     * @return <code>true</code> if <code>modelPart</code> is readable, <code>false</code> else
     */
    public static boolean isReadable(IModelPart modelPart) {
        boolean readable = false;
        if (null != modelPart) {
            Set<Role> roles = UserContext.INSTANCE.getRoles();
            Role[] configuredRoles = mapRead.get(modelPart.getModelName());
            if (null != configuredRoles) {
                for (Role role : configuredRoles) {
                    if (roles.contains(role)) {
                        readable = true;
                    }
                }
            }
        } else {
            readable = true;
        }
        return readable;
    }

    /**
     * Returns whether the given configuration is writable.
     * 
     * @param config the configuration to check
     * @return <code>true</code> if <code>config</code> is readable, <code>false</code> else
     */
    public static boolean isWritable(de.uni_hildesheim.sse.model.confModel.Configuration config) {
        boolean writable = false;
        String name = config.getProject().getName();
        Set<Role> roles = UserContext.INSTANCE.getRoles();
        Role[] configuredRoles = mapWrite.get(name);
        if (null != configuredRoles) {
            for (Role role : configuredRoles) {
                if (roles.contains(role)) {
                    writable = true;
                }
            }
        }
        return writable;
    }
    
    /**
     * Returns whether the given variable is writable.
     * 
     * @param variable the variable to check
     * @return <code>true</code> if <code>variable</code> is readable, <code>false</code> else
     */
    public static boolean isWritable(IDecisionVariable variable) {
        return isWritable(variable.getConfiguration());
    }
    
    /**
     * Returns whether the given model part is writable.
     * 
     * @param part the modelPart to check
     * @return <code>true</code> if <code>part</code> is readable, <code>false</code> else
     */
    public static boolean isWritable(IModelPart part) {
        boolean writable = false;
        if (null == part) {
            writable = true;
        } else {
            writable = isWritable(part.getConfiguration());
        }
        return writable;
    }
    
    /**
     * Registers application specific editors.
     */
    public static void registerEditors() {
        // TODO String is not nice but sufficient for demo
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Parameters", 
            ParameterEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Tuples", 
            TuplesEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("setOf(Constraint)", 
            ConstraintsEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::ArtifactString", ArtifactEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Description", MultipleLineText.CREATOR);
    }

    /**
     * Initializes the images for the model parts.
     */
    public static void initializeImages() {
        ImageRegistry registry = ImageRegistry.INSTANCE;
        registry.registerImage(Configuration.BASICS, 
            IconManager.retrieveImage(IconManager.TYPES));
        registry.registerImage(Configuration.BASICS, 0, 
            IconManager.retrieveImage(IconManager.TYPE));
        
        registry.registerImage(Configuration.HARDWARE, 
            IconManager.retrieveImage(IconManager.GP_MACHINES));
        registry.registerImage(Configuration.HARDWARE, 0, 
            IconManager.retrieveImage(IconManager.GP_MACHINE));
        
        registry.registerImage(Configuration.RECONFIG_HARDWARE, 
            IconManager.retrieveImage(IconManager.RECONFIG_MACHINES));
        registry.registerImage(Configuration.RECONFIG_HARDWARE, 0, 
            IconManager.retrieveImage(IconManager.RECONFIG_MACHINE));
        registry.registerImage(Configuration.RECONFIG_HARDWARE, "MPCCNode", 
            IconManager.retrieveImage(IconManager.RECONFIG_MACHINE));
        
        registry.registerImage(Configuration.DATA_MANAGEMENT, 
            IconManager.retrieveImage(IconManager.DATA_MANAGEMENT));
        registry.registerImage(Configuration.DATA_MANAGEMENT, 0, 
            IconManager.retrieveImage(IconManager.DATA_SOURCE));
        registry.registerImage(Configuration.DATA_MANAGEMENT, 1, 
            IconManager.retrieveImage(IconManager.DATA_SINK));
        registry.registerImage(Configuration.DATA_MANAGEMENT, 2, 
            IconManager.retrieveImage(IconManager.DATA_ELEMENT));
        
        registry.registerImage(Configuration.ALGORITHMS, 
            IconManager.retrieveImage(IconManager.ALGORITHMS));
        registry.registerImage(Configuration.ALGORITHMS, 0, 
            IconManager.retrieveImage(IconManager.ALGORITHM));
        
        registry.registerImage(Configuration.FAMILIES, 
            IconManager.retrieveImage(IconManager.FAMILIES));
        registry.registerImage(Configuration.FAMILIES, 0, 
            IconManager.retrieveImage(IconManager.FAMILY));
        
        registry.registerImage(Configuration.PIPELINES, 
            IconManager.retrieveImage(IconManager.PIPELINES));
        registry.registerImage(Configuration.PIPELINES, 0, 
            IconManager.retrieveImage(IconManager.PIPELINE));
        
        registry.registerImage(Configuration.INFRASTRUCTURE, 
            IconManager.retrieveImage(IconManager.INFRASTRUCTURE));
        
        registry.registerImage(Configuration.ADAPTIVITY, 
            IconManager.retrieveImage(IconManager.ADAPTATION));
        registry.registerImage(Configuration.OBSERVABLES, 
            IconManager.retrieveImage(IconManager.OBSERVABLES));
    }
    
    /**
     * Returns whether a decision variable is visible. This is helpful to filter out runtime variables (dynamic
     * configuration at runtime).
     * 
     * @param variable
     *            the variable to be considered
     * @return <code>true</code> if the variable is visible, <code>false</code> else
     */
    public static boolean isVisible(IDecisionVariable variable) {
        // no constraints, only compile time variables
        boolean visible = !ConstraintType.TYPE.isAssignableFrom(variable.getDeclaration().getType());
        for (int a = 0; visible && a < variable.getAttributesCount(); a++) {
            IDecisionVariable attribute = variable.getAttribute(a);
            String name = attribute.getDeclaration().getName();
            if (BINDING_TIME_NAME.equals(name)) {
                Value value = attribute.getValue();
                if (null != value && value instanceof EnumValue) {
                    EnumLiteral lit = ((EnumValue) value).getValue();
                    if (null != lit && !lit.getName().equals(VariabilityModel.BINDING_TIME_LITERAL_VISIBLE)) {
                        visible = false;
                    }
                }
            } else if (USER_VISIBLE_NAME.equals(name)) { // restrict only if given an false
                Value value = attribute.getValue();
                if (null != value && value instanceof BooleanValue) {
                    Boolean val = ((BooleanValue) value).getValue();
                    if (Boolean.FALSE == val) {
                        visible = false;
                    }
                }
            }
        }
        return visible;
    }

}
