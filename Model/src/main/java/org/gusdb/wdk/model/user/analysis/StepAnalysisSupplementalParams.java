package org.gusdb.wdk.model.user.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.db.platform.PostgreSQL;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;

public class StepAnalysisSupplementalParams {

  public static final String ANSWER_ID_SQL_PARAM_NAME = "answerIdSql";
  public static final String STEP_PARAM_VALUES_SQL_PARAM_NAME = "stepParamValuesSql";

  private static final Map<String, Function<RunnableObj<AnswerSpec>,String>> PARAM_VALUE_GENERATORS =
    new HashMap<>() {{

      put(ANSWER_ID_SQL_PARAM_NAME, spec ->
        Functions.f0Swallow(() -> AnswerValueFactory.makeAnswer(spec).getIdSql()).get()
      );

      // NOTE: PostgreSQL only.  VALUES list is a SQL construct that creates a
      // temporary table, in this case, with two fields, one for the param name,
      // one for the param value, allowing stepAnalysis parameters to be
      // dependent on step parameter values
      put(STEP_PARAM_VALUES_SQL_PARAM_NAME, spec ->
        spec.get().getWdkModel().getAppDb().getPlatform() instanceof PostgreSQL
        // param value only valid in PostgreSQL
        ? String.format("SELECT * FROM ( VALUES %s ) AS p (name, value)",
            spec.get().getQueryInstanceSpec().entrySet().stream()
            .map(entry -> ("('" + entry.getKey() + "', '" + entry.getValue() + "')"))
            .collect(Collectors.joining(",")))
        : Functions.doThrow(() -> new WdkRuntimeException(
            "Invalid step analysis parameter: stepParamValuesSql only valid for PostgreSQL.")));

    }};

  public static Set<String> getAllNames() {
    return PARAM_VALUE_GENERATORS.keySet();
  }

  public static Set<String> getNames(StepAnalysis analysis) {
    return getAllNames().stream()
      .filter(name -> analysis.getParamMap().keySet().contains(name))
      .collect(Collectors.toSet());
  }

  public static Map<String,String> getValues(StepAnalysis analysis, RunnableObj<AnswerSpec> runnableSpec) {
    return PARAM_VALUE_GENERATORS.entrySet().stream()
      .filter(entry -> analysis.getParamMap().keySet().contains(entry.getKey()))
      .map(e -> new TwoTuple<>(e.getKey(), e.getValue().apply(runnableSpec)))
      .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
  }

}
