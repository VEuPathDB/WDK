package org.gusdb.wdk.model.fix.table.edaanalysis.plugins;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.fix.table.TableRowInterfaces;
import org.gusdb.wdk.model.fix.table.edaanalysis.AnalysisRow;
import org.hamcrest.MatcherAssert;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class VDIMigrationPluginTest {
  private WdkModel mockedModel;
  private ClassLoader classLoader;
  private VDIEntityIdRetriever retriever;

  @Before
  public void setup() {
    classLoader = getClass().getClassLoader();
    mockedModel = Mockito.mock(WdkModel.class);
    retriever = Mockito.mock(VDIEntityIdRetriever.class);
  }

  @Test
  public void test() throws Exception {
    File analysisFile = new File(Objects.requireNonNull(classLoader.getResource("analysis-unit-test-1.json")).getFile());
    JSONObject descriptor = new JSONObject(Files.readString(Path.of(analysisFile.getPath())));
    final File file = new File(Objects.requireNonNull(classLoader.getResource("migration-unit-test-1.json")).getFile());
    final VDIMigrationPlugin migrationPlugin = new VDIMigrationPlugin();
    final List<String> args = List.of("--tinyDb=" + file.getPath());
    Mockito.when(retriever.queryEntityId("EDAUD_123XyZ")).thenReturn(Optional.of("EDAUD_Migrated_ID"));
    migrationPlugin.configure(mockedModel, args, retriever);
    TableRowInterfaces.RowResult<AnalysisRow> result = migrationPlugin.processRecord(
        new AnalysisRow("x",
            "EDAUD_1234",
            descriptor,
            3,
            4,
            5));
    Assert.assertEquals("EDAUD_123XyZ", result.getRow().getDatasetId());
    Assert.assertTrue(result.getRow().getDescriptor().toString().contains("VAR_c73e53adb951e2fe"));
  }
}
