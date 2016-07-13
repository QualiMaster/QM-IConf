/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.editors;

import static eu.qualimaster.easy.extension.QmConstants.SLOT_ALGORITHM_DESCRIPTION;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_ALGORITHM_TOPOLOGYCLASS;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_INPUT;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_OUTPUT;
import static eu.qualimaster.easy.extension.QmConstants.SLOT_PARAMETERS;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_ALGORITHM;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_DATASINK;
import static eu.qualimaster.easy.extension.QmConstants.TYPE_DATASOURCE;

import java.lang.reflect.InvocationTargetException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.SearchPattern;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.DialogsUtil;
import de.uni_hildesheim.sse.qmApp.images.IconManager;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import eu.qualimaster.manifestUtils.ManifestConnection;
import eu.qualimaster.manifestUtils.ManifestUtilsException;
import eu.qualimaster.manifestUtils.data.Algorithm;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Manifest;
import eu.qualimaster.manifestUtils.data.Manifest.ManifestType;
import eu.qualimaster.manifestUtils.data.Parameter;
import net.ssehub.easy.basics.logger.EASyLoggerFactory;
import net.ssehub.easy.basics.progress.ProgressObserver;
import net.ssehub.easy.producer.eclipse.observer.EclipseProgressObserver;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.AssignmentState;
import net.ssehub.easy.varModel.confModel.CompoundVariable;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.ConfigurationException;
import net.ssehub.easy.varModel.confModel.ContainerVariable;
import net.ssehub.easy.varModel.confModel.IConfigurationElement;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.ModelQuery;
import net.ssehub.easy.varModel.model.ModelQueryException;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Container;
import net.ssehub.easy.varModel.model.datatypes.CustomDatatype;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.filter.DatatypeFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder;
import net.ssehub.easy.varModel.model.filter.DeclarationFinder.VisibilityType;
import net.ssehub.easy.varModel.model.filter.FilterType;
import net.ssehub.easy.varModel.model.values.NullValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import qualimasterapplication.Activator;

/**
 * The {@link ArtifactEditor} specializes {@link AbstractTextSelectionEditorCreator} to select from a 
 * class list.
 * 
 * @author Holger Eichelberger
 * @author Patrik Pastuschek
 */
public class ClassEditor extends AbstractTextSelectionEditorCreator {

    public static final boolean ENABLE_BROWSE = true;
    public static final IEditorCreator CREATOR = new ClassEditor();
    private static final String ERROR_PREFIX = "Manifest analysis";
    
    private String artifact = null;
    private ManifestConnection con = null;
    private Map<String, AbstractVariable> instances;
    
    /**
     * Prevents external creation.
     */
    private ClassEditor() {
    }

    @Override
    protected void browseButtonSelected(String text, IDecisionVariable context, ITextUpdater updater) {
        List<String> classes = new ArrayList<String>();
        artifact = getArtifact(context);
        if (null != artifact && !artifact.isEmpty()) {
            
            System.out.println("RETRIEVE CLASSES FROM " + artifact);
            
            createProgressDialog(DialogsUtil.getActiveShell());
            
            List<String> list;
            try {
                list = con.getAllValidClasses(ManifestConnection.getArtifactId(artifact));
                for (String s : list) {
                    classes.add(s);
                }
            } catch (ManifestUtilsException e1) {
                Dialogs.showErrorDialog("ERROR", "Manifest corrupt: " + e1.getMessage());
                e1.printStackTrace();
            }

            
            ClassSelectorDialog dlg = new ClassSelectorDialog(DialogsUtil.getActiveShell(), "Select class", classes);
            dlg.setInitialPattern("?");
            int res = dlg.open();
            String cls = dlg.getFirstResult();
            if (Window.OK == res && null != cls) {
                boolean changed = false;
                try {
                    Value clsValue = ValueFactory.createValue(context.getDeclaration().getType(), cls);
                    context.setValue(clsValue, AssignmentState.ASSIGNED);
                    changed = true;
                } catch (ValueDoesNotMatchTypeException e) {
                    EASyLoggerFactory.INSTANCE.getLogger(ClassEditor.class, Activator.PLUGIN_ID).exception(e);
                } catch (ConfigurationException e) {
                    EASyLoggerFactory.INSTANCE.getLogger(ClassEditor.class, Activator.PLUGIN_ID).exception(e);
                }
                updateArtifactInfo(cls, ManifestConnection.getArtifactId(artifact), context);
                if (changed) {
                    updateEditors(getTitle(context));
                } else {
                    updater.updateText(cls);
                }
            }
            
        } else {
            
            Dialogs.showInfoDialog("No artifact selected", "Please select an artifact first!");
            
        }
        
    }
    
    /**
     * Updates the editor tables. Used when new data is inserted into the model.
     * TODO: move this and informEditor(VariableEditor) into a utility class!
     * @param title The title of this editor, to ensure that only THIS editor is updated!
     */
    private static void updateEditors(String title) {
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        IEditorReference[] allOpenEditors = page.getEditorReferences();
        for (int i = 0; i < allOpenEditors.length; i++) {
            IEditorPart editor = allOpenEditors[i].getEditor(false);
            if (editor instanceof VariableEditor && editor.getTitle().equals(title)) {
                VariableEditor varEditor = (VariableEditor) editor;
                varEditor.refreshNestedEditors();
                informEditor(varEditor);
            }
        }
    }
    
    /**
     * Updates a single editor.
     * @param editor The editor to update.
     */
    private static void informEditor(final VariableEditor editor) {
        Display.getDefault().syncExec(new Runnable() {
        
            @Override
            public void run() {
                editor.refreshEditor();
            }
        });
    }
    
    /**
     * Updates the In- and Output and the Parameters of the artifact.
     * @param className The name of the class.
     * @param artifactId The name of the artifact.
     * @param context The current context.
     */
    private void updateArtifactInfo(String className, String artifactId, IDecisionVariable context) {

        List<Item> input = new ArrayList<Item>();
        List<Parameter> param = new ArrayList<Parameter>();
        List<Item> output = new ArrayList<Item>();
        try {
            input = con.getInput(className, artifactId);
            output = con.getOutput(className, artifactId);
            param = con.getParameters(className, artifactId);
        } catch (ManifestUtilsException e) {
            Dialogs.showErrorDialog(e.getShortMessage(), e.getDetailedMessage());
        }
        Manifest uManifest = null;
        try {
            uManifest = con.getUnderlyingManifest(artifactId);
        } catch (ManifestUtilsException e) {
            Dialogs.showErrorDialog("ERROR", "Manifest corrupt: " + e.getMessage());
            e.printStackTrace();
        }
        ManifestType type = ManifestType.UNKNOWN;
        
        IDecisionVariable var = (IDecisionVariable) context.getParent();
        
        if (null == uManifest) {
            Dialogs.showInfoDialog("No manifest", "No manifest could be found!");
        } else {
            type = uManifest.getType();
        }
        
        if (takeOverValues(var, type)) {
            createMap(var);
            
            var.unfreeze(AssignmentState.ASSIGNED);
            List<IDecisionVariable> inputVars = getIDecisionVariable(var, SLOT_INPUT);
            List<IDecisionVariable> outputVars = getIDecisionVariable(var, SLOT_OUTPUT);
            List<IDecisionVariable> parameterVars = getIDecisionVariable(var, SLOT_PARAMETERS);
            
            if (type.isSource() || type.isSink()) {
                List<IDecisionVariable> tmp = inputVars;
                inputVars = outputVars;
                outputVars = tmp;
            }
            
            ensureParent(outputVars);
            ensureParent(parameterVars);
            clearInput(inputVars);
            clearInput(outputVars);
            clearInput(parameterVars);
            
            if (outputVars.size() > 0) {
                outputVars.remove(0);
            }
            
            addInOut(input, inputVars, true);
            addInOut(output, outputVars, false);
            addParameter(param, parameterVars, uManifest, artifactId);
            setTopoClass(var, type, className);
            if (null != uManifest) {
                Algorithm alg = uManifest.getMember(artifactId);
                if (!uManifest.getMembers().isEmpty() && null == alg) {
                    alg = uManifest.getMembers().iterator().next();
                }
                if (null != alg) {
                    setDescription(var, alg.getDescription());
                }
            }
            ModelAccess.freezeAgain(var);
        }
        
    }

    /**
     * Adds the parent to <code>vars</code> - whyever.
     * 
     * @param vars the vars to be modified
     */
    private void ensureParent(List<IDecisionVariable> vars) {
        if (vars.size() > 0) {
            // may easily fail on top-level variables
            vars.add(0, (IDecisionVariable) vars.get(0).getParent());
        }
    }
    
    /**
     * Checks manifest type versus variable type and asks the user on how to go on.
     * 
     * @param var the variable
     * @param manifestType the obtained manifest type
     * @return <code>true</code> for take over values, <code>false</code> for skip
     */
    private boolean takeOverValues(IDecisionVariable var, ManifestType manifestType) {
        Boolean result = null;
        IDatatype varType = var.getDeclaration().getType();
        Project prj = var.getConfiguration().getProject();
        try {
            final IDatatype algType = ModelQuery.findType(prj, TYPE_ALGORITHM, null);
            if (null != algType && algType.isAssignableFrom(varType)) { // null -> depends on config view
                result = manifestType.isAlgorithm();
            } 
            if (null == result) {
                final IDatatype srcType = ModelQuery.findType(prj, TYPE_DATASOURCE, null);
                if (null != srcType && srcType.isAssignableFrom(varType)) { // null -> depends on config view
                    result = manifestType.isSource();
                } 
            }
            if (null == result) {
                final IDatatype snkType = ModelQuery.findType(prj, TYPE_DATASINK, null);
                if (null != snkType && snkType.isAssignableFrom(varType)) { // null -> depends on config view
                    result = manifestType.isSink();
                }
            }
        } catch (ModelQueryException e) {
            Dialogs.showErrorDialog(ERROR_PREFIX + " - Model problem", e.getMessage());
        }
        if (null == result) {
            Dialogs.showErrorDialog(ERROR_PREFIX, "Cannot apply the manifest to this editor. Ignoring.");
            result = false;
        } else {
            if (!result) {
                int msgCode = Dialogs.showInfoConfirmDialog(ERROR_PREFIX, "Type in manifest does not match type of "
                    + "configurable element. Take manifest information over (where applicable) anyway?");
                if (SWT.YES == msgCode) {
                    result = true;
                }
            }
        }
        return result;
    }
    
    /**
     * Sets the topology class information if possible.
     * 
     * @param var the decision variable to modify
     * @param type the manifest type.
     * @param className The name of the class.
     */
    private void setTopoClass(IDecisionVariable var, ManifestType type, String className) {
        try {
            IDecisionVariable topo = var.getNestedElement(SLOT_ALGORITHM_TOPOLOGYCLASS);
            if (null != topo && StringType.TYPE.isAssignableFrom(topo.getDeclaration().getType())) {
                String topoCls = null;
                if (ManifestType.STORMBASED == type) {
                    int pos = className.lastIndexOf('.');
                    if (pos > 0 && pos < className.length() - 1) {
                        topoCls = className.substring(pos + 1);
                    } else if (pos < 0) {
                        topoCls = className;
                    } // else invalid
                }
                Value value;
                if (null == topoCls) {
                    value = NullValue.INSTANCE;
                } else {
                    value = ValueFactory.createValue(StringType.TYPE, topoCls);
                }
                topo.setValue(value, AssignmentState.ASSIGNED);
            }
        } catch (ConfigurationException | ValueDoesNotMatchTypeException e) {
            Dialogs.showErrorDialog(ERROR_PREFIX, "Unable to set topology name: " + e.getMessage());
        }
    }
    
    /**
     * Sets the description (usually extracted from the manifest).
     * 
     * @param var The decision variable to modify.
     * @param description The actual description.
     */
    private void setDescription(IDecisionVariable var, String description) {
        if (null != var && null != description && !description.isEmpty()) {
            try {
                IDecisionVariable descVar = var.getNestedElement(SLOT_ALGORITHM_DESCRIPTION);
                Value value = ValueFactory.createValue(StringType.TYPE, description);
                descVar.setValue(value, AssignmentState.ASSIGNED);
            } catch (ConfigurationException | ValueDoesNotMatchTypeException e) {
                Dialogs.showErrorDialog(ERROR_PREFIX, "Unable to set description: " + e.getMessage());
            }
        }
    }
    
    /**
     * Adds parameters into the editor.
     * @param param A list of parameters to add.
     * @param list The actual list-object of the editor.
     * @param manifest The underlying manifest, used to for default values for parameters.
     * @param algorithmName The name of the processed algorithm.
     */
    public void addParameter(List<Parameter> param, List<IDecisionVariable> list, 
            Manifest manifest, String algorithmName) {
        
        if (!list.isEmpty()) {
         
            IDecisionVariable base = list.get(0);
            Configuration config = base.getConfiguration();
            Project project = config.getProject(); 
            ContainerVariable container = null;   
            List<IDecisionVariable> found = null;         
            found = getIDecisionVariable(base, "parameters");
            IDecisionVariable var = found.get(0); 
            container = (ContainerVariable) var;
            addManifestParameters(param, manifest, algorithmName);
            
            for (Parameter p : param) {
                
                try {

                    container.addNestedElement(); 

                    IDatatype containerType = container.getDeclaration().getType();
                    // The container could be a deriveddatatype -> resolve to basis to be sure that we have a container
                    containerType = DerivedDatatype.resolveToBasis(containerType);
                    IDatatype containedType = null;
                    if (containerType instanceof Container) {
                        containedType = ((Container) containerType).getContainedType();
                    }
                    
                    AbstractVariable parameterType = instances.get(p.getNormalizedTypeName());
                    parameterType = getVarInstance(p.getNormalizedTypeName());     
                    DatatypeFinder finder = new DatatypeFinder(project, FilterType.ALL, containedType);
                    List<CustomDatatype> foundTypes = finder.getFoundDatatypes();
                    
                    if (null != parameterType) {
                        
                        Value refValue = ValueFactory.createValue(
                                new Reference("", parameterType.getType(), var.getConfiguration().getProject()), 
                                parameterType); //instances.get(p.getNormalizedTypeName())
                        IDatatype refinedType = null;
                        
                        for (CustomDatatype cDtype : foundTypes) {
                            if (cDtype.getName().contains(p.getNormalizedTypeName())) {
                                refinedType = cDtype;  
                            }       
                        }
                        if (null != refinedType) {
                            
                            String defaultValue = getDefaultParamValue(manifest, p, algorithmName);
                            //"name", "class" and "defaultValue" must not be changed, they are the names of the columns!
                            Value val = ValueFactory.createValue(refinedType, 
                                    new Object[] {"name", new String(p.getName()), 
                                        "class", refValue, "defaultValue", defaultValue});
                        
                            container.getNestedElement(container.getNestedElementsCount() - 1)
                                .setValue(val, AssignmentState.ASSIGNED);        
                        }        
                    } else {
                        System.err.println("Unable to find ParameterType for: " + p.getNormalizedTypeName());
                    }  
                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ValueDoesNotMatchTypeException e) {
                    e.printStackTrace();
                }     
            }     
        }    
    }
    
    /**
     * Checks for an existing default value inside the manifest, 
     * since default values can (usually) not be extracted from the classes.
     * @param manifest The underlying manifest.
     * @param param The actual parameter.
     * @param algorithmName The name of the processed algorithm.
     * @return The default value for that parameter or null if none exists.
     */
    private String getDefaultParamValue(Manifest manifest, Parameter param, String algorithmName) {
        String defaultValue = null;
        if ((null == param || null == param.getValue() || param.getValue().isEmpty()) && null != manifest) {
            Algorithm alg = manifest.getMember(algorithmName);
            if (!manifest.getMembers().isEmpty() && null == alg) {
                alg = manifest.getMembers().iterator().next();
            }
            if (null != alg) {
                for (Parameter mParam : alg.getParameters()) {
                    if (param.getName().equalsIgnoreCase(mParam.getName())) {
                        defaultValue = mParam.getValue();
                    }
                        
                }
            } else {
                defaultValue = param.getValue();
            }
        } else {
            defaultValue = param.getValue();
        }
        return defaultValue;
    }
    
    /**
     * Adds all parameters from the manifest into the given parameter list, while avoiding duplicates.
     * @param params The existing list of parameters.
     * @param manifest The manifest to take parameters from.
     * @param algorithmName The name of the processed algorithm.
     */
    private void addManifestParameters(List<Parameter> params, Manifest manifest, String algorithmName) {
        
        if (null != manifest && null != params) {
            Algorithm alg = manifest.getMember(algorithmName);
            if (!manifest.getMembers().isEmpty() && null == alg) {
                alg = manifest.getMembers().iterator().next();
            }
            if (null != alg) {
                for (Parameter mParam : alg.getParameters()) {
                    
                    boolean exists = false;
                    for (Parameter param : params) {
                        
                        if (param.getName().equalsIgnoreCase(mParam.getName())) {
                            
                            exists = true;
                            break;
                            
                        }
                        
                    }
                    if (!exists) {
                        params.add(mParam);
                    }
                    
                }
            }
        }
        
    }
    
    /**
     * Adds input information for the artifact.
     * @param input A list of input items.
     * @param list The base for model operations.
     * @param isInput true if this is input, false for output.
     */
    private void addInOut(List<Item> input, List<IDecisionVariable> list, boolean isInput) {
        
        if (!list.isEmpty()) {
            
            IDecisionVariable base = list.get(0);
            Configuration config = base.getConfiguration();
            Project project = config.getProject();
            ContainerVariable container = null;
            
            List<IDecisionVariable> found = null;
            found = getIDecisionVariable(base, "fields");

            if (found.isEmpty()) {
                found.add(base); 
            }
            
            container = (ContainerVariable) base;
            
            for (Item item : input) {

                if (container.getNestedElementsCount() == 0) {
                    container.addNestedElement();
                }
                ((ContainerVariable) container.getNestedElement(
                        container.getNestedElementsCount() - 1).getParent()).addNestedElement();
                
                try {
                    
                    container.getNestedElement(container.getNestedElementsCount() - 1)
                        .getNestedElement(0).setValue(ValueFactory.createValue(
                            StringType.TYPE, new String(item.getName())), AssignmentState.ASSIGNED);
                    
                    int count = 0;
                    
                    for (Field f : item.getFields()) {                  

                        ((ContainerVariable) container.getNestedElement(container.getNestedElementsCount() - 1)
                                .getNestedElement(1)).addNestedElement();
                        
                        IDecisionVariable second = container.getNestedElement(
                                container.getNestedElementsCount() - 1).getNestedElement(1)
                                .getNestedElement(count);
                        
                        container.getNestedElement(container.getNestedElementsCount() - 1)
                            .getNestedElement(1).getNestedElement(count).getNestedElement(0)
                            .setValue(ValueFactory.createValue(StringType.TYPE, f.getName()), 
                                    AssignmentState.ASSIGNED);
                        
                        AbstractVariable neededType = getVarInstance(f.getFieldType().getNormalizedName()); 
                        
                        if (null != neededType) {
                            
                            Value refValue = ValueFactory.createValue(
                                    new Reference("", neededType.getType(), project), neededType
                                    );
                            
                            second.getNestedElement(1).setValue(refValue, AssignmentState.ASSIGNED);
                            
                        }   
                        count++;
                    }

                } catch (ConfigurationException e) {
                    e.printStackTrace();
                } catch (ValueDoesNotMatchTypeException e) {
                    e.printStackTrace();
                }  
            }
        }     
    }
    
    /**
     * Attempts to resolve the name and get a matching AbstractVariable.
     * @param name The name of the type.
     * @return A matching AbstractVariable or null if none was found.
     */
    private AbstractVariable getVarInstance(String name) {
        
        if (name.equalsIgnoreCase("double")) {
            name = "real";
        }
        
        AbstractVariable result = null;
        Set<String> keys = instances.keySet();
        for (String key : keys) {
            String temp = instances.get(key).getName().toLowerCase();
            if (temp.substring(0, temp.length() - 4).equals(name.toLowerCase())) {
                    //|| temp.equals(name.toLowerCase())) { //|| temp.contains(name.toLowerCase())
                result = instances.get(key);
                break;
            }
        }
        
        return result;
        
    }
    
    /**
     * Creates a map of type-references.
     * @param var A IDecisionVariable with access to the configuration.
     */
    private void createMap(IDecisionVariable var) {
        
        Project project = var.getConfiguration().getProject();
        IDatatype type;
        try {
            type = ModelQuery.findType(project, "FieldType", null);
            DeclarationFinder finder = new DeclarationFinder(project, FilterType.ALL, type);
            List<AbstractVariable> declarations = finder.getVariableDeclarations(VisibilityType.ALL);
            instances = new HashMap<String, AbstractVariable>();
            for (int i = 0; i < declarations.size(); i++) {
                IDecisionVariable variable = var.getConfiguration().getDecision(declarations.get(i));
                instances.put(variable.getNestedElement(1).getValue().getValue().toString(), variable.getDeclaration());
            }
        } catch (ModelQueryException e) {
            e.printStackTrace();
        }   
        
    }
    
    /**
     * Removes all input information for the artifact.
     * @param list The base for model operations.
     */
    private void clearInput(List<IDecisionVariable> list) {
        
        if (!list.isEmpty()) {
            
            for (IDecisionVariable var : list) {
                IDecisionVariable base = var;
                for (int i = 0; i < base.getNestedElementsCount(); i++) {
                    clearInput(base.getNestedElement(i), false);
                }
            }
        
        }

    }
    
    /**
     * Removes all input information for the artifact.
     * @param base The base for model operations.
     * @param deleteBase True if the base itself should be deleted aswell.
     */
    private void clearInput(IDecisionVariable base, boolean deleteBase) {
        
        for (int i = 0; i < base.getNestedElementsCount(); i++) {
            clearInput(base.getNestedElement(i), true);
        }
        
        if (base.getParent() instanceof ContainerVariable && deleteBase) {
            ContainerVariable container = (ContainerVariable) base.getParent();
            for (int i = 0; i < container.getNestedElementsCount(); i++) {
                container.removeNestedElement(container.getNestedElement(i));
            }
        }
        
    }
    
    /**
     * Returns a list of nested elements with the given name, can be empty.
     * @param parent The base element.
     * @param name The name of the desired nested element.
     * @return A list of matching elements, can be empty.
     */
    private List<IDecisionVariable> getIDecisionVariable(IDecisionVariable parent, String name) {
        
        List<IDecisionVariable> result = new ArrayList<IDecisionVariable>();
        
        if (parent.getDeclaration().getName().equals(name)) {
            result.add(parent);
        }
        
        if (parent.getNestedElementsCount() > 0) {
            for (int i = 0; i < parent.getNestedElementsCount(); i++) {
                result.addAll(getIDecisionVariable(parent.getNestedElement(i), name));
            }
        }
        
        return result;
        
    }
    
    @Override
    protected boolean isTextEditorEnabled(boolean cell, IDecisionVariable context) {
        return true;
    }
    
    @Override
    protected boolean isBrowseButtonActive(boolean cell, IDecisionVariable context) {
        return ENABLE_BROWSE; // && null != getArtifact(context);
    }
    
    /**
     * Returns the artifact specification from the (parent) decision variable.
     * 
     * @param context the context for this editor
     * @return the artifact specification (if available, may be <b>null</b> if there is none)
     */
    private String getArtifact(IDecisionVariable context) {
        String result = null;
        if (null != context) {
            IConfigurationElement par = context.getParent();
            if (par instanceof CompoundVariable) {
                CompoundVariable artifactHolder = (CompoundVariable) par;
                IDecisionVariable artifactVar = artifactHolder.getNestedVariable("artifact");
                if (null != artifactVar) {
                    Value val = artifactVar.getValue();
                    if (val instanceof StringValue) {
                        result = ((StringValue) val).getValue();
                    }
                }
            }
        }
        return result;
    } 
    
    /**
     * Returns the artifact specification from the (parent) decision variable.
     * 
     * @param context the context for this editor
     * @return the artifact specification (if available, may be <b>null</b> if there is none)
     */
    private String getTitle(IDecisionVariable context) {
        String result = null;
        if (null != context) {
            IConfigurationElement par = context.getParent();
            if (par instanceof CompoundVariable) {
                CompoundVariable artifactHolder = (CompoundVariable) par;
                IDecisionVariable artifactVar = artifactHolder.getNestedVariable("name");
                if (null != artifactVar) {
                    Value val = artifactVar.getValue();
                    if (val instanceof StringValue) {
                        result = ((StringValue) val).getValue();
                    }
                }
            }
        }
        return result;
    } 
    
    /**
     * Create progress Dialog.
     * 
     * @param parent
     *            parent composite.
     */
    protected void createProgressDialog(Composite parent) {
        try {
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(parent.getShell()) {

                @Override
                protected void setShellStyle(int newShellStyle) {
                    super.setShellStyle(SWT.CLOSE | SWT.INDETERMINATE
                            | SWT.BORDER | SWT.TITLE);
                    setBlockOnOpen(false);
                }
            };
            pmd.run(true, true, new ProgressDialogOperation());
            
        } catch (final InvocationTargetException e) {
            Throwable exc = e;
            if (null != e.getCause()) {
                exc = e.getCause();
            }
            MessageDialog.openError(parent.getShell(), "Error", "Error: " + exc.getMessage());
            e.printStackTrace();
        } catch (final InterruptedException e) {
            MessageDialog.openInformation(parent.getShell(), "Cancelled",
                    "Error: ");
            e.printStackTrace();
        }
        
    }
    
    /**
     * This Operation is used to monitor the ManifestConnection and its progress.
     */
    class ProgressDialogOperation implements IRunnableWithProgress {
        
        @Override
        public void run(final IProgressMonitor monitor)
            throws InvocationTargetException, InterruptedException {
            
            startManifestConnection(monitor);
            
            monitor.done();
        }
        
    }
    
    /**
     * Starts the ManifestConnection process and monitors it.
     * @param monitor The used monitor.
     */
    private void startManifestConnection(IProgressMonitor monitor) {
        EclipseProgressObserver obs = new EclipseProgressObserver();
        obs.register(monitor);
        resolveManifest(obs);
        obs.unregister(monitor);
    }
    
    /**
     * Will actually use the ManifestConnection in order to retrieve and resolve the manifest information.
     * @param observer The used observer.
     */
    private void resolveManifest(ProgressObserver observer) {
        
        con = new ManifestConnection();
        String artifactId = ManifestConnection.getArtifactId(artifact);
        String groupId = ManifestConnection.getGroupId(artifact);
        String version = ManifestConnection.getVersion(artifact);  
        
        System.out.println(artifact);
        System.out.println(artifactId);
        System.out.println(groupId);
        System.out.println(version);
        
        con.load(observer, groupId, artifactId, version);
        
    }
    
    /**
     * Keep this class private until the interface of Patrick is clear.
     * 
     * @author Holger Eichelberger
     */
    private static class ClassSelectorDialog extends FilteredItemsSelectionDialog {

        private List<String> classes;

        /**
         * Creates the selector dialog.
         * 
         * @param shell the parent shell
         * @param title the title of the dialog
         * @param classes the classes to select from
         */
        public ClassSelectorDialog(Shell shell, String title, List<String> classes) {
            super(shell);
            this.classes = classes;
            setTitle(title);
            setListLabelProvider(new ClassListLabelProvider(false));
            setDetailsLabelProvider(new ClassListLabelProvider(true));
            setSelectionHistory(new ClassSelectionHistory());
        }

        /**
         * Implements an empty variable selection history.
         * 
         * @author Holger Eichelberger
         */
        private static class ClassSelectionHistory extends SelectionHistory {
            
            @Override
            protected Object restoreItemFromMemento(IMemento element) {
                return null; 
            }

            @Override
            protected void storeItemToMemento(Object item, IMemento element) {
            }
             
        }
        
        /**
         * Implements a list label provider for variables.
         * 
         * @author Holger Eichelberger
         */
        private class ClassListLabelProvider implements ILabelProvider {

            private boolean qualified;
            
            /**
             * Creates a label provider.
             * 
             * @param qualified display qualified names or not
             */
            private ClassListLabelProvider(boolean qualified) {
                this.qualified = qualified;
            }
            
            @Override
            public void addListener(ILabelProviderListener listener) {
            }

            @Override
            public void dispose() {
            }

            @Override
            public boolean isLabelProperty(Object element, String property) {
                return false;
            }

            @Override
            public void removeListener(ILabelProviderListener listener) {
            }

            @Override
            public Image getImage(Object element) {
                return IconManager.retrieveImage(IconManager.CLASS);
            }

            @Override
            public String getText(Object element) {
                String name = getElementName(element);
                if (!qualified) {
                    int pos = name.lastIndexOf('.');
                    if (pos > 0 && pos < name.length() - 1) {
                        name = name.substring(pos + 1);
                    }
                }
                return name;
            }
            
        }
        
        @Override
        protected Control createExtendedContentArea(Composite parent) {
            return null;
        }

        @Override
        protected IDialogSettings getDialogSettings() {
            return DialogsUtil.getDialogSettings("FilteredClassSelectorDialogSettings");
        }

        @Override
        protected IStatus validateItem(Object item) {
            return Status.OK_STATUS;
        }

        @Override
        protected ItemsFilter createFilter() {
            SearchPattern pattern = new SearchPattern();
            pattern.setPattern(getInitialPattern());
            return new ItemsFilter(pattern) {
                
                @Override
                public boolean matchItem(Object item) {
                    return matches(item.toString());
                }
            
                @Override
                public boolean isConsistentItem(Object item) {
                    return true;
                }
            };
        }

        @Override
        protected Comparator<Object> getItemsComparator() {
            return Collator.getInstance();
        }

        @Override
        protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
            IProgressMonitor progressMonitor) throws CoreException {
            for (int c = 0; c < classes.size(); c++) {
                contentProvider.add(classes.get(c), itemsFilter);
            }
        }

        @Override
        public String getElementName(Object item) {
            return item.toString();
        }

        @Override
        public String getFirstResult() {
            return (String) super.getFirstResult();
        }

    }

}
