package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AbstractAnalysisUpdater;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VDIMigrationPlugin extends AbstractAnalysisUpdater {
  private static final Logger LOG = Logger.getLogger(VDIMigrationPlugin.class);
  private static final String UD_DATASET_ID_PREFIX = "EDAUD_";
  private static final Pattern VAR_ID_PATTERN = Pattern.compile("variableId\":\\s*\"([a-zA-Z0-9_-]+)");
  private static final Pattern ENTITY_ID_PATTERN = Pattern.compile("entityId\":\\s*\"([a-zA-Z0-9_-]+)");

  private Map<String, String> _legacyIdToVdiId;
  private VDIEntityIdRetriever _vdiEntityIdRetriever;
  private final AtomicInteger missingFromVdiCount = new AtomicInteger(0);

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

    // Validate required args.
    if (!args.containsKey("--tinyDb")) {
      throw new IllegalArgumentException("Missing required flag --tinyDb");
    }
    if (!args.containsKey(("--schema"))) {
      throw new IllegalArgumentException("Missing required argument --schema");
    }

    final String schema = args.get("--schema");
    setEntityIdRetriever(new VDIEntityIdRetriever(wdkModel.getAppDb().getDataSource(), schema));

    final File tinyDbFile = new File(args.get("--tinyDb"));
    readVdiMappingFile(tinyDbFile);

    // Default to dryrun to avoid incidental migrations when testing.
    _writeToDb = Boolean.parseBoolean(args.getOrDefault("-write", "false"));
    _wdkModel = wdkModel;
  }

  // Visible for testing.
  void setEntityIdRetriever(VDIEntityIdRetriever entityIdRetriever) {
    _vdiEntityIdRetriever = entityIdRetriever;
  }

  // Visible for testing
  void readVdiMappingFile(File mappingFile) {
    _legacyIdToVdiId = readLegacyStudyIdToVdiId(mappingFile);
  }

  @Override
  public TableRowInterfaces.RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    final String legacyDatasetId = nextRow.getDatasetId();

    if (!legacyDatasetId.startsWith(UD_DATASET_ID_PREFIX)) {
      return new TableRowInterfaces.RowResult<>(nextRow).setShouldWrite(false);
    }

    final String legacyUdId = legacyDatasetId.replace(UD_DATASET_ID_PREFIX, "");
    final String vdiId = _legacyIdToVdiId.get(legacyUdId);

    if (vdiId == null) {
      LOG.warn("Unable to find legacy ID " + legacyUdId + " in the tinydb file.");
      missingFromVdiCount.incrementAndGet();
      return new TableRowInterfaces.RowResult<>(nextRow)
          .setShouldWrite(false);
    }

    // Append UD prefix to VDI ID. The prefix is prepended in the view that maps stable VDI IDs to the unstable study
    // ID, which is the currency of EDA.
    final String vdiDatasetId = UD_DATASET_ID_PREFIX + vdiId;
    final Optional<String> vdiEntityId = _vdiEntityIdRetriever.queryEntityId(vdiDatasetId);
    if (vdiEntityId.isEmpty()) {
      LOG.warn("Unable to find entity ID in appdb for VDI dataset ID: " + vdiDatasetId);
      return new TableRowInterfaces.RowResult<>(nextRow)
          .setShouldWrite(false);
    }

    LOG.info("Analysis descriptor before migration: " + nextRow.getDescriptor());
    String descriptor = nextRow.getDescriptor().toString();

    // Find all variable IDs.
    final Set<String> legacyVariableIds = VAR_ID_PATTERN.matcher(descriptor).results()
        .map(match -> match.group(1))
        .collect(Collectors.toSet());

    final String entityId = ENTITY_ID_PATTERN.matcher(descriptor).results()
        .findAny()
        .map(m -> m.group(1))
        .orElse(null);

    // Replace all entityID with entityID looked up from database.
    if (entityId != null) {
      descriptor = descriptor.replaceAll(entityId, vdiEntityId.get());
    }

    // Replace all variable IDs with value converted from legacy variable ID.
    for (String legacyVariableId: legacyVariableIds) {
      descriptor = descriptor.replaceAll(legacyVariableId, convertToVdiId(legacyVariableId));
    }

    // Create a copy with just the dataset ID updated to VDI counterpart.
    AnalysisRow out = new AnalysisRow(nextRow.getAnalysisId(), vdiDatasetId, new JSONObject(descriptor),
        nextRow.getNumFilters(), nextRow.getNumComputations(), nextRow.getNumVisualizations());
    
    LOG.info("Analysis descriptor after migration: " + out);

    return new TableRowInterfaces.RowResult<>(out)
        .setShouldWrite(_writeToDb);
  }

  private String convertToVdiId(String legacyVariableId) {
    byte[] encodedId = DigestUtils.digest(DigestUtils.getSha1Digest(), legacyVariableId.getBytes(StandardCharsets.UTF_8));
    return "VAR_" + Hex.encodeHexString(encodedId).substring(0, 16);
  }

  @Override
  public void dumpStatistics() {
    if (missingFromVdiCount.get() > 0) {
      LOG.warn("Failed to migrate " + missingFromVdiCount + " datasets, they were not found in the provided tinydb file.");
    }
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
}
