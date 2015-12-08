package de.uni_hildesheim.sse.qmApp.model;

import java.util.List;

import de.uni_hildesheim.sse.model.confModel.Configuration;
import de.uni_hildesheim.sse.model.varModel.AbstractVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.treeView.IConfigurableElementFactory;

/**
 * Defines the interface for a model part.
 * 
 * @author Holger Eichelberger
 */
public interface IModelPart {
    
    /**
     * Defines the source mode, i.e., which source for possible variables
     * to use.
     * 
     * @author Holger Eichelberger
     */
    public enum SourceMode {
        
        PROVIDED_TYPES(true, false),
        VARIABLES(false, true);
        
        private boolean types;
        private boolean variables;

        /**
         * Creates a source mode constant.
         * 
         * @param types use types
         * @param variables use variables
         */
        private SourceMode(boolean types, boolean variables) {
            this.types = types;
            this.variables = variables;
        }
        
        /**
         * Returns whether provided types shall be used.
         * 
         * @return <code>true</code> if provided types shall be used, <code>false</code> else
         */
        public boolean useProvidedTypes() {
            return types;
        }

        /**
         * Returns whether top-level variables shall be used.
         * 
         * @return <code>true</code> if top-level variables shall be used, <code>false</code> else
         */
        public boolean useVariables() {
            return variables;
        }
    }
    
    /**
     * Returns the defining variability model part.
     * 
     * @return the variability model part in case of a configuration, the part itself
     *   in case of a defining part
     */
    public IModelPart getDefinition();
    
    /**
     * Returns the name of the underlying IVML project / project.
     * 
     * @return the name of the IVML model
     */
    public String getModelName();
    
    /**
     * Returns the IVML configuration of the model part.
     * 
     * @return the configuration
     */
    public Configuration getConfiguration();

    /**
     * Returns the names of the provided IVML types. This method may be needed during the initialization 
     * phase of the application if the types are not already loaded.
     * 
     * @return the names
     */
    public String[] getProvidedTypeNames();
    
    /**
     * Returns the top-level types provided by this model part.
     * 
     * @return the top-level types
     */
    public IDatatype[] getProvidedTypes();
    
    /**
     * Returns the top-level variables.
     * 
     * @return the top-level variables
     */
    public String[] getTopLevelVariables();
    
    /**
     * Returns the possible values indicated by the variables and the provided types.
     * 
     * @return the possible values
     */
    public List<AbstractVariable> getPossibleValues();

    /**
     * Returns the source mode.
     * 
     * @return the source mode
     */
    public SourceMode getSourceMode();
    
    /**
     * Returns the element factory.
     * 
     * @return the element factory
     */
    public IConfigurableElementFactory getElementFactory();
    
    /**
     * Returns whether a new instance shall immediately be added to its configuring container.
     * 
     * @return <code>true</code> if it shall be added immediately, <code>false</code> else
     */
    public boolean addOnCreation();
}
