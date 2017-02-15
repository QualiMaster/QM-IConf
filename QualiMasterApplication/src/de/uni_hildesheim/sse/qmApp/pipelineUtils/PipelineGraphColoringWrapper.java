/*
 * Copyright 2009-2016 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.pipelineUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;

import eu.qualimaster.easy.extension.QmConstants;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.model.AbstractVariable;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;

/**
 * Wrapper which wraps up info about a pipelines element, its type, variable,
 * declaration, color and observable which should be drawn.
 * 
 * @author Niko Nowatzki
 */
public class PipelineGraphColoringWrapper implements Serializable {
    
    private static final long serialVersionUID = 998164785967639136L;
    private String elemName;
    private PipelineNodeType type;
    private IDecisionVariable var;
    private Color color;
    private String obs;
    private AbstractVariable decl;
    private List<PipelineGraphColoringWrapper> descendants = new ArrayList<PipelineGraphColoringWrapper>();
    private String pipelineParent;

    /**
     * Create a new Wrapper which wraps up info about wanted coloring.
     * @param elemName The user configured name of the pipeline element
     * @param type the elements type.
     * @param pipelineParent the elements parent. In the tree this is the Pipeline.
     * @param var the elements variable.
     * @param decl the elements declaration.
     */
    public PipelineGraphColoringWrapper(String elemName, PipelineNodeType type,
            String pipelineParent, IDecisionVariable var, AbstractVariable decl) {
        this.elemName = elemName;
        this.type = type;
        this.pipelineParent = pipelineParent;
        this.var = var;
        this.decl = decl;
    }
    
    /**
     * Create a new Wrapper which wraps up info about wanted coloring.
     * @param var the pipeline elements variable.
     * @param type the elements type.
     * @param pipelineParent the elements parent. In the tree this is the Pipeline.
     */
    public PipelineGraphColoringWrapper(IDecisionVariable var, PipelineNodeType type, String pipelineParent) {
        this(var.getNestedElement(QmConstants.SLOT_NAME).getValue().getValue().toString(), type, pipelineParent,
            var, var.getDeclaration());
    }
    /**
     * Create a new Wrapper which wraps up info about watned coloring.
     * @param elemName the elements name.
     * @param type the elements type.
     * @param pipelineParent the elements parent. In the tree this is the Pipeline.
     * @param var the elements variable.
     */
    public PipelineGraphColoringWrapper(String elemName, PipelineNodeType type,
            String pipelineParent, IDecisionVariable var) {
        this.elemName = elemName;
        this.type = type;
        this.pipelineParent = pipelineParent;
        this.var = var;
    }

    /**
     * Returns the number of descendants.
     * 
     * @return the number of descendants
     */
    public int getDescendantsCount() {
        return descendants.size();
    }
    
    /**
     * Set the color of a pipeline wrapper.
     * @param color Color to set for the wrapper which stands for a pipelines.
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Set the observable of a pipeline wrapper.
     * @param obs Observable to set for the wrapper which stands for a pipelines. 
     */
    public void setObservable(String obs) {
        this.setObs(obs);
    }
    
    /**
     * Set the observables for this wrapper.
     * @param obs observable zo set.
     */
    private void setObs(String obs) {
        this.obs = obs;
    }
    
    /**
     * Set the variable of a pipeline wrapper.
     * @param desVar Variable to set for the wrapper which stands for a pipelines. 
     */
    public void setVar(IDecisionVariable desVar) {
        this.var = desVar;
    }
    
    /**
     * Set the declaration of a pipeline wrapper.
     * @param decl Declaration to set for the wrapper which stands for a pipelines. 
     */
    public void setDeclaration(DecisionVariableDeclaration decl) {
        this.setDecl(decl);
    }
    
    /**
     * Set the declaration fot this wrapper.
     * @param decl declaration to set.
     */
    private void setDecl(DecisionVariableDeclaration decl) {
        this.decl = decl;
    }
    
    /**
     * Add a treeElement to this wrapper.
     * @param newElem new element to add.
     */
    public void addTreeElement(PipelineGraphColoringWrapper newElem) {
        boolean contained = false;
        for (int i = 0; i < this.descendants.size(); i++) {
            PipelineGraphColoringWrapper wrapper = this.descendants.get(i);
            if (wrapper.getElemName().trim().equals(newElem.getElemName().trim())) {
                contained = true;
            }
        }
        if (!contained) {
            descendants.add(newElem);
        }
    }

    /**
     * Method which checks whether a certain element is already contained within this wrapper.
     * @param newElem element which can be added.
     * @return true if newElem is not contained. Therefore it can be added. False if already contained.
     */
    public boolean alreadyContains(PipelineGraphColoringWrapper newElem) {
        boolean contains = false;
        for (int i = 0; i < this.descendants.size(); i++) {
            PipelineGraphColoringWrapper in = this.descendants.get(i);
            String inName = in.getElemName().replaceAll("\\s", "");
            String attendName = newElem.getElemName().replaceAll("\\s", "");
            if (inName.equals(attendName)) {
                contains = true;
            }
        }
        return contains;
    }
    
    /**
     * Get the descendants of this wrapper.
     * 
     * @return descendants Alle children of this wrapper object.
     */
    public Object[] getDecsendants() {
        return descendants.toArray();
    }
    
    /**
     * Get the name of the wrapper.
     * @return elemName name of the wrapper,
     */
    public String getElemName() {
        return elemName;
    }
    
    /**
     * Get the type of the wrapper.
     * @return type the type of the wrapper.
     */
    public PipelineNodeType getType() {
        return type;
    }
    
    /**
     * Get the var of the wrapper.
     * @return var the variable of thw wrapper.
     */
    public IDecisionVariable getVar() {
        return var;
    }
    
    /**
     * Get the pipelineParent of the wrapper.
     * @return pipelineParent pipeline parent of the wrapper.
     */
    public String getPipelineParent() {
        return pipelineParent;
    }
    
    /**
     * Get the declaration of the wrapper.
     * @return declaration the wrappers declaration.
     */
    public AbstractVariable getDecl() {
        return decl;
    }
    
    /**
     * Get the observables for the wrapper.
     * @return obs the wrappers observable.
     */
    public String getObs() {
        return obs;
    }
    
    /**
     * Get the wrappers color.
     * @return color the wrappers color.
     */
    public Color getColor() {
        return color;
    }
    
}