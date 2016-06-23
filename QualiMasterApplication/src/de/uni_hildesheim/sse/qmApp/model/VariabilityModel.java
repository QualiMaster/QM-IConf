package de.uni_hildesheim.sse.qmApp.model;

import static eu.qualimaster.easy.extension.QmConstants.ANNOTATION_BINDING_TIME;
import static eu.qualimaster.easy.extension.QmConstants.ANNOTATION_USER_VISIBLE;
import static eu.qualimaster.easy.extension.QmConstants.CFG_POSTFIX;
import static eu.qualimaster.easy.extension.QmConstants.CONST_BINDING_TIME_COMPILE;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_ADAPTIVITY;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_ALGORITHMS;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_BASICS;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_DATAMGT;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_FAMILIES;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_HARDWARE;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_INFRASTRUCTURE;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_OBSERVABLES;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_PIPELINES;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_RECONFHW;
import static eu.qualimaster.easy.extension.QmConstants.PROJECT_TOP_LEVEL;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_NAME;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_ADAPTIVITY_QPARAMWEIGHTING;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_ALGORITHM;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_DATASINK;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_DATASOURCE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_FAMILY;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_FIELDTYPE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_HWNODE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_MACHINE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_OBSERVABLES_CONFIGUREDQPARAM;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_PERSISTENTDATAELT;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_PIPELINE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_SUBPIPELINE;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_SUBPIPELINE_ALGORITHM;
import static eu.qualimaster.easy.extension.QmConstants.VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_ADAPTIVITY_PIPELINEIMPORTANCE;
import static eu.qualimaster.easy.extension.QmConstants.VAR_ALGORITHMS_ALGORITHMS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_BASICS_TYPES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_DATAMGT_DATASINKS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_DATAMGT_DATASOURCES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_DATAMGT_PERSISTENTDATAELTS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_FAMILIES_FAMILIES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_HARDWARE_MACHINES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_INFRASTRUCTURE_ACTIVEPIPELINES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_OBSERVABLES_CONFIGUREDPARAMS;
import static eu.qualimaster.easy.extension.QmConstants.VAR_PIPELINES_PIPELINES;
import static eu.qualimaster.easy.extension.QmConstants.VAR_RECONFHW_CLUSTERS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_hildesheim.sse.qmApp.editorInput.EmptyEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator.CloneMode;
import de.uni_hildesheim.sse.qmApp.editorInput.RtVilEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.VarModelEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editors.AlgorithmEditor;
import de.uni_hildesheim.sse.qmApp.editors.ArtifactEditor;
import de.uni_hildesheim.sse.qmApp.editors.ClassEditor;
import de.uni_hildesheim.sse.qmApp.editors.ConstraintsEditor;
import de.uni_hildesheim.sse.qmApp.editors.DefaultAlgorithmCellEditor;
import de.uni_hildesheim.sse.qmApp.editors.FamilyEditor;
import de.uni_hildesheim.sse.qmApp.editors.MultipleLineText;
import de.uni_hildesheim.sse.qmApp.editors.ParameterEditor;
import de.uni_hildesheim.sse.qmApp.editors.SourceSinkEditor;
import de.uni_hildesheim.sse.qmApp.editors.TupleTypeEditor;
import de.uni_hildesheim.sse.qmApp.editors.TuplesEditor;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.Utils.ConfigurationProperties;
import de.uni_hildesheim.sse.qmApp.tabbedViews.FlowPropertyEditorCreator;
import de.uni_hildesheim.sse.qmApp.tabbedViews.PipelineDiagramElementPropertyEditorCreator;
import de.uni_hildesheim.sse.qmApp.tabbedViews.PropertyEditorFactory;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElement;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElements;
import de.uni_hildesheim.sse.qmApp.treeView.ConfigurableElements.IElementReferrer;
import de.uni_hildesheim.sse.qmApp.treeView.DecisionVariableElementFactory;
import de.uni_hildesheim.sse.qmApp.treeView.IConfigurableElementFactory;
import de.uni_hildesheim.sse.qmApp.treeView.PipelineElementFactory;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.ApplicationRole;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory;
import net.ssehub.easy.varModel.confModel.IConfiguration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.values.BooleanValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.Value;
import pipeline.impl.DataManagementElementImpl;
import pipeline.impl.FamilyElementImpl;
import pipeline.impl.PipelineImpl;
import pipeline.impl.ReplaySinkImpl;
import pipeline.impl.SinkImpl;
import pipeline.impl.SourceImpl;

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

    public static final boolean DISPLAY_ALGORITHMS_NESTED = true;

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
        BASICS(PROJECT_BASICS,
            new String[] {VAR_BASICS_TYPES},
            new String[] {TYPE_FIELDTYPE},
            SourceMode.VARIABLES),
        HARDWARE(PROJECT_HARDWARE,
            new String[] {VAR_HARDWARE_MACHINES}, 
            new String[] {TYPE_MACHINE}, 
            SourceMode.VARIABLES), 
        RECONFIG_HARDWARE(PROJECT_RECONFHW, 
            new String[] {VAR_RECONFHW_CLUSTERS}, 
            new String[] {TYPE_HWNODE},
            SourceMode.VARIABLES), 
        DATA_MANAGEMENT(PROJECT_DATAMGT,
            new String[] {VAR_DATAMGT_DATASOURCES, VAR_DATAMGT_DATASINKS, VAR_DATAMGT_PERSISTENTDATAELTS},
            new String[] {TYPE_DATASOURCE, TYPE_DATASINK, TYPE_PERSISTENTDATAELT}, 
            SourceMode.VARIABLES, 
            new DecisionVariableElementFactory(SourceSinkEditor.ID)), 
        ALGORITHMS(PROJECT_ALGORITHMS,
            new String[] {VAR_ALGORITHMS_ALGORITHMS}, 
            new String[] {TYPE_ALGORITHM, 
//                PROJECT_PIPELINES + IvmlKeyWords.NAMESPACE_SEPARATOR + 
                TYPE_SUBPIPELINE_ALGORITHM},
            SourceMode.VARIABLES,
            new DecisionVariableElementFactory(AlgorithmEditor.ID)), 
        FAMILIES(PROJECT_FAMILIES, 
            new String[] {VAR_FAMILIES_FAMILIES}, 
            new String[] {TYPE_FAMILY}, 
            SourceMode.VARIABLES, 
            new DecisionVariableElementFactory(FamilyEditor.ID)), 
        OBSERVABLES(PROJECT_OBSERVABLES, 
            new String[] {VAR_OBSERVABLES_CONFIGUREDPARAMS}, 
            new String[] {TYPE_OBSERVABLES_CONFIGUREDQPARAM}, 
            SourceMode.VARIABLES), // add provided types
        ADAPTIVITY(PROJECT_ADAPTIVITY, 
            new String[] {VAR_ADAPTIVITY_PIPELINEIMPORTANCE, VAR_ADAPTIVITY_CROSSPIPELINETRADEOFFS}, 
            new String[] {TYPE_ADAPTIVITY_QPARAMWEIGHTING, TYPE_ADAPTIVITY_QPARAMWEIGHTING}, 
            SourceMode.VARIABLES), // add provided types
        PIPELINES(PROJECT_PIPELINES, 
            new String[] {VAR_PIPELINES_PIPELINES}, 
            new String[] {TYPE_PIPELINE, TYPE_SUBPIPELINE}, 
            SourceMode.VARIABLES,
            PipelineElementFactory.INSTANCE), 
        INFRASTRUCTURE(PROJECT_INFRASTRUCTURE, 
            new String[] {VAR_INFRASTRUCTURE_ACTIVEPIPELINES}, 
            new String[] {TYPE_PIPELINE}, 
            SourceMode.VARIABLES),
        TOP_LEVEL(PROJECT_TOP_LEVEL, 
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
        public net.ssehub.easy.varModel.confModel.Configuration getConfiguration() {
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
        public net.ssehub.easy.varModel.confModel.Configuration getConfiguration() {
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
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(ReplaySinkImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(FamilyElementImpl.class));
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(
                DataManagementElementImpl.class));
        PropertyEditorFactory.registerCreator(new FlowPropertyEditorCreator());
        PropertyEditorFactory.registerCreator(new PipelineDiagramElementPropertyEditorCreator(PipelineImpl.class));
    }
    
    /**
     * A subgrouping referrer for algorithms in their families.
     *  
     * @author Holger Eichelberger
     */
    private static class FamilyAlgorithmReferrer implements IElementReferrer {

        private static final IModelPart PART = Configuration.ALGORITHMS; 
        
        @Override
        public IModelPart getSubModelPart() {
            return PART;
        }

        @Override
        public void variableToConfigurableElements(IDecisionVariable var, ConfigurableElement parent) {
            IDecisionVariable members = var.getNestedElement("members");
            if (null != members) {
                for (int m = 0; m < members.getNestedElementsCount(); m++) {
                    IDecisionVariable member = dereference(members.getNestedElement(m));
                    AbstractVariable decl = member.getDeclaration();
                    ConfigurableElements.variableToConfigurableElements(PART, decl.getName(), member, parent, 
                        PART.getElementFactory(), null);
                }
            }
        }

        @Override
        public ConfigurableElement getActualParent(String name, ConfigurableElement parent) {
            return parent;
        }
        
    }

    /**
     * A name-based subgrouping referrer for hardware elements. This referrer is based on 
     * grouping according to IP prefixes or domain name suffixes.
     * 
     * @author Holger Eichelberger
     */
    private static class HardwareReferrer implements IElementReferrer {

        private static final IModelPart PART = Configuration.HARDWARE; 
        private Map<String, ConfigurableElement> parents = new HashMap<String, ConfigurableElement>();

        @Override
        public IModelPart getSubModelPart() {
            return PART;
        }

        @Override
        public void variableToConfigurableElements(IDecisionVariable var, ConfigurableElement parent) {
        }

        @Override
        public ConfigurableElement getActualParent(String name, ConfigurableElement parent) {
            ConfigurableElement result = parent;
            String pName = getHardwareGroup(name);
            if (null != pName) {
                result = parents.get(pName);
                if (null == result) {
                    result = new ConfigurableElement(parent, pName, null, null, PART);
                    result.setImage(ImageRegistry.INSTANCE.getImage(PART));
                    parent.addChild(result);
                    parents.put(pName, result);
                }
            }
            return result;
        }

    }
    
    /**
     * Returns the hardware group for the given machine <code>name</code>.
     * 
     * @param name the machine name
     * @return the hardware group based on qualified domain name or IP, <b>null</b> if no group was found
     */
    public static String getHardwareGroup(String name) {
        String result = null;
        int firstDot = -1;
        int lastDot = -1;
        int dotCount = 0;
        int digitCount = 0;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if ('.' == c) {
                dotCount++;
                lastDot = i;
                firstDot = firstDot < 0 ? i : firstDot;
            } else if (Character.isDigit(c)) {
                digitCount++;
            }
        }
        if (dotCount > 0 && lastDot < name.length() - 1) {
            if (3 == dotCount && digitCount + dotCount == name.length()) { // it's an IP
                result = name.substring(0, lastDot);
            } else if (dotCount > 0) { // its a qualified name
                result = name.substring(firstDot + 1, name.length());
            }
            if (null != result) {
                result = "*." + result;
            }
        }
        return result;
    }

    /**
     * Creates the configurable elements.
     * 
     * @param elements
     *            the elements data structure to be modified as a side effect
     */
    public static void createConfigurationElements(ConfigurableElements elements) {
        boolean demoMode = ConfigurationProperties.DEMO_MODE.getBooleanValue();

        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.BASICS, "Types");
        elements.variableToConfigurableElements(Configuration.BASICS, "de.uni_hildesheim.sse.qmApp.TypesEditor");
        
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.HARDWARE, 
            "General-purpose Machines");
        elements.variableToConfigurableElements(Configuration.HARDWARE, "de.uni_hildesheim.sse.qmApp.HardwareEditor", 
            DISPLAY_ALGORITHMS_NESTED ? new HardwareReferrer() : null);

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
        if (DISPLAY_ALGORITHMS_NESTED) {
            List<IDecisionVariable> unattached = unreferencedAlgorithms();
            // start with families and use referrer to attach algorithms
            ConfigurableElement elt = elements.variableToConfigurableElements(Configuration.FAMILIES, 
                "de.uni_hildesheim.sse.qmApp.AlgorithmsEditor", new FamilyAlgorithmReferrer());
            elements.variableToConfigurableElements(Configuration.ALGORITHMS, 
                "de.uni_hildesheim.sse.qmApp.AlgorithmsEditor", elt, unattached);
        } else {
            elements.variableToConfigurableElements(Configuration.ALGORITHMS, 
                "de.uni_hildesheim.sse.qmApp.AlgorithmsEditor");
        }

        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.PIPELINES, "Pipelines");
        elements.variableToConfigurableElements(Configuration.PIPELINES, "de.uni_hildesheim.sse.qmApp.PipelinesEditor");

        String tmp = "Infrastructure";
        QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.INFRASTRUCTURE, tmp);
        elements.addElement(tmp, "de.uni_hildesheim.sse.qmApp.InfrastructureEditor",
            new VarModelEditorInputCreator(Configuration.INFRASTRUCTURE, "Infrastructure"),
            Configuration.INFRASTRUCTURE);
        
        if (!demoMode) {
            QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.OBSERVABLES, 
                "Observables");
            elements.variableToConfigurableElements(Configuration.OBSERVABLES, 
                "de.uni_hildesheim.sse.qmApp.ObservablesEditor");
    
            QualiMasterDisplayNameProvider.INSTANCE.registerModelPartDisplayName(Configuration.ADAPTIVITY, 
                "Adaptation");
            ConfigurableElement elt = elements.variableToConfigurableElements(Configuration.ADAPTIVITY, 
                "de.uni_hildesheim.sse.qmApp.AdaptationEditor");
            elt.setImage(IconManager.retrieveImage(IconManager.ADAPTATION));
            IEditorInputCreator editorInput = new RtVilEditorInputCreator();
            if (editorInput.isEnabled()) {
                ConfigurableElement rtVIL = new ConfigurableElement(elt, "rt-VIL", 
                    "de.uni_hildesheim.sse.vil.rt.RtVil", editorInput);
                rtVIL.setImage(IconManager.retrieveImage(IconManager.RTVIL)); // TODO preliminary - take from rtVIL
                rtVIL.setMenuContributor(new RtVilMenuContributor());
                elt.addChild(rtVIL);
            }
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
        return SLOT_NAME.equals(decl.getName()) && StringType.TYPE.isAssignableFrom(decl.getType());
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
    public static boolean isWritable(net.ssehub.easy.varModel.confModel.Configuration config) {
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
        boolean demoMode = ConfigurationProperties.DEMO_MODE.getBooleanValue();

        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Parameters", 
            ParameterEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Tuples", 
            TuplesEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("setOf(Constraint)", 
            ConstraintsEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::ArtifactString", ArtifactEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::OptionalArtifactString", ArtifactEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("Basics::Description", MultipleLineText.CREATOR);

        // Special property editors in pipeline editor -> References, which will be saved as Strings (not as numbers)
        ConfigurationTableEditorFactory.registerEditorCreator("refTo(Basics::Tuple)", TupleTypeEditor.CREATOR);
        ConfigurationTableEditorFactory.registerEditorCreator("refTo(Algorithms::Algorithm)",
            DefaultAlgorithmCellEditor.CREATOR);
        
        if (!demoMode) {
            ConfigurationTableEditorFactory.registerEditorCreator("Basics::ClassString", ClassEditor.CREATOR);
            ConfigurationTableEditorFactory.registerEditorCreator("Basics::OptionalClassString", ClassEditor.CREATOR);
        }
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
        registry.registerImage(Configuration.ALGORITHMS, 1, 
            IconManager.retrieveImage(IconManager.SUBALGORITHM));
        if (DISPLAY_ALGORITHMS_NESTED) {
            registry.registerImage(Configuration.ALGORITHMS, "Family", 
                IconManager.retrieveImage(IconManager.FAMILY));
        }
        
        registry.registerImage(Configuration.FAMILIES, 
            IconManager.retrieveImage(IconManager.FAMILIES));
        registry.registerImage(Configuration.FAMILIES, 0, 
            IconManager.retrieveImage(IconManager.FAMILY));
        
        registry.registerImage(Configuration.PIPELINES, 
            IconManager.retrieveImage(IconManager.PIPELINES));
        registry.registerImage(Configuration.PIPELINES, 0, 
            IconManager.retrieveImage(IconManager.PIPELINE));
        registry.registerImage(Configuration.PIPELINES, 1, 
                IconManager.retrieveImage(IconManager.SUBPIPELINE));
        
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
            if (ANNOTATION_BINDING_TIME.equals(name)) {
                Value value = attribute.getValue();
                if (null != value && value instanceof EnumValue) {
                    EnumLiteral lit = ((EnumValue) value).getValue();
                    if (null != lit && !lit.getName().equals(CONST_BINDING_TIME_COMPILE)) {
                        visible = false;
                    }
                }
            } else if (ANNOTATION_USER_VISIBLE.equals(name)) { // restrict only if given an false
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
    
    /**
     * Dereferences a variable.
     * 
     * @param var the variable to be dereferenced (may be <b>null</b>)
     * @return the dereferenced variable (<b>null</b> if <code>var</code> was <b>null</b>)
     */
    public static IDecisionVariable dereference(IDecisionVariable var) {
        return net.ssehub.easy.varModel.confModel.Configuration.dereference(var);
    }
    
    /**
     * Dereferences a value.
     * 
     * @param config the configuration access
     * @param value the value to be dereferenced
     * @return the dereferenced value
     */
    public static Value dereference(IConfiguration config, Value value) {
        return net.ssehub.easy.varModel.confModel.Configuration.dereference(config, value);
    }
    
    /**
     * Returns the instance name of a decision variable. This name is composed 
     * from the names of the given variable and its parent variables. Please note
     * that the instance name is typically different from the qualified name of 
     * the declaration, which, in case of compound slots, leads to the variable
     * in the compound definition. The result is unqualified regarding the top-level
     * variable.
     * 
     * @param var the variable to return the name for (may be <b>null</b>)
     * @return the instance name (may be empty if <code>var == <b>null</b></code>
     */
    public static String getInstanceName(IDecisionVariable var) {
        return net.ssehub.easy.varModel.confModel.Configuration.getInstanceName(var);
    }

    /**
     * Identifies unreferenced algorithms, i.e., algorithms not used as members in families.
     * 
     * @return unreferenced algorithms
     */
    public static List<IDecisionVariable> unreferencedAlgorithms() {
        List<IDecisionVariable> result = new ArrayList<IDecisionVariable>();
        Set<String> referencedAlgs = new HashSet<String>();
        final IModelPart families = Configuration.FAMILIES;
        List<AbstractVariable> decls = families.getPossibleValues();
        net.ssehub.easy.varModel.confModel.Configuration cfg = families.getConfiguration();
        for (AbstractVariable decl : decls) {
            IDecisionVariable var = cfg.getDecision(decl);
            for (int n = 0; n < var.getNestedElementsCount(); n++) {
                IDecisionVariable nested = dereference(var.getNestedElement(n));
                IDecisionVariable members = nested.getNestedElement("members");
                if (null != members) {
                    for (int m = 0; m < members.getNestedElementsCount(); m++) {
                        IDecisionVariable algorithm = dereference(members.getNestedElement(m));
                        referencedAlgs.add(getInstanceName(algorithm));
                    }
                }
            }
        }
        
        final IModelPart algorithms = Configuration.ALGORITHMS;
        decls = algorithms.getPossibleValues();
        cfg = algorithms.getConfiguration();
        for (AbstractVariable decl : decls) {
            IDecisionVariable var = cfg.getDecision(decl);
            for (int n = 0; n < var.getNestedElementsCount(); n++) {
                IDecisionVariable algorithm = dereference(var.getNestedElement(n));
                if (!referencedAlgs.contains(getInstanceName(algorithm))) {
                    result.add(algorithm);
                }
            }
        }
        return result;
    }

}
