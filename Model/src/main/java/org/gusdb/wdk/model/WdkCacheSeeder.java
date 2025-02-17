package org.gusdb.wdk.model;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Timer;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.fgputil.validation.OptionallyInvalid;
import org.gusdb.fgputil.validation.ValidObjectFactory.DisplayablyValid;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.query.spec.ParameterContainerInstanceSpecBuilder.FillStrategy;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.Strategy;
import org.json.JSONObject;

public class WdkCacheSeeder {

  private static final Logger LOG = Logger.getLogger(WdkCacheSeeder.class);

  private static final String RESULTS_COPY_FILE = /* $GUS_HOME/ */ "../html/test_output/wdk_cache_seeder_output.json";

  public static void main(String[] args) throws WdkModelException {

    if (args.length != 2 || !args[0].equals("-model")) {
      System.err.println("\nUSAGE: WdkCacheSeeder -model <project_id>\n");
      System.exit(1);
    }

    try (WdkModel wdkModel = WdkModel.construct(args[1], GusHome.getGusHome())) {
      WdkCacheSeeder seeder = new WdkCacheSeeder(wdkModel);
      String result = new JSONObject()
          .put("questionResults", seeder.cacheQuestions())
          .put("publicStratsResults", seeder.cachePublicStrategies())
          .toString(2);

      System.out.println("WDK Cache Seeding Complete with results: " + result);

      Path path = Paths.get(GusHome.getGusHome(), RESULTS_COPY_FILE);
      System.out.println("Writing copy of results to " + path.toAbsolutePath());
      try (FileWriter out = new FileWriter(path.toFile())) {
        out.write(result);
        out.write("\n");
      }
      catch (IOException e) {
        System.err.println("Unable to write copy of results.");
        e.printStackTrace(System.err);
      }
    }
  }

  private final WdkModel _wdkModel;

  public WdkCacheSeeder(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  /**
   * Generates displayable (default) param value sets for all questions in the
   * WDK model.  This includes generating enum param vocabularies, which will
   * be cached in DB/memory along the way.
   *
   * @return statistics about this run
   */
  public JSONObject cacheQuestions() {
    Timer t = new Timer();
    List<String> validatedQuestions = new ArrayList<>();
    Map<String,String> invalidQuestions = new HashMap<>();
    Map<String,String> questionErrors = new HashMap<>();
    List<Question> questions = _wdkModel.getAllQuestions();
    for (Question q : questions) {
      LOG.info("Caching question: " + q.getFullName());
      try {
        OptionallyInvalid<DisplayablyValid<AnswerSpec>, AnswerSpec> spec =
          AnswerSpec.builder(_wdkModel)
            .setQuestionFullName(q.getFullName())
            .build(
                _wdkModel.getSystemUser(),
                StepContainer.emptyContainer(),
                ValidationLevel.DISPLAYABLE,
                FillStrategy.FILL_PARAM_IF_MISSING)
            .getDisplayablyValid();
        spec
          .ifLeft(s -> validatedQuestions.add(q.getFullName()))
          .ifRight(s -> invalidQuestions.put(q.getFullName(), s.getValidationBundle().toString(2)));
      }
      catch (Exception e) {
        questionErrors.put(q.getFullName(), e.toString());
      }
    }

    // build and return results
    return new JSONObject()
        .put("numQuestionsProcessed", questions.size())
        .put("validatedQuestions", validatedQuestions)
        .put("invalidQuestions", invalidQuestions)
        .put("questionErrors", questionErrors)
        .put("questionsDuration", t.getElapsedString());
  }

  /**
   * Runs all found public strategies for this model, adding their results to the WDK cache
   *
   * @return statistics about this run
   * @throws WdkModelException 
   */
  public JSONObject cachePublicStrategies() throws WdkModelException {
    Timer t = new Timer();
    Map<Long,Integer> publicStratResultSizes = new HashMap<>();
    Map<Long,String> unrunnablePublicStrats = new HashMap<>();
    Map<Long,String> publicStratErrors = new HashMap<>();
    List<Strategy> publicStrategies = new StepFactory(_wdkModel.getSystemUser()).getPublicStrategies();
    for (Strategy publicStrategy : publicStrategies) {
      LOG.info("Caching public strategy: " + publicStrategy.getStrategyId());
      try {
        Step step = publicStrategy.getRootStep();
        if (step.isRunnable())
          publicStratResultSizes.put(publicStrategy.getStrategyId(), step.recalculateResultSize().get());
        else
          unrunnablePublicStrats.put(publicStrategy.getStrategyId(), step.getValidationBundle().toString());
      }
      catch (Exception e) {
        publicStratErrors.put(publicStrategy.getStrategyId(), e.toString());
      }
    }

    // build and return results
    return new JSONObject()
        .put("numPublicStrategies", publicStrategies.size())
        .put("publicStratResultSizes", publicStratResultSizes)
        .put("unrunnablePublicStrats", unrunnablePublicStrats)
        .put("publicStratErrors", publicStratErrors)
        .put("publicStratsDuration",t.getElapsedString());
  }
}
