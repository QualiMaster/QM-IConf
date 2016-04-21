/*
 * Copyright 2014-2016 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.model;

import java.util.HashMap;
import java.util.Map;

import pipeline.FamilyElement;
import pipeline.Sink;

/**
 * A "struct" for collection temporary states while converting one pipeline and all of its elements into IVML objects
 * by means of{@link PipelineTranslationOperations#translationFromEcoregraphToIVMLfile(org.eclipse.emf.common.util.URI,
 * java.util.List)}.
 * @author El-Sharkawy
 *
 */
class PipelineSaveContext {
    
    private Map<FamilyElement, String> familyNodesAndName = new HashMap<FamilyElement, String>();
    private Map<Sink, String> sinkNodesAndName = new HashMap<Sink, String>();
    
    /**
     * Adds a mapping for Family elements of (ECORE Source, name in IVML).
     * @param element The origin ECORE source.
     * @param ivmlName The name of the translated IVML object.
     */
    void addFamilyMapping(FamilyElement element, String ivmlName) {
        familyNodesAndName.put(element, ivmlName);
    }

    /**
     * Tests whether the given ECORE model element was already translated to IVML.
     * @param element The pipeline element to be translated.
     * @return <tt>true</tt> if it was already translated, <tt>false</tt> if not.
     */
    boolean hasFamilyMapping(FamilyElement element) {
        return familyNodesAndName.containsKey(element);
    }
    
    /**
     * Returns the name of the translated IVML object for an ECORE object of a Family element.
     * @param element The pipeline element to be translated.
     * @return The name of the translated IVML object, or <tt>null</tt> if it was not translated so far.
     */
    String getFamilyMapping(FamilyElement element) {
        return familyNodesAndName.get(element);
    }
    
    /**
     * The number of already translated family elements.
     * @return A number greater or equal to 0.
     */
    int getFamilyCount() {
        return familyNodesAndName.keySet().size();
    }

    /**
     * Adds a mapping for Sink elements of (ECORE Source, name in IVML).
     * @param element The origin ECORE source.
     * @param ivmlName The name of the translated IVML object.
     */
    void addSinkMapping(Sink element, String ivmlName) {
        sinkNodesAndName.put(element, ivmlName);
    }
    
    /**
     * Tests whether the given ECORE model element was already translated to IVML.
     * @param element The pipeline element to be translated.
     * @return <tt>true</tt> if it was already translated, <tt>false</tt> if not.
     */
    boolean hasSinkMapping(Sink element) {
        return sinkNodesAndName.containsKey(element);
    }
    
    /**
     * Returns the name of the translated IVML object for an ECORE object of a Sink.
     * @param element The pipeline element to be translated.
     * @return The name of the translated IVML object, or <tt>null</tt> if it was not translated so far.
     */
    String getSinkMapping(Sink element) {
        return sinkNodesAndName.get(element);
    }
    
    /**
     * The number of already translated family elements.
     * @return A number greater or equal to 0.
     */
    int getSinkCount() {
        return sinkNodesAndName.keySet().size();
    }
    
}
