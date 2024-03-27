package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AbstractAnalysisUpdater;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VDIMigrationPlugin extends AbstractAnalysisUpdater {
  private static final Logger LOG = Logger.getLogger(VDIMigrationPlugin.class);
  public static final String UD_DATASET_ID_PREFIX = "EDAUD_";

  private Map<String, String> legacyIdToVdiId;
  private int missingFromVdiCount = 0;

  @Override
  public TableRowInterfaces.RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    final String legacyDatasetId = nextRow.getDatasetId();
    final String legacyUdId = legacyDatasetId.replace(UD_DATASET_ID_PREFIX, "");
    final String vdiId = legacyIdToVdiId.get(legacyUdId);

    if (vdiId == null) {
      LOG.warn("Unable to find legacy ID " + legacyUdId + " in the tinydb file.");
      missingFromVdiCount++;
      return new TableRowInterfaces.RowResult<>(nextRow);
    }

    // Append UD prefix to VDI ID. The prefix is prepended in the view that maps stable VDI IDs to the unstable study
    // ID, which is the currency of EDA.
    final String vdiDatasetId = UD_DATASET_ID_PREFIX + vdiId;

    // Create a copy with just the dataset ID updated to VDI counterpart.
    AnalysisRow out = new AnalysisRow(nextRow.getAnalysisId(), vdiDatasetId, nextRow.getDescriptor(),
        nextRow.getNumFilters(), nextRow.getNumComputations(), nextRow.getNumVisualizations());

    return new TableRowInterfaces.RowResult<>(out);
  }

  @Override
  public void dumpStatistics() {
    if (missingFromVdiCount > 0) {
      LOG.warn("Failed to migrate " + missingFromVdiCount + " datasets, they were not found in the provided tinydb file.");
    }
  }

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    // Parse args in the format --<argname>=<argvalue>
    final Map<String, String> args = additionalArgs.stream()
        .map(arg -> Arrays.stream(arg.split("="))
            .map(String::trim) // Trim whitespace from args
            .collect(Collectors.toList()))
        .collect(Collectors.toMap(
            pair -> pair.get(0),
            pair -> pair.size() > 1 ? pair.get(1) : "true")); // A flag without an "=" is a boolean. Set true if present.

    // Validate required arg.
    if (!args.containsKey("--tinyDb")) {
      throw new IllegalArgumentException("Missing required flag --tinyDb");
    }
    final File tinyDbFile = new File(args.get("--tinyDb"));

    this.legacyIdToVdiId = readLegacyStudyIdToVdiId(tinyDbFile);

    // Default to dryrun to avoid incidental migrations when testing.
    this._writeToDb = Boolean.parseBoolean(args.getOrDefault("--liveRun", "false"));
  }

  /**
   * Parse the tinydb file into a map of legacy UD identifiers to VDI identifiers.
   *
   * Example file format:
   *
   * {
   *   "_default": {
   *     "1": {
   *       "type": "owner",
   *       "udId": 1234,
   *       "vdiId": "123XyZ",
   *       "msg": null,
   *       "time": "Fri Mar 26 00:00:00 2024"
   *     }
   *  }
   *
   * @param tinyDbFile TinyDB file, output of the migration script run to migrate legacy UDs into VDI.
   * @return Map of legacy UD Ids to VDI Ids.
   */
  private Map<String, String> readLegacyStudyIdToVdiId(File tinyDbFile) {
    try {
      JsonNode root = JsonUtil.Jackson.readTree(tinyDbFile);
      JsonNode dbRoot = root.get("_default");

      Map<String, String> mapping = new HashMap<>();
      Iterator<Map.Entry<String, JsonNode>> fieldIterator = dbRoot.fields();

      // Iterate through each field in the "_default" node.
      // Ignore the numeric index keys and extract the udId and vdiId fields to create mapping.
      while (fieldIterator.hasNext()) {
        Map.Entry<String, JsonNode> entry = fieldIterator.next();
        mapping.put(entry.getValue().get("udId").asText(), entry.getValue().get("vdiId").asText());
      }

      LOG.info("Extracted a mapping of " + mapping.size() + " legacy to VDI identifiers.");
      return mapping;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private enum CliArg {
    
  }
}
