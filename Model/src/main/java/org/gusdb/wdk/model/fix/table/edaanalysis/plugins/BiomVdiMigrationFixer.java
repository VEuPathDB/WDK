package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AbstractAnalysisUpdater;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BiomVdiMigrationFixer extends AbstractAnalysisUpdater {
  private static final Logger LOG = Logger.getLogger(BiomVdiMigrationFixer.class);
  private static final Pattern VAR_ID_PATTERN = Pattern.compile("variableId\":\\s*\"([a-zA-Z0-9_-]+)");

  private Map<String, String> fixedUpMapping;

  @Override
  public void configure(WdkModel wdkModel, List<String> additionalArgs) throws Exception {
    this.fixedUpMapping = readFixedUpMapping(Path.of(additionalArgs.get(0)));
    this._writeToDb = additionalArgs.size() == 2 && additionalArgs.get(1).equals("-write");
  }

  @Override
  public boolean isPerformTableBackup() {
    return _writeToDb;
  }

  @Override
  public TableRowInterfaces.RowResult<AnalysisRow> processRecord(AnalysisRow nextRow) throws Exception {
    String descriptor = nextRow.getDescriptor().toString();

    // Find all variable IDs.
    final Set<String> currentVarIds = VAR_ID_PATTERN.matcher(descriptor).results()
        .map(match -> match.group(1))
        .collect(Collectors.toSet());

    // Replace all variable IDs with value converted from legacy variable ID.
    for (String currentVarId: currentVarIds) {
      descriptor = descriptor.replaceAll(currentVarId, fixupId(currentVarId));
    }

    // Create a copy with just the dataset ID updated to VDI counterpart.
    nextRow.setDescriptor(new JSONObject(descriptor));

    LOG.info("Analysis descriptor after migration: " + descriptor);

    return new TableRowInterfaces.RowResult<>(nextRow)
        .setShouldWrite(_writeToDb);
  }

  private Map<String, String> readFixedUpMapping(Path mappingFile) throws IOException {
    return Files.readAllLines(mappingFile).stream()
        .map(line -> line.split(","))
        .collect(Collectors.toMap(tokens -> tokens[0], tokens -> tokens[1]));
  }

  private String fixupId(String currentVarId) {
    return this.fixedUpMapping.getOrDefault(currentVarId, currentVarId);
  }

  @Override
  public void dumpStatistics() {

  }
}
