package org.gusdb.wdk.model.user.dataset.event.datastore;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.*;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.UserDatasetType;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventStatus;
import org.gusdb.wdk.model.user.dataset.event.model.EventRow;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEventType;

/**
 * Repository for DB actions related to the {@code UserDatasetEvent} DB table.
 * <p>
 * {@code UserDatasetEvent} Table:
 * <table>
 *   <caption>User Dataset Event Columns Definition</caption>
 *   <tr>
 *     <th>Column</th>
 *     <th>Type</th>
 *     <th>Notes</th>
 *   </tr>
 *   <tr>
 *     <td><code>EVENT_ID</code></td>
 *     <td><code>long</code></td>
 *     <td>iRODS generated event ID.</td>
 *   </tr>
 *   <tr>
 *     <td><code>EVENT_TYPE</code></td>
 *     <td><code>{@link UserDatasetEventType}</code></td>
 *     <td>Type of the event in iRODS</td>
 *   </tr>
 *   <tr>
 *     <td><code>STATUS</code></td>
 *     <td>{@link UserDatasetEventStatus}</td>
 *     <td>Last updated status for the event.</td>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_ID</code></td>
 *     <td><code>long</code></td>
 *     <td>ID for the user dataset this event relates to.</td>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_TYPE_NAME</code></td>
 *     <td>{@link String}</td>
 *     <td>Name of the user dataset type.</td>
 *   </tr>
 *   <tr>
 *     <td><code>USER_DATASET_TYPE_VERSION</code></td>
 *     <td>{@link String}</td>
 *     <td>Version of the user dataset type.</td>
 *   </tr>
 *   <tr>
 *     <td><code>HANDLED_TIME</code></td>
 *     <td>{@link java.time.LocalDateTime}</td>
 *     <td>Timestamp for when the event was first handled.</td>
 *   </tr>
 * </table>
 */
public class UserDatasetEventDBActions
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventDBActions.class);

  private static final String TABLE_USER_DATASET_EVENT = "userdatasetevent";

  private final String     schema;
  private final DataSource ds;

  public UserDatasetEventDBActions(String schema, DataSource ds) {
    this.schema = schema;
    this.ds     = ds;
  }

  /**
   * Retrieves the event ID for the last "completed" event.  If no such row is
   * found, {@code 0} is returned.
   *
   * @return The event ID for the last "completed" event if such an event
   * exists, else returns {@code 0};
   */
  public long getLastHandledEvent() {
    var sql = "SELECT MAX(event_id) FROM " + schema + TABLE_USER_DATASET_EVENT
      + " WHERE status = '" + UserDatasetEventStatus.COMPLETE + "'";

    var ret = new SQLRunner(ds, sql).executeQuery(rs -> rs.next() ? rs.getLong(1) : 0);

    LOG.info("Last completed event ID = {}", ret);

    return ret;
  }

  /**
   * Updates the status of an event row in the DB to reflect the status value
   * of the given {@link EventRow} instance.
   *
   * @param row Row instance representing the DB row to update.
   */
  public void updateEventStatus(EventRow row) {
    var sql = "UPDATE " + schema + TABLE_USER_DATASET_EVENT
      + " SET status = ?"
      + " WHERE event_id = ?";

    new SQLRunner(ds, sql).executeUpdate(
      new Object[]{row.getStatus().internalValue(), row.getEventID()},
      new Integer[]{Types.VARCHAR, Types.BIGINT}
    );
  }

  /**
   * Fetches a set of event IDs for events that are in the status
   * "cleanup_complete".
   *
   * @return Set of event IDs.
   */
  public HashSet<Long> getCleanupCompleteEventIDs() {
    var sql = "SELECT event_id FROM " + schema + TABLE_USER_DATASET_EVENT
      + " WHERE status = '" + UserDatasetEventStatus.CLEANUP_COMPLETE + "'";

    var ret = new SQLRunner(ds, sql).executeQuery(rs -> {
      var out = new HashSet<Long>();
      while (rs.next())
        out.add(rs.getLong(1));
      return out;
    });

    LOG.info("Fetched {} recovered events", ret.size());

    return ret;
  }

  /**
   * Fetches the event ID for the first sequential event in the
   * {@link UserDatasetEventStatus#CLEANUP_COMPLETE} status.
   *
   * @return An option containing the first event ID for an event that is
   * {@code CLEANUP_COMPLETE}.  If no records were found in this status, an
   * empty option is returned.
   */
  public Optional<Long> getEarliestCleanupCompleteEvent() {
    var sql = "SELECT MIN(event_id) FROM " + schema + TABLE_USER_DATASET_EVENT
      + " WHERE status = '" + UserDatasetEventStatus.CLEANUP_COMPLETE + "'";

    return new SQLRunner(ds, sql)
      .executeQuery(rs -> {
        if (!rs.next())
          return Optional.empty();

        final var id = rs.getLong(1);

        return rs.wasNull() ? Optional.empty() : Optional.of(id);
      });
  }

  /**
   * Retrieves a list of EventRows in the DB that are currently in the
   * {@code CLEANUP_READY} status.
   * <p>
   * This method returns a snapshot at the time of the call and makes no
   * guarantee that any individual row will be in the {@code CLEANUP_READY}
   * status once it is time to process that event.  The row should be locked
   * before processing using the {@link #claimCleanupEvent(EventRow)} method.
   *
   * @return A list of zero or more event rows that were in the
   * {@code CLEANUP_READY} status as of the time this method was called.
   */
  public List<EventRow> getCleanupReadyEvents() {
    var sql = "SELECT\n"
      + "  event_id\n"
      + ", event_type\n"
      + ", user_dataset_id\n"
      + ", user_dataset_type_name\n"
      + ", user_dataset_type_version\n"
      + ", handled_time\n"
      + "FROM " + schema + TABLE_USER_DATASET_EVENT + "\n"
      + "WHERE status = '" + UserDatasetEventStatus.CLEANUP_READY + "'";

    var ret = new SQLRunner(ds, sql)
      .executeQuery((SQLRunner.ResultSetHandler<List<EventRow>>) rs -> {
        var out = new ArrayList<EventRow>();

        while (rs.next()) {
          out.add(new EventRow(
            rs.getLong(1),
            rs.getLong(3),
            rs.getObject(6, LocalDateTime.class),
            UserDatasetEventStatus.CLEANUP_READY,
            UserDatasetEventType.fromString(rs.getString(2)),
            new UserDatasetType(rs.getString(4), rs.getString(5))
          ));
        }

        return out;
      });

    LOG.info("Fetched {} cleanup ready events from the DB.", ret.size());

    return ret;
  }

  /**
   * Fetches a set of dataset IDs for datasets that have an event in a status
   * other than {@code COMPLETE} or {@code CLEANUP_COMPLETE}.
   * <p>
   * User datasets with an event in any other status either have a failed or
   * have been "claimed" by another process.
   *
   * @return Set of dataset IDs.
   */
  public HashSet<Long> getIgnoredDatasetIDs() {
    var sql = "SELECT user_dataset_id FROM " + schema + TABLE_USER_DATASET_EVENT
      + " WHERE status NOT IN ("
      + "'" + UserDatasetEventStatus.COMPLETE + "',"
      + "'" + UserDatasetEventStatus.CLEANUP_COMPLETE + "'"
      + ")";

    var ret = new SQLRunner(ds, sql).executeQuery(rs -> {
      var out = new HashSet<Long>();
      while (rs.next())
        out.add(rs.getLong(1));
      return out;
    });

    LOG.info("Fetched {} ignorable dataset IDs", ret.size());

    return ret;
  }

  /**
   * Attempts to claim the given sync event row (creating it if it wasn't
   * previously in the DB).
   * <p>
   * Additionally updates the passed row instance status field to either an
   * up-to-date status from the DB or
   * {@link UserDatasetEventStatus#PROCESSING}.
   *
   * @param event row to claim
   *
   * @return {@code true} if the event was successfully claimed and is safe to
   * process further.  {@code false} if the event has already been claimed by
   * another process.
   */
  public boolean claimSyncEvent(EventRow event) {
    try (var con = ds.getConnection()) {
      // Begin connection rollback block. (Rolls back if a failure occurs)
      try {
        // Lock the table to prevent race conditions with other processes.
        lockEventTable(con);

        // Get an optional status.  Option will be empty if no row yet exists
        // for this event.
        var optStat = getEventStatus(con, event);

        // Row did not previously exist, insert it to prevent other processes
        // from attempting to process this row.
        // Return true to indicate that this row has been "locked"
        if (optStat.isEmpty()) {
          event.setStatus(UserDatasetEventStatus.PROCESSING);
          insertClaimedEvent(con, event);
          unlockEventTable(con);
          return true;
        }

        // If someone else has updated (or "claimed") the row, return false as
        // another process has this event and it should not be processed.
        if (optStat.get() != UserDatasetEventStatus.CLEANUP_COMPLETE) {
          event.setStatus(optStat.get());
          unlockEventTable(con);
          return false;
        }

        // If we've made it this far, the row previously existed in the
        // "cleanup_complete" status.  Update the status and return true to
        // indicate the row is "claimed"
        event.setStatus(UserDatasetEventStatus.PROCESSING);
        updateEventStatus(con, event);
        unlockEventTable(con);
        return true;

      } catch (SQLException ex) {
        LOG.warn("Exception caught while attempting to claim event row.  Rolling back transaction.");
        con.rollback();
        throw new WdkRuntimeException(ex);
      }
    } catch (SQLException ex) {
      throw new WdkRuntimeException(ex);
    }
  }

  /**
   * Attempts to claim the given event row by setting the event status to
   * {@link UserDatasetEventStatus#CLEANUP_PROCESSING}.
   * <p>
   * Additionally updates the passed row instance status field to either an
   * up-to-date status from the DB or
   * {@link UserDatasetEventStatus#CLEANUP_PROCESSING}.
   *
   * @param event row to claim
   *
   * @return {@code true} if the event was successfully claimed and is safe to
   * process further.  {@code false} if the event has already been claimed by
   * another process.
   */
  public boolean claimCleanupEvent(EventRow event) {
    try (var con = ds.getConnection()) {
      // Begin connection rollback block. (Rolls back if a failure occurs)
      try {
        // Lock the table to prevent race conditions with other processes.
        lockEventTable(con);

        // Get an optional status.  Option will be empty if no row yet exists
        // for this event.
        var optStat = getEventStatus(con, event);

        // Row no longer exists?  Someone is probably manually mucking about in
        // the DB or something if this happens.
        if (optStat.isEmpty()) {
          unlockEventTable(con);
          return true;
        }

        // If someone else has updated (or "claimed") the row, return false as
        // another process has this event and it should not be processed.
        if (optStat.get() != UserDatasetEventStatus.CLEANUP_READY) {
          event.setStatus(optStat.get());
          unlockEventTable(con);
          return false;
        }

        // If we've made it this far, the row is in the "cleanup_ready" status.
        // Update the status and return true to indicate the row is "claimed"
        event.setStatus(UserDatasetEventStatus.CLEANUP_PROCESSING);
        updateEventStatus(con, event);
        unlockEventTable(con);
        return true;

      } catch (SQLException ex) {
        LOG.warn("Exception caught while attempting to lock event row.  Rolling back transaction.");
        con.rollback();
        throw new WdkRuntimeException(ex);
      }
    } catch (SQLException ex) {
      throw new WdkRuntimeException(ex);
    }
  }

  /**
   * Acquires a lock on the events table to prevent race conditions where other
   * processes attempt to operate on the same row as this process.
   *
   * @param con Open JDBC connection.
   */
  private void lockEventTable(Connection con) throws SQLException {
    var sql = "LOCK TABLE " + schema + TABLE_USER_DATASET_EVENT + " IN EXCLUSIVE MODE";

    con.setAutoCommit(false);

    // Use SQLRunner as this query may have to wait for other table locks to
    // release, making this a potentially "slow" query.
    new SQLRunner(con, sql, "lock-event-table").executeUpdate();
  }

  /**
   * Commits the open JDBC transaction, unlocking the events table for other
   * processes.
   *
   * @param con Open JDBC transaction.
   */
  private void unlockEventTable(Connection con) throws SQLException {
    con.commit();
  }

  /**
   * Inserts a new "claimed" event row in the
   * {@link UserDatasetEventStatus#PROCESSING} status.
   *
   * @param con Open JDBC transaction.
   * @param row Row to insert.
   */
  private void insertClaimedEvent(Connection con, EventRow row) throws SQLException {
    var sql = "INSERT INTO\n"
      + "  " + schema + TABLE_USER_DATASET_EVENT + "(\n"
      + "    event_id\n"
      + "  , event_type\n"
      + "  , status\n"
      + "  , user_dataset_id\n"
      + "  , user_dataset_type_name\n"
      + "  , user_dataset_type_version\n"
      + "  , handled_time\n"
      + "  )"
      + "VALUES\n"
      + "  (?, ?, ?, ?, ?, ?, ?)";

    try (var stmt = con.prepareStatement(sql)) {
      stmt.setLong(1, row.getEventID());
      stmt.setString(2, row.getEventType().internalValue());
      stmt.setString(3, row.getStatus().internalValue());
      stmt.setLong(4, row.getUserDatasetID());
      stmt.setString(5, row.getUserDatasetType().getName());
      stmt.setString(6, row.getUserDatasetType().getVersion());
      stmt.setObject(7, row.getHandledTime(), Types.TIMESTAMP);

      stmt.executeUpdate();
    }
  }

  /**
   * Updates the given row's status in the DB (and the row instance) to
   * "processing".
   *
   * @param con Open JDBC transaction.
   * @param row Row to update.
   */
  private void updateEventStatus(Connection con, EventRow row) throws SQLException {
    var sql = "UPDATE " + schema + TABLE_USER_DATASET_EVENT
      + " SET status = ?"
      + " WHERE event_id = ?";

    try (var ps = con.prepareStatement(sql)) {
      ps.setString(1, row.getStatus().internalValue());
      ps.setLong(2, row.getEventID());

      ps.executeUpdate();
    }
  }

  /**
   * Gets the current status for the given event.
   * <p>
   * If no such event exists, an empty option is returned.
   * If an event is found, the current row status is returned in addition to
   * the passed row's status field being updated.
   *
   * @param con Open JDBC transaction.
   * @param row Row to update/fetch event status for.
   *
   * @return An option that will contain the current event status if the event
   * exists in the DB.
   */
  private Optional<UserDatasetEventStatus> getEventStatus(Connection con, EventRow row)
  throws SQLException {
    var sql = "SELECT status FROM " + schema + TABLE_USER_DATASET_EVENT
      + " WHERE event_id = ?";

    try (var ps = con.prepareStatement(sql)) {
      ps.setLong(1, row.getEventID());

      try (var rs = ps.executeQuery()) {
        if (!rs.next())
          return Optional.empty();

        var stat = UserDatasetEventStatus.fromInternalValue(rs.getString(1));

        row.setStatus(stat);

        return Optional.of(stat);
      }
    }
  }
}
