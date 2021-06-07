package org.gusdb.wdk.model.user.dataset.event;

import java.util.*;
import java.util.stream.Collectors;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.user.dataset.*;
import org.gusdb.wdk.model.user.dataset.event.model.*;
import org.gusdb.wdk.model.user.dataset.event.model.UserDatasetShareEvent.ShareAction;
import org.gusdb.wdk.model.user.dataset.event.raw.UDDependency;
import org.gusdb.wdk.model.user.dataset.event.raw.UDEvent;
import org.gusdb.wdk.model.user.dataset.event.raw.UDType;
import org.gusdb.wdk.model.user.dataset.event.repo.UserDatasetEventRepo;

/**
 * This object accepts and events file in the form of a json array
 *
 * @author crisl-adm
 */
public class UserDatasetEventSync extends UserDatasetEventProcessor
{
  private static final Logger logger = Logger.getLogger(UserDatasetEventSync.class);

  private static final String LogStrSkipEvent = "%s event %d refers to typeHandler %s which is not"
    + " present in the wdk configuration. Skipping the install but declaring the event as handled.";

  public UserDatasetEventSync(String projectId) throws WdkModelException {
    super(projectId);
  }

  /**
   * A list of user dataset events is processed.  Only events that have not been
   * handled to date are processed.  The nature of the processing is defined by
   * the user dataset event object (install, uninstall, share).
   *
   * @param eventList list of user dataset event to be processed. database
   *                  records in tables.
   */
  public void handleEventList(List<UserDatasetEvent> eventList) throws WdkModelException {

    try (
      final var appDb     = openAppDB();
      final var dsSession = getUserDatasetStore().getSession()
    ) {
      final var handler = initHandler(appDb.getDataSource(), dsSession);

      int count = 0;

      // If the subject user dataset does not have a currently supported type
      // handler the install or uninstall command will be skipped.
      //
      // In theory, no user dataset with an unsupported type handler should ever
      // be installed on the system as type handlers should only be added and
      // removed at release time when the UD database is emptied.
      for (final var event : eventList) {

        final var eventRow = new EventRow(
          event.getEventId(),
          event.getUserDatasetId(),
          event.getUserDatasetType()
        );

        // see handler method for event skipping criteria.
        if (handler.shouldHandleEvent(eventRow))
          continue;

        // Exceptions thrown here are considered a catastrophic failure and all
        // processing should stop.
        if (!handler.acquireEventLock(eventRow))
          continue;

        // Exceptions thrown in this try block are considered "recoverable" and
        // should not stop event processing.  Instead, the individual events
        // will be marked as failed.
        try {

          // If the event does not apply to this project, complete the event
          // handling and skip to the next event.
          if (!event.getProjectsFilter().contains(getProjectId())) {
            handler.handleNoOpEvent(eventRow);
            count++;
            continue;
          }

          if (event instanceof UserDatasetInstallEvent) {
            final var typeHandler = getUserDatasetStore().getTypeHandler(event.getUserDatasetType());

            // If the user dataset type is unsupported:
            if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {

              // Write out a warning that we are skipping this install event.
              logger.warn(skipLog("Install", event));

              // Mark the event as "completed"
              handler.handleNoOpEvent(eventRow);

            } else {
              handler.handleInstallEvent((UserDatasetInstallEvent) event, typeHandler);
            }

          } else if (event instanceof UserDatasetUninstallEvent) {
            final var typeHandler = getUserDatasetStore().getTypeHandler(event.getUserDatasetType());

            // If the user dataset type is unsupported:
            if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {

              // Write out a warning that we are skipping this uninstall event.
              logger.warn(skipLog("Uninstall", event));

              // Mark the event as "completed"
              handler.handleNoOpEvent(eventRow);

            } else {
              handler.handleUninstallEvent((UserDatasetUninstallEvent) event, typeHandler);
            }
          } else if (event instanceof UserDatasetShareEvent) {
            handler.handleShareEvent((UserDatasetShareEvent) event);
          }

          count++;
        } catch (Exception e) {
          logger.warn("Event processing failed for event " + event.getEventId(), e);

          // If this call fails, it is a catastrophic failure, the outer try
          // block will catch it and all processing will stop.
          handler.markEventAsFailed(eventRow);
        }
      }
      logger.info("Handled " + count + " new events");
    } catch (Exception e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Find the highest event id in the app db's handled events log.  Null if
   * none.
   */
  public Long findLastHandledEvent(DataSource appDbDataSource) {
    final var db = new UserDatasetEventRepo(getUserDatasetSchemaName(), appDbDataSource);
    final var opt = db.getEarliestCleanupCompleteEvent();

    if (opt.isPresent())
      return opt.get();

    final var out = db.getLastHandledEvent();

    return out == 0 ? null : out;
  }

  /**
   * Accepts an array of event json objects from a Jenkins job that composes the
   * content a series of event files containing json objects into a single json
   * array and returns a corresponding list of UserDatasetEvent objects.
   *
   * @param rawEvents List containing the raw deserialized user dataset events.
   *
   * @return - a list of UserDatasetEvents corresponding to
   */
  public static List<UserDatasetEvent> parseEventsArray(List<UDEvent> rawEvents) {
    return rawEvents.stream()
      .map(UserDatasetEventSync::parseEventObject)
      .collect(Collectors.toList());
  }

  protected UserDatasetEventSyncHandler initHandler(
    DataSource appDbDs,
    UserDatasetSession udSession
  ) {
    return new UserDatasetEventSyncHandler(
      udSession,
      appDbDs,
      getModelConfig().getWdkTempDir(),
      getUserDatasetSchemaName(),
      getProjectId()
    );
  }

  /**
   * Parse the individual JSONObject representing an event and call the
   * appropriate event handler based on the information contained within that
   * object.
   *
   * @param rawEvent JSONObject representing the event
   */
  private static UserDatasetEvent parseEventObject(UDEvent rawEvent) {
    switch (rawEvent.getEvent()) {
      // Dataset is in the user's workspace and now needs to be installed into the
      // database.
      case INSTALL:
        return new UserDatasetInstallEvent(
          rawEvent.getEventID(),
          rawEvent.getProjects(),
          rawEvent.getDatasetID(),
          getDsType(rawEvent.getType()),
          rawEvent.getOwner(),
          convertDeps(rawEvent.getDependencies())
        );

      // Dataset has been deleted from the workspace and now needs to be removed
      // from the database.
      case UNINSTALL:
        return new UserDatasetUninstallEvent(
          rawEvent.getEventID(),
          rawEvent.getProjects(),
          rawEvent.getDatasetID(),
          getDsType(rawEvent.getType())
        );

      // Dataset sharing has either been granted or revoked in the workspace and
      // now must be reflected in the database
      case SHARE:
        return new UserDatasetShareEvent(
          rawEvent.getEventID(),
          rawEvent.getProjects(),
          rawEvent.getDatasetID(),
          getDsType(rawEvent.getType()),
          rawEvent.getOwner(),
          rawEvent.getRecipient(),
          ShareAction.values()[rawEvent.getAction().ordinal()]
        );

      default:
        throw new WdkRuntimeException("Unrecognized user dataset event type: "
          + rawEvent.getEvent().externalValue());
    }
  }

  private static UserDatasetType getDsType(UDType raw) {
    return UserDatasetTypeFactory.getUserDatasetType(raw.getName(), raw.getVersion());
  }

  private static Set<UserDatasetDependency> convertDeps(Collection<UDDependency> raw) {
    return raw.stream()
      .map(UserDatasetEventSync::convertDep)
      .collect(Collectors.toSet());
  }

  private static UserDatasetDependency convertDep(UDDependency raw) {
    return new UserDatasetDependency(raw.getIdentifier(), raw.getVersion(), raw.getDisplayName());
  }

  /**
   * Formats a log string indicating that the given event type for the given
   * event is being skipped due to an absent type handler.
   *
   * @param ev  Event type: Install | Uninstall | Share
   * @param ude Event
   *
   * @return A formatted log string.
   */
  private static String skipLog(String ev, UserDatasetEvent ude) {
    return String.format(LogStrSkipEvent, ev, ude.getEventId(), ude.getUserDatasetType());
  }
}
