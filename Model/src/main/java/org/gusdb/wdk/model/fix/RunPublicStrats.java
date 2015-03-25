package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
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
    }
    catch (Exception e) {
      LOG.error("Error while running " + cmdName, e);
    }
    finally {
      LOG.info("Public Strategies done.");
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

    WdkModel wdkModel = null;
    try {
      wdkModel = WdkModel.construct(projectId, gusHome);
      LOG.debug("\n\n\n**********  MODEL CONSTRUCTED : using " + gusHome + "**********");
      LOG.info("\n**********  MODEL CONSTRUCTED : " + projectId + "**********");
      LOG.info("\n**********  MODEL CONSTRUCTED : running ALL Public Strategies**********");

      List<Strategy> erroredStratIds = publicStrats(wdkModel);
      if (!erroredStratIds.isEmpty()) {
        LOG.warn("At least one public strategy is invalid.  See below for list");
        for (Strategy st : erroredStratIds) {
          LOG.info("{ strategyId: " + st.getStrategyId() + ", strategyName: " + st.getName() + ", owner: " + st.getUser().getEmail());
        }
      }
    }
    finally {
      if (wdkModel != null) wdkModel.releaseResources();
    }
  }

  private static List<Strategy> publicStrats(WdkModel wdkModel) throws WdkModelException {

    StepFactory factory = wdkModel.getStepFactory();
    List<Strategy> publicStrategies = factory.loadPublicStrategies();
    int stratCount = 0;
    List<Strategy> erroredPublicStrats = new ArrayList<>();
    int allStratSize = publicStrategies.size();
    LOG.info("\n\n\n********** WE HAVE " + allStratSize + " public strategies total *********");

    for (int i = 0; i < allStratSize; i++) {
      Strategy st = publicStrategies.get(i);

      LOG.info("\n\n\n********** Found a Public Strat: " + st.getStrategyId() + " of user: " +
          st.getUser().getEmail() + "  *********");

      try {
        // TEST STRAT
        stratCount++;
        // to get the proper number of bad strats, we must get the answer value;
        //   this takes longer, but the alternative (getResultSize) masks bad
        //   strategies because it can't tell whether the exception is due to a
        //   bad strat or due to something else
        st.getLatestStep().getAnswerValue(true);
      }
      catch (Exception ex) {
        // check if strategy is already invalid, ignore if user NOT eupathdb
        LOG.error("Adding strategy " + st.getName() + " to list of errored strats.", ex);
        erroredPublicStrats.add(st);
      }
    }
    LOG.info("\n\n********** TESTED " + stratCount + " public strategies  *********\n");
    LOG.info("\n\n****** FOUND " + erroredPublicStrats.size() + " errored public strategies  *****\n");
    return erroredPublicStrats;
  }
}
