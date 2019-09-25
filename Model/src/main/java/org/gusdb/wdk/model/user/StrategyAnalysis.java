package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.filter;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.user.Strategy.StrategyBuilder;
import org.gusdb.wdk.model.user.StrategyLoader.UnbuildableStrategyList;

public class StrategyAnalysis {

  private static final String HEADER_BAR = "=====================================================";

  public static void main(String[] args) throws WdkModelException {

    usage(args);

    String projectId = args[0];
    ValidationLevel validationLevel = getValidationLevel(args[1]);
    Optional<String> userEmail = args.length == 2 ? Optional.empty() : Optional.of(args[2]);

    try (WdkModel model = WdkModel.construct(projectId, GusHome.getGusHome())) {
      Timer timer = new Timer();
      UnbuildableStrategyList<InvalidStrategyStructureException> malformedStrats = new UnbuildableStrategyList<>();
      UnbuildableStrategyList<WdkModelException> stratsWithBuildErrors = new UnbuildableStrategyList<>();
      Collection<Strategy> strategies =
        !userEmail.isPresent() ?
        model.getStepFactory().getAllStrategies(validationLevel, malformedStrats, stratsWithBuildErrors).values() :
        model.getStepFactory().getStrategies(
          Optional.ofNullable(model.getUserFactory()
            .getUserByEmail(userEmail.get()))
            .orElseThrow(() -> new WdkModelException(
              "Could not find user with email: " + userEmail
            ))
            .getUserId(),
          validationLevel, FillStrategy.NO_FILL, malformedStrats, stratsWithBuildErrors).values();
      List<Strategy> validStrats = filter(strategies, str -> str.isValid());
      List<Strategy> invalidStrats = filter(strategies, str -> !str.isValid());
      System.out.println(
        header("Found " + validStrats.size() + " valid strategies") +
        (userEmail.isPresent() ?
          getStrategyText(validStrats) :
          "Skipping display of all valid strategies." + NL) +
        header("Found " + invalidStrats.size() + " invalid strategies") +
        getStrategyText(invalidStrats) +
        header("Found " + malformedStrats.size() + " malformed strategies") +
        getStrategyText(malformedStrats) +
        header("Errors while building " + stratsWithBuildErrors.size() + " strategies") +
        getStrategyText(stratsWithBuildErrors) +
        "Done in " + timer.getElapsedString() + NL
      );
    }
  }

  private static void usage(String[] args) {
    if (args.length != 2 && args.length != 3) {
      System.err.println(
        NL +
        "Produces a report of valid, invalid, and malformed strategies, " + NL +
        "either in the entire user database or for a single user." + NL +
        "You choose the preferred validation level (semantic will take much longer)." + NL +
        "To analyze only strategies for a single user, add their email as a third argument." + NL +
        NL +
        "USAGE: fgpJava " + StrategyAnalysis.class.getName() + " <projectId> [syntactic|semantic] [<userEmail>]" + NL
      );
      System.exit(1);
    }
  }

  private static ValidationLevel getValidationLevel(String string) {
    try {
      ValidationLevel validationLevel = ValidationLevel.valueOf(string.toUpperCase());
      if (!validationLevel.equals(ValidationLevel.SYNTACTIC) &&
          !validationLevel.equals(ValidationLevel.SEMANTIC)) {
        throw new IllegalArgumentException("Must specify 'syntactic' or 'semantic'.");
      }
      return validationLevel;
    }
    catch (IllegalArgumentException e) {
      System.err.println(e.getMessage());
      System.exit(1);
      return null;
    }
  }

  private static String header(String string) {
    return NL + HEADER_BAR + NL + string + NL + HEADER_BAR + NL + NL;
  }

  private static String getStrategyText(Collection<Strategy> strategies) {
    StringBuilder sb = new StringBuilder();
    for (Strategy strat : strategies) {
      sb.append(strat.getStrategyId() + " has " + strat.getAllSteps().size() + " steps.");
      if (!strat.isValid()) {
        sb.append(" Invalid because:").append(NL);
        for (Step step : strat.getAllSteps()) {
          if (!step.isValid()) {
            for (String error : step.getValidationBundle().getAllErrors()) {
              sb.append("  In step ").append(step.getStepId()).append(", ").append(error).append(NL);
            }
          }
        }
      }
      sb.append(NL);
    }
    return sb.toString();
  }

  private static <T extends Exception> String getStrategyText(UnbuildableStrategyList<T> malformedStrats) {
    StringBuilder sb = new StringBuilder();
    for (Entry<StrategyBuilder, T> entry : malformedStrats) {
      StrategyBuilder builder = entry.getKey();
      sb.append(builder.getStrategyId() + " has " + builder.getNumSteps() +
          " steps. Malformed because: " + NL + "  " + entry.getValue().getMessage() + NL + NL);
    }
    return sb.toString();
  }
}
