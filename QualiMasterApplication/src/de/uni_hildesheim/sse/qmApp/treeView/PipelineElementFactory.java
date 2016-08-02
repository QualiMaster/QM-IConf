package de.uni_hildesheim.sse.qmApp.treeView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.editorInput.IEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInput;
import de.uni_hildesheim.sse.qmApp.editorInput.IVariableEditorInputCreator;
import de.uni_hildesheim.sse.qmApp.editors.QMPipelineEditor;
import de.uni_hildesheim.sse.qmApp.images.ImageRegistry;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.IVMLModelOperations;
import de.uni_hildesheim.sse.qmApp.model.Location;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.PipelineDiagramUtils;
import de.uni_hildesheim.sse.qmApp.model.PipelineTranslationOperations;
import de.uni_hildesheim.sse.qmApp.model.Utils;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.qmApp.runtime.UIUtils;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.modelop.BasicIVMLModelOperations;
import eu.qualimaster.manifestUtils.ManifestConnection;
import net.ssehub.easy.basics.modelManagement.ModelInfo;
import net.ssehub.easy.producer.eclipse.observer.EclipseProgressObserver;
import net.ssehub.easy.producer.ui.productline_editor.EasyProducerDialog;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.management.VarModel;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.ProjectImport;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.persistency.StringProvider;

/**
 * Implements the configuration element factory for pipelines.
 * 
 * @author Holger Eichelberger
 * @author Cui Qin
 * @author Patrik Pastuschek
 */
public class PipelineElementFactory implements IConfigurableElementFactory {

    public static final IConfigurableElementFactory INSTANCE = new PipelineElementFactory();

    /**
     * Implements a deferred editor input so that missing models are created when the editor requests them.
     * 
     * @author Holger Eichelberger
     * @author Cui Qin
     */
    public static class DeferredURIEditorInput extends URIEditorInput implements IVariableEditorInput {

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

        /**
         * Returns whether a pipeline or a subpipeline shall be edited.
         * @return <tt>true</tt> if a subpipeline shall be edited, <tt>false</tt> if a normal pipeline is handled.
         */
        public boolean isSubpipeline() {
            return QmConstants.TYPE_SUBPIPELINE.equals(variable.getDeclaration().getType().getName());
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
        
        @Override
        public IDecisionVariable getVariable() {
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
            String projectName = pName + QmConstants.CFG_POSTFIX;
            ModelAccess.removeConfiguration(projectName);
            Configuration cfg = modelPart.getConfiguration();
            ModelInfo<Project> info = VarModel.INSTANCE.availableModels()
                    .getModelInfo(cfg.getProject());
            // delete the related elements in the main ivml file -PipelinesCfg.ivml
            ProjectImport pImport = new ProjectImport(projectName, null);
            boolean dImport = BasicIVMLModelOperations.modifyImports(cfg.getProject(),
                BasicIVMLModelOperations.DEL, pImport);

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
                            IDecisionVariable pipelineVar = getVariable(); // take data from
                            Dialogs.showInfoDialog("Ivy Publish", "Will deploy '" 
                                + ModelAccess.getDisplayName(pipelineVar) + "' to " + deploymentUrl
                                + "eu/qualimaster/PatriksTestDeployment"); //TODO: !!!!
                            createProgressDialog(DialogsUtil.getActiveShell(), pipelineVar);
                        }
                    };
                    //only enable the deploy button if this specific pipeline has been instantiated
                    action.setEnabled(isInfrastructureAdmin && (null != deploymentUrl && deploymentUrl.length() > 0)
                            && isInstantiated());
                    action.setText("Deploy...");
                    if (!action.isEnabled()) {
                        String toolTip = null;
                        if (!isInfrastructureAdmin) {
                            toolTip = "You need to be infrastructure admin to deploy this pipeline.";
                        } else if (!isInstantiated()) {
                            toolTip = "The pipeline has not been instantiated yet.";
                        } else if (!(null != deploymentUrl && deploymentUrl.length() > 0)) {
                            toolTip = "Please specify the deployment URL.";
                        }
                        action.setToolTipText(toolTip);
                    }
                    manager.add(action);
                    action = new Action() {
                        @Override
                        public void run() {
                            IDecisionVariable var = getVariable();
                            Infrastructure.send(new PipelineMessage(ModelAccess.getDisplayName(var), 
                                PipelineMessage.Status.START).elevate()); // admin permission, elevate implicit
                        }
                    };
                    UIUtils.customizeAdminInfraAction(action, "Start...");
                    manager.add(action);

                    action = new Action() {
                        @Override
                        public void run() {
                            IDecisionVariable var = getVariable();
                            Infrastructure.send(new PipelineMessage(ModelAccess.getDisplayName(var), 
                                PipelineMessage.Status.STOP).elevate()); // admin permission, elevate implicit
                        }
                    };
                    UIUtils.customizeAdminInfraAction(action, "Stop...");
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
     * Create progress Dialog.
     * 
     * @param parent
     *            parent composite.
     * @param pipelineVar the IDecisionVariable of the target pipeline.
     */
    private static void createProgressDialog(Composite parent, IDecisionVariable pipelineVar) {
        try {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(parent.getShell()) {

                @Override
                protected void setShellStyle(int newShellStyle) {
                    super.setShellStyle(SWT.CLOSE | SWT.INDETERMINATE
                            | SWT.BORDER | SWT.TITLE);
                    setBlockOnOpen(false);
                }
            };
            pmd.run(false, true, new ProgressDialogOperation(pipelineVar));
            
        } catch (final InvocationTargetException e) {
            Throwable exc = e;
            if (null != e.getCause()) {
                exc = e.getCause();
            }
            EasyProducerDialog.showDialog(null, "Error", "Error: " + exc.getMessage(), true);
//            MessageDialog.openError(parent.getShell(), "Error", "Error: " + exc.getMessage());
            e.printStackTrace();
        } catch (final InterruptedException e) {
            MessageDialog.openInformation(parent.getShell(), "Cancelled",
                    "Error: ");
            e.printStackTrace();
        }
        
    }
    
    /**
     * This Operation is used to monitor the Deployment and its progress.
     */
    private static class ProgressDialogOperation implements IRunnableWithProgress {
        
        private IDecisionVariable pipelineVar;
        
        /**
         * Calls the super constructor and handles the pipeline IDecisionVariable for uploading purposes.
         * @param pipelineVar The IDecisionVariable of the target pipeline.
         */
        public ProgressDialogOperation(IDecisionVariable pipelineVar) {
            super();
            this.pipelineVar = pipelineVar;
        }
        
        @Override
        public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
            
            startDeployment(monitor, pipelineVar);
            
            monitor.done();
        }
        
    }
    
    /**
     * Returns true if the specific pipeline has been instantiated.
     * @return True if the specific pipeline has been instantiated. False otherwise.
     */
    private static boolean isInstantiated() {
        
        boolean isInstantiated = false;
        File instantiationDir = Location.getInstantiationFolder();
        if (Location.hasInstantiated()) {
            isInstantiated = true; //pipelineVar
        }
        System.out.println("isInstantiated: " + isInstantiated);
        return isInstantiated;
    }
    
    /**
     * Starts the deployment process and monitors it.
     * @param monitor The used monitor.
     * @param pipelineVar The IDecisionVariable of the target pipeline.
     */
    private static void startDeployment(IProgressMonitor monitor, IDecisionVariable pipelineVar) {
        
        int userSelection = Dialogs.showInfoConfirmDialog("Overwrite?", 
                "Do you want to overwrite, in case of an existing artifact?");
        boolean overwrite;
        if (userSelection == SWT.YES) {
            overwrite = true;
        } else {
            overwrite = false;
        }
        
        System.out.println("Overwrite = " + overwrite);
        
        IDecisionVariable artifact = pipelineVar.getNestedElement("artifact");
        //example: eu.qualimaster:PriorityPip:0.0.2-SNAPSHOT
        String artifactName = artifact.getValue().getValue().toString();
        
        
        
        EclipseProgressObserver obs = new EclipseProgressObserver();
        obs.register(monitor);
        
        ManifestConnection con = new ManifestConnection();
        File instFile = Location.getInstantiationFolder(); 
        //SessionModel.INSTANCE.getInstantationFolder();
        //TODO: commented line is a DIRTY hack for quicker testing of the publishing feature,
        //since the instantiation path is discarded once the application is closed.
        //instFile = new File("C:\\Instant_Test"); 
        if (null != instFile && instFile.exists()) {
            
            String instDir = instFile.getAbsolutePath();
            String pipelineDir = instDir + File.separator + "pipelines"; //TODO: move to constants or find in model!!!!
            String[] artifactNameSplitted = artifactName.split(":")[0].split("\\.");
            for (int i = 0; i < artifactNameSplitted.length; i++) {
                pipelineDir += File.separator + artifactNameSplitted[i];
            }
            
            pipelineDir += File.separator + artifactName.split(":")[1];
            String pomFile = pipelineDir + File.separator + "pom.xml";
            String dir = pipelineDir + File.separator + "target";
            String jarName = artifactName.split(":", 2)[1].replace(":", "-");
            String jarFile = dir + File.separator + jarName + ".jar";
            
            //TODO: This is a testing hack. For safer testing all uploads are redirected to a test dir!
            final String deploymentUrl = ModelAccess.getDeploymentUrl() 
                   + "eu/qualimaster/PatriksTestDeployment";
            System.out.println("##### " + jarFile);
            
//            try {
                //con.publishDirWithPom(dir, pomFile, deploymentUrl, overwrite, obs);
            con.publishWithPom(jarFile, pomFile, deploymentUrl, artifactName.split(":")[2], overwrite);
//            } catch (ManifestUtilsException e) {
//                Dialogs.showErrorDialog("ERROR", e.getMessage());
//            }
        } else {
            Dialogs.showErrorDialog("Error", "Unable to locate instantiation folder at: '" + instFile + "'.");
        }
        
        obs.unregister(monitor);
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
        if (comVariable.getNestedVariable(QmConstants.SLOT_NAME).hasValue()) {
            displayName = comVariable.getNestedVariable(QmConstants.SLOT_NAME).getValue().toString();
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
