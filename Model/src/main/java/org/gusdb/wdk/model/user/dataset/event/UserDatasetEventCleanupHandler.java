package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.user.dataset.event.model.EventRow;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventStatus;

public class UserDatasetEventCleanupHandler extends UserDatasetEventHandler
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventCleanupHandler.class);

  public UserDatasetEventCleanupHandler(
    DataSource ds,
    Path tmpDir,
    String dsSchema,
    String projectId
  ) {
    super(ds, tmpDir, dsSchema, projectId);
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
  public boolean attemptEventLock(EventRow row) {
    return getEventRepo().lockCleanupEvent(row);
  }

  public List<EventRow> getCleanableEvents() {
    return getEventRepo().getCleanupReadyEvents();
  }

  @Override
  public void markEventAsFailed(EventRow row) {
    row.setStatus(UserDatasetEventStatus.CLEANUP_FAILED);
    getEventRepo().updateEventStatus(row);
  }

  @Override
  protected void closeEventHandling(EventRow row) {
    row.setStatus(UserDatasetEventStatus.CLEANUP_COMPLETE);
    getEventRepo().updateEventStatus(row);

    LOG.info("Done handling event: " + row.getEventID());
  }
}
