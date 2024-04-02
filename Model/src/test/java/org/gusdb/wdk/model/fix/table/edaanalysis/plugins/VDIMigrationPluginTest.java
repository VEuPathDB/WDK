package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.List;
import java.util.Objects;

public class VDIMigrationPluginTest {
  private WdkModel mockedModel;
  private ClassLoader classLoader;

  @Before
  public void setup() {
    classLoader = getClass().getClassLoader();
    mockedModel = Mockito.mock(WdkModel.class);
  }

  @Test
  public void testUpdateEnabled() throws Exception {
    final File file = new File(Objects.requireNonNull(classLoader.getResource("migration-unit-test-1.json")).getFile());
    final VDIMigrationPlugin migrationPlugin = new VDIMigrationPlugin();
    final List<String> args = List.of("--tinyDb=" + file.getPath());
    migrationPlugin.configure(mockedModel, args);
    TableRowInterfaces.RowResult<AnalysisRow> result = migrationPlugin.processRecord(
        new AnalysisRow("x",
            "EDAUD_1234",
            new JSONObject(),
            3,
            4,
            5));
    Assert.assertEquals("EDAUD_123XyZ", result.getRow().getDatasetId());
    Assert.assertFalse(result.shouldWrite());
  }

  @Test
  public void testUpdateDisabled() throws Exception {
    final File file = new File(Objects.requireNonNull(classLoader.getResource("migration-unit-test-1.json")).getFile());
    final VDIMigrationPlugin migrationPlugin = new VDIMigrationPlugin();
    final List<String> args = List.of("--tinyDb=" + file.getPath(), "--liveRun");
    migrationPlugin.configure(mockedModel, args);
    TableRowInterfaces.RowResult<AnalysisRow> result = migrationPlugin.processRecord(
        new AnalysisRow("x",
            "EDAUD_1234",
            new JSONObject(),
            3,
            4,
            5));
    Assert.assertEquals("EDAUD_123XyZ", result.getRow().getDatasetId());
    Assert.assertTrue(result.shouldWrite());
  }
}
