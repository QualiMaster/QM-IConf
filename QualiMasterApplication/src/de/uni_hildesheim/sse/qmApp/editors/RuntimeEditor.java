package de.uni_hildesheim.sse.qmApp.editors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.csstudio.swt.widgets.figures.GaugeFigure;
import org.csstudio.swt.widgets.figures.MeterFigure;
import org.csstudio.swt.widgets.figures.TankFigure;
import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.figures.Trace.PointStyle;
import org.csstudio.swt.xygraph.figures.XYGraph;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.EnumValue;
import de.uni_hildesheim.sse.model.varModel.values.IntValue;
import de.uni_hildesheim.sse.model.varModel.values.StringValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import de.uni_hildesheim.sse.qmApp.runtime.IInfrastructureListener;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure;
import de.uni_hildesheim.sse.qmApp.runtime.Infrastructure.IClientDispatcher;
import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ChangeParameterRequest;
import eu.qualimaster.adaptation.external.DisconnectRequest;
import eu.qualimaster.adaptation.external.ExecutionResponseMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.InformationMessage;
import eu.qualimaster.adaptation.external.LoggingFilterRequest;
import eu.qualimaster.adaptation.external.LoggingMessage;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmRequest;
import eu.qualimaster.easy.extension.QmObservables;

import static eu.qualimaster.easy.extension.QmConstants.*;
import static eu.qualimaster.easy.extension.QmObservables.*;

/**
 * The preliminary editor for runtime settings. Will be replaced by an appropriate
 * generic version later in the project.
 * 
 * @author Holger Eichelberger
 */
public class RuntimeEditor extends EditorPart implements IClientDispatcher, IInfrastructureListener {

    private static int counter = 0;
    
    private static final int PIPELINE_DISPLAY_BUFFER_SIZE = 50;
    private static final int PIPELINE_DISPLAY_DELAY = 100;
    
    private MeterFigure usedClusterMachines;
    private MeterFigure usedHardwareMachines;
    //private CircularBufferDataProvider pipelineLatencyDataProvider;
    //private CircularBufferDataProvider pipelineThroughputDataProvider;
    private Map<String, PipelineTrace> pipelineTraces = new HashMap<String, PipelineTrace>();

    /**
     * Returns a point style of <code>count</code> modulo the actual point styles.
     * 
     * @param count the count to determine the style
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
        private int observationTime = 0;

        /**
         * Creates the trace.
         * 
         * @param label the label
         * @param xAxis the x-axis to display on
         * @param yAxis the y-axis to display on
         * @param style the display style
         * @param observable the observable to draw
         */
        private PipelineTrace(String label, Axis xAxis, Axis yAxis, PointStyle style, String observable) {
            this.observable = observable;
            
            //create a trace data provider, which will provide the data to the trace.
            dataProvider = new CircularBufferDataProvider(true);
            dataProvider.setBufferSize(PIPELINE_DISPLAY_BUFFER_SIZE);
            dataProvider.setUpdateDelay(PIPELINE_DISPLAY_DELAY);
            
            //create the latency trace
            trace = new Trace(label, xAxis, xAxis, dataProvider);
            trace.setDataProvider(dataProvider);
            trace.setPointStyle(style);
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
         * @param observations the observations to take the value from 
         */
        private void update(Map<String, Double> observations) {
            final Double observation = observations.get(observable);
            if (null != observation) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        dataProvider.addSample(new Sample(observationTime, observation.doubleValue()));
                        observationTime++;
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

    @Override
    public void createPartControl(Composite parent) {
        GridLayout layout = new GridLayout(2, false);
        parent.setLayout(layout);
        createConnectionPanel(parent);
        Composite panel = createMonitoringPanel(parent);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        panel.setLayoutData(data);
        Infrastructure.registerDispatcher(this);
        Infrastructure.registerListener(this);
        enableButtons();
    }
    
    @Override
    public void dispose() {
        Infrastructure.unregisterDispatcher(this);
        Infrastructure.unregisterListener(this);
    }

    /**
     * Enables or disables the buttons on this editor.
     */
    private void enableButtons() {
        //boolean connected = Infrastructure.isConnected();
    }

    /**
     * Creates the connection panel with the settings for the QM infrastructure.
     * 
     * @param parent the parent panel
     */
    private void createConnectionPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        panel.setLayout(layout);
    }
    
    /**
     * Returns the selected element name of a combo.
     * 
     * @param combo the combo to return the selected for
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
     * @param parent parent composite on which the widgets are placed.
     * @return the created control
     */
    private Control createMeter(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(2, true));
        
        Canvas clusterCanvas = new Canvas(panel, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.heightHint = 200;
        data.widthHint = 300;
        clusterCanvas.setLayoutData(data);

        Canvas hardwareCanvas = new Canvas(panel, SWT.BORDER);
        data = new GridData(GridData.FILL_VERTICAL);
        data.heightHint = 200;
        data.widthHint = 300;
        hardwareCanvas.setLayoutData(data);
        
        Label label = new Label(panel, SWT.CENTER);
        label.setText("used nodes");
        data = new GridData();
        data.widthHint = 300;
        label.setLayoutData(data);

        label = new Label(panel, SWT.CENTER);
        label.setText("used DFEs");
        data = new GridData();
        data.widthHint = 300;
        label.setLayoutData(data);

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
     * @param parent parent composite on which the widgets are placed.
     */
    @SuppressWarnings("unused")
    private void createGauche(final Composite parent) {
        Canvas gaugeCanvas = new Canvas(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        gaugeCanvas.setLayoutData(gd);

        LightweightSystem lws = new LightweightSystem(gaugeCanvas);
        //Create widgets

        final GaugeFigure gauge = new GaugeFigure();
        gauge.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(0, 0, 0));
        gauge.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        lws.setContents(gauge);
    }
    
    /**
     * Create meter-widget for monitoring panel.
     * @param parent parent composite on which the widgets are placed.
     */
    @SuppressWarnings("unused")
    private void createTank(final Composite parent) {
    
        Canvas tankCanvas = new Canvas(parent, SWT.BORDER);
        GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
        tankCanvas.setLayoutData(gd);
        LightweightSystem lws = new LightweightSystem(tankCanvas);
   
        //Create widget
        final TankFigure tank = new TankFigure();
      
        //Init widget
        tank.setBackgroundColor(
                XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
   
        tank.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));
   
        tank.setRange(-100, 100);
        tank.setLoLevel(-50);
        tank.setLoloLevel(-80);
        tank.setHiLevel(60);
        tank.setHihiLevel(80);
        tank.setMajorTickMarkStepHint(50);
   
        lws.setContents(tank);
   
        //Update the widget in another thread.
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
     * Creates the meter figure for cluster machines. Modifies {@link #usedClusterMachines}.
     * 
     * @param meterCanvas the canvas for the meter
     * @return the system holding the meter
     */
    private LightweightSystem createClusterMachinesMeter(Canvas meterCanvas) {
        //Create Figure
        usedClusterMachines = new MeterFigure();
        usedClusterMachines.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        usedClusterMachines.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));
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
        //meterFigure.setLoLevel(20);
        //meterFigure.setLoloLevel(40);
        //meterFigure.setHiLevel(60);
        //meterFigure.setHihiLevel(80);
        //meterFigure.setMajorTickMarkStepHint(50);

        LightweightSystem lws = new LightweightSystem(meterCanvas);
        lws.setContents(usedClusterMachines);
        return lws;
    }

    /**
     * Creates the meter figure for reconfigurable hardware machines. Modifies {@link #usedHardwareMachines}.
     * 
     * @param meterCanvas the canvas for the meter
     * @return the system holding the meter
     */
    private LightweightSystem createHardwareNodesMeter(Canvas meterCanvas) {
        //Create Figure
        usedHardwareMachines = new MeterFigure();
        usedHardwareMachines.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        usedHardwareMachines.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));
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
        //meterFigure.setLoLevel(20);
        //meterFigure.setLoloLevel(40);
        //meterFigure.setHiLevel(60);
        //meterFigure.setHihiLevel(80);
        //meterFigure.setMajorTickMarkStepHint(50);

        LightweightSystem lws = new LightweightSystem(meterCanvas);
        lws.setContents(usedHardwareMachines);
        return lws;
    }

    /**
     * Create meter-widget for monitoring panel.
     * @param parent parent composite on which the widgets are placed.
     * @return the created control
     */
    private Control createLatencyGraph(final Composite parent) {

        Canvas xyGraphCanvas = new Canvas(parent, SWT.BORDER);
        LightweightSystem lws = new LightweightSystem(xyGraphCanvas);
        
        //create a new XY Graph.
        XYGraph xyGraph = new XYGraph();
        xyGraph.setTitle("Pipeline activitiy");
        xyGraph.primaryXAxis.setShowMajorGrid(true);
        xyGraph.primaryXAxis.setTitle("execution time");
        xyGraph.primaryXAxis.setAutoScale(true);
        //xyGraph.primaryXAxis.setDateEnabled(true);
        
        xyGraph.primaryYAxis.setShowMajorGrid(true);
        xyGraph.primaryYAxis.setTitle("throughput (items/s)");
        xyGraph.primaryYAxis.setAutoScale(true);
        xyGraph.primaryYAxis.setForegroundColor(
            XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));

        /*Axis throughputAxis = new Axis("throughput (items)", true);
        throughputAxis.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
        throughputAxis.setTickLableSide(LabelSide.Secondary);
        throughputAxis.setAutoScale(true);
        xyGraph.addAxis(throughputAxis);*/
        
        IDecisionVariable actPipelines = ModelAccess.findTopContainer(Configuration.INFRASTRUCTURE, 
            Configuration.INFRASTRUCTURE.getProvidedTypes()[0]); // uhh
        Value value = actPipelines.getValue();
        if (value instanceof ContainerValue) {
            ContainerValue cnt = (ContainerValue) value;
            for (int i = 0; i < cnt.getElementSize(); i++) {
                Value pip = VariabilityModel.dereference(Configuration.INFRASTRUCTURE.getConfiguration(), 
                    cnt.getElement(i));
                if (pip instanceof CompoundValue) {
                    CompoundValue cmp = (CompoundValue) pip;
                    Value nameValue = cmp.getNestedValue(SLOT_PIPELINE_NAME);
                    if (nameValue instanceof StringValue) {
                        String name = ((StringValue) nameValue).getValue();
                        PipelineTrace pTrace = new PipelineTrace(name, xyGraph.primaryXAxis, xyGraph.primaryYAxis, 
                            getPointStyleMod(pipelineTraces.size()), QmObservables.SCALABILITY_ITEMS);
                        pipelineTraces.put(name, pTrace);
                        xyGraph.addTrace(pTrace.getTrace());
                    }
                }                
            }
        }
        
        lws.setContents(xyGraph);
        return xyGraphCanvas;
    }
    
    
    /**
     * Creates the panel for monitoring some quality properties.
     * 
     * @param parent the parent panel
     * @return the created panel
     */
    private Composite createMonitoringPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.BORDER | SWT.CENTER);
        GridLayout layout = new GridLayout(1, false);
        //layout.marginBottom = 50;
        //layout.marginWidth = 50;
        panel.setLayout(layout);

        Control control = createMeter(panel);
        GridData data = new GridData(GridData.CENTER);
        data.widthHint = 600;
        data.heightHint = 200;
        //data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.FILL;
        control.setLayoutData(data);
        
        //createGauche(panel);
        //createTank(panel);
        control = createLatencyGraph(panel);
        data = new GridData(GridData.FILL_BOTH);
        //data.verticalAlignment = GridData.FILL;
        //data.horizontalAlignment = GridData.FILL;
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
        // not nice, but we do not put the QM.events dependency in here by now; Names taken from resource descriptors / 
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
            PipelineTrace trace = pipelineTraces.get(part);
            if (null != trace) {
                trace.update(observations);
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
            for (PipelineTrace trace : pipelineTraces.values()) {
                trace.clearTrace();
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

}
