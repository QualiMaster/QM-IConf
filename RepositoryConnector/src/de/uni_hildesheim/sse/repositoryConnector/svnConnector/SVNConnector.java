package de.uni_hildesheim.sse.repositoryConnector.svnConnector;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.BasicAuthenticationManager;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.fs.FSRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNStatusHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNConflictChoice;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNStatusClient;
import org.tmatesoft.svn.core.wc.SVNStatusType;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCClient;
import org.tmatesoft.svn.core.wc2.SvnDiff;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;

import de.uni_hildesheim.sse.repositoryConnector.Bundle;
import de.uni_hildesheim.sse.repositoryConnector.IRepositoryConnector;
import de.uni_hildesheim.sse.repositoryConnector.UserContext;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.RoleFetcher;
import de.uni_hildesheim.sse.repositoryConnector.roleFetcher.model.Role;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory;
import de.uni_hildesheim.sse.utils.logger.EASyLoggerFactory.EASyLogger;

/**
 * Implementation for a repository connector for SVN repositories.
 * 
 * @author Sass
 * 
 */
public class SVNConnector implements IRepositoryConnector {

    private static EASyLogger logger = EASyLoggerFactory.INSTANCE.getLogger(SVNConnector.class, Bundle.ID);

    private String repositoryURL;

    private SVNRepository repository;

    private UpdateEventHandler updateEventHandler;

    private CommitEventHandler commitEventHandler;

    private SVNClientManager svnClientManager;

    private SVNWCClient workingCopyClient;

    private SVNCommitClient commitClient;

    private SVNUpdateClient updateClient;

    private int repositoryEntryCount;

    /**
     * Default constructor.
     */
    public SVNConnector() {
        Security.insertProviderAt(new BouncyCastleProvider(), 1);
        svnClientManager = SVNClientManager.newInstance();
        workingCopyClient = svnClientManager.getWCClient();
        commitClient = svnClientManager.getCommitClient();
        updateClient = svnClientManager.getUpdateClient();
        updateEventHandler = new UpdateEventHandler();
        commitEventHandler = new CommitEventHandler();
        updateClient.setEventHandler(updateEventHandler);
        commitClient.setEventHandler(commitEventHandler);
        // Default repository URL
        repositoryURL = "https://projects.sse.uni-hildesheim.de/svn/test_svn/Model/QM2";
    }

    @Override
    public boolean authenticate(String username, String password) {
        DAVRepositoryFactory.setup();
        repository = null;
        boolean auth = false;
        try {
            repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(repositoryURL));
            /*
             * If DefaultAuthenticationManager is used there will occur problems
             * when running the application on Linux OS because its behavior
             * differs when running on Windows OS.
             */
            ISVNAuthenticationManager authManager = new BasicAuthenticationManager(username, password);
            repository.setAuthenticationManager(authManager);
            logger.info("Repository Root: " + repository.getRepositoryRoot(true));
            logger.info("Repository UUID: " + repository.getRepositoryUUID(true));
            auth = true;
            logger.debug("Authentication successfull");
            UserContext.INSTANCE.init(username, password, repository);
            UserContext.INSTANCE.setRoles(RoleFetcher.getUserRoles());
        } catch (SVNException svne) {
            logger.exception(svne);
        }
        countEntries("");
        return auth;
    }

    @Override
    public File loadModel(File destinationFile) {
        checkout(destinationFile);
        return destinationFile;
    }

    @Override
    public boolean storeModel(File destinationFile) {
        return commit(destinationFile, false);
    }

    // @Override
    // public void storeModel(File destinationFile, boolean override) {
    // commit(destinationFile, override);
    // }

    @Override
    public List<File> updateModel(File destinationFile) {
        return update(destinationFile);
    }

    @Override
    public void resolveConflicts(File path, boolean keepMine) {
        SVNConflictChoice conflictChoice = SVNConflictChoice.THEIRS_FULL;
        if (keepMine) {
            conflictChoice = SVNConflictChoice.MINE_FULL;
        }
        resolve(path, conflictChoice);
    }

    /**
     * Checks out a Working Copy given a repository URL.
     * 
     * @param destination
     *            The destination directory where the model should be stored on
     *            the local system
     */
    private void checkout(File destination) {
        svnClientManager.setAuthenticationManager(repository.getAuthenticationManager());
        updateClient.setIgnoreExternals(false);
        try {
            updateClient.doCheckout(repository.getLocation(), destination,
                    SVNRevision.create(repository.getLatestRevision()),
                    SVNRevision.create(repository.getLatestRevision()), SVNDepth.INFINITY, false);
        } catch (SVNException e) {
            logger.exception(e);
        }
    }

    /**
     * Updates the working copy to HEAD.
     * 
     * @param wcPath
     *            working copy paths
     * @return List with conflicting files
     */
    private List<File> update(File wcPath) {
        svnClientManager.setAuthenticationManager(repository.getAuthenticationManager());
        updateClient.setIgnoreExternals(false);
        doStatus(wcPath, true);
        try {
            updateClient.doUpdate(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, true, false);
        } catch (SVNException e) {
            logger.exception(e);
        }
        return getConflictingFilesInWorkspace(wcPath);
    }

    /**
     * Adds all new entries to version control.
     * 
     * @param workingCopyPath
     *            Path of the workspace
     */
    private void addEntry(File workingCopyPath) {
        try {
            workingCopyClient.doAdd(workingCopyPath, true, false, false, SVNDepth.INFINITY, false, false);
        } catch (SVNException e) {
            logger.exception(e);
        }
    }

    /**
     * Deletes entries from version control.
     * 
     * @param workingCopyPath
     *            Path to the workspace
     * @param force
     *            true or false
     */
    private void deleteEntry(File workingCopyPath, boolean force) {
        try {
            workingCopyClient.doDelete(workingCopyPath, force, false);
        } catch (SVNException e) {
            logger.exception(e);
        }
    }

    /**
     * Commits all changes to the repository.
     * 
     * @param path
     *            File of the working copy
     * @param override
     *            Should the remote file be overwritten in case of conflicts
     * @return conflict true or false whether there are conflicts
     */
    private boolean commit(File path, boolean override) {
        boolean conflict = false;
        // SVNConflictChoice conflictChoice = SVNConflictChoice.THEIRS_FULL;
        // if (override) {
        // conflictChoice = SVNConflictChoice.MINE_FULL;
        // }
        if (repository == null) {
            authenticate(UserContext.INSTANCE.getUsername(), UserContext.INSTANCE.getPassword());
        }
        svnClientManager.setAuthenticationManager(repository.getAuthenticationManager());
        doStatus(path, false);
        try {
            commitClient.doCommit(new File[] {path}, false, "<commit> " + "", null, null, false, true,
                    SVNDepth.INFINITY);
        } catch (SVNException e) {
            // Resolve conflicts
            if (e.getErrorMessage().getErrorCode().getCode() == 160024
                    || e.getErrorMessage().getErrorCode().getCode() == 155011) {
                conflict = true;
                // update(path);
                // resolve(path, conflictChoice);
                // try {
                // commitClient.doCommit(new File[] {path}, false, "<commit> " +
                // "", null, null, false, true,
                // SVNDepth.INFINITY);
                // } catch (SVNException e1) {
                // logger.exception(e1);
                // }
            } else {
                logger.exception(e);
            }
        }
        return conflict;
    }

    /**
     * Resolves conflicts in the working copy.
     * 
     * @param path
     *            Path to working
     * @param conflictChoice
     *            Choice on how to handle conflict
     *            (SVNConflictChoice.THEIRS_FULL or SVNConflictChoice.MINE_FULL)
     */
    private void resolve(File path, SVNConflictChoice conflictChoice) {
        SVNWCClient svnWCClient = svnClientManager.getWCClient();
        try {
            svnWCClient.doResolve(path, SVNDepth.INFINITY, conflictChoice);
        } catch (SVNException ex) {
            logger.exception(ex);
        }
    }

    /**
     * Gets all conflicting files in the working copy.
     * 
     * @param wcPath
     *            Path to working copy
     * @return List with all conflicting files
     */
    @Override
    public List<File> getConflictingFilesInWorkspace(File wcPath) {
        // Ignore certain files
        // SVNPropertyValue value = SVNPropertyValue.create(".settings");
        // try {
        // workingCopyClient.doSetProperty(wcPath, SVNProperty.IGNORE, value,
        // false, SVNDepth.UNKNOWN, null, null);
        // } catch (SVNException e) {
        // logger.exception(e);
        // }
        final List<File> changeLists = new ArrayList<File>();
        try {
            SVNStatusClient statusclient = svnClientManager.getStatusClient();
            boolean remote = true;
            boolean reportAll = false;
            boolean includeIgnored = false;
            boolean collectParentExternals = false;
            statusclient.doStatus(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, remote, reportAll, includeIgnored,
                    collectParentExternals, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            SVNStatusType remoteStatusType = status.getRemoteContentsStatus();
                            SVNStatusType localStatusType = status.getContentsStatus();
                            // Assumption: If the remote and the local file are
                            // modified it will result in a conflict
                            if (localStatusType == SVNStatusType.STATUS_MODIFIED
                                    && remoteStatusType == SVNStatusType.STATUS_MODIFIED) {
                                changeLists.add(status.getFile());
                            } else if (localStatusType == SVNStatusType.STATUS_CONFLICTED) {
                                changeLists.add(status.getFile());
                            }
                        }
                    }, null);
        } catch (SVNException e) {
            logger.exception(e);
        }
        doDiff(changeLists);
        return changeLists;
    }

    /**
     * Diff of two separate files.
     * 
     * @param changeList
     *            List with all changes in working copy
     */
    private void doDiff(List<File> changeList) {
        try {
            for (File file : changeList) {
                final SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
                try {
                    final ByteArrayOutputStream byteArrayOutputStream1 = new ByteArrayOutputStream();
                    final SvnDiff diff = svnOperationFactory.createDiff();
                    diff.setSources(SvnTarget.fromFile(file, SVNRevision.HEAD),
                            SvnTarget.fromFile(file, SVNRevision.WORKING));
                    diff.setOutput(byteArrayOutputStream1);
                    diff.run();
                } finally {
                    svnOperationFactory.dispose();
                }
            }
        } catch (SVNException e1) {
            logger.exception(e1);
        }
    }

    /***
     * Given a path to a working copy directory (or single file), calls handler
     * with a set of SVNStatus objects which describe the status of the path,
     * and its children (recursing according to depth).
     * 
     * @param wcPath
     *            working copy path
     * @param isRemote
     *            true to check up the status of the item in the repository,
     *            that will tell if the local item is out-of-date (like '-u'
     *            option in the SVN client's 'svn status' command), otherwise
     *            false
     * 
     * @return number of changed items
     */
    private List<File> doStatus(File wcPath, final boolean isRemote) {
        // reportAll - true to collect status information on all items including
        // those ones that are in a 'normal'
        // state (unchanged), otherwise false
        boolean isReportAll = false;
        boolean isIncludeIgnored = false;
        boolean isCollectParentExternals = true;
        final List<File> changeLists = new ArrayList<File>();
        try {
            svnClientManager.setAuthenticationManager(repository.getAuthenticationManager());
            SVNStatusClient statusclient = svnClientManager.getStatusClient();
            statusclient.doStatus(wcPath, SVNRevision.HEAD, SVNDepth.INFINITY, isRemote, isReportAll, isIncludeIgnored,
                    isCollectParentExternals, new ISVNStatusHandler() {
                        @Override
                        public void handleStatus(SVNStatus status) throws SVNException {
                            if (isRemote) {
                                SVNStatusType statusType = status.getRemoteContentsStatus();
                                if (statusType != SVNStatusType.STATUS_NORMAL
                                        && statusType != SVNStatusType.STATUS_IGNORED 
                                        && statusType != SVNStatusType.STATUS_NONE) {
                                    changeLists.add(status.getFile());
                                }
                            } else {
                                SVNStatusType statusType = status.getNodeStatus();
                                if (statusType != SVNStatusType.STATUS_NONE && statusType != SVNStatusType.STATUS_NORMAL
                                        && statusType != SVNStatusType.STATUS_IGNORED
                                        && statusType != SVNStatusType.STATUS_MISSING) {
                                    changeLists.add(status.getFile());
                                }
                                if (SVNStatusType.STATUS_UNVERSIONED.equals(status.getNodeStatus())) {
                                    addEntry(status.getFile());
                                } else if (SVNStatusType.STATUS_MISSING.equals(status.getNodeStatus())) {
                                    deleteEntry(status.getFile(), true);
                                }
                            }
                        }
                    }, null);
        } catch (SVNException e) {
            logger.exception(e);
        }
        return changeLists;
    }

    /**
     * Overrides the local changes made in the working copy.
     * 
     * @param path
     *            path to the working copy
     */
    @Override
    public void revert(File path) {
        List<File> list = doStatus(path, false);
        File[] changes = list.toArray(new File[list.size()]);
        try {
            workingCopyClient.doRevert(changes, SVNDepth.INFINITY, null);
        } catch (SVNException e) {
            logger.exception(e);
        }
        // Remove unversioned items
        for (File file : changes) {
            try {
                SVNStatusClient statusclient = svnClientManager.getStatusClient();
                statusclient.doStatus(file, SVNRevision.HEAD, SVNDepth.INFINITY, false, false, false, true,
                        new ISVNStatusHandler() {
                            @Override
                            public void handleStatus(SVNStatus status) throws SVNException {
                                SVNStatusType statusType = status.getNodeStatus();
                                if (statusType == SVNStatusType.STATUS_UNVERSIONED) {
                                    // TODO: What about .settings? Should it be
                                    // deleted as well?
                                    status.getFile().delete();
                                }
                            }
                        }, null);
            } catch (SVNException e) {
                logger.exception(e);
            }
        }
    }

    /**
     * Counts the entries in the repository.
     * 
     * @param path
     *            path in the repository(usually "")
     * @return number of repository entries
     */
    private int countEntries(String path) {
        Collection<SVNDirEntry> entries;
        try {
            entries = castList(SVNDirEntry.class, repository.getDir(path, -1, null, (Collection<SVNDirEntry>) null));
            Iterator<SVNDirEntry> iterator = entries.iterator();
            while (iterator.hasNext()) {
                SVNDirEntry entry = (SVNDirEntry) iterator.next();
                repositoryEntryCount++;
                if (entry.getKind() == SVNNodeKind.DIR) {
                    countEntries((path.equals("")) ? entry.getName() : path + "/" + entry.getName());
                }
            }
        } catch (SVNException exception) {
            logger.exception(exception);
        }
        return repositoryEntryCount;
    }

    /**
     * Gets all changes in the working copy.
     * 
     * @param path
     *            File path to working copy
     * @param isRemote
     *            check against repository
     * @return List with all files that contain changes
     */
    public List<File> getStatus(File path, boolean isRemote) {
        return doStatus(path, isRemote);
    }

    /**
     * Casts a Collection to a List with TypeSafety.
     * 
     * @param clazz
     *            Class for type safety
     * @param collection
     *            The collection to be casted.
     * @param <T>
     *            Type
     * @return List with casted collection
     */
    private static <T> List<T> castList(Class<? extends T> clazz, Collection<?> collection) {
        List<T> list = new ArrayList<T>(collection.size());
        for (Object object : collection) {
            list.add(clazz.cast(object));
        }
        return list;
    }

    @Override
    public Set<Role> getRoles() {
        return RoleFetcher.getUserRoles();
    }

    @Override
    public void setRepositoryURL(String repoURL) {
        // repoURL shall not occur as default is loaded from conf.properties -
        // anyway we keep this as last resort
        if (repoURL == null) {
            repoURL = "https://projects.sse.uni-hildesheim.de/svn/QualiMaster/trunk/experiments/QM2";
        }
        repositoryURL = repoURL;
    }

    @Override
    public UserContext getUserContext() {
        return UserContext.INSTANCE;
    }

    /**
     * Returns the number of entries in the repository.
     * 
     * @return number of entries in the repository
     */
    @Override
    public int getRepositoryEntryCount() {
        if (repositoryEntryCount == 0) {
            countEntries("");
        }
        return repositoryEntryCount;
    }

    @Override
    public int getChangesCount(File destinationFile, boolean isRemote) {
        return doStatus(destinationFile, isRemote).size();
    }

    @Override
    public void setUpdateEventHandler(RepositoryEventHandler handler) {
        updateEventHandler.setHandler(handler);
    }

    @Override
    public void setCommitEventHandler(RepositoryEventHandler handler) {
        commitEventHandler.setHandler(handler);
    }

    /**
     * Creates a local repository for testing purposes.
     * @param path Path to local repository.
     */
    public void createLocalRepository(String path) {
        try {
            File file = new File(path);
            SVNURL url = SVNRepositoryFactory.createLocalRepository(file, true, true);
            FSRepositoryFactory.setup();
            repository = SVNRepositoryFactory.create(url);
            System.out.println(repository.getLocation());
            UserContext.INSTANCE.init("test", "test", repository);
        } catch (SVNException e) {
            logger.exception(e);
        }
    }

}
