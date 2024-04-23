package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AbstractAnalysisUpdater;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CorrelationRefactorPlugin extends AbstractAnalysisUpdater {

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    super.configure(wdkModel, additionalArgs);
  }

  @Override
  public TableRowInterfaces.RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    JSONObject descriptor = nextRow.getDescriptor();
    JSONArray computations = descriptor.getJSONArray("computations");

    boolean updateNeeded = computations.toList().stream()
        .anyMatch(CorrelationRefactorPlugin::needsUpdate);

    if (!updateNeeded) {
      // Short-circuit to avoid doing migration work.
      return new TableRowInterfaces.RowResult<>(nextRow)
          .setShouldWrite(false);
    }

    List<JSONObject> updatedComputations = computations.toList().stream()
        .map(computation -> migrateComputation(new JSONObject((Map<?, ?>) computation)))
        .collect(Collectors.toList());

    descriptor.put("computations", updatedComputations);

    return new TableRowInterfaces.RowResult<>(nextRow)
        .setShouldWrite(_writeToDb);
  }

  private static boolean needsUpdate(Object computation) {
    List<String> migratableComputeTypes = List.of("correlationassaymetadata", "correlationassayassay");
    return migratableComputeTypes.contains(new JSONObject((Map<?, ?>) computation).getJSONObject("descriptor").getString("type"));
  }

  /**
   * Migrates a computation, mutating it in-place.
   * @param computation
   * @return
   */
  private JSONObject migrateComputation(JSONObject computation) {
    String computationType = computation.getJSONObject("descriptor").getString("type");
    JSONObject descriptor = computation.getJSONObject("descriptor");
    if (computationType.equals("correlationassaymetadata")) {
      JSONObject configuration = descriptor.getJSONObject("configuration");
      descriptor.put("type", "correlation");

      configuration.put("data1", configuration.getJSONObject("collectionVariable"));
      configuration.getJSONObject("data1").put("dataType", "collection");
      configuration.remove("collectionVariable");

      configuration.put("data2", new JSONObject());
      configuration.getJSONObject("data2").put("dataType", "metadata");
      descriptor.put("configuration", configuration);
      computation.put("descriptor", descriptor);

      return computation;
    } else if (computationType.equals("correlationassayassay")) {
      JSONObject configuration = descriptor.getJSONObject("configuration");
      descriptor.put("type", "correlation");

      configuration.put("data1", configuration.getJSONObject("collectionVariable1"));
      configuration.getJSONObject("data1").put("dataType", "collection");
      configuration.remove("collectionVariable1");

      configuration.put("data2", configuration.getJSONObject("collectionVariable2"));
      configuration.getJSONObject("data2").put("dataType", "collection");
      configuration.remove("collectionVariable2");
      return computation;
    }
    return computation;
  }

  @Override
  public void dumpStatistics() {

  }
}
