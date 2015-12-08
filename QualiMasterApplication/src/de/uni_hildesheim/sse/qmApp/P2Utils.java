/*
 * Copyright 2009-2015 University of Hildesheim, Software Systems Engineering
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
package de.uni_hildesheim.sse.qmApp;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper;
import org.eclipse.equinox.internal.p2.ui.ProvUIActivator;
//import org.eclipse.equinox.internal.p2.ui.model.ElementUtils;
import org.eclipse.equinox.internal.p2.ui.model.MetadataRepositoryElement;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.engine.IProfile;
import org.eclipse.equinox.p2.engine.IProfileRegistry;
import org.eclipse.equinox.p2.engine.query.UserVisibleRootQuery;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.operations.ProvisioningJob;
import org.eclipse.equinox.p2.operations.ProvisioningSession;
import org.eclipse.equinox.p2.operations.UpdateOperation;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.ui.ProvisioningUI;

import qualimasterapplication.Activator;

/**
 * Installing update URIs as p2.inf seems to fail in the nightly version. Temporarily, most of the code is taken
 * over from Eclipse to find problems in self-updating.
 * 
 * @author Holger Eichelberger
 */
@SuppressWarnings("restriction")
public class P2Utils {

    // http://stackoverflow.com/questions/3254441/configure-a-p2-update-repository-programmatically
    
    /**
     * Reads the p2.inf file and returns update repository URIs.
     * 
     * @return update repository URIs (may be empty)
     */
    private static List<URI> readP2Inf() {
        Set<String> urls = new HashSet<String>();
        InputStream is = P2Utils.class.getClassLoader().getResourceAsStream("p2.inf");
        if (null != is) {
            try {
                LineNumberReader lnr = new LineNumberReader(new InputStreamReader(is));
                String line;
                do {
                    line = lnr.readLine();
                    if (null != line) {
                        parseLine(line, urls);
                    }
                } while (null != line);
                lnr.close();
            } catch (IOException e) {
                try {
                    is.close();
                } catch (IOException e1) {
                }
            }
        }
        List<URI> result = new ArrayList<URI>();
        for (String url : urls) {
            try {
                result.add(new URI(url));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Parses a line and tries to extract repository URIs.
     * 
     * @param line the line to be parsed
     * @param urls the URLs found (modified as a side effect)
     */
    private static void parseLine(String line, Set<String> urls) {
        line = line.trim();
        int pos = line.indexOf("addRepository(");
        if (pos >= 0) {
            pos = line.indexOf("location:", pos);
            if (pos > 0) {
                pos += 9;
                int lastPos = line.lastIndexOf(")");
                if (pos < lastPos) {
                    urls.add(line.substring(pos, lastPos).replace("${#58}", ":"));
                }
            }
        }
    }
    
    /**
     * Ensures the update URIs.
     */
    public static void ensureUpdateURI() {
        List<URI> uris = readP2Inf();
        /*List<URI> uris = new ArrayList<URI>(); // TODO just for debugging
        try {
            uris.add(new URI("http://projects.sse.uni-hildesheim.de/qm/qmicNightly/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }*/
        
        //final ProvisioningUI ui = getProvisioningUI();
        if (!uris.isEmpty()) {
            MetadataRepositoryElement[] elements = new MetadataRepositoryElement[uris.size()];
            int pos = 0;
            for (URI uri : uris) {
                System.out.println("Adding update site: " + uri);
                elements[pos++] = new MetadataRepositoryElement(null, uri, true);
            }
            // Commented out due to issues with new Eclipse and Java 64bit
//            ElementUtils.updateRepositoryUsingElements(ui, elements, null);
        }
    }
    
    /**
     * Checks for convenience method taking the provisioning agent from the service helper via the activator's context.
     * 
     * @param monitor the progress monitor
     * @return the status of the operation
     * @throws OperationCanceledException in case that the user cancelled the operation
     */
    public static IStatus checkForUpdates(IProgressMonitor monitor) throws OperationCanceledException {
        final IProvisioningAgent agent = (IProvisioningAgent) ServiceHelper.getService(
            Activator.getContext(), IProvisioningAgent.SERVICE_NAME);
        ProvisioningUI ui = getProvisioningUI();
        String profileId = ui.getProfileId();
        //IArtifactRepositoryManager artifactManager = ProvUI.getArtifactRepositoryManager(ui.getSession());
        //IProvisioningAgent agent = artifactManager.getAgent();
        return checkForUpdates(agent, profileId, monitor);
    }

    /**
     * Checks for updates.
     * 
     * @param agent the provisioning agent
     * @param profileId the id of the profile to update (use the default "_SELF_" if <b>null</b>)
     * @param monitor the progress monitor
     * @return the status of the operation
     * @throws OperationCanceledException in case that the user cancelled the operation
     */
    public static IStatus checkForUpdates(IProvisioningAgent agent, String profileId, IProgressMonitor monitor) 
        throws OperationCanceledException {
        // http://help.eclipse.org/luna/index.jsp?topic=%2Forg.eclipse.platform.doc.isv%2Fguide%2Fp2_uireuse.htm
        ProvisioningSession session = new ProvisioningSession(agent);
        // the default update operation looks for updates to the currently
        // running profile, using the default profile root marker. However, the 
        // default constructor tries to update user visible installable units, which
        // fails on our side
        IProfileRegistry registry = (IProfileRegistry) agent.getService(IProfileRegistry.SERVICE_NAME);
        IProfile profile = registry.getProfile(profileId);
        Collection<IInstallableUnit> units = profile.query(new UserVisibleRootQuery(), null).toSet();
        System.out.println("ROOTS " + units);
        units = profile.query(QueryUtil.ALL_UNITS, null).toSet();
        //System.out.println("ALL " + units);
        List<IInstallableUnit> nUnits = new ArrayList<IInstallableUnit>();
        for (IInstallableUnit unit : units) {
            System.out.println(unit.getId());
            if (unit.getId().equals("de.uni-hildesheim.sse.qualiMasterApplication")) {
                nUnits.add(unit);
            }
        }
        System.out.println("FILTERED " + nUnits);
        if (nUnits.isEmpty()) {
            units = profile.query(new UserVisibleRootQuery(), null).toSet();
        } else {
            units = nUnits;
        }
        UpdateOperation operation = new UpdateOperation(session, units);
        if (null != profileId) {
            operation.setProfileId(profileId);
        }
        SubMonitor sub = SubMonitor.convert(monitor, "Checking for updates...", 200);
        IStatus status = operation.resolveModal(sub.newChild(100));
        if (status.getCode() != UpdateOperation.STATUS_NOTHING_TO_UPDATE) {
            if (status.getSeverity() == IStatus.CANCEL) {
                throw new OperationCanceledException();
            } else if (status.getSeverity() != IStatus.ERROR) {
                // More complex status handling might include showing the user what updates
                // are available if there are multiples, differentiating patches vs. updates, etc.
                // In this example, we simply update as suggested by the operation.
                ProvisioningJob job = operation.getProvisioningJob(null);
                status = job.runModal(sub.newChild(100));
                if (status.getSeverity() == IStatus.CANCEL) {
                    throw new OperationCanceledException();
                }
            }
        }
        return status;
    }
    
    /**
     * Returns an instance of the provisioning UI.
     * 
     * @return the provisioning UI instance
     */
    public static ProvisioningUI getProvisioningUI() {
        return ProvUIActivator.getDefault().getProvisioningUI();
    }

}
