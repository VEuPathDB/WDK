package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class VDIMigrationPluginTest {

  private ClassLoader classLoader;
  private VDIEntityIdRetriever retriever;

  @Before
  public void setup() {
    classLoader = getClass().getClassLoader();
    retriever = Mockito.mock(VDIEntityIdRetriever.class);
  }

  @Test
  public void testUpdateEnabled() throws Exception {
    File analysisFile = new File(Objects.requireNonNull(classLoader.getResource("analysis-unit-test-1.json")).getFile());
    JSONObject descriptor = new JSONObject(Files.readString(Path.of(analysisFile.getPath())));
    final File file = new File(Objects.requireNonNull(classLoader.getResource("migration-unit-test-1.json")).getFile());
    final VDIMigrationPlugin migrationPlugin = new VDIMigrationPlugin();
    Mockito.when(retriever.queryEntityId("EDAUD_123XyZ")).thenReturn(Optional.of("asdf"));
    migrationPlugin.readVdiMappingFile(file);
    migrationPlugin.setEntityIdRetriever(retriever);
    TableRowInterfaces.RowResult<AnalysisRow> result = migrationPlugin.processRecord(
        new AnalysisRow("x",
            "EDAUD_1234",
            descriptor.getJSONObject("descriptor"),
            3,
            4,
            5));
    Assert.assertEquals("EDAUD_123XyZ", result.getRow().getDatasetId());
    Assert.assertTrue(result.getRow().getDescriptor().toString().contains("VAR_c73e53adb951e2fe"));
    Assert.assertFalse(result.shouldWrite());
  }

  @Test
  public void testUpdateDisabled() throws Exception {
    File analysisFile = new File(Objects.requireNonNull(classLoader.getResource("analysis-unit-test-1.json")).getFile());
    JSONObject descriptor = new JSONObject(Files.readString(Path.of(analysisFile.getPath())));
    final File file = new File(Objects.requireNonNull(classLoader.getResource("migration-unit-test-1.json")).getFile());
    final VDIMigrationPlugin migrationPlugin = new VDIMigrationPlugin();
    Mockito.when(retriever.queryEntityId("EDAUD_123XyZ")).thenReturn(Optional.of("asdf"));
    migrationPlugin.readVdiMappingFile(file);
    migrationPlugin.setEntityIdRetriever(retriever);
    TableRowInterfaces.RowResult<AnalysisRow> result = migrationPlugin.processRecord(
        new AnalysisRow("x",
            "EDAUD_1234",
            descriptor.getJSONObject("descriptor"),
            3,
            4,
            5));
    Assert.assertEquals("EDAUD_123XyZ", result.getRow().getDatasetId());
    Assert.assertFalse(result.shouldWrite());
  }
}
