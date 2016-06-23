package pipeline.diagram.part;

import java.util.Collections;
import java.util.List;
import org.eclipse.gef.Tool;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeConnectionTool;
import org.eclipse.gmf.runtime.diagram.ui.tools.UnspecifiedTypeCreationTool;
import org.eclipse.gmf.runtime.emf.type.core.IElementType;
import pipeline.diagram.providers.PipelineElementTypes;

/**
 * @generated
 */
public class PipelinePaletteFactory {

	/**
	 * @generated
	 */
	public void fillPalette(PaletteRoot paletteRoot) {
		paletteRoot.add(createObjects1Group());
		paletteRoot.add(createConnections2Group());
	}

	/**
	 * Creates "Objects" palette tool group
	 * @generated
	 */
	private PaletteContainer createObjects1Group() {
		PaletteDrawer paletteContainer = new PaletteDrawer(
				Messages.Objects1Group_title);
		paletteContainer.setId("createObjects1Group"); //$NON-NLS-1$
		paletteContainer.add(createDataManagementElement1CreationTool());
		paletteContainer.add(createFamilyElement2CreationTool());
		paletteContainer.add(createReplaySink3CreationTool());
		paletteContainer.add(createSink4CreationTool());
		paletteContainer.add(createSource5CreationTool());
		return paletteContainer;
	}

	/**
	 * Creates "Connections" palette tool group
	 * @generated
	 */
	private PaletteContainer createConnections2Group() {
		PaletteDrawer paletteContainer = new PaletteDrawer(
				Messages.Connections2Group_title);
		paletteContainer.setId("createConnections2Group"); //$NON-NLS-1$
		paletteContainer.add(createFlow1CreationTool());
		return paletteContainer;
	}

	/**
	 * @generated
	 */
	private ToolEntry createDataManagementElement1CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(
				Messages.DataManagementElement1CreationTool_title,
				Messages.DataManagementElement1CreationTool_desc,
				Collections
						.singletonList(PipelineElementTypes.DataManagementElement_2006));
		entry.setId("createDataManagementElement1CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.DataManagementElement_2006));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createFamilyElement2CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(
				Messages.FamilyElement2CreationTool_title,
				Messages.FamilyElement2CreationTool_desc,
				Collections
						.singletonList(PipelineElementTypes.FamilyElement_2005));
		entry.setId("createFamilyElement2CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.FamilyElement_2005));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createReplaySink3CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(
				Messages.ReplaySink3CreationTool_title,
				Messages.ReplaySink3CreationTool_desc,
				Collections.singletonList(PipelineElementTypes.ReplaySink_2007));
		entry.setId("createReplaySink3CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.ReplaySink_2007));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createSink4CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(
				Messages.Sink4CreationTool_title,
				Messages.Sink4CreationTool_desc,
				Collections.singletonList(PipelineElementTypes.Sink_2002));
		entry.setId("createSink4CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.Sink_2002));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createSource5CreationTool() {
		NodeToolEntry entry = new NodeToolEntry(
				Messages.Source5CreationTool_title,
				Messages.Source5CreationTool_desc,
				Collections.singletonList(PipelineElementTypes.Source_2001));
		entry.setId("createSource5CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.Source_2001));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private ToolEntry createFlow1CreationTool() {
		LinkToolEntry entry = new LinkToolEntry(
				Messages.Flow1CreationTool_title,
				Messages.Flow1CreationTool_desc,
				Collections.singletonList(PipelineElementTypes.Flow_4001));
		entry.setId("createFlow1CreationTool"); //$NON-NLS-1$
		entry.setSmallIcon(PipelineElementTypes
				.getImageDescriptor(PipelineElementTypes.Flow_4001));
		entry.setLargeIcon(entry.getSmallIcon());
		return entry;
	}

	/**
	 * @generated
	 */
	private static class NodeToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List<IElementType> elementTypes;

		/**
		 * @generated
		 */
		private NodeToolEntry(String title, String description,
				List<IElementType> elementTypes) {
			super(title, description, null, null);
			this.elementTypes = elementTypes;
		}

		/**
		 * @generated
		 */
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeCreationTool(elementTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}

	/**
	 * @generated
	 */
	private static class LinkToolEntry extends ToolEntry {

		/**
		 * @generated
		 */
		private final List<IElementType> relationshipTypes;

		/**
		 * @generated
		 */
		private LinkToolEntry(String title, String description,
				List<IElementType> relationshipTypes) {
			super(title, description, null, null);
			this.relationshipTypes = relationshipTypes;
		}

		/**
		 * @generated
		 */
		public Tool createTool() {
			Tool tool = new UnspecifiedTypeConnectionTool(relationshipTypes);
			tool.setProperties(getToolProperties());
			return tool;
		}
	}
}
