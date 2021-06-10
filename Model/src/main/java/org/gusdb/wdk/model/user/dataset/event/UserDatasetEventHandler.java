package org.gusdb.wdk.model.user.dataset.event;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.user.dataset.UserDatasetTypeHandler;
import org.gusdb.wdk.model.user.dataset.event.model.EventError;
import org.gusdb.wdk.model.user.dataset.event.model.EventRow;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetEvent;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetUninstallEvent;
import org.gusdb.wdk.model.user.dataset.event.repo.InstalledUserDatasetRepo;
import org.gusdb.wdk.model.user.dataset.event.repo.UserDatasetEventRepo;
import org.gusdb.wdk.model.user.dataset.event.repo.UserDatasetOwnerRepo;
import org.gusdb.wdk.model.user.dataset.event.repo.UserDatasetShareRepo;

/**
 * Handle events that impact which user datasets a user can use in this website.
 * <p>
 * We use the word "installed" to mean that a user dataset is available for use
 * on this website for this user.  It never means anything else.
 * <p>
 * Three database tables control if a user sees a dataset as installed.
 * <ol>
 *   <li>
 *     The InstalledUserDataset table holds the IDs of all datasets that are
 *     installed for use on this site. It includes the name of the UD, to show
 *     to the user in parameters in WDK Searches.
 *   </li>
 *   <li>
 *     The UserDatasetOwner table tells us who owns the UD that is in the
 *     InstalledUserDataset table. It has a foreign key to the
 *     InstalledUserDataset table.
 *   </li>
 *   <li>
 *     the UserDatasetSharedWith table tells us who has share access to an
 *     installed UD.  has a foreign key to the InstalledUserDataset table.
 *   </li>
 * </ol>
 * <p>
 * An install event causes the UD to be inserted into the install table and the
 * owner table.
 * <p>
 * A share event causes the a row to be inserted into the shared table, (and
 * unshare is vice versa)
 * <p>
 * A delete event causes rows from share, owner and install table to be
 * removed.
 * <p>
 * To see which UDs a user has installed, we query the union of the Owner and
 * Shared table.
 *
 * @author Steve
 */
// TODO: it seems we should add the owner as a column to the
//       InstalledUserDatasets table, and lose the Owner table, since they are 1-1
// TODO: if the user changes the name of their UD, this will not be reflected in
//       installed UDs, since there is no event to convey that.
public abstract class UserDatasetEventHandler
{
  private static final Logger LOG = LogManager.getLogger(UserDatasetEventHandler.class);

  private final DataSource dataSource;

  private final UserDatasetEventRepo     eventRepo;
  private final InstalledUserDatasetRepo installRepo;
  private final UserDatasetShareRepo     shareRepo;
  private final UserDatasetOwnerRepo     ownerRepo;

  private final Set<Long>        externallyClaimedDatasets;
  private final List<EventError> errors;

  private final Path        tmpDir;
  private final String      projectId;
  private final ModelConfig modelConfig;


  public UserDatasetEventHandler(
    final DataSource ds,
    final String dsSchema,
    final String projectId,
    final ModelConfig model
  ) {
    this.dataSource  = ds;
    this.tmpDir      = model.getWdkTempDir();
    this.projectId   = projectId;
    this.modelConfig = model;

    this.eventRepo   = new UserDatasetEventRepo(dsSchema, ds);
    this.installRepo = new InstalledUserDatasetRepo(dsSchema, ds);
    this.shareRepo   = new UserDatasetShareRepo(dsSchema, ds);
    this.ownerRepo   = new UserDatasetOwnerRepo(dsSchema, ds);

    this.externallyClaimedDatasets = new HashSet<>();

    this.errors = new ArrayList<>();
  }

  /**
   * Checks if the given event should be handled.
   * <p>
   * See specific implementations for criteria handleable events must meet.
   */
  public abstract boolean shouldHandleEvent(EventRow row);

  /**
   * Attempts to acquire a process lock on the event row.  When locked, other
   * simultaneous executions of this tool will ignore the event.
   * <p>
   * If another process has claimed the event this method will do nothing and
   * return false.
   * <p>
   * When claimed, the event will be recorded or updated in the DB with an
   * 'in progress' status.
   *
   * @param row Row to attempt to lock.
   *
   * @return {@code true} if the row could be locked and now "belongs to" this
   * process.  {@code false} if another process has already claimed this event
   * row.
   */
  protected abstract boolean attemptEventLock(EventRow row);

  /**
   * Marks an event as failed in the DB.  All future events for this UD should
   * be ignored.
   *
   * @param row Row representing the event to mark as failed.
   */
  protected abstract void _markEventAsFailed(EventRow row);

  /**
   * Marks an event as failed in the DB and records it for error reporting.
   *
   * @param row   Row representing the event to mark as failed.
   * @param cause Exception thrown that caused this event processing failure.
   */
  public void failEvent(EventRow row, Exception cause) {
    this.errors.add(new EventError(row, cause));
    this._markEventAsFailed(row);
  }

  /**
   * Method to handle an event that is either not relevant to this WDK project
   * or is related to an unsupported type.  The event is not installed but it is
   * noted in the dataset as handled so that the event is not repeatedly and
   * unnecessarily processed.
   */
  public void handleNoOpEvent(EventRow row) {
    closeEventHandling(row);
  }

  /**
   * Send an error email containing details about all the events that could not
   * be processed.
   */
  public void sendErrorNotifications() throws WdkModelException {
    // No errors, no notifications to send.
    if (errors.isEmpty())
      return;

    LOG.info("Sending error email");

    Utilities.sendEmail(
      modelConfig.getSmtpServer(),
      "epharper@upenn.edu", //modelConfig.getAdminEmail(),
      "do-not-reply@apidb.org",
      "User Dataset Event Processing Errors",
      buildErrorEmailBody()
    );
  }

  public boolean acquireEventLock(EventRow row) {
    // Dataset has been marked as claimed by another process.  Cannot acquire a
    // lock without potential race conditions so don't bother trying.
    // (See comment below)
    if (externallyClaimedDatasets.contains(row.getUserDatasetID()))
      return false;

    LOG.info("Attempting to acquire a lock on UD {}", row.getUserDatasetID());

    var out = attemptEventLock(row);

    // If someone else has claimed this event, add the dataset ID to the list of
    // ignored datasets to prevent this process from handling any further events
    // for that dataset.
    //
    // This is done to prevent race conditions such as an install event starting
    // in process 1 and a share event starting in process 2.  The share event in
    // process 2 will fail if process 1 does not complete the install first.
    if (!out)
      externallyClaimedDatasets.add(row.getUserDatasetID());

    return out;
  }

  public void handleUninstallEvent(
    UserDatasetUninstallEvent event,
    UserDatasetTypeHandler typeHandler
  ) throws WdkModelException {
    LOG.info("Uninstalling user dataset " + event.getUserDatasetId());

    revokeAllAccess(event.getUserDatasetId());
    typeHandler.uninstallInAppDb(event.getUserDatasetId(), getTmpDir(), getProjectId());

    installRepo.deleteUserDataset(event.getUserDatasetId());

    closeEventHandling(event);
  }

  protected UserDatasetShareRepo getShareRepo() {
    return shareRepo;
  }

  protected UserDatasetOwnerRepo getOwnerRepo() {
    return ownerRepo;
  }

  protected InstalledUserDatasetRepo getInstallRepo() {
    return installRepo;
  }

  protected UserDatasetEventRepo getEventRepo() {
    return eventRepo;
  }

  protected Path getTmpDir() {
    return tmpDir;
  }

  protected String getProjectId() {
    return projectId;
  }

  protected DataSource getDataSource() {
    return dataSource;
  }

  protected void revokeAllAccess(long userDatasetId) {
    LOG.info("Revoking all access to user dataset " + userDatasetId);
    ownerRepo.deleteAllOwners(userDatasetId);
    shareRepo.deleteAllShares(userDatasetId);
  }

  protected abstract void closeEventHandling(EventRow row);

  protected void closeEventHandling(UserDatasetEvent event) {
    closeEventHandling(new EventRow(
        event.getEventId(),
        event.getUserDatasetId(),
        event.getEventType(),
        event.getUserDatasetType()
      )
    );
  }

  private String buildErrorEmailBody() {
    var body = new StringBuilder();
    body.append("<body><h1>Event processing errors for ")
      .append(projectId)
      .append("</h1>");

    body.append("<p><b>URL:</b>")
      .append(modelConfig.getWebServiceUrl())
      .append("</p>");

    for (var err : this.errors) {
      err.toString(body);
    }

    body.append("</body>");

    return body.toString();
  }
}
