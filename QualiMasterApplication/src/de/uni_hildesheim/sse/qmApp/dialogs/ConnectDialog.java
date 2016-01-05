package de.uni_hildesheim.sse.qmApp.dialogs;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import eu.qualimaster.adaptation.external.AlgorithmChangedMessage;
import eu.qualimaster.adaptation.external.ClientEndpoint;
import eu.qualimaster.adaptation.external.DisconnectMessage;
import eu.qualimaster.adaptation.external.HardwareAliveMessage;
import eu.qualimaster.adaptation.external.IDispatcher;
import eu.qualimaster.adaptation.external.MonitoringDataMessage;
import eu.qualimaster.adaptation.external.PipelineMessage;
import eu.qualimaster.adaptation.external.SwitchAlgorithmMessage;

/**
 * Dialog asks the user for ip, port and creates a connection.
 * The ip and port are stored in the eclipse metadata.
 * 
 * @author Niko Nowatzki
 */
public class ConnectDialog extends AbstractDialog implements IDispatcher, Serializable {

    private static final long serialVersionUID = -2643903468689003241L;
    private ClientEndpoint endpoint;
    private Button connect;
    private Button disconnect;
    private Text platformIP;
    private Text platformPort;
    @SuppressWarnings("unused")
    private Label ipLabel;
    @SuppressWarnings("unused")
    private Label portLabel;
    
    /**
     * Wrapps ip and port of a connection.
     * @author User
     *
     */
    private static class ConnectionWrapper implements Serializable {

        private static final long serialVersionUID = 5587500725887648543L;
        private String ip;
        private String port;
        
        /**
         * Constructs a ConnectionsWrapper which wraps both an ip and a port.
         * @param ip connection ip.
         * @param port connection port.
         */
        public ConnectionWrapper(String ip, String port) {
            this.ip = ip;
            this.port = port;
        }
    }
    /**
     * Default constructor.
     * @param parentShell The parent shell.
     */
    public ConnectDialog(Shell parentShell) {
        super(parentShell);
        setBlockOnOpen(true);
    }

    /**
     * Creates a Label with the parameter composite as parent and the parameter text as text.
     * @param composite parent composite.
     * @param text text to set.
     * @return cLabel Label with set parent and text.
     */
    private Label createLabel(final Composite composite, String text) {
        final Label cLabel = new Label(composite, SWT.BEGINNING);
        cLabel.setText(text);
        return cLabel;
    }
    
    /**
     * Creates a {@link Text} for a given {@link Composite}.
     * 
     * @param composite
     *            The given Composite
     * 
     * @return the new {@link Text}
     */
    private Text createTextField(final Composite composite) {
        Text newText = new Text(composite, SWT.FILL | SWT.BORDER);
        final GridData data = new GridData();

        data.widthHint = 350;
        newText.setLayoutData(data);
        return newText;
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite panel = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        panel.setLayout(layout);

        ipLabel = createLabel(panel, "QM platform interface IP (internal):");
 
        platformIP = createTextField(panel);
        
        portLabel = createLabel(panel, "QM platform interface port (internal):");
        
        platformPort = createTextField(panel);
        
        try {
            loadPluginSettings();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        enableButtons();
        return panel;
    };
    
    /**
     * Save the ip and port of a connection.
     * @param address ip address.
     * @param port connections port.
     */
    private void savePluginSettings(InetAddress address, String port) {
    
        ConnectionWrapper wrapper = new ConnectionWrapper(address.toString(), port);
        
        try {
            String workspace = ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation().toString();
            String metadataFolder = workspace + "/.metadata";
            FileOutputStream fileoutputstream = new FileOutputStream(
                    metadataFolder + "/RuntimeConnection.ser");
            ObjectOutputStream outputstream = new ObjectOutputStream(
                    fileoutputstream);
            outputstream.writeObject(wrapper);
            outputstream.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Load the saved data into the TextrFields.
     * 
     *  @throws FileNotFoundException e If file with info about the latest connection is not found.
     */
    private void loadPluginSettings() throws FileNotFoundException {

        ConnectionWrapper wrapper = null;
        try {
            String workspace = ResourcesPlugin.getWorkspace().getRoot()
                    .getLocation().toString();
            String metadataFolder = workspace + "/.metadata";
            FileInputStream fileIn = new FileInputStream(metadataFolder
                    + "/RuntimeConnection.ser");

            ObjectInputStream in = new ObjectInputStream(fileIn);
            wrapper = (ConnectionWrapper) in.readObject();
            in.close();
            fileIn.close();

            if (wrapper != null) {
                platformIP.setText(wrapper.ip);
                platformPort.setText(wrapper.port);
            }
        } catch (IOException | ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
      
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        
        Button ok = getButton(IDialogConstants.OK_ID);
        ok.setText("Connect/Disconnect");
        setButtonLayoutData(ok);

        setButtonLayoutData(ok);
    }
    
    @Override
    protected void okPressed() {
        if (null == endpoint) {
            try {
                if (!platformIP.isDisposed() && platformPort != null) {
                    String platform = platformIP.getText();
                    //String platform = "192.168.91.129"; // local VM fallback
                    InetAddress address = InetAddress.getByName(platform);
                    int port = Integer.parseInt(platformPort.getMessage());
                    
                    savePluginSettings(address, Integer.toString(port));
                    
                    endpoint = new ClientEndpoint(ConnectDialog.this, address, port);
                    System.out.println(endpoint);
                    enableButtons();
                }

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
    }
    
    @Override
    protected Point getIntendedSize() {
        return new Point(600, 150);
    }

    @Override
    protected String getTitle() {
        return "Connection";
    }

    @Override
    public void handleAlgorithmChangedMessage(AlgorithmChangedMessage msg) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleDisconnect(DisconnectMessage arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleHardwareAliveMessage(HardwareAliveMessage msg) {
        // TODO Auto-generated method stub 
    }

    @Override
    public void handleMonitoringData(MonitoringDataMessage arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handlePipelineMessage(PipelineMessage msg) {
        // TODO Auto-generated method stub
    }

    @Override
    public void handleSwitchAlgorithm(SwitchAlgorithmMessage arg0) {
        // TODO Auto-generated method stub
    }
}

