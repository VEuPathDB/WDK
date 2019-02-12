package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public class UserCliUtils {

  public static void main(String[] args) throws WdkModelException {

    if (args.length != 3) {
      System.err.println(NL + "USAGE: fgpJava " + UserCliUtils.class.getName() +
          " <projectId> <userEmail> <operation>" + NL + NL + "Use 'help' operation for more info." + NL);
      System.exit(1);
    }
    String projectId = args[0];
    String userEmail = args[1];
    String operation = args[2];

    try (WdkModel model = WdkModel.construct(projectId, GusHome.getGusHome())) {
      User user = Optional.ofNullable(model.getUserFactory().getUserByEmail(userEmail))
          .orElseThrow(() -> new WdkModelException("Could not find user with email: " + userEmail));
      switch(operation) {
        case "getStratBasics":
          Map<Long, Strategy> invalidStrats = new HashMap<>();
          Map<Long, Strategy> validStrats = model.getStepFactory().getStrategies(user.getUserId(), invalidStrats);
          System.out.println(
              "Found " + validStrats.size() + " valid strategies:" + NL +
              getStrategyBasicsText(validStrats.values()) +
              "Found " + invalidStrats.size() + " invalid strategies: " + NL +
              getStrategyBasicsText(invalidStrats.values()));
          break;
        default:
          System.err.println("The following operations are supported: " + NL + NL +
              "  getStratBasics: show basic info about user's strats" + NL +
              "  help: shows this message" + NL);
      }
    }
  }

  private static String getStrategyBasicsText(Collection<Strategy> strategies) {
    StringBuilder sb = new StringBuilder();
    for (Strategy strat : strategies) {
      sb.append(strat.getStrategyId() + " has " + strat.getNumSteps() + " steps.  Validity: " + strat.getValidationBundle().toString() + NL);
    }
    return sb.toString();
  }
}
