package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.dataset.UserDataset;
import org.gusdb.wdk.model.user.dataset.UserDatasetSession;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.model.*;

public class UserDatasetEventSyncHandler extends UserDatasetEventHandler
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventSyncHandler.class);

  private static final int copyTimeoutMinutes = 180;

  private final HashSet<Long> failedDatasets;

  /**
   * Recovered Event IDs.
   * <p>
   * A set of all the IDs of events in the DB that are in the
   * {@link UserDatasetEventStatus#CLEANUP_COMPLETE} status.
   */
  private final HashSet<Long> recoveredEvents;

  /**
   * Previous Last Handled Event ID.
   * <p>
   * This is the event ID for the last event in the DB in the
   * {@link UserDatasetEventStatus#COMPLETE} status.
   * <p>
   * This value is unrelated to the value returned by the method
   * {@link UserDatasetEventSync#findLastHandledEvent(DataSource)}.  That value
   * is used to determine what to pull from iRODS but this value is what is
   * actually used to determine if a non-{@code CLEANUP_READY} event should be
   * handled.
   */
  private final long previousLastHandled;

  private final UserDatasetSession dsSession;

  public UserDatasetEventSyncHandler(
    UserDatasetSession dsSession,
    DataSource ds,
    String dsSchema,
    String projectId,
    ModelConfig modelConfig
  ) {
    super(ds, dsSchema, projectId, modelConfig);

    this.dsSession  = dsSession;

    failedDatasets      = getEventRepo().getIgnoredDatasetIDs();
    recoveredEvents     = getEventRepo().getCleanupCompleteEventIDs();
    previousLastHandled = getEventRepo().getLastHandledEvent();
  }

  @Override
  protected boolean attemptEventClaim(EventRow row) {
    return getEventRepo().claimSyncEvent(row);
  }

  public void handleShareEvent(UserDatasetShareEvent event) {
    LOG.info("Updating share of user dataset " + event.getUserDatasetId());

    if (!checkUserDatasetInstalled(event.getUserDatasetId())) {
      // this can happen if the install was skipped, because the ud was deleted first
      LOG.info("User dataset " + event.getUserDatasetId() + " is not installed. Skipping share.");
    } else {
      if (event.getAction() == UserDatasetShareEvent.ShareAction.GRANT)
        grantShareAccess(event.getOwnerId(), event.getRecipientId(),
          event.getUserDatasetId()
        );
      else
        revokeShareAccess(event.getOwnerId(), event.getRecipientId(),
          event.getUserDatasetId()
        );
    }
    closeEventHandling(event);
  }

  /**
   * Checks if the given event should be handled.
   * <p>
   * Returns {@code true} if all of the following conditions are met.
   * <ul>
   *   <li>The event ID falls after the last completed event OR the event is
   *   marked as "cleanup_complete".</li>
   *   <li>The event user dataset ID has no previous failures associated.</li>
   * </ul>
   */
  @Override
  public boolean shouldHandleEvent(EventRow row) {
    // User dataset has a failure associated with it, do not process.
    if (failedDatasets.contains(row.getUserDatasetID()))
      return false;

    // Event has been marked as retryable, process if we can lock it.
    if (recoveredEvents.contains(row.getEventID()))
      return true;

    // Event is after the last run's last handled event, process if we can lock
    // it.
    return row.getEventID() > previousLastHandled;
  }

  /**
   * Marks an event as failed in the DB.  All future events for this UD should
   * be ignored.  Calls to {@link #shouldHandleEvent(EventRow)} will return
   * {@code false} after this method is called for a UD.
   *
   * @param row Row representing the event to mark as failed.
   */
  @Override
  protected void _markEventAsFailed(EventRow row) {
    row.setStatus(UserDatasetEventStatus.FAILED);

    // Update the event status in the DB
    getEventRepo().updateEventStatus(row);

    // Add the user dataset ID to the failed list to avoid processing any
    // further events for this UD
    failedDatasets.add(row.getUserDatasetID());
  }

  public void handleInstallEvent(
    UserDatasetInstallEvent event,
    UserDatasetTypeHandler typeHandler
  ) throws WdkModelException {
    final var datasetId = event.getUserDatasetId();

    LOG.info("Installing user dataset " + datasetId);

    handleInstallEvent(typeHandler, event.getOwnerUserId(), datasetId);
    closeEventHandling(event);
  }

  /**
   * Checks if a user dataset is installed (in the installed table).
   * <p>
   * Operations that call this method are at theoretical risk of a race
   * condition, since this check is in its own transaction.  However, the chance
   * that a UD will be uninstalled in the intervening millisecond is small, not
   * worth engineering for.
   */
  protected boolean checkUserDatasetInstalled(long userDatasetId) {
    LOG.info("Checking if user dataset " + userDatasetId + " is installed");
    return getInstallRepo().isUserDatasetInstalled(userDatasetId);
  }

  /**
   * Adds a share to the UserDatasetSharedWith table.
   */
  protected void grantShareAccess(long ownerId, long recipientId, long userDatasetId) {
    LOG.info("Granting recipient " + recipientId + " access to user dataset "
      + userDatasetId + " belonging to owner " + ownerId);

    getShareRepo().insertShare(ownerId, recipientId, userDatasetId);
  }

  protected void revokeShareAccess(long ownerId, long recipientId, long userDatasetId) {
    LOG.info("Revoking access by recipient " + recipientId + " to user dataset "
      + userDatasetId + " belonging to owner " + ownerId);

    getShareRepo().deleteShare(ownerId, recipientId, userDatasetId);
  }

  protected void grantAccess(long userId, long userDatasetId) {
    LOG.info("Granting access to user dataset " + userDatasetId + " to user " + userId);

    getOwnerRepo().insertOwner(userId, userDatasetId);
  }


  protected void handleInstallEvent(
    final UserDatasetTypeHandler typeHandler,
    final Long ownerUserId,
    final Long datasetId
  ) throws WdkModelException {
    LOG.trace("UserDatasetEventHandler#handleInstallEvent");
    final Path              cwd;
    final UserDataset       userDataset;
    final Map<String, Path> files;

    // there is a theoretical race condition here, because this check is not
    // in the same transaction as the rest of this method.
    // but that risk is very small.
    if (!dsSession.getUserDatasetExists(ownerUserId, datasetId)) {
      LOG.info("User dataset" + datasetId
        + " not found in store.  Was probably deleted.  Skipping install.");
      return;
    }

    userDataset = dsSession.getUserDataset(ownerUserId, datasetId);

    // Weeding out obsolete user datasets - skipped but completed.
    var compatibility = typeHandler.getCompatibility(userDataset, getDataSource());

    if (!compatibility.isCompatible()) {
      LOG.info("User dataset " + datasetId + " deemed obsolete: "
        + compatibility.notCompatibleReason() + ".  Skipping install.");
      return;
    }

    cwd   = typeHandler.createWorkingDir(getTmpDir(), userDataset.getUserDatasetId());
    files = copyToLocalTimeout(dsSession, userDataset, typeHandler, cwd);

    // insert into the installedTable
    getInstallRepo().insertUserDataset(datasetId, userDataset.getMeta().getName());

    // insert into the type-specific tables
    typeHandler.installInAppDb(userDataset, cwd, getProjectId(), files);
    typeHandler.deleteWorkingDir(cwd);

    // grant access to the owner, by installing into the ownerTable
    grantAccess(ownerUserId, datasetId);
  }

  /**
   * Copies the contents of a user dataset from iRODS to a temporary working dir
   * under the given path {@code tmpDir} or fails if the copy takes longer than
   * {@link #copyTimeoutMinutes}.
   *
   * @param session     active iRODS session
   * @param dataset     user dataset from which the files should be copied.
   * @param typeHandler dataset type handler.  Used to create the working
   *                    directory and perform the file copy.
   * @param cwd         working directory path.  All relevant dataset files will
   *                    be copied from iRODS to this directory.
   *
   * @return a map of dataset file names to their path in the filesystem.
   *
   * @throws WdkModelException if an error occurs during the file copy, or if
   *                           the copy request times out.
   */
  protected Map<String, Path> copyToLocalTimeout(
    final UserDatasetSession session,
    final UserDataset dataset,
    final UserDatasetTypeHandler typeHandler,
    final Path cwd
  ) throws WdkModelException {
    LOG.trace("UserDatasetEventHandler#copyToLocalTimeout");

    final var exec = Executors.newSingleThreadExecutor();
    final var err  = new AtomicReference<WdkModelException>();
    final var out  = new AtomicReference<Map<String, Path>>();

    exec.execute(() -> {
      try {
        out.set(typeHandler.copyFilesToTemp(session, dataset, cwd));
      } catch (WdkModelException e) {
        err.set(e);
      }
    });

    exec.shutdown();
    try {
      if (!exec.awaitTermination(copyTimeoutMinutes, TimeUnit.MINUTES)) {
        throw new WdkModelException("Copy from iRODS to temp directory timed out after "
          + copyTimeoutMinutes
          + " minutes.");
      }
    } catch (InterruptedException e) {
      throw new WdkModelException(e);
    }

    if (err.get() != null)
      throw err.get();

    return out.get();
  }

  @Override
  protected void closeEventHandling(EventRow row) {
    row.setStatus(UserDatasetEventStatus.COMPLETE);
    getEventRepo().updateEventStatus(row);

    LOG.info("Done handling event: " + row.getEventID());
  }

  @Override
  protected String getRunModeName() {
    return "Sync";
  }
}
