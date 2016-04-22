package de.uni_hildesheim.sse.qmApp.tabbedViews;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * A factory for creating property cell editors.
 * 
 * @author Holger Eichelberger
 */
public class PropertyEditorFactory {
    
    private static final Map<Class<?>, IPropertyEditorCreator> CREATORS 
        = new HashMap<Class<?>, IPropertyEditorCreator>();

    /**
     * Prevents creation. Utility class.
     */
    private PropertyEditorFactory() {
    }
    
    /**
     * Registers the given creator. Existing creators for the same class they react on will be overwritten.
     * 
     * @param creator the creator to be registered ({@link IPropertyEditorCreator#reactsOn()} must not be <b>null</b>)
     */
    public static void registerCreator(IPropertyEditorCreator creator) {
        if (null != creator && null != creator.reactsOn()) {
            CREATORS.put(creator.reactsOn(), creator);
        }
    }

    /**
     * Unregisters the given <code>creator</code>.
     * 
     * @param creator the creator to be unregistered(may be <b>null</b> or unknown to this factory, but then nothing
     *   is unregistered)
     */
    public static void unregisterCreator(IPropertyEditorCreator creator) {
        if (null != creator && null != creator.reactsOn()) {
            Class<?> reactsOn = creator.reactsOn();
            if (CREATORS.get(reactsOn) == creator) {
                CREATORS.remove(reactsOn);
            }
        }
    }
    
    /**
     * Unregisters the creator for <code>dataClass</code>.
     * 
     * @param dataClass the class the creator shall be unregistered (may be <b>null</b> or unknown to this factory)
     */
    public static void unregisterCreatorFor(Class<?> dataClass) {
        if (null != dataClass) {
            CREATORS.remove(dataClass);
        }
    }

    /**
     * Returns whether this factory has a registered creator for <code>dataClass</code>.
     * 
     * @param dataClass the class to look for (may be <b>null</b> or unknown to this factory)
     * @return <code>true</code> if there is a registered creator, <code>false</code> else
     */
    public static boolean hasCreatorFor(Class<?> dataClass) {
        boolean result = false;
        if (null != dataClass) {
            result = CREATORS.containsKey(dataClass);
        }
        return result;
    }

    /**
     * Returns the cell editor for the given property.
     * 
     * @param composite the parent composite
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the cell editor shall be returned for 
     *   inside of <code>data</code>
     * @param fallback a fallback creator provided by the Eclipse framework (may be <b>null</b>)
     * @return the cell editor
     */
    public static CellEditor createPropertyEditor(Composite composite, Object data, String propertyIdentifier, 
        IFallbackEditorCreator fallback) {
        CellEditor result = null;
        if (null != data && null != propertyIdentifier) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                result = creator.createPropertyEditor(composite, data, propertyIdentifier, fallback);
            }
        }
        return result;
    }
    
    /**
     * Returns the display name for the given property.
     * 
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the display name shall be returned for 
     *   inside of <code>data</code>
     * @return the display name
     */
    public static String getDisplayName(Object data, String propertyIdentifier) {
        String result = null;
        if (null != data && null != propertyIdentifier) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                result = creator.getDisplayName(data, propertyIdentifier);
            }
        }
        return result;
    }

    /**
     * Returns the description for the given property.
     * 
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the display name shall be returned for 
     *   inside of <code>data</code>
     * @return the description
     */
    public static String getDescription(Object data, String propertyIdentifier) {
        String result = null;
        if (null != data && null != propertyIdentifier) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                result = creator.getDescription(data, propertyIdentifier);
            }
        }
        return result;
    }
    
    /**
     * Specifies whether the given <tt>propertyIdentifier</tt> shall be displayed at the GUI or not, i.e., whether
     * this property can be edited by the user or not.
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the cell editor shall be returned for 
     *   inside of <code>data</code>
     * @return <tt>true</tt> the editor shall be shown, <tt>false</tt> the editor should not be shown in the UI.
     */
    public static boolean isVisible(Object data, String propertyIdentifier) {
        boolean isVisible = true;
        if (null != data && null != propertyIdentifier) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                isVisible = creator.isVisible(data, propertyIdentifier);
            }
        }
        
        return isVisible;
    }
    
    /**
     * Specifies whether this kind or Pipeline element can be filtered at all.
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @return <tt>true</tt> {@link #isVisible(String)} should be considered for all elements, <tt>false</tt> no
     * filtering needed.
     */
    public static boolean isFilterable(Object data) {
        boolean isFilterable = false;
        if (null != data) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                isFilterable = creator.isFilterable();
            }
        }
        
        return isFilterable;
    }
    
    /**
     * Returns the label provider, i.e., the label describing the actual object value.
     * 
     * @param data the data object identifying the {@link IPropertyEditorCreator}
     * @param propertyIdentifier an identifier specifying the property the cell editor shall be returned for 
     *   inside of <code>data</code>
     * @param value the value to be set for <code>propertyIdentifier</code>
     * @param imageProvider an image provider for default images for the label to be created
     * @return the label provider or <b>null</b> if no label provider was created
     */
    public static ILabelProvider getLabelProvider(Object data, String propertyIdentifier, Object value, 
        IFallbackImageProvider imageProvider) {
        ILabelProvider result = null;
        if (null != data && null != propertyIdentifier) {
            IPropertyEditorCreator creator = CREATORS.get(data.getClass());
            if (null != creator) {
                result = creator.getLabelProvider(data, propertyIdentifier, value, imageProvider);
            }
        }
        return result;
    }
    
}
