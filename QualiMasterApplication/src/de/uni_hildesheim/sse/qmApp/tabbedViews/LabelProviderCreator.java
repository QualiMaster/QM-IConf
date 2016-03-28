package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import net.ssehub.easy.varModel.confModel.Configuration;
import net.ssehub.easy.varModel.confModel.DisplayNameProvider;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.datatypes.AnyType;
import net.ssehub.easy.varModel.model.datatypes.BooleanType;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.datatypes.ConstraintType;
import net.ssehub.easy.varModel.model.datatypes.DerivedDatatype;
import net.ssehub.easy.varModel.model.datatypes.Enum;
import net.ssehub.easy.varModel.model.datatypes.IDatatype;
import net.ssehub.easy.varModel.model.datatypes.IDatatypeVisitor;
import net.ssehub.easy.varModel.model.datatypes.IntegerType;
import net.ssehub.easy.varModel.model.datatypes.MetaType;
import net.ssehub.easy.varModel.model.datatypes.OrderedEnum;
import net.ssehub.easy.varModel.model.datatypes.RealType;
import net.ssehub.easy.varModel.model.datatypes.Reference;
import net.ssehub.easy.varModel.model.datatypes.Sequence;
import net.ssehub.easy.varModel.model.datatypes.Set;
import net.ssehub.easy.varModel.model.datatypes.StringType;
import net.ssehub.easy.varModel.model.datatypes.VersionType;
import net.ssehub.easy.varModel.model.filter.ReferenceValuesFinder;

/**
 * Implements a label provider creator based on IVML datatypes.
 * 
 * @author Holger Eichelberger
 */
class LabelProviderCreator implements IDatatypeVisitor {

    private IModelPart modelPart;
    private ILabelProvider result;
    private AbstractVariable variable;
    private Object value;
    private IFallbackImageProvider imageProvider;
    
    /**
     * Creates an instance and bind it to a given model part.
     * 
     * @param modelPart the underlying model part
     */
    LabelProviderCreator(IModelPart modelPart) {
        this.modelPart = modelPart;
    }

    /**
     * Binds the mapper to the input <code>value</code>.
     * 
     * @param value the input value to be mapped
     * @param variable the variable related to the value and the editor being mapped
     * @param imageProvider an image provider for default images for the label to be created
     */
    public void bind(Object value, AbstractVariable variable, IFallbackImageProvider imageProvider) {
        this.value = value;
        this.variable = variable;
        this.imageProvider = imageProvider;
    }

    /**
     * Clears this creator for reuse.
     */
    public void clear() {
        value = null;
        variable = null;
        result = null;
    }
    
    /**
     * Returns the created label provider.
     * 
     * @return the created label provider, may be <b>null</b> if the default one shall be used
     */
    public ILabelProvider getResult() {
        return result;
    }

    /**
     * Returns the name for the IVML null value based on the given (current) string label value.
     * 
     * @param label the current label
     * @return the label for null, i.e., either the one defined by the {@link DisplayNameProvider} 
     *   or <code>label</code>
     */
    private String getNullName(String label) {
        String result;
        DisplayNameProvider provider = DisplayNameProvider.getInstance();
        if (provider.enableNullValueInConfiguration(variable)) {
            result = provider.getNullName(variable);
        } else {
            result = label;
        }
        return result;
    }
    
    /**
     * Creates the result label provider.
     * 
     * @param labelText the label text
     * @param image the label image (may be <b>null</b>, then the 
     * {@link #imageProvider fallback image provider} is taken into account)
     */
    private void createResult(String labelText, Image image) {
        if (null == image) {
            image = imageProvider.getImage();
        }
        result = new StaticLabelProvider(labelText, image);
    }
    
    @Override
    public void visitReference(Reference reference) {
        String labelText = "";
        if (value instanceof Integer) {
            int index = (Integer) value;
            Configuration cfg = modelPart.getConfiguration();
            List<AbstractVariable> possible = ReferenceValuesFinder.findPossibleValues(cfg.getProject(), reference);
            int count = possible.size();
            if (0 <= index && index < count) {
                AbstractVariable decl = possible.get(index);
                IDecisionVariable var = cfg.getDecision(decl);
                if (null != var) {
                    labelText = ModelAccess.getDisplayName(var);
                } else {
                    labelText = decl.getName();
                }
            } else if (index >= count) {
                labelText = getNullName(labelText);
            }
        }
        createResult(labelText, null);
    }

    @Override
    public void visitEnumType(Enum enumType) {
        String labelText = "";
        if (value instanceof Integer) {
            int index = (Integer) value;
            int count = enumType.getLiteralCount();
            if (0 <= index && index < count) {
                labelText = enumType.getLiteral(index).getName();
            } else if (index >= count) {
                labelText = getNullName(labelText);
            }
        }
        createResult(labelText, null);
    }
    
    // delegated

    @Override
    public void visitDerivedType(DerivedDatatype datatype) {
        datatype.getBasisType().accept(this);
    }
    
    @Override
    public void visitOrderedEnumType(OrderedEnum enumType) {
        visitEnumType(enumType);
    }
    
    // direct mapping
    
    @Override
    public void visitBooleanType(BooleanType type) {
        if (value instanceof Integer) {
            int index = (Integer) value;
            if (0 == index) {
                createResult("true", null); // EASY-Editor convention :|
            } else {
                createResult("false", null); // EASY-Editor convention :|
            }
        }
    }

    @Override
    public void visitStringType(StringType type) {
        // nothing to do - direct mapping
    }

    @Override
    public void visitRealType(RealType type) {
        // nothing to do - direct mapping
    }

    @Override
    public void visitIntegerType(IntegerType type) {
        // nothing to do - direct mapping
    }

    // currently not relevant
    
    @Override
    public void visitConstraintType(ConstraintType type) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitCompoundType(Compound compound) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitDatatype(IDatatype datatype) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitAnyType(AnyType datatype) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitMetaType(MetaType datatype) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitSet(Set set) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitSequence(Sequence sequence) {
        // nothing to do for now - not relevant to the model
    }

    @Override
    public void visitVersionType(VersionType type) {
        // nothing to do for now - not relevant to the model
    }

}
