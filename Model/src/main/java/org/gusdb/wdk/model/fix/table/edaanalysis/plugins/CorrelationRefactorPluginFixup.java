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

public class CorrelationRefactorPluginFixup extends AbstractAnalysisUpdater {
  private static final Logger LOG = Logger.getLogger(CorrelationRefactorPluginFixup.class);
  public static final String DATA_1_KEY = "data1";
  public static final String DATA_2_KEY = "data2";
  public static final String DATA_TYPE_KEY = "dataType";

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
        .anyMatch(CorrelationRefactorPluginFixup::needsUpdate);

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
    List<String> migratableAppNames = List.of("correlation");
    return migratableAppNames.contains(new JSONObject((Map<?, ?>) computation).getJSONObject("descriptor").getString("type"));
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

    if (computationType.equals("correlation")) {

      JSONObject configuration = descriptor.getJSONObject("configuration");

      JSONObject data1 = configuration.getJSONObject(DATA_1_KEY);
      JSONObject data2 = configuration.getJSONObject(DATA_2_KEY);

      if (data1.getString(DATA_TYPE_KEY).equals("metadata") || data2.getString(DATA_TYPE_KEY).equals("metadata")) {
        descriptor.put("type", "correlationassaymetadata");
      } else {
        descriptor.put("type", "correlationassayassay");
      }
    }

    return computation;
  }

  @Override
  public void dumpStatistics() {

  }
}
