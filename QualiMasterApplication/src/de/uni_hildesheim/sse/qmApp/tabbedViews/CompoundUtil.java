/*
 * Copyright 2009-2017 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp.tabbedViews;

import net.ssehub.easy.varModel.model.datatypes.Compound;

/**
 * Compound helper methods.
 * @author Holger Eichelberger
 */
public class CompoundUtil {

    /**
     * Returns the refinement basis, i.e., the topmost refined compound.
     * 
     * @param cmp the compound to investigate
     * @return the topmost refined compound or <b>this</b> if this compound does not refine another compound
     */
    public static Compound getRefinementBasis(Compound cmp) {
        // for legacy reasons we just consider the first refined compound here
        Compound basis = cmp;
        while (basis.getRefinesCount() > 0) {
            basis = basis.getRefines(0);
        }
        return basis;
    }
    
}
