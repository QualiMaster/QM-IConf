package de.uni_hildesheim.sse.qmApp.images;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import de.uni_hildesheim.sse.model.varModel.datatypes.Container;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.model.varModel.datatypes.Reference;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;

/**
 * Implements a generic image registry to store and provide the images and icons for 
 * this application.
 * 
 * @author Holger Eichelberger
 */
public class ImageRegistry {

    public static final ImageRegistry INSTANCE = new ImageRegistry();
    
    private Map<String, Image> images = new HashMap<String, Image>();

    /**
     * Prevents external creation.
     */
    private ImageRegistry() {
    }

    /**
     * Obtains a key for the model part.
     * 
     * @param modelPartName the namme of the model part to obtain the key for
     * @return the key for the model part
     */
    public static String obtainKey(String modelPartName) {
        return "IVML." + modelPartName;
    }
    
    /**
     * Obtains a key for the model part.
     * 
     * @param modelPart the model part to obtain the key for
     * @return the key for the model part
     */
    public static String obtainKey(IModelPart modelPart) {
        return obtainKey(modelPart.getModelName());
    }

    /**
     * Obtains a key for the provided datatype within model part.
     * 
     * @param modelPart the model part to obtain the key for
     * @param type the provided data type (may be <b>null</b>, ignored then)
     * @return the key for the model part
     */
    public static String obtainKey(IModelPart modelPart, IDatatype type) {
        return appendTypeKey(obtainKey(modelPart), type);
    }
    
    /**
     * Obtains a key for the provided datatype within model part.
     * 
     * @param modelPart the model part to obtain the key for
     * @param typeName the name of the data type (may be <b>null</b>, ignored then)
     * @return the key for the model part
     */
    public static String obtainKey(IModelPart modelPart, String typeName) {
        return appendTypeKey(obtainKey(modelPart), typeName);
    }
    
    /**
     * Obtains a key for the provided datatype within model part.
     * 
     * @param modelPartName the name of the model part to obtain the key for
     * @param type the provided data type (may be <b>null</b>, ignored then)
     * @return the key for the model part
     */
    public static String obtainKey(String modelPartName, IDatatype type) {
        return appendTypeKey(obtainKey(modelPartName), type);
    }
    
    /**
     * Appends the type (as key) to the given model part key.
     * 
     * @param modelPartKey the model part key
     * @param type the type (may be <b>null</b>, ignored then)
     * @return the combined key
     */
    private static String appendTypeKey(String modelPartKey, IDatatype type) {
        if (null != type) {
            type = Reference.dereference(type);
            while (Container.TYPE.isAssignableFrom(type) && type.getGenericTypeCount() > 0) {
                type = Reference.dereference(type.getGenericType(0));
            }
        }
        return appendTypeKey(modelPartKey, null != type ? type.getName() : null);
    }

    /**
     * Appends the type key to the given model part key.
     * 
     * @param modelPartKey the model part key
     * @param typeKey the type key (may be <b>null</b>, ignored then)
     * @return the combined key
     */
    private static String appendTypeKey(String modelPartKey, String typeKey) {
        String result = modelPartKey;
        if (null != typeKey) {
            result += "." + typeKey;            
        }
        return result;
    }

    /**
     * Returns the image for a given key.
     * 
     * @param key the key to return the image for
     * @return the image (may be <b>null</b> in order to use the platform default image)
     */
    public Image getImage(String key) {
        return images.get(key);
    }
    
    /**
     * Returns the image for a certain model part.
     * 
     * @param modelPart the model part to return the image for
     * @return the image (may be <b>null</b> in order to use the platform default image)
     */
    public Image getImage(IModelPart modelPart) {
        return getImage(obtainKey(modelPart));
    }

    /**
     * Returns the image for a certain model part and provided type.
     * 
     * @param modelPart the model part to return the image for
     * @param providedType the provided type (may be <b>null</b>, see then {@link #getImage(IModelPart)}.
     * @return the image (may be <b>null</b> in order to use the platform default image)
     */
    public Image getImage(IModelPart modelPart, IDatatype providedType) {
        return getImage(obtainKey(modelPart, providedType));
    }

    /**
     * Registers an image for a given key.
     * 
     * @param key the key
     * @param image the image
     */
    public void registerImage(String key, Image image) {
        images.put(key, image);
    }
    
    /**
     * Registers an image for a model part.
     * 
     * @param modelPart the model part
     * @param image the image
     */
    public void registerImage(IModelPart modelPart, Image image) {
        registerImage(obtainKey(modelPart), image);
    }

    /**
     * Registers an image for a model part and a given datatype.
     * 
     * @param modelPart the model part
     * @param providedType the the provided type from <code>modelPart</code>
     * @param image the image
     */
    public void registerImage(IModelPart modelPart, IDatatype providedType, Image image) {
        registerImage(obtainKey(modelPart, providedType), image);
    }
    
    /**
     * Registers an image for a model part and a given datatype.
     * 
     * @param modelPart the model part
     * @param providedType the the provided type from <code>modelPart</code>
     * @param image the image
     */
    public void registerImage(IModelPart modelPart, String providedType, Image image) {
        registerImage(obtainKey(modelPart, providedType), image);
    }
    
    /**
     * Registers an image for a model part and a given datatype.
     * 
     * @param modelPart the model part
     * @param providedTypeIndex the index of the provided type (via its index in {@link IModelPart#getProvidedTypes())})
     * @param image the image
     */
    public void registerImage(IModelPart modelPart, int providedTypeIndex, Image image) {
        //using getProvidedTypes would be more direct but may fail if the model is not present
        String imageKey = appendTypeKey(obtainKey(modelPart), modelPart.getProvidedTypeNames()[providedTypeIndex]);
        registerImage(imageKey, image);
    }
    
}
