package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import de.uni_hildesheim.sse.easy.ui.productline_editor.ConfigurationTableEditorFactory.UIParameter;
import de.uni_hildesheim.sse.model.confModel.AllFreezeSelector;
import de.uni_hildesheim.sse.model.confModel.AssignmentState;
import de.uni_hildesheim.sse.model.confModel.ConfigurationException;
import de.uni_hildesheim.sse.model.confModel.ContainerVariable;
import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.ModelQuery;
import de.uni_hildesheim.sse.model.varModel.ModelQueryException;
//import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.model.varModel.datatypes.StringType;
import de.uni_hildesheim.sse.model.varModel.values.ValueDoesNotMatchTypeException;
import de.uni_hildesheim.sse.model.varModel.values.ValueFactory;
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
     */
    private void addInput(List<Item> input, List<IDecisionVariable> list) {
        
        if (!list.isEmpty()) {
            
            IDecisionVariable base = list.get(0);
            ContainerVariable container = null;
            for (int i = 0; i < input.size(); i++) {
                
                List<IDecisionVariable> found = null;

                found = getIDecisionVariable(base, "fields");

                
//                for (IDecisionVariable var : found) {
                IDecisionVariable var = found.get(0);    
                
                container = (ContainerVariable) var.getParent().getParent();
                
                for (Item item : input) {
                    //container.addNestedElement();
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
                            
                            container.getNestedElement(container.getNestedElementsCount() - 1)
                                .getNestedElement(1).getNestedElement(count).getNestedElement(0)
                                .setValue(ValueFactory.createValue(StringType.TYPE, f.getName()), 
                                        AssignmentState.ASSIGNED);
                            
                            String typeName = f.getFieldType().toString().toLowerCase();
                            typeName = Character.toUpperCase(typeName.charAt(0)) 
                                    + typeName.substring(1);
                            count++;
                        }
                        
                        System.out.println(container.getNestedElement(1).getValue());
                        System.out.println("---");
                        System.out.println(container.getNestedElement(0).getValue());
//                        container.removeNestedElement(container.getNestedElement(0));
                    } catch (ConfigurationException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (ValueDoesNotMatchTypeException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
                    
//                }
                
            }
        }  
        
    }
    
    /**
     * Adds parameters into the editor.
     * @param param A list of parameters to add.
     * @param list The actual list-object of the editor.
     */
    public void addParameter(List<Parameter> param, List<IDecisionVariable> list) {
        
        if (!list.isEmpty()) {
         
            IDecisionVariable base = list.get(0);
            
            ContainerVariable container = null;
                
            List<IDecisionVariable> found = null;

            found = getIDecisionVariable(base, "parameters");

            IDecisionVariable var = found.get(0);    
            
            container = (ContainerVariable) var;
            
            for (Parameter p : param) {

                container.addNestedElement();
                
                try {
                    
                    container.getNestedElement(container.getNestedElementsCount() - 1)
                        .getNestedElement(0).setValue(ValueFactory.createValue(
                        StringType.TYPE, new String(p.getName())), AssignmentState.ASSIGNED);
                    
                    container.addNestedElement();
                    
                    container.getNestedElement(container.getNestedElementsCount() - 1)
                        .getNestedElement(0).setValue(ValueFactory.createValue(
                            StringType.TYPE, new String(p.getType().name())), AssignmentState.ASSIGNED);
                    
//                    container.getNestedElement(container.getNestedElementsCount() - 1)
//                        .getNestedElement(1).setValue(ValueFactory.createValue(
//                            StringType.TYPE, new String(p.getType().name())), AssignmentState.ASSIGNED);
                    
                } catch (ConfigurationException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ValueDoesNotMatchTypeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            
        }
        
    }
    
    /**
     * Removes all input information for the artifact.
     * @param list The base for model operations.
     */
    private void clearInput(List<IDecisionVariable> list) {
        
        if (!list.isEmpty()) {
        
            IDecisionVariable base = list.get(0);
            for (int i = 0; i < base.getNestedElementsCount(); i++) {
                clearInput(base.getNestedElement(i), false);
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
     * Is called if the upload button is pressed.
     * 
     * @param parent the parent composite
     */
    private void onUpload(Composite parent) {
        ManifestConnection con = new ManifestConnection();
        String className = null;
        String artifactId = null;
        String groupId = null;
        String version = null;   
        System.out.println("Testing...");
        
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
        con.load(groupId, artifactId, version);
        List<Item> input = con.getInput(className, artifactId);
        List<Item> output = con.getOutput(className, artifactId);
        List<Parameter> param = con.getParameters(className, artifactId);
        
        try {
            IDecisionVariable var = getVariable();
            /*IDatatype type =*/ ModelQuery.findType(var.getConfiguration().getProject(), "Algorithm", null);
            var.unfreeze(AssignmentState.ASSIGNED);
            //List<IDecisionVariable> subList;
            List<IDecisionVariable> list = getIDecisionVariable(var, "input");
            List<IDecisionVariable> list2 = getIDecisionVariable(var, "output");
            List<IDecisionVariable> list3 = getIDecisionVariable(var, "parameters");

//            param.add(new Parameter("Test", ParameterType.STRING));
//            param.add(new Parameter("Test_2", ParameterType.STRING));
//            clearInput(getIDecisionVariable(list.get(0), "fields").get(0));
//            clearInput(getIDecisionVariable(list2.get(0), "fields").get(0));
//            printTree(var, 0);
            clearInput(list);
            clearInput(list2);
            clearInput(list3);
            addInput(input, list);
            addInput(output, list2);
            addParameter(param, list3);
            System.out.println("Updated INPUT...");
            //list = getIDecisionVariable(var, "output");
            //updateInput(output, list.get(0));
            //printTree(var, 0);
            var.freeze(AllFreezeSelector.INSTANCE);
        } catch (ModelQueryException e) {
            System.out.println("FAILED!");
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
    protected void printTree(IDecisionVariable base, int indent) {
        
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
