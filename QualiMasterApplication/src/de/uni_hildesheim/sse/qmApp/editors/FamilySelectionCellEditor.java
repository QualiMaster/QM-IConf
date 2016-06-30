/*
 * Copyright 2016 University of Hildesheim, Software Systems Engineering
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Tree;

import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.producer.ui.productline_editor.ConfigurationTableEditorFactory.IEditorCreator;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.cst.ConstraintSyntaxTree;
import net.ssehub.easy.varModel.cst.Variable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;

/**
 * Cell editor for the property of a sub pipeline to configure the family, to which this pipeline belongs to.
 * @author El-Sharkawy
 *
 */
public class FamilySelectionCellEditor extends AbstractChangeableDropBoxCellEditorCreator {
    
    public static final IEditorCreator CREATOR = new FamilySelectionCellEditor();
    
    /**
     * Singleton constructor.
     */
    private FamilySelectionCellEditor() {}

    @Override
    protected List<ConstraintSyntaxTree> retrieveFilteredElements(Tree propertiesTree) {
        List<ConstraintSyntaxTree> possibleValues = null;
        Configuration familyConfig = ModelAccess.getConfiguration(VariabilityModel.Configuration.FAMILIES);
        Iterator<IDecisionVariable> varItr = familyConfig.iterator();
        IDecisionVariable familySetVar = null;
        
        while (varItr.hasNext() && null == familySetVar) {
            IDecisionVariable tmpVar = varItr.next();
            if (QmConstants.VAR_FAMILIES_FAMILIES.equals(tmpVar.getDeclaration().getName())) {
                familySetVar = tmpVar;
            }
        }
        
        if (null != familySetVar && null != familySetVar.getValue()
            && familySetVar.getValue() instanceof ContainerValue) {
            
            possibleValues = new ArrayList<ConstraintSyntaxTree>();
            ContainerValue value = (ContainerValue) familySetVar.getValue();
            for (int i = 0, end = value.getElementSize(); i < end; i++) {
                ReferenceValue refValue = (ReferenceValue) value.getElement(i);
                Object referredValue = null != refValue.getValue() ? refValue.getValue() : refValue.getValueEx();
                if (null != referredValue) {
                    if (referredValue instanceof AbstractVariable) {
                        possibleValues.add(new Variable((AbstractVariable) referredValue));                        
                    } else if (referredValue instanceof ConstraintSyntaxTree) {
                        possibleValues.add((ConstraintSyntaxTree) referredValue);                        
                    }
                }
            }
        }
        
        return possibleValues;
    }

}
