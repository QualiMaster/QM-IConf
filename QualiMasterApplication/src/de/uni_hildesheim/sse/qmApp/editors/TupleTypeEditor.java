package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;

/**
 * CellEditor creator for the selection of tuple types inside the topological pipeline editor.
 * @author El-Sharkawy
 *
 */
public class TupleTypeEditor extends AbstractChangeableDropBoxCellEditorCreator {

    public static final IEditorCreator CREATOR = new TupleTypeEditor();
    
    /**
     * Tuple of (Pipeline element type, Slot name for the algorithm).
     */
    private static final Map<String, String> SLOTNAMES = new HashMap<String, String>();
    
    static {
        SLOTNAMES.put("Source", "source");
        SLOTNAMES.put("Family", "family");
        SLOTNAMES.put("Data", "dataManagement");
    }
    
    @Override
    protected List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree) {
        List<ConstraintSyntaxTree> cstValues = null;
        Configuration config = ModelAccess.getConfiguration(VariabilityModel.Configuration.PIPELINES);
        
        // Extract selected source from property editor
        String sourceType = null;
        String sourceName = null;
        for (int i = 0, end = propertiesTree.getItems().length; i < end && null == sourceName; i++) {
            TreeItem tmpItem = propertiesTree.getItems()[i];
            if (tmpItem.getText().contains("Source")) {
                sourceName = tmpItem.getText(1);
                if (sourceName != null && !sourceName.isEmpty()) {
                    String[] tmpArray = sourceName.split(" ");
                    sourceType = tmpArray[0];
                    sourceName = tmpArray[tmpArray.length - 1];
                }
            }
        }
        
        // Try to get declaration for the source element
        AbstractVariable sourceDecl = null;
        if (null != sourceName && !sourceName.isEmpty() && null != sourceType && !sourceType.isEmpty()) {
            Iterator<IDecisionVariable> varItr = config.iterator();
            boolean found = false;
            while (varItr.hasNext() && !found) {
                IDecisionVariable tmpVar = varItr.next();
                IDecisionVariable nameSlot = tmpVar.getNestedElement("name");
                if (null != nameSlot && sourceName.equals(nameSlot.getValue().getValue())) {
                    
                    // Convert all member references to cst values
                    IDecisionVariable members = tmpVar.getNestedElement(SLOTNAMES.get(sourceType));
                    if (null != members && null != members.getValue()) {
                        found = true;
                        sourceDecl = (AbstractVariable) members.getValue().getValue();
                    }
                }
            }
        }
        
        // Read tuple input/output types from source
        if (null != sourceDecl) {
            cstValues = buildCSTValues(sourceType, sourceDecl);
        }
        
        return cstValues;
    }

    /**
     * Builds the filtered CST values for a tuple type reference.
     * @param sourceType Determination which kind of PipelineElement is the source for this flow, should be one of
     * Source, Family, or Data.
     * @param sourceDecl The declaration of the source element.
     * @return Relevant values as {@link ConstraintSyntaxTree}s or <tt>null</tt> if it could not be created.
     */
    private List<ConstraintSyntaxTree> buildCSTValues(String sourceType, AbstractVariable sourceDecl) {
        
        List<ConstraintSyntaxTree> cstValues = null;
        Configuration config = ModelAccess.getConfiguration(VariabilityModel.Configuration.PIPELINES);
                
        ConstraintSyntaxTree basis = new Variable(sourceDecl);
        IDecisionVariable sourceVar = config.getDecision(sourceDecl);
        IDecisionVariable tupleTypeSourceVar = null;
        if ("Source".equals(sourceType)) {
            basis = new CompoundAccess(basis, "input");
            tupleTypeSourceVar = sourceVar.getNestedElement("input");
        } else if ("Family".equals(sourceType)) {
            basis = new CompoundAccess(basis, "output");
            tupleTypeSourceVar = sourceVar.getNestedElement("output");
        }
        // Else abort: currently filtering is not possible for DataManagement
        
        if (null != tupleTypeSourceVar && null != tupleTypeSourceVar.getValue()
            && tupleTypeSourceVar.getValue() instanceof ContainerValue) {
            
            ContainerValue conValue = (ContainerValue) tupleTypeSourceVar.getValue();
            cstValues = new ArrayList<ConstraintSyntaxTree>();
            
            for (int i = 0, end = conValue.getElementSize(); i < end; i++) {
                try {
                    Value indexValue = ValueFactory.createValue(IntegerType.TYPE, i);
                    ConstantValue indexCst = new ConstantValue(indexValue);
                    ConstraintSyntaxTree cst = new OCLFeatureCall(basis, OclKeyWords.INDEX_ACCESS, indexCst);
                    cst.inferDatatype();
                    cstValues.add(cst);
                } catch (ValueDoesNotMatchTypeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (CSTSemanticException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return cstValues;
    }
}
