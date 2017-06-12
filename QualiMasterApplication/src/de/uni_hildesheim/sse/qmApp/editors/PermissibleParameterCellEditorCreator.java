package de.uni_hildesheim.sse.qmApp.editors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.CSTSemanticException;
import net.ssehub.easy.varModel.cst.CompoundAccess;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.OclKeyWords;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.Value;
import net.ssehub.easy.varModel.model.values.ValueDoesNotMatchTypeException;
import net.ssehub.easy.varModel.model.values.ValueFactory;
import pipeline.FamilyElement;
import pipeline.PipelineElement;
import pipeline.Sink;
import pipeline.Source;
import qualimasterapplication.Activator;

/**
 * CellEditorCreator selecting permissible parameters of a <tt>Source</tt>, <tt>Sink</tt>, or <tt>FamilyElement</tt>.
 * @author El-Sharkawy
 *
 */
public class PermissibleParameterCellEditorCreator extends AbstractChangeableConstraintCellEditorCreator {

    public static final IEditorCreator CREATOR = new PermissibleParameterCellEditorCreator();
    
    /**
     * {@inheritDoc}..<br/>
     * This method is structured as follows:
     * <ol>
     *   <li>Selection of the relevant {@link IDecisionVariable} depending on its type in:
     *       {@link #getReferencedVariable(int, Configuration, String)}</li>
     *   <li>Gathering of possible values in {@link #possibleValuesForFamilyElement(IDecisionVariable)}</li>
     * </ol>
     */
    @Override
    protected List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree) {
        List<ConstraintSyntaxTree> possibleValues = null;
        PipelineElement element = getPipelineElement();
        if (element instanceof FamilyElement) {
            IDecisionVariable var = getReferencedVariable(((FamilyElement) element).getFamily(),
                VariabilityModel.Configuration.FAMILIES, QmConstants.SLOT_FAMILYELEMENT_FAMILY);
            if (null != var) {
                possibleValues = possibleValues(var);
            }
        } else if (element instanceof Source) {
            IDecisionVariable var = getReferencedVariable(((Source) element).getSource(),
                VariabilityModel.Configuration.DATA_MANAGEMENT, QmConstants.SLOT_SOURCE_SOURCE);
            if (null != var) {
                possibleValues = possibleValues(var);
            }
        } else if (element instanceof Sink) {
            IDecisionVariable var = getReferencedVariable(((Sink) element).getSink(),
                VariabilityModel.Configuration.DATA_MANAGEMENT, QmConstants.SLOT_SINK_SINK);
            if (null != var) {
                possibleValues = possibleValues(var);
            }
        } else {
            Activator.getLogger(PermissibleParameterCellEditorCreator.class).warn("Unsupported pipeline element: "
                + element.getClass().getSimpleName());
        }
        
        return possibleValues;
    }
    
    /**
     * Locates the selected {@link IDecisionVariable}.
     * @param selectedIndex The index of the selected {@link IDecisionVariable} as returned by
     *     {@link Source#getSource()}, {@link Sink#getSink()}, or {@link FamilyElement#getFamily()}.
     * @param modelPart Either {@link VariabilityModel.Configuration#DATA_MANAGEMENT}
     *     or {@link VariabilityModel.Configuration#FAMILIES}
     * @param slotName One of QmConstants#SLOT_FAMILYELEMENT_FAMILY, {@link QmConstants#SLOT_SINK_SINK},
     *     or {@link QmConstants#SLOT_SOURCE_SOURCE}
     * @return The referenced {@link IDecisionVariable} or <tt>null</tt> if it could not be found.
     */
    private IDecisionVariable getReferencedVariable(int selectedIndex, Configuration modelPart, String slotName) {
        IDecisionVariable variable = null;
        
        IDecisionVariable parentVar = (IDecisionVariable) getVariable().getParent();
        Compound cmpType = (Compound) parentVar.getDeclaration().getType();
        IDatatype referencedType = Reference.dereference(cmpType.getElement(slotName).getType());
        IDecisionVariable referencedVariable = ModelAccess.getFromGlobalIndex(modelPart, referencedType, selectedIndex);
        
        if (null != referencedVariable) {
            ReferenceValue refValue = (ReferenceValue) referencedVariable.getValue();
            if (null != refValue && null != refValue.getValue()) {
                AbstractVariable orgDecl = refValue.getValue();
                variable = modelPart.getConfiguration().getDecision(orgDecl);
            }
        }
        
        return variable;
    }
    
    /**
     * Final method to create possible values.
     * @param orgVar The original variable from where the possible permissible parameters come from.
     * @return  Relevant values as {@link ConstraintSyntaxTree}s or <tt>null</tt> if it could not be created. Maybe
     *     empty if no permissible parameters are specified for the given variable.
     */
    private List<ConstraintSyntaxTree> possibleValues(IDecisionVariable orgVar) {
        List<ConstraintSyntaxTree> possibleValues = null;
        IDecisionVariable parameters = orgVar.getNestedElement(QmConstants.SLOT_PARAMETERS);
        
        if (null != parameters) {
            // Parameters exist -> Create list of (possible empty) values
            possibleValues = new ArrayList<ConstraintSyntaxTree>();

            if (null != parameters.getValue() && parameters.getValue() instanceof ContainerValue) {
                // Possible values exist -> fill the list
                ContainerValue value = (ContainerValue) parameters.getValue();
                ConstraintSyntaxTree basis = new CompoundAccess(new Variable(orgVar.getDeclaration()),
                    QmConstants.SLOT_PARAMETERS);
                
                for (int i = 0, end = value.getElementSize(); i < end; i++) {
                    try {
                        Value index = ValueFactory.createValue(IntegerType.TYPE, OclKeyWords.toIvmlIndex(i));
                        ConstantValue indexValue = new ConstantValue(index);
                        ConstraintSyntaxTree cstValue = new OCLFeatureCall(basis, OclKeyWords.INDEX_ACCESS, indexValue);
                        cstValue.inferDatatype();
                        possibleValues.add(cstValue);
                    } catch (ValueDoesNotMatchTypeException e) {
                        Activator.getLogger(PermissibleParameterCellEditorCreator.class).exception(e);
                    } catch (CSTSemanticException e) {
                        Activator.getLogger(PermissibleParameterCellEditorCreator.class).exception(e);
                    }
                }
            }
        }
        
        return possibleValues;
    }
    
    @Override
    protected CellEditor createCellEditor(Composite parent, String[] labels, List<ConstraintSyntaxTree> cstValues) {
        PipelineElement pipElement = getPipelineElement();
        return new PermissibleParameterDialogCellEditor(pipElement, parent, labels, cstValues);
    }
}
