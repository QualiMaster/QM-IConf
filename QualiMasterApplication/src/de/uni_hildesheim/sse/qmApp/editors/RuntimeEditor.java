package de.uni_hildesheim.sse.qmApp.editors;

import static eu.qualimaster.easy.extension.QmConstants.SLOT_NAME;
import static eu.qualimaster.easy.extension.QmObservables.PART_INFRASTRUCTURE;
import static eu.qualimaster.easy.extension.QmObservables.RESOURCEUSAGE_USED_DFES;
import static eu.qualimaster.easy.extension.QmObservables.RESOURCEUSAGE_USED_MACHINES;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
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
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;
import eu.qualimaster.adaptation.external.UpdateCloudResourceMessage;
import eu.qualimaster.easy.extension.QmConstants;
import eu.qualimaster.easy.extension.QmObservables;
import net.ssehub.easy.varModel.confModel.IDecisionVariable;
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
    private static final int PIPELINE_DISPLAY_BUFFER_SIZE = 50;
    private static final int PIPELINE_DISPLAY_DELAY = 100;
    
    //private static final String QUALITY_PARAMETERS = "qualityParameters";
    
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
    private Table observablesTable;
    private Table savedObservablesTable;

    private List<PipelineGraphColoringWrapper> pipelinesToDisplayInTableWithObservable =
            new ArrayList<PipelineGraphColoringWrapper>();
    
    /**
     * Returns a point style of <code>count</code> modulo the actual point styles.
     * 
     * @param count
     *            the count to determine the style
     * @return the point style
     */
    private static PointStyle getPointStyleMod(int count) {
        PointStyle[] styles = PointStyle.values();
        return styles[count % styles.length];
    }

    /**
     * Represents a pipeline trace, i.e., an observation buffer and an XY-graph trace.
     * 
     * @author Holger Eichelberger
     */
    private static class PipelineTrace {
        private CircularBufferDataProvider dataProvider;
        private Trace trace;
        private String observable;

        /**
         * Creates the trace.
         * 
         * @param label
         *            the label
         * @param xAxis
         *            the x-axis to display on
         * @param yAxis
         *            the y-axis to display on
         * @param style
         *            the display style
         * @param observable
         *            the observable to draw
         */
        private PipelineTrace(String label, Axis xAxis, Axis yAxis, PointStyle style, String observable) {
            this.observable = observable;

            // create a trace data provider, which will provide the data to the
            // trace.
            dataProvider = new CircularBufferDataProvider(true);
            dataProvider.setBufferSize(PIPELINE_DISPLAY_BUFFER_SIZE);
            dataProvider.setUpdateDelay(PIPELINE_DISPLAY_DELAY);

            // create the latency trace
            trace = new Trace(label, xAxis, yAxis, dataProvider);
            trace.setDataProvider(dataProvider);
            //trace.setPointStyle(style);
        }

        /**
         * Set the color of this trace.
         * @param color Color to set.
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
     * Create more controls for the runtime-editor.
     * 
     * @param outer
     *            outer composite which constains everything the runtime-editor has to offer.
     * @param innerLeft
     *            left part of the view.
     * @param innerRight
     *            right side of the view.
     * @param parent
     *            parent composite.
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
  
        parent.addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent exc) {
                //saveTreeLocally();
            }
        });
        
        // Create all the controls needed for the customizing of graphs.
        createOptionsPanel(bottom, innerLeftBottom);

        // Create the controls which will be needed for displaying graphs.
        createMonitoringPanel(innerRight);

        addTabFolder(innerLeftTop, topButtons, parent);
        
        //loadItemsLocally();
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
     * @param tabFolder The tabfolder which contains the graphs.
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
//                insertMark++;
//
//                RuntimeGraphTabItem newTabItem = new RuntimeGraphTabItem(tabFolder, SWT.NONE);
//                newTabItem.setText("New Tab");
//
//                Control control = createGraphPanel(tabFolder);
//                GridData data = new GridData(GridData.FILL_BOTH);
//                data.grabExcessHorizontalSpace = true;
//                data.horizontalAlignment = SWT.FILL;
//                data.horizontalSpan = 4;
//                control.setLayoutData(data);
//
//                Infrastructure.registerListener(newTabItem);
//                
//                newTabItem.setControl(control);
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
        xyGraph.primaryXAxis.setDateEnabled(true);
        xyGraph.primaryYAxis.setShowMajorGrid(true);
        //xyGraph.primaryYAxis.setFormatPattern("00000");
        xyGraph.primaryYAxis.setAutoScale(true);
        xyGraph.primaryYAxis.setRange(0, 3);
        
        xyGraph.primaryYAxis
                .setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));
        String pipelineString = pipelineActivityString;
        StringBuilder pipelineTitle = new StringBuilder(pipelineString);
        String observables = observablesString;
        StringBuilder observableTitle = new StringBuilder(observables);
        
        //Go through all saved info and create the corresponding traces.
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
        
        //set some contant titels, such as the achses captions.
        PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(0);
        pipelineTitle.append(wrapper.getPipelineParent());
        observableTitle.append(wrapper.getObs() + "...");
 
//        if (pipelineTitle.charAt(pipelineTitle.length()) == ',') {
//            pipelineTitle.setLength(pipelineTitle.length() - 1);
//        }
        
        pipelineTitle.append(")"); 
        observableTitle.append(")");
        
        Font titleFont = new Font(Display.getCurrent(), "Arial", 11, SWT.BOLD);
        Font achseFont = new Font(Display.getCurrent(), "Arial", 8, SWT.BOLD);      
        xyGraph.setTitleFont(titleFont);
        xyGraph.setTitle(pipelineTitle.toString());
        xyGraph.primaryYAxis.setFont(achseFont);
        xyGraph.primaryYAxis.setTitle(observableTitle.toString());
        
        lws.setContents(xyGraph);
        return xyGraphCanvas;
    }

    /**
     * Create the traces which are specified by the selected items in the trees and tables.
     * @param pipElementName name of the currently selected element from the tree.
     * @param pipelineName name of the possible parents name. (Pipeline name)
     * @param xyGraph the {@link XYGraph} to draw upon.
     * @param color Color for this specific pipeline.
     * @param obs the observable to draw.
     */
    private void createObservableTraces(String pipElementName, String pipelineName, XYGraph xyGraph,
            Color color, String obs) {
        
        ArrayList<PipelineTrace> pipCollection = new ArrayList<PipelineTrace>();
        
        ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();
                
        String observalbeForTrace = determineQMObervable(obs, allPossibleObservables);

        if (observalbeForTrace != null) {
            
            //Check if pipeline already exists
            if (!checkIfPipelineIsExisting(pipelineTraces, pipElementName, observalbeForTrace)) {
            
                String name = pipelineName + ":" + pipElementName;
                PipelineTrace pTrace = new PipelineTrace(name,
                                xyGraph.primaryXAxis, xyGraph.primaryYAxis, getPointStyleMod(pipelineTraces.size()),
                                    observalbeForTrace);
                
                pTrace.setColor(color);             
                pipCollection.add(pTrace);
                
                xyGraph.addTrace(pTrace.getTrace());
                
                if (pipelineTraces != null) {
                   
                    pipelineTraces.put(name, pipCollection);
                }
            }  
        } 
    }
    /**
     * Create the traces which are specified by the selected items in the trees and tables.      
     * @param pipelineName name of the possible parents name. (Pipeline name)
     * @param xyGraph the {@link XYGraph} to draw upon.
     * @param color Color for this specific pipeline.
     * @param obs the observable to draw.
     */
    private void createObservableTraces(String pipelineName, XYGraph xyGraph, Color color, String obs) {
        
        ArrayList<PipelineTrace> pipCollection = new ArrayList<PipelineTrace>();
      
        ArrayList<String> allPossibleObservables = QmObservables.getAllObservables();
                
        String observalbeForTrace = determineQMObervable(obs, allPossibleObservables);

        if (observalbeForTrace != null) {
            
            if (!checkIfPipelineIsExisting(pipelineTraces, pipelineName, observalbeForTrace)) {
                checkIfPipelineIsExisting(pipelineTraces, pipelineName, observalbeForTrace);
                
                PipelineTrace pTrace = new PipelineTrace(pipelineName + " (" + observalbeForTrace + ")",
                        xyGraph.primaryXAxis, xyGraph.primaryYAxis,
                                getPointStyleMod(pipelineTraces.size()), observalbeForTrace);
                
                pTrace.setColor(color);        
                pipCollection.add(pTrace);    
                
                xyGraph.addTrace(pTrace.getTrace());
            }
          
            pipelineTraces.put(pipelineName, pipCollection);
        }
        
    }
    
    /**
     * Check if a pipeline with a given name already exists.
     * @param pipelineTraces map of all traces.
     * @param pipelineName name of new pipeline.
     * @param observalbeForTrace observable for the trace.
     * @return toReturn true if pipeline already esits/ false otherwise.
     */
    private boolean checkIfPipelineIsExisting(Map<String, ArrayList<PipelineTrace>> pipelineTraces, String pipelineName,
            String observalbeForTrace) {
        
        boolean toReturn = false;
        
        Iterator<ArrayList<PipelineTrace>> iterator = pipelineTraces.values().iterator();
        for (int i = 0; i < pipelineTraces.values().size(); i++) {
            
            ArrayList<PipelineTrace> traces = iterator.next();
            
            for (int j = 0; j < traces.size(); j++) {
                
                PipelineTrace trace = traces.get(j);
                
                if (trace.trace.getName().contains(pipelineName)
                        && trace.trace.getName().contains(observalbeForTrace)) {
                    toReturn = true;
                }
            }
            
        }
        
        return toReturn;
        
    }

    /**
     * Determine the QM observables with given String.
     * @param observableName String representation of observable. 
     * @param allPossibleObservables all possible QMObservables.
     * @return toReturn the correct QMObservable.
     */
    private String determineQMObervable(String observableName, ArrayList<String> allPossibleObservables) {
        
        String toReturn = null;
        
        for (int j = 0; j < allPossibleObservables.size(); j++) {
            
            String currentObservable = allPossibleObservables.get(j);
            
            if (observableName.trim().toLowerCase().contains(currentObservable.trim().toLowerCase())
                    || currentObservable.trim().toLowerCase().contains(observableName.trim().toLowerCase())) {
                toReturn = currentObservable;
            }
        }
        return toReturn;
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

        net.ssehub.easy.varModel.confModel.Configuration config = Configuration.OBSERVABLES.getConfiguration();

        Iterator<IDecisionVariable> iter = config.iterator();

        while (iter.hasNext()) {
            Object object = iter.next();

            if (object instanceof SetVariable) {
                collectQualityParameters(object);
            }
        }

        //Add listener for user action. When the user selects pipeline-elements and/or observalbes,
        //we have to react.
        treeViewerPipelineChooser.addCheckStateListener(new ICheckStateListener() {
            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                CheckboxTreeViewer treeViewer = (CheckboxTreeViewer) event.getSource();

                PipelineGraphColoringWrapper object = (PipelineGraphColoringWrapper) event.getElement();
                for (int i = 0; i < treeViewer.getTree().getItemCount(); i++) {
                    TreeItem topLevelItem = treeViewer.getTree().getItem(i);
                    if (topLevelItem.getText().equals(object.getElemName())) {

                        PipelineNodeType type = object.getType();

                        PipelinesRuntimeUtils.INSTANCE.setObservablesTableSelections(observablesTable, type);
                        // Check for observalbes and disable some
                    } else {
                        topLevelItem.setChecked(false);
                    }

                    for (int j = 0; j < topLevelItem.getItemCount(); j++) {
                        TreeItem item = treeViewer.getTree().getItem(i).getItem(j);
                        if (item.getText().equals(object.getElemName())) {
                            item.setChecked(true);

                            PipelineNodeType type = object.getType();

                            PipelinesRuntimeUtils.INSTANCE.setObservablesTableSelections(observablesTable, type);
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
        button4.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                //When the "Save"-Button is selected, save the selected pipeline-elements and observalbes
                //by creating objects, which wrap up all info.
                saveSelections();
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent evt) {
            }
        });
        GridData data = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
        button4.setLayoutData(data);

        savedObservablesTable = new Table(innerLeftTop, SWT.BORDER);
        savedObservablesTable.setLinesVisible(false);
        savedObservablesTable.setHeaderVisible(true);
        TableColumn column6 = new TableColumn(savedObservablesTable, SWT.NONE);
        column6.setText("chosen");
        column6.setWidth(200);
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        savedObservablesTable.setLayoutData(data);

        Menu menu = new Menu(savedObservablesTable.getShell(), SWT.POP_UP);   
        savedObservablesTable.setMenu(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Delete Selection");
        item.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                performDeletion();
            }  
        });
        
        treeViewerColorChooser = new Table(innerLeftTop, SWT.MULTI | SWT.BORDER);
        treeViewerColorChooser.setLinesVisible(false);
        treeViewerColorChooser.setHeaderVisible(true);

        String[] titles2 = {"Pipeline", "Choose", "Color"};
        for (int i = 0; i < titles2.length; i++) {
            TableColumn column5 = new TableColumn(treeViewerColorChooser, SWT.NONE);
            column5.setText(titles2[i]);
            column5.setWidth(66);
        }
        data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewerColorChooser.setLayoutData(data);

        Button button = new Button(innerLeftBottomRight, SWT.NONE);
        button.setText("Draw");

        data = new GridData(SWT.LEFT, SWT.BOTTOM, true, false);
        button.setLayoutData(data);

        button.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                //Draw the graph with the collected info so far.
                drawGraphsInNewTab();
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

        //Delete a certain pipelineTrace.
        TableItem tableItem = savedObservablesTable.getSelection()[0];

        Collection<ArrayList<PipelineTrace>> allTraces = pipelineTraces.values();
     
        for (int i = 0; i < allTraces.size(); i++) {
            Iterator<ArrayList<PipelineTrace>> iterator = allTraces.iterator();
          
            if (iterator.hasNext()) {
                ArrayList<PipelineTrace> traces = (ArrayList<PipelineTrace>) iterator.next();
             
                for (int j = 0; j < traces.size(); j++) {
                    PipelineTrace trace = traces.get(j);
                  
                    String name = trace.getTrace().getName();
                    name = name.substring(0, name.indexOf("("));
                    String obs = trace.observable.toLowerCase();
                 
                    String item = tableItem.getText();
                    if (item.contains(name)) {
                        if (item.toLowerCase().contains(obs)) {
                            //trace.clearTrace();
                            traces.remove(trace);
                        }
                    }
                }
            }
        }

        pipelinesToDisplayInTableWithObservable.remove(savedObservablesTable.getSelectionIndices());
        savedObservablesTable.remove(savedObservablesTable.getSelectionIndices());
             
//        pipelineTraces.remove(pipelineTraces.keySet().remove(pipelineTraces.keySet().toArray()[
//                savedObservablesTable.getSelectionIndex()]));
    }

    /**
     * Collect the qualityParameters from the ivml-configuration, so they can be mapped upn pipeline-elements.
     * 
     * @param object
     *            Set which holds the observalbes.
     */
    private void collectQualityParameters(Object object) {

        SetVariable set = (SetVariable) object;
        String name = set.getDeclaration().getName();

        if (name.equals(QmConstants.VAR_OBSERVABLES_QUALITYPARAMS)) {

            ConstantValue vaule = (ConstantValue) set.getDeclaration().getDefaultValue();
            ContainerValue container = (ContainerValue) vaule.getConstantValue();
            
            Collection<Value> observalbeSet = new ArrayList<Value>();
            
            for (int i = 0; i < container.getElementSize(); i++) {

                CompoundValue value = (CompoundValue) container.getElement(i);
                Compound cType = (Compound) value.getType();
                
                for (int j = 0; j < cType.getInheritedElementCount(); j++) {
                    DecisionVariableDeclaration slotDecl = cType.getInheritedElement(j);
                    Value nestedValue = value.getNestedValue(slotDecl.getName());
                    
                    observalbeSet.add(nestedValue);
                }
            }
            
            Object[] valueArray = observalbeSet.toArray();

            for (int k = 0; k < valueArray.length; k++) {
                Object obj = valueArray[k];

                if (obj != null && obj instanceof StringValue) {
                    StringValue stringValue = (StringValue) obj;

                    String observalbeName = stringValue.getValue();

                    TableItem treeItem = new TableItem(observablesTable, 0);
                    treeItem.setText(observalbeName);

                    PipelinesRuntimeUtils.INSTANCE.getBackupObservableItem().add(observalbeName);
                }
            }
            
        }
    }

    /**
     * Save the users selections concerning the observables.
     */
    private void saveSelections() {

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

                PipelineGraphColoringWrapper newWrapper = new PipelineGraphColoringWrapper(name, type, pipParent, var,
                        decl);

                newWrapper.setObservable(observableName);

                pipelinesToDisplayInTableWithObservable.add(newWrapper);

                if (!PipelinesRuntimeUtils.INSTANCE.pipelineObservableCombinationIsNotExisting(savedObservablesTable,
                        pipParent, name, observableName)) {
                    performSelection(pipParent, name, observableName);
                }
            }
        }
    }

    /**
     * Draw the graphs.
     */
    private void drawGraphsInNewTab() {
    

        final RuntimeGraphTabItem newTabItem = new RuntimeGraphTabItem(tabFolder, SWT.NONE);
        newTabItem.setText("New Tab");

        Control control = createGraphPanel(tabFolder);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.grabExcessHorizontalSpace = true;
        data.horizontalAlignment = SWT.FILL;
        data.horizontalSpan = 4;
        control.setLayoutData(data);

        Infrastructure.registerListener(newTabItem);
        
        newTabItem.setControl(control);
        
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
     *            The label which is showing the actuel color.
     * @param color2
     *            The chosen color to set.
     */
    private void adjustChosenColor(Label label, Color color2) {
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

            TableItem[] items = treeViewerColorChooser.getItems();

            for (int i = 0; i < items.length; i++) {

                TableItem item = treeViewerColorChooser.getItem(i);
                String pipelineName = item.getText(0);

                for (int j = 0; j < pipelinesToDisplayInTableWithObservable.size(); j++) {
                    PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(j);

                    String wrapperName = wrapper.getElemName();

                    if (wrapperName.equals(pipelineName)) {
                        wrapper.setColor(color);
                    }
//                    System.out.println(wrapper.getElemName());
//                    System.out.println(wrapper.getObs());
//                    System.out.println(wrapper.getPipelineParent());
//                    System.out.println(wrapper.getColor());
//                    System.out.println(wrapper.getDecl());
//                    System.out.println(wrapper.getVar());
                }

            }
        }
    }

    /**
     * Get the sleected color for the although selected pipeline and adjust the table.
     * 
     * @param pipParent
     *            Given pipeline.
     * @param name
     *            Given name where the color should be set.
     * @param observableName
     *            name of the observable.
     */
    private void performSelection(String pipParent, String name, String observableName) {
        // get parent pipeline checkbox

        TableItem tableItem = new TableItem(savedObservablesTable, SWT.NONE);

        if (!pipParent.equals(name)) {
            tableItem.setText(pipParent + ": " + name + ": " + observableName);
        } else {
            tableItem.setText(pipParent + ": " + observableName);
        }

        boolean isExisting = PipelinesRuntimeUtils.INSTANCE.pipNotExisting(treeViewerColorChooser, pipParent);

        if (!isExisting) {

            TableItem treeItem0 = new TableItem(treeViewerColorChooser, 0);
            treeItem0.setText(pipParent);

            TableItem[] items = treeViewerColorChooser.getItems();
            final Color color = new Color(treeViewerColorChooser.getShell().getDisplay(), new RGB(124, 252, 0));

            for (int i = 0; i < items.length; i++) {
                TableEditor editor = new TableEditor(treeViewerColorChooser);

                editor = new TableEditor(treeViewerColorChooser);
                Button button = new Button(treeViewerColorChooser, SWT.NONE);
                button.setText("Color");
                button.pack();

                editor.minimumWidth = button.getSize().x;
                editor.horizontalAlignment = SWT.LEFT;
                editor.setEditor(button, items[i], 1);

                editor = new TableEditor(treeViewerColorChooser);
                final Label label = new Label(treeViewerColorChooser, SWT.BORDER);
                label.setText("           ");
                label.pack();
                editor.minimumWidth = label.getSize().x;
                editor.horizontalAlignment = SWT.LEFT;
                label.setBackground(color);

                button.addSelectionListener(new SelectionAdapter() {
                    public void widgetSelected(SelectionEvent event) {

                        adjustChosenColor(label, color);
                    }
                });
                editor.setEditor(label, items[i], 2);
            }
        }
        savedObservablesTable.redraw();
    }

    @Override
    public void dispose() {
        Infrastructure.unregisterDispatcher(this);
        Infrastructure.unregisterListener(this);
    
    }

    /**
     * Save the List of {@link TreeItem}. If the user is offline, the tree can
     * be loaded from the saved file.
     */
//    private void saveTreeLocally() {
//        CSVWriter writer = null;
//        try {
//            
//            writer = new CSVWriter(getItemsFile(), true, ", ", "\n");
//            //save selected elements
//            
//            String toWrite = "";
//            for (int i = 0; i < pipelinesToDisplayInTableWithObservable.size(); i++) {
//                PipelineGraphColoringWrapper wrapper = pipelinesToDisplayInTableWithObservable.get(i);    
//
//                toWrite += wrapper.getElemName() + ",";
//                toWrite += wrapper.getType() + ",";
//                toWrite += wrapper.getPipelineParent() + ",";
//                toWrite += wrapper.getVar() + ",";
//                toWrite += wrapper.getDecl() + ",";
//                toWrite += wrapper.getObs() + ",";
//                
//                
//                //writer.writeLine(item.getText());
//                
//            }
//
//            writer.writeLine(toWrite);
//            
//        } finally {
//            if (null != writer) {
//                writer.close();
//            }
//        }
//    }

    /**
     * Load the serialized tree. Use the extracted list to set the treeviewer
     * input.
     */
//    private void loadItemsLocally() {
//    
//        try (BufferedReader br = new BufferedReader(new FileReader(getItemsFile()))) {
//            StringBuilder sb = new StringBuilder();
//            String line = br.readLine();
//
//            while (line != null) {
//                
//                String[] all = line.split(",");
//
//                for (int i = 0; i < all.length; i++) {
//                    String elemName = all[0];
//                    String type = all[1];
//                    String pipelineParent = all[2];
//                    String variable = all[3];
//                    String declaration = all[4];
//                    String observable = all[5];
//                    PipelineNodeType typeToSet;
//                    
//                    PipelineNodeType types = PipelineNodeType.Source;
//
//                    switch (type) { 
//                    case "Source": 
//                        typeToSet = PipelineNodeType.Source;
//                        break;
//                    case "Sink": 
//                        typeToSet = PipelineNodeType.Sink;
//                        break;
//                    case "FamilyElement":
//                        typeToSet = PipelineNodeType.FamilyElement;
//                        break;
//                    case "DataManagementElement": 
//                        typeToSet = PipelineNodeType.DataManagementElement;
//                        break;
//                    case "Pipeline": 
//                        typeToSet = PipelineNodeType.Pipeline;
//                        break;
//                    case "Flow": 
//                        typeToSet = PipelineNodeType.Flow;
//                        break;
//                    case "ProcessingElement": 
//                        typeToSet = PipelineNodeType.ProcessingElement;
//                        break;
//                    default: 
//                        typeToSet = PipelineNodeType.Source;
//                        break;
//                    }
//
//                    PipelineGraphColoringWrapper newWrapper = new PipelineGraphColoringWrapper(elemName,
//                            typeToSet, pipelineParent, declaration);
//                    newWrapper.setObservable(observable);
//                }
//                
//                pipelinesToDisplayInTableWithObservable.add(newWrapper);
//                 
//                TableItem item = new TableItem(savedObservablesTable, SWT.NONE);
//                item.setText(line);     
//            }
//            
//        }
//        
//        CSVWriter writer = null;
//        try {
//            
//            writer = new CSVWriter(getItemsFile(), true, ", ", "\n");
//            //save selected elements
//            for (int i = 0; i < savedObservablesTable.getItemCount(); i++) {
//                TableItem item = savedObservablesTable.getItem(i);    
//
//                writer.writeLine(item.getText());
//                
//            }
//
//        } finally {
//            if (null != writer) {
//                writer.close();
//            }
//        }
//    }

    /**
     * Returns the file for storing the Maven tree persistently (offline use).
     * 
     * @return the file
     */
//    private File getItemsFile() {
//        return new File(WorkspaceUtils.getMetadataFolder(), "runtimeSavedItems.csv");
//    }
    
    /**
     * Returns the file for storing the Maven tree persistently (offline use).
     * 
     * @return the file
     */
//    private File getWrapperFile() {
//        return new File(WorkspaceUtils.getMetadataFolder(), "runtimeSavedWrappers.ser");
//    }
    
    /**
     * Enables or disables the buttons on this editor.
     */
    private void enableButtons() {
        // boolean connected = Infrastructure.isConnected();
    }

    /**
     * Creates the connection panel with the settings for the QM infrastructure.
     * 
     * @param parent
     *            the parent panel
     */
    // private void createConnectionPanel(Composite parent) {
    // Composite panel = new Composite(parent, SWT.NONE);
    //
    // GridLayout layout = new GridLayout(2, false);
    // panel.setLayout(layout);
    // }

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
    private void createGauche(final Composite parent) {
        // Canvas gaugeCanvas = new Canvas(parent, SWT.BORDER);
        // GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        // gaugeCanvas.setLayoutData(gd);
        //
        // LightweightSystem lws = new LightweightSystem(gaugeCanvas);
        // // Create widgets
        //
        // final GaugeFigure gauge = new GaugeFigure();
        // gauge.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(0,
        // 0, 0));
        // gauge.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(255,
        // 255, 255));
        // lws.setContents(gauge);
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

        // createGauche(panel);
        // createTank(panel);
        // control = createLatencyGraph(panel);
        // data = new GridData(GridData.FILL_BOTH);
        // //data.verticalAlignment = GridData.FILL;
        // //data.horizontalAlignment = GridData.FILL;
        // control.setLayoutData(data);
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
            // if (part.lastIndexOf(":") < 0) { // it's a pipeline
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
    
}
