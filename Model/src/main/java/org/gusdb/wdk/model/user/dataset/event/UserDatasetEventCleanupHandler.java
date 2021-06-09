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

  private final Set<Long> ignoredDatasets;

  public UserDatasetEventCleanupHandler(
    DataSource ds,
    Path tmpDir,
    String dsSchema,
    String projectId
  ) {
    super(ds, tmpDir, dsSchema, projectId);

    ignoredDatasets = new HashSet<>();
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
    return !ignoredDatasets.contains(row.getUserDatasetID());
  }

  @Override
  public boolean acquireEventLock(EventRow row) {
    var out = getEventRepo().lockCleanupEvent(row);

    // If someone else has claimed this event, add the dataset ID to the list of
    // ignored datasets to prevent this process from handling any further events
    // for that dataset.
    //
    // This is done to prevent race conditions such as an install event starting
    // in process 1 and a share event starting in process 2.  The share event in
    // process 2 will fail if process 1 does not complete the install first.
    if (!out)
      ignoredDatasets.add(row.getUserDatasetID());

    return out;
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
