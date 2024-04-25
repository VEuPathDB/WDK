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
  public static final String COLLECTION_SPEC_KEY = "collectionSpec";
  public static final String DATA_1_KEY = "data1";
  public static final String DATA_2_KEY = "data2";
  public static final String DATA_TYPE_KEY = "dataType";
  public static final String COLLECTION_TYPE = "collection";

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

      JSONObject data1 = new JSONObject();
      configuration.put(DATA_1_KEY, data1);
      data1.put(DATA_TYPE_KEY, COLLECTION_TYPE);
      data1.put(COLLECTION_SPEC_KEY, configuration.getJSONObject("collectionVariable"));
      configuration.remove("collectionVariable");

      JSONObject data2 = new JSONObject();
      configuration.put(DATA_2_KEY, data2);
      data2.put(DATA_TYPE_KEY, "metadata");
      return computation;
    } else if (computationType.equals("correlationassayassay")) {
      JSONObject configuration = descriptor.getJSONObject("configuration");
      descriptor.put("type", "correlation");

      JSONObject data1 = new JSONObject();
      configuration.put(DATA_1_KEY, data1);
      data1.put(COLLECTION_SPEC_KEY, configuration.getJSONObject("collectionVariable1"));
      data1.put(DATA_TYPE_KEY, COLLECTION_TYPE);
      configuration.remove("collectionVariable1");

      JSONObject data2 = new JSONObject();
      configuration.put(DATA_2_KEY, data2);
      data2.put(COLLECTION_SPEC_KEY, configuration.getJSONObject("collectionVariable2"));
      data2.put(DATA_TYPE_KEY, COLLECTION_TYPE);
      configuration.remove("collectionVariable2");
      return computation;
    }
    return computation;
  }

  @Override
  public void dumpStatistics() {

  }
}
