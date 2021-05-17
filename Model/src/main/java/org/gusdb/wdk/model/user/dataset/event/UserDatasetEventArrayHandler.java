package org.gusdb.wdk.model.user.dataset.event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;
import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler.Status;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.config.ModelConfig;
import org.gusdb.wdk.model.config.ModelConfigParser;
import org.gusdb.wdk.model.user.dataset.*;
import org.gusdb.wdk.model.user.dataset.event.UserDatasetShareEvent.ShareAction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.*;

/**
 * This object accepts and events file in the form of a json array
 *
 * @author crisl-adm
 */
public class UserDatasetEventArrayHandler
{
  private static final Logger logger = Logger.getLogger(UserDatasetEventArrayHandler.class);

  private UserDatasetStore userDatasetStore;
  private ModelConfig      modelConfig;
  private String           projectId;

  public UserDatasetEventArrayHandler(String projectId)
  throws WdkModelException {
    this.projectId   = projectId;
    modelConfig      = getModelConfig();
    userDatasetStore = getUserDatasetStore();
  }

  /**
   * A list of user dataset events is processed.  Only events that have not been
   * handled to date are processed.  The nature of the processing is defined by
   * the user dataset event object (install, uninstall, share).
   *
   * @param eventList list of user dataset event to be processed. database
   *                  records in tables.
   */
  public void handleEventList(List<UserDatasetEvent> eventList)
  throws WdkModelException {

    try (
      final var appDb = new DatabaseInstance(
        getModelConfig().getAppDB(),
        WdkModel.DB_INSTANCE_APP, true
      );
      final var dsSession = getUserDatasetStore().getSession()
    ) {

      final var appDbDataSource = appDb.getDataSource();
      final var lastHandledEventId = findLastHandledEvent(
        appDbDataSource,
        getUserDatasetSchemaName()
      );

      final var handler = new UserDatasetEventHandler(
        dsSession,
        appDbDataSource,
        getModelConfig().getWdkTempDir(),
        getUserDatasetSchemaName(),
        getProjectId()
      );

      int count = 0;

      // If the subject user dataset does not have a currently supported type
      // handler the install or uninstall command will be skipped.
      // In theory, no user dataset with an unsupported type handler should ever
      // be installed on the system as type handlers should only be added and
      // removed at release time when the UD database is emptied.
      for (final var event : eventList) {

        // If this event was handled before, skip to the next event - this
        // really shouldn't happen.
        if (lastHandledEventId != null && event.getEventId() <= lastHandledEventId)
          continue;

        // If the event does not apply to this project, complete the event
        // handling and skip to the next event.
        if (!event.getProjectsFilter().contains(getProjectId())) {
          handler.completeEventHandling(event.getEventId());
          count++;
          continue;
        }

        if (event instanceof UserDatasetInstallEvent) {
          final var typeHandler = userDatasetStore.getTypeHandler(event.getUserDatasetType());
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            logger.warn("Install event " + event.getEventId()
              + " refers to typeHandler " + event.getUserDatasetType()
              + " which is not present in the wdk configuration."
              + "Skipping the install but declaring the event as handled.");

            handler.completeEventHandling(event.getEventId());
            continue;
          } else {
            handler.handleInstallEvent((UserDatasetInstallEvent) event, typeHandler);
          }
        } else if (event instanceof UserDatasetUninstallEvent) {
          final var typeHandler = userDatasetStore.getTypeHandler(event.getUserDatasetType());
          if (UnsupportedTypeHandler.NAME.equals(typeHandler.getUserDatasetType().getName())) {
            logger.warn("Uninstall event " + event.getEventId()
              + " refers to typeHandler " + event.getUserDatasetType()
              + " which is not present in the wdk configuration."
              + "Skipping the uninstall but declaring the event as handled.");

            handler.completeEventHandling(event.getEventId());
          } else {
            handler.handleUninstallEvent((UserDatasetUninstallEvent) event, typeHandler);
          }
        } else if (event instanceof UserDatasetShareEvent) {
          handler.handleShareEvent((UserDatasetShareEvent) event);
        }

        count++;
      }
      logger.info("Handled " + count + " new events");
    } catch (Exception e) {
      throw new WdkModelException(e);
    }
  }

  /**
   * Find the highest event id in the app db's handled events log.  Null if
   * none.
   *
   * @throws WdkModelException if the log has a failed event (no complete date)
   *                           from a previous run.
   */
  public Long findLastHandledEvent(
    DataSource appDbDataSource,
    String userDatasetSchemaName
  ) throws WdkModelException {

    final var handler = new SingleLongResultSetHandler();

    // first confirm there are no failed events from the last run.  (They'll
    // have a null completed time)
    var sql = "select min(event_id) from " + userDatasetSchemaName
      + "UserDatasetEvent where completed is null";
    var sqlRunner = new SQLRunner(appDbDataSource, sql,
      "find-earliest-incomplete-event-id"
    );

    sqlRunner.executeQuery(handler);

    if (!handler.getStatus().equals(Status.NULL_VALUE)) {
      throw new WdkModelException("Event id " + handler.getRetrievedValue()
        + " failed to complete in a previous run");
    }

    // find highest previously handled event id
    sql = "select max(event_id) from " + userDatasetSchemaName
      + "UserDatasetEvent";

    sqlRunner = new SQLRunner(appDbDataSource, sql, "find-latest-event-id");
    sqlRunner.executeQuery(handler);

    return handler.getRetrievedValue();
  }

  public UserDatasetStore getUserDatasetStore() throws WdkModelException {
    if (userDatasetStore == null) {
      var udsConfig = getModelConfig().getUserDatasetStoreConfig();
      userDatasetStore = udsConfig.getUserDatasetStore(getModelConfig().getWdkTempDir());
    }
    return userDatasetStore;
  }

  // TODO: get from model config
  public String getUserDatasetSchemaName() {
    return "ApiDBUserDatasets.";
  }

  public ModelConfig getModelConfig() throws WdkModelException {
    if (modelConfig == null) {
      try {
        var gusHome = GusHome.getGusHome();
        var parser  = new ModelConfigParser(gusHome);

        modelConfig = parser.parseConfig(getProjectId()).build();
        QueryLogger.initialize(modelConfig.getQueryMonitor());
      } catch (SAXException | IOException e) {
        throw new WdkModelException(e);
      }
    }
    return modelConfig;
  }

  /**
   * Accepts an array of event json objects from a Jenkins job that composes the
   * content a series of event files containing json objects into a single json
   * array and returns a corresponding list of UserDatasetEvent objects.
   *
   * @param eventJsonArray JSONArray containing an array of event JSONObjects
   *
   * @return - a list of UserDatasetEvents corresponding to
   */
  public static List<UserDatasetEvent> parseEventsArray(JSONArray eventJsonArray)
  throws WdkModelException {
    var events = new ArrayList<UserDatasetEvent>();
    for (int i = 0; i < eventJsonArray.length(); i++) {
      var eventJson = eventJsonArray.getJSONObject(i);
      parseEventObject(eventJson, events);
    }
    return events;
  }

  /**
   * Parse the individual JSONObject representing an event and call the
   * appropriate event handler based on the information contained within that
   * object.
   *
   * @param eventJson JSONObject representing the event
   * @param events    a list of UserDatasetEvents to which new events are added.
   */
  protected static void parseEventObject(
    JSONObject eventJson,
    List<UserDatasetEvent> events
  ) throws WdkModelException {
    var eventId = eventJson.getLong("eventId");
    var event   = eventJson.getString("event");

    // Extract an array of projects relevant to the event and add them to a
    // project filter.
    var projectsJson = eventJson.getJSONArray("projects").toString();
    var mapper       = new ObjectMapper();
    var setType = new TypeReference<Set<String>>()
    {
    };

    Set<String> projects;
    try {
      projects = mapper.readValue(projectsJson, setType);
    } catch (IOException ioe) {
      throw new WdkModelException(ioe);
    }

    var projectsFilter = new HashSet<>(projects);

    var userDatasetId = eventJson.getLong("datasetId");
    var mapType = new TypeReference<Map<String, String>>()
    {
    };
    var typeJson = eventJson.getJSONObject("type").toString();

    Map<String, String> type;
    try {
      type = mapper.readValue(typeJson, mapType);
    } catch (IOException ioe) {
      throw new WdkModelException(ioe);
    }

    var userDatasetType = UserDatasetTypeFactory.getUserDatasetType(type.get(
      "name"), type.get("version"));

    // Dataset is in the user's workspace and now needs to be installed into the
    // database.
    if ("install".equals(event)) {
      var ownerUserId = eventJson.getLong("owner");

      // A dataset may have multiple dependencies
      var dependencies        = new HashSet<UserDatasetDependency>();
      var dependencyJsonArray = eventJson.getJSONArray("dependencies");

      for (int i = 0; i < dependencyJsonArray.length(); i++) {
        var dependencyJson = dependencyJsonArray.getJSONObject(i);
        dependencies.add(new UserDatasetDependency(
          dependencyJson.getString(
            "resourceIdentifier"),
          dependencyJson.getString("resourceVersion"),
          dependencyJson.getString("resourceDisplayName")
        ));
      }

      events.add(new UserDatasetInstallEvent(
        eventId,
        projectsFilter,
        userDatasetId,
        userDatasetType,
        ownerUserId,
        dependencies
      ));

    } else if ("uninstall".equals(event)) {
      // Dataset has been deleted from the workspace and now needs to be removed
      // from the database.

      events.add(new UserDatasetUninstallEvent(
        eventId,
        projectsFilter,
        userDatasetId,
        userDatasetType
      ));
    } else if ("share".equals(event)) {
      // Dataset sharing has either been granted or revoked in the workspace and
      // now must be reflected in the database

      var ownerId     = eventJson.getLong("owner");
      var recipientId = eventJson.getLong("recipient");
      var action = "grant".equals(eventJson.getString("action"))
        ? ShareAction.GRANT
        : ShareAction.REVOKE;

      events.add(new UserDatasetShareEvent(
        eventId,
        projectsFilter,
        userDatasetId,
        userDatasetType,
        ownerId,
        recipientId,
        action
      ));
    } else {
      throw new WdkModelException("Unrecognized user dataset event type: "
        + event);
    }

  }

  public void setProjectId(String projectId) {
    this.projectId = projectId;
  }

  private String getProjectId() {
    return projectId;
  }
}
