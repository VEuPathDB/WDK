package org.gusdb.wdk.model.fix;

import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.BaseCLI;
import org.gusdb.wdk.model.Utilities;
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

  private static final String ARG_EUPATH_USER = "onlyEuPathUser";

  private static final Logger LOG = Logger.getLogger(RunPublicStrats.class);

  public static void main(String[] args) throws Exception {
    String cmdName = System.getProperty("cmdName");
    RunPublicStrats runPublic = new RunPublicStrats(cmdName);
    try {
      runPublic.invoke(args);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
    finally {
      LOG.info("Public Strategies done.");
      System.exit(0);
    }
  }

  private WdkModel wdkModel;

  // private String userSchema;

  public RunPublicStrats(String command) {
    super((command != null) ? command : "wdkRunPublicStrats",
        "This command runs public strategies (default is only for the EuPathDB user).");
  }

  @Override
  protected void declareOptions() {
    addSingleValueOption(ARG_PROJECT_ID, true, null, "a ProjectId, which should match the directory name "
        + "under $GUS_HOME, where model-config.xml is stored.");

    addSingleValueOption(ARG_EUPATH_USER, true, null,
        "if 'yes', will only run the EuPathDB user Public Strategies ");
  }

  @Override
  protected void execute() throws Exception {
    LOG.info("****Public Strategies run:  IN EXECUTE******");
    String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
    String projectId = (String) getOptionValue(ARG_PROJECT_ID);
    String onlyEuPath = (String) getOptionValue(ARG_EUPATH_USER);

    wdkModel = WdkModel.construct(projectId, gusHome);
    LOG.debug("\n\n\n**********  MODEL CONSTRUCTED : using " + gusHome + "**********");
    LOG.info("\n**********  MODEL CONSTRUCTED : " + projectId + "**********");
    if (onlyEuPath.equals("yes"))
      LOG.info("\n\n**********  MODEL CONSTRUCTED : running only EuPathDB user Public Strategies*********");
    else
      LOG.info("\n**********  MODEL CONSTRUCTED : running ALL Public Strategies**********");

    publicStrats(onlyEuPath);

  }

  private void publicStrats(String onlyEuPath) throws WdkModelException {

    StepFactory factory = wdkModel.getStepFactory();
    List<Strategy> publicStrategies = factory.loadPublicStrategies();
    Strategy st = null;
    int result, stratCount = 0;
    int allStratSize = publicStrategies.size();
    LOG.info("\n\n\n********** WE HAVE " + allStratSize + " public strategies total *********");

    try {
      for (int i = 0; i < allStratSize; i++) {
        st = publicStrategies.get(i);
        if (onlyEuPath.equals("yes")) {
          if (st.getUser().getUserId() == 1926010) {
            LOG.info("\n\n\n********** Found a EuPathDB Public Strat: " + st.getStrategyId() + "**********");
            // TEST STRAT
            result = st.getLatestStep().getResultSize();
            stratCount++;
          }
        }
        else {
          LOG.info("\n\n\n********** Found a Public Strat: " + st.getStrategyId() + " of user: " +
              st.getUser().getEmail() + "  *********");

          // TEST STRAT
          result = st.getLatestStep().getResultSize();
          stratCount++;
        }
      }
      LOG.info("\n\n\n********** TESTED " + stratCount + " public strategies  *********");
      return;
    }
    catch (WdkModelException ex) {
      // check if strategy is already invalid, ignore if user NOT eupathdb
      throw ex;
    }

  }

}
