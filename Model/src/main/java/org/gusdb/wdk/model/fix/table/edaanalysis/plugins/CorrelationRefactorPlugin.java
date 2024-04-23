package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.apache.log4j.Logger;
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
  private static final Logger LOG = Logger.getLogger(CorrelationRefactorPlugin.class);

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    super.configure(wdkModel, additionalArgs);
  }

  @Override
  public TableRowInterfaces.RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    JSONObject descriptor = nextRow.getDescriptor();
    JSONArray computations = descriptor.getJSONArray("computations");

    LOG.info("Descriptor before migration: " + descriptor);

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

    LOG.info("Descriptor after migration: " + descriptor);

    return new TableRowInterfaces.RowResult<>(nextRow)
        .setShouldWrite(_writeToDb);
  }

  private static boolean needsUpdate(Object computation) {
    List<String> migratableComputeTypes = List.of("correlationassaymetadata", "correlationassayassay");
    return migratableComputeTypes.contains(new JSONObject((Map<?, ?>) computation).getJSONObject("descriptor").getString("type"));
  }

  /**
   * Migrates a computation, mutating it in-place and returning the migrated object.
   *
   * Only migrates computations of type:
   *   - correlationassaymetadata
   *   - correlationassayassay
   * @param computation Specification of the computation to migrate.
   * @return Migrated computation.
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
