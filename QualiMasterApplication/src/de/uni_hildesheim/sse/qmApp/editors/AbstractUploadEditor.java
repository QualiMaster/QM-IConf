package de.uni_hildesheim.sse.qmApp.editors;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import de.uni_hildesheim.sse.easy_producer.observer.EclipseProgressObserver;
import de.uni_hildesheim.sse.model.confModel.AllFreezeSelector;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
import de.uni_hildesheim.sse.model.varModel.Project;
import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.datatypes.CustomDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.DerivedDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
//import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.filter.DatatypeFinder;
import de.uni_hildesheim.sse.model.varModel.filter.DeclarationFinder;
import de.uni_hildesheim.sse.model.varModel.filter.DeclarationFinder.VisibilityType;
import de.uni_hildesheim.sse.model.varModel.filter.FilterType;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
import de.uni_hildesheim.sse.utils.progress.ProgressObserver;
import eu.qualimaster.manifestUtils.ManifestConnection;
import eu.qualimaster.manifestUtils.data.Field;
import eu.qualimaster.manifestUtils.data.Item;
import eu.qualimaster.manifestUtils.data.Parameter;

/**
 * A specialized editor for algorithms. This editor is not complete
 * as details on the algorithm packages are not agreed upon at the moment.
 * 
 * @author Holger Eichelberger
 */
public abstract class AbstractUploadEditor extends VariableEditor {

    private Button upload;
    private Map<String, AbstractVariable> instances;
    
    @Override
    protected void createAdditionalControls(final Composite parent) {
        disableInput();
        
        upload = new Button(parent, SWT.NULL);
        upload.setText("upload (experimental)");
        upload.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                onUpload(parent);
            }
        });
        new Label(parent, SWT.NONE); // fill the left side
    }
    
    /**
     * Disable all inputs.
     */
    protected void disableInput() {
        // exclude "name" - better way than fixed constant would be nice
        // reenabled so far until upload is available
        /*for (int e = 1, count = getEditorCount(); e < count; e++) {
            getEditor(e).setEnabled(false);
        }*/
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
            if (temp.substring(0, temp.length() - 4).contains(name.toLowerCase())
                    || temp.contains(name.toLowerCase()) || temp.equals(name.toLowerCase())) {
                result = instances.get(key);
                break;
            }
        }
        
        return result;
        
    }
    
    /**
     * Adds parameters into the editor.
     * @param param A list of parameters to add.
     * @param list The actual list-object of the editor.
     */
    public void addParameter(List<Parameter> param, List<IDecisionVariable> list) {
        
        if (!list.isEmpty()) {
         
            IDecisionVariable base = list.get(0);
            Configuration config = base.getConfiguration();
            Project project = config.getProject(); 
            ContainerVariable container = null;   
            List<IDecisionVariable> found = null;         
            found = getIDecisionVariable(base, "parameters");
            IDecisionVariable var = found.get(0); 
            container = (ContainerVariable) var;
            
            for (Parameter p : param) {
                
                try {
                    
                    //if (container.getNestedElementsCount() == 0) {
                    container.addNestedElement(); 
                    //}
                    
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
                            
                            //"name", "class" and "defaultValue" must not be changed, they are the names of the columns!
                            Value val = ValueFactory.createValue(refinedType, 
                                    new Object[] {"name", new String(p.getName()), 
                                        "class", refValue, "defaultValue", p.getValue()});
                        
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
     * This Operation will is used to monitor the ManifestConnection and its progress.
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
     * Will actually use the ManifestConnection in order to retrieve and resolve the manifest information.
     * @param observer The used observer.
     */
    public void resolveManifest(ProgressObserver observer) {
        ManifestConnection con = new ManifestConnection();
        String className = null;
        String artifactId = null;
        String groupId = null;
        String version = null;   
        
        for (int i = 0; i < this.getVariable().getNestedElementsCount(); i++) {
            IDecisionVariable var = this.getVariable().getNestedElement(i);
            String dec = var.getValue() == null ? "" : var.getValue().toString();
            if (var.getDeclaration().getName().equals("artifact")) {
                try {
                    artifactId = dec.split(":")[1].trim();
                    groupId = dec.split(":")[0].trim();
                    version = dec.split(":")[2].trim();
                } catch (ArrayIndexOutOfBoundsException exc) {
                    //Some kind of report is needed!
                }
            } else if (var.getDeclaration().getName().equals("class")) {
                try {
                    String[] classArray = dec.split("/.");
                    className = classArray[classArray.length - 1].split(":")[0].trim();
                } catch (ArrayIndexOutOfBoundsException exc) {
                    //Some kind of report is needed!
                }
            } 
            
        }   
        con.load(observer, groupId, artifactId, version);
        
        List<Item> input = con.getInput(className, artifactId);
        List<Item> output = con.getOutput(className, artifactId);
        List<Parameter> param = con.getParameters(className, artifactId);
        
        try {
            IDecisionVariable var = getVariable();
            
            createMap(var);
            
            ModelQuery.findType(var.getConfiguration().getProject(), "Algorithm", null);
            var.unfreeze(AssignmentState.ASSIGNED);
           
            List<IDecisionVariable> list = getIDecisionVariable(var, "input");
            List<IDecisionVariable> list2 = getIDecisionVariable(var, "output");
            List<IDecisionVariable> list3 = getIDecisionVariable(var, "parameters");
            
            list2.add(0, (IDecisionVariable) list2.get(0).getParent());
            list3.add(0, (IDecisionVariable) list3.get(0).getParent());

            clearInput(list);
            clearInput(list2);
            clearInput(list3);
            
            list2.remove(0);
            
            for (Item i : output) {
                System.out.println(">> " + i.getName());
            }
            
            addInOut(input, list, true);
            addInOut(output, list2, false);
            addParameter(param, list3);
            System.out.println("Updated INPUT...");
            var.freeze(AllFreezeSelector.INSTANCE);
        } catch (ModelQueryException e) {
            System.out.println("FAILED!");
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
     * Is called if the upload button is pressed.
     * 
     * @param parent the parent composite
     */
    private void onUpload(Composite parent) {
        
        createProgressDialog(parent);
        
    }

    /**
     * Create progress Dialog.
     * 
     * @param parent
     *            parent composite.
     */
    private void createProgressDialog(Composite parent) {
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
            for (int i = 0; i < this.getEditorCount(); i++) {
                if (this.getEditor(i) instanceof ParameterEditor) {
                    ((ParameterEditor) this.getEditor(i)).refresh();
                } else if (this.getEditor(i) instanceof TuplesEditor) {
                    ((TuplesEditor) this.getEditor(i)).refresh();
                }
            }
            parent.redraw();
            parent.update();
            
        } catch (final InvocationTargetException e) {
            MessageDialog.openError(parent.getShell(), "Error", "Error: ");
            e.printStackTrace();
        } catch (final InterruptedException e) {
            MessageDialog.openInformation(parent.getShell(), "Cancelled",
                    "Error: ");
            e.printStackTrace();
        }
        
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
                IDecisionVariable variable = getConfiguration().getDecision(declarations.get(i));
                instances.put(variable.getNestedElement(1).getValue().getValue().toString(), variable.getDeclaration());
            }
        } catch (ModelQueryException e) {
            e.printStackTrace();
        }   
        
    }
    
    /**
     * Returns whether the tuple key part entry shall be disabled on this editor.
     * 
     * @return <code>true</code> if the key part shall be disabled, <code>false</code> else
     */
    protected abstract boolean disableKeyPart();

    @Override
    protected Map<UIParameter, Object> getUiParameter() {
        Map<UIParameter, Object> result;
        if (disableKeyPart()) {
            result = new HashMap<UIParameter, Object>();
            result.put(TuplesEditor.SHOW_KEY_PART, false);
        } else {
            result = null;
        }
        return result;
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
    
    /**
     * Prints an IDecisionVariable-Tree to the console.
     * @param base The base to start from.
     * @param indent The current indent for formation.
     */
    @SuppressWarnings("unused")
    private void printTree(IDecisionVariable base, int indent) {
        
        String output = "";
        for (int i = 0; i < indent; i++) {
            output += "    ";
        }
        System.out.println(output + base.getConfiguration().getName() + " === " + base.getValue());
        
        if (base instanceof Reference) {
            System.out.println("$$$$ " + base + " " + base.getValue());
        }
        
        for (int i = 0; i < base.getNestedElementsCount(); i++) {
            printTree(base.getNestedElement(i), indent + 1);
        }
        
    }

}
