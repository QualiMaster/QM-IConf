package de.uni_hildesheim.sse.qmApp.model;

import java.util.HashMap;
import java.util.Map;

import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.DisplayNameProvider;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.IModelElement;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.EnumLiteral;

/**
 * Implements a specific display name provider in order to hide details
 * of the variability model.
 * 
 * @author Holger Eichelberger
 */
public class QualiMasterDisplayNameProvider extends DisplayNameProvider {

    public static final QualiMasterDisplayNameProvider INSTANCE = new QualiMasterDisplayNameProvider();
    private static final Map<IModelPart, String> MODEL_PART_NAMES = new HashMap<IModelPart, String>();

    /**
     * Prevents external creation [singleton].
     */
    private QualiMasterDisplayNameProvider() {
    }
    
    @Override
    public String getDisplayName(AbstractVariable variable) {
        String displayName = null;
        Configuration config = ModelAccess.getConfiguration(variable);
        if (null != config) {
            IDecisionVariable dec = config.getDecision(variable);
            if (null != dec) {
                displayName = ModelAccess.getDisplayName(dec);
            }
        }
        if (null == displayName) {
            displayName = ModelAccess.getDescription(variable);
            if (null == displayName) {
                displayName = variable.getName(); // fallback
            }
        }
        if (null == displayName || displayName.isEmpty()) {
            displayName = variable.getUniqueName(); // fallback
        }
        return displayName;
    }
    
    @Override
    public String getParentNames(AbstractVariable variable) {
        String result = "";
        IModelElement parent = variable.getParent();
        while (null != parent) {
            IModelPart part = null;
            String parentName = parent.getName();
            for (VariabilityModel.Configuration tmp : VariabilityModel.Configuration.values()) {
                if (tmp.getModelName().equals(parentName)) {
                    part = tmp;
                    break;
                }
                if (tmp.getDefinition().getModelName().equals(parentName)) {
                    part = tmp.getDefinition();
                    break;
                }
            }
            String tmp = null;
            if (null != part) {
                tmp = MODEL_PART_NAMES.get(part);
            }
            if (null == tmp) {
                tmp = ModelAccess.getDescription(parent);
                if (null == tmp) {
                    tmp = parent.getName();
                }
            }
            if (null != tmp) { // descending iteration
                if (result.length() > 0) {
                    result = "/" + result;
                }
                result = tmp + result;
            }
            parent = parent.getParent();
        }
        return result;
    }

    @Override
    public String getDisplayName(EnumLiteral literal) {
        String result = literal.getName();
        IModelElement parent = literal.getParent();
        if ("FieldType".equals(parent.getName()) && parent instanceof Enum) {
            switch (result) {
            case "INTEGER":
                result = "Integer";
                break;
            case "STRING":
                result = "String";
                break;
            case "BOOLEAN":
                result = "Boolean";
                break;
            case "REAL":
                result = "Real";
                break;
            default:
                break;
            }
        }
        return literal.getName();
    }

    /**
     * Returns whether this variable represents the Algorithm-Hardware-Link.
     * 
     * @param variable the variable to be considered (may be <b>null</b>)
     * @return <code>true</code> if it is the Algorithm-Hardware-Link, <code>false</code> else
     */
    private boolean isAlgorithmHwLink(AbstractVariable variable) {
        // this is preliminary, may be moved to an attribute "nullable"
        return null != variable && "Algorithms::Algorithm::hwType".equals(variable.getQualifiedName());
    }
    
    @Override
    public String getNullName(AbstractVariable variable) {
        String name;
        if (isAlgorithmHwLink(variable)) {
            name = "Software-based";
        } else {
            name = "not configured";
        }
        return name; // preliminary
    }

    @Override
    public boolean enableNullValueInConfiguration(AbstractVariable variable) {
        return isAlgorithmHwLink(variable);
    }

    /**
     * Returns the display name of the given model part.
     * 
     * @param part the model part
     * @return the display name
     */
    public String getModelPartDisplayName(IModelPart part) {
        String result = MODEL_PART_NAMES.get(part);
        if (null == result) {
            result = part.getModelName();
        }
        return result;
    }
    
    /**
     * Registers the given display name for <code>part</code>.
     * 
     * @param part the model part to receive the display name
     * @param displayName the display name
     */
    public void registerModelPartDisplayName(IModelPart part, String displayName) {
        MODEL_PART_NAMES.put(part, displayName);
        if (part.getDefinition() != part) {
            MODEL_PART_NAMES.put(part.getDefinition(), displayName);
        }
    }
}
