package de.uni_hildesheim.sse.qmApp.editors;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
import org.csstudio.swt.xygraph.linearscale.AbstractScale.LabelSide;
import org.csstudio.swt.xygraph.util.XYGraphMediaFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.SchemeBorder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import de.uni_hildesheim.sse.model.confModel.IDecisionVariable;
import de.uni_hildesheim.sse.model.varModel.values.CompoundValue;
import de.uni_hildesheim.sse.model.varModel.values.ContainerValue;
import de.uni_hildesheim.sse.model.varModel.values.EnumValue;
import de.uni_hildesheim.sse.model.varModel.values.Value;
import de.uni_hildesheim.sse.qmApp.dialogs.Dialogs;
import de.uni_hildesheim.sse.qmApp.model.ModelAccess;
import de.uni_hildesheim.sse.qmApp.model.VariabilityModel.Configuration;
import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ClientEndpoint;
import eu.qualimaster.adaptation.external.DisconnectMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.IDispatcher;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmMessage;

/**
 * The preliminary editor for runtime settings. Will be replaced by an appropriate
 * generic version later in the project.
 * 
 * @author Holger Eichelberger
 */
public class RuntimeEditor extends EditorPart implements IDispatcher {

    private static int counter = 0;
    private static final String CORRELATION_SOFTWARE = "Software";
    private static final String CORRELATION_HARDWARE = "Hardware";
    private static final String SENTIMENT_SUPERVISED = "SVM";
    private static final String SENTIMENT_SENTIWORD = "Sentiworld";
    
    private static final String PIPELINE_NAME = "PriorityPip";
    private static final String PIPELINE_ELEMENT_CORRELATION = "fCorrelationFinancial";
    private static final String PIPELINE_ELEMENT_SENTIMENT = "fSentimentAnalysis";
    
    private static final int PIPELINE_DISPLAY_BUFFER_SIZE = 50;
    private static final int PIPELINE_DISPLAY_DELAY = 100;
    
    private static final String EMPTY_ALG_LABEL = "---------";
    private static final String CHANGE_ALG_LABEL = "---------";
    
    private ClientEndpoint endpoint;
    private Button connect;
    private Button disconnect;
    private Text platformIP;
    private Text platformPort;
    
    private Button enact;
    private Button startPipeline;
    private Button stopPipeline;
    private String correlation;
    private String sentiment;
    private Label corrState;
    private Label sentState;
    
    private MeterFigure usedClusterMachines;
    private CircularBufferDataProvider pipelineLatencyDataProvider;
    private CircularBufferDataProvider pipelineThroughputDataProvider;
    private int observationTime = 0;
    
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
        createAdaptationPanel(parent);
        Composite panel = createMonitoringPanel(parent);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 2;
        panel.setLayoutData(data);
    }

    /**
     * Enables or disables the buttons on this editor.
     */
    private void enableButtons() {
        if (null != connect) {
            connect.setEnabled(null == endpoint);
        }
        if (null != disconnect) {
            disconnect.setEnabled(null != endpoint);
        }
        if (null != enact) {
            enact.setEnabled(null != correlation || null != sentiment);
        }
        if (null != endpoint) {
            pipelineLatencyDataProvider.clearTrace();
        }
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

        Label label = new Label(panel, SWT.NONE);
        label.setText("QM platform interface IP (internal):");

        platformIP = new Text(panel, SWT.BORDER);
        platformIP.setText("snf-618466.vm.okeanos.grnet.gr"); // localhost

        label = new Label(panel, SWT.NONE);
        label.setText("QM platform interface port (internal):");

        platformPort = new Text(panel, SWT.BORDER);
        platformPort.setText("7012"); // TODO take from configuration

        
        connect = new Button(panel, SWT.PUSH);
        connect.setText("Connect");
        connect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                if (null == endpoint) {
                    try {
                        String platform = platformIP.getText();
                        //String platform = "192.168.91.129"; // local VM fallback
                        InetAddress address = InetAddress.getByName(platform);
                        int port = Integer.parseInt(platformPort.getText());
                        endpoint = new ClientEndpoint(RuntimeEditor.this, address, port);
                        System.out.println(endpoint);
                        enableButtons();
                        usedClusterMachines.setValid(true);
                    } catch (UnknownHostException e) {
                        Dialogs.showErrorDialog("Cannot connect to QM infrastructure", e.getMessage());
                    } catch (SecurityException e) {
                        Dialogs.showErrorDialog("Cannot connect to QM infrastructure", e.getMessage());
                    } catch (NumberFormatException e) {
                        Dialogs.showErrorDialog("Port number", e.getMessage());
                    } catch (IOException e) {
                        Dialogs.showErrorDialog("Cannot connect to QM infrastructure", e.getMessage());
                    }
                }
            }
        });
        
        disconnect = new Button(panel, SWT.PUSH);
        disconnect.setText("Disconnect");
        disconnect.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                ClientEndpoint ep = endpoint;
                endpoint = null; // -> message
                ep.schedule(new DisconnectMessage());
                ep.stop();
                enableButtons();
                invalidateGauges();
            }
        });
        
        enableButtons();
    }
    
    /**
     * Returns the selected element name of a combo.
     * 
     * @param combo the combo to return the selected for
     * @return the selected name or <b>null</b> if nothing (valid) was selected
     */
    private String getComboSelected(Combo combo) {
        String result = combo.getText();
        if (null == result || 0 == result.length()) {
            result = null;
        }
        return result;
    }
    
    /**
     * Creates the panel for controlling the adaptation.
     * 
     * @param parent the parent panel
     */
    private void createAdaptationPanel(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        
        GridLayout layout = new GridLayout(4, false);
        panel.setLayout(layout);
        
        startPipeline = new Button(panel, SWT.PUSH);
        startPipeline.setText("Start pipeline");
        startPipeline.addSelectionListener(new PipelineSelectionListener(PipelineMessage.Status.START));
        
        Label label = new Label(panel, SWT.NONE);
        label.setText("Correlation computation");
        Combo combo = new Combo(panel, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.add(CORRELATION_SOFTWARE);
        combo.add(CORRELATION_HARDWARE);
        combo.select(0);
        correlation = getComboSelected(combo);
        combo.addSelectionListener(new AdaptationSelectionListener(true));

        corrState = new Label(panel, SWT.NONE);
        corrState.setText(EMPTY_ALG_LABEL);
        
        stopPipeline = new Button(panel, SWT.PUSH);
        stopPipeline.setText("Stop pipeline");
        stopPipeline.addSelectionListener(new PipelineSelectionListener(PipelineMessage.Status.STOP));

        label = new Label(panel, SWT.NONE);
        label.setText("Sentiment computation");
        combo = new Combo(panel, SWT.DROP_DOWN | SWT.READ_ONLY);
        combo.add(SENTIMENT_SUPERVISED);
        combo.add(SENTIMENT_SENTIWORD);
        combo.select(0);
        sentiment = getComboSelected(combo);
        combo.addSelectionListener(new AdaptationSelectionListener(false));
                
        sentState = new Label(panel, SWT.NONE);
        sentState.setText(EMPTY_ALG_LABEL);
        
        new Label(panel, SWT.NONE);
        new Label(panel, SWT.NONE);
        enact = new Button(panel, SWT.PUSH);
        enact.setText("Enact");
        enact.addSelectionListener(new EnactmentSelectionListener());
    }
    
    /**
     * Implements status changes of the pipeline.
     * 
     * @author Holger Eichelberger
     */
    private class PipelineSelectionListener extends SelectionAdapter {

        private PipelineMessage.Status status;
        
        /**
         * Creates a pipeline selection listener.
         * 
         * @param status the intended status
         */
        private PipelineSelectionListener(PipelineMessage.Status status) {
            this.status = status;
        }
        
        @Override
        public void widgetSelected(SelectionEvent event) {
            endpoint.schedule(new PipelineMessage(PIPELINE_NAME, status));
            if (PipelineMessage.Status.STOP == status) {
                corrState.setText(EMPTY_ALG_LABEL);
                sentState.setText(EMPTY_ALG_LABEL);
            }
        }

    }
    
    
    /**
     * Implements a selection listener for the choice of adaptation.
     * 
     * @author Holger Eichelberger
     */
    private class AdaptationSelectionListener extends SelectionAdapter {
        
        private boolean isCorrelation;
        
        /**
         * Creates the listener.
         * 
         * @param isCorrelation correlation (<code>true</code>) or sentiment (<code>false</code>)
         */
        private AdaptationSelectionListener(boolean isCorrelation) {
            this.isCorrelation = isCorrelation;
        }
        
        @Override
        public void widgetSelected(SelectionEvent event) {
            if (event.getSource() instanceof Combo) {
                String selection = getComboSelected((Combo) event.getSource());
                if (isCorrelation) {
                    correlation = selection;
                } else {
                    sentiment = selection;
                }
                enableButtons();
            }
        }
        
    }

    /**
     * Implements the listener for the enactment button.
     * 
     * @author Holger Eichelberger
     */
    private class EnactmentSelectionListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent event) {
            if (null != sentiment) {
                String algo;
                switch (sentiment) {
                case SENTIMENT_SENTIWORD:
                    algo = "SentimentAnaylsisSentiWordNetTopology";
                    break;
                case SENTIMENT_SUPERVISED:
                    algo = "SentimentAnaylsisSVMTopology";
                    break;
                default:
                    algo = null;
                    break;
                }
                endpoint.schedule(new SwitchAlgorithmMessage(PIPELINE_NAME, PIPELINE_ELEMENT_SENTIMENT, algo));
                sentState.setText(CHANGE_ALG_LABEL);
                sentiment = null;
            }
            if (null != correlation) {
                String algo;
                switch (correlation) {
                case CORRELATION_HARDWARE:
                    algo = "TopoHardwareCorrelationFinancial";
                    break;
                case CORRELATION_SOFTWARE:
                    algo = "TopoSoftwareCorrelationFinancial";
                    break;
                default:
                    algo = null;
                    break;
                }
                endpoint.schedule(new SwitchAlgorithmMessage(PIPELINE_NAME, PIPELINE_ELEMENT_CORRELATION, algo));
                corrState.setText(CHANGE_ALG_LABEL);
                correlation = null;
            }
            enableButtons();
        }

    }

    /**
     * Create meter-widget for monitoring panel.
     * @param parent parent composite on which the widgets are placed.
     * @return the created control
     */
    private Control createMeter(final Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new GridLayout(1, true));
        
        Canvas meterCanvas = new Canvas(panel, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_VERTICAL);
        data.heightHint = 200;
        data.widthHint = 350;
        meterCanvas.setLayoutData(data);
        
        Label label = new Label(panel, SWT.CENTER);
        label.setText("used machines");
        data = new GridData();
        data.widthHint = 400;
        label.setLayoutData(data);

        //Create Figure
        usedClusterMachines = new MeterFigure();
        usedClusterMachines.setBackgroundColor(XYGraphMediaFactory.getInstance().getColor(255, 255, 255));
        usedClusterMachines.setBorder(new SchemeBorder(SchemeBorder.SCHEMES.ETCHED));
        IDecisionVariable machines = ModelAccess.findTopContainer(Configuration.HARDWARE, 
            Configuration.HARDWARE.getProvidedTypes()[0]); // uhh
        Value value = machines.getValue();
        int workerCount = 0;
        if (value instanceof ContainerValue) {
            ContainerValue cnt = (ContainerValue) value;
            for (int i = 0; i < cnt.getElementSize(); i++) {
                Value val = cnt.getElement(i);
                if (val instanceof CompoundValue) {
                    CompoundValue cmp = (CompoundValue) val;
                    Value roleValue = cmp.getNestedValue("role");
                    if (roleValue instanceof EnumValue) {
                        EnumValue eValue = (EnumValue) roleValue;
                        if ("Worker".equals(eValue.getValue().getName())) {
                            workerCount++;
                        }
                    }
                }
            }
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
        xyGraph.primaryYAxis.setTitle("latency (ms)");
        xyGraph.primaryYAxis.setAutoScale(true);
        xyGraph.primaryYAxis.setForegroundColor(
            XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_BLUE));

        Axis throughputAxis = new Axis("throughput (items)", true);
        throughputAxis.setForegroundColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
        throughputAxis.setTickLableSide(LabelSide.Secondary);
        throughputAxis.setAutoScale(true);
        xyGraph.addAxis(throughputAxis);
        
        
        // AXIS
        
        //create a trace data provider, which will provide the data to the trace.
        pipelineLatencyDataProvider = new CircularBufferDataProvider(true);
        pipelineLatencyDataProvider.setBufferSize(PIPELINE_DISPLAY_BUFFER_SIZE);
        pipelineLatencyDataProvider.setUpdateDelay(PIPELINE_DISPLAY_DELAY);
        
        pipelineThroughputDataProvider = new CircularBufferDataProvider(true);
        pipelineThroughputDataProvider.setBufferSize(PIPELINE_DISPLAY_BUFFER_SIZE);
        pipelineThroughputDataProvider.setUpdateDelay(PIPELINE_DISPLAY_DELAY);
        
        //create the latency trace
        Trace trace = new Trace("latency (ms)", xyGraph.primaryXAxis, xyGraph.primaryYAxis, 
            pipelineLatencyDataProvider);
        trace.setDataProvider(pipelineLatencyDataProvider);
        trace.setPointStyle(PointStyle.XCROSS);
        xyGraph.addTrace(trace);
        
        //create the throughput trace
        trace = new Trace("throughput (items)", xyGraph.primaryXAxis, throughputAxis, 
            pipelineThroughputDataProvider);
        trace.setDataProvider(pipelineThroughputDataProvider);
        trace.setPointStyle(PointStyle.DIAMOND);
        xyGraph.addTrace(trace);

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
        Composite panel = new Composite(parent, SWT.BORDER);
        GridLayout layout = new GridLayout(1, false);
        //layout.marginBottom = 50;
        //layout.marginWidth = 50;
        panel.setLayout(layout);

        Control control = createMeter(panel);
        GridData data = new GridData(GridData.CENTER);
        data.widthHint = 400;
        data.heightHint = 200;
        //data.verticalAlignment = GridData.FILL;
        data.horizontalAlignment = GridData.CENTER;
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
    public void handleDisconnect(DisconnectMessage message) {
        // server side message, just for sure
        if (null != endpoint) {
            endpoint.stop();
            endpoint = null;
        }
        enableButtons();
        invalidateGauges();
    }

    @Override
    public void handleMonitoringData(MonitoringDataMessage message) {
        String part = message.getPart();
        // not nice, but we do not put the QM.events dependency in here by now; Names taken from resource descriptors / 
        // monitoring layer system state
        Map<String, Double> observations = message.getObservations();
        if ("Infrastructure".equals(part)) {
            final Double usedMachines = observations.get("USED_MACHINES"); 
            if (null != usedMachines) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        int machineCount  = usedMachines.intValue();
                        usedClusterMachines.setValue(machineCount);
                    }
                });
            }
        } else if (PIPELINE_NAME.equals(part)) {
            final Double latency = observations.get("LATENCY");
            final Double items = observations.get("THROUGHPUT_ITEMS");
            if (null != latency || null != items) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        if (null != latency) {
                            pipelineLatencyDataProvider.addSample(new Sample(observationTime, latency.doubleValue()));
                        }
                        if (null != items) {
                            pipelineThroughputDataProvider.addSample(new Sample(observationTime, items.doubleValue()));
                        }
                        observationTime++;
                    }
                });
            }
        }
    }

    @Override
    public void handleSwitchAlgorithm(SwitchAlgorithmMessage message) {
        // server side - nothing to do
    }

    @Override
    public void handlePipelineMessage(PipelineMessage msg) {
        // server side - nothing to do
    }

    /**
     * Updates a label from another thread.
     * 
     * @param label the label to be updated
     * @param text the new text of <code>label</code>
     */
    private static void updateLabel(final Label label, final String text) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                label.setText(text);
            }     
        });
    }
    
    @Override
    public void handleAlgorithmChangedMessage(AlgorithmChangedMessage msg) {
        String algorithm = msg.getAlgorithm(); // passed through, not translated
        if ("TopoSoftwareCorrelationFinancial".equals(algorithm)) {
            updateLabel(corrState, "SW");
        } else if ("TopoHardwareCorrelationFinancial".equals(algorithm)) {
            updateLabel(corrState, "HW");
        } else if ("SentimentAnaylsisSentiWordNetTopology".equals(algorithm)) {
            updateLabel(sentState, "SWord");
        } else if ("SentimentAnaylsisSVMTopology".equals(algorithm)) {
            updateLabel(sentState, "SVM");
        }
        System.out.println("AlgChg: " + msg.getPipeline() + " " + msg.getPipelineElement() + " " + msg.getAlgorithm());
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage msg) {
        System.out.println("HwAlive: " + msg.getIdentifier());
        updateLabel(corrState, "HW-run");
    }

}
