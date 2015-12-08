package de.uni_hildesheim.sse.repositoryConnector.svnConnector;

import org.tmatesoft.svn.core.SVNCancelException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.wc.ISVNEventHandler;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNEvent;
import org.tmatesoft.svn.core.wc.SVNEventAction;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * Function which will report us status (including remote status) of our Working Copy.
 * 
 * @author Sass
 * 
 */
public class StatusEventHandler implements ISVNStatusHandler, ISVNEventHandler {
    
    private boolean myIsRemote;
    
    /**
     * Default Constructor.
     * 
     * @param isRemote
     *            remote working copy
     */
    public StatusEventHandler(boolean isRemote) {
        myIsRemote = isRemote;
    }

    /**
     * Handles the status.
     * 
     * @param status
     *            the SVN status
     */
    public void handleStatus(SVNStatus status) {
        SVNStatusType contentsStatus = status.getContentsStatus();
        String pathChangeType = " ";
        boolean isAddedWithHistory = status.isCopied();
        if (contentsStatus == SVNStatusType.STATUS_MODIFIED) {
            pathChangeType = "M";
        } else if (contentsStatus == SVNStatusType.STATUS_CONFLICTED) {
            pathChangeType = "C";
        } else if (contentsStatus == SVNStatusType.STATUS_DELETED) {
            pathChangeType = "D";
        } else if (contentsStatus == SVNStatusType.STATUS_ADDED) {
            pathChangeType = "A";
        } else if (contentsStatus == SVNStatusType.STATUS_UNVERSIONED) {
            pathChangeType = "?";
        } else if (contentsStatus == SVNStatusType.STATUS_EXTERNAL) {
            pathChangeType = "X";
        } else if (contentsStatus == SVNStatusType.STATUS_IGNORED) {
            pathChangeType = "I";
        } else if (contentsStatus == SVNStatusType.STATUS_MISSING 
            || contentsStatus == SVNStatusType.STATUS_INCOMPLETE) {
            pathChangeType = "!";
        } else if (contentsStatus == SVNStatusType.STATUS_OBSTRUCTED) {
            pathChangeType = "~";
        } else if (contentsStatus == SVNStatusType.STATUS_REPLACED) {
            pathChangeType = "R";
        } else if (contentsStatus == SVNStatusType.STATUS_NONE || contentsStatus == SVNStatusType.STATUS_NORMAL) {
            pathChangeType = " ";
        }
        String remoteChangeType = " ";
        if (status.getRemotePropertiesStatus() != SVNStatusType.STATUS_NONE
                || status.getRemoteContentsStatus() != SVNStatusType.STATUS_NONE) {
            remoteChangeType = "*";
        }
        SVNStatusType propertiesStatus = status.getPropertiesStatus();
        String propertiesChangeType = " ";
        if (propertiesStatus == SVNStatusType.STATUS_MODIFIED) {
            propertiesChangeType = "M";
        } else if (propertiesStatus == SVNStatusType.STATUS_CONFLICTED) {
            propertiesChangeType = "C";
        }
        SVNLock localLock = status.getLocalLock();
        SVNLock remoteLock = status.getRemoteLock();
        String lockLabel = " ";
        if (localLock != null) {
            lockLabel = "K";
            if (remoteLock != null) {
                if (!remoteLock.getID().equals(localLock.getID())) {
                    lockLabel = "T";
                }
            } else {
                if (myIsRemote) {
                    lockLabel = "B";
                }
            }
        } else if (remoteLock != null) {
            lockLabel = "O";
        }
        long lastChangedRevision = status.getCommittedRevision().getNumber();
        String offset = "                                ";
        String[] offsets = new String[3];
        offsets[0] = offset.substring(0, 6 - String.valueOf(status.getRevision().getNumber()).length());
        offsets[1] = offset.substring(0, 6 - String.valueOf(lastChangedRevision).length());
        offsets[2] = offset.substring(0, offset.length()
                - (status.getAuthor() != null ? status.getAuthor().length() : 1));
        System.out.println(pathChangeType + propertiesChangeType + (status.isLocked() ? "L" : " ")
                + (isAddedWithHistory ? "+" : " ") + (status.isSwitched() ? "S" : " ") + lockLabel + "  " 
                + remoteChangeType + "  " + status.getRevision().getNumber() + offsets[0]
                + (lastChangedRevision >= 0 ? String.valueOf(lastChangedRevision) : "?") + offsets[1]
                + (status.getAuthor() != null ? status.getAuthor() : "?") + offsets[2] + status.getFile().getPath());
    }

    /**
     * Handles the event.
     * 
     * @param event
     *            event
     * @param progress
     *            progress
     */
    public void handleEvent(SVNEvent event, double progress) {
        SVNEventAction action = event.getAction();
        if (action == SVNEventAction.STATUS_COMPLETED) {
            System.out.println("Status against revision:  " + event.getRevision());
        }
    }
    
    /**
     * Check if the check is cancelled.
     * 
     * @throws SVNCancelException
     *             exception
     */
    public void checkCancelled() throws SVNCancelException {
    }

}