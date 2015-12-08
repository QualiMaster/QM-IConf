package de.uni_hildesheim.sse.qmApp.tabbedViews;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.properties.filters.ConnectionEditPartPropertySectionFilter;
import org.eclipse.gmf.runtime.diagram.ui.properties.filters.DiagramEditPartPropertySectionFilter;
import org.eclipse.gmf.runtime.diagram.ui.properties.filters.ShapeEditPartPropertySectionFilter;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.ConnectionAppearancePropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.DiagramColorsAndFontsPropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.appearance.ShapeColorsAndFontsPropertySection;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.grid.RulerGridPropertySection;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptor;
import org.eclipse.ui.views.properties.tabbed.ITabDescriptorProvider;

import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;

/**
 * Implements an (overriding) tab descriptor provider.
 * 
 * @author Holger Eichelberger
 */
public class TabDescriptorProvider implements ITabDescriptorProvider {
    
    static {
        VariabilityModel.initPropertyEditorCreators();
    }
    
    @Override
    public ITabDescriptor[] getTabDescriptors(IWorkbenchPart part, ISelection selection) {
        ITabDescriptor[] result;
        if (part instanceof pipeline.diagram.part.PipelineDiagramEditor) {
            result = new ITabDescriptor[3];
            
            String tabId = "property.tab.domain";
            SimpleSectionDescriptor sDesc = new SimpleSectionDescriptor("property.tab.AppearancePropertySection", 
                new IvmlPipelinePropertySection(), tabId, View.class, EditPart.class);
            result[0] = new SimpleTabDescriptor("domain", tabId, "Core", sDesc);

            // the remainder is taken from the generated editor. Alternative solution: read out from IExtensionManager
            tabId = "property.tab.AppearancePropertySection";
            SimpleSectionDescriptor sDesc1 = new SimpleSectionDescriptor(
                "property.section.ConnectorAppearancePropertySection", new ConnectionAppearancePropertySection(), 
                tabId, new ConnectionEditPartPropertySectionFilter());
            SimpleSectionDescriptor sDesc2 = new SimpleSectionDescriptor(
                "property.section.ShapeColorAndFontPropertySection", new ShapeColorsAndFontsPropertySection(), 
                tabId, new ShapeEditPartPropertySectionFilter());
            SimpleSectionDescriptor sDesc3 = new SimpleSectionDescriptor(
                "property.section.DiagramColorsAndFontsPropertySection", 
                new DiagramColorsAndFontsPropertySection(), tabId, new DiagramEditPartPropertySectionFilter());
            result[1] = new SimpleTabDescriptor("visual", tabId, "Appearance", sDesc1, sDesc2, sDesc3);

            tabId = "property.tab.DiagramPropertySection";
            sDesc = new SimpleSectionDescriptor("property.section.RulerGridPropertySection", 
                new RulerGridPropertySection(), tabId, new DiagramEditPartPropertySectionFilter());
            result[2] = new SimpleTabDescriptor("visual", tabId, "Rulers & Grid", sDesc);
        } else {
            result = new ITabDescriptor[0];
        }
        return result;
    }

}
