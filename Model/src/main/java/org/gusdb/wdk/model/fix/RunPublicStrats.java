package org.gusdb.wdk.model.fix;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;

/**
 * 1. load a model
 * 2. get a list of public strategies (only care for EuPathDB user for now)
 *    StepFactory.loadPublicStrategies() will load all the public strategies
 *    (and their users)
 * 3. run each strategy, and get the resultSize from the last step
 *    strategy.getLatestStep().getResultSize(); (catch any
 *    exceptions on that, and log it, and move to the next one)
 * 
 * @author Cris
 *
 */
public class RunPublicStrats extends BaseCLI {

  private static final Logger LOG = Logger.getLogger(RunPublicStrats.class);

  public static void main(String[] args) {
    String cmdName = System.getProperty("cmdName");
    RunPublicStrats runPublic = new RunPublicStrats(cmdName);
    try {
      runPublic.invoke(args);
      LOG.info("Public Strategies done.");
      System.exit(0);
    }
    catch (Exception e) {
      LOG.error("Error while running " + cmdName, e);
      System.exit(1);
    }
  }

  public RunPublicStrats(String command) {
    super((command != null) ? command : "wdkRunPublicStrats",
        "This command runs public strategies, will log at end a list of invalid ones.");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");
  }

  @Override
  protected void execute() throws WdkModelException {
    LOG.info("****Public Strategies run:  IN EXECUTE******");
    String gusHome = GusHome.getGusHome();
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);

    try (WdkModel wdkModel = WdkModel.construct(projectId, gusHome)) {
      LOG.debug("\n\n\n**********  MODEL CONSTRUCTED : using " + gusHome + "**********");
      LOG.info("\n**********  MODEL CONSTRUCTED : " + projectId + "**********");
      LOG.info("\n**********  MODEL CONSTRUCTED : running ALL Public Strategies**********");

      List<Strategy> erroredStrategies = getInvalidPublicStrats(wdkModel);
      if (!erroredStrategies.isEmpty()) {
        LOG.warn("At least one public strategy is invalid.  See below for list");
        for (Strategy st : erroredStrategies) {
          LOG.info("{  owner: " + st.getOwningUser().getEmail() + ", project: " + projectId + ", modifiedTime: "  + st.getLastModifiedTime() + ", strategyName: " + st.getName() + ", createdTime: " + st.getCreatedTime() + ", strategyId: " + st.getStrategyId() +  ", importId: " + st.getSignature() + " }");
        }
      }
    }
  }

  private static List<Strategy> getInvalidPublicStrats(WdkModel wdkModel) throws WdkModelException {

    StepFactory factory = new StepFactory(wdkModel.getSystemUser());
    List<Strategy> publicStrategies = factory.getPublicStrategies();
    List<Strategy> erroredPublicStrats = publicStrategies.stream()
        .filter(strat -> strat.isValid()).collect(Collectors.toList());
    LOG.info("\n\n\n********** WE HAVE " + publicStrategies.size() + " public strategies total *********");

    for (Strategy st : publicStrategies) {
      LOG.info("\n\n\n********** Found a Public Strat: " + st.getStrategyId() +
          " of user: " + st.getOwningUser().getEmail() + "  *********");
    }

    for (Strategy st : erroredPublicStrats) {
      LOG.error("Adding strategy " + st.getName() + " -belonging to user " +
          st.getOwningUser().getEmail() + " created on " + st.getCreatedTime() +
          " and modified last on " + st.getLastModifiedTime() +
          "..... to list of errored strats." + FormatUtil.NL +
          st.getValidationBundle().toString());
    }

    LOG.info("\n\n********** TESTED " + publicStrategies.size() + " public strategies  *********\n");
    LOG.info("\n\n****** FOUND " + erroredPublicStrats.size() + " errored public strategies  *****\n");

    return erroredPublicStrats;
  }
}
