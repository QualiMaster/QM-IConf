package de.uni_hildesheim.sse.qmApp.editors;

import static eu.qualimaster.easy.extension.QmConstants.SLOT_NAME;
import static eu.qualimaster.easy.extension.QmObservables.PART_INFRASTRUCTURE;
import static eu.qualimaster.easy.extension.QmObservables.RESOURCEUSAGE_USED_DFES;
import static eu.qualimaster.easy.extension.QmObservables.RESOURCEUSAGE_USED_MACHINES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.csstudio.swt.widgets.figures.MeterFigure;
import org.csstudio.swt.widgets.figures.TankFigure;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelineElementObservableWrapper;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelineNodeType;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelinesRuntimeUtils;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.PipelinesRuntimeUtils.PipelineGraphColoringWrapper;
import de.uni_hildesheim.sse.qmApp.pipelineUtils.RuntimeGraphTabItem;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure.IClientDispatcher;
import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ChangeParameterRequest;
import eu.qualimaster.adaptation.external.CloudPipelineMessage;
import eu.qualimaster.adaptation.external.ConfigurationChangeRequest;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.PipelineStatusRequest;
import eu.qualimaster.adaptation.external.PipelineStatusResponse;
import eu.qualimaster.adaptation.external.ReplayMessage;
import eu.qualimaster.adaptation.external.ResourceChangeRequest;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;
import eu.qualimaster.adaptation.external.UpdateCloudResourceMessage;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.QmObservables;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
import net.ssehub.easy.varModel.confModel.SequenceVariable;
import net.ssehub.easy.varModel.confModel.SetVariable;
import net.ssehub.easy.varModel.cst.ConstantValue;
import net.ssehub.easy.varModel.cst.OCLFeatureCall;
import net.ssehub.easy.varModel.model.Constraint;
import net.ssehub.easy.varModel.model.ContainableModelElement;
import net.ssehub.easy.varModel.model.DecisionVariableDeclaration;
import net.ssehub.easy.varModel.model.Project;
import net.ssehub.easy.varModel.model.datatypes.Compound;
import net.ssehub.easy.varModel.model.values.CompoundValue;
import net.ssehub.easy.varModel.model.values.ContainerValue;
import net.ssehub.easy.varModel.model.values.EnumValue;
import net.ssehub.easy.varModel.model.values.IntValue;
import net.ssehub.easy.varModel.model.values.ReferenceValue;
import net.ssehub.easy.varModel.model.values.StringValue;
import net.ssehub.easy.varModel.model.values.Value;
/**
 * The preliminary editor for runtime settings.
 * 
 * The editors main feature lies in the creation of user generated, customized graphs which provide information about
 * certain pipelines, pipeline-elements, selected observables. Moreover the user can specify preferred colors for chosen
 * pipelines and their corresponding underlying elements.
 * 
 * If every choice is taken, the graphs can be shown in a new Tab.
 * 
 * @author Holger Eichelberger
 * @author Niko Nowatzki
 */
public class RuntimeEditor extends EditorPart implements IClientDispatcher, IInfrastructureListener {

    private static int counter = 0;
    private static CTabFolder tabFolder;
    private static Label infoLabel;
    private static HashMap<String, String> observablesMap = new HashMap<String, String>();
    private static Table observablesTable;
    
    private static final int PIPELINE_DISPLAY_BUFFER_SIZE = 50;
    private static final int PIPELINE_DISPLAY_DELAY = 100; 
    private static Collection<Value> observalbeSet = new ArrayList<Value>();
    private String executionTimeString = "execution time (s)";
    private String pipelineActivityString = "pipeline activity (";
    private String observablesString = "observables (";
    private String saveSelectionsText = "Save Selections";

    @SuppressWarnings("unused")
    private int insertMark = 0;
    private MeterFigure usedClusterMachines;
    private MeterFigure usedHardwareMachines;
    private Map<String, ArrayList<PipelineTrace>> pipelineTraces = new HashMap<String, ArrayList<PipelineTrace>>();

    private CheckboxTreeViewer treeViewerPipelineChooser;

    private Table treeViewerColorChooser;
    
    private List<Color> colorList = new ArrayList<Color>();
    private int colorIndex = 0;
    private List<PipelineGraphColoringWrapper> pipelinesToDisplayInTableWithObservable =
            new ArrayList<PipelineGraphColoringWrapper>();
    private HashMap<String, Set<String>> deliveringObservables = new HashMap<String, Set<String>>();
    private int pointStyleIndicator = 0;
    
    /**
     * Represents a pipeline trace, i.e., an observation buffer and an XY-graph trace.
     * 
     * @author Holger Eichelberger
     */
    private static class PipelineTrace {
        private CircularBufferDataProvider dataProvider;
        private Trace trace;
        private String observable;
        @SuppressWarnings("unused")
        private String monitoringName;

        /**
         * Creates the trace.
         * 
         * @param monitoringName
         *            the label
         * @param actulName The traces actual name.
         * @param xAxis
         *            the x-axis to display on
         * @param yAxis
         *            the y-axis to display on
         * @param observable
         *            the observable to draw
         */
        private PipelineTrace(String monitoringName, String actulName, Axis xAxis, Axis yAxis, String observable) {
            this.observable = observable;
            // create a trace data provider, which will provide the data to the trace.
            dataProvider = new CircularBufferDataProvider(true);
            dataProvider.setBufferSize(PIPELINE_DISPLAY_BUFFER_SIZE);
            dataProvider.setUpdateDelay(PIPELINE_DISPLAY_DELAY);

            // create the latency trace
            this.monitoringName = monitoringName;
            trace = new Trace(actulName, xAxis, yAxis, dataProvider);
            trace.setLineWidth(5);
            trace.setDataProvider(dataProvider);
        }

        /**
         * Set the color of this trace.
         * 
         * @param color
         *            Color to set.
         */
        private void setColor(Color color) {
            trace.setTraceColor(color);
        }

        /**
         * Returns the trace.
         * 
         * @return the trace
         */
        private Trace getTrace() {
            return trace;
        }

        /**
         * Clears the trace.
         */
        private void clearTrace() {
            dataProvider.clearTrace();
        }

        /**
         * Updates the trace.
         * 
         * @param observations
         *            the observations to take the value from
         */
        private void update(Map<String, Double> observations) {
            final Double observation = observations.get(observable);
            if (null != observation) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        dataProvider.addSample(new Sample(System.currentTimeMillis(), observation.doubleValue()));
                    }
                });

            }
        }
    }

    @Override
    public void doSave(IProgressMonitor monitor) {
        // nothing to save
    }

    @Override
    public void doSaveAs() {
        // nothing to save
    }

    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        setSite(site);
        setInput(input);
    }

    @Override
    public boolean isDirty() {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * Let a tab pop out.
     * 
     * @param tabItem
     *            The tabItem which will be popped out.
     * @param location
     *            Location for the lew opped out dialog.
     */
    private static void popOut(CTabItem tabItem, Point location) {
        Control control = tabItem.getControl();
        tabItem.setControl(null);
        Shell itemShell = new Shell(tabItem.getParent().getShell(), SWT.DIALOG_TRIM | SWT.RESIZE);
        itemShell.setLayout(new FillLayout());
        control.setParent(itemShell);
        control.setVisible(true); // control is hidden by tabItem.setControl( null ), make visible again
        itemShell.setLocation(location);
        itemShell.open();
    }

    @Override
    public void createPartControl(Composite parent) {

        Infrastructure.registerDispatcher(this);
        Infrastructure.registerListener(this);

        fillColorList();
        
        FillLayout fillLayout = new FillLayout();
        fillLayout.marginHeight = 5;
        fillLayout.marginWidth = 5;
        parent.setLayout(fillLayout);

        Composite outer = new Composite(parent, SWT.BORDER);
        FormLayout formLayout = new FormLayout();
        formLayout.marginHeight = 5;
        formLayout.marginWidth = 5;
        formLayout.spacing = 5;
        outer.setLayout(formLayout);

        // The whole left part which contains everything but the meter-figures.
        Composite innerLeft = new Composite(outer, SWT.BORDER);
        formLayout = new FormLayout();
        formLayout.marginHeight = 5;
        formLayout.marginWidth = 5;
        formLayout.spacing = 5;
        formLayout.marginTop = 5;
        innerLeft.setLayout(formLayout);

        FormData fData = new FormData();
        fData.top = new FormAttachment(0);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(80); // Locks on 60% of the view
        fData.bottom = new FormAttachment(60);
        innerLeft.setLayoutData(fData);

        // Right part for special stuff like the meter figures.
        Composite innerRight = new Composite(outer, SWT.BORDER);
        GridLayout innerRightLayout = new GridLayout();
        innerRightLayout.numColumns = 1;
        innerRight.setLayout(innerRightLayout);
        fData = new FormData();
        fData.top = new FormAttachment(0);
        fData.left = new FormAttachment(innerLeft);
        fData.right = new FormAttachment(100);
        fData.bottom = new FormAttachment(60);
        innerRight.setLayoutData(fData);
        
        createMoreControls(outer, innerLeft, innerRight, parent);
    }

    /**
     * Prepare some standard colors for traces.
     */
    private void fillColorList() {
        colorList.clear();
        Color green = new Color(Display.getCurrent(), new RGB(0, 204, 0));
        Color red = new Color(Display.getCurrent(), new RGB(255, 0, 0));
        Color blue = new Color(Display.getCurrent(), new RGB(0, 128, 255));
        Color orange = new Color(Display.getCurrent(), new RGB(255, 128, 0));
        Color purple = new Color(Display.getCurrent(), new RGB(204, 0, 204));
        Color grey = new Color(Display.getCurrent(), new RGB(64, 64, 64));
        Color yellow = new Color(Display.getCurrent(), new RGB(255, 255, 0));
        colorList.add(green);
        colorList.add(red);
        colorList.add(blue);
        colorList.add(orange);
        colorList.add(purple);
        colorList.add(grey);
        colorList.add(yellow);  
    }

    /**
     * Create more controls for the runtime-editor.
     * 
     * @param outer
     *            outer composite which constains everything the runtime-editor has to offer.
     * @param innerLeft left part of the view.
     * @param innerRight right side of the view.
     * @param parent parent composite.
     */
    private void createMoreControls(Composite outer, Composite innerLeft, Composite innerRight, Composite parent) {
        // A special area for a button and a label which holds a tip.
        Composite topButtons = new Composite(innerLeft, SWT.FILL);
        topButtons.setLayout(new RowLayout());

        // Section for graphs.
        Composite innerLeftTop = new Composite(innerLeft, SWT.BORDER);
        FormData fData = new FormData();
        fData.top = new FormAttachment(topButtons);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(100); // Locks on 60% of the view
        fData.bottom = new FormAttachment(100);
        innerLeftTop.setLayoutData(fData);
        innerLeftTop.setLayout(new FillLayout());

        // The bottom part holds controls for customizing the graphs.
        Composite bottom = new Composite(outer, SWT.BORDER);
        fData = new FormData();
        fData.top = new FormAttachment(innerLeft);
        fData.left = new FormAttachment(0);
        fData.right = new FormAttachment(93); // Locks on 60% of the view
        fData.bottom = new FormAttachment(100);
        bottom.setLayoutData(fData);
        bottom.setLayout(new FillLayout());

        Composite innerLeftBottom = new Composite(outer, SWT.NONE);
        fData = new FormData();
        fData.top = new FormAttachment(innerRight);
        fData.left = new FormAttachment(bottom);
        fData.right = new FormAttachment(100); // Locks on 60% of the view
        fData.bottom = new FormAttachment(100);
        innerLeftBottom.setLayoutData(fData);
        innerLeftBottom.setLayout(new GridLayout());

        Composite innerLeftBottomRight = new Composite(innerLeft, SWT.NONE);
        fData = new FormData();
        fData.top = new FormAttachment(innerLeftTop);
        fData.left = new FormAttachment(innerLeftBottom);
        fData.right = new FormAttachment(100); // Locks on 60% of the view
        fData.bottom = new FormAttachment(100);
        innerLeftBottomRight.setLayoutData(fData);
        innerLeftBottomRight.setLayout(new GridLayout());

        // Create all the controls needed for the customizing of graphs.
        createOptionsPanel(bottom, innerLeftBottom);

        // Create the controls which will be needed for displaying graphs.
        createMonitoringPanel(innerRight);

        addTabFolder(innerLeftTop, topButtons, parent);

        determineQMObervable();
        
        boolean answer = MessageDialog.openQuestion(this.getSite().getShell(), "Restore selections",
                "Do you want to load the selections from the last session?");

        if (answer && PipelinesRuntimeUtils.storedDataExist()) {
            loadItemsLocally();
        }
    }

    /**
     * Add TabFolders in the inner-top-left composite. In these tabs graphs will be displayed.
     * 
     * @param innerLeftTop
     *            The Composite in which the tabs are created.
     * @param topButtons
     *            Composite which holds buttons we have to listen to in order to add new tabs.
     * @param parent
     *            parent Composite.
     */
    private void addTabFolder(Composite innerLeftTop, Composite topButtons, Composite parent) {

        // Let the graphs be tabs in oder to allow floating dialogs for graphs.
        tabFolder = new CTabFolder(innerLeftTop, SWT.CLOSE);

        Listener dragListener = new Listener() {
            private CTabItem dragItem;

            public void handleEvent(Event event) {
                Point mouseLocation = new Point(event.x, event.y);
                switch (event.type) {
                case SWT.DragDetect: {
                    CTabItem item = tabFolder.getItem(mouseLocation);
                    if (dragItem == null && item != null) {
                        dragItem = item;
                        tabFolder.setCapture(true);
                    }
                    break;
                }
                case SWT.MouseUp: {
                    if (dragItem != null && !tabFolder.getBounds().contains(mouseLocation)) {
                        popOut(dragItem, tabFolder.toDisplay(mouseLocation));
                        dragItem.dispose();
                        dragItem = null;
                    }
                    break;
                }
                default:
                    break;
                }
            }
        };
        tabFolder.addListener(SWT.DragDetect, dragListener);
        tabFolder.addListener(SWT.MouseUp, dragListener);

        createButtons(topButtons, tabFolder);

        Rectangle clientArea = parent.getBounds();
        tabFolder.setBounds(clientArea);
        // tabFolder.pack();
        tabFolder.update();
        tabFolder.setSimple(true);
        tabFolder.layout();

        tabFolder
                .setSelectionBackground(
                        new Color[] {Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW),
                                Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
                                Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW) },
                        new int[] {50, 100});

    }

    /**
     * Creates the buttons for moving the insert mark and adding a tab.
     * 
     * @param composite
     *            the parent composite
     * @param tabFolder
     *            The tabfolder which contains the graphs.
     */
    private void createButtons(Composite composite, final CTabFolder tabFolder) {
        // Add a tab
        Button button = new Button(composite, SWT.NONE);
        button.setText("Add Tab");

        infoLabel = new Label(composite, SWT.BOLD);
        infoLabel.setText("Tip: Just drag a tab in order to view it in a separate dialog."
                + "This way several diagrams can be viewed at the same time");

        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // Still needed?
            }
        });
    }

    /**
     * Create the graph-panel.
     * 
     * @param innerLeftBottom
     *            The composite on which the canvas is created.
     * @return xyGraphCanvas composite with added {@link LightweightSystem}.
     */
    private Control createGraphPanel(Composite innerLeftBottom) {

        Canvas xyGraphCanvas = new Canvas(innerLeftBottom, SWT.BORDER);
        LightweightSystem lws = new LightweightSystem(xyGraphCanvas);

        // create a new XY Graph.
        XYGraph xyGraph = new XYGraph();
        xyGraph.primaryXAxis.setShowMajorGrid(true);
        xyGraph.primaryXAxis.setTitle(executionTimeString);
        xyGraph.primaryXAxis.setAutoScale(true);

        xyGraph.primaryYAxis.setVisible(false);

        xyGraph.primaryYAxis
                .setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));
        String pipelineString = pipelineActivityString;
        StringBuilder pipelineTitle = new StringBuilder(pipelineString);
        String observables = observablesString;
        StringBuilder observableTitle = new StringBuilder(observables);
        
        // Go through all saved info and create the corresponding traces.
        for (int i = 0; i < pipelinesToDisplayInTableWithObservable.size(); i++) {

            PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(i);

            String varName = wrapper.getElemName();
            String parent = wrapper.getPipelineParent();
            String obs = wrapper.getObs();
            Color color = wrapper.getColor();

            if (parent != null && !"".equals(parent) && !varName.equals(parent)
                    && !pipelineTitle.toString().contains(parent)) {
                createObservableTraces(varName, parent, xyGraph, color, obs);
            } else {
                createObservableTraces(varName, xyGraph, color, obs);
            }
        }

        try {
            // set some constant titles, such as the achses captions.
            if (!pipelinesToDisplayInTableWithObservable.isEmpty()) {
                PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(0);
                pipelineTitle.append(wrapper.getPipelineParent());
                observableTitle.append(wrapper.getObs() + "...");
            }

            pipelineTitle.append(",...)");
            observableTitle.append(")");

            Font titleFont = new Font(Display.getCurrent(), "Arial", 11, SWT.BOLD);
            xyGraph.setTitleFont(titleFont);
            xyGraph.setTitle(pipelineTitle.toString());

            lws.setContents(xyGraph); 

        } catch (java.lang.IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return xyGraphCanvas;
    }

    /**
     * Create the traces which are specified by the selected items in the trees and tables.
     * 
     * @param pipElementName
     *            name of the currently selected element from the tree.
     * @param pipelineName
     *            name of the possible parents name. (Pipeline name)
     * @param xyGraph
     *            the {@link XYGraph} to draw upon.
     * @param color
     *            Color for this specific pipeline.
     * @param obs
     *            the observable to draw.
     */
    private void createObservableTraces(String pipElementName, String pipelineName, XYGraph xyGraph, Color color,
            String obs) {

        //ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();

        String observalbeForTrace = observablesMap.get(obs);

        if (observalbeForTrace != null) {

            String monitoringName = pipelineName + ":" + pipElementName;
            String actulName = pipelineName + ":" + pipElementName + " (" + obs + ")";
            Axis axis = new Axis(actulName, true);
            xyGraph.addAxis(axis);
            axis.setAutoFormat(true);

            PipelineTrace pTrace = new PipelineTrace(monitoringName, actulName, xyGraph.primaryXAxis, axis,
                    observalbeForTrace);
            axis.setAutoScale(true);

            if (!colorList.contains(color)) {
                pTrace.setColor(color);
                colorList.add(color);
            } else {
                pTrace.setColor(color);
                pTrace.trace.setPointStyle(getRandomPointStyle());
            }

            ArrayList<PipelineTrace> traces = pipelineTraces.get(pipelineName);

            if (traces == null) {
                traces = new ArrayList<PipelineTrace>();
            }

            if (!traces.contains(pTrace)) {
                traces.add(pTrace);
                pipelineTraces.put(pipelineName, traces);
                xyGraph.addTrace(pTrace.getTrace());
            }
        }
    }

    /**
     * Get a random pointstyle for the traces.
     * 
     * @return style The randomly chosen style. May be NULL.
     */
    private PointStyle getRandomPointStyle() {

        PointStyle style = null;

        int length = PointStyle.values().length;

        if (pointStyleIndicator >= length) {
            pointStyleIndicator = 0;
        }
        style = PointStyle.values()[pointStyleIndicator];
        pointStyleIndicator++;

        return style;
    }

    /**
     * Create the traces which are specified by the selected items in the trees and tables.
     * 
     * @param pipelineName
     *            name of the possible parents name. (Pipeline name)
     * @param xyGraph
     *            the {@link XYGraph} to draw upon.
     * @param color
     *            Color for this specific pipeline.
     * @param obs
     *            the observable to draw.
     */
    private void createObservableTraces(String pipelineName, XYGraph xyGraph, Color color, String obs) {

        //ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();

        String observalbeForTrace =  observablesMap.get(obs);
        
        if (observalbeForTrace != null) {
            
            String actulName = pipelineName + " (" + observalbeForTrace + ")";
            Axis axis = new Axis(actulName, true);
            xyGraph.addAxis(axis);
            axis.setAutoFormat(true);
            
            PipelineTrace pTrace = new PipelineTrace(pipelineName + " (" + observalbeForTrace + ")",
                    pipelineName + " (" + observalbeForTrace + ")", xyGraph.primaryXAxis, axis, observalbeForTrace);
            axis.setAutoScale(true);

            if (!colorList.contains(color)) {
                pTrace.setColor(color);
                colorList.add(color);
            } else {
                pTrace.setColor(color);
                pTrace.trace.setPointStyle(getRandomPointStyle());
            }

            ArrayList<PipelineTrace> traces = pipelineTraces.get(pipelineName);

            if (traces == null) {
                traces = new ArrayList<PipelineTrace>();
            }

            if (!traces.contains(pTrace)) {
                traces.add(pTrace);
                pipelineTraces.put(pipelineName, traces);
                xyGraph.addTrace(pTrace.getTrace());
            }
        }
    }

    /**
     * Remove all null-values from the given Collection of values.
     * @param observalbeSet Given Set of values.
     * @return observableSet Same set without null values.
     */
    public static Collection<Value> clean(final Collection<Value> observalbeSet) {
        
        observalbeSet.removeAll(Collections.singleton(null));
        return observalbeSet;
    }
    
    /**
     * Determine the QM observables with given String.
     */
    private void determineQMObervable() {
        
        ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();
        observalbeSet = clean(observalbeSet);
        Iterator<Value> obsIterator = observalbeSet.iterator();
        
        while (obsIterator.hasNext()) {
            
            Value iterObs = (Value) obsIterator.next();
            
            if (iterObs != null && iterObs.getValue() != null) {
                String oldIterObsString = (String) iterObs.getValue();
                

                for (int j = 0; j < allPossibleObservables.size(); j++) {
                
                    String oldPossObsString = allPossibleObservables.get(j);
                    
                    //Vom iterator
                    String newIterObsString = oldIterObsString.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase(); 
                    //Von allen möglichen observables als string
                    String newPossObsString = oldPossObsString.replaceAll("[^a-zA-Z0-9]", "").trim().toLowerCase(); 
                    
                    if (newIterObsString.equals(newPossObsString)
                            || newIterObsString.substring(0, newIterObsString.
                        length() - 1).equals(newPossObsString) || newPossObsString.substring(0, newPossObsString.
                                length() - 1).equals(newIterObsString)) {
                        
                        observablesMap.put(oldIterObsString, oldPossObsString);
                    }
                }
            }
        }
    }

    /**
     * Create the options panel in which the user can specify the pipelines, pipeline-elements, observables and colors
     * he wants to display.
     * 
     * @param innerLeftTop
     *            innerLeft composite in the view. Here the optionsPanel is located.
     * @param innerLeftBottomRight
     *            Composite which holds buttons we´ll listen to.
     */
    private void createOptionsPanel(Composite innerLeftTop, Composite innerLeftBottomRight) {
        GridLayout gridLayout = new GridLayout(5, true);
        gridLayout.horizontalSpacing = 10;
        innerLeftTop.setLayout(gridLayout);

        PipelinesRuntimeUtils instance = PipelinesRuntimeUtils.INSTANCE;

        treeViewerPipelineChooser = new CheckboxTreeViewer(innerLeftTop, SWT.BORDER | SWT.RADIO);

        Project project = Configuration.PIPELINES.getConfiguration().getProject();

        for (int i = 0; i < project.getElementCount(); i++) {
            ContainableModelElement modelElement = project.getElement(i);

            if (modelElement instanceof Constraint) {
                Constraint constraint = (Constraint) modelElement;
                OCLFeatureCall object = (OCLFeatureCall) constraint.getConsSyntax();

                for (int j = 0; j < object.getParameterCount(); j++) {
                    if (object.getParameter(j) instanceof ConstantValue) {
                        if (object.getParameter(j) instanceof ConstantValue) {
                            ConstantValue constValue = (ConstantValue) object.getParameter(j);

                            if (constValue.getConstantValue() instanceof ContainerValue) {
                                ContainerValue container = (ContainerValue) constValue.getConstantValue();

                                for (int k = 0; k < container.getElementSize(); k++) {
                                    ReferenceValue reference = (ReferenceValue) container.getElement(k);
                                    instance.addPipeline(reference.getValue().getName());
                                }
                            }
                        }
                    }
                }
            }
        }

        File workspaceURI = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();

        PipelinesRuntimeUtils.INSTANCE.getPipelineConfiguration(workspaceURI);
        // getPipelineConfiguration(workspaceURI);

        treeViewerPipelineChooser.setContentProvider(PipelinesRuntimeUtils.INSTANCE.getContentProvider());
        treeViewerPipelineChooser.setLabelProvider(PipelinesRuntimeUtils.INSTANCE.getLabelProvider());

        TreeColumn column = new TreeColumn(treeViewerPipelineChooser.getTree(), SWT.LEFT);
        column.setText("Pipeline Observable Elements");
        column.setWidth(200);

        treeViewerPipelineChooser.getTree().setHeaderVisible(true);
        treeViewerPipelineChooser.setInput(PipelinesRuntimeUtils.INSTANCE.getPipelinesToDisplayInTable());

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewerPipelineChooser.getTree().setLayoutData(data);

        // ------------------------------------------------------------------------------------------
        createObservableTable(innerLeftTop, innerLeftBottomRight);
    }

    /**
     * Create the section which is responsible for selecting observables.
     * 
     * @param innerLeftTop
     *            part of the view.
     * @param innerLeftBottomRight
     *            part of the view.
     */
    private void createObservableTable(Composite innerLeftTop, Composite innerLeftBottomRight) {
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        observablesTable = new Table(innerLeftTop, SWT.CHECK | SWT.BORDER);
        observablesTable.setLayoutData(data);
        TableColumn column2 = new TableColumn(observablesTable, SWT.LEFT);
        column2.setText("Observables");
        column2.setWidth(200);
        observablesTable.setHeaderVisible(true);
        observablesTable.clearAll();
        observablesTable.redraw();
        
        net.ssehub.easy.varModel.confModel.Configuration config = Configuration.OBSERVABLES.getConfiguration();
        Iterator<IDecisionVariable> iter = config.iterator();

        while (iter.hasNext()) {
            Object object = iter.next();
            if (object instanceof SetVariable || object instanceof SequenceVariable) {
                collectQualityParameters(object);
            }
        }
        // Add listener for user action. When the user selects pipeline-elements and/or observalbes, we have to react.
        treeViewerPipelineChooser.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                CheckboxTreeViewer treeViewer = (CheckboxTreeViewer) event.getSource();
                observablesTable.setToolTipText("");
                
                PipelineGraphColoringWrapper wrapper = (PipelineGraphColoringWrapper) event.getElement();
                String pipParent = wrapper.getPipelineParent();
                String elemName = wrapper.getElemName();
                
                for (int i = 0; i < treeViewer.getTree().getItemCount(); i++) {
                    TreeItem topLevelItem = treeViewer.getTree().getItem(i);
                    if (topLevelItem.getText().equals(wrapper.getElemName())) {

                        PipelineNodeType type = wrapper.getType();
                       
                        if (pipParent != null && !elemName.trim().equals(pipParent.trim())) {                           
                            PipelinesRuntimeUtils.INSTANCE.setObservablesTableSelections(observablesTable, type,
                                deliveringObservables, pipParent + ":" + elemName);
                                 // Check for observalbes and disable some
                        } else {
                            PipelinesRuntimeUtils.INSTANCE.setObservablesTableSelections(observablesTable, type,
                                    deliveringObservables, elemName);
                        }
                    } else {
                        topLevelItem.setChecked(false);
                    }

                    for (int j = 0; j < topLevelItem.getItemCount(); j++) {
                        TreeItem item = treeViewer.getTree().getItem(i).getItem(j);
                        if (item.getText().equals(elemName)) {
                            item.setChecked(true);

                            PipelineNodeType type = wrapper.getType();

                            String elementName = pipParent + ":" + elemName;
                            PipelinesRuntimeUtils.INSTANCE.setObservablesTableSelections(observablesTable, type,
                                    deliveringObservables, elementName);
                        } else {
                            item.setChecked(false);
                        }
                    }
                }
            }
        });
        createObservableTable2(innerLeftTop, innerLeftBottomRight);
    }

    /**
     * Create the observables-table which shows the found observables from the config and makes them selectable.
     * 
     * @param innerLeftTop
     *            part of the view.
     * @param innerLeftBottomRight
     *            part of the view.
     */
    private void createObservableTable2(Composite innerLeftTop, Composite innerLeftBottomRight) {
        Button button4 = new Button(innerLeftTop, SWT.NONE);
        button4.setText(saveSelectionsText);
        GridData data = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
        button4.setLayoutData(data);
        treeViewerColorChooser = new Table(innerLeftTop, SWT.MULTI | SWT.BORDER);
        treeViewerColorChooser.setLinesVisible(false);
        treeViewerColorChooser.setHeaderVisible(true);

        String[] titles2 = {"Pipeline", "Choose", "Color"};
        TableColumn column1 = new TableColumn(treeViewerColorChooser, SWT.NONE);
        column1.setText(titles2[0]);
        column1.setWidth(200);

        TableColumn column2 = new TableColumn(treeViewerColorChooser, SWT.NONE);
        column2.setText(titles2[1]);
        column2.setWidth(70);

        TableColumn column3 = new TableColumn(treeViewerColorChooser, SWT.NONE);
        column3.setText(titles2[2]);
        column3.setWidth(70);

        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.horizontalSpan = 2;
        treeViewerColorChooser.setLayoutData(data);

        Menu menu = new Menu(treeViewerColorChooser.getShell(), SWT.POP_UP);
        treeViewerColorChooser.setMenu(menu);
        final MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Delete Selection");
        
        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                int activePipCount = pipelineTraces.values().size();
                if (activePipCount > 0) {
                    performDeletion();
                } else {
                    MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Information",
                            "You should draw and take a look at you selected traces before you delete them");
                }
            }
        });
        Button button = new Button(innerLeftBottomRight, SWT.NONE);
        button.setText("Draw");

        data = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
        button.setLayoutData(data);

        button.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                // Draw the graph with the collected info so far.
                drawGraphsInNewTab(false);
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
        button4.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                // When the "Save"-Button is selected, save the selected pipeline-elements and observalbes
                // by creating objects, which wrap up all info.
                saveSelections();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
    }

    /**
     * Remove the chosen item from the savedObservablesTable.
     */
    protected void performDeletion() {
        // Delete a certain PipelineTrace.
        TableItem tableItem = treeViewerColorChooser.getSelection()[0];
        int index = treeViewerColorChooser.getSelectionIndex();
        
        Collection<ArrayList<PipelineTrace>> allTraces = pipelineTraces.values();

        for (int i = 0; i < allTraces.size(); i++) {
            Iterator<ArrayList<PipelineTrace>> iterator = allTraces.iterator();

            if (iterator.hasNext()) {
                ArrayList<PipelineTrace> traces = (ArrayList<PipelineTrace>) iterator.next();
                
                String name = tableItem.getText();
                if (name.contains("(")) {
                    name = name.substring(name.lastIndexOf(":"), name.length());
                } else {
                    name = name.substring(name.lastIndexOf(":"), name.length());
                }
                for (int j = 0; j < traces.size(); j++) {
                    PipelineTrace trace = traces.get(j);

                    String wholeName = trace.getTrace().getName().replaceAll("[^a-zA-Z0-9]", "")
                            .toLowerCase().trim();
                      
                    String obs = trace.observable.replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();

                    String item = tableItem.getText().toLowerCase();
                    if (item.contains(":")) {
                        item = item.substring(0, item.lastIndexOf(":"));
                    }

                    String itemToCompare = item.trim().replaceAll("[^a-zA-Z0-9]", "").toLowerCase().trim();
                    name = name.replaceAll("[^a-zA-Z0-9]", "");
                    name = name.trim().toLowerCase();
                                    
                    if (wholeName.contains(itemToCompare)) {
                        
                        //throughputitem != throughputitems
                        if (name.toLowerCase().equals(obs) || obs.substring(0, obs.length() - 1).equals(name)) {
                            traces.remove(trace);
                            pipelineTraces.remove(trace);
                            trace.clearTrace();
                            pipelinesToDisplayInTableWithObservable.remove(index);

                        }
                    }
                }
            }
        }

        TableEditor editor = (TableEditor) tableItem.getData("ColorButton");
        TableEditor editor2 = (TableEditor) tableItem.getData("ColorLabel");
        treeViewerColorChooser.remove(treeViewerColorChooser.indexOf(tableItem));

        editor.getEditor().dispose();
        editor.dispose();

        editor2.getEditor().dispose();
        editor2.dispose();
        
        treeViewerColorChooser.update();
        treeViewerColorChooser.redraw();
        //Go though the labels and update the colors in the pipelineColorChooser-table.
        updateColorLabels();
    }

    /**
     * Update the color-labels in PipelineColorChooser.
     */
    private void updateColorLabels() {

        for (int j = 0; j < pipelinesToDisplayInTableWithObservable.size(); j++) {
            
            PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(j);
            Color color = wrapper.getColor();
            
            TableItem item = treeViewerColorChooser.getItem(j);
            TableEditor editor = (TableEditor) item.getData("ColorLabel");
            Label label = (Label) editor.getEditor();
            label.setBackground(color);
                   
        }            
        treeViewerColorChooser.update();
        treeViewerColorChooser.redraw();       
    }

    /**
     * Collect the qualityParameters from the ivml-configuration, so they can be mapped upn pipeline-elements.
     * 
     * @param object
     *            Set which holds the observalbes.
     */
    public static void collectQualityParameters(Object object) {

        String name = "";
        ConstantValue value = null;

        if (object instanceof SetVariable) {
            SetVariable set = (SetVariable) object;
            name = set.getDeclaration().getName();

            if (name.equals(QmConstants.VAR_OBSERVABLES_QUALITYPARAMS)) {
                value = (ConstantValue) set.getDeclaration().getDefaultValue();
            }
        }
        if (object instanceof SequenceVariable) {
            SequenceVariable sequence = (SequenceVariable) object;
            name = sequence.getDeclaration().getName();

            if (name.equals(QmConstants.VAR_OBSERVABLES_QUALITYPARAMS)) {
                value = (ConstantValue) sequence.getDeclaration().getDefaultValue();
            }
        }

        if (!"".equals(name) && value != null) {

            ContainerValue container = (ContainerValue) value.getConstantValue();

            if (observalbeSet.size() > 0) {
                observalbeSet.clear();
            }
            for (int i = 0; i < container.getElementSize(); i++) {

                CompoundValue cmpValue = (CompoundValue) container.getElement(i);
                Compound cType = (Compound) cmpValue.getType();

                for (int j = 0; j < cType.getInheritedElementCount(); j++) {
                    DecisionVariableDeclaration slotDecl = cType.getInheritedElement(j);
                    Value nestedValue = cmpValue.getNestedValue(slotDecl.getName());

                    observalbeSet.add(nestedValue);
                }
            }
            Object[] valueArray = observalbeSet.toArray();
            //sort valueArray
            valueArray = removeNull(valueArray);
            Arrays.sort(valueArray);
            
            //clear backup
            PipelinesRuntimeUtils.INSTANCE.getBackupObservableItem().clear();
            
            for (int k = 0; k < valueArray.length; k++) {
                Object obj = valueArray[k];

                if (obj != null && obj instanceof String) {
                    String stringValue = (String) obj;

                    TableItem treeItem = new TableItem(observablesTable, 0);
                    treeItem.setText(stringValue);

                    PipelinesRuntimeUtils.INSTANCE.getBackupObservableItem().add(stringValue);
                }
            }
        }
    }

    /**
     * Remove null values from an array.
     * @param array Given array of Strings.
     * @return toReturn Same array without null values.
     */
    public static String[] removeNull(Object[] array) {
        String[] toReturn = null;
        
        ArrayList<String> removed = new ArrayList<String>();
        for (Object str : array) {
            if (str != null) {
                StringValue toAdd = (StringValue) str;
                removed.add(toAdd.getValue());
            }
        }
        toReturn = removed.toArray(new String[0]);
        return toReturn;
    }
    
    /**
     * Save the users selections concerning the observables.
     */
    private void saveSelections() {

        try {
            PipelineGraphColoringWrapper selection = (PipelineGraphColoringWrapper) treeViewerPipelineChooser
                    .getCheckedElements()[0];
            String name = selection.getElemName();
            PipelineNodeType type = selection.getType();
            IDecisionVariable var = selection.getVar();
            String pipParent = selection.getPipelineParent();
            DecisionVariableDeclaration decl = selection.getDecl();

            for (TableItem item : observablesTable.getItems()) {

                if (item.getChecked()) {

                    String observableName = item.getText();
                    PipelineGraphColoringWrapper newWrapper = new PipelineGraphColoringWrapper(name, type, pipParent,
                            var, decl);

                    newWrapper.setObservable(observableName);
                    
                    Color color = null;
                    if (colorIndex < colorList.size()) {
                        color = colorList.get(colorIndex);
                    } else {
                        colorIndex = 0;
                        color = colorList.get(colorIndex);
                    }
                    
                    newWrapper.setColor(colorList.get(colorIndex));
                    colorIndex++;
                    
                    if (!containsWrapper(newWrapper)) {
                        if (newWrapper.getObs() != null) {
                            if (!alreadyContains(pipelinesToDisplayInTableWithObservable, selection)) {
                                pipelinesToDisplayInTableWithObservable.add(newWrapper);
                            }
                        }
                    }
                    if (!PipelinesRuntimeUtils.INSTANCE.pipelineObservableCombinationIsNotExisting(
                            treeViewerColorChooser, pipParent, name, observableName)) {
                        performSelection(pipParent, name, observableName, color);
                    }
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check whether a wrapper already exists within the List pipelinesToDisplayInTableWithObservable.
     * 
     * @param newWrapper
     *            The new wrapper to add.
     * @return toReturn true if already contained, false otherwise.
     */
    private boolean containsWrapper(PipelineGraphColoringWrapper newWrapper) {

        boolean toReturn = false;

        for (int i = 0; i < pipelinesToDisplayInTableWithObservable.size(); i++) {

            PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(i);

            String name = newWrapper.getElemName();
            String obs = newWrapper.getObs();

            if (wrapper.getElemName() != null && wrapper.getObs() != null) {
                if (wrapper.getElemName().equals(name) && wrapper.getObs().equals(obs)) {
                    toReturn = true;
                }
            }
        }
        return toReturn;
    }

    /**
     * Draw the graphs.
     * @param loading true if saved items shall be loadad, false otherwise.
     */
    private void drawGraphsInNewTab(boolean loading) {

        final RuntimeGraphTabItem newTabItem = new RuntimeGraphTabItem(tabFolder, SWT.NONE);
        newTabItem.setText("New Tab");

        if (!loading) {
            saveTreeLocally();
        }
        Control control = createGraphPanel(tabFolder);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 4;
        control.setLayoutData(data);

        Infrastructure.registerListener(newTabItem);

        newTabItem.setControl(control);
        
        //Let the new tabItem be shown
        tabFolder.setSelection(newTabItem);
        tabFolder.showItem(newTabItem);

        newTabItem.addListener(SWT.Dispose, new Listener() {
            public void handleEvent(Event event) {
                Infrastructure.unregisterListener(newTabItem);
            }
        });

    }

    /**
     * Adjust the chosen color in the editor.
     * 
     * @param label
     *            The label which is showing the actual color.
     * @param color2
     *            The chosen color to set.
     * @param caption The items caption.
     */
    private void adjustChosenColor(Label label, Color color2, String caption) {
        Rectangle bounds = treeViewerColorChooser.getShell().getBounds();

        final int x = bounds.x + bounds.width / 2 - 120;
        final int y = bounds.y + bounds.height / 2 - 170;

        final Shell shell = new Shell(treeViewerColorChooser.getShell());

        shell.setBounds(x, y, 0, 0);

        final ColorDialog dlg = new ColorDialog(shell);

        // Set the selected color in the dialog from
        // user's selected color
        dlg.setRGB(label.getBackground().getRGB());

        // Change the title bar text
        dlg.setText("Choose a Color");

        // Open the dialog and retrieve the selected color
        RGB rgb = dlg.open();

        if (rgb != null) {
            // Dispose the old color, create the
            // new one, and set into the label
            color2.dispose();
            Color color = new Color(treeViewerColorChooser.getShell().getDisplay(), rgb);
            label.setBackground(color);

            for (int i = 0; i < pipelinesToDisplayInTableWithObservable.size(); i++) {
                PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(i);

                String wrapperName = wrapper.getElemName();
                String observableName = wrapper.getObs();

                if (caption.contains(wrapperName) && caption.contains(observableName)) {
                    wrapper.setColor(color);
                }
            }
        }
    }

    /**
     * Get the selected color for the although selected pipeline and adjust the table.
     * 
     * @param pipParent
     *            Given pipeline.
     * @param name
     *            Given name where the color should be set.
     * @param observableName
     *            name of the observable.
     * @param givenColor 
     */
    private void performSelection(String pipParent, String name, String observableName, Color givenColor) {
        // get parent pipeline checkbox
        String caption = "";
        TableItem treeItem0 = new TableItem(treeViewerColorChooser, 0);
        if (!pipParent.equals(name)) {
            caption = pipParent + ": " + name + ": " + observableName;
            treeItem0.setText(caption);
        } else {
            caption = pipParent + ": " + observableName;
            treeItem0.setText(caption);
        }

        final String finalString = caption;

        TableItem[] items = treeViewerColorChooser.getItems();

        for (int i = 0; i < items.length; i++) {
            TableEditor editor = new TableEditor(treeViewerColorChooser);
            TableItem item = items[i];

            editor = new TableEditor(treeViewerColorChooser);
            Button button = new Button(treeViewerColorChooser, SWT.NONE);
            button.setText("Color");
            button.pack();

            editor.minimumWidth = button.getSize().x;
            editor.horizontalAlignment = SWT.LEFT;
            editor.setEditor(button, items[i], 1);
            
            if (item.getData("ColorButton") == null) {
                item.setData("ColorButton", editor);
            }
            editor = new TableEditor(treeViewerColorChooser);
            final Label label = new Label(treeViewerColorChooser, SWT.BORDER);
            label.setText("                  ");
            label.pack();

            editor.minimumWidth = label.getSize().x;
            editor.horizontalAlignment = SWT.LEFT;
            
            if (givenColor != null) {
                label.setBackground(givenColor);
            } else {
                label.setBackground(new Color(treeViewerColorChooser.getShell().getDisplay(), new RGB(124, 252, 0)));
            }
            
            GridData data = new GridData();
            data.grabExcessHorizontalSpace = true;

            final Color color = givenColor;
            label.setLayoutData(SWT.FILL);

            button.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    adjustChosenColor(label, color, finalString);
                }
            });
            editor.setEditor(label, items[i], 2);
            if (item.getData("ColorLabel") == null) {
                item.setData("ColorLabel", editor);
            }
        } 
    }

    @Override
    public void dispose() {
        Infrastructure.unregisterDispatcher(this);
        Infrastructure.unregisterListener(this);

        PipelinesRuntimeUtils instance = PipelinesRuntimeUtils.INSTANCE;
        instance.clearPipelines();
        
    }

    /**
     * Save the List of {@link TreeItem}. If the user is offline, the tree can be loaded from the saved file.
     */
    private void saveTreeLocally() {

        List<PipelineElementObservableWrapper> wrapperList = new ArrayList<PipelineElementObservableWrapper>();

        if (!treeViewerColorChooser.isDisposed()) {
            for (int i = 0; i < treeViewerColorChooser.getItemCount(); i++) {

                TableItem item = treeViewerColorChooser.getItem(i);
                String itemText = item.getText();
                
                //Get the name from the items text.
                String name = itemText.substring(0, itemText.indexOf(":"));
                //Get the observable by cutting with the last occurence of : in the items text.
                String observable = itemText.substring(itemText.lastIndexOf(":") + 1, itemText.length()).trim();

                PipelineElementObservableWrapper wrapper = new PipelineElementObservableWrapper(name.trim(),
                        observable.trim());
                wrapperList.add(wrapper);
            }             
            PipelinesRuntimeUtils.storeInfoInMetadata(wrapperList);  
        }
    }

    /**
     * Load the serialized tree. Use the extracted list to set the treeviewer input.
     */
    private void loadItemsLocally() {
        try {
            for (int a = 0; a < PipelinesRuntimeUtils.FILENAMES.length; a++) {
                
                if (PipelinesRuntimeUtils.getItemsFile(PipelinesRuntimeUtils.FILENAMES[a]) != null) {
                    
                    FileInputStream fileIn = new FileInputStream(PipelinesRuntimeUtils.getItemsFile(
                            PipelinesRuntimeUtils.FILENAMES[a]));
                    if (fileIn != null) {
                        ObjectInputStream in = new ObjectInputStream(fileIn);
                        @SuppressWarnings("unchecked")
                        List<PipelineElementObservableWrapper> wrapperList =
                                (ArrayList<PipelineElementObservableWrapper>) in.readObject();
                        pipelinesToDisplayInTableWithObservable.clear();
                        for (int i = 0; i < wrapperList.size(); i++) {
                            PipelineElementObservableWrapper wrapper = wrapperList.get(i);
            
                            String name = wrapper.getName();
                            String observable = wrapper.getObservable();
                            // get real variable and declaration
                            for (int j = 0; j < treeViewerPipelineChooser.getTree().getItemCount(); j++) {
            
                                TreeItem outerItem = treeViewerPipelineChooser.getTree().getItem(j);
                                if (outerItem.getText().equals(name)) {
                                    outerItem.setChecked(true);
                                    
                                    for (int m = 0; m < observablesTable.getItemCount(); m++) {
                                        TableItem obsItem = observablesTable.getItem(m);
                                        if (obsItem.getText().equals(observable)) {
                                            obsItem.setChecked(true);
                                        }
                                    }
                                    saveSelections();
                                    removeSelections();
                                }
                                for (int l = 0; l < outerItem.getItemCount(); l++) {
            
                                    TreeItem innerItem = outerItem.getItem(l);
            
                                    if (innerItem.getText().equals(name)) {
                                        innerItem.setChecked(true);
            
                                        for (int m = 0; m < observablesTable.getItemCount(); m++) {
                                            TableItem obsItem = observablesTable.getItem(m);
            
                                            if (obsItem.getText().equals(observable)) {
                                                obsItem.setChecked(true);
                                            }
                                        }
                                        saveSelections();
                                        removeSelections();
                                    }
                                }
                            }
                            removeSelections();   
                        }
                        drawGraphsInNewTab(true);
                        in.close();
                        fileIn.close();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            //...
        }
    }

    /**
     * Check if a {@link PipelineGraphColoringWrapper} already exists in the list.
     * 
     * @param pipelinesToDisplayInTableWithObservable
     *            List of {@link PipelineElementColoringWrapper}.
     * @param selection
     *            The attending {@link PipelineElementColoringWrapper}.
     * @return toReturn true if contains, false otherwise.
     */
    private boolean alreadyContains(List<PipelineGraphColoringWrapper> pipelinesToDisplayInTableWithObservable,
            PipelineGraphColoringWrapper selection) {

        boolean toReturn = false;

        for (int i = 0; i < pipelinesToDisplayInTableWithObservable.size(); i++) {
            PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(i);

            if (wrapper.getObs() != null) {
                if (wrapper.getElemName().equals(selection.getElemName())
                        && wrapper.getObs().equals(selection.getObs())) {
                    toReturn = true;
                }
            }
            if (wrapper.getObs() == null) {
                toReturn = true;
            }
        }
        return toReturn;
    }

    /**
     * Remove every selection from both the first and second table.
     */
    private void removeSelections() {

        for (int i = 0; i < treeViewerPipelineChooser.getTree().getItemCount(); i++) {

            TreeItem outerItem = treeViewerPipelineChooser.getTree().getItem(i);
            outerItem.setChecked(false);

            for (int j = 0; j < outerItem.getItemCount(); j++) {

                TreeItem innerItem = outerItem.getItem(j);
                innerItem.setChecked(false);
            }
        }

        for (int k = 0; k < observablesTable.getItemCount(); k++) {

            TableItem item = observablesTable.getItem(k);
            item.setChecked(false);
        }
    }

    /**
     * Get the map of all mapped observables.
     * @return observableMap A map containing all infrastructur-observables and corresponding
     * qualimaster-observables.
     */
    public static HashMap<String, String> getObservablesMap() {
        return observablesMap;
    }
    
    /**
     * Enables or disables the buttons on this editor.
     */
    private void enableButtons() {
        // boolean connected = Infrastructure.isConnected();
    }

    /**
     * Returns the selected element name of a combo.
     * 
     * @param combo
     *            the combo to return the selected for
     * @return the selected name or <b>null</b> if nothing (valid) was selected
     */
    protected String getComboSelected(Combo combo) {
        String result = combo.getText();
        if (null == result || 0 == result.length()) {
            result = null;
        }
        return result;
    }

    /**
     * Create meter-widget for monitoring panel.
     * 
     * @param parent
     *            parent composite on which the widgets are placed.
     * @return the created control
     */
    private Control createMeter(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(1, true));

        Label nodeLabel = new Label(panel, SWT.CENTER);
        nodeLabel.setText("used nodes: ");
        GridData data = new GridData();
        nodeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        nodeLabel.setLayoutData(data);

        Canvas clusterCanvas = new Canvas(panel, SWT.BORDER);
        data = new GridData(GridData.FILL_VERTICAL);
        data.heightHint = 100;
        data.widthHint = 150;
        clusterCanvas.setLayoutData(data);

        Label dfeLabel = new Label(panel, SWT.CENTER);
        dfeLabel.setText("used DFE´s: ");
        data = new GridData();
        dfeLabel.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, true, true));
        dfeLabel.setLayoutData(data);

        Canvas hardwareCanvas = new Canvas(panel, SWT.BORDER);
        data = new GridData(GridData.FILL_VERTICAL);
        data.heightHint = 100;
        data.widthHint = 150;
        hardwareCanvas.setLayoutData(data);

        createClusterMachinesMeter(clusterCanvas);
        createHardwareNodesMeter(hardwareCanvas);

        invalidateGauges();
        return panel;
    }

    /**
     * Invalidates all gauges on initialization / disconnection.
     */
    private void invalidateGauges() {
        if (null != usedClusterMachines) {
            usedClusterMachines.setValue(0);
            usedClusterMachines.setValid(false);
        }
        if (null != usedHardwareMachines) {
            usedHardwareMachines.setValue(0);
            usedHardwareMachines.setValid(false);
        }
    }

    /**
     * Create meter-widget for monitoring panel.
     * 
     * @param parent
     *            parent composite on which the widgets are placed.
     */
    @SuppressWarnings("unused")
    private void createTank(final Composite parent) {

        Canvas tankCanvas = new Canvas(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tankCanvas.setLayoutData(gd);
        LightweightSystem lws = new LightweightSystem(tankCanvas);

        // Create widget
        final TankFigure tank = new TankFigure();

        // Init widget
        tank.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));

        tank.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));

        tank.setRange(-100, 100);
        tank.setLoLevel(-50);
        tank.setLoloLevel(-80);
        tank.setHiLevel(60);
        tank.setHihiLevel(80);
        tank.setMajorTickMarkStepHint(50);

        lws.setContents(tank);

        // Update the widget in another thread.
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        tank.setValue(Math.sin(counter++ / 10.0) * 100);
                    }
                });
            }
        }, 100, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets basic settings for meter figures.
     * 
     * @param figure
     *            the figure
     */
    private void configureMeterFigure(MeterFigure figure) {
        figure.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        figure.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));
        figure.setLoloColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_GREEN));
        figure.setLoColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_GREEN));
        figure.setHiColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_ORANGE));
        figure.setHihiColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
    }

    /**
     * Creates the meter figure for cluster machines. Modifies {@link #usedClusterMachines}.
     * 
     * @param meterCanvas
     *            the canvas for the meter
     * @return the system holding the meter
     */
    private LightweightSystem createClusterMachinesMeter(Canvas meterCanvas) {
        // Create Figure
        usedClusterMachines = new MeterFigure();
        configureMeterFigure(usedClusterMachines);
        IDecisionVariable machines = ModelAccess.findTopContainer(Configuration.HARDWARE,
                Configuration.HARDWARE.getProvidedTypes()[0]); // uhh
        Value value = machines.getValue();
        Map<String, Integer> groupCounts = new HashMap<String, Integer>();
        if (value instanceof ContainerValue) {
            ContainerValue cnt = (ContainerValue) value;
            for (int i = 0; i < cnt.getElementSize(); i++) {
                Value val = VariabilityModel.dereference(Configuration.HARDWARE.getConfiguration(), cnt.getElement(i));
                if (val instanceof CompoundValue) {
                    CompoundValue cmp = (CompoundValue) val;
                    String group = null;
                    int inc = 0;
                    Value name = cmp.getNestedValue(SLOT_NAME);
                    if (name instanceof StringValue) {
                        group = VariabilityModel.getHardwareGroup(((StringValue) name).getValue());
                    }
                    Value roleValue = cmp.getNestedValue("role");
                    if (null != group && roleValue instanceof EnumValue) {
                        EnumValue eValue = (EnumValue) roleValue;
                        if ("Worker".equals(eValue.getValue().getName())) {
                            inc = 1;
                        }
                    }
                    Integer actual = groupCounts.get(group);
                    if (null == actual) {
                        actual = inc;
                    } else {
                        actual = actual + inc;
                    }
                    groupCounts.put(group, actual);
                }
            }
        }

        int workerCount = 0;
        for (Integer entry : groupCounts.values()) {
            workerCount = Math.max(workerCount, entry);
        }

        usedClusterMachines.setRange(0, workerCount);
        usedClusterMachines.setValue(0);
        usedClusterMachines.setHiLevel(workerCount * 0.9);
        // meterFigure.setMajorTickMarkStepHint(50);

        LightweightSystem lws = new LightweightSystem(meterCanvas);
        lws.setContents(usedClusterMachines);
        return lws;
    }

    /**
     * Creates the meter figure for reconfigurable hardware machines. Modifies {@link #usedHardwareMachines}.
     * 
     * @param meterCanvas
     *            the canvas for the meter
     * @return the system holding the meter
     */
    private LightweightSystem createHardwareNodesMeter(Canvas meterCanvas) {
        // Create Figure
        usedHardwareMachines = new MeterFigure();
        configureMeterFigure(usedHardwareMachines);
        IDecisionVariable machines = ModelAccess.findTopContainer(Configuration.RECONFIG_HARDWARE,
                Configuration.RECONFIG_HARDWARE.getProvidedTypes()[0]); // uhh
        Value value = machines.getValue();
        int dfeCount = 0;
        if (value instanceof ContainerValue) {
            ContainerValue cnt = (ContainerValue) value;
            for (int i = 0; i < cnt.getElementSize(); i++) {
                Value val = VariabilityModel.dereference(Configuration.RECONFIG_HARDWARE.getConfiguration(),
                        cnt.getElement(i));
                if (val instanceof CompoundValue) {
                    CompoundValue cmp = (CompoundValue) val;
                    Value dfeValue = cmp.getNestedValue("numDFEs");
                    if (dfeValue instanceof IntValue) {
                        dfeCount += ((IntValue) dfeValue).getValue();
                    }
                }
            }
        }

        usedHardwareMachines.setRange(0, dfeCount);
        usedHardwareMachines.setValue(0);
        usedHardwareMachines.setHiLevel(dfeCount * 0.9);
        // meterFigure.setMajorTickMarkStepHint(50);

        LightweightSystem lws = new LightweightSystem(meterCanvas);
        lws.setContents(usedHardwareMachines);
        return lws;
    }

    /**
     * Creates the panel for monitoring some quality properties.
     * 
     * @param parent
     *            the parent panel
     * @return the created panel
     */
    private Composite createMonitoringPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE | SWT.CENTER);
        GridLayout layout = new GridLayout(1, false);
        // layout.marginBottom = 50;
        // layout.marginWidth = 50;
        panel.setLayout(layout);

        Control control = createMeter(panel);
        GridData data = new GridData(GridData.CENTER);
        data.widthHint = 600;
        data.heightHint = 200;
        // data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.CENTER;
        control.setLayoutData(data);
        return panel;
    }

    @Override
    public void setFocus() {
    }

    @Override
    public void handleDisconnectRequest(DisconnectRequest message) {
        enableButtons();
        invalidateGauges();
    }

    @Override
    public void handleMonitoringDataMessage(MonitoringDataMessage message) {
        String part = message.getPart();
        // not nice, but we do not put the QM.events dependency in here by now;
        // Names taken from resource descriptors /
        // monitoring layer system state
        Map<String, Double> observations = message.getObservations();

        String partName = message.getPart();
        Set<String> elementObservations = message.getObservations().keySet();
        
        deliveringObservables.put(partName, elementObservations);
        
        if (PART_INFRASTRUCTURE.equals(part)) {
            final Double usedMachines = observations.get(RESOURCEUSAGE_USED_MACHINES);
            final Double usedDfes = observations.get(RESOURCEUSAGE_USED_DFES);
            if (null != usedMachines || null != usedDfes) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (null != usedMachines) {
                            usedClusterMachines.setValue(usedMachines);
                        }
                        if (null != usedDfes) {
                            usedHardwareMachines.setValue(usedDfes);
                        }
                    }
                });
            }
        } else {
            ArrayList<PipelineTrace> traces = pipelineTraces.get(part);
            if (null != traces) {

                for (int i = 0; i < traces.size(); i++) {
                    PipelineTrace trace = traces.get(i);

                    trace.update(observations);
                }

            }

        }
    }

    @Override
    public void handleSwitchAlgorithmRequest(SwitchAlgorithmRequest message) {
        // server side - nothing to do
    }

    @Override
    public void handlePipelineMessage(PipelineMessage msg) {
        // server side - nothing to do
    }

    @Override
    public void handleAlgorithmChangedMessage(AlgorithmChangedMessage msg) {
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage msg) {
    }

    @Override
    public void infrastructureConnectionStateChanged(boolean hasConnection) {
        usedClusterMachines.setValid(hasConnection);
        usedHardwareMachines.setValid(hasConnection);
        if (hasConnection) {
            for (ArrayList<PipelineTrace> traces : pipelineTraces.values()) {
                for (int i = 0; i < traces.size(); i++) {

                    PipelineTrace actualTrace = traces.get(i);
                    actualTrace.clearTrace();
                }

            }
        } else {
            usedClusterMachines.setValue(0);
            usedHardwareMachines.setValue(0);
        }
        enableButtons();
    }

    @Override
    public void handleChangeParameterRequest(ChangeParameterRequest<?> arg0) {
        // server side - nothing to do
    }

    @Override
    public void handleExecutionResponseMessage(ExecutionResponseMessage arg0) {
        // may show executed commands
    }

    @Override
    public void handleLoggingFilterRequest(LoggingFilterRequest arg0) {
        // server side - nothing to do
    }

    @Override
    public void handleLoggingMessage(LoggingMessage arg0) {
        // server side - nothing to do
    }

    @Override
    public void handleInformationMessage(InformationMessage arg0) {
        // show along with executed commands
    }

    @Override
    public void handlePipelineStatusRequest(PipelineStatusRequest arg0) {
    }

    @Override
    public void handlePipelineStatusResponse(PipelineStatusResponse arg0) {
    }

    @Override
    public void handleUpdateCloudResourceMessage(UpdateCloudResourceMessage arg0) {
    }

    @Override
    public void handleCloudPipelineMessage(CloudPipelineMessage arg0) {
    }

    @Override
    public void handleReplayMessage(ReplayMessage arg0) {
    }

    @Override
    public void handleConfigurationChangeMessage(ConfigurationChangeRequest arg0) {
    }

    @Override
    public void handleResourceChangeMessage(ResourceChangeRequest arg0) {
    } 
}
