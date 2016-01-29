package de.uni_hildesheim.sse.qmApp.treeView;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.model.confModel.CompoundVariable;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.management.VarModel;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.ProjectImport;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.persistency.StringProvider;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInput;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editors.QMPipelineEditor;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.IVMLModelOperations;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.model.PipelineTranslationOperations;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.utils.modelManagement.ModelInfo;
import eu.qualimaster.adaptation.external.PipelineMessage;

/**
 * Implements the configuration element factory for pipelines.
 * 
 * @author Holger Eichelberger
 * @author Cui Qin
 */
public class PipelineElementFactory implements IConfigurableElementFactory {

    public static final IConfigurableElementFactory INSTANCE = new PipelineElementFactory();

    /**
     * Implements a deferred editor input so that missing models are created when the editor requests them.
     * 
     * @author Holger Eichelberger
     * @author Cui Qin
     */
    private static class DeferredURIEditorInput extends URIEditorInput implements IVariableEditorInput {

        protected static final String URI_TAG = "deferred.URI";
        protected static final String NAME_TAG = "deferred.name";
        protected static final String INITIALIZED_TAG = "deferred.initialized";
        private static final String CREATOR_FACTORY_ID_TAG = "deferred.creatorId";
        
        private URI uri; // we need this for deferred init
        private boolean initialized = false;
        private IDecisionVariable variable;
        private String name; // allow for serialization of editors
        private IVariableEditorInputCreator creator;

        /**
         * Creates a deferred input object.
         * 
         * @param uri
         *            the URI of the input object
         * @param variable
         *            the underlying decision variable
         * @param creator
         *            the actual creator for memento persistence            
         */
        public DeferredURIEditorInput(URI uri, IDecisionVariable variable, IVariableEditorInputCreator creator) {
            super(uri, ModelAccess.getDisplayName(variable));
            this.uri = uri;
            this.variable = variable;
            this.creator = creator;
        }
        
        /**
         * Creates an URI editor input from the given memento.
         * 
         * @param memento the memento to create the instance from
         */
        public DeferredURIEditorInput(IMemento memento) {
            super(memento);
            name = memento.getString(NAME_TAG);
            initialized = memento.getBoolean(INITIALIZED_TAG);
            uri = URI.createURI(memento.getString(URI_TAG));

            String factoryId = memento.getString(CREATOR_FACTORY_ID_TAG);
            IElementFactory factory = PlatformUI.getWorkbench().getElementFactory(factoryId);
            if (null != factory) {
                IAdaptable tmp = factory.createElement(memento);
                if (tmp instanceof IVariableEditorInputCreator) {
                    creator = (IVariableEditorInputCreator) tmp;
                    variable = creator.getVariable();
                }
            }
        }
        
        @Override
        public IDecisionVariable getVariable() {
            return variable;
        }

        /**
         * Deferred lazy initialization on opening the editor.
         */
        private void initialize() {
            if (!initialized) {
                createArtifacts(uri);
                initialized = true;
            }
        }

        @Override
        public boolean exists() {
            initialize();
            return super.exists();
        }

        @Override
        public IPersistableElement getPersistable() {
            return this;
        }

        @Override
        public void saveState(IMemento memento) {
            super.saveState(memento);
            memento.putString(NAME_TAG, name);
            memento.putBoolean(INITIALIZED_TAG, initialized);
            memento.putString(URI_TAG, uri.toString());
            if (creator instanceof IPersistableElement) {
                IPersistableElement elt = ((IPersistableElement) creator);
                memento.putString(CREATOR_FACTORY_ID_TAG, elt.getFactoryId());
                elt.saveState(memento);
            }
        }
        
        @Override
        public String getFactoryId() {
            return DeferredURIEditorInputElementFactory.ID;
        }

        @Override
        public URI getURI() {
            initialize();
            return super.getURI();
        }

        @Override
        public String getName() {
            String name = ModelAccess.getDisplayName(variable);
            if (null == name && null == this.name) {
                name = super.getName();
            }
            this.name = name;
            return name;
        }

    }

    /**
     * A factory for the deferred URI editor input.
     * 
     * @author Holger Eichelberger
     */
    public static class DeferredURIEditorInputElementFactory implements IElementFactory {
        
        public static final String ID = DeferredURIEditorInputElementFactory.class.getName();

        @Override
        public IAdaptable createElement(IMemento memento) {
            return new DeferredURIEditorInput(memento);
        }
        
    }
    
    /**
     * Defines a deferring URI editor input creator.
     *  
     * @author Holger Eichelberger
     */
    private static class URIEditorInputCreator implements IEditorInputCreator {

        private IVariableEditorInputCreator creator;
        private URI diagramURI;
        private IEditorInput last = null;
        private IDecisionVariable fallback; // not nice, needed for creation
        //private IModelPart modelPart = de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration.PIPELINES;
        
        /**
         * Creates URI editor input creator.
         *  
         * @param diagramURI the URI of the diagram
         * @param creator the original creator providing access to the variable
         * @param fallback in case of just created variables :(
         */
        private URIEditorInputCreator(URI diagramURI, IVariableEditorInputCreator creator, IDecisionVariable fallback) {
            this.diagramURI = diagramURI;
            this.creator = creator;
            this.fallback = fallback;
        }
        
        /**
         * Returns the actual variable.
         * 
         * @return the variable
         */
        protected IDecisionVariable getVariable() {
            IDecisionVariable var = creator.getVariable();
            if (null == var) {
                var = fallback;
            }
            return var;
        }
        
        @Override
        public IEditorInput create() {
            IDecisionVariable variable = getVariable();
            IEditorInput result = null == variable ? null : new DeferredURIEditorInput(diagramURI, variable, creator);
            last = result;
            return result;
        }

        @Override
        public String getName() {
            return creator.getName();
        }

        @Override
        public IDatatype getType() {
            return creator.getType();
        }

        @Override
        public boolean isEnabled() {
            return null != diagramURI && null != getVariable();
        }

        @Override
        public void createArtifacts() {
            creator.createArtifacts();
            if (null != diagramURI) {
                PipelineElementFactory.createArtifacts(diagramURI);
            }
        }

        @Override
        public boolean isWritable() {
            return VariabilityModel.isWritable(VariabilityModel.Configuration.PIPELINES);
        }

        @Override
        public CloneMode isCloneable() {
            return VariabilityModel.isCloneable(VariabilityModel.Configuration.PIPELINES);
        }

        @Override
        public boolean isDeletable() {
            return VariabilityModel.isDeletable(VariabilityModel.Configuration.PIPELINES);
        }

        @Override
        public boolean isReadable() {
            return VariabilityModel.isReadable(VariabilityModel.Configuration.PIPELINES);
        }

        @Override
        public void delete(Object source, IModelPart modelPart) {
            IDecisionVariable variable = getVariable();
            // delete pipeline element                
            String pName = variable.getDeclaration().getName();
            String projectName = pName + VariabilityModel.CFG_POSTFIX;
            ModelAccess.removeConfiguration(projectName);
            Configuration cfg = modelPart.getConfiguration();
            ModelInfo<Project> info = VarModel.INSTANCE.availableModels()
                    .getModelInfo(cfg.getProject());
            // delete the related elements in the main ivml file -PipelinesCfg.ivml
            ProjectImport pImport = new ProjectImport(projectName, null);
            boolean dImport = IVMLModelOperations.modifyImports(cfg.getProject(),
                    IVMLModelOperations.DEL, pImport);

            boolean dElement = IVMLModelOperations.deletePipelineElementFromMainProject(
                    cfg.getProject(), variable.getDeclaration());
            String path = info.getLocation().getPath();
            // delete the related ivml file
            if (dImport && dElement) {
                String selectedElementPath = path.substring(0, path.lastIndexOf("/") + 1) + projectName
                        + PipelineTranslationOperations.EXTENSION;
                File file = new File(selectedElementPath);                
                if (file.exists()) {
                    file.delete();
//                    PipelineDiagramUtils.deleteWorkspaceFile(file);   
                }                                 
            }
            // overwrite IVML file
            int globalIndex = ModelAccess.getGlobalIndex(modelPart, variable);            
            ChangeManager.INSTANCE.variableDeleting(source, variable, globalIndex);
            String output = StringProvider.toIvmlString(cfg.getProject());
            PipelineTranslationOperations.writeIVMLStringToFile(output, path);              
            ModelAccess.reloadModel(modelPart);
            // this deletes via the workspace
            PipelineDiagramUtils.deleteWorkspaceFile(PipelineDiagramUtils.getDiagramURI(variable));
            PipelineDiagramUtils.deleteWorkspaceFile(PipelineDiagramUtils.getModelURI(variable));         
            ChangeManager.INSTANCE.variableDeleted(source, variable, globalIndex);
            if (null != last) {
                IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                IEditorPart editor = page.findEditor(last);
                if (null != editor) {
                    page.closeEditor(editor, false);
                }
            }
        }
        
        @Override
        public List<IDecisionVariable> clone(int cloneCount) {
            // TODO @Cui: currently disabled via VariabilityModel. This only clones the pipeline entry, not sources, 
            // sinks, etc. Specific code for handling model files and hooking the new element into the PipelineCfg 
            // needed!
            return ModelAccess.cloneElement(de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration.PIPELINES, 
                getVariable(), true, cloneCount);
        }

        @Override
        public boolean holds(IDecisionVariable variable) {
            return Utils.equalsOrContains(getVariable(), variable);
        }

        @Override
        public boolean isReferencedIn(IModelPart modelPart, IModelPart... defining) {
            return ModelAccess.isReferencedIn(modelPart, getVariable(), defining);
        }

        @Override
        public IMenuContributor getMenuContributor() {
            return new IMenuContributor() {
                
                @Override
                public void contributeTo(IMenuManager manager) {
                    boolean isInfrastructureAdmin = UserContext.INSTANCE.isInfrastructureAdmin();
                    final String deploymentUrl = ModelAccess.getDeploymentUrl();
                    Action action = new Action() {
                        @Override
                        public void run() {
                            IDecisionVariable var = getVariable(); // take data from
                            Dialogs.showInfoDialog("In implementation...", "Will deploy '" 
                                + ModelAccess.getDisplayName(var) + "' to " + deploymentUrl);
                            // TODO deploy
                        }
                    };
                    action.setEnabled(isInfrastructureAdmin && (null != deploymentUrl && deploymentUrl.length() > 0));
                    action.setText("Deploy...");
                    manager.add(action);

                    action = new Action() {
                        @Override
                        public void run() {
                            IDecisionVariable var = getVariable();
                            Infrastructure.send(new PipelineMessage(ModelAccess.getDisplayName(var), 
                                PipelineMessage.Status.START).elevate()); // admin permission, elevate implicit
                        }
                    };
                    action.setEnabled(isInfrastructureAdmin && Infrastructure.isConnected());
                    action.setText("Start...");
                    manager.add(action);

                    action = new Action() {
                        @Override
                        public void run() {
                            IDecisionVariable var = getVariable();
                            Infrastructure.send(new PipelineMessage(ModelAccess.getDisplayName(var), 
                                PipelineMessage.Status.STOP).elevate()); // admin permission, elevate implicit
                        }
                    };
                    action.setEnabled(isInfrastructureAdmin && Infrastructure.isConnected());
                    action.setText("Stop...");
                    manager.add(action);
                }
            };
        }

    }

    /**
     * Prevents external creation.
     */
    private PipelineElementFactory() {
    }

    /**
     * Creates the artifacts for a diagram model.
     * 
     * @param uri the diagram URI
     */
    private static void createArtifacts(URI uri) {
        // check inside eclipse - resource URI
        Boolean exists = null;
        String path = uri.toPlatformString(true);
        if (null != path) {
            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(
                new org.eclipse.core.runtime.Path(path));
            if (null != file) {
                exists = file.exists();
            } 
        }
        if (null == exists) {
            // check outside eclipse although editor prevents saving
            if (uri.isFile()) {
                String fString = uri.toFileString();
                exists = new File(fString).exists();
            }
        }
        if (!exists) { // no super.exists (loop with getURI)
            PipelineDiagramUtils.createModel(uri);
        }
    }

    @Override
    public ConfigurableElement createElement(ConfigurableElement parent, IDecisionVariable variable, 
        IVariableEditorInputCreator creator) {
        IEditorInputCreator editorInput = null;
        String editorId = null;
        String name = variable.getDeclaration().getName();
        CompoundVariable comVariable = (CompoundVariable) variable;
        String displayName;
        if (comVariable.getNestedVariable("name").hasValue()) {
            displayName = comVariable.getNestedVariable("name").getValue().toString();
            displayName = displayName.substring(0, displayName.lastIndexOf(":") - 1);
            // links pipeline variable name and display name, only for the first loading time
            QMPipelineEditor.getPipelineNameAndDisplayname().put(name, displayName);
        } else {
            displayName = name;
        }
        URI diagramURI = PipelineDiagramUtils.getDiagramURI(variable);

        if (null != diagramURI) {
            editorInput = new URIEditorInputCreator(diagramURI, creator, variable);
            editorId = QMPipelineEditor.ID;
        }
        ConfigurableElement result = new ConfigurableElement(parent, displayName, editorId, editorInput); 
        result.setImage(ImageRegistry.INSTANCE.getImage(parent.getModelPart(), variable.getDeclaration().getType()));
        return result;
    }
    
    /**
     * Initializes this factory.
     */
    public static void initialize() {
        // force loading the InputCreator it into the JVM allowing Eclipse to access the editor input class 
        // for persisted editors during Workbench startup
        URIEditorInputCreator.class.getName();
    }

}
