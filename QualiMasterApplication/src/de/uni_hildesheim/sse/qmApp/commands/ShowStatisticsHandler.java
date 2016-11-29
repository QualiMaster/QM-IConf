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
package de.uni_hildesheim.sse.qmApp.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.dialogs.StatisticsDialog;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import eu.qualimaster.easy.extension.modelop.ModelStatistics;
import eu.qualimaster.easy.extension.modelop.QMConfigStatisticsVisitor;
import net.ssehub.easy.varModel.confModel.Configuration;

/**
 * Collects and prints statistics of the QM (Meta-) Model.
 * @author El-Sharkawy
 *
 */
public class ShowStatisticsHandler extends AbstractConfigurableHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // Collect statistics
        Configuration config = VariabilityModel.Definition.TOP_LEVEL.getConfiguration();
        QMConfigStatisticsVisitor visitor = new QMConfigStatisticsVisitor();
        config.accept(visitor);
        ModelStatistics statistics = visitor.getStatistics();
        
        // Print statistics
        StatisticsDialog dialog = new StatisticsDialog(Dialogs.getDefaultShell(), statistics);
        dialog.open();
        
        return null;
    }

}
