package org.gusdb.wdk.model.user.dataset.event;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UnsupportedTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventStatus;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventType;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetUninstallEvent;

/**
 * Processes events in the {@link UserDatasetEventStatus#CLEANUP_READY} status
 * and attempts to uninstall those events.
 * <p>
 * Events that fail when attempting to uninstall will be updated to be in the
 * {@link UserDatasetEventStatus#CLEANUP_FAILED}.  Events in this status will
 * be ignored by future sync and cleanup runs.
 * <p>
 * Events that succeed when attempting to uninstall will be updated to the
 * {@link UserDatasetEventStatus#CLEANUP_COMPLETE} status.  Events in this
 * status will be picked up by the next sync run.
 * <p>
 * Events are placed in the {@code CLEANUP_READY} status after they have been
 * investigated and whatever the cause is resolved.
 */
public class UserDatasetEventCleanup extends UserDatasetEventProcessor
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventCleanup.class);

  public UserDatasetEventCleanup(String projectID) throws WdkModelException {
    super(projectID);
  }

  public void cleanupFailedInstalls() throws WdkModelException {
    LOG.info("Beginning user dataset event cleanup.");

    try (var appDb = openAppDB()) {

      var handler = initHandler(appDb.getDataSource());
      var events  = handler.getCleanableEvents();

      for (var event : events) {
        LOG.info("Processing event: " + event.getEventID());

        if (!handler.shouldHandleEvent(event)) {
          LOG.info("Skipping event");
          continue;
        }

        if (!handler.claimEvent(event)) {
          LOG.info("Event already claimed.");
          continue;
        }

        // Error recovery block.  Errors in this try/catch do not halt event
        // processing.  Instead when an error occurs, the event will be marked
        // with an error status and the process will continue.
        try {
          // We only want to actually attempt to uninstall events of the
          // "install" type.  Calling uninstall on other event types will leave
          // the DB in a bad state.
          //
          // For example, calling uninstall on a SHARE event, will cause the
          // successfully installed UD to be uninstalled even though it had no
          // issues.  This would result in the UD's INSTALL event being undone
          // without updating it's status.  Only the SHARE event would be marked
          // as CLEANUP_COMPLETE.  This means the DB would say the UD is
          // installed (and would prevent the UD being reinstalled) when it
          // actually is not in the DB at all.
          //
          // Since the other event types are effectively idempotent, rerunning
          // those events without doing any "cleanup" shouldn't cause any
          // issues.  Instead we just mark the event as CLEANUP_COMPLETE and
          // let the re-run of the event fix the issue.
          if (event.getEventType() != UserDatasetEventType.INSTALL) {
            LOG.info("Non-install event, marking as completed.");
            handler.handleNoOpEvent(event);
            continue;
          }

          var typeHandler = getUserDatasetStore().getTypeHandler(event.getUserDatasetType());

          // A type handler was removed after the event was "installed".  This
          // should never happen, but safety first.
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            var error = "Type handler for type " + event.getUserDatasetType().getName() + " has been removed.";
            LOG.error(error + "  Marking cleanup as failed.");
            handler.failEvent(event, new Exception(error));
            continue;
          }

          LOG.info("Attempting to run event cleanup.");

          handler.handleUninstallEvent(new UserDatasetUninstallEvent(
            event.getEventID(),
            null, // null as the set of projects is not known, but is also not used for uninstalls.
            event.getUserDatasetID(),
            event.getUserDatasetType()
          ), typeHandler);
        } catch (Exception ex) {
          LOG.warn("Exception occurred while attempting to process event cleanup.  Marking cleanup as failed.", ex);
          handler.failEvent(event, ex);
        }
      }

      handler.sendErrorNotifications();
    } catch (Exception ex) {
      LOG.error("Fatal error occurred, halting event processing.");
      throw new WdkModelException(ex);
    }
  }

  protected UserDatasetEventCleanupHandler initHandler(DataSource appDbDs) {
    return new UserDatasetEventCleanupHandler(
      appDbDs,
      getUserDatasetSchemaName(),
      getProjectId(),
      getModelConfig()
    );
  }
}
