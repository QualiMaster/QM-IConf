/**
 * Implements additional views and view customizations, in particular
 * for tabbed views. The customization of tabbed views is a bit weird and not well documented. We need
 * to override the {@link de.uni_hildesheim.sse.qmApp.tabbedViews.IvmlPipelinePropertySection property section} 
 * in order to override the {@link de.uni_hildesheim.sse.qmApp.tabbedViews.IvmlPropertySource property source} 
 * to define a {@link de.uni_hildesheim.sse.qmApp.tabbedViews.IvmlPropertyDescriptor} property descriptor that
 * enables us to define the cell editor accordingly. Initially, the specific section is defined by the the generated 
 * diagram editor. However, Eclipse plugins just contribute to, but are not allowed to override existing 
 * extension points. Thus, we define a {@link de.uni_hildesheim.sse.qmApp.tabbedViews.TabDescriptorProvider} 
 * in the plugin.xml which applies to the specific contribution identifier of the generated editor and the two property
 * categories defined there, omits the original property section and returns our implementation as well as 
 * the descriptors of the other sections already defined in the generated editor.
 */
package de.uni_hildesheim.sse.qmApp.tabbedViews;