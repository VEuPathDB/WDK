package org.gusdb.wdk.model.user.dataset.event;

import java.util.List;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.dataset.event.model.EventRow;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventStatus;

public class UserDatasetEventCleanupHandler extends UserDatasetEventHandler
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventCleanupHandler.class);

  public UserDatasetEventCleanupHandler(
    DataSource ds,
    String dsSchema,
    String projectId,
    ModelConfig modelConfig
  ) {
    super(ds, dsSchema, projectId, modelConfig);
  }

  /**
   * Verifies that the given event row should be processed.
   * <p>
   * In the case of the cleanup handler, the event should always be processed.
   *
   * @return {@code true}
   */
  @Override
  public boolean shouldHandleEvent(EventRow row) {
    return true;
  }

  @Override
  protected boolean attemptEventLock(EventRow row) {
    return getEventRepo().lockCleanupEvent(row);
  }

  /**
   * Fetches a list of events from the DB that are in the
   * {@link UserDatasetEventStatus#CLEANUP_READY} status.
   * <p>
   * As this tool may be run with more than one simultaneous execution, the
   * status of these events may have changed by the time the processing reaches
   * them.  Attempting to lock the event will return false if the status has
   * changed.
   *
   * @return A list of events presently in the {@code CLEANUP_READY} status.
   */
  public List<EventRow> getCleanableEvents() {
    return getEventRepo().getCleanupReadyEvents();
  }

  @Override
  protected void _markEventAsFailed(EventRow row) {
    row.setStatus(UserDatasetEventStatus.CLEANUP_FAILED);
    getEventRepo().updateEventStatus(row);
  }

  @Override
  protected void closeEventHandling(EventRow row) {
    row.setStatus(UserDatasetEventStatus.CLEANUP_COMPLETE);
    getEventRepo().updateEventStatus(row);

    LOG.info("Done handling event: " + row.getEventID());
  }

  @Override
  protected String getRunModeName() {
    return "Cleanup";
  }
}
