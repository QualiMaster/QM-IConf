/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.editorInput;

import java.util.List;

import org.eclipse.ui.IEditorInput;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.datatypes.IDatatype;
import de.uni_hildesheim.sse.qmApp.model.IModelPart;
import de.uni_hildesheim.sse.qmApp.treeView.IMenuContributor;

/**
 * Allows deferring the creation of editor inputs. The background is that editor inputs shall be provided shortly
 * before opening an editor, in particular as early created editor inputs may contain model references that are
 * already outdated due to updates. Thus, implementing classes shall contain the way how to create an editor input
 * rather than just a model reference.
 * 
 * @author Holger Eichelberger
 */
public interface IEditorInputCreator {
    
    /**
     * Describes how elements of this input creator shall be cloned.
     * 
     * @author Holger Eichelberger
     */
    public enum CloneMode {
        SINGLE(1),
        MULTI(Integer.MAX_VALUE),
        NONE(0);
        
        private int maxCount;
        
        /**
         * Creates a clone mode "constant".
         * 
         * @param maxCount maximum allowed number of clones
         */
        private CloneMode(int maxCount) {
            this.maxCount = maxCount;
        }
        
        /**
         * Returns whether a given clone count is allowed.
         * 
         * @param count the clone count to check for
         * @return <code>true</code> if allowed, <code>false</code> else
         */
        public boolean countAllowed(int count) {
            return count >= 0 && count <= maxCount;
        }
        
        /**
         * Returns whether cloning in this mode is allowed at all.
         * 
         * @return <code>true</code> if cloning is allowed at all, <code>false</code> else
         */
        public boolean cloneAllowed() {
            return maxCount > 0;
        }
        
        /**
         * Returns whether an input of the number of clones is required.
         * 
         * @return <code>true</code> if an input is requested, <code>false</code> else
         */
        public boolean requiresCountInput() {
            return maxCount > 1;
        }
        
    }
    
    /**
     * Creates the actual editor input. Just to be called shortly before the receiving editor is being opened.  
     * 
     * @return the editor input
     */
    public IEditorInput create();

    /**
     * Returns the name of the editor input will provide.
     * 
     * @return the name
     */
    public String getName();
    
    /**
     * Returns the IMVL type this editor is representing.
     * 
     * @return the type (may be <b>null</b>)
     */
    public IDatatype getType();
    
    /**
     * Returns whether this creator will (probably) lead to opening an editor. This allows to disable menu items early.
     * 
     * @return <code>true</code> if this creator will probably lead to opening an editor, <code>false</code> else
     */
    public boolean isEnabled();
    
    /**
     * Creates the underlying artifacts if required.
     */
    public void createArtifacts();
    
    /**
     * Returns whether the editor input to be created is writable.
     * 
     * @return <code>true</code> if this element is writable, <code>false</code> else
     */
    public boolean isWritable();
    
    /**
     * Returns whether the underlying element / editor input to be created is (basically) deletable.
     * 
     * @return <code>true</code> if this element is deletable, <code>false</code> else
     */
    public boolean isDeletable();
    
    /**
     * Returns whether the underlying element / editor input to be created is cloneable.
     * 
     * @return the clone mode
     */
    public CloneMode isCloneable();

    /**
     * Returns whether the underlying element / editor input to be created is readable.
     * 
     * @return <code>true</code> if this element is readable, <code>false</code> else
     */
    public boolean isReadable();
    
    /**
     * Deletes the element behind the input. The caller must ensure
     * that the editor input is not used anymore. This method may issue change events.
     * 
     * @param source the event source (for sending change events)
     * @param modelPart the model part holding the variable and the variable container
     */
    public void delete(Object source, IModelPart modelPart);
    
    /**
     * Clones this editor input if possible.
     * 
     * @param cloneCount the number of clones to be created
     * @return the created clones (may be <b>empty</b> if cloning is not possible)
     */
    public List<IDecisionVariable> clone(int cloneCount);
    
    /**
     * Returns whether this editor input holds the given <code>variable</code> directly or indirectly.
     * 
     * @param variable the input object
     * @return <code>true</code> if this editor input holds the given <code>variable</code>, <code>false</code> else
     */
    public boolean holds(IDecisionVariable variable);

    /**
     * Returns whether the underlying element is referenced in the given <code>modelPart</code>.
     * 
     * @param modelPart the part to search in
     * @param defining the model part(s) which define top-level variables for the underlying element
     * @return <code>true</code> if the underlying element is referenced in <code>modelPart</code>, <code>false</code> 
     *   else
     */
    public boolean isReferencedIn(IModelPart modelPart, IModelPart... defining);
    
    /**
     * Returns an optional menu contributor.
     * 
     * @return the menu contributor or <b>null</b>
     */
    public IMenuContributor getMenuContributor();

    /**
     * Returns the actual variable.
     * 
     * @return the variable (may be <b>null</b>)
     */
    public IDecisionVariable getVariable();

}
