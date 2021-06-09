package org.gusdb.wdk.model.user.dataset.event;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.UnsupportedTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetUninstallEvent;

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

      LOG.info("Found {} cleanable events.", events.size());

      for (var event : events) {
        LOG.info("Processing event: " + event.getEventID());

        if (!handler.shouldHandleEvent(event)) {
          LOG.info("Skipping event");
          continue;
        }

        if (!handler.acquireEventLock(event)) {
          LOG.info("Event already claimed.");
          continue;
        }

        // Error recovery block.  Errors in this try/catch do not halt event
        // processing.  Instead when an error occurs, the event will be marked
        // with an error status and the process will continue.
        try {
          var typeHandler = getUserDatasetStore().getTypeHandler(event.getType());

          // A type handler was removed after the event was "installed".  This
          // should never happen, but safety first.
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            LOG.error("Type handler for type {} has been removed.  Marking cleanup as failed.", event.getType().getName());
            handler.markEventAsFailed(event);
            continue;
          }

          LOG.info("Attempting to run event cleanup.");

          handler.handleUninstallEvent(new UserDatasetUninstallEvent(
            event.getEventID(),
            null, // null as this value is not known, but is also not used for uninstalls.
            event.getUserDatasetID(),
            event.getType()
          ), typeHandler);
        } catch (Exception ex) {
          LOG.warn("Exception occurred while attempting to process event cleanup.  Marking cleanup as failed.", ex);
          handler.markEventAsFailed(event);
        }
      }
    } catch (Exception ex) {
      LOG.error("Fatal error occurred, halting event processing.");
      throw new WdkModelException(ex);
    }
  }

  protected UserDatasetEventCleanupHandler initHandler(DataSource appDbDs) {
    return new UserDatasetEventCleanupHandler(
      appDbDs,
      getModelConfig().getWdkTempDir(),
      getUserDatasetSchemaName(),
      getProjectId()
    );
  }
}
