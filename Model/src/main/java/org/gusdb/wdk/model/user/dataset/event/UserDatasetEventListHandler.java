package org.gusdb.wdk.model.user.dataset.event;

import java.io.File;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.wdk.model.user.dataset.event.raw.EventParser;

/**
 * A wrapper around the UserDatasetEventArrayHandler library to allow it to be
 * called via a command line interface.
 *
 * @author crisl-adm
 */
public class UserDatasetEventListHandler extends BaseCLI
{
  protected static final String ARG_PROJECT     = "project";
  protected static final String ARG_EVENTS_FILE = "eventsFile";
  protected static final String ARG_RUN_MODE    = "mode";

  private static final Logger logger = Logger.getLogger(UserDatasetEventListHandler.class);

  public UserDatasetEventListHandler(String command) {
    super(command, "Handle a list of user dataset events.");
  }

  public static void main(String[] args) {
    var cmdName = System.getProperty("cmdName");
    var handler = new UserDatasetEventListHandler(cmdName);

    try {
      handler.invoke(args);
      logger.info("done.");
      System.exit(0);
    } catch (Exception ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

  @Override
  protected void execute() throws Exception {
    var projectID = (String) getOptionValue(ARG_PROJECT);

    switch ((String) getOptionValue(ARG_RUN_MODE)) {
      case "cleanup":
        new UserDatasetEventCleanup(projectID).cleanupFailedInstalls();
      case "sync":
        new UserDatasetEventSync(projectID)
          .handleEventList(UserDatasetEventSync.parseEventsArray(EventParser.parseList(
            new File((String) getOptionValue(ARG_EVENTS_FILE))
          )));
      default:
        throw new Exception("Unknown run mode, must be one of \"sync\" or \"cleanup\"");
    }
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT, true, null, "The project of the app db");
    addSingleValueOption(
      ARG_EVENTS_FILE,
      true,
      null,
      "File containing an ordered JSON Array of user dataset events"
    );
  }
}
